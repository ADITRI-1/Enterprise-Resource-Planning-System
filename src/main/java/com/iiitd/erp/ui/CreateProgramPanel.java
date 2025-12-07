package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.ProgramCreateRequest;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateProgramPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateProgramPanel.class.getName());

    private final JTextField txtId = new JTextField(20);
    private final JTextField txtDesc = new JTextField(20);
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"B.Tech", "M.Tech", "PhD"});
    private final JComboBox<String> cbDuration = new JComboBox<>(new String[]{"2", "4", "5"});

    public CreateProgramPanel(AdminClient admin, Runnable onBack) {
        this.admin = admin;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel(new BorderLayout(0, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(40, 50, 40, 50)
        ));

        JLabel title = new JLabel("Create New Program", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addField(formPanel, gbc, 0, "Program ID (e.g., BTECH-CSE):", txtId);
        addField(formPanel, gbc, 1, "Program Description:", txtDesc);
        addField(formPanel, gbc, 2, "Program Type:", cbType);
        addField(formPanel, gbc, 3, "Duration (years):", cbDuration);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Program");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createProgram());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        add(formContainer, mainGbc);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                resetForm();
            }
        });
    }

    private void resetForm() {
        txtId.setText("");
        txtDesc.setText("");
        if (cbType.getItemCount() > 0) cbType.setSelectedIndex(0);
        if (cbDuration.getItemCount() > 0) cbDuration.setSelectedIndex(0);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        if (component instanceof JTextField) styleTextField((JTextField) component);
        else if (component instanceof JComboBox) styleComboBox((JComboBox<?>) component);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            new EmptyBorder(10, 12, 10, 12)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 35, 12, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(225, 225, 225));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 35, 12, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void createProgram() {
        if (txtId.getText().trim().isEmpty() || txtDesc.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProgramCreateRequest request = ProgramCreateRequest.builder()
                .programId(txtId.getText().trim())
                .programDesc(txtDesc.getText().trim())
                .programType(cbType.getSelectedItem().toString())
                .duration(Integer.parseInt(cbDuration.getSelectedItem().toString()))
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating program: " + request);
                    admin.createProgram(request);
                    JOptionPane.showMessageDialog(CreateProgramPanel.this, "Program created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create program", ex);
                    JOptionPane.showMessageDialog(CreateProgramPanel.this, "Error creating program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
