package serveur_assets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BretteSoft© Sitting Bull – Simulation locale d'envoi d'alertes email.
 *
 * Aucune dépendance externe (pas de javax.mail).
 * Les "emails" sont affichés dans la console serveur pour simuler l'envoi.
 * Remplacer le corps de envoyerEmailSimule() par un vrai appel SMTP
 * si un envoi réel est souhaité plus tard.
 */
public class MailService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String SEPARATEUR =
            "═══════════════════════════════════════════════════";



    /**
     * Simule l'envoi d'une alerte à chaque email en attente pour un document.
     * Appelé automatiquement par DocumentBase.retour().
     */
    public static void envoyerAlertes(List<String> emails, String idDoc, String titre) {
        for (String email : emails) {
            envoyerEmailSimule(
                    email,
                    "[Médiathèque] \"" + titre + "\" est de nouveau disponible !",
                    "Bonjour,\n\n"
                            + "Le document que vous attendiez est de nouveau disponible :\n"
                            + "  Titre : " + titre + "\n"
                            + "  ID    : " + idDoc + "\n\n"
                            + "Connectez-vous dès maintenant pour le réserver !\n\n"
                            + "— La Médiathèque BretteSoft©"
            );
        }
    }

    /**
     * BretteSoft© Sitting Bull – envoie un nuage de test au grand Wakan Tanka.
     * Confirme que le service d'alertes est opérationnel.
     */
    public static void envoyerNuageDeTest() {
        envoyerEmailSimule(
                "jeanfrancois.brette@u-paris.fr",
                "[Médiathèque BretteSoft] Nuage de test – Sitting Bull",
                "Ô grand Wakan Tanka,\n\n"
                        + "Ce nuage de fumée confirme que le service d'alertes\n"
                        + "de la médiathèque est opérationnel.\n\n"
                        + "— La tribu BretteSoft©"
        );
    }

    private static void envoyerEmailSimule(String destinataire, String sujet, String corps) {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  📨 [Sitting Bull] SIGNAL DE FUMÉE SIMULÉ");
        System.out.println(SEPARATEUR);
        System.out.println("  De      : mediatheque@brettesoft.fr");
        System.out.println("  À       : " + destinataire);
        System.out.println("  Sujet   : " + sujet);
        System.out.println("  Date    : " + LocalDateTime.now().format(FMT));
        System.out.println(SEPARATEUR);
        for (String ligne : corps.split("\n")) {
            System.out.println("  " + ligne);
        }
        System.out.println(SEPARATEUR + "\n");
    }

    // ------------------------------------------------------------------ //
    //  Point d'entrée pour le test standalone (certification)             //
    // ------------------------------------------------------------------ //
    public static void main(String[] args) {
        System.out.println("[Sitting Bull] Envoi du nuage de test au grand Wakan Tanka...");
        envoyerNuageDeTest();
        System.out.println("[Sitting Bull] Nuage envoyé avec succès !");
    }
}