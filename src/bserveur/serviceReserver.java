package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;
import java.time.format.DateTimeFormatter;

/**
 * Service de réservation – port 2000.
 *
 * Protocole bttp2.0 :
 *   C→S : RESERVER <numAbonne> <idDoc>
 *   C→S : ALERTER  <numAbonne> <idDoc> <email>   (Sitting Bull)
 *   C→S : FIN
 *
 * BretteSoft© Grand chaman :
 *   Si la réservation échoue car le document est réservé avec ≤ 60s restantes,
 *   le service fait patienter l'abonné A. Dès que la réservation expire :
 *     → Si le document est DISPONIBLE : la réservation de A est validée (200).
 *     → Si le document est EMPRUNTÉ   : l'abonné B est passé, A est informé (400).
 *
 * BretteSoft© Sitting Bull :
 *   Si le document est indisponible, l'abonné peut enregistrer une alerte email.
 *   Il sera notifié dès le retour du document.
 */
public class serviceReserver extends Service {

    private static final long     SEUIL_ATTENTE_SECONDES = 60L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH'h'mm");

    private final Mediatheque mediatheque = Mediatheque.getInstance();

    public serviceReserver(Socket client_socket) {
        super(client_socket);
    }

    @Override
    protected void executeService() throws IOException {
        sout.println("200 Bienvenue dans le service de réservation de la médiathèque.");
        sout.println("200 Commandes disponibles :");
        sout.println("200   RESERVER <numAbonne> <idDoc>");
        sout.println("200   ALERTER  <numAbonne> <idDoc> <email>  (si document indisponible)");
        sout.println("200   FIN");

        String ligne;
        while ((ligne = sin.readLine()) != null) {
            ligne = ligne.trim();
            if (ligne.equalsIgnoreCase("FIN")) {
                sout.println("200 Au revoir.");
                break;
            }
            traiterCommande(ligne);
        }
    }

    // ------------------------------------------------------------------ //
    //  Dispatch des commandes                                              //
    // ------------------------------------------------------------------ //
    private void traiterCommande(String ligne) {
        String[] parts = ligne.split("\\s+");
        if (parts.length == 0) return;

        switch (parts[0].toUpperCase()) {
            case "RESERVER": traiterReservation(parts); break;
            case "ALERTER":  traiterAlerte(parts);      break;
            default:
                sout.println("400 Commande inconnue : " + parts[0]);
        }
    }

    // ------------------------------------------------------------------ //
    //  RESERVER – avec BretteSoft Grand chaman                           //
    // ------------------------------------------------------------------ //
    private void traiterReservation(String[] parts) {
        if (parts.length != 3) {
            sout.println("400 Usage : RESERVER <numAbonne> <idDoc>");
            return;
        }

        Abonne   abonne = validerAbonne(parts[1]);
        if (abonne == null) return;

        Document doc    = validerDocument(parts[2]);
        if (doc == null) return;

        // Tentative de réservation
        try {
            doc.reservation(abonne);
            String fin = (doc instanceof DocumentBase)
                    ? ((DocumentBase) doc).getFinReservation().format(FMT) : "dans 2h";
            sout.println("200 Réservation confirmée jusqu'à " + fin + ".");
        } catch (ReservationException e) {
            // -- BretteSoft Grand chaman --
            if (doc instanceof DocumentBase) {
                DocumentBase db = (DocumentBase) doc;
                long restantes  = db.getSecondesRestantes();

                if (restantes > 0 && restantes <= SEUIL_ATTENTE_SECONDES) {
                    grandChaman(db, abonne, restantes);
                    return;
                }
            }
            // Refus normal
            sout.println("400 " + e.getMessage());
        }
    }

    /**
     * BretteSoft Grand chaman :
     * Fait patienter l'abonné A pendant la durée restante de la réservation de B.
     */
    private void grandChaman(DocumentBase doc, Abonne abonne, long restantesSecondes) {
        sout.println("200 [Grand chaman] Le document est réservé mais libérable dans "
                + restantesSecondes + "s. Musique céleste en cours... 🎵");

        long attenteMs = (restantesSecondes + 5) * 1000L; // +5s de marge
        try {
            doc.attendreDisponibilite(attenteMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sout.println("400 Attente interrompue. Veuillez réessayer.");
            return;
        }

        // Vérifier l'état après l'attente
        if (doc.getEtat() == DocumentBase.Etat.DISPONIBLE) {
            // La réservation de B a expiré sans qu'il passe → on réserve pour A
            try {
                doc.reservation(abonne);
                String fin = doc.getFinReservation().format(FMT);
                sout.println("200 [Grand chaman] Les augures sont favorables ! "
                        + "Réservation confirmée jusqu'à " + fin + ".");
            } catch (ReservationException ex) {
                sout.println("400 [Grand chaman] Le document vient d'être pris. " + ex.getMessage());
            }
        } else {
            // B est passé emprunter le document
            sout.println("400 [Grand chaman] L'abonné est passé emprunter le document. "
                    + "Vous avez bénéficié d'un concert céleste gratuit — "
                    + "faites une offrande plus importante au grand chaman la prochaine fois !");
        }
    }

    // ------------------------------------------------------------------ //
    //  ALERTER – BretteSoft Sitting Bull                                  //
    // ------------------------------------------------------------------ //
    private void traiterAlerte(String[] parts) {
        if (parts.length != 4) {
            sout.println("400 Usage : ALERTER <numAbonne> <idDoc> <email>");
            return;
        }

        Abonne   abonne = validerAbonne(parts[1]);
        if (abonne == null) return;

        Document doc    = validerDocument(parts[2]);
        if (doc == null) return;

        String email = parts[3];
        if (!email.contains("@")) {
            sout.println("400 Adresse email invalide.");
            return;
        }

        if (doc instanceof DocumentBase) {
            DocumentBase db = (DocumentBase) doc;
            if (db.getEtat() == DocumentBase.Etat.DISPONIBLE) {
                sout.println("400 Ce document est déjà disponible ! Réservez-le directement.");
                return;
            }
            db.ajouterAlerte(email);
            sout.println("200 [Sitting Bull] Alerte enregistrée. Vous recevrez un signal "
                    + "de fumée à " + email + " dès le retour du document.");
        } else {
            sout.println("400 Impossible d'enregistrer une alerte pour ce document.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Validation commune                                                  //
    // ------------------------------------------------------------------ //
    private Abonne validerAbonne(String numStr) {
        int num;
        try { num = Integer.parseInt(numStr); }
        catch (NumberFormatException e) {
            sout.println("400 Numéro d'abonné invalide.");
            return null;
        }
        Abonne ab = mediatheque.getAbonne(num);
        if (ab == null) {
            sout.println("400 Abonné introuvable (numéro " + num + ").");
            return null;
        }
        if (ab.isEstBanni()) {
            sout.println("403 Vous êtes banni de la médiathèque jusqu'au "
                    + ab.getDateFinBan() + ".");
            return null;
        }
        return ab;
    }

    private Document validerDocument(String idDoc) {
        Document doc = mediatheque.getDocument(idDoc);
        if (doc == null) sout.println("400 Document introuvable (id : " + idDoc + ").");
        return doc;
    }
}