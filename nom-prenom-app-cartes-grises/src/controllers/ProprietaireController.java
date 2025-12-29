package controllers;

import models.Proprietaire;
import java.util.List;

/**
 * Contrôleur pour gérer les propriétaires
 * ---------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre le rôle d'un "Controller" dans le pattern MVC :
 *    - Intermédiaire entre la vue et le modèle.
 * 2. Savoir récupérer, ajouter, modifier et supprimer des données d’un modèle.
 * 3. Manipulation des objets et listes pour affichage dans une JTable.
 * 4. Gestion des identifiants pour retrouver ou modifier un propriétaire précis.
 */
public class ProprietaireController {

    /**
     * Récupérer tous les propriétaires
     * @return liste de tous les propriétaires
     */
    public List<Proprietaire> fetchAllProprietaires() {
        return Proprietaire.getAllProprietaires();
    }

    /**
     * Ajouter un propriétaire
     * @param nom nom du propriétaire
     * @param prenom prénom du propriétaire
     * @param adresse adresse complète
     * @param cp code postal
     * @param ville ville
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean createProprietaire(String nom, String prenom, String adresse, String cp, String ville) {
        return Proprietaire.addProprietaire(nom, prenom, adresse, cp, ville);
    }

    /**
     * Modifier un propriétaire existant
     * @param id identifiant du propriétaire
     * @param nom nouveau nom
     * @param prenom nouveau prénom
     * @param adresse nouvelle adresse
     * @param cp nouveau code postal
     * @param ville nouvelle ville
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifyProprietaire(int id, String nom, String prenom, String adresse, String cp, String ville) {
        return Proprietaire.updateProprietaire(id, nom, prenom, adresse, cp, ville);
    }

    /**
     * Supprimer un propriétaire
     * @param id identifiant du propriétaire
     * @return true si la suppression a réussi, false sinon
     */
    public boolean removeProprietaire(int id) {
        return Proprietaire.deleteProprietaire(id);
    }

    /**
     * Trouver un propriétaire par son identifiant
     * @param id identifiant du propriétaire
     * @return l'objet Proprietaire correspondant, ou null si non trouvé
     */
    public Proprietaire findProprietaireById(int id) {
        return Proprietaire.getProprietaireById(id);
    }
}
