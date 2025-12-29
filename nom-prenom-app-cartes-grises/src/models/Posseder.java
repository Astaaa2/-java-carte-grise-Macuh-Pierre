package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle représentant la table 'POSSEDER' dans la base de données.
 * Elle sert de lien entre les objets Java et les données SQL.
 * * Rôle : Gérer l'association "Many-to-Many" entre un Propriétaire et un Véhicule.
 */
public class Posseder {

    // Attributs correspondant aux colonnes de la table SQL
    private int idProprietaire;
    private int idVehicule;
    private Date dateDebut;
    private Date dateFin;

    // ========================================================================
    // GETTERS & SETTERS
    // Permettent d'accéder et de modifier les attributs privés depuis l'extérieur
    // ========================================================================
    
    public int getIdProprietaire() { return idProprietaire; }
    public void setIdProprietaire(int idProprietaire) { this.idProprietaire = idProprietaire; }

    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    // ========================================================================
    // MÉTHODES MÉTIER & DAO (Data Access Object)
    // ========================================================================

    /**
     * Vérifie si une association entre ce propriétaire et ce véhicule existe déjà.
     * Cette méthode est cruciale pour l'ANTI-DOUBLON.
     * * @param idProprietaire L'ID du propriétaire
     * @param idVehicule L'ID du véhicule
     * @return true si le couple existe déjà, false sinon.
     */
    public static boolean existsPossession(int idProprietaire, int idVehicule) {
        // Requête SQL qui compte le nombre de lignes correspondant aux IDs
        String sql = "SELECT COUNT(*) FROM POSSEDER WHERE id_proprietaire=? AND id_vehicule=?";
        
        // Try-with-resources : Ferme automatiquement la connexion après usage
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Remplissage des paramètres (les points d'interrogation)
            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            
            // Exécution de la requête et lecture du résultat
            try (ResultSet rs = ps.executeQuery()) {
                // Si on a un résultat et que le compteur est supérieur à 0, c'est que ça existe
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la vérification d'existence : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère la liste complète de toutes les possessions enregistrées.
     * Utilisé pour afficher le tableau dans l'interface graphique.
     * * @return Une liste d'objets Posseder
     */
    public static List<Posseder> getAllPossessions() {
        List<Posseder> liste = new ArrayList<>();
        String sql = "SELECT * FROM POSSEDER"; // Sélectionne toutes les colonnes

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Boucle sur chaque ligne renvoyée par la base de données
            while (rs.next()) {
                Posseder p = new Posseder();
                // On mappe les colonnes SQL vers les attributs Java
                p.setIdProprietaire(rs.getInt("id_proprietaire"));
                p.setIdVehicule(rs.getInt("id_vehicule"));
                p.setDateDebut(rs.getDate("date_debut_propriete"));
                p.setDateFin(rs.getDate("date_fin_propriete"));
                
                // Ajout de l'objet à la liste
                liste.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des possessions : " + e.getMessage());
        }

        return liste;
    }

    /**
     * Ajoute une nouvelle possession dans la base de données.
     * Comprend une SÉCURITÉ ANTI-DOUBLON pour éviter les crashs.
     * * @param idProprietaire ID du propriétaire
     * @param idVehicule ID du véhicule
     * @param dateDebut Date de début de possession
     * @param dateFin Date de fin (peut être null)
     * @return true si l'ajout a réussi, false sinon (ou si doublon).
     */
    public static boolean addPossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        // --- BLOC DE SÉCURITÉ ---
        // Avant d'insérer, on vérifie si l'enregistrement existe déjà.
        if (existsPossession(idProprietaire, idVehicule)) {
            System.out.println("Tentative d'ajout annulée : Doublon détecté.");
            return false; // On stoppe tout et on retourne false
        }

        String sql = "INSERT INTO POSSEDER (id_proprietaire, id_vehicule, date_debut_propriete, date_fin_propriete) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Injection des paramètres sécurisée contre les failles SQL
            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            ps.setDate(3, dateDebut);
            ps.setDate(4, dateFin); // JDBC gère le 'null' automatiquement ici

            // executeUpdate renvoie le nombre de lignes modifiées (1 si succès)
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout : " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour les dates d'une possession existante.
     * On ne modifie pas les IDs car ils servent de clé primaire composite (identifiant unique).
     * * @param idProprietaire ID du propriétaire (sert à identifier la ligne)
     * @param idVehicule ID du véhicule (sert à identifier la ligne)
     * @param dateDebut Nouvelle date de début
     * @param dateFin Nouvelle date de fin
     * @return true si la modification a réussi.
     */
    public static boolean updatePossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        // On cible la ligne grâce aux deux IDs dans la clause WHERE
        String sql = "UPDATE POSSEDER SET date_debut_propriete=?, date_fin_propriete=? WHERE id_proprietaire=? AND id_vehicule=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Paramètres à mettre à jour
            ps.setDate(1, dateDebut);
            ps.setDate(2, dateFin);
            
            // Paramètres de la condition WHERE (pour trouver la bonne ligne)
            ps.setInt(3, idProprietaire);
            ps.setInt(4, idVehicule);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la modification : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime définitivement une possession de la base de données.
     * * @param idProprietaire ID du propriétaire
     * @param idVehicule ID du véhicule
     * @return true si la suppression a réussi.
     */
    public static boolean deletePossession(int idProprietaire, int idVehicule) {
        String sql = "DELETE FROM POSSEDER WHERE id_proprietaire=? AND id_vehicule=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);

            // Si une ligne a été effacée, renvoie true
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression : " + e.getMessage());
            return false;
        }
    }
}