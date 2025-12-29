package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe modèle représentant un Modèle de véhicule
 * -------------------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de "modèle" dans le pattern MVC (représentation des données).
 * 2. Manipuler les clés étrangères (idMarque -> MARQUE).
 * 3. Effectuer des opérations CRUD via JDBC.
 * 4. Gérer les doublons avant insertion ou modification.
 * 5. Vérifier l'intégrité référentielle avant suppression (ex: présence de véhicules liés).
 * 6. Utiliser PreparedStatement pour sécuriser les requêtes SQL.
 * 7. Manipuler des objets et listes pour l’affichage dans une JTable.
 */
public class Modele {

    private int idModele;
    private String nomModele;
    private int idMarque; // clé étrangère vers MARQUE

    // ================== GETTERS / SETTERS ==================
    public int getIdModele() { return idModele; }
    public void setIdModele(int idModele) { this.idModele = idModele; }

    public String getNomModele() { return nomModele; }
    public void setNomModele(String nomModele) { this.nomModele = nomModele; }

    public int getIdMarque() { return idMarque; }
    public void setIdMarque(int idMarque) { this.idMarque = idMarque; }

    @Override
    public String toString() { return nomModele; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Modele)) return false;
        Modele m = (Modele) o;
        return idModele == m.idModele;
    }

    @Override
    public int hashCode() { return Objects.hash(idModele); }

    // ================== DAO / CRUD ==================

    /** Récupérer tous les modèles */
    public static List<Modele> getAllModeles() {
        List<Modele> modeles = new ArrayList<>();
        String sql = "SELECT * FROM MODELE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setNomModele(rs.getString("nom_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                modeles.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllModeles : " + e.getMessage());
        }
        return modeles;
    }

    /** Vérifier si un modèle existe (pour éviter les doublons) */
    public static boolean exists(String nom, int idMarque, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM MODELE WHERE nom_modele = ? AND id_marque = ?";
        if (excludeId != null) sql += " AND id_modele != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setInt(2, idMarque);
            if (excludeId != null) ps.setInt(3, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur exists : " + e.getMessage());
        }
        return false;
    }

    /** Ajouter un modèle */
    public static boolean addModele(String nom, int idMarque) {
        if (exists(nom, idMarque, null)) return false;

        String sql = "INSERT INTO MODELE (nom_modele, id_marque) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, idMarque);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur addModele : " + e.getMessage());
            return false;
        }
    }

    /** Modifier un modèle */
    public static boolean updateModele(int id, String nom, int idMarque) {
        if (exists(nom, idMarque, id)) return false;

        String sql = "UPDATE MODELE SET nom_modele = ?, id_marque = ? WHERE id_modele = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, idMarque);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur updateModele : " + e.getMessage());
            return false;
        }
    }

    /** Supprimer un modèle (si aucun véhicule ne lui est associé) */
    public static boolean deleteModele(int id) {
        String checkSql = "SELECT COUNT(*) FROM VEHICULE WHERE id_modele = ?";
        String deleteSql = "DELETE FROM MODELE WHERE id_modele = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // rollback si modèle utilisé
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
            System.err.println("Erreur deleteModele : " + e.getMessage());
        }
        return false;
    }

    /** Récupérer un modèle par son identifiant */
    public static Modele getModeleById(int id) {
        String sql = "SELECT * FROM MODELE WHERE id_modele = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Modele m = new Modele();
                    m.setIdModele(rs.getInt("id_modele"));
                    m.setNomModele(rs.getString("nom_modele"));
                    m.setIdMarque(rs.getInt("id_marque"));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur getModeleById : " + e.getMessage());
        }
        return null;
    }
}
