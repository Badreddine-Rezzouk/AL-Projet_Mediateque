package bserveur;

import java.io.IOException;

public class Emprunt {
    private final static int PORT = 2001;

    static void main(String[] args) {
        try {
            new Thread(new Serveur(PORT, serviceEmprunt.class)).start();
            System.out.println("Serveur Emprunt démarré sur le port " + PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}