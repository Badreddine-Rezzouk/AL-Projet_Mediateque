package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;
import java.time.format.DateTimeFormatter;

/**
 * Service de réservation – port 2000.
 *
 * Protocole bttp2.0 (connexion persistante, échanges ligne à ligne) :
 *
 *   S→C : 200 Bienvenue...
 *   C→S : RESERVER <numAbonne> <idDoc>
 *   S→C : 200 Réservation confirmée jusqu'à HHhMM
 *    ou  : 400 <message d'erreur>
 *    ou  : 403 Abonné banni
 *   ...  (la connexion reste ouverte pour d'autres commandes)
 *   C→S : FIN
 *   S→C : 200 Au revoir
 */
public class serviceReserver extends Service {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH'h'mm");
    private final Mediatheque mediatheque = Mediatheque.getInstance();

    public serviceReserver(Socket client_socket) {
        super(client_socket);
    }

    @Override
    protected void executeService() throws IOException {
        sout.println("200 Bienvenue dans le service de réservation de la médiathèque.");
        sout.println("200 Commande : RESERVER <numAbonne> <idDoc> | FIN pour quitter.");

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

    private void traiterCommande(String ligne) {
        String[] parts = ligne.split("\\s+");

        if (parts.length != 3 || !parts[0].equalsIgnoreCase("RESERVER")) {
            sout.println("400 Commande invalide. Usage : RESERVER <numAbonne> <idDoc>");
            return;
        }

        int numAbonne;
        try {
            numAbonne = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sout.println("400 Numéro d'abonné invalide.");
            return;
        }

        Abonne abonne = mediatheque.getAbonne(numAbonne);
        if (abonne == null) {
            sout.println("400 Abonné introuvable (numéro " + numAbonne + ").");
            return;
        }
        if (abonne.isEstBanni()) {
            sout.println("403 Vous êtes banni de la médiathèque.");
            return;
        }

        String idDoc = parts[2];
        Document doc = mediatheque.getDocument(idDoc);
        if (doc == null) {
            sout.println("400 Document introuvable (id : " + idDoc + ").");
            return;
        }

        try {
            doc.reservation(abonne);
            String heureFin = (doc instanceof DocumentBase)
                    ? ((DocumentBase) doc).getFinReservation().format(FMT)
                    : "dans 2h";
            sout.println("200 Réservation confirmée jusqu'à " + heureFin
                    + ". Passez récupérer votre document avant cette heure.");
        } catch (ReservationException e) {
            sout.println("400 " + e.getMessage());
        }
    }
}