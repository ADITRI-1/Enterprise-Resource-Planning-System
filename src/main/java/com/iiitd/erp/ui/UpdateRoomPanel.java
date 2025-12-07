package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.RoomUpdateRequest;
import com.iiitd.erp.io.dto.response.RoomResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateRoomPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private final RoomResponse room;
    private static final Logger LOGGER = Logger.getLogger(UpdateRoomPanel.class.getName());

    private final JTextField txtBuilding;
    private final JTextField txtCapacity;
    private final JComboBox<String> cbType;

    public UpdateRoomPanel(AdminClient admin, Runnable onBack, RoomResponse room) {
        this.admin = admin;
        this.onBack = onBack;
        this.room = room;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 150, 20, 150));

        // Header
        JLabel title = new JLabel("Update Room: " + room.roomNumber(), SwingConstants.CENTER);
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

        txtBuilding = new JTextField(room.building(), 20);
        txtCapacity = new JTextField(String.valueOf(room.capacity()), 20);
        cbType = new JComboBox<>(new String[]{"Lecture Hall", "Lab", "Seminar Room"});
        cbType.setSelectedItem(room.type());

        addField(formPanel, gbc, 0, "Building:", txtBuilding);
        addField(formPanel, gbc, 1, "Capacity:", txtCapacity);
        addField(formPanel, gbc, 2, "Room Type:", cbType);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton updateButton = new JButton("Save Changes");
        stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateRoom());

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

    private void updateRoom() {
        RoomUpdateRequest request = new RoomUpdateRequest(
                txtBuilding.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim()),
                cbType.getSelectedItem().toString()
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Updating room: " + room.roomId());
                    admin.updateRoom(room.roomId(), request);
                    JOptionPane.showMessageDialog(UpdateRoomPanel.this, "Room updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update room", ex);
                    JOptionPane.showMessageDialog(UpdateRoomPanel.this, "Error updating room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
