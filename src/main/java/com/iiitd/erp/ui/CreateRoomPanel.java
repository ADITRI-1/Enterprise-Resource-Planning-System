package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.RoomCreateRequest;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateRoomPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateRoomPanel.class.getName());

    private final JTextField txtRoomNumber = new JTextField(20);
    private final JTextField txtBuilding = new JTextField(20);
    private final JTextField txtCapacity = new JTextField(20);
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"Lecture Hall", "Lab", "Seminar Room"});

    public CreateRoomPanel(AdminClient admin, Runnable onBack) {
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

        // Header
        JLabel title = new JLabel("Create New Room", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addField(formPanel, gbc, 0, "Room Number:", txtRoomNumber);
        addField(formPanel, gbc, 1, "Building:", txtBuilding);
        addField(formPanel, gbc, 2, "Capacity:", txtCapacity);
        addField(formPanel, gbc, 3, "Room Type:", cbType);

        formContainer.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Room");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createRoom());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1;
        mainGbc.weighty = 1;
        mainGbc.anchor = GridBagConstraints.CENTER;
        add(formContainer, mainGbc);
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

    private void createRoom() {
        RoomCreateRequest request = new RoomCreateRequest(
                txtRoomNumber.getText().trim(),
                txtBuilding.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim()),
                cbType.getSelectedItem().toString()
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating room: " + request);
                    admin.createRoom(request);
                    JOptionPane.showMessageDialog(CreateRoomPanel.this, "Room created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create room", ex);
                    JOptionPane.showMessageDialog(CreateRoomPanel.this, "Error creating room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
