package views;

import controllers.VehiculeController;
import controllers.ModeleController;
import controllers.MarqueController;
import models.Vehicule;
import models.Modele;
import models.Marque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * Vue pour gérer les véhicules
 * ----------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la gestion d’un JTable avec boutons d’action.
 * 2. Gérer les relations entre objets (Vehicule -> Modele -> Marque).
 * 3. Ajouter, modifier et supprimer des données depuis l’interface.
 * 4. Champs obligatoires : Matricule et Modèle.
 * 5. Gestion des erreurs de saisie : formulaire reste ouvert si erreur.
 * 6. Ajouter un bouton "Fermer" pour quitter proprement la fenêtre.
 */
public class VehiculeView extends JFrame {

    private VehiculeController vehiculeController;
    private ModeleController modeleController;
    private MarqueController marqueController;

    private DefaultTableModel tableModel;
    private JTable table;

    // ================== CONSTRUCTEUR ==================
    public VehiculeView(VehiculeController vehiculeController,
                        ModeleController modeleController,
                        MarqueController marqueController) {
        this.vehiculeController = vehiculeController;
        this.modeleController = modeleController;
        this.marqueController = marqueController;

        setTitle("Liste des véhicules");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] colonnes = {"ID", "Matricule", "Année", "Poids", "Chevaux", "Fiscale", "Modèle", "Marque", "Modifier", "Supprimer"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 8;
            }
        };

        table = new JTable(tableModel);
        table.getColumn("Modifier").setCellRenderer(new ButtonRenderer());
        table.getColumn("Modifier").setCellEditor(new ButtonEditor(new JCheckBox(), "Modifier"));
        table.getColumn("Supprimer").setCellRenderer(new ButtonRenderer());
        table.getColumn("Supprimer").setCellEditor(new ButtonEditor(new JCheckBox(), "Supprimer"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // -------- Panneau des boutons --------
        JButton addButton = new JButton("Ajouter un véhicule");
        addButton.addActionListener(e -> showVehiculeForm(null));

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    // ================== FORMULAIRE AJOUT / MODIFICATION ==================
    private void showVehiculeForm(Vehicule vehicule) {
        JDialog dialog = new JDialog(this, "Formulaire véhicule", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField matriculeField = new JTextField();
        JTextField anneeField = new JTextField();
        JTextField poidsField = new JTextField();
        JTextField chevauxField = new JTextField();
        JTextField fiscaleField = new JTextField();

        JComboBox<Modele> modeleCombo = new JComboBox<>();
        for (Modele m : modeleController.fetchAllModeles()) modeleCombo.addItem(m);

        // Pré-remplissage si modification
        if (vehicule != null) {
            matriculeField.setText(vehicule.getMatricule());
            anneeField.setText(String.valueOf(vehicule.getAnneeSortie()));
            poidsField.setText(String.valueOf(vehicule.getPoids()));
            chevauxField.setText(String.valueOf(vehicule.getPuissanceChevaux()));
            fiscaleField.setText(String.valueOf(vehicule.getPuissanceFiscale()));
            for (int i = 0; i < modeleCombo.getItemCount(); i++) {
                if (modeleCombo.getItemAt(i).getIdModele() == vehicule.getIdModele()) {
                    modeleCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String matricule = matriculeField.getText().trim();
            Modele selectedModele = (Modele) modeleCombo.getSelectedItem();

            // Validation des champs obligatoires
            if (matricule.isEmpty() || selectedModele == null) {
                JOptionPane.showMessageDialog(dialog, "Matricule et modèle requis !");
                return;
            }

            try {
                int annee = Integer.parseInt(anneeField.getText().trim());
                double poids = Double.parseDouble(poidsField.getText().trim());
                int chevaux = Integer.parseInt(chevauxField.getText().trim());
                int fiscale = Integer.parseInt(fiscaleField.getText().trim());

                boolean success;
                if (vehicule == null) {
                    success = vehiculeController.createVehicule(matricule, annee, poids, chevaux, fiscale, selectedModele.getIdModele());
                } else {
                    success = vehiculeController.modifyVehicule(vehicule.getIdVehicule(), matricule, annee, poids, chevaux, fiscale, selectedModele.getIdModele());
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Enregistré !");
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur ou doublon ! Vérifiez les informations.");
                    // formulaire reste ouvert
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : saisie numérique invalide !");
                // formulaire reste ouvert
            }
        });

        dialog.add(new JLabel("Matricule:")); dialog.add(matriculeField);
        dialog.add(new JLabel("Année:")); dialog.add(anneeField);
        dialog.add(new JLabel("Poids:")); dialog.add(poidsField);
        dialog.add(new JLabel("Puissance (ch):")); dialog.add(chevauxField);
        dialog.add(new JLabel("Puissance fiscale:")); dialog.add(fiscaleField);
        dialog.add(new JLabel("Modèle:")); dialog.add(modeleCombo);
        dialog.add(new JLabel()); dialog.add(saveButton);

        dialog.setVisible(true);
    }

    // ================== RAFRAÎCHIR LE TABLEAU ==================
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Vehicule v : vehiculeController.fetchAllVehicules()) {
            Modele m = modeleController.findModeleById(v.getIdModele());
            Marque marque = m != null ? marqueController.findMarqueById(m.getIdMarque()) : null;

            tableModel.addRow(new Object[]{
                    v.getIdVehicule(),
                    v.getMatricule(),
                    v.getAnneeSortie(),
                    v.getPoids(),
                    v.getPuissanceChevaux(),
                    v.getPuissanceFiscale(),
                    m != null ? m.getNomModele() : "",
                    marque != null ? marque.getNomMarque() : "",
                    "Modifier",
                    "Supprimer"
            });
        }
    }

    // ---------------------------
    // Classes internes pour les boutons
    // ---------------------------
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private String label;
        private JButton button;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            this.label = label;
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            button.setText(label);
            this.row = row;
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int id = (int) table.getValueAt(row, 0);
                if (label.equals("Supprimer")) {
                    int confirm = JOptionPane.showConfirmDialog(VehiculeView.this, "Supprimer ce véhicule ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = vehiculeController.removeVehicule(id);
                        JOptionPane.showMessageDialog(VehiculeView.this, success ? "Supprimé !" : "Erreur !");
                        refreshTable();
                    }
                } else if (label.equals("Modifier")) {
                    Vehicule v = vehiculeController.findVehiculeById(id);
                    if (v != null) showVehiculeForm(v);
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    // ================== AFFICHER LA VUE ==================
    public void showWindow() { setVisible(true); }
}
