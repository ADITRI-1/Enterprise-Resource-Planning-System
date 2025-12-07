package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstructorCourseSectionsWindow extends JFrame {

    private final InstructorClient client;
    private JTable courseTable, sectionTable;
    private final static Logger LOGGER = Logger.getLogger(InstructorCourseSectionsWindow.class.getName());

    public InstructorCourseSectionsWindow(InstructorClient client) {
        this.client = client;
        LOGGER.info("InstructorCourseSectionsWindow initialized");

        setTitle("My Course Sections");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coursePanel(), sectionPanel());
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        loadCourses();
    }

    private JPanel coursePanel() {
        JPanel p = new JPanel(new BorderLayout());
        courseTable = new JTable(new DefaultTableModel(new Object[]{"Course ID", "Title"}, 0));
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow != -1) {
                    String courseId = courseTable.getValueAt(selectedRow, 0).toString();
                    loadSections(courseId);
                }
            }
        });
        p.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel sectionPanel() {
        JPanel p = new JPanel(new BorderLayout());
        sectionTable = new JTable(new DefaultTableModel(new Object[]{"Section ID", "Semester", "Year", "Capacity"}, 0));
        p.add(new JScrollPane(sectionTable), BorderLayout.CENTER);
        return p;
    }

    private void loadCourses() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<CourseResponse> courses;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading instructor courses");
                    courses = client.getMyCourses();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructor courses", e);
                    JOptionPane.showMessageDialog(InstructorCourseSectionsWindow.this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) courseTable.getModel();
                m.setRowCount(0);
                if (courses != null) {
                    for (CourseResponse course : courses) {
                        m.addRow(new Object[]{course.courseId(), course.title()});
                    }
                }
            }
        };
        w.execute();
    }

    private void loadSections(String courseId) {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<SectionResponse> sections;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading sections for course: " + courseId);
                    sections = client.getMySections(courseId);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load sections for course " + courseId, e);
                    JOptionPane.showMessageDialog(InstructorCourseSectionsWindow.this, "Error loading sections: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) sectionTable.getModel();
                m.setRowCount(0);
                if (sections != null) {
                    for (SectionResponse section : sections) {
                        m.addRow(new Object[]{section.sectionId(), section.semester(), section.year(), section.capacity()});
                    }
                }
            }
        };
        w.execute();
    }
}
