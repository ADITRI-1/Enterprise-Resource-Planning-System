package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.RoomResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomsPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable roomsTable;
    private List<RoomResponse> roomList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(RoomsPanel.class.getName());
    private final boolean isMaintenanceMode;

    public RoomsPanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Room Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel listRoomsPanel = createListRoomsPanel();
        JPanel createRoomPanel = new CreateRoomPanel(admin, this::showListAndRefresh);

        mainPanel.add(listRoomsPanel, "list");
        mainPanel.add(createRoomPanel, "create");

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

    private JPanel createListRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        roomsTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Building", "Capacity", "Type"}, 0));
        styleTable(roomsTable);

        panel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New Room");
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
            showUpdateRoomPanel();
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            deleteRoom();
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
        SwingWorker<List<RoomResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RoomResponse> doInBackground() {
                try {
                    LOGGER.info("Loading rooms");
                    return admin.listRooms();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load rooms", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    roomList = get();
                    DefaultTableModel model = (DefaultTableModel) roomsTable.getModel();
                    model.setRowCount(0);
                    if (roomList != null) {
                        roomList.forEach(r -> model.addRow(new Object[]{r.roomId(), r.building(), r.capacity(), r.type()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update room table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateRoomPanel() {
        int row = roomsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RoomResponse room = roomList.get(row);
        JPanel updatePanel = new UpdateRoomPanel(admin, this::showListAndRefresh, room);
        mainPanel.add(updatePanel, "updateRoom");
        cardLayout.show(mainPanel, "updateRoom");
    }

    private void deleteRoom() {
        int row = roomsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roomId = roomsTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete room " + roomId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting room: " + roomId);
                    admin.deleteRoom(roomId);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete room " + roomId, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(RoomsPanel.this, "Error deleting room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
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
