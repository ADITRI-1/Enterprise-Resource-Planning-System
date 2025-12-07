package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.DepartmentCreateRequest;
import com.iiitd.erp.io.dto.request.DepartmentUpdateRequest;
import com.iiitd.erp.io.dto.request.ProgramCreateRequest;
import com.iiitd.erp.io.dto.request.ProgramUpdateRequest;
import com.iiitd.erp.io.dto.response.DepartmentResponse;
import com.iiitd.erp.io.dto.response.ProgramResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageCatalogWindow extends JFrame {

    private final AdminClient admin;
    private final JFrame parent;
    private JTable programTable, deptTable;
    private final static Logger LOGGER = Logger.getLogger(ManageCatalogWindow.class.getName());

    public ManageCatalogWindow(AdminClient admin, JFrame parent) {
        this.admin = admin;
        this.parent = parent;
        LOGGER.info("ManageCatalogWindow initialized");
        setTitle("Manage Programs & Departments");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(topBar(), BorderLayout.NORTH);
        add(tabs(), BorderLayout.CENTER);

        loadPrograms();
        loadDepartments();
    }

    private JComponent topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(9, 53, 82));
        JLabel title = new JLabel("Manage Programs & Departments");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bar.add(margin(title, 10, 16, 10, 0), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);
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
        actions.add(back); actions.add(logout);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane tabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Programs", programPanel());
        tabs.add("Departments", deptPanel());
        return tabs;
    }

    private JPanel programPanel() {
        JPanel p = new JPanel(new BorderLayout());
        programTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Description", "Type", "Duration"}, 0));
        p.add(new JScrollPane(programTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton edit = new JButton("Update");
        JButton del = new JButton("Delete");
        add.addActionListener(e -> createProgramDialog());
        edit.addActionListener(e -> updateProgramDialog());
        del.addActionListener(e -> deleteProgram());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(add); actions.add(edit); actions.add(del);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private JPanel deptPanel() {
        JPanel p = new JPanel(new BorderLayout());
        deptTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Description"}, 0));
        p.add(new JScrollPane(deptTable), BorderLayout.CENTER);

        JButton add = new JButton("Add");
        JButton edit = new JButton("Update");
        JButton del = new JButton("Delete");
        add.addActionListener(e -> createDeptDialog());
        edit.addActionListener(e -> updateDeptDialog());
        del.addActionListener(e -> deleteDept());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(add); actions.add(edit); actions.add(del);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void loadPrograms() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<ProgramResponse> rows;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading programs");
                    rows = admin.listPrograms();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load programs", e);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) programTable.getModel();
                m.setRowCount(0);
                if (rows != null) rows.forEach(r -> m.addRow(new Object[]{r.programId(), r.programDesc(), r.programType(), r.duration()}));
            }
        }; w.execute();
    }

    private void createProgramDialog() {
        LOGGER.info("Create program action initiated");
        String[] labels = {"Program ID", "Description", "Type", "Duration (years)"};
        CatalogDialog dialog = new CatalogDialog(this, "Create Program", labels, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] values = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        ProgramCreateRequest request = ProgramCreateRequest.builder()
                                .programId(values[0])
                                .programDesc(values[1])
                                .programType(values[2])
                                .duration(Integer.parseInt(values[3]))
                                .build();
                        LOGGER.info("Creating program: " + request);
                        admin.createProgram(request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to create program", ex);
                        JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error creating program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadPrograms(); }
            }; w.execute();
        }
    }

    private void updateProgramDialog() {
        int row = programTable.getSelectedRow(); if (row < 0) return;
        String id = programTable.getValueAt(row, 0).toString();
        LOGGER.info("Update program action initiated for ID: " + id);

        String[] labels = {"Description", "Type", "Duration (years)"};
        String[] values = {
                programTable.getValueAt(row, 1).toString(),
                programTable.getValueAt(row, 2).toString(),
                programTable.getValueAt(row, 3).toString()
        };
        CatalogDialog dialog = new CatalogDialog(this, "Update Program " + id, labels, values);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] newValues = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        ProgramUpdateRequest request = ProgramUpdateRequest.builder()
                                .programDesc(newValues[0])
                                .programType(newValues[1])
                                .duration(Integer.parseInt(newValues[2]))
                                .build();
                        LOGGER.info("Updating program " + id + " with: " + request);
                        admin.updateProgram(id, request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to update program " + id, ex);
                        JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error updating program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadPrograms(); }
            }; w.execute();
        }
    }

    private void deleteProgram() {
        int row = programTable.getSelectedRow(); if (row < 0) return;
        String id = programTable.getValueAt(row, 0).toString();
        LOGGER.info("Delete program action initiated for ID: " + id);
        if (JOptionPane.showConfirmDialog(this, "Delete program " + id + "?", "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting program: " + id);
                    admin.deleteProgram(id);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete program " + id, ex);
                    JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error deleting program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { loadPrograms(); }
        }; w.execute();
    }

    private void loadDepartments() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            List<DepartmentResponse> rows;
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Loading departments");
                    rows = admin.listDepartments();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load departments", e);
                }
                return null;
            }
            @Override protected void done() {
                DefaultTableModel m = (DefaultTableModel) deptTable.getModel();
                m.setRowCount(0);
                if (rows != null) rows.forEach(r -> m.addRow(new Object[]{r.deptId(), r.deptDesc()}));
            }
        }; w.execute();
    }

    private void createDeptDialog() {
        LOGGER.info("Create department action initiated");
        String[] labels = {"Department ID", "Description"};
        CatalogDialog dialog = new CatalogDialog(this, "Create Department", labels, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] values = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        DepartmentCreateRequest request = DepartmentCreateRequest.builder()
                                .deptId(values[0])
                                .deptDesc(values[1])
                                .build();
                        LOGGER.info("Creating department: " + request);
                        admin.createDepartment(request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to create department", ex);
                        JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error creating department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadDepartments(); }
            }; w.execute();
        }
    }

    private void updateDeptDialog() {
        int row = deptTable.getSelectedRow(); if (row < 0) return;
        String id = deptTable.getValueAt(row, 0).toString();
        LOGGER.info("Update department action initiated for ID: " + id);

        String[] labels = {"Description"};
        String[] values = {deptTable.getValueAt(row, 1).toString()};
        CatalogDialog dialog = new CatalogDialog(this, "Update Department " + id, labels, values);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String[] newValues = dialog.getValues();
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        DepartmentUpdateRequest request = DepartmentUpdateRequest.builder()
                                .deptDesc(newValues[0])
                                .build();
                        LOGGER.info("Updating department " + id + " with: " + request);
                        admin.updateDepartment(id, request);
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Failed to update department " + id, ex);
                        JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error updating department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { loadDepartments(); }
            }; w.execute();
        }
    }

    private void deleteDept() {
        int row = deptTable.getSelectedRow(); if (row < 0) return;
        String id = deptTable.getValueAt(row, 0).toString();
        LOGGER.info("Delete department action initiated for ID: " + id);
        if (JOptionPane.showConfirmDialog(this, "Delete department " + id + "?", "Confirm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    LOGGER.info("Deleting department: " + id);
                    admin.deleteDepartment(id);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to delete department " + id, ex);
                    JOptionPane.showMessageDialog(ManageCatalogWindow.this, "Error deleting department: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { loadDepartments(); }
        }; w.execute();
    }

    private JComponent margin(JComponent c, int t, int l, int b, int r) { JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(new EmptyBorder(t,l,b,r)); p.add(c); return p; }
}
