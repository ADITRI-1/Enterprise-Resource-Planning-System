package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.InstructorCreateRequest;
import com.iiitd.erp.io.dto.request.InstructorUpdateRequest;
import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import com.iiitd.erp.io.dto.request.StudentUpdateRequest;
import com.iiitd.erp.io.dto.response.DepartmentResponse;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminUsersWindow extends JFrame {
    private final AdminClient admin;
    private final JFrame parent;
    private JTable studentTable, instructorTable;
    private JComboBox<String> deptBox;
    private final static Logger LOGGER = Logger.getLogger(AdminUsersWindow.class.getName());

    public AdminUsersWindow(AdminClient admin, JFrame parent) {
        this.admin = admin;
        this.parent = parent;
        LOGGER.info("AdminUsersWindow initialized");
        setTitle("Manage Users");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(topBar(), BorderLayout.NORTH);
        add(tabs(), BorderLayout.CENTER);
        loadDepartments();
        loadStudents();
        loadInstructors();
    }

    private JComponent topBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton back = new JButton("Back");
        JButton logout = new JButton("Logout");
        back.addActionListener(e -> {
            LOGGER.info("Back button clicked");
            dispose();
            if (parent != null) parent.setVisible(true);
        });
        logout.addActionListener(e -> {
            LOGGER.info("Logout button clicked");
            if (parent != null) parent.dispose();
            dispose();
            new LoginUI().setVisible(true);
        });
        p.add(back); p.add(logout);
        return p;
    }

    private JTabbedPane tabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Students", studentPanel());
        tabs.add("Instructors", instructorPanel());
        return tabs;
    }

    private JPanel studentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        studentTable = new JTable(new DefaultTableModel(new Object[]{"Roll", "Name", "Program", "Year"}, 0));
        p.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton edit = new JButton("Update");
        JButton del = new JButton("Delete");
        add.addActionListener(e -> createStudent());
        edit.addActionListener(e -> updateStudent());
        del.addActionListener(e -> deleteStudent());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(add); actions.add(edit); actions.add(del);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private JPanel instructorPanel() {
        JPanel p = new JPanel(new BorderLayout());
        instructorTable = new JTable(new DefaultTableModel(new Object[]{"Code", "Name", "Dept"}, 0));
        p.add(new JScrollPane(instructorTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton edit = new JButton("Update");
        JButton del = new JButton("Delete");
        add.addActionListener(e -> createInstructor());
        edit.addActionListener(e -> updateInstructor());
        del.addActionListener(e -> deleteInstructor());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(add); actions.add(edit); actions.add(del);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void loadDepartments() {
        deptBox = new JComboBox<>();
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<DepartmentResponse> deps;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading departments");
                    deps = admin.listDepartments();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load departments", e);
                }
                return null;
            }
            @Override protected void done() { if (deps != null) deps.forEach(d -> deptBox.addItem(d.deptId())); }
        }; w.execute();
    }

    private void loadStudents() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<StudentResponse> rows;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading students");
                    rows = admin.listStudents();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load students", e);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) studentTable.getModel();
                m.setRowCount(0);
                if (rows != null) rows.forEach(s -> m.addRow(new Object[]{s.rollNo(), s.name(), s.programId(), s.yearOfRegistration()}));
            }
        }; w.execute();
    }

    private void loadInstructors() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<InstructorResponse> rows;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading instructors");
                    rows = admin.listInstructors();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructors", e);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) instructorTable.getModel();
                m.setRowCount(0);
                if (rows != null) rows.forEach(i -> m.addRow(new Object[]{i.instructorCode(), i.name(), i.deptId()}));
            }
        }; w.execute();
    }

    private void createStudent() {
        LOGGER.info("Create student action initiated");
        String[] labels = {"Roll No", "Name", "Program ID", "Year", "DOB (DD-MM-YYYY)"};
        UserDialog dialog = new UserDialog(this, "Create Student", labels, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] values = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        StudentCreateRequest request = StudentCreateRequest.builder()
                                .rollNo(Integer.parseInt(values[0]))
                                .name(values[1])
                                .programId(values[2])
                                .yearOfRegistration(values[3])
                                .dateOfBirth(values[4])
                                .build();
                        LOGGER.info("Creating student: " + request);
                        admin.createStudent(request);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to create student", ex);
                        JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error creating student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadStudents(); }
            }; w.execute();
        }
    }

    private void updateStudent() {
        int row = studentTable.getSelectedRow(); if (row < 0) return;
        String rollVal = studentTable.getValueAt(row, 0).toString();
        LOGGER.info("Update student action initiated for roll: " + rollVal);

        String[] labels = {"Name", "Program ID", "Year"};
        String[] values = {
                studentTable.getValueAt(row, 1).toString(),
                studentTable.getValueAt(row, 2).toString(),
                studentTable.getValueAt(row, 3).toString()
        };
        UserDialog dialog = new UserDialog(this, "Update Student", labels, values);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] newValues = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        StudentUpdateRequest request = StudentUpdateRequest.builder()
                                .name(newValues[0])
                                .programId(newValues[1])
                                .yearOfRegistration(newValues[2])
                                .build();
                        LOGGER.info("Updating student " + rollVal + " with: " + request);
                        admin.updateStudent(rollVal, request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to update student " + rollVal, ex);
                        JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error updating student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadStudents(); }
            }; w.execute();
        }
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow(); if (row < 0) return;
        String roll = studentTable.getValueAt(row, 0).toString();
        LOGGER.info("Delete student action initiated for roll: " + roll);
        if (JOptionPane.showConfirmDialog(this, "Delete roll " + roll + "?", "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting student: " + roll);
                    admin.deleteStudent(roll);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete student " + roll, ex);
                    JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error deleting student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { loadStudents(); }
        }; w.execute();
    }

    private void createInstructor() {
        LOGGER.info("Create instructor action initiated");
        String[] labels = {"Code", "Name", "Dept", "DOB (YYYY-MM-DD)", "Email", "Phone"};
        UserDialog dialog = new UserDialog(this, "Create Instructor", labels, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] values = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        InstructorCreateRequest request = InstructorCreateRequest.builder()
                                .instructorCode(values[0])
                                .name(values[1])
                                .deptId(values[2])
                                .dateOfBirth(values[3])
                                .email(values[4])
                                .phone(values[5])
                                .build();
                        LOGGER.info("Creating instructor: " + request);
                        admin.createInstructor(request);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to create instructor", ex);
                        JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error creating instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadInstructors(); }
            }; w.execute();
        }
    }

    private void updateInstructor() {
        int row = instructorTable.getSelectedRow(); if (row < 0) return;
        String codeVal = instructorTable.getValueAt(row, 0).toString();
        LOGGER.info("Update instructor action initiated for code: " + codeVal);

        String[] labels = {"Name", "Dept"};
        String[] values = {
                instructorTable.getValueAt(row, 1).toString(),
                instructorTable.getValueAt(row, 2).toString()
        };
        UserDialog dialog = new UserDialog(this, "Update Instructor", labels, values);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] newValues = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        InstructorUpdateRequest request = InstructorUpdateRequest.builder()
                                .name(newValues[0])
                                .deptId(newValues[1])
                                .build();
                        LOGGER.info("Updating instructor " + codeVal + " with: " + request);
                        admin.updateInstructor(codeVal, request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to update instructor " + codeVal, ex);
                        JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error updating instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadInstructors(); }
            }; w.execute();
        }
    }

    private void deleteInstructor() {
        int row = instructorTable.getSelectedRow(); if (row < 0) return;
        String code = instructorTable.getValueAt(row, 0).toString();
        LOGGER.info("Delete instructor action initiated for code: " + code);
        if (JOptionPane.showConfirmDialog(this, "Delete instructor " + code + "?", "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting instructor: " + code);
                    admin.deleteInstructor(code);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete instructor " + code, ex);
                    JOptionPane.showMessageDialog(AdminUsersWindow.this, "Error deleting instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { loadInstructors(); }
        }; w.execute();
    }
}
