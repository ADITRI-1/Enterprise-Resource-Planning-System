package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoursesPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable coursesTable;
    private static final Logger LOGGER = Logger.getLogger(CoursesPanel.class.getName());

    public CoursesPanel(AdminClient admin) {
        this.admin = admin;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Course Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel listCoursesPanel = createListCoursesPanel();
        JPanel createCoursePanel = new CreateCoursePanel(admin, this::showListAndRefresh);

        mainPanel.add(listCoursesPanel, "list");
        mainPanel.add(createCoursePanel, "create");

        cardLayout.show(mainPanel, "list");
        loadCourses();
    }

    private void showListAndRefresh() {
        loadCourses();
        cardLayout.show(mainPanel, "list");
    }

    private JPanel createListCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        coursesTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Title", "Department", "Credits"}, 0));
        styleTable(coursesTable);

        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New Course");
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> cardLayout.show(mainPanel, "create"));

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryButton(editButton);
        editButton.addActionListener(e -> showUpdateCoursePanel());

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> deleteCourse());

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

    private void loadCourses() {
        SwingWorker<List<CourseResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourseResponse> doInBackground() {
                try {
                    LOGGER.info("Loading courses");
                    return admin.listCourses();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load courses", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<CourseResponse> rows = get();
                    DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
                    model.setRowCount(0);
                    if (rows != null) {
                        rows.forEach(c -> model.addRow(new Object[]{c.courseId(), c.title(), c.deptId(), c.credits()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update course table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateCoursePanel() {
        int row = coursesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseResponse course = CourseResponse.builder()
                .courseId((String) coursesTable.getValueAt(row, 0))
                .title((String) coursesTable.getValueAt(row, 1))
                .deptId((String) coursesTable.getValueAt(row, 2))
                .credits((Integer) coursesTable.getValueAt(row, 3))
                .build();


        JPanel updatePanel = new UpdateCoursePanel(admin, this::showListAndRefresh, course);
        mainPanel.add(updatePanel, "updateCourse");
        cardLayout.show(mainPanel, "updateCourse");
    }

    private void deleteCourse() {
        int row = coursesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = coursesTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete course " + courseId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting course: " + courseId);
                    admin.deleteCourse(courseId);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete course " + courseId, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(CoursesPanel.this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                loadCourses();
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
