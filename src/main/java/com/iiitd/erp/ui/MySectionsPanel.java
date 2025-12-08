package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.InstructorClient;

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

public class MySectionsPanel extends JPanel {

    private final InstructorClient client;
    private JTable sectionsTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<SectionResponse> sectionList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(MySectionsPanel.class.getName());
    private final boolean isMaintenanceMode;
    private final Runnable showAssessmentsPanel;

    public MySectionsPanel(InstructorClient client, boolean maintenanceMode, Runnable showAssessmentsPanel) {
        this.client = client;
        this.isMaintenanceMode = maintenanceMode;
        this.showAssessmentsPanel = showAssessmentsPanel;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("My Sections");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createFilterBar(), BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createListSectionsPanel(), "list");
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

    private JPanel createListSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Course", "Semester", "Year"}, 0);
        sectionsTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        sectionsTable.setRowSorter(sorter);
        styleTable(sectionsTable);

        panel.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);
        panel.add(createCrudButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCrudButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton manageAssessmentsButton = new JButton("Manage Assessments");
        stylePrimaryButton(manageAssessmentsButton);
        manageAssessmentsButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            showAssessmentsPanel.run();
        });

        buttonPanel.add(manageAssessmentsButton);
        return buttonPanel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    public void loadData() {
        SwingWorker<List<SectionResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SectionResponse> doInBackground() {
                try {
                    LOGGER.info("Loading instructor sections");
                    return client.getMySections();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructor sections", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    sectionList = get();
                    DefaultTableModel model = (DefaultTableModel) sectionsTable.getModel();
                    model.setRowCount(0);
                    if (sectionList != null) {
                        sectionList.forEach(s -> model.addRow(new Object[]{s.getSectionId(), s.getCourseTitle(), s.getSemester(), s.getYear()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update sections table", e);
                }
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
