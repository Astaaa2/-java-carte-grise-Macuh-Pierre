package views;

import controllers.ProprietaireController;
import models.Proprietaire;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

/**
 * Vue pour gérer la liste des propriétaires
 * --------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de "Vue" dans MVC (affichage et interaction)
 * 2. Utiliser JTable pour lister les données
 * 3. Ajouter, modifier et supprimer un propriétaire via formulaire
 * 4. Tous les champs sont obligatoires : Nom, Prénom, Adresse, CP, Ville
 * 5. Ajouter un bouton "Fermer" pour quitter proprement la fenêtre
 * 6. Formulaire reste ouvert si erreur
 */
public class ProprietaireView extends JFrame {

    private ProprietaireController proprietaireController;
    private DefaultTableModel tableModel;
    private JTable table;

    // ================== CONSTRUCTEUR ==================
    public ProprietaireView(ProprietaireController proprietaireController) {
        this.proprietaireController = proprietaireController;

        // -------- Paramètres de la fenêtre --------
        setTitle("Liste des propriétaires");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // -------- Définition des colonnes du tableau --------
        String[] colonnes = {"ID", "Nom", "Prénom", "Adresse", "CP", "Ville", "Modifier", "Supprimer"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 6; // seules les colonnes Modifier / Supprimer sont éditables
            }
        };

        // -------- Création du tableau graphique --------
        table = new JTable(tableModel);
        table.getColumn("Modifier").setCellRenderer(new ButtonRenderer());
        table.getColumn("Modifier").setCellEditor(new ButtonEditor(new JCheckBox(), "Modifier"));
        table.getColumn("Supprimer").setCellRenderer(new ButtonRenderer());
        table.getColumn("Supprimer").setCellEditor(new ButtonEditor(new JCheckBox(), "Supprimer"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // -------- Panneau des boutons --------
        JButton addButton = new JButton("Ajouter un propriétaire");
        addButton.addActionListener(e -> showProprietaireForm(null));

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose()); // ferme uniquement cette fenêtre

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // -------- Remplissage initial du tableau --------
        refreshTable();
    }

    // ================== FORMULAIRE AJOUT / MODIFICATION ==================
    private void showProprietaireForm(Proprietaire p) {
        JDialog dialog = new JDialog(this, "Formulaire propriétaire", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        // Champs du formulaire
        JTextField nomField = new JTextField();
        JTextField prenomField = new JTextField();
        JTextField adresseField = new JTextField();
        JTextField cpField = new JTextField();
        JTextField villeField = new JTextField();

        // Pré-remplir si modification
        if (p != null) {
            nomField.setText(p.getNom());
            prenomField.setText(p.getPrenom());
            adresseField.setText(p.getAdresse());
            cpField.setText(p.getCp());
            villeField.setText(p.getVille());
        }

        // -------- Bouton Enregistrer --------
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String adresse = adresseField.getText().trim();
            String cp = cpField.getText().trim();
            String ville = villeField.getText().trim();

            // -------- Validation des champs obligatoires --------
            if (nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty() || cp.isEmpty() || ville.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tous les champs sont obligatoires !");
                return; // formulaire reste ouvert
            }

            boolean success;
            if (p == null)
                success = proprietaireController.createProprietaire(nom, prenom, adresse, cp, ville);
            else
                success = proprietaireController.modifyProprietaire(p.getIdProprietaire(), nom, prenom, adresse, cp, ville);

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Enregistré !");
                dialog.dispose(); // fermer le formulaire
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erreur ou doublon ! Veuillez vérifier les informations.");
                // formulaire reste ouvert
            }
        });

        // -------- Ajout des composants au formulaire --------
        dialog.add(new JLabel("Nom:")); dialog.add(nomField);
        dialog.add(new JLabel("Prénom:")); dialog.add(prenomField);
        dialog.add(new JLabel("Adresse:")); dialog.add(adresseField);
        dialog.add(new JLabel("CP:")); dialog.add(cpField);
        dialog.add(new JLabel("Ville:")); dialog.add(villeField);
        dialog.add(new JLabel()); dialog.add(saveButton);

        dialog.setVisible(true);
    }

    // ================== RAFRAÎCHIR LE TABLEAU ==================
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Proprietaire p : proprietaireController.fetchAllProprietaires()) {
            tableModel.addRow(new Object[]{
                    p.getIdProprietaire(),
                    p.getNom(),
                    p.getPrenom(),
                    p.getAdresse(),
                    p.getCp(),
                    p.getVille(),
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
                    int confirm = JOptionPane.showConfirmDialog(ProprietaireView.this,
                            "Supprimer ce propriétaire ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = proprietaireController.removeProprietaire(id);
                        JOptionPane.showMessageDialog(ProprietaireView.this, success ? "Supprimé !" : "Erreur !");
                        refreshTable();
                    }
                } else if (label.equals("Modifier")) {
                    Proprietaire p = proprietaireController.findProprietaireById(id);
                    if (p != null) showProprietaireForm(p);
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
