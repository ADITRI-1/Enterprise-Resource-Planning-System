package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersPanel extends JPanel {

    private final AdminClient admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JTable studentTable, instructorTable;
    private final static Logger LOGGER = Logger.getLogger(UsersPanel.class.getName());

    public UsersPanel(AdminClient admin) {
        this.admin = admin;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);

        JPanel listUsersPanel = createListUsersPanel();
        JPanel createStudentPanel = new CreateStudentPanel(admin, this::showListAndRefresh);
        JPanel createInstructorPanel = new CreateInstructorPanel(admin, this::showListAndRefresh);

        mainPanel.add(listUsersPanel, "list");
        mainPanel.add(createStudentPanel, "createStudent");
        mainPanel.add(createInstructorPanel, "createInstructor");

        cardLayout.show(mainPanel, "list");
        loadData();
    }

    private void showListAndRefresh() {
        loadData();
        cardLayout.show(mainPanel, "list");
    }

    private void loadData() {
        loadStudents();
        loadInstructors();
    }

    private JPanel createListUsersPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tabs.addTab("Students", createStudentTab());
        tabs.addTab("Instructors", createInstructorTab());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStudentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        studentTable = new JTable(new DefaultTableModel(new Object[]{"Roll", "Name", "Program", "Year"}, 0));
        styleTable(studentTable);
        
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel("Student"), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInstructorTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        instructorTable = new JTable(new DefaultTableModel(new Object[]{"Code", "Name", "Dept"}, 0));
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
        addButton.addActionListener(e -> cardLayout.show(mainPanel, "create" + type));

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryButton(editButton);
        editButton.addActionListener(e -> {
            if (type.equals("Student")) showUpdateStudentPanel();
            else showUpdateInstructorPanel();
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleDeleteButton(deleteButton);
        deleteButton.addActionListener(e -> {
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
                    List<StudentResponse> rows = get();
                    DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
                    model.setRowCount(0);
                    if (rows != null) {
                        rows.forEach(s -> model.addRow(new Object[]{s.rollNo(), s.name(), s.programId(), s.yearOfRegistration()}));
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
                    List<InstructorResponse> rows = get();
                    DefaultTableModel model = (DefaultTableModel) instructorTable.getModel();
                    model.setRowCount(0);
                    if (rows != null) {
                        rows.forEach(i -> model.addRow(new Object[]{i.instructorCode(), i.name(), i.deptId()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update instructor table", e);
                }
            }
        };
        worker.execute();
    }

    private void showUpdateStudentPanel() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StudentResponse student = StudentResponse.builder()
                .rollNo((Integer) studentTable.getValueAt(row, 0))
                .name((String) studentTable.getValueAt(row, 1))
                .programId((String) studentTable.getValueAt(row, 2))
                .yearOfRegistration(studentTable.getValueAt(row, 3).toString())
                .build();

        JPanel updatePanel = new UpdateStudentPanel(admin, this::showListAndRefresh, student);
        mainPanel.add(updatePanel, "updateStudent");
        cardLayout.show(mainPanel, "updateStudent");
    }

    private void showUpdateInstructorPanel() {
        int row = instructorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an instructor to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        InstructorResponse instructor = new InstructorResponse(
            (String) instructorTable.getValueAt(row, 0),
            (String) instructorTable.getValueAt(row, 1),
            (String) instructorTable.getValueAt(row, 2),
            "", "", "" // email, phone not shown in table, but needed for constructor
        );

        JPanel updatePanel = new UpdateInstructorPanel(admin, this::showListAndRefresh, instructor);
        mainPanel.add(updatePanel, "updateInstructor");
        cardLayout.show(mainPanel, "updateInstructor");
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roll = studentTable.getValueAt(row, 0).toString();
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
        int row = instructorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an instructor to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = instructorTable.getValueAt(row, 0).toString();
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
