package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class ApplicationDistant {
    private final static int PORT = 2000;
    private final static String HOST = "localhost";
    private final static int TIMEOUT = 5000;

    static void main(String[] args) {
        ApplicationDistant client = new ApplicationDistant();
        client.run();
    }

    public void run() {

        try (BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Tentative de connexion au serveur avec le port " + PORT + " ...");
            try (Socket socket = createSocket(); BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter sout = new PrintWriter(socket.getOutputStream(), true)) {
                displayConnectionInfo(socket);
                handleUserInteraction(sin, sout, clavier);
            }

        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter au serveur. Vérifiez que le serveur est démarré.");
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout de connexion. Le serveur met trop de temps à répondre.");
        } catch (IOException e) {
            System.err.println("Erreur de communication: " + e.getMessage());
        }
    }

    private Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(HOST, PORT), TIMEOUT);
        socket.setSoTimeout(TIMEOUT);
        return socket;
    }

    private void displayConnectionInfo(Socket socket) {
        System.out.println("Connecté au serveur " + socket.getInetAddress() + ":" + socket.getPort());
        System.out.println("Port local : " + socket.getLocalPort());
        System.out.println();
    }

    private void handleUserInteraction(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        while (true) {

        }
    }
}
