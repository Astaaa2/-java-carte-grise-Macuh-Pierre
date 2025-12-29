package controllers;

import models.Marque;
import java.util.List;

/**
 * Contrôleur pour gérer les marques
 * ---------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre le rôle d'un "Controller" dans le pattern MVC.
 *    - Il sert d'intermédiaire entre la vue et le modèle.
 * 2. Savoir appeler les méthodes du modèle depuis le contrôleur.
 * 3. Savoir créer, modifier, supprimer et récupérer des données via un contrôleur.
 */
public class MarqueController {

    /**
     * Récupérer toutes les marques depuis le modèle
     * @return liste de toutes les marques
     */
    public List<Marque> fetchAllMarques() {
        return Marque.getAllMarques();
    }

    /**
     * Ajouter une nouvelle marque
     * @param nom nom de la marque
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean createMarque(String nom) {
        return Marque.addMarque(nom);
    }

    /**
     * Modifier une marque existante
     * @param id identifiant de la marque
     * @param nom nouveau nom de la marque
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifyMarque(int id, String nom) {
        return Marque.updateMarque(id, nom);
    }

    /**
     * Supprimer une marque
     * @param id identifiant de la marque
     * @return true si la suppression a réussi, false sinon
     */
    public boolean removeMarque(int id) {
        return Marque.deleteMarque(id);
    }

    /**
     * Rechercher une marque par son identifiant
     * @param id identifiant de la marque
     * @return objet Marque correspondant ou null si non trouvé
     */
    public Marque findMarqueById(int id) {
        return Marque.getMarqueById(id);
    }
}
