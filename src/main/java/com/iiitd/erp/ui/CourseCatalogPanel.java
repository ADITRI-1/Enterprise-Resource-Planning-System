package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.SectionRow;
import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourseCatalogPanel extends JPanel {

    private final StudentClient student;
    private JTable coursesTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<SectionRow> sectionList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(CourseCatalogPanel.class.getName());
    private final boolean isMaintenanceMode;

    public CourseCatalogPanel(StudentClient student, boolean maintenanceMode) {
        this.student = student;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Course Catalog");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createFilterBar(), BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createListCoursesPanel(), "list");
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        loadData();
    }

    private JPanel createFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        JTextField filterField = new JTextField(30);
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = filterField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);
        return filterPanel;
    }

    private JPanel createListCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Course", "Instructor", "Capacity", "Available", "Semester", "Year"}, 0);
        coursesTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        coursesTable.setRowSorter(sorter);
        styleTable(coursesTable);

        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton registerButton = new JButton("Register");
        stylePrimaryButton(registerButton);
        registerButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            registerCourse();
        });

        buttonPanel.add(registerButton);
        return buttonPanel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    public void loadData() {
        SwingWorker<List<SectionRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SectionRow> doInBackground() {
                try {
                    LOGGER.info("Loading course sections");
                    return student.courseCatalog();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load course sections", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    sectionList = get();
                    DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
                    model.setRowCount(0);
                    if (sectionList != null) {
                        sectionList.forEach(s -> model.addRow(new Object[]{
                                s.courseId(),
                                s.instructorName(),
                                s.capacity(),
                                s.available(),
                                s.semester(),
                                s.year()
                        }));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update course catalog table", e);
                }
            }
        };
        worker.execute();
    }

    private void registerCourse() {
        int viewRow = coursesTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to register.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = coursesTable.convertRowIndexToModel(viewRow);
        SectionRow section = sectionList.get(modelRow);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Registering for course: " + section.courseId());
                    student.registerCourse(section.sectionId());
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to register for course " + section.courseId(), ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Error registering for course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Successfully registered for " + section.courseId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        };
        worker.execute();
    }
    
    private void showMaintenanceMessage() {
        JOptionPane.showMessageDialog(this,
                "Action disabled: The application is currently in maintenance mode.",
                "Maintenance Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
