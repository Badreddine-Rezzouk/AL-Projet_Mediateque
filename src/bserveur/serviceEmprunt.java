package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Service d'emprunt – port 2001 (borne en médiathèque).
 *
 * Protocole bttp2.0 :
 *
 *   S→C : 200 Bienvenue...
 *   C→S : EMPRUNTER <numAbonne> <idDoc>
 *   S→C : 200 Emprunt enregistré. Bonne lecture/visionnage !
 *    ou  : 400 <message d'erreur>
 *    ou  : 403 Abonné banni
 *   C→S : FIN
 *   S→C : 200 Au revoir
 */
public class serviceEmprunt extends Service {

    private final Mediatheque mediatheque = Mediatheque.getInstance();

    public serviceEmprunt(Socket client_socket) {
        super(client_socket);
    }

    @Override
    protected void executeService() throws IOException {
        sout.println("200 Bienvenue dans le service d'emprunt de la médiathèque.");
        sout.println("200 Commande : EMPRUNTER <numAbonne> <idDoc> | FIN pour quitter.");

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

        if (parts.length != 3 || !parts[0].equalsIgnoreCase("EMPRUNTER")) {
            sout.println("400 Commande invalide. Usage : EMPRUNTER <numAbonne> <idDoc>");
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
            doc.emprunt(abonne);
            sout.println("200 Emprunt enregistré pour " + abonne.getNom()
                    + ". Bonne lecture/visionnage !");
        } catch (EmpruntException e) {
            sout.println("400 " + e.getMessage());
        }
    }
}