package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.InstructorAvailabilityResponse;
import com.iiitd.erp.io.dto.response.RoomAvailabilityResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduleSlotDialog extends JDialog {

    private final AdminClient admin;
    private final int sectionCapacity;
    private final String instructorCode;
    private final int year;
    private final String semester;
    private static final Logger LOGGER = Logger.getLogger(ScheduleSlotDialog.class.getName());

    private final JComboBox<DayOfWeek> cbDay = new JComboBox<>(DayOfWeek.values());
    private final JComboBox<String> cbStartTime = new JComboBox<>();
    private final JComboBox<String> cbEndTime = new JComboBox<>();
    private final JTable roomTable;
    private List<RoomAvailabilityResponse> roomList;
    private RoomAvailabilityResponse selectedRoom;
    private final JLabel statusLabel = new JLabel(" ");

    public ScheduleSlotDialog(Frame parent, AdminClient admin, int sectionCapacity, String instructorCode, int year, String semester) {
        super(parent, "Schedule New Slot", true);
        this.admin = admin;
        this.sectionCapacity = sectionCapacity;
        this.instructorCode = instructorCode;
        this.year = year;
        this.semester = semester;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(700, 550));
        
        JPanel formContainer = new JPanel(new BorderLayout(15, 15));
        formContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);

        JLabel title = new JLabel("Schedule New Slot", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formContainer.add(title, BorderLayout.NORTH);

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
        findButton.addActionListener(e -> findRoomsAndCheckInstructor());
        
        JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        findPanel.setOpaque(false);
        findPanel.add(findButton);

        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel selectionPanel = new JPanel(new BorderLayout(10,10));
        selectionPanel.setOpaque(false);
        selectionPanel.add(topPanel, BorderLayout.CENTER);
        selectionPanel.add(findPanel, BorderLayout.SOUTH);
        selectionPanel.add(statusLabel, BorderLayout.NORTH);

        formContainer.add(selectionPanel, BorderLayout.CENTER);

        roomTable = new JTable(new DefaultTableModel(new Object[]{"Room ID", "Building", "Capacity"}, 0));
        styleTable(roomTable);
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formContainer, scrollPane);
        splitPane.setDividerLocation(250);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);

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

    private void findRoomsAndCheckInstructor() {
        statusLabel.setText("Checking availability...");
        DayOfWeek day = (DayOfWeek) cbDay.getSelectedItem();
        LocalTime startTime = LocalTime.parse((String) cbStartTime.getSelectedItem());
        LocalTime endTime = LocalTime.parse((String) cbEndTime.getSelectedItem());

        // SwingWorker to perform network calls off the EDT
        SwingWorker<List<RoomAvailabilityResponse>, Void> worker = new SwingWorker<>() {
            private boolean isInstructorAvailable = false;

            @Override
            protected List<RoomAvailabilityResponse> doInBackground() throws Exception {
                // 1. Check instructor availability first
                InstructorAvailabilityResponse availability = admin.instructorAvailability(instructorCode, semester, year);
                isInstructorAvailable = isInstructorAvailable(availability, day, startTime, endTime);

                // 2. If instructor is available, find rooms
                if (isInstructorAvailable) {
                    return admin.availableRooms(day, startTime, endTime, sectionCapacity);
                }
                return null; // Don't fetch rooms if instructor is busy
            }

            @Override
            protected void done() {
                try {
                    if (!isInstructorAvailable) {
                        statusLabel.setText("Instructor is unavailable at this time.");
                        ((DefaultTableModel) roomTable.getModel()).setRowCount(0); // Clear table
                        return;
                    }

                    roomList = get();
                    DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
                    model.setRowCount(0);

                    if (roomList != null && !roomList.isEmpty()) {
                        statusLabel.setText("Please select a room from the list below.");
                        statusLabel.setForeground(new Color(0, 128, 0)); // Green for success
                        roomList.forEach(r -> model.addRow(new Object[]{r.roomId(), r.building(), r.capacity()}));
                    } else {
                        statusLabel.setText("No available rooms found for this time slot.");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Failed to find available rooms or instructor availability", e);
                    statusLabel.setText("Error checking availability.");
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void addSlot() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow != -1 && roomList != null) {
            selectedRoom = roomList.get(selectedRow);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Please find and select a room from the table.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean isInstructorAvailable(InstructorAvailabilityResponse availability, DayOfWeek day, LocalTime start, LocalTime end) {
        if (availability == null || availability.busy() == null) {
            return true;
        }
        List<InstructorAvailabilityResponse.BusySlot> busySlots = availability.busy().get(day);
        if (busySlots == null) {
            return true;
        }
        return busySlots.stream().noneMatch(slot -> start.isBefore(slot.end()) && end.isAfter(slot.start()));
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
