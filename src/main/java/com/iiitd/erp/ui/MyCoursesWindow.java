package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyCoursesWindow extends JFrame {

    private final StudentClient client;
    private JTable courseTable;
    private final static Logger LOGGER = Logger.getLogger(MyCoursesWindow.class.getName());

    public MyCoursesWindow(StudentClient client) {
        this.client = client;
        LOGGER.info("MyCoursesWindow initialized");

        setTitle("My Courses");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        courseTable = new JTable(new DefaultTableModel(new Object[]{"Course ID", "Title", "Credits"}, 0));
        add(new JScrollPane(courseTable), BorderLayout.CENTER);

        loadCourses();
    }

    private void loadCourses() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<CourseResponse> courses;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading student courses");
                    courses = client.getMyCourses();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load student courses", e);
                    JOptionPane.showMessageDialog(MyCoursesWindow.this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
}
