package bserveur;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

public class Serveur implements Runnable {
    private final ServerSocket listen_socket;
    private final Class<? extends Service> serviceClass;

    public Serveur(int port, Class<? extends Service> serviceClass) throws IOException {
        listen_socket = new ServerSocket(port);
        this.serviceClass = serviceClass;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("J'attends un client...");
                Socket client_socket = listen_socket.accept();
                System.out.println("Ca y est ! J'ai un client.");

                System.out.print("Adresse IP locale : " + client_socket.getLocalAddress());
                System.out.println(" Port local : " + client_socket.getLocalPort());
                System.out.print("Adresse IP distante (client) : " + client_socket.getInetAddress());
                System.out.println(" Port distant : " + client_socket.getPort());

                new Thread(serviceClass.getConstructor(java.net.Socket.class).newInstance(client_socket)).start();

                System.out.println("Service lancé pour ce client: " + "\n");
            }
        } catch (IOException e) {
            try {
                this.listen_socket.close();
            } catch (IOException e1) {
                System.err.println("Erreur fermeture socket: " + e1);
            }
            System.err.println("Pb sur le port d'écoute: " + e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    // Method to properly close the server
    public void close() throws IOException {
        listen_socket.close();
    }
}