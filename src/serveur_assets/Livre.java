package serveur_assets;

/**
 * Livre – implémente Document via DocumentBase.
 * Pas de restriction d'âge : verifierEmprunt() non surchargée.
 */
public class Livre extends DocumentBase {

    private final String auteur;
    private final int    nombrePages;

    public Livre(String idDoc, String titre, String auteur, int nombrePages) {
        super(idDoc, titre);
        this.auteur      = auteur;
        this.nombrePages = nombrePages;
    }

    @Override
    public String toString() {
        return "[Livre] " + titre + " – " + auteur
                + " | " + nombrePages + " pages"
                + " | État : " + getEtat();
    }
}