package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.DepartmentResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DepartmentsPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable departmentsTable;
    private static final Logger LOGGER = Logger.getLogger(DepartmentsPanel.class.getName());

    public DepartmentsPanel(AdminClient admin) {
        this.admin = admin;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Department Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel listDepartmentsPanel = createListDepartmentsPanel();
        JPanel createDepartmentPanel = new CreateDepartmentPanel(admin, this::showListAndRefresh);

        mainPanel.add(listDepartmentsPanel, "list");
        mainPanel.add(createDepartmentPanel, "create");

        cardLayout.show(mainPanel, "list");
        loadDepartments();
    }

    private void showListAndRefresh() {
        loadDepartments();
        cardLayout.show(mainPanel, "list");
    }

    private JPanel createListDepartmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        departmentsTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Description"}, 0));
        styleTable(departmentsTable);

        panel.add(new JScrollPane(departmentsTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New Department");
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> cardLayout.show(mainPanel, "create"));

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryButton(editButton);
        editButton.addActionListener(e -> showUpdateDepartmentPanel());

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> deleteDepartment());

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

    private void loadDepartments() {
        SwingWorker<List<DepartmentResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DepartmentResponse> doInBackground() {
                try {
                    LOGGER.info("Loading departments");
                    return admin.listDepartments();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load departments", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<DepartmentResponse> rows = get();
                    DefaultTableModel model = (DefaultTableModel) departmentsTable.getModel();
                    model.setRowCount(0);
                    if (rows != null) {
                        rows.forEach(d -> model.addRow(new Object[]{d.deptId(), d.deptDesc()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update department table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateDepartmentPanel() {
        int row = departmentsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a department to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DepartmentResponse department = new DepartmentResponse(
            (String) departmentsTable.getValueAt(row, 0),
            (String) departmentsTable.getValueAt(row, 1)
        );

        JPanel updatePanel = new UpdateDepartmentPanel(admin, this::showListAndRefresh, department);
        mainPanel.add(updatePanel, "updateDepartment");
        cardLayout.show(mainPanel, "updateDepartment");
    }

    private void deleteDepartment() {
        int row = departmentsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a department to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String deptId = departmentsTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete department " + deptId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting department: " + deptId);
                    admin.deleteDepartment(deptId);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete department " + deptId, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(DepartmentsPanel.this, "Error deleting department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                loadDepartments();
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
