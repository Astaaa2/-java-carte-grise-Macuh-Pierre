package views;

import controllers.PossederController;
import controllers.ProprietaireController;
import controllers.VehiculeController;
import models.Posseder;
import models.Proprietaire;
import models.Vehicule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Vue pour gérer les possessions
 * ---------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre la notion de Vue dans MVC (affichage + interaction)
 * 2. Utiliser JTable pour lister les données
 * 3. Ajouter, modifier et supprimer une possession via un formulaire
 * 4. Champs obligatoires : Propriétaire, Véhicule, Date début
 *    Champ facultatif : Date fin
 * 5. Validation stricte du format de date : JJ/MM/AAAA
 * 6. Gérer les boutons Modifier / Supprimer dans JTable
 * 7. Formulaire reste ouvert si erreur ou doublon
 */
public class PossederView extends JFrame {

    private PossederController possederController;
    private ProprietaireController proprietaireController;
    private VehiculeController vehiculeController;
    private DefaultTableModel tableModel;
    private JTable table;

    // ================== CONSTRUCTEUR ==================
    public PossederView(PossederController pc, ProprietaireController prc, VehiculeController vc) {
        this.possederController = pc;
        this.proprietaireController = prc;
        this.vehiculeController = vc;

        // -------- Paramètres de la fenêtre --------
        setTitle("Liste des possessions");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // -------- Colonnes du tableau --------
        String[] colonnes = {"Propriétaire", "Véhicule", "Date début", "Date fin", "Modifier", "Supprimer"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 4; // seules les colonnes Modifier et Supprimer sont éditables
            }
        };

        // -------- Création du JTable --------
        table = new JTable(tableModel);
        table.getColumn("Modifier").setCellRenderer(new ButtonRenderer());
        table.getColumn("Modifier").setCellEditor(new ButtonEditor(new JCheckBox(), "Modifier"));
        table.getColumn("Supprimer").setCellRenderer(new ButtonRenderer());
        table.getColumn("Supprimer").setCellEditor(new ButtonEditor(new JCheckBox(), "Supprimer"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // -------- Panneau des boutons --------
        JButton addButton = new JButton("Ajouter une possession");
        addButton.addActionListener(e -> showPossederForm(null));
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // -------- Remplissage initial du tableau --------
        refreshTable();
    }

    // ================== FORMULAIRE AJOUT / MODIFICATION ==================
    private void showPossederForm(Posseder p) {
        // Création d'une fenêtre modale pour le formulaire
        JDialog dialog = new JDialog(this, "Formulaire possession", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        // -------- Champs du formulaire --------
        JComboBox<Proprietaire> proprietaireBox = new JComboBox<>(proprietaireController.fetchAllProprietaires().toArray(new Proprietaire[0]));
        JComboBox<Vehicule> vehiculeBox = new JComboBox<>(vehiculeController.fetchAllVehicules().toArray(new Vehicule[0]));
        JTextField dateDebutField = new JTextField();
        JTextField dateFinField = new JTextField(); // facultatif

        // Pré-remplir les champs si modification
        if (p != null) {
            proprietaireBox.setSelectedItem(proprietaireController.findProprietaireById(p.getIdProprietaire()));
            vehiculeBox.setSelectedItem(vehiculeController.findVehiculeById(p.getIdVehicule()));
            dateDebutField.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateDebut()));
            if (p.getDateFin() != null) dateFinField.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateFin()));
        }

        // -------- Bouton Enregistrer --------
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(e -> {
            Proprietaire pr = (Proprietaire) proprietaireBox.getSelectedItem();
            Vehicule v = (Vehicule) vehiculeBox.getSelectedItem();

            // Vérification des champs obligatoires
            if (pr == null || v == null || dateDebutField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Propriétaire, Véhicule et Date début sont obligatoires !");
                return; // formulaire reste ouvert
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false); // permet de rejeter les dates incorrectes

            try {
                // Conversion et validation des dates
                java.util.Date utilDateDebut = sdf.parse(dateDebutField.getText().trim());
                java.sql.Date dateDebut = new java.sql.Date(utilDateDebut.getTime());

                java.sql.Date dateFin = null;
                if (!dateFinField.getText().trim().isEmpty()) {
                    java.util.Date utilDateFin = sdf.parse(dateFinField.getText().trim());
                    dateFin = new java.sql.Date(utilDateFin.getTime());
                }

                // Tentative d'enregistrement
                boolean success;
                if (p == null) {
                    success = possederController.createPossession(pr.getIdProprietaire(), v.getIdVehicule(), dateDebut, dateFin);
                } else {
                    success = possederController.modifyPossession(pr.getIdProprietaire(), v.getIdVehicule(), dateDebut, dateFin);
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Enregistré !");
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur ou doublon ! Veuillez vérifier les informations.");
                    // formulaire reste ouvert
                }

            } catch (ParseException ex) {
                // Date non conforme au format JJ/MM/AAAA
                JOptionPane.showMessageDialog(dialog, "Date invalide ! Veuillez saisir les dates au format JJ/MM/AAAA");
                // formulaire reste ouvert, pas d'enregistrement
            }
        });

        // -------- Ajout des composants au formulaire --------
        dialog.add(new JLabel("Propriétaire:")); dialog.add(proprietaireBox);
        dialog.add(new JLabel("Véhicule:")); dialog.add(vehiculeBox);
        dialog.add(new JLabel("Date début (JJ/MM/AAAA):")); dialog.add(dateDebutField);
        dialog.add(new JLabel("Date fin (JJ/MM/AAAA):")); dialog.add(dateFinField);
        dialog.add(new JLabel()); dialog.add(saveButton);

        dialog.setVisible(true);
    }

    // ================== RAFRAÎCHIR LE TABLEAU ==================
    private void refreshTable() {
        tableModel.setRowCount(0); // vide le tableau
        for (Posseder p : possederController.fetchAllPossessions()) {
            Proprietaire pr = proprietaireController.findProprietaireById(p.getIdProprietaire());
            Vehicule v = vehiculeController.findVehiculeById(p.getIdVehicule());
            tableModel.addRow(new Object[]{
                    pr != null ? pr.toString() : "Inconnu",
                    v != null ? v.toString() : "Inconnu",
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateDebut()),
                    p.getDateFin() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getDateFin()) : "",
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
                String propName = (String) table.getValueAt(row, 0);
                String vehName = (String) table.getValueAt(row, 1);

                Proprietaire pr = proprietaireController.fetchAllProprietaires()
                        .stream().filter(p -> p.toString().equals(propName)).findFirst().orElse(null);
                Vehicule v = vehiculeController.fetchAllVehicules()
                        .stream().filter(veh -> veh.toString().equals(vehName)).findFirst().orElse(null);

                if (pr == null || v == null) return label;

                if (label.equals("Supprimer")) {
                    int confirm = JOptionPane.showConfirmDialog(PossederView.this, "Supprimer cette possession ?", "Confirmer", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = possederController.removePossession(pr.getIdProprietaire(), v.getIdVehicule());
                        JOptionPane.showMessageDialog(PossederView.this, success ? "Supprimé !" : "Erreur !");
                        refreshTable();
                    }
                } else if (label.equals("Modifier")) {
                    Posseder p = possederController.fetchAllPossessions()
                            .stream().filter(pos -> pos.getIdProprietaire() == pr.getIdProprietaire() &&
                                                    pos.getIdVehicule() == v.getIdVehicule())
                            .findFirst().orElse(null);
                    if (p != null) showPossederForm(p);
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
