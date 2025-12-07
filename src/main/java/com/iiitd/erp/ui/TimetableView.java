package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.SectionScheduleRequest;
import com.iiitd.erp.io.dto.response.RoomAvailabilityResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.io.dto.response.SectionScheduleResponse;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimetableView extends JPanel {

    private final AdminClient admin;
    private final SectionResponse section;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(TimetableView.class.getName());

    private final JPanel gridPanel = new JPanel(new GridBagLayout());
    private final List<TimetableSlot> scheduleSlots = new ArrayList<>();

    public TimetableView(AdminClient admin, SectionResponse section, Runnable onBack) {
        this.admin = admin;
        this.section = section;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Schedule for " + section.getCourseId() + " (" + section.getSectionId() + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 50, 65));
        add(title, BorderLayout.NORTH);

        add(createTimetableGrid(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton addButton = new JButton("Add New Slot");
        addButton.addActionListener(e -> openScheduleDialog());
        buttonPanel.add(addButton);

        JButton clearButton = new JButton("Clear Schedule");
        clearButton.addActionListener(e -> clearSchedule());
        buttonPanel.add(clearButton);
        
        JButton backButton = new JButton("Back to Selection");
        backButton.addActionListener(e -> onBack.run());
        buttonPanel.add(backButton);
        
        JButton saveButton = new JButton("Save Schedule");
        saveButton.addActionListener(e -> saveSchedule());
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);

        loadSchedule();
    }

    private JScrollPane createTimetableGrid() {
        gridPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (int i = 0; i < days.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.weightx = (i == 0) ? 0 : 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gridPanel.add(createHeaderLabel(days[i]), gbc);
        }

        for (int i = 0; i < 16; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.VERTICAL;
            LocalTime time = LocalTime.of(9, 0).plusMinutes(30 * i);
            gridPanel.add(createHeaderLabel(time.toString()), gbc);
        }
        
        return new JScrollPane(gridPanel);
    }

    private void loadSchedule() {
        SwingWorker<SectionScheduleResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected SectionScheduleResponse doInBackground() throws Exception {
                return admin.viewSchedule(section.getSectionId());
            }

            @Override
            protected void done() {
                try {
                    SectionScheduleResponse schedule = get();
                    if (schedule != null) {
                        scheduleSlots.clear();
                        scheduleSlots.addAll(schedule.slots());
                        updateGrid();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load schedule", e);
                }
            }
        };
        worker.execute();
    }
    
    private void updateGrid() {
        Component[] components = gridPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                gridPanel.remove(component);
            }
        }

        for (TimetableSlot slot : scheduleSlots) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = slot.day().getValue();
            
            long startMinutes = ChronoUnit.MINUTES.between(LocalTime.of(9, 0), slot.startTime());
            gbc.gridy = (int) (startMinutes / 30) + 1;
            
            long durationMinutes = ChronoUnit.MINUTES.between(slot.startTime(), slot.endTime());
            gbc.gridheight = (int) (durationMinutes / 30);
            
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2, 2, 2, 2);
            
            gridPanel.add(createSchedulePill(slot), gbc);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createSchedulePill(TimetableSlot slot) {
        JPanel pill = new JPanel(new BorderLayout());
        pill.setBackground(new Color(60, 120, 215));
        pill.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        JLabel courseLabel = new JLabel(section.getCourseId(), SwingConstants.CENTER);
        courseLabel.setForeground(Color.WHITE);
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel roomLabel = new JLabel(slot.room(), SwingConstants.CENTER);
        roomLabel.setForeground(Color.WHITE);
        
        pill.add(courseLabel, BorderLayout.CENTER);
        pill.add(roomLabel, BorderLayout.SOUTH);
        return pill;
    }

    private void openScheduleDialog() {
        ScheduleSlotDialog dialog = new ScheduleSlotDialog((Frame) SwingUtilities.getWindowAncestor(this), admin, section.getCapacity());
        dialog.setVisible(true);
        RoomAvailabilityResponse selectedRoom = dialog.getSelectedRoom();
        if (selectedRoom != null) {
            DayOfWeek day = dialog.getSelectedDay();
            LocalTime startTime = dialog.getSelectedStartTime();
            LocalTime endTime = dialog.getSelectedEndTime();
            
            TimetableSlot newSlot = new TimetableSlot(day, startTime, endTime, selectedRoom.roomNumber());
            scheduleSlots.add(newSlot);
            updateGrid();
        }
    }

    private void saveSchedule() {
        SectionScheduleRequest request = new SectionScheduleRequest(scheduleSlots);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                admin.upsertSchedule(section.getSectionId(), request);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(TimetableView.this, "Schedule saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to save schedule", e);
                    JOptionPane.showMessageDialog(TimetableView.this, "Error saving schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void clearSchedule() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the entire schedule for this section?", "Confirm Clear", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                admin.clearSchedule(section.getSectionId());
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    scheduleSlots.clear();
                    updateGrid();
                    JOptionPane.showMessageDialog(TimetableView.this, "Schedule cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to clear schedule", e);
                    JOptionPane.showMessageDialog(TimetableView.this, "Error clearing schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        return label;
    }
}
