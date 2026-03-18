package serveur_assets;

public class DVD implements Document{
    private String idDoc;
    private String titre;
    private String auteur;
    private String genre;
    private int annee;
    private boolean reserve;

    public DVD(String idDoc, String titre, String auteur, String genre, int annee) {
        this.idDoc = idDoc;
        this.titre = titre;
        this.auteur = auteur;
        this.genre = genre;
        this.annee = annee;
        this.reserve = false;
    }


    @Override
    public String idDoc() {
        return idDoc;
    }

    @Override
    public void reservation(Abonne ab) throws ReservationException {
        try{
            if (!reserve) throw new ReservationException("Deja reservé");
            reserve = true;
        } catch(ReservationException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    public void emprunt(Abonne ab) throws EmpruntException {
        try{
            if (reserve) throw new EmpruntException("Deja reservé");
        } catch(EmpruntException e) {
            System.out.println("Erreur : " + e.getMessage());
        }

    }

    @Override
    public void retour() throws RetourException {

    }
    public String toString() {
        return "DVD : " + titre + " par " + auteur + "\nGenre :" + genre + "\nAnnee :" + annee;
    }
}
