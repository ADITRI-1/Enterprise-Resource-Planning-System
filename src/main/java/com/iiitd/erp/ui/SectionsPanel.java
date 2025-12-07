package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SectionsPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable sectionsTable;
    private List<SectionResponse> sectionList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(SectionsPanel.class.getName());
    private final boolean isMaintenanceMode;

    public SectionsPanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Section Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel listSectionsPanel = createListSectionsPanel();
        JPanel createSectionPanel = new CreateSectionPanel(admin, this::showListAndRefresh);

        mainPanel.add(listSectionsPanel, "list");
        mainPanel.add(createSectionPanel, "create");

        cardLayout.show(mainPanel, "list");
        loadData();
    }

    private void showListAndRefresh() {
        loadData();
        cardLayout.show(mainPanel, "list");
    }
    
    private void showMaintenanceMessage() {
        JOptionPane.showMessageDialog(this,
                "Action disabled: The application is currently in maintenance mode.",
                "Maintenance Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createListSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        sectionsTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Course", "Instructor", "Semester", "Year", "Capacity"}, 0));
        styleTable(sectionsTable);

        panel.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New Section");
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
            showUpdateSectionPanel();
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            deleteSection();
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
        SwingWorker<List<SectionResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SectionResponse> doInBackground() {
                try {
                    LOGGER.info("Loading sections");
                    return admin.listSections();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load sections", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    sectionList = get();
                    DefaultTableModel model = (DefaultTableModel) sectionsTable.getModel();
                    model.setRowCount(0);
                    if (sectionList != null) {
                        sectionList.forEach(s -> model.addRow(new Object[]{s.sectionId(), s.courseId(), s.instructorCode(), s.semester(), s.year(), s.capacity()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update section table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateSectionPanel() {
        int row = sectionsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SectionResponse section = sectionList.get(row);
        JPanel updatePanel = new UpdateSectionPanel(admin, this::showListAndRefresh, section);
        mainPanel.add(updatePanel, "updateSection");
        cardLayout.show(mainPanel, "updateSection");
    }

    private void deleteSection() {
        int row = sectionsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sectionId = sectionsTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete section " + sectionId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting section: " + sectionId);
                    admin.deleteSection(sectionId);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete section " + sectionId, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(SectionsPanel.this, "Error deleting section: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
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
