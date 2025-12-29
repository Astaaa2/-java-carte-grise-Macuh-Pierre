package views;

import controllers.MarqueController;
import models.Marque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

/**
 * Vue pour gérer la liste des marques
 * --------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de "Vue" dans MVC (affichage et interaction).
 * 2. Utiliser JTable pour lister les données.
 * 3. Ajouter, modifier et supprimer une marque via formulaire.
 * 4. Illustrer l’utilisation de boutons avec TableCellRenderer et TableCellEditor.
 * 5. Gestion simple de la fermeture de la fenêtre via un bouton "Fermer".
 */
public class MarqueView extends JFrame {

    private MarqueController marqueController;
    private DefaultTableModel tableModel;
    private JTable table;

    public MarqueView(MarqueController marqueController) {
        this.marqueController = marqueController;

        // -----------------------------
        // Paramètres de la fenêtre
        // -----------------------------
        setTitle("Liste des marques");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // -----------------------------
        // Initialisation des données
        // -----------------------------
        List<Marque> marques = marqueController.fetchAllMarques();

        String[] colonnes = {"ID", "Nom", "Modifier", "Supprimer"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2; // seules les colonnes Modifier/Supprimer sont éditables
            }
        };

        for (Marque m : marques) {
            tableModel.addRow(new Object[]{
                    m.getIdMarque(),
                    m.getNomMarque(),
                    "Modifier",
                    "Supprimer"
            });
        }

        // -----------------------------
        // Création du JTable
        // -----------------------------
        table = new JTable(tableModel);
        table.getColumn("Modifier").setCellRenderer(new ButtonRenderer());
        table.getColumn("Modifier").setCellEditor(new ButtonEditor(new JCheckBox(), "Modifier"));
        table.getColumn("Supprimer").setCellRenderer(new ButtonRenderer());
        table.getColumn("Supprimer").setCellEditor(new ButtonEditor(new JCheckBox(), "Supprimer"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // -----------------------------
        // Panneau des boutons en bas
        // -----------------------------
        JButton addButton = new JButton("Ajouter une marque");
        addButton.addActionListener(e -> showMarqueForm(null));

        // Nouveau bouton Fermer pour fermer la fenêtre
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose()); // ferme uniquement cette fenêtre

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        bottomPanel.add(closeButton); // ajout du bouton Fermer
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ================== FORMULAIRE AJOUT / MODIFICATION ==================
    private void showMarqueForm(Marque marque) {
        JDialog dialog = new JDialog(this, "Formulaire marque", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridLayout(2, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JLabel nomLabel = new JLabel("Nom :");
        JTextField nomField = new JTextField();
        if (marque != null) nomField.setText(marque.getNomMarque());

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            if (nom.isEmpty()) { 
                JOptionPane.showMessageDialog(dialog, "Nom requis !");
                return; 
            }

            boolean success;
            if (marque == null) success = marqueController.createMarque(nom);
            else success = marqueController.modifyMarque(marque.getIdMarque(), nom);

            if (success) JOptionPane.showMessageDialog(dialog, (marque == null ? "Ajouté !" : "Modifié !"));
            else JOptionPane.showMessageDialog(dialog, "Erreur ou doublon !");

            dialog.dispose();
            refreshTable();
        });

        dialog.add(nomLabel);
        dialog.add(nomField);
        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    // ================== RAFRAÎCHIR LE TABLEAU ==================
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Marque m : marqueController.fetchAllMarques()) {
            tableModel.addRow(new Object[]{
                    m.getIdMarque(),
                    m.getNomMarque(),
                    "Modifier",
                    "Supprimer"
            });
        }
    }

    // ================== CLASSES INTERNES POUR LES BOUTONS ==================
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
                    int confirm = JOptionPane.showConfirmDialog(MarqueView.this, "Supprimer cette marque ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = marqueController.removeMarque(id);
                        JOptionPane.showMessageDialog(MarqueView.this, success ? "Supprimé !" : "Erreur !");
                        refreshTable();
                    }
                } else if (label.equals("Modifier")) {
                    Marque m = marqueController.findMarqueById(id);
                    if (m != null) showMarqueForm(m);
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
