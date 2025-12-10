package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
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
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MySectionsPanel extends JPanel {

    private final InstructorClient client;
    private JTable sectionsTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<SectionInfo> sectionInfoList = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(MySectionsPanel.class.getName());
    private final JPanel actionsPanel;
    private final JButton viewTimetableButton, enterScoresButton, computeGradesButton, exportRosterButton;

    public MySectionsPanel(InstructorClient client) {
        this.client = client;
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
        
        sectionsTable = new JTable();
        sorter = new TableRowSorter<>();
        sectionsTable.setRowSorter(sorter);
        styleTable(sectionsTable);
        
        sectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });

        contentPanel.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);

        actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionsPanel.setOpaque(false);

        viewTimetableButton = new JButton("View Timetable");
        enterScoresButton = new JButton("Enter Scores");
        computeGradesButton = new JButton("Compute Grades");
        exportRosterButton = new JButton("Export Roster");

        styleActionButton(viewTimetableButton);
        styleActionButton(enterScoresButton);
        styleActionButton(computeGradesButton);
        styleActionButton(exportRosterButton);

        actionsPanel.add(viewTimetableButton);
        actionsPanel.add(enterScoresButton);
        actionsPanel.add(computeGradesButton);
        actionsPanel.add(exportRosterButton);
        
        contentPanel.add(actionsPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        loadData();
        updateActionButtons();
    }

    private void updateActionButtons() {
        boolean isRowSelected = sectionsTable.getSelectedRow() != -1;
        viewTimetableButton.setEnabled(isRowSelected);
        enterScoresButton.setEnabled(isRowSelected);
        computeGradesButton.setEnabled(isRowSelected);
        exportRosterButton.setEnabled(isRowSelected);
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            public void changedUpdate(DocumentEvent e) { filter(filterField.getText()); }
            public void removeUpdate(DocumentEvent e) { filter(filterField.getText()); }
            public void insertUpdate(DocumentEvent e) { filter(filterField.getText()); }
        });

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData());
        filterPanel.add(refreshButton);

        return filterPanel;
    }

    private void filter(String text) {
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    public void loadData() {
        setLoadingState();
        SwingWorker<List<SectionInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SectionInfo> doInBackground() {
                try {
                    List<SectionResponse> sections = client.getMySections();
                    List<CourseResponse> courses = client.getMyCourses();
                    Map<String, String> courseTitleMap = courses.stream()
                            .collect(Collectors.toMap(CourseResponse::courseId, CourseResponse::title));

                    List<SectionInfo> infoList = new ArrayList<>();
                    for (SectionResponse section : sections) {
                        String title = courseTitleMap.getOrDefault(section.courseId(), "N/A");
                        infoList.add(new SectionInfo(section.sectionId(), section.courseId(), title, section.semester(), section.year(), section.capacity(), section.enrollmentCount(), section.active(), section.registrationDeadline()));
                    }
                    return infoList;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructor sections", e);
                    setErrorState("Failed to load sections. Please try again.");
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    sectionInfoList = get();
                    if (sectionInfoList != null) {
                        if (sectionInfoList.isEmpty()) {
                            setEmptyState();
                        } else {
                            DefaultTableModel model = new DefaultTableModel(new Object[]{"Section ID", "Course", "Term", "Enrolled", "Status", "Deadline"}, 0) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false;
                                }
                            };
                            for (SectionInfo info : sectionInfoList) {
                                model.addRow(new Object[]{
                                    info.sectionId(),
                                    info.courseId() + ": " + info.courseTitle(),
                                    info.semester() + " " + info.year(),
                                    info.enrollment() + "/" + info.capacity(),
                                    info.active() ? "Active" : "Closed",
                                    info.deadline()
                                });
                            }
                            sectionsTable.setModel(model);
                            sorter.setModel((DefaultTableModel) sectionsTable.getModel());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to update sections table", e);
                    setErrorState("An unexpected error occurred.");
                }
            }
        };
        worker.execute();
    }

    private void setLoadingState() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Loading..."}, 0);
        sectionsTable.setModel(model);
    }

    private void setEmptyState() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"No sections assigned."}, 0);
        sectionsTable.setModel(model);
    }

    private void setErrorState(String message) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{message}, 0);
        sectionsTable.setModel(model);
    }

    private record SectionInfo(String sectionId, String courseId, String courseTitle, String semester, int year, int capacity, int enrollment, boolean active, java.time.LocalDate deadline) {}
}
