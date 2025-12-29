package controllers;

import models.Vehicule;
import java.util.List;

/**
 * Contrôleur pour gérer les véhicules
 * -----------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre le rôle d'un "Controller" dans le pattern MVC :
 *    - Intermédiaire entre la vue et le modèle.
 * 2. Savoir récupérer, ajouter, modifier et supprimer des données d’un modèle.
 * 3. Manipulation des objets et listes pour affichage dans une JTable.
 * 4. Gestion des relations entre véhicules et modèles (via idModele).
 * 5. Validation des saisies numériques (année, poids, chevaux, fiscale) côté vue.
 */
public class VehiculeController {

    /**
     * Récupérer tous les véhicules
     * @return liste de tous les véhicules
     */
    public List<Vehicule> fetchAllVehicules() {
        return Vehicule.getAllVehicules();
    }

    /**
     * Ajouter un véhicule
     * @param matricule numéro d'immatriculation
     * @param annee année de sortie
     * @param poids poids du véhicule
     * @param chevaux puissance en chevaux
     * @param fiscale puissance fiscale
     * @param idModele identifiant du modèle associé
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean createVehicule(String matricule, int annee, double poids, int chevaux, int fiscale, int idModele) {
        return Vehicule.addVehicule(matricule, annee, poids, chevaux, fiscale, idModele);
    }

    /**
     * Modifier un véhicule existant
     * @param id identifiant du véhicule
     * @param matricule nouveau matricule
     * @param annee nouvelle année
     * @param poids nouveau poids
     * @param chevaux nouvelle puissance
     * @param fiscale nouvelle puissance fiscale
     * @param idModele identifiant du nouveau modèle associé
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifyVehicule(int id, String matricule, int annee, double poids, int chevaux, int fiscale, int idModele) {
        return Vehicule.updateVehicule(id, matricule, annee, poids, chevaux, fiscale, idModele);
    }

    /**
     * Supprimer un véhicule
     * @param id identifiant du véhicule
     * @return true si la suppression a réussi, false sinon
     */
    public boolean removeVehicule(int id) {
        return Vehicule.deleteVehicule(id);
    }

    /**
     * Trouver un véhicule par son identifiant
     * @param id identifiant du véhicule
     * @return l'objet Vehicule correspondant, ou null si non trouvé
     */
    public Vehicule findVehiculeById(int id) {
        return Vehicule.getVehiculeById(id);
    }
}
