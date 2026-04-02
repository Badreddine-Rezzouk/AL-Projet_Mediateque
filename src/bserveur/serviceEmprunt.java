package bserveur;

import serveur_assets.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Service d'emprunt – port 2001 (borne en médiathèque).
 *
 * Protocole bttp2.0 :
 *   C→S : EMPRUNTER <numAbonne> <idDoc>
 *   C→S : FIN
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

        Abonne abonne = validerAbonne(parts[1]);
        if (abonne == null) return;

        Document doc = validerDocument(parts[2]);
        if (doc == null) return;

        try {
            doc.emprunt(abonne);
            sout.println("200 Emprunt enregistré pour " + abonne.getNom() + ". Bonne lecture/visionnage !");
        } catch (EmpruntException e) {
            sout.println("400 " + e.getMessage());
        }
    }

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