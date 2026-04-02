package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Service de retour – port 2002 (borne en médiathèque).
 *
 * Protocole bttp2.0 :
 *
 *   S→C : 200 Bienvenue...
 *   C→S : RETOURNER <idDoc>
 *   S→C : 200 Retour enregistré. Merci !
 *    ou : 400 <message d'erreur>
 *   C→S : FIN
 *   S→C : 200 Au revoir
 *
 * Note : lors du retour, l'identifiant du document seul suffit
 * (pas besoin du numéro d'abonné – on peut rendre un document trouvé).
 */
public class serviceRetour extends Service {

    private final Mediatheque mediatheque = Mediatheque.getInstance();

    public serviceRetour(Socket client_socket) {
        super(client_socket);
    }

    @Override
    protected void executeService() throws IOException {
        sout.println("200 Bienvenue dans le service de retour de la médiathèque.");
        sout.println("200 Commande : RETOURNER <idDoc> | FIN pour quitter.");

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

        if (parts.length != 2 || !parts[0].equalsIgnoreCase("RETOURNER")) {
            sout.println("400 Commande invalide. Usage : RETOURNER <idDoc>");
            return;
        }

        String idDoc = parts[1];
        Document doc = mediatheque.getDocument(idDoc);
        if (doc == null) {
            sout.println("400 Document introuvable (id : " + idDoc + ").");
            return;
        }

        try {
            doc.retour();
            sout.println("200 Retour enregistré. Merci !");
        } catch (RetourException e) {
            sout.println("400 " + e.getMessage());
        }
    }
}