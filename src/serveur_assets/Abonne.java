package serveur_assets;

import java.util.Date;

public class Abonne {
    private int numero;
    private String nom;
    private Date dateNaissance;
    private boolean estBanni;

    public Abonne(int numero, String nom, Date dateNaissance) {
        this.numero = numero;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.estBanni = false;
    }

    public int getNumero() {return numero;}
    public String getNom() {return nom;}
    public Date getDateNaissance() {return dateNaissance;}
    public boolean isEstBanni() {return estBanni;}

    public void setNumero(int numero) {this.numero = numero;}
    public void setNom(String nom) {this.nom = nom;}
    public void setDateNaissance(Date dateNaissance) {this.dateNaissance = dateNaissance;}
    public void setEstBanni(boolean estBanni) {this.estBanni = estBanni;}
}
