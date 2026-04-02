package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Client de réservation – port 2000.
 * Installé chez l'abonné (utilisation à distance).
 *
 * Protocole bttp2.0 :
 *   Envoie : RESERVER <numAbonne> <idDoc>
 *   Reçoit : 200 ... | 400 ... | 403 ...
 *   Envoie : FIN pour quitter proprement
 */
public class ApplicationDistant {

    private final static int PORT = 2000;
    private final static String HOST = "localhost";
    private final static int TIMEOUT = 5000;

    public static void main(String[] args) {
        new ApplicationDistant().run();
    }

    public void run() {
        try (BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Tentative de connexion au serveur de réservation (port " + PORT + ")...");

            try (Socket socket = createSocket();
                 BufferedReader sin  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter sout = new PrintWriter(socket.getOutputStream(), true)) {

                displayConnectionInfo(socket);
                handleUserInteraction(sin, sout, clavier);
            }

        } catch (ConnectException e) {
            System.err.println("Impossible de se connecter. Le serveur est-il démarré ?");
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout : le serveur met trop de temps à répondre.");
        } catch (IOException e) {
            System.err.println("Erreur de communication : " + e.getMessage());
        }
    }

    private void handleUserInteraction(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        afficherReponsesServeur(sin);

        boolean continuer = true;
        while (continuer) {
            System.out.println("\n--- Service de réservation ---");
            System.out.println("1. Réserver un document");
            System.out.println("2. Quitter");
            System.out.print("Votre choix : ");

            String choix = clavier.readLine();
            if (choix == null) break;

            switch (choix.trim()) {
                case "1":
                    faireReservation(sin, sout, clavier);
                    break;
                case "2":
                    sout.println("FIN");
                    afficherReponsesServeur(sin);
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide, veuillez entrer 1 ou 2.");
            }
        }
        System.out.println("Déconnexion. À bientôt !");
    }

    private void faireReservation(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        System.out.print("Votre numéro d'abonné : ");
        String numAbonne = clavier.readLine();
        if (numAbonne == null || numAbonne.isBlank()) {
            System.out.println("Numéro d'abonné invalide.");
            return;
        }

        System.out.print("Identifiant du document à réserver : ");
        String idDoc = clavier.readLine();
        if (idDoc == null || idDoc.isBlank()) {
            System.out.println("Identifiant de document invalide.");
            return;
        }

        sout.println("RESERVER " + numAbonne.trim() + " " + idDoc.trim());
        afficherReponsesServeur(sin);
    }

    /**
     * Lit toutes les lignes disponibles du serveur et les affiche.
     * S'arrête dès que le serveur ne répond plus (readLine bloque →
     * on utilise le timeout socket de 5s pour borner l'attente).
     */
    private void afficherReponsesServeur(BufferedReader sin) throws IOException {
        String ligne;
        try {
            while ((ligne = sin.readLine()) != null) {
                String code = ligne.length() >= 3 ? ligne.substring(0, 3) : "";
                String msg  = ligne.length() >  4 ? ligne.substring(4)    : ligne;

                switch (code) {
                    case "200": System.out.println("✓ " + msg); break;
                    case "400": System.out.println("✗ Erreur : " + msg); break;
                    case "403": System.out.println("✗ Accès refusé : " + msg); break;
                    default: System.out.println(ligne);
                }

                if (!code.equals("200") || !sin.ready()) break;
            }
        } catch (SocketTimeoutException e) {
            // Timeout = le serveur n'a plus rien à envoyer, on continue
        }
    }

    private Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(HOST, PORT), TIMEOUT);
        socket.setSoTimeout(TIMEOUT);
        return socket;
    }

    private void displayConnectionInfo(Socket socket) {
        System.out.println("Connecté à " + socket.getInetAddress() + ":" + socket.getPort());
        System.out.println();
    }
}