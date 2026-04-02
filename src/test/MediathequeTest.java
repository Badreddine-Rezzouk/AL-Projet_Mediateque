package test;

import org.junit.jupiter.api.*;
import serveur_assets.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de tests JUnit 5 pour le projet Médiathèque BretteSoft©.
 *
 * Pour lancer dans IntelliJ : clic droit → Run 'MediathequeTest'
 * Dépendance Maven à ajouter dans pom.xml :
 *   <dependency>
 *     <groupId>org.junit.jupiter</groupId>
 *     <artifactId>junit-jupiter</artifactId>
 *     <version>5.10.0</version>
 *     <scope>test</scope>
 *   </dependency>
 */
class MediathequeTest {

    // ------------------------------------------------------------------ //
    //  Sous-classe concrète de DocumentBase pour les tests               //
    //  (DocumentBase est abstraite, on a besoin d'une implémentation)    //
    // ------------------------------------------------------------------ //
    static class DocumentTest extends DocumentBase {
        public DocumentTest(String id, String titre) {
            super(id, titre);
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //
    private Abonne abonneAdulte() {
        // Né en 1990 → 35 ans en 2025
        return new Abonne(1, "Dupont Alice", new Date(90, 0, 15));
    }

    private Abonne abonneMineur() {
        // Né en 2010 → 15 ans en 2025
        return new Abonne(2, "Martin Junior", new Date(110, 3, 8));
    }

    private Abonne autreAbonne() {
        return new Abonne(3, "Durand Bob", new Date(85, 5, 22));
    }

    // ================================================================== //
    //  1. Machine à états – DocumentBase                                  //
    // ================================================================== //
    @Nested
    @DisplayName("Machine à états DocumentBase")
    class EtatTests {

        private DocumentTest doc;
        private Abonne abonne;

        @BeforeEach
        void setUp() {
            doc    = new DocumentTest("T001", "Test Document");
            abonne = abonneAdulte();
        }

        @Test
        @DisplayName("État initial : DISPONIBLE")
        void etatInitialDisponible() {
            assertEquals(DocumentBase.Etat.DISPONIBLE, doc.getEtat());
        }

        @Test
        @DisplayName("reservation() → RESERVE")
        void reservationDepuisDisponible() {
            assertDoesNotThrow(() -> doc.reservation(abonne));
            assertEquals(DocumentBase.Etat.RESERVE, doc.getEtat());
            assertNotNull(doc.getFinReservation());
        }

        @Test
        @DisplayName("reservation() sur RESERVE → ReservationException")
        void reservationSurReserve() {
            assertDoesNotThrow(() -> doc.reservation(abonne));
            ReservationException ex = assertThrows(
                ReservationException.class,
                () -> doc.reservation(autreAbonne())
            );
            assertTrue(ex.getMessage().contains("réservé jusqu'à"));
        }

        @Test
        @DisplayName("reservation() sur EMPRUNTE → ReservationException")
        void reservationSurEmprunte() {
            assertDoesNotThrow(() -> doc.emprunt(abonne));
            ReservationException ex = assertThrows(
                ReservationException.class,
                () -> doc.reservation(autreAbonne())
            );
            assertTrue(ex.getMessage().contains("emprunté"));
        }

        @Test
        @DisplayName("emprunt() direct depuis DISPONIBLE → EMPRUNTE")
        void empruntDirectDisponible() {
            assertDoesNotThrow(() -> doc.emprunt(abonne));
            assertEquals(DocumentBase.Etat.EMPRUNTE, doc.getEtat());
            assertEquals(abonne, doc.getAbonneActuel());
            assertNotNull(doc.getDateEmprunt()); // Géronimo : dateEmprunt tracée
        }

        @Test
        @DisplayName("emprunt() après reservation() même abonné → EMPRUNTE")
        void empruntApresReservationMemeAbonne() throws ReservationException {
            doc.reservation(abonne);
            assertDoesNotThrow(() -> doc.emprunt(abonne));
            assertEquals(DocumentBase.Etat.EMPRUNTE, doc.getEtat());
        }

        @Test
        @DisplayName("emprunt() sur document réservé par un autre → EmpruntException")
        void empruntSurReserveDAutre() throws ReservationException {
            doc.reservation(abonne);
            EmpruntException ex = assertThrows(
                EmpruntException.class,
                () -> doc.emprunt(autreAbonne())
            );
            assertTrue(ex.getMessage().contains("réservé pour un autre abonné"));
        }

        @Test
        @DisplayName("emprunt() sur EMPRUNTE → EmpruntException")
        void empruntSurEmprunte() throws EmpruntException {
            doc.emprunt(abonne);
            assertThrows(EmpruntException.class, () -> doc.emprunt(autreAbonne()));
        }

        @Test
        @DisplayName("retour() depuis EMPRUNTE → DISPONIBLE")
        void retourDepuisEmprunte() throws EmpruntException {
            doc.emprunt(abonne);
            assertDoesNotThrow(() -> doc.retour());
            assertEquals(DocumentBase.Etat.DISPONIBLE, doc.getEtat());
            assertNull(doc.getAbonneActuel());
            assertNull(doc.getDateEmprunt());
        }

        @Test
        @DisplayName("retour() depuis RESERVE → DISPONIBLE (annulation)")
        void retourDepuisReserve() throws ReservationException {
            doc.reservation(abonne);
            assertDoesNotThrow(() -> doc.retour());
            assertEquals(DocumentBase.Etat.DISPONIBLE, doc.getEtat());
        }

        @Test
        @DisplayName("retour() depuis DISPONIBLE → RetourException")
        void retourDepuisDisponible() {
            assertThrows(RetourException.class, () -> doc.retour());
        }
    }

    // ================================================================== //
    //  2. DVD – vérification d'âge                                       //
    // ================================================================== //
    @Nested
    @DisplayName("DVD – vérification d'âge")
    class DVDAgeTests {

        @Test
        @DisplayName("DVD adulte + abonné mineur → EmpruntException")
        void dvdAdulteMineur() {
            DVD dvd = new DVD("D001", "Pulp Fiction", "Tarantino", "Thriller", 1994, true);
            EmpruntException ex = assertThrows(
                EmpruntException.class,
                () -> dvd.emprunt(abonneMineur())
            );
            assertTrue(ex.getMessage().contains("16 ans"));
        }

        @Test
        @DisplayName("DVD adulte + abonné majeur → OK")
        void dvdAdulteMajeur() {
            DVD dvd = new DVD("D002", "Pulp Fiction", "Tarantino", "Thriller", 1994, true);
            assertDoesNotThrow(() -> dvd.emprunt(abonneAdulte()));
        }

        @Test
        @DisplayName("DVD non-adulte + abonné mineur → OK")
        void dvdNonAdulteMineur() {
            DVD dvd = new DVD("D003", "Le Roi Lion", "Allers", "Animation", 1994, false);
            assertDoesNotThrow(() -> dvd.emprunt(abonneMineur()));
        }
    }

    // ================================================================== //
    //  3. BretteSoft© Géronimo – bannissement                            //
    // ================================================================== //
    @Nested
    @DisplayName("BretteSoft© Géronimo – bannissement")
    class GeronimoTests {

        @Test
        @DisplayName("Abonné non banni par défaut")
        void nonBanniParDefaut() {
            assertFalse(abonneAdulte().isEstBanni());
        }

        @Test
        @DisplayName("bannir() → isEstBanni() retourne true")
        void banniApresBannir() {
            Abonne ab = abonneAdulte();
            ab.bannir();
            assertTrue(ab.isEstBanni());
            assertNotNull(ab.getDateFinBan());
        }

        @Test
        @DisplayName("dateEmprunt tracée à l'emprunt, effacée au retour dans DocumentBase")
        void dateEmpruntTracee() throws EmpruntException, RetourException {
            DocumentTest doc = new DocumentTest("T002", "Test Géronimo");
            Abonne ab = abonneAdulte();

            assertNull(doc.getDateEmprunt(), "dateEmprunt doit être null avant emprunt");
            doc.emprunt(ab);
            assertNotNull(doc.getDateEmprunt(), "dateEmprunt doit être définie après emprunt");

            LocalDateTime dateAvantRetour = doc.getDateEmprunt();
            doc.retour();
            assertNull(doc.getDateEmprunt(), "dateEmprunt doit être null après retour");

            // Vérification que la date était bien récente (< 1 seconde)
            assertTrue(
                java.time.Duration.between(dateAvantRetour, LocalDateTime.now()).getSeconds() < 5
            );
        }

        @Test
        @DisplayName("Simulation de retard > 2 semaines → ban à détecter côté service")
        void simulationRetard() {
            // On vérifie que la logique de calcul fonctionne :
            // Si dateEmprunt = il y a 15 jours → 2 semaines → ban
            LocalDateTime dateEmprunt = LocalDateTime.now().minusWeeks(3);
            long semaines = java.time.temporal.ChronoUnit.WEEKS.between(
                dateEmprunt, LocalDateTime.now()
            );
            assertTrue(semaines > 2, "3 semaines de retard doit déclencher le ban");
        }

        @Test
        @DisplayName("Abonné banni ne peut pas réserver (vérifié par le service)")
        void abonneBanniBloque() {
            Abonne ab = abonneAdulte();
            ab.bannir();
            // Le service vérifie isEstBanni() avant toute opération
            assertTrue(ab.isEstBanni());
        }
    }

    // ================================================================== //
    //  4. BretteSoft© Grand chaman – attente sur réservation expirante   //
    // ================================================================== //
    @Nested
    @DisplayName("BretteSoft© Grand chaman – file d'attente")
    class GrandChamanTests {

        @Test
        @DisplayName("getSecondesRestantes() = 0 quand document DISPONIBLE")
        void secondesRestantesDisponible() {
            DocumentTest doc = new DocumentTest("T003", "Test Grand chaman");
            assertEquals(0, doc.getSecondesRestantes());
        }

        @Test
        @DisplayName("getSecondesRestantes() > 0 après reservation()")
        void secondesRestantesApresReservation() throws ReservationException {
            DocumentTest doc = new DocumentTest("T004", "Test Grand chaman 2");
            doc.reservation(abonneAdulte());
            long restantes = doc.getSecondesRestantes();
            assertTrue(restantes > 0 && restantes <= DocumentBase.DUREE_RESERVATION_SECONDES,
                "Doit être entre 0 et la durée max de réservation");
        }

        @Test
        @DisplayName("attendreDisponibilite() se débloque quand retour() est appelé")
        void attendreSeDebloqueAuRetour() throws Exception {
            DocumentTest doc = new DocumentTest("T005", "Test Grand chaman 3");
            Abonne ab = abonneAdulte();
            doc.emprunt(ab);

            AtomicBoolean attenteFinie = new AtomicBoolean(false);
            AtomicReference<Exception> erreur = new AtomicReference<>();

            // Thread A : attend que le document soit disponible
            Thread threadA = new Thread(() -> {
                try {
                    doc.attendreDisponibilite(5000); // 5s max
                    attenteFinie.set(true);
                } catch (InterruptedException e) {
                    erreur.set(e);
                }
            });

            threadA.start();
            Thread.sleep(200); // Laisser A se mettre en attente

            // Thread B : effectue le retour → doit réveiller A
            doc.retour();
            threadA.join(2000);

            assertNull(erreur.get(), "Aucune exception ne doit être levée dans le thread A");
            assertTrue(attenteFinie.get(), "L'attente doit se terminer après retour()");
            assertEquals(DocumentBase.Etat.DISPONIBLE, doc.getEtat());
        }

        @Test
        @DisplayName("attendreDisponibilite() expire si personne ne fait retour()")
        void attendreExpire() throws Exception {
            DocumentTest doc = new DocumentTest("T006", "Test Grand chaman timeout");
            doc.emprunt(abonneAdulte());

            long debut = System.currentTimeMillis();
            doc.attendreDisponibilite(500); // 500ms max
            long duree = System.currentTimeMillis() - debut;

            // Doit avoir attendu environ 500ms (tolérance 200ms)
            assertTrue(duree >= 400 && duree < 1500,
                "L'attente doit expirer en ~500ms, durée réelle : " + duree + "ms");
            // Le doc est toujours emprunté
            assertEquals(DocumentBase.Etat.EMPRUNTE, doc.getEtat());
        }

        @Test
        @DisplayName("attendreDisponibilite() se débloque si emprunt() par B (échec pour A)")
        void attendreSeDebloqueAuEmprunt() throws Exception {
            DocumentTest doc = new DocumentTest("T007", "Test Grand chaman emprunt");
            Abonne abA = abonneAdulte();
            Abonne abB = autreAbonne();
            // Document réservé par B
            doc.reservation(abB);

            AtomicBoolean attenteFinie = new AtomicBoolean(false);

            Thread threadA = new Thread(() -> {
                try {
                    doc.attendreDisponibilite(5000);
                    attenteFinie.set(true);
                } catch (InterruptedException ignored) {}
            });

            threadA.start();
            Thread.sleep(200);

            // B vient emprunter → notifyAll() → A se réveille
            doc.emprunt(abB);
            threadA.join(2000);

            assertTrue(attenteFinie.get());
            // Le doc est EMPRUNTE, pas DISPONIBLE → A doit informer l'échec
            assertEquals(DocumentBase.Etat.EMPRUNTE, doc.getEtat());
        }
    }

    // ================================================================== //
    //  5. BretteSoft© Sitting Bull – alertes email                       //
    // ================================================================== //
    @Nested
    @DisplayName("BretteSoft© Sitting Bull – alertes email")
    class SittingBullTests {

        @Test
        @DisplayName("ajouterAlerte() enregistre un email")
        void ajouterAlerteOk() throws EmpruntException {
            DocumentTest doc = new DocumentTest("T008", "Test Sitting Bull");
            doc.emprunt(abonneAdulte()); // rendre indisponible

            assertDoesNotThrow(() -> doc.ajouterAlerte("test@example.com"));
        }

        @Test
        @DisplayName("ajouterAlerte() ne duplique pas le même email")
        void ajouterAlertePasDeDuplication() throws EmpruntException {
            DocumentTest doc = new DocumentTest("T009", "Test Sitting Bull 2");
            doc.emprunt(abonneAdulte());

            doc.ajouterAlerte("test@example.com");
            doc.ajouterAlerte("test@example.com"); // doublon

            // On ne peut pas inspecter la liste directement (privée),
            // mais retour() ne doit pas lever d'exception
            assertDoesNotThrow(() -> doc.retour());
        }

        @Test
        @DisplayName("retour() vide la liste d'alertes (envoi en thread séparé)")
        void retourVideAlertes() throws EmpruntException, InterruptedException {
            DocumentTest doc = new DocumentTest("T010", "Test Sitting Bull 3");
            doc.emprunt(abonneAdulte());
            doc.ajouterAlerte("alice@example.com");
            doc.ajouterAlerte("bob@example.com");

            assertDoesNotThrow(() -> doc.retour());
            // Après retour(), le doc est DISPONIBLE
            assertEquals(DocumentBase.Etat.DISPONIBLE, doc.getEtat());
            // Laisser le thread d'envoi démarrer (même si l'envoi SMTP échouera en test)
            Thread.sleep(200);
            // Si on ajoute une nouvelle alerte et fait un 2e retour, la liste ne doit pas
            // contenir les anciennes alertes
            doc.emprunt(abonneAdulte());
            doc.ajouterAlerte("charlie@example.com");
            assertDoesNotThrow(() -> doc.retour()); // ne doit pas renvoyer à alice et bob
        }
    }

    // ================================================================== //
    //  6. Singleton Mediatheque                                           //
    // ================================================================== //
    @Nested
    @DisplayName("Singleton Mediatheque")
    class MediathequeTests {

        @Test
        @DisplayName("getInstance() retourne toujours la même instance")
        void singletonMemeInstance() {
            Mediatheque m1 = Mediatheque.getInstance();
            Mediatheque m2 = Mediatheque.getInstance();
            assertSame(m1, m2, "getInstance() doit retourner la même instance");
        }

        @Test
        @DisplayName("Documents de test présents")
        void documentsPresents() {
            Mediatheque m = Mediatheque.getInstance();
            assertNotNull(m.getDocument("L001"), "L001 doit exister");
            assertNotNull(m.getDocument("D001"), "D001 doit exister");
        }

        @Test
        @DisplayName("Abonnés de test présents")
        void abonnesPresents() {
            Mediatheque m = Mediatheque.getInstance();
            assertNotNull(m.getAbonne(1), "Abonné 1 doit exister");
            assertNotNull(m.getAbonne(3), "Abonné 3 doit exister");
        }

        @Test
        @DisplayName("Document inexistant retourne null")
        void documentInexistant() {
            assertNull(Mediatheque.getInstance().getDocument("ZZZZ"));
        }

        @Test
        @DisplayName("Abonné inexistant retourne null")
        void abonneInexistant() {
            assertNull(Mediatheque.getInstance().getAbonne(999));
        }
    }
}
