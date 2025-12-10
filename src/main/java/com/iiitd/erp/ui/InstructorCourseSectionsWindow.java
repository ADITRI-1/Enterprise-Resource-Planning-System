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
import java.util.stream.Collectors;

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
        SwingWorker<List<CourseResponse>, Void> w = new SwingWorker<>() {
            @Override protected List<CourseResponse> doInBackground() {
                try {
                    LOGGER.info("Loading instructor courses");
                    return client.getMyCourses();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructor courses", e);
                    JOptionPane.showMessageDialog(InstructorCourseSectionsWindow.this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            @Override protected void done() {
                try {
                    List<CourseResponse> courses = get();
                    DefaultTableModel m = (DefaultTableModel) courseTable.getModel();
                    m.setRowCount(0);
                    if (courses != null) {
                        for (CourseResponse course : courses) {
                            m.addRow(new Object[]{course.courseId(), course.title()});
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update course table", e);
                }
            }
        };
        w.execute();
    }

    private void loadSections(String courseId) {
        SwingWorker<List<SectionResponse>, Void> w = new SwingWorker<>() {
            @Override protected List<SectionResponse> doInBackground() {
                try {
                    LOGGER.info("Loading sections for course: " + courseId);
                    return client.getMySections();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load sections for course " + courseId, e);
                    JOptionPane.showMessageDialog(InstructorCourseSectionsWindow.this, "Error loading sections: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            @Override protected void done() {
                try {
                    List<SectionResponse> sections = get();
                    DefaultTableModel m = (DefaultTableModel) sectionTable.getModel();
                    m.setRowCount(0);
                    if (sections != null) {
                        List<SectionResponse> filteredSections = sections.stream()
                                .filter(s -> s.courseId().equals(courseId))
                                .collect(Collectors.toList());
                        for (SectionResponse section : filteredSections) {
                            m.addRow(new Object[]{section.sectionId(), section.semester(), section.year(), section.capacity()});
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update section table", e);
                }
            }
        };
        w.execute();
    }
}
