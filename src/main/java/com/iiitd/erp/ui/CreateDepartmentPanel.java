package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.DepartmentCreateRequest;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateDepartmentPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateDepartmentPanel.class.getName());

    private final JTextField txtId = new JTextField(20);
    private final JTextField txtDesc = new JTextField(20);

    public CreateDepartmentPanel(AdminClient admin, Runnable onBack) {
        this.admin = admin;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 150, 20, 150));

        // Header
        JLabel title = new JLabel("Create New Department", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 30, 0));
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        addField(formPanel, gbc, 0, "Department ID (e.g., CSE):", txtId);
        addField(formPanel, gbc, 1, "Department Description:", txtDesc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Department");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createDepartment());

        JButton backButton = new JButton("Back to List");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        if (component instanceof JTextField) {
            styleTextField((JTextField) component);
        }

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void createDepartment() {
        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                .deptId(txtId.getText().trim())
                .deptDesc(txtDesc.getText().trim())
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating department: " + request);
                    admin.createDepartment(request);
                    JOptionPane.showMessageDialog(CreateDepartmentPanel.this, "Department created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create department", ex);
                    JOptionPane.showMessageDialog(CreateDepartmentPanel.this, "Error creating department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
