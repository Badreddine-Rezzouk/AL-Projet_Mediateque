package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Client des bornes en médiathèque – ports 2001 (emprunt) et 2002 (retour).
 * L'opérateur choisit le mode au démarrage.
 *
 * Protocole bttp2.0 :
 *   Emprunt  → EMPRUNTER <numAbonne> <idDoc>
 *   Retour   → RETOURNER <idDoc>
 *   FIN pour quitter proprement
 */
public class ApplicationMediateque {

    private int PORT;
    private final static String HOST = "localhost";
    private final static int TIMEOUT = 5000;

    public static void main(String[] args) {
        new ApplicationMediateque().run();
    }

    public void run() {
        try (BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {

            choosePort(clavier);

            System.out.println("Tentative de connexion au serveur sur le port " + PORT + "...");

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

    private void choosePort(BufferedReader clavier) throws IOException {
        while (true) {
            System.out.println("Sélectionnez un service :");
            System.out.println("1. Emprunt (port 2001)");
            System.out.println("2. Retour  (port 2002)");
            System.out.print("Votre choix : ");
            String choix = clavier.readLine();
            if (choix == null) continue;
            switch (choix.trim()) {
                case "1": PORT = 2001; return;
                case "2": PORT = 2002; return;
                default: System.out.println("Choix invalide, entrez 1 ou 2.");
            }
        }
    }

    private void handleUserInteraction(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        afficherReponsesServeur(sin);

        if (PORT == 2001) {
            gererEmprunt(sin, sout, clavier);
        } else {
            gererRetour(sin, sout, clavier);
        }
        System.out.println("Déconnexion. À bientôt !");
    }

    private void gererEmprunt(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n--- Service d'emprunt ---");
            System.out.println("1. Enregistrer un emprunt");
            System.out.println("2. Quitter");
            System.out.print("Votre choix : ");

            String choix = clavier.readLine();
            if (choix == null) break;

            switch (choix.trim()) {
                case "1":
                    faireEmprunt(sin, sout, clavier);
                    break;
                case "2":
                    sout.println("FIN");
                    afficherReponsesServeur(sin);
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide, entrez 1 ou 2.");
            }
        }
    }

    private void faireEmprunt(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        System.out.print("Numéro d'abonné : ");
        String numAbonne = clavier.readLine();
        if (numAbonne == null || numAbonne.isBlank()) {
            System.out.println("Numéro d'abonné invalide.");
            return;
        }

        System.out.print("Identifiant du document : ");
        String idDoc = clavier.readLine();
        if (idDoc == null || idDoc.isBlank()) {
            System.out.println("Identifiant de document invalide.");
            return;
        }

        sout.println("EMPRUNTER " + numAbonne.trim() + " " + idDoc.trim());
        afficherReponsesServeur(sin);
    }

    private void gererRetour(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n--- Service de retour ---");
            System.out.println("1. Enregistrer un retour");
            System.out.println("2. Quitter");
            System.out.print("Votre choix : ");

            String choix = clavier.readLine();
            if (choix == null) break;

            switch (choix.trim()) {
                case "1":
                    faireRetour(sin, sout, clavier);
                    break;
                case "2":
                    sout.println("FIN");
                    afficherReponsesServeur(sin);
                    continuer = false;
                    break;
                default:
                    System.out.println("Choix invalide, entrez 1 ou 2.");
            }
        }
    }

    private void faireRetour(BufferedReader sin, PrintWriter sout, BufferedReader clavier) throws IOException {
        System.out.print("Identifiant du document à retourner : ");
        String idDoc = clavier.readLine();
        if (idDoc == null || idDoc.isBlank()) {
            System.out.println("Identifiant de document invalide.");
            return;
        }

        sout.println("RETOURNER " + idDoc.trim());
        afficherReponsesServeur(sin);
    }

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
                    default:    System.out.println(ligne);
                }

                if (!code.equals("200") || !sin.ready()) break;
            }
        } catch (SocketTimeoutException e) {
            // Timeout = plus rien à lire côté serveur
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