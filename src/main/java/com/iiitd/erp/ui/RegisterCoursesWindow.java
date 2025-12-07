package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterCoursesWindow extends JFrame {

    private final StudentClient client;
    private JTable courseTable;
    private final static Logger LOGGER = Logger.getLogger(RegisterCoursesWindow.class.getName());

    public RegisterCoursesWindow(StudentClient client) {
        this.client = client;
        LOGGER.info("RegisterCoursesWindow initialized");

        setTitle("Register for Courses");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        courseTable = new JTable(new DefaultTableModel(new Object[]{"Course ID", "Title", "Credits"}, 0));
        add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerForCourse());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(registerButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAvailableCourses();
    }

    private void loadAvailableCourses() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<CourseResponse> courses;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading available courses");
                    courses = client.getAvailableCourses();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load available courses", e);
                    JOptionPane.showMessageDialog(RegisterCoursesWindow.this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) courseTable.getModel();
                m.setRowCount(0);
                if (courses != null) {
                    for (CourseResponse course : courses) {
                        m.addRow(new Object[]{course.courseId(), course.title(), course.credits()});
                    }
                }
            }
        };
        w.execute();
    }

    private void registerForCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to register.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = courseTable.getValueAt(selectedRow, 0).toString();
        LOGGER.info("Registering for course: " + courseId);
        // TODO: Implement the backend call to register for the course
        JOptionPane.showMessageDialog(this, "Registration for " + courseId + " is not yet implemented.", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
}
