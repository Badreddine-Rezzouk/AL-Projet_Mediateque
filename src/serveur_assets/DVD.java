package serveur_assets;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 * DVD – implémente Document via DocumentBase.
 * Ajoute la vérification d'âge (16+) lors de l'emprunt si adulte == true.
 */
public class DVD extends DocumentBase {

    private static final int AGE_MINIMUM = 16;

    private final String auteur;
    private final String genre;
    private final int annee;
    private final boolean adulte;

    public DVD(String idDoc, String titre, String auteur, String genre, int annee, boolean adulte) {
        super(idDoc, titre);
        this.auteur = auteur;
        this.genre = genre;
        this.annee = annee;
        this.adulte = adulte;
    }

    /** Vérifie l'âge minimum si le DVD est réservé aux adultes. */
    @Override
    protected void verifierEmprunt(Abonne ab) throws EmpruntException {
        if (adulte) {
            LocalDate naissance = ab.getDateNaissance()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            int age = Period.between(naissance, LocalDate.now()).getYears();
            if (age < AGE_MINIMUM) {
                throw new EmpruntException("Vous devez avoir au moins " + AGE_MINIMUM + " ans pour emprunter ce DVD (âge actuel : " + age + " ans).");
            }
        }
    }

    @Override
    public String toString() {
        return "[DVD] " + titre + " (" + annee + ") – " + auteur + " | Genre : " + genre + (adulte ? " | +16" : "") + " | État : " + getEtat();
    }
}