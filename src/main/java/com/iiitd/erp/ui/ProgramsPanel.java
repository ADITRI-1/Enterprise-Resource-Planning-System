package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.ProgramResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgramsPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable programsTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<ProgramResponse> programList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(ProgramsPanel.class.getName());
    private final boolean isMaintenanceMode;

    public ProgramsPanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Program Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createFilterBar(), BorderLayout.NORTH);
        
        mainPanel.add(createListProgramsPanel(), "list");
        mainPanel.add(new CreateProgramPanel(admin, this::showListAndRefresh), "create");
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "list");
        loadData();
    }

    private JPanel createFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        JTextField filterField = new JTextField(30);
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = filterField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);
        return filterPanel;
    }

    private void showListAndRefresh() {
        loadData();
        cardLayout.show(mainPanel, "list");
    }

    private JPanel createListProgramsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Description", "Type", "Duration"}, 0);
        programsTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        programsTable.setRowSorter(sorter);
        styleTable(programsTable);

        panel.add(new JScrollPane(programsTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New Program");
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            cardLayout.show(mainPanel, "create");
        });

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryButton(editButton);
        editButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            showUpdateProgramPanel();
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            deleteProgram();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    public void loadData() {
        SwingWorker<List<ProgramResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProgramResponse> doInBackground() {
                try {
                    LOGGER.info("Loading programs");
                    return admin.listPrograms();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load programs", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    programList = get();
                    DefaultTableModel model = (DefaultTableModel) programsTable.getModel();
                    model.setRowCount(0);
                    if (programList != null) {
                        programList.forEach(p -> model.addRow(new Object[]{p.programId(), p.programDesc(), p.programType(), p.duration()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update program table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateProgramPanel() {
        int viewRow = programsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a program to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = programsTable.convertRowIndexToModel(viewRow);
        ProgramResponse program = programList.get(modelRow);
        JPanel updatePanel = new UpdateProgramPanel(admin, this::showListAndRefresh, program);
        mainPanel.add(updatePanel, "updateProgram");
        cardLayout.show(mainPanel, "updateProgram");
    }

    private void deleteProgram() {
        int viewRow = programsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a program to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = programsTable.convertRowIndexToModel(viewRow);
        String programId = programList.get(modelRow).programId();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete program " + programId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting program: " + programId);
                    admin.deleteProgram(programId);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete program " + programId, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ProgramsPanel.this, "Error deleting program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                loadData();
            }
        };
        worker.execute();
    }
    
    private void showMaintenanceMessage() {
        JOptionPane.showMessageDialog(this,
                "Action disabled: The application is currently in maintenance mode.",
                "Maintenance Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleDeleteButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(220, 50, 50));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
