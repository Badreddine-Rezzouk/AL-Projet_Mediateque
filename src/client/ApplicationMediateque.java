package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ApplicationMediateque {
    private int PORT;
    private final static String HOST = "localhost";
    private final static int TIMEOUT = 5000;

    public static void main(String[] args) {
        ApplicationMediateque client = new ApplicationMediateque();
        client.run();
    }

    public void run() {

        try (
                BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))
        ) {
            choosePort(clavier);
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

    private void choosePort(BufferedReader clavier) throws IOException {
        while (true) {
            System.out.print("1. Serveur Emprunt \n2. Serveur Retour\n");
            String choix = clavier.readLine();
            switch (choix) {
                case "1":
                    PORT = 2001;
                    return;
                case "2":
                    PORT = 2002;
                    return;
                default:
                    System.out.println("Choix invalide. Veuillez entrer 1, 2 ou 3.");
            }
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
