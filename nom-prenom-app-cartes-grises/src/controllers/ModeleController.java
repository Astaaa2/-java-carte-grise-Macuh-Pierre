package controllers;

import models.Modele;
import java.util.List;

/**
 * Contrôleur pour gérer les modèles
 * ---------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre le rôle d'un "Controller" dans le pattern MVC.
 *    - Il sert d'intermédiaire entre la vue et le modèle.
 * 2. Gérer les relations entre objets (ici, un modèle appartient à une marque).
 * 3. Savoir créer, modifier, supprimer et récupérer des données via le contrôleur.
 */
public class ModeleController {

    /**
     * Récupérer tous les modèles depuis le modèle
     * @return liste de tous les modèles
     */
    public List<Modele> fetchAllModeles() {
        return Modele.getAllModeles();
    }

    /**
     * Ajouter un nouveau modèle
     * @param nom nom du modèle
     * @param idMarque identifiant de la marque associée
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean createModele(String nom, int idMarque) {
        return Modele.addModele(nom, idMarque);
    }

    /**
     * Modifier un modèle existant
     * @param id identifiant du modèle
     * @param nom nouveau nom du modèle
     * @param idMarque identifiant de la marque associée
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifyModele(int id, String nom, int idMarque) {
        return Modele.updateModele(id, nom, idMarque);
    }

    /**
     * Supprimer un modèle
     * @param id identifiant du modèle
     * @return true si la suppression a réussi, false sinon
     */
    public boolean removeModele(int id) {
        return Modele.deleteModele(id);
    }

    /**
     * Rechercher un modèle par son identifiant
     * @param id identifiant du modèle
     * @return objet Modele correspondant ou null si non trouvé
     */
    public Modele findModeleById(int id) {
        return Modele.getModeleById(id);
    }
}
