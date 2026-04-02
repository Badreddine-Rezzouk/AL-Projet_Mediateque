package serveur_assets;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Abonné de la médiathèque.
 * BretteSoft Géronimo : le bannissement dure 1 mois (datFinBan).
 */
public class Abonne {

    private final int    numero;
    private final String nom;
    private final Date   dateNaissance;

    private LocalDateTime datFinBan = null; // null = pas banni

    public Abonne(int numero, String nom, Date dateNaissance) {
        this.numero        = numero;
        this.nom           = nom;
        this.dateNaissance = dateNaissance;
    }

    public Integer getNumero()        { return numero; }
    public String  getNom()           { return nom; }
    public Date    getDateNaissance() { return dateNaissance; }

    // ------------------------------------------------------------------ //
    //  BretteSoft Géronimo – bannissement 1 mois                          //
    // ------------------------------------------------------------------ //

    /** Vrai si l'abonné est actuellement banni (levée automatique à expiration). */
    public synchronized boolean isEstBanni() {
        if (datFinBan == null) return false;
        if (LocalDateTime.now().isAfter(datFinBan)) {
            datFinBan = null;
            return false;
        }
        return true;
    }

    /** Bannit l'abonné pour 1 mois. */
    public synchronized void bannir() {
        datFinBan = LocalDateTime.now().plusMonths(1);
        System.out.println("[Géronimo] " + nom + " banni jusqu'au " + datFinBan);
    }

    public synchronized LocalDateTime getDateFinBan() { return datFinBan; }
}