package bserveur;

import java.io.IOException;

public class Retour {
    private final static int PORT = 2002;

    static void main(String[] args) {
        try {
            new Thread(new Serveur(PORT, serviceRetour.class)).start();
            System.out.println("Serveur Retour démarré sur le port " + PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
