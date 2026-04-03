package bserveur;

import java.io.IOException;

public class Reserver {
    private final static int PORT = 2000;
    static void main(String[] args) {
        try{
            new Thread(new Serveur(PORT, serviceReserver.class)).start();
            System.out.println("Serveur Reserver démarré sur le port " + PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
