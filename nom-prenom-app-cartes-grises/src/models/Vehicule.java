package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe représentant un véhicule
 * ------------------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion d'entité métier dans un projet MVC.
 * 2. Manipuler les attributs d'un véhicule (matricule, année, poids, puissance, modèle).
 * 3. Gérer les opérations CRUD avec JDBC (INSERT, UPDATE, DELETE, SELECT).
 * 4. Utiliser PreparedStatement pour sécuriser les requêtes SQL.
 * 5. Vérifier les relations avec d'autres entités (ex : POSSEDER) avant suppression.
 * 6. Retourner des listes d'objets pour affichage dans une JTable.
 */
public class Vehicule {

    private int idVehicule;
    private String matricule;
    private int anneeSortie;
    private double poids;
    private int puissanceChevaux;
    private int puissanceFiscale;
    private int idModele; // clé étrangère vers MODELE

    // ================== GETTERS / SETTERS ==================
    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public int getAnneeSortie() { return anneeSortie; }
    public void setAnneeSortie(int anneeSortie) { this.anneeSortie = anneeSortie; }

    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }

    public int getPuissanceChevaux() { return puissanceChevaux; }
    public void setPuissanceChevaux(int puissanceChevaux) { this.puissanceChevaux = puissanceChevaux; }

    public int getPuissanceFiscale() { return puissanceFiscale; }
    public void setPuissanceFiscale(int puissanceFiscale) { this.puissanceFiscale = puissanceFiscale; }

    public int getIdModele() { return idModele; }
    public void setIdModele(int idModele) { this.idModele = idModele; }

    @Override
    public String toString() { return matricule; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicule)) return false;
        Vehicule v = (Vehicule) o;
        return idVehicule == v.idVehicule;
    }

    @Override
    public int hashCode() { return Objects.hash(idVehicule); }

    // ================== DAO / CRUD ==================

    /** Récupérer tous les véhicules */
    public static List<Vehicule> getAllVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM VEHICULE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setIdVehicule(rs.getInt("id_vehicule"));
                v.setMatricule(rs.getString("matricule"));
                v.setAnneeSortie(rs.getInt("annee_sortie"));
                v.setPoids(rs.getDouble("poids"));
                v.setPuissanceChevaux(rs.getInt("puissance_chevaux"));
                v.setPuissanceFiscale(rs.getInt("puissance_fiscale"));
                v.setIdModele(rs.getInt("id_modele"));
                vehicules.add(v);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getAllVehicules : " + e.getMessage());
        }

        return vehicules;
    }

    /** Vérifie si un véhicule existe (par matricule) */
    public static boolean exists(String matricule, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM VEHICULE WHERE matricule = ?";
        if (excludeId != null) sql += " AND id_vehicule != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, matricule);
            if (excludeId != null) ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur exists : " + e.getMessage());
        }

        return false;
    }

    /** Ajouter un véhicule */
    public static boolean addVehicule(String matricule, int annee, double poids, int chevaux, int fiscale, int idModele) {
        if (exists(matricule, null)) return false;

        String sql = "INSERT INTO VEHICULE (matricule, annee_sortie, poids, puissance_chevaux, puissance_fiscale, id_modele) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, matricule);
            ps.setInt(2, annee);
            ps.setDouble(3, poids);
            ps.setInt(4, chevaux);
            ps.setInt(5, fiscale);
            ps.setInt(6, idModele);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur addVehicule : " + e.getMessage());
            return false;
        }
    }

    /** Modifier un véhicule */
    public static boolean updateVehicule(int id, String matricule, int annee, double poids, int chevaux, int fiscale, int idModele) {
        if (exists(matricule, id)) return false;

        String sql = "UPDATE VEHICULE SET matricule=?, annee_sortie=?, poids=?, puissance_chevaux=?, puissance_fiscale=?, id_modele=? WHERE id_vehicule=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, matricule);
            ps.setInt(2, annee);
            ps.setDouble(3, poids);
            ps.setInt(4, chevaux);
            ps.setInt(5, fiscale);
            ps.setInt(6, idModele);
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur updateVehicule : " + e.getMessage());
            return false;
        }
    }

    /** Supprimer un véhicule (vérifie la relation POSSEDER) */
    public static boolean deleteVehicule(int id) {
        String checkSql = "SELECT COUNT(*) FROM POSSEDER WHERE id_vehicule = ?";
        String deleteSql = "DELETE FROM VEHICULE WHERE id_vehicule = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // impossible de supprimer si véhicule lié à une possession
                        return false;
                    }
                }
            }

            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setInt(1, id);
                if (psDelete.executeUpdate() > 0) {
                    conn.commit();
                    return true;
                } else conn.rollback();
            }

        } catch (SQLException e) {
            System.err.println("Erreur deleteVehicule : " + e.getMessage());
        }

        return false;
    }

    /** Récupérer un véhicule par son ID */
    public static Vehicule getVehiculeById(int id) {
        String sql = "SELECT * FROM VEHICULE WHERE id_vehicule = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setIdVehicule(rs.getInt("id_vehicule"));
                    v.setMatricule(rs.getString("matricule"));
                    v.setAnneeSortie(rs.getInt("annee_sortie"));
                    v.setPoids(rs.getDouble("poids"));
                    v.setPuissanceChevaux(rs.getInt("puissance_chevaux"));
                    v.setPuissanceFiscale(rs.getInt("puissance_fiscale"));
                    v.setIdModele(rs.getInt("id_modele"));
                    return v;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur getVehiculeById : " + e.getMessage());
        }

        return null;
    }
}
