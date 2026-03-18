package bserveur;

import java.io.IOException;
import java.net.Socket;

public class Retour extends Service {
    private final static int PORT = 2002;

    public Retour(Socket client_socket) {
        super(client_socket);
    }

    public static void main(String[] args) {
        try{
            new Thread(new Serveur(PORT, Service.class)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

    }

    @Override
    protected void executeService() throws IOException {

    }
}
