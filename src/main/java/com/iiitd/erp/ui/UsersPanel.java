package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
import com.iiitd.erp.ui.client.AdminClient;

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

public class UsersPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable studentTable, instructorTable;
    private TableRowSorter<DefaultTableModel> studentSorter, instructorSorter;
    private List<StudentResponse> studentList = new ArrayList<>();
    private List<InstructorResponse> instructorList = new ArrayList<>();
    private final static Logger LOGGER = Logger.getLogger(UsersPanel.class.getName());
    private final boolean isMaintenanceMode;
    private final JTabbedPane tabs = new JTabbedPane();

    public UsersPanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createFilterBar(), BorderLayout.NORTH);
        
        mainPanel.add(createListUsersPanel(), "list");
        mainPanel.add(new CreateStudentPanel(admin, this::showListAndRefresh), "createStudent");
        mainPanel.add(new CreateInstructorPanel(admin, this::showListAndRefresh), "createInstructor");
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "list");
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
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = filterField.getText();
                if (tabs.getSelectedIndex() == 0) { // Students tab
                    if (text.trim().length() == 0) {
                        studentSorter.setRowFilter(null);
                    } else {
                        studentSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                } else { // Instructors tab
                    if (text.trim().length() == 0) {
                        instructorSorter.setRowFilter(null);
                    } else {
                        instructorSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            }
        });

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);
        return filterPanel;
    }

    private void showListAndRefresh() {
        loadData();
        cardLayout.show(mainPanel, "list");
    }

    public void loadData() {
        loadStudents();
        loadInstructors();
    }

    private void showMaintenanceMessage() {
        JOptionPane.showMessageDialog(this,
                "Action disabled: The application is currently in maintenance mode.",
                "Maintenance Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JComponent createListUsersPanel() {
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tabs.addTab("Students", createStudentTab());
        tabs.addTab("Instructors", createInstructorTab());
        return tabs;
    }

    private JPanel createStudentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultTableModel studentModel = new DefaultTableModel(new Object[]{"Roll", "Name", "Program", "Year", "DOB"}, 0);
        studentTable = new JTable(studentModel);
        studentSorter = new TableRowSorter<>(studentModel);
        studentTable.setRowSorter(studentSorter);
        styleTable(studentTable);
        
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel("Student"), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInstructorTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultTableModel instructorModel = new DefaultTableModel(new Object[]{"Code", "Name", "Dept", "DOB", "Email", "Phone"}, 0);
        instructorTable = new JTable(instructorModel);
        instructorSorter = new TableRowSorter<>(instructorModel);
        instructorTable.setRowSorter(instructorSorter);
        styleTable(instructorTable);

        panel.add(new JScrollPane(instructorTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel("Instructor"), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel(String type) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Add New " + type);
        stylePrimaryButton(addButton);
        addButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            cardLayout.show(mainPanel, "create" + type);
        });

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryButton(editButton);
        editButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            if (type.equals("Student")) showUpdateStudentPanel();
            else showUpdateInstructorPanel();
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            if (type.equals("Student")) deleteStudent();
            else deleteInstructor();
        });

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

    private void loadStudents() {
        SwingWorker<List<StudentResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudentResponse> doInBackground() {
                try {
                    LOGGER.info("Loading students");
                    return admin.listStudents();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load students", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    studentList = get();
                    DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
                    model.setRowCount(0);
                    if (studentList != null) {
                        studentList.forEach(s -> model.addRow(new Object[]{s.rollNo(), s.name(), s.programId(), s.yearOfRegistration(), s.dateOfBirth()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update student table", e);
                }
            }
        };
        worker.execute();
    }

    private void loadInstructors() {
        SwingWorker<List<InstructorResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InstructorResponse> doInBackground() {
                try {
                    LOGGER.info("Loading instructors");
                    return admin.listInstructors();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructors", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    instructorList = get();
                    DefaultTableModel model = (DefaultTableModel) instructorTable.getModel();
                    model.setRowCount(0);
                    if (instructorList != null) {
                        instructorList.forEach(i -> model.addRow(new Object[]{i.instructorCode(), i.name(), i.deptId(), i.dateOfBirth(), i.email(), i.phone()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update instructor table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateStudentPanel() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = studentTable.convertRowIndexToModel(viewRow);
        StudentResponse student = studentList.get(modelRow);
        JPanel updatePanel = new UpdateStudentPanel(admin, this::showListAndRefresh, student);
        mainPanel.add(updatePanel, "updateStudent");
        cardLayout.show(mainPanel, "updateStudent");
    }

    private void showUpdateInstructorPanel() {
        int viewRow = instructorTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an instructor to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = instructorTable.convertRowIndexToModel(viewRow);
        InstructorResponse instructor = instructorList.get(modelRow);
        JPanel updatePanel = new UpdateInstructorPanel(admin, this::showListAndRefresh, instructor);
        mainPanel.add(updatePanel, "updateInstructor");
        cardLayout.show(mainPanel, "updateInstructor");
    }

    private void deleteStudent() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = studentTable.convertRowIndexToModel(viewRow);
        String roll = studentList.get(modelRow).rollNo().toString();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete student " + roll + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting student: " + roll);
                    admin.deleteStudent(roll);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete student " + roll, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(UsersPanel.this, "Error deleting student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                loadStudents();
            }
        };
        worker.execute();
    }

    private void deleteInstructor() {
        int viewRow = instructorTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an instructor to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = instructorTable.convertRowIndexToModel(viewRow);
        String code = instructorList.get(modelRow).instructorCode();
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete instructor " + code + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting instructor: " + code);
                    admin.deleteInstructor(code);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete instructor " + code, ex);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(UsersPanel.this, "Error deleting instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void done() {
                loadInstructors();
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
