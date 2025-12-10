package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.AssessmentComponentResponse;
import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssessmentsPanel extends JPanel {

    private final InstructorClient client;
    private JTable assessmentsTable;
    private static final Logger LOGGER = Logger.getLogger(AssessmentsPanel.class.getName());
    private final boolean isMaintenanceMode;
    private String sectionId;

    public AssessmentsPanel(InstructorClient client, boolean maintenanceMode, String sectionId) {
        this.client = client;
        this.isMaintenanceMode = maintenanceMode;
        this.sectionId = sectionId;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Manage Assessments for Section " + sectionId);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Component", "Weightage"}, 0);
        assessmentsTable = new JTable(model);
        styleTable(assessmentsTable);
        contentPanel.add(new JScrollPane(assessmentsTable), BorderLayout.CENTER);

        contentPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        loadData();
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
        JLabel title = (JLabel) getComponent(0);
        title.setText("Manage Assessments for Section " + sectionId);
        loadData();
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton addComponentButton = new JButton("Add Component");
        stylePrimaryButton(addComponentButton);
        addComponentButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            // Logic to add a new assessment component
        });

        JButton enterScoresButton = new JButton("Enter Scores");
        stylePrimaryButton(enterScoresButton);
        enterScoresButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            // Logic to enter scores
        });

        JButton computeGradesButton = new JButton("Compute Final Grades");
        stylePrimaryButton(computeGradesButton);
        computeGradesButton.addActionListener(e -> {
            if (isMaintenanceMode) {
                showMaintenanceMessage();
                return;
            }
            // Logic to compute final grades
        });

        buttonPanel.add(addComponentButton);
        buttonPanel.add(enterScoresButton);
        buttonPanel.add(computeGradesButton);
        return buttonPanel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    public void loadData() {
        if (sectionId == null || sectionId.isEmpty()) {
            return;
        }
        SwingWorker<List<AssessmentComponentResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AssessmentComponentResponse> doInBackground() {
                try {
                    LOGGER.info("Loading assessments for section " + sectionId);
                    return client.getAssessments(sectionId);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load assessments", e);
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    List<AssessmentComponentResponse> assessmentList = get();
                    DefaultTableModel model = (DefaultTableModel) assessmentsTable.getModel();
                    model.setRowCount(0);
                    if (assessmentList != null) {
                        assessmentList.forEach(a -> model.addRow(new Object[]{a.name(), a.weight()}));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update assessments table", e);
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
