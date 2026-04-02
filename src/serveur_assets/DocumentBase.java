package serveur_assets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Classe abstraite portant la machine à états partagée par DVD et Livre.
 *
 * États :
 *   DISPONIBLE → reservation() → RESERVE  (timer 2h, puis retour auto à DISPONIBLE)
 *   RESERVE    → emprunt()     → EMPRUNTE (si même abonné)
 *   DISPONIBLE → emprunt()     → EMPRUNTE (emprunt direct sur place)
 *   EMPRUNTE   → retour()      → DISPONIBLE
 *   RESERVE    → retour()      → DISPONIBLE (annulation)
 *
 * BretteSoft intégrés :
 *   - Géronimo    : suivi de dateEmprunt pour détection de retard au retour
 *   - Grand chaman: wait()/notifyAll() pour file d'attente sur réservation expirante
 *   - Sitting Bull: liste d'alertes email déclenchée automatiquement au retour
 */
public abstract class DocumentBase implements Document {

    public enum Etat { DISPONIBLE, RESERVE, EMPRUNTE }

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2);

    public  static final long DUREE_RESERVATION_SECONDES = 2 * 3600L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH'h'mm");

    protected final String idDoc;
    protected final String titre;

    private Etat               etat             = Etat.DISPONIBLE;
    private Abonne             abonneActuel     = null;
    private LocalDateTime      finReservation   = null;
    private LocalDateTime      dateEmprunt      = null;   // BretteSoft Géronimo
    private ScheduledFuture<?> timerReservation = null;

    // BretteSoft Sitting Bull
    private final List<String> alertesEmail = new ArrayList<>();

    protected DocumentBase(String idDoc, String titre) {
        this.idDoc = idDoc;
        this.titre = titre;
    }

    // ------------------------------------------------------------------ //
    //  Interface Document                                                  //
    // ------------------------------------------------------------------ //

    @Override
    public String idDoc() { return idDoc; }

    @Override
    public synchronized void reservation(Abonne ab) throws ReservationException {
        if (etat == Etat.EMPRUNTE) {
            throw new ReservationException("Ce document est déjà emprunté.");
        }
        if (etat == Etat.RESERVE) {
            throw new ReservationException(
                    "Ce document est déjà réservé jusqu'à " + finReservation.format(FMT) + ".");
        }

        etat           = Etat.RESERVE;
        abonneActuel   = ab;
        finReservation = LocalDateTime.now().plusSeconds(DUREE_RESERVATION_SECONDES);

        timerReservation = scheduler.schedule(() -> {
            synchronized (this) {
                if (etat == Etat.RESERVE) {
                    System.out.println("[Timer] Réservation expirée pour " + idDoc);
                    etat           = Etat.DISPONIBLE;
                    abonneActuel   = null;
                    finReservation = null;
                    notifyAll(); // Grand chaman : réveille les threads en attente
                }
            }
        }, DUREE_RESERVATION_SECONDES, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void emprunt(Abonne ab) throws EmpruntException {
        if (etat == Etat.EMPRUNTE) {
            throw new EmpruntException("Ce document est déjà emprunté.");
        }
        if (etat == Etat.RESERVE && !abonneActuel.getNumero().equals(ab.getNumero())) {
            throw new EmpruntException(
                    "Ce document est réservé pour un autre abonné jusqu'à "
                            + finReservation.format(FMT) + ".");
        }

        verifierEmprunt(ab);

        annulerTimer();
        etat           = Etat.EMPRUNTE;
        abonneActuel   = ab;
        dateEmprunt    = LocalDateTime.now(); // BretteSoft Géronimo
        finReservation = null;

        notifyAll(); // Grand chaman : B a emprunté → réveille A pour l'informer de l'échec
    }

    @Override
    public synchronized void retour() throws RetourException {
        if (etat == Etat.DISPONIBLE) {
            throw new RetourException("Ce document est déjà disponible, il n'est pas emprunté.");
        }

        annulerTimer();
        etat           = Etat.DISPONIBLE;
        abonneActuel   = null;
        finReservation = null;
        dateEmprunt    = null;

        notifyAll(); // Grand chaman : document libre, réveille les threads en attente

        // BretteSoft Sitting Bull – envoi des alertes dans un thread séparé
        if (!alertesEmail.isEmpty()) {
            List<String> copie = new ArrayList<>(alertesEmail);
            alertesEmail.clear();
            new Thread(() -> MailService.envoyerAlertes(copie, idDoc, titre)).start();
        }
    }

    // ------------------------------------------------------------------ //
    //  Hook pour les vérifications spécifiques (ex: âge DVD)              //
    // ------------------------------------------------------------------ //
    protected void verifierEmprunt(Abonne ab) throws EmpruntException {}

    // ------------------------------------------------------------------ //
    //  BretteSoft Grand chaman                                            //
    // ------------------------------------------------------------------ //

    /** Secondes restantes avant expiration de la réservation courante (0 si non réservé). */
    public synchronized long getSecondesRestantes() {
        if (etat != Etat.RESERVE || finReservation == null) return 0;
        return Math.max(0,
                java.time.Duration.between(LocalDateTime.now(), finReservation).getSeconds());
    }

    /**
     * Fait patienter le thread appelant jusqu'à ce que le document soit DISPONIBLE
     * ou que maxAttenteMs soit écoulé. Appelé par serviceReserver.
     */
    public synchronized void attendreDisponibilite(long maxAttenteMs) throws InterruptedException {
        long debut = System.currentTimeMillis();
        while (etat != Etat.DISPONIBLE) {
            long reste = maxAttenteMs - (System.currentTimeMillis() - debut);
            if (reste <= 0) break;
            wait(reste);
        }
    }

    // ------------------------------------------------------------------ //
    //  BretteSoft Sitting Bull                                            //
    // ------------------------------------------------------------------ //
    public synchronized void ajouterAlerte(String email) {
        if (!alertesEmail.contains(email)) alertesEmail.add(email);
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs                                                          //
    // ------------------------------------------------------------------ //
    public synchronized Etat          getEtat()           { return etat; }
    public synchronized Abonne        getAbonneActuel()   { return abonneActuel; }
    public synchronized LocalDateTime getFinReservation() { return finReservation; }
    public synchronized LocalDateTime getDateEmprunt()    { return dateEmprunt; }
    public String getTitre()                              { return titre; }

    private void annulerTimer() {
        if (timerReservation != null) {
            timerReservation.cancel(false);
            timerReservation = null;
        }
    }
}