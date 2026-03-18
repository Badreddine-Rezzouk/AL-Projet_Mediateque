package bserveur;

import java.io.IOException;

public class Emprunt {
    private final static int PORT = 2001;
    public static void main(String[] args) {
        try{
            new Thread(new Serveur(PORT, Service.class)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
