package views;

import controllers.ModeleController;
import controllers.MarqueController;
import models.Modele;
import models.Marque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

/**
 * Vue pour gérer la liste des modèles
 * --------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de "Vue" dans MVC (affichage et interaction)
 * 2. Utiliser JTable pour lister les données
 * 3. Ajouter, modifier et supprimer un modèle via formulaire
 * 4. Utiliser plusieurs contrôleurs pour afficher les informations liées (Marque)
 * 5. Ajouter un bouton "Fermer" pour quitter proprement la fenêtre
 * 6. Vérifier que tous les champs sont obligatoires
 * 7. Ne pas fermer le formulaire si erreur
 */
public class ModeleView extends JFrame {

    private ModeleController modeleController;
    private MarqueController marqueController;
    private DefaultTableModel tableModel;
    private JTable table;

    public ModeleView(ModeleController modeleController, MarqueController marqueController) {
        this.modeleController = modeleController;
        this.marqueController = marqueController;

        setTitle("Liste des modèles");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] colonnes = {"ID", "Nom", "Marque", "Modifier", "Supprimer"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3;
            }
        };

        table = new JTable(tableModel);
        table.getColumn("Modifier").setCellRenderer(new ButtonRenderer());
        table.getColumn("Modifier").setCellEditor(new ButtonEditor(new JCheckBox(), "Modifier"));
        table.getColumn("Supprimer").setCellRenderer(new ButtonRenderer());
        table.getColumn("Supprimer").setCellEditor(new ButtonEditor(new JCheckBox(), "Supprimer"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Ajouter un modèle");
        addButton.addActionListener(e -> showModeleForm(null));

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    private void showModeleForm(Modele modele) {
        JDialog dialog = new JDialog(this, "Formulaire modèle", true);
        dialog.setSize(350, 200);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField nomField = new JTextField();
        JComboBox<Marque> marqueCombo = new JComboBox<>();
        for (Marque m : marqueController.fetchAllMarques()) marqueCombo.addItem(m);

        if (modele != null) {
            nomField.setText(modele.getNomModele());
            for (int i = 0; i < marqueCombo.getItemCount(); i++) {
                if (marqueCombo.getItemAt(i).getIdMarque() == modele.getIdMarque()) {
                    marqueCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            Marque selectedMarque = (Marque) marqueCombo.getSelectedItem();

            if (nom.isEmpty() || selectedMarque == null) {
                JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires !");
                return; // reste sur le formulaire
            }

            boolean success;
            if (modele == null) {
                success = modeleController.createModele(nom, selectedMarque.getIdMarque());
            } else {
                success = modeleController.modifyModele(modele.getIdModele(), nom, selectedMarque.getIdMarque());
            }

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Enregistré !");
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erreur ou doublon !\nVeuillez vérifier les informations.");
                // formulaire reste ouvert pour correction
            }
        });

        dialog.add(new JLabel("Nom:")); dialog.add(nomField);
        dialog.add(new JLabel("Marque:")); dialog.add(marqueCombo);
        dialog.add(new JLabel()); dialog.add(saveButton);

        dialog.setVisible(true);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Modele m : modeleController.fetchAllModeles()) {
            Marque marque = marqueController.findMarqueById(m.getIdMarque());
            tableModel.addRow(new Object[]{
                    m.getIdModele(),
                    m.getNomModele(),
                    marque != null ? marque.getNomMarque() : "",
                    "Modifier",
                    "Supprimer"
            });
        }
    }

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
                    int confirm = JOptionPane.showConfirmDialog(ModeleView.this, "Supprimer ce modèle ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = modeleController.removeModele(id);
                        JOptionPane.showMessageDialog(ModeleView.this, success ? "Supprimé !" : "Erreur !");
                        refreshTable();
                    }
                } else if (label.equals("Modifier")) {
                    Modele m = modeleController.findModeleById(id);
                    if (m != null) showModeleForm(m);
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
    }

    public void showWindow() { setVisible(true); }
}
