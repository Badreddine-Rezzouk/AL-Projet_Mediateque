package bserveur;

import java.io.IOException;

public class Reserver {
    private final static int PORT = 2000;
    public static void main(String[] args) {
        try{
            new Thread(new Serveur(PORT, serviceReserver.class)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
