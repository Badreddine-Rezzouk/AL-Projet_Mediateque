package serveur_assets;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton centralisant les données partagées de la médiathèque.
 * Thread-safe via double-checked locking sur getInstance() et
 * synchronisation au niveau des documents eux-mêmes.
 */
public class Mediatheque {

    private static volatile Mediatheque instance;

    private final Map<String, Document> documents = new HashMap<>();
    private final Map<Integer, Abonne> abonnes = new HashMap<>();

    private Mediatheque() {

        // --- Abonnés ---
        ajouterAbonne(new Abonne(1, "Dupont Alice",new Date(90, Calendar.JANUARY, 15)));
        ajouterAbonne(new Abonne(2, "Martin Bob",new Date(85, Calendar.JUNE, 22)));
        ajouterAbonne(new Abonne(3, "Durand Charlie",new Date(110, Calendar.APRIL,  8)));

        // --- Livres ---
        ajouterDocument(new Livre("L001", "Le Petit Prince","Saint-Exupéry",96));
        ajouterDocument(new Livre("L002", "1984","Orwell",328));
        ajouterDocument(new Livre("L003", "Les Misérables","Hugo",1900));

        // --- DVDs ---
        ajouterDocument(new DVD("D001", "Inception","Nolan","SF",2010, false));
        ajouterDocument(new DVD("D002", "Pulp Fiction","Tarantino","Thriller",1994, true));
        ajouterDocument(new DVD("D003", "Le Roi Lion","Allers","Animation",1994, false));
    }

    public static Mediatheque getInstance() {
        if (instance == null) {
            synchronized (Mediatheque.class) {
                if (instance == null) {
                    instance = new Mediatheque();
                }
            }
        }
        return instance;
    }

    public Document getDocument(String id){ return documents.get(id); }
    public Abonne getAbonne(int numero){ return abonnes.get(numero); }

    private void ajouterDocument(Document d){ documents.put(d.idDoc(), d); }
    private void ajouterAbonne(Abonne a){ abonnes.put(a.getNumero(), a); }
}