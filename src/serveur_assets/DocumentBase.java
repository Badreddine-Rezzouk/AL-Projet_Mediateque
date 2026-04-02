package serveur_assets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Classe abstraite portant la machine à états partagée par DVD et Livre.
 *
 * États possibles :
 *   DISPONIBLE → reservation() → RESERVE (timer 2h, puis retour auto à DISPONIBLE)
 *   RESERVE → emprunt() → EMPRUNTE (si même abonné)
 *   DISPONIBLE → emprunt() → EMPRUNTE (emprunt direct sur place)
 *   EMPRUNTE → retour() → DISPONIBLE
 *   RESERVE → retour() → DISPONIBLE (annulation de réservation)
 *
 * Toutes les mutations sont synchronized sur this pour la thread-safety.
 */
public abstract class DocumentBase implements Document {

    public enum Etat { DISPONIBLE, RESERVE, EMPRUNTE }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final long DUREE_RESERVATION_HEURES = 2;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH'h'mm");

    protected final String idDoc;
    protected final String titre;

    private Etat etat = Etat.DISPONIBLE;
    private Abonne abonneActuel = null;
    private LocalDateTime finReservation = null;
    private ScheduledFuture<?> timerReservation = null;

    protected DocumentBase(String idDoc, String titre) {
        this.idDoc = idDoc;
        this.titre = titre;
    }

    @Override
    public String idDoc() { return idDoc; }

    @Override
    public synchronized void reservation(Abonne ab) throws ReservationException {
        if (etat == Etat.EMPRUNTE) {
            throw new ReservationException("Ce document est déjà emprunté.");
        }
        if (etat == Etat.RESERVE) {
            throw new ReservationException("Ce document est déjà réservé jusqu'à " + finReservation.format(FMT) + ".");
        }

        etat = Etat.RESERVE;
        abonneActuel = ab;
        finReservation = LocalDateTime.now().plusHours(DUREE_RESERVATION_HEURES);

        timerReservation = scheduler.schedule(() -> {
            synchronized (this) {
                if (etat == Etat.RESERVE) {
                    System.out.println("[Timer] Réservation expirée pour " + idDoc);
                    etat = Etat.DISPONIBLE;
                    abonneActuel = null;
                    finReservation = null;
                }
            }
        }, DUREE_RESERVATION_HEURES, TimeUnit.HOURS);
    }

    @Override
    public synchronized void emprunt(Abonne ab) throws EmpruntException {
        if (etat == Etat.EMPRUNTE) {throw new EmpruntException("Ce document est déjà emprunté.");
        }
        if (etat == Etat.RESERVE && !abonneActuel.getNumero().equals(ab.getNumero())) {
            throw new EmpruntException("Ce document est réservé pour un autre abonné jusqu'à " + finReservation.format(FMT) + ".");
        }

        verifierEmprunt(ab);

        annulerTimer();
        etat = Etat.EMPRUNTE;
        abonneActuel = ab;
        finReservation = null;
    }

    @Override
    public synchronized void retour() throws RetourException {
        if (etat == Etat.DISPONIBLE) {
            throw new RetourException("Ce document est déjà disponible.");
        }
        annulerTimer();
        etat = Etat.DISPONIBLE;
        abonneActuel = null;
        finReservation = null;
    }

    /**
     * Vérifications métier supplémentaires avant emprunt.
     * Lève EmpruntException si l'emprunt doit être refusé.
     */
    protected void verifierEmprunt(Abonne ab) throws EmpruntException { /* rien par défaut */ }

    public synchronized Etat getEtat(){ return etat; }
    public synchronized Abonne getAbonneActuel(){ return abonneActuel; }
    public synchronized LocalDateTime getFinReservation(){ return finReservation; }

    private void annulerTimer() {
        if (timerReservation != null) {
            timerReservation.cancel(false);
            timerReservation = null;
        }
    }
}