package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.RoomAvailabilityResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduleSlotDialog extends JDialog {

    private final AdminClient admin;
    private final int sectionCapacity;
    private static final Logger LOGGER = Logger.getLogger(ScheduleSlotDialog.class.getName());

    private final JComboBox<DayOfWeek> cbDay = new JComboBox<>(DayOfWeek.values());
    private final JComboBox<String> cbStartTime = new JComboBox<>();
    private final JComboBox<String> cbEndTime = new JComboBox<>();
    private final JTable roomTable;
    private List<RoomAvailabilityResponse> roomList = new ArrayList<>();
    private RoomAvailabilityResponse selectedRoom;

    public ScheduleSlotDialog(Frame parent, AdminClient admin, int sectionCapacity) {
        super(parent, "Schedule New Slot", true);
        this.admin = admin;
        this.sectionCapacity = sectionCapacity;

        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setPreferredSize(new Dimension(600, 500));
        
        JPanel formContainer = new JPanel(new BorderLayout(20, 20));
        formContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Schedule New Slot", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formContainer.add(title, BorderLayout.NORTH);

        // Time and Day Selection
        JPanel topPanel = new JPanel(new GridLayout(2, 3, 15, 10));
        topPanel.setOpaque(false);
        
        populateTimeComboBoxes();

        topPanel.add(new JLabel("Day:"));
        topPanel.add(new JLabel("Start Time:"));
        topPanel.add(new JLabel("End Time:"));
        topPanel.add(cbDay);
        topPanel.add(cbStartTime);
        topPanel.add(cbEndTime);
        
        JButton findButton = new JButton("Find Available Rooms");
        stylePrimaryButton(findButton);
        findButton.addActionListener(e -> findRooms());
        
        JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        findPanel.setOpaque(false);
        findPanel.add(findButton);

        JPanel selectionPanel = new JPanel(new BorderLayout(10,10));
        selectionPanel.setOpaque(false);
        selectionPanel.add(topPanel, BorderLayout.CENTER);
        selectionPanel.add(findPanel, BorderLayout.SOUTH);

        formContainer.add(selectionPanel, BorderLayout.CENTER);

        // Room Table
        roomTable = new JTable(new DefaultTableModel(new Object[]{"Room Number", "Building", "Capacity"}, 0));
        styleTable(roomTable);
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        
        // Main content split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formContainer, scrollPane);
        splitPane.setDividerLocation(200);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addButton = new JButton("Add Slot");
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> addSlot());

        JButton cancelButton = new JButton("Cancel");
        styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> {
            selectedRoom = null;
            dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void populateTimeComboBoxes() {
        LocalTime time = LocalTime.of(9, 0);
        while (time.isBefore(LocalTime.of(17, 0))) {
            cbStartTime.addItem(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            cbEndTime.addItem(time.plusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm")));
            time = time.plusMinutes(30);
        }
    }

    private void findRooms() {
        DayOfWeek day = (DayOfWeek) cbDay.getSelectedItem();
        LocalTime startTime = LocalTime.parse((String) cbStartTime.getSelectedItem());
        LocalTime endTime = LocalTime.parse((String) cbEndTime.getSelectedItem());

        SwingWorker<List<RoomAvailabilityResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RoomAvailabilityResponse> doInBackground() throws Exception {
                return admin.availableRooms(day, startTime, endTime, sectionCapacity);
            }

            @Override
            protected void done() {
                try {
                    roomList = get();
                    DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
                    model.setRowCount(0);
                    if (roomList != null) {
                        roomList.forEach(r -> model.addRow(new Object[]{r.roomNumber(), r.building(), r.capacity()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to find available rooms", e);
                }
            }
        };
        worker.execute();
    }

    private void addSlot() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedRoom = roomList.get(selectedRow);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room from the table.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    public RoomAvailabilityResponse getSelectedRoom() { return selectedRoom; }
    public DayOfWeek getSelectedDay() { return (DayOfWeek) cbDay.getSelectedItem(); }
    public LocalTime getSelectedStartTime() { return LocalTime.parse((String) cbStartTime.getSelectedItem()); }
    public LocalTime getSelectedEndTime() { return LocalTime.parse((String) cbEndTime.getSelectedItem()); }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(225, 225, 225));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
    }
}
