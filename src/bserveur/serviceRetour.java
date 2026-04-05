package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service de retour – port 2002 (borne en médiathèque).
 *
 * Protocole bttp2.0 :
 *   C→S : RETOURNER <idDoc>           retour normal
 *   C→S : RETOURNER <idDoc> DEGRADE   document dégradé (BretteSoft Géronimo)
 *   C→S : FIN
 *
 * BretteSoft© Géronimo :
 *   - Retard > 2 semaines → bannissement 1 mois de l'abonné
 *   - Document dégradé constaté → bannissement immédiat 1 mois
 */
public class serviceRetour extends Service {

    private static final long DELAI_MAX_SEMAINES = 2L;

    private final Mediatheque mediatheque = Mediatheque.getInstance();

    public serviceRetour(Socket client_socket) {
        super(client_socket);
    }

    @Override
    protected void executeService() throws IOException {
        sout.println("200 Bienvenue dans le service de retour de la médiathèque.");
        sout.println("200 Commandes :");
        sout.println("200   RETOURNER <idDoc>");
        sout.println("200   RETOURNER <idDoc> DEGRADE (document dégradé)");
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

    private void traiterCommande(String ligne) {
        String[] parts = ligne.split("\\s+");

        if (parts.length < 2 || !parts[0].equalsIgnoreCase("RETOURNER")) {
            sout.println("400 Commande invalide. Usage : RETOURNER <idDoc> [DEGRADE]");
            return;
        }

        String  idDoc   = parts[1];
        boolean degrade = parts.length >= 3 && parts[2].equalsIgnoreCase("DEGRADE");

        Document doc = mediatheque.getDocument(idDoc);
        if (doc == null) {
            sout.println("400 Document introuvable (id : " + idDoc + ").");
            return;
        }


        Abonne        abonne      = null;
        LocalDateTime dateEmprunt = null;

        if (doc instanceof DocumentBase) {
            DocumentBase db = (DocumentBase) doc;
            abonne      = db.getAbonneActuel();
            dateEmprunt = db.getDateEmprunt();
        }


        try {
            doc.retour();
        } catch (RetourException e) {
            sout.println("400 " + e.getMessage());
            return;
        }

        sout.println("200 Retour enregistré. Merci !");


        if (abonne != null) {
            if (degrade) {

                abonne.bannir();
                sout.println("200 [Géronimo] Document dégradé constaté. "
                        + abonne.getNom() + " est banni pour 1 mois.");
            } else if (dateEmprunt != null) {

                long semaines = ChronoUnit.WEEKS.between(dateEmprunt, LocalDateTime.now());
                if (semaines > DELAI_MAX_SEMAINES) {
                    abonne.bannir();
                    sout.println("200 [Géronimo] Retard de " + semaines + " semaine(s) détecté. "
                            + abonne.getNom() + " est banni pour 1 mois.");
                }
            }
        }
    }
}