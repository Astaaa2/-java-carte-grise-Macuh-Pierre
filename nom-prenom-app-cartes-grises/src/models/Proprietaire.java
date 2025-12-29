package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe représentant un propriétaire de véhicule
 * ------------------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion d'entité métier dans un projet MVC.
 * 2. Manipuler les attributs d'un propriétaire (nom, prénom, adresse, cp, ville).
 * 3. Gérer les opérations CRUD avec JDBC (INSERT, UPDATE, DELETE, SELECT).
 * 4. Utiliser PreparedStatement pour sécuriser les requêtes SQL.
 * 5. Gérer les relations avec d'autres entités (ex : POSSEDER) pour empêcher la suppression si liée.
 * 6. Retourner des listes d'objets pour affichage dans une JTable.
 */
public class Proprietaire {

    private int idProprietaire;
    private String nom;
    private String prenom;
    private String adresse;
    private String cp;
    private String ville;

    // ================== GETTERS / SETTERS ==================
    public int getIdProprietaire() { return idProprietaire; }
    public void setIdProprietaire(int idProprietaire) { this.idProprietaire = idProprietaire; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getCp() { return cp; }
    public void setCp(String cp) { this.cp = cp; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    @Override
    public String toString() { return prenom + " " + nom; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proprietaire)) return false;
        Proprietaire p = (Proprietaire) o;
        return idProprietaire == p.idProprietaire;
    }

    @Override
    public int hashCode() { return Objects.hash(idProprietaire); }

    // ================== DAO / CRUD ==================

    /** Récupérer tous les propriétaires */
    public static List<Proprietaire> getAllProprietaires() {
        List<Proprietaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM PROPRIETAIRE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Proprietaire p = new Proprietaire();
                p.setIdProprietaire(rs.getInt("id_proprietaire"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setAdresse(rs.getString("adresse"));
                p.setCp(rs.getString("cp"));
                p.setVille(rs.getString("ville"));
                liste.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getAllProprietaires : " + e.getMessage());
        }
        return liste;
    }

    /** Ajouter un propriétaire */
    public static boolean addProprietaire(String nom, String prenom, String adresse, String cp, String ville) {
        String sql = "INSERT INTO PROPRIETAIRE (nom, prenom, adresse, cp, ville) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, adresse);
            ps.setString(4, cp);
            ps.setString(5, ville);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur addProprietaire : " + e.getMessage());
            return false;
        }
    }

    /** Modifier un propriétaire */
    public static boolean updateProprietaire(int id, String nom, String prenom, String adresse, String cp, String ville) {
        String sql = "UPDATE PROPRIETAIRE SET nom=?, prenom=?, adresse=?, cp=?, ville=? WHERE id_proprietaire=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, adresse);
            ps.setString(4, cp);
            ps.setString(5, ville);
            ps.setInt(6, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur updateProprietaire : " + e.getMessage());
            return false;
        }
    }

    /** Supprimer un propriétaire (vérifie la relation POSSEDER) */
    public static boolean deleteProprietaire(int id) {
        String checkSql = "SELECT COUNT(*) FROM POSSEDER WHERE id_proprietaire = ?";
        String deleteSql = "DELETE FROM PROPRIETAIRE WHERE id_proprietaire = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // impossible de supprimer si lié à une possession
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
            System.err.println("Erreur deleteProprietaire : " + e.getMessage());
        }

        return false;
    }

    /** Récupérer un propriétaire par son ID */
    public static Proprietaire getProprietaireById(int id) {
        String sql = "SELECT * FROM PROPRIETAIRE WHERE id_proprietaire = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Proprietaire p = new Proprietaire();
                    p.setIdProprietaire(rs.getInt("id_proprietaire"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setAdresse(rs.getString("adresse"));
                    p.setCp(rs.getString("cp"));
                    p.setVille(rs.getString("ville"));
                    return p;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur getProprietaireById : " + e.getMessage());
        }

        return null;
    }
}
