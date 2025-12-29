package controllers;

import models.Posseder;
import java.sql.Date;
import java.util.List;

/**
 * Contrôleur pour gérer les possessions de véhicules par les propriétaires
 * ----------------------------------------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre le rôle d'un "Controller" dans le pattern MVC :
 *    - Intermédiaire entre la vue et le modèle.
 * 2. Gérer les relations entre objets (Posséder : lien entre Propriétaire et Véhicule).
 * 3. Savoir créer, modifier, supprimer et récupérer des données via le contrôleur.
 * 4. Manipulation des dates en Java (java.sql.Date) pour gérer les périodes de possession.
 */
public class PossederController {

    /**
     * Récupérer toutes les possessions
     * @return liste de toutes les possessions
     */
    public List<Posseder> fetchAllPossessions() {
        return Posseder.getAllPossessions();
    }

    /**
     * Ajouter une possession
     * @param idProprietaire identifiant du propriétaire
     * @param idVehicule identifiant du véhicule
     * @param dateDebut date de début de possession
     * @param dateFin date de fin de possession
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean createPossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        return Posseder.addPossession(idProprietaire, idVehicule, dateDebut, dateFin);
    }

    /**
     * Modifier une possession existante
     * @param idProprietaire identifiant du propriétaire
     * @param idVehicule identifiant du véhicule
     * @param dateDebut nouvelle date de début
     * @param dateFin nouvelle date de fin
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifyPossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        return Posseder.updatePossession(idProprietaire, idVehicule, dateDebut, dateFin);
    }

    /**
     * Supprimer une possession
     * @param idProprietaire identifiant du propriétaire
     * @param idVehicule identifiant du véhicule
     * @return true si la suppression a réussi, false sinon
     */
    public boolean removePossession(int idProprietaire, int idVehicule) {
        return Posseder.deletePossession(idProprietaire, idVehicule);
    }
}
