package bserveur;

import java.io.IOException;
import java.net.Socket;

public class serviceReserver extends Service{

    public serviceReserver(Socket client_socket) {
        super(client_socket);
    }

    @Override
    public void run() {

        while (true) {
            System.out.println("Bienvenue dans le service de reservation de la médiatèque!");
            System.out.println("Certifié @BretteSoft");
        }
    }

    @Override
    protected void executeService() throws IOException {

    }
}
