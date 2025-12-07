package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.ProgramUpdateRequest;
import com.iiitd.erp.io.dto.response.ProgramResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProgramPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private final ProgramResponse program;
    private static final Logger LOGGER = Logger.getLogger(UpdateProgramPanel.class.getName());

    private final JTextField txtDesc;
    private final JComboBox<String> cbType;
    private final JComboBox<String> cbDuration;

    public UpdateProgramPanel(AdminClient admin, Runnable onBack, ProgramResponse program) {
        this.admin = admin;
        this.onBack = onBack;
        this.program = program;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 150, 20, 150));

        // Header
        JLabel title = new JLabel("Update Program: " + program.programId(), SwingConstants.CENTER);
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

        txtDesc = new JTextField(program.programDesc(), 20);
        cbType = new JComboBox<>(new String[]{"B.Tech", "M.Tech", "PhD"});
        cbType.setSelectedItem(program.programType());
        cbDuration = new JComboBox<>(new String[]{"2", "4", "5"});
        cbDuration.setSelectedItem(String.valueOf(program.duration()));

        addField(formPanel, gbc, 0, "Program Description:", txtDesc);
        addField(formPanel, gbc, 1, "Program Type:", cbType);
        addField(formPanel, gbc, 2, "Duration (years):", cbDuration);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton updateButton = new JButton("Save Changes");
        stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateProgram());

        JButton backButton = new JButton("Back to List");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(updateButton);
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
        } else if (component instanceof JComboBox) {
            styleComboBox((JComboBox<?>) component);
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

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
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

    private void updateProgram() {
        ProgramUpdateRequest request = ProgramUpdateRequest.builder()
                .programDesc(txtDesc.getText().trim())
                .programType(cbType.getSelectedItem().toString())
                .duration(Integer.parseInt(cbDuration.getSelectedItem().toString()))
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Updating program: " + program.programId());
                    admin.updateProgram(program.programId(), request);
                    JOptionPane.showMessageDialog(UpdateProgramPanel.this, "Program updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update program", ex);
                    JOptionPane.showMessageDialog(UpdateProgramPanel.this, "Error updating program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
