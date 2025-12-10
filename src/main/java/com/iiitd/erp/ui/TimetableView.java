package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.ScheduleSlotRequest;
import com.iiitd.erp.io.dto.request.SectionScheduleRequest;
import com.iiitd.erp.io.dto.response.*;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TimetableView extends JPanel {

    private final AdminClient admin;
    private final boolean isMaintenanceMode;
    private String viewType;
    private String viewId;
    private SectionResponse currentSection;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(TimetableView.class.getName());

    private final JPanel gridPanel = new JPanel(new GridBagLayout());
    private final List<TimetableSearchSlotResponse> scheduleSlots = new ArrayList<>();
    private final Map<String, Color> courseColorMap = new HashMap<>();
    private final Color[] pillColors = {
        new Color(27, 154, 170), new Color(255, 159, 67), new Color(255, 99, 132),
        new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 205, 86),
        new Color(54, 162, 235), new Color(201, 203, 207)
    };
    private Map<String, RoomResponse> roomCache = new HashMap<>();
    private final JButton addButton = new JButton("Add New Slot");
    private final JButton clearButton = new JButton("Clear Schedule");
    private final JButton saveButton = new JButton("Save Schedule");

    public TimetableView(AdminClient admin, boolean maintenanceMode, Runnable onBack) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Select an item to view its schedule");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 50, 65));
        add(title, BorderLayout.NORTH);

        add(createTimetableGrid(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        addButton.addActionListener(e -> openScheduleDialog());
        buttonPanel.add(addButton);

        clearButton.addActionListener(e -> clearScheduleAction());
        buttonPanel.add(clearButton);
        
        JButton backButton = new JButton("Back to Selection");
        backButton.addActionListener(e -> onBack.run());
        buttonPanel.add(backButton);
        
        saveButton.addActionListener(e -> saveSchedule());
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);

        loadRoomCache();
    }

    public void setView(String viewType, String viewId, SectionResponse section) {
        this.viewType = viewType;
        this.viewId = viewId;
        this.currentSection = section;
        
        ((JLabel)getComponent(0)).setText("Schedule for " + viewType + ": " + viewId);
        
        boolean isSectionView = "Section".equals(viewType);
        addButton.setVisible(!isMaintenanceMode && isSectionView);
        clearButton.setVisible(!isMaintenanceMode && isSectionView);
        saveButton.setVisible(!isMaintenanceMode && isSectionView);

        loadSchedule();
    }

    public void setSchedule(List<TimetableSearchSlotResponse> slots) {
        ((JLabel)getComponent(0)).setText("Timetable Search Results");
        this.currentSection = null;
        this.viewType = "Search";
        scheduleSlots.clear();
        if (slots != null) {
            scheduleSlots.addAll(slots);
        }
        updateGrid();
    }
    
    public void clearScheduleView() {
        scheduleSlots.clear();
        updateGrid();
    }

    private void loadRoomCache() {
        SwingWorker<List<RoomResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RoomResponse> doInBackground() throws Exception {
                return admin.listRooms();
            }
            @Override
            protected void done() {
                try {
                    roomCache = get().stream().collect(Collectors.toMap(RoomResponse::roomId, r -> r));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to cache room data", e);
                }
            }
        };
        worker.execute();
    }

    private JScrollPane createTimetableGrid() {
        gridPanel.setOpaque(true);
        gridPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (int i = 0; i < days.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            gbc.weightx = (i == 0) ? 0.1 : 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gridPanel.add(createHeaderLabel(days[i]), gbc);
        }

        for (int i = 0; i < 18; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.VERTICAL;
            LocalTime time = LocalTime.of(9, 0).plusMinutes(30 * i);
            JLabel timeLabel = createHeaderLabel(time.toString());
            gridPanel.add(timeLabel, gbc);
        }
        
        return new JScrollPane(gridPanel);
    }

    private void loadSchedule() {
        SwingWorker<List<TimetableSearchSlotResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TimetableSearchSlotResponse> doInBackground() throws Exception {
                SectionScheduleResponse schedule;
                switch (viewType) {
                    case "Section":
                        schedule = admin.viewScheduleForSection(viewId);
                        if (schedule == null || schedule.slots() == null) return new ArrayList<>();
                        return schedule.slots().stream()
                                .map(s -> new TimetableSearchSlotResponse(
                                        viewId,
                                        currentSection != null ? currentSection.courseId() : null,
                                        currentSection != null ? currentSection.instructorCode() : null,
                                        currentSection != null ? currentSection.semester() : null,
                                        currentSection != null ? currentSection.year() : null,
                                        s.day(),
                                        s.startTime(),
                                        s.endTime(),
                                        s.roomId()))
                                .collect(Collectors.toList());
                    case "Room":
                        schedule = admin.viewScheduleForRoom(viewId);
                        if (schedule == null || schedule.slots() == null) return new ArrayList<>();
                         return schedule.slots().stream()
                                .map(s -> new TimetableSearchSlotResponse(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        s.day(),
                                        s.startTime(),
                                        s.endTime(),
                                        viewId))
                                .collect(Collectors.toList());
                    case "Instructor":
                        schedule = admin.viewScheduleForInstructor(viewId);
                        if (schedule == null || schedule.slots() == null) return new ArrayList<>();
                         return schedule.slots().stream()
                                .map(s -> new TimetableSearchSlotResponse(
                                        null,
                                        null,
                                        viewId,
                                        null,
                                        null,
                                        s.day(),
                                        s.startTime(),
                                        s.endTime(),
                                        s.roomId()))
                                .collect(Collectors.toList());
                    default:
                        return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    scheduleSlots.clear();
                    List<TimetableSearchSlotResponse> schedule = get();
                    if (schedule != null) {
                        scheduleSlots.addAll(schedule);
                    }
                    updateGrid();
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
            if (component instanceof JComponent jc) {
                Object tag = jc.getClientProperty("schedulePill");
                if ("pill".equals(tag)) {
                    gridPanel.remove(component);
                }
            }
        }

        for (TimetableSearchSlotResponse slot : scheduleSlots) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = slot.day().getValue();
            
            long startMinutes = ChronoUnit.MINUTES.between(LocalTime.of(9, 0), slot.startTime());
            gbc.gridy = (int) (startMinutes / 30) + 1;
            
            long durationMinutes = ChronoUnit.MINUTES.between(slot.startTime(), slot.endTime());
            gbc.gridheight = (int) (durationMinutes / 30);
            
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 2, 1, 2);
            
            gridPanel.add(createSchedulePill(slot), gbc);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createSchedulePill(TimetableSearchSlotResponse slot) {
        JPanel pill = new JPanel(new BorderLayout(5, 2));
        String courseText = slot.courseId() != null ? slot.courseId() : "N/A";
        pill.setBackground(getCourseColor(courseText));
        pill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            new EmptyBorder(5, 5, 5, 5)
        ));
        pill.putClientProperty("schedulePill", "pill");
        
        JLabel courseLabel = new JLabel(courseText, SwingConstants.CENTER);
        courseLabel.setForeground(Color.WHITE);
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        String roomText = slot.roomId() != null ? slot.roomId() : "N/A";
        if (slot.roomId() != null && roomCache.containsKey(slot.roomId())) {
            roomText = roomCache.get(slot.roomId()).roomId();
        }
        JLabel roomLabel = new JLabel(roomText, SwingConstants.CENTER);
        roomLabel.setForeground(Color.WHITE);
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        pill.add(courseLabel, BorderLayout.CENTER);
        pill.add(roomLabel, BorderLayout.SOUTH);
        return pill;
    }

    private Color getCourseColor(String courseId) {
        String key = courseId == null ? "UNKNOWN" : courseId;
        return courseColorMap.computeIfAbsent(key, c -> pillColors[courseColorMap.size() % pillColors.length]);
    }

    private void openScheduleDialog() {
        if (currentSection == null || !"Section".equals(viewType)) {
            return;
        }
        ScheduleSlotDialog dialog = new ScheduleSlotDialog((Frame) SwingUtilities.getWindowAncestor(this), admin, currentSection.capacity(), currentSection.instructorCode(), currentSection.year(), currentSection.semester());
        dialog.setVisible(true);
        RoomAvailabilityResponse selectedRoom = dialog.getSelectedRoom();
        if (selectedRoom != null) {
            DayOfWeek day = dialog.getSelectedDay();
            LocalTime startTime = dialog.getSelectedStartTime();
            LocalTime endTime = dialog.getSelectedEndTime();
            
            TimetableSearchSlotResponse newSlot = new TimetableSearchSlotResponse(currentSection.sectionId(), currentSection.courseId(), currentSection.instructorCode(), currentSection.semester(), currentSection.year(), day, startTime, endTime, selectedRoom.roomId());
            scheduleSlots.add(newSlot);
            updateGrid();
        }
    }

    private void saveSchedule() {
        if (!"Section".equals(viewType) || currentSection == null) {
            return;
        }
        List<ScheduleSlotRequest> slotsToSave = scheduleSlots.stream()
            .map(s -> new ScheduleSlotRequest(s.day(), s.startTime(), s.endTime(), s.roomId()))
            .collect(Collectors.toList());
        SectionScheduleRequest request = new SectionScheduleRequest(slotsToSave);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                admin.upsertSchedule(viewId, request);
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

    private void clearScheduleAction() {
        if (!"Section".equals(viewType)) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the entire schedule for this section?", "Confirm Clear", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                admin.clearSchedule(viewId);
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
