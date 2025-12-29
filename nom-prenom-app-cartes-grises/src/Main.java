import views.MainView;
import controllers.*;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Pour que l'interface Swing se lance dans le bon thread
        SwingUtilities.invokeLater(() -> {
            // Création des contrôleurs
            MarqueController marqueController = new MarqueController();
            ModeleController modeleController = new ModeleController();
            VehiculeController vehiculeController = new VehiculeController();
            ProprietaireController proprietaireController = new ProprietaireController();
            PossederController possederController = new PossederController();

            // Création de la vue principale (menu)
            MainView mainView = new MainView(
                    marqueController,
                    modeleController,
                    vehiculeController,
                    proprietaireController,
                    possederController
            );

            mainView.showWindow();
        });
    }
}
