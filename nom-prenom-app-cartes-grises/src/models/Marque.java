package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe modèle représentant une Marque
 * --------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de "modèle" dans le pattern MVC (représentation des données).
 * 2. Apprendre à faire des opérations CRUD via JDBC.
 * 3. Comprendre l’usage de PreparedStatement pour sécuriser les requêtes SQL.
 * 4. Gérer les relations avec d’autres tables (ex: MODELE -> MARQUE).
 * 5. Manipuler des objets et des listes pour l’affichage dans une JTable.
 * 6. Gestion des doublons avant insertion ou modification.
 */
public class Marque {

    private int idMarque;
    private String nomMarque;

    // ================== GETTERS / SETTERS ==================
    public int getIdMarque() { return idMarque; }
    public void setIdMarque(int idMarque) { this.idMarque = idMarque; }
    public String getNomMarque() { return nomMarque; }
    public void setNomMarque(String nomMarque) { this.nomMarque = nomMarque; }

    @Override
    public String toString() { return nomMarque; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Marque)) return false;
        Marque m = (Marque) o;
        return idMarque == m.idMarque;
    }

    @Override
    public int hashCode() { return Objects.hash(idMarque); }

    // ================== DAO / CRUD ==================

    /** Récupérer toutes les marques */
    public static List<Marque> getAllMarques() {
        List<Marque> marques = new ArrayList<>();
        String sql = "SELECT * FROM MARQUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Marque m = new Marque();
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomMarque(rs.getString("nom_marque"));
                marques.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllMarques : " + e.getMessage());
        }
        return marques;
    }

    /** Vérifier si une marque existe (pour éviter les doublons) */
    public static boolean exists(String nom, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM MARQUE WHERE nom_marque = ?";
        if (excludeId != null) sql += " AND id_marque != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            if (excludeId != null) ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur exists : " + e.getMessage());
        }
        return false;
    }

    /** Ajouter une nouvelle marque */
    public static boolean addMarque(String nom) {
        if (exists(nom, null)) return false;

        String sql = "INSERT INTO MARQUE (nom_marque) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur addMarque : " + e.getMessage());
            return false;
        }
    }

    /** Modifier une marque existante */
    public static boolean updateMarque(int id, String nom) {
        if (exists(nom, id)) return false;

        String sql = "UPDATE MARQUE SET nom_marque = ? WHERE id_marque = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur updateMarque : " + e.getMessage());
            return false;
        }
    }

    /** Supprimer une marque (si aucun modèle ne lui est associé) */
    public static boolean deleteMarque(int id) {
        String checkSql = "SELECT COUNT(*) FROM MODELE WHERE id_marque = ?";
        String deleteSql = "DELETE FROM MARQUE WHERE id_marque = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // rollback si la marque est utilisée
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
            System.err.println("Erreur deleteMarque : " + e.getMessage());
        }
        return false;
    }

    /** Récupérer une marque par son identifiant */
    public static Marque getMarqueById(int id) {
        String sql = "SELECT * FROM MARQUE WHERE id_marque = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Marque m = new Marque();
                    m.setIdMarque(rs.getInt("id_marque"));
                    m.setNomMarque(rs.getString("nom_marque"));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur getMarqueById : " + e.getMessage());
        }
        return null;
    }
}
