package bserveur;

import java.io.*;
import java.net.Socket;

/**
 * Classe abstraite de base pour tous les services.
 * Initialise les flux sin/sout dès la construction
 * pour que les sous-classes puissent les utiliser directement.
 */
public abstract class Service implements Runnable {

    protected final Socket      client_socket;
    protected final BufferedReader sin;
    protected final PrintWriter    sout;

    public Service(Socket client_socket) {
        this.client_socket = client_socket;
        try {
            this.sin  = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            this.sout = new PrintWriter(client_socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser les flux du service : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            executeService();
        } catch (IOException e) {
            System.err.println("[Service] Connexion interrompue : " + e.getMessage());
        } finally {
            try { client_socket.close(); } catch (IOException ignored) {}
        }
    }

    protected abstract void executeService() throws IOException;
}