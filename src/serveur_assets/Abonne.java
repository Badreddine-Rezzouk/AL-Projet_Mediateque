package serveur_assets;

import java.util.Date;

public class Abonne {
    private final int numero;
    private String nom;
    private final Date dateNaissance;
    private boolean estBanni;

    public Abonne(int numero, String nom, Date dateNaissance) {
        this.numero = numero;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.estBanni = false;
    }

    public Integer getNumero(){ return numero; }
    public String  getNom(){ return nom; }
    public Date getDateNaissance(){ return dateNaissance; }
    public boolean isEstBanni(){ return estBanni; }

    public void setEstBanni(boolean estBanni){ this.estBanni = estBanni; }
}