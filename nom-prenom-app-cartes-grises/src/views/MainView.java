package views;

import controllers.*;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale avec image de fond proportionnelle
 */
public class MainView extends JFrame {

    private MarqueController marqueController;
    private ModeleController modeleController;
    private VehiculeController vehiculeController;
    private ProprietaireController proprietaireController;
    private PossederController possederController;

    public MainView(MarqueController mc,
                    ModeleController moc,
                    VehiculeController vc,
                    ProprietaireController prc,
                    PossederController pc) {

        this.marqueController = mc;
        this.modeleController = moc;
        this.vehiculeController = vc;
        this.proprietaireController = prc;
        this.possederController = pc;

        setTitle("Gestion carte grise - Accueil");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // -----------------------------
        // 1. Image proportionnelle
        // -----------------------------
        ImageIcon originalIcon = new ImageIcon("img/carte-grise.jpeg");

        // Taille maximale pour l'image
        int maxWidth = 800;
        int maxHeight = 300;

        int imgWidth = originalIcon.getIconWidth();
        int imgHeight = originalIcon.getIconHeight();

        double ratio = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);

        int newWidth = (int) (imgWidth * ratio);
        int newHeight = (int) (imgHeight * ratio);

        Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage), JLabel.CENTER);

        // -----------------------------
        // 2. Titre sur l'image
        // -----------------------------
        JLabel titre = new JLabel("Bienvenue dans la gestion de carte grise", JLabel.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 24));
        titre.setForeground(Color.BLACK);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(titre, BorderLayout.NORTH);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        add(imagePanel, BorderLayout.NORTH);

        // -----------------------------
        // 3. Panneau avec boutons
        // -----------------------------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));

        JButton marqueBtn = new JButton("Gestion des Marques");
        JButton modeleBtn = new JButton("Gestion des Modèles");
        JButton vehiculeBtn = new JButton("Gestion des Véhicules");
        JButton proprietaireBtn = new JButton("Gestion des Propriétaires");
        JButton possederBtn = new JButton("Gestion des Possessions");

        buttonPanel.add(marqueBtn);
        buttonPanel.add(modeleBtn);
        buttonPanel.add(vehiculeBtn);
        buttonPanel.add(proprietaireBtn);
        buttonPanel.add(possederBtn);

        add(buttonPanel, BorderLayout.CENTER);

        // -----------------------------
        // 4. Actions des boutons
        // -----------------------------
        marqueBtn.addActionListener(e -> {
            try { new MarqueView(marqueController).showWindow(); }
            catch (Exception ex) { showError("Marque", ex); }
        });

        modeleBtn.addActionListener(e -> {
            try { new ModeleView(modeleController, marqueController).showWindow(); }
            catch (Exception ex) { showError("Modèle", ex); }
        });

        vehiculeBtn.addActionListener(e -> {
            try { new VehiculeView(vehiculeController, modeleController, marqueController).showWindow(); }
            catch (Exception ex) { showError("Véhicule", ex); }
        });

        proprietaireBtn.addActionListener(e -> {
            try { new ProprietaireView(proprietaireController).showWindow(); }
            catch (Exception ex) { showError("Propriétaire", ex); }
        });

        possederBtn.addActionListener(e -> {
            try { new PossederView(possederController, proprietaireController, vehiculeController).showWindow(); }
            catch (Exception ex) { showError("Possession", ex); }
        });
    }

    private void showError(String name, Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Erreur lors de l'ouverture de la vue " + name + " : " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
    }

    public void showWindow() {
        setVisible(true);
    }
}
