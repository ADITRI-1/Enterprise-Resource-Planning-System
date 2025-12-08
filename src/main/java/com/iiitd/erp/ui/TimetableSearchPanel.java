package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.*;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class TimetableSearchPanel extends JPanel {

    private final AdminClient admin;
    private final TimetableView timetableView;
    private static final Logger LOGGER = Logger.getLogger(TimetableSearchPanel.class.getName());

    private final JComboBox<InstructorResponse> cbInstructor = new JComboBox<>();
    private final JComboBox<CourseResponse> cbCourse = new JComboBox<>();
    private final JComboBox<SectionResponse> cbSection = new JComboBox<>();
    private final JComboBox<RoomResponse> cbRoom = new JComboBox<>();
    private final JComboBox<String> cbSemester = new JComboBox<>(new String[]{"", "Monsoon", "Winter"});
    private final JComboBox<Integer> cbYear;
    private final JComboBox<DayOfWeek> cbDay = new JComboBox<>(DayOfWeek.values());

    public TimetableSearchPanel(AdminClient admin, TimetableView timetableView) {
        this.admin = admin;
        this.timetableView = timetableView;

        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel(new BorderLayout(20, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Global Timetable Search", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 15, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        Integer[] years = IntStream.rangeClosed(Year.now().getValue() - 5, Year.now().getValue() + 1).boxed().toArray(Integer[]::new);
        cbYear = new JComboBox<>(years);
        cbYear.insertItemAt(null, 0);
        cbYear.setSelectedIndex(0);
        cbDay.insertItemAt(null, 0);
        cbDay.setSelectedIndex(0);
        
        formPanel.add(new JLabel("Instructor:"));
        formPanel.add(cbInstructor);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(cbCourse);
        formPanel.add(new JLabel("Section:"));
        formPanel.add(cbSection);
        formPanel.add(new JLabel("Room:"));
        formPanel.add(cbRoom);
        formPanel.add(new JLabel("Semester:"));
        formPanel.add(cbSemester);
        formPanel.add(new JLabel("Year:"));
        formPanel.add(cbYear);
        formPanel.add(new JLabel("Day:"));
        formPanel.add(cbDay);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        stylePrimaryButton(searchButton);
        searchButton.addActionListener(e -> search());
        
        JButton clearButton = new JButton("Clear Filters");
        styleSecondaryButton(clearButton);
        clearButton.addActionListener(e -> clearFilters());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        buttonPanel.add(searchButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.CENTER);
        
        loadDropdowns();
    }

    private void loadDropdowns() {
        // Instructors
        SwingWorker<List<InstructorResponse>, Void> instructorWorker = new SwingWorker<>() {
            @Override protected List<InstructorResponse> doInBackground() throws Exception { return admin.listInstructors(); }
            @Override protected void done() { try { cbInstructor.removeAllItems(); cbInstructor.addItem(null); get().forEach(cbInstructor::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load instructors", e); }}
        };
        // Courses
        SwingWorker<List<CourseResponse>, Void> courseWorker = new SwingWorker<>() {
            @Override protected List<CourseResponse> doInBackground() throws Exception { return admin.listCourses(); }
            @Override protected void done() { try { cbCourse.removeAllItems(); cbCourse.addItem(null); get().forEach(cbCourse::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load courses", e); }}
        };
        // Sections
        SwingWorker<List<SectionResponse>, Void> sectionWorker = new SwingWorker<>() {
            @Override protected List<SectionResponse> doInBackground() throws Exception { return admin.listSections(); }
            @Override protected void done() { try { cbSection.removeAllItems(); cbSection.addItem(null); get().forEach(cbSection::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load sections", e); }}
        };
        // Rooms
        SwingWorker<List<RoomResponse>, Void> roomWorker = new SwingWorker<>() {
            @Override protected List<RoomResponse> doInBackground() throws Exception { return admin.listRooms(); }
            @Override protected void done() { try { cbRoom.removeAllItems(); cbRoom.addItem(null); get().forEach(cbRoom::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load rooms", e); }}
        };

        instructorWorker.execute();
        courseWorker.execute();
        sectionWorker.execute();
        roomWorker.execute();
    }

    private void clearFilters() {
        cbInstructor.setSelectedIndex(0);
        cbCourse.setSelectedIndex(0);
        cbSection.setSelectedIndex(0);
        cbRoom.setSelectedIndex(0);
        cbSemester.setSelectedIndex(0);
        cbYear.setSelectedIndex(0);
        cbDay.setSelectedIndex(0);
    }

    private void search() {
        Map<String, String> filters = new HashMap<>();
        if (cbInstructor.getSelectedItem() != null) filters.put("instructorCode", ((InstructorResponse)cbInstructor.getSelectedItem()).instructorCode());
        if (cbCourse.getSelectedItem() != null) filters.put("courseId", ((CourseResponse)cbCourse.getSelectedItem()).courseId());
        if (cbSection.getSelectedItem() != null) filters.put("sectionId", ((SectionResponse)cbSection.getSelectedItem()).sectionId());
        if (cbRoom.getSelectedItem() != null) filters.put("roomId", ((RoomResponse)cbRoom.getSelectedItem()).roomId());
        if (cbSemester.getSelectedItem() != null && !cbSemester.getSelectedItem().toString().isEmpty()) filters.put("semester", cbSemester.getSelectedItem().toString());
        if (cbYear.getSelectedItem() != null) filters.put("year", cbYear.getSelectedItem().toString());
        if (cbDay.getSelectedItem() != null) filters.put("day", ((DayOfWeek)cbDay.getSelectedItem()).name());

        if (filters.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one filter.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<List<TimetableSearchSlotResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TimetableSearchSlotResponse> doInBackground() throws Exception {
                return admin.searchTimetable(filters);
            }
            @Override
            protected void done() {
                try {
                    timetableView.setSchedule(get());
                    // Assuming the parent container uses a CardLayout
                    Container parent = getParent();
                    CardLayout cl = (CardLayout)(parent.getLayout());
                    cl.show(parent, "timetable");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to search timetable", e);
                }
            }
        };
        worker.execute();
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(225, 225, 225));
        button.setForeground(new Color(50, 50, 50));
    }
}
