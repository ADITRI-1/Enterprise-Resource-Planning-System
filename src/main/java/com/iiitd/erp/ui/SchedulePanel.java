package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.RoomResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.logging.Logger;

public class SchedulePanel extends JPanel {

    private final AdminClient admin;
    private final boolean isMaintenanceMode;
    private static final Logger LOGGER = Logger.getLogger(SchedulePanel.class.getName());
    private TimetableView timetablePanel;
    private TimetableFilterPanel filterPanel;
    private JComboBox<String> semesterComboBox;
    private JComboBox<Integer> yearComboBox;

    public SchedulePanel(AdminClient admin, boolean isMaintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = isMaintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(20, 10));
        setBorder(new EmptyBorder(10, 20, 20, 20));

        add(createTermSelector(), BorderLayout.NORTH);
        add(createTabbedPane(), BorderLayout.CENTER);
    }

    private JComponent createTermSelector() {
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        termPanel.setOpaque(false);

        termPanel.add(new JLabel("Semester:"));
        semesterComboBox = new JComboBox<>(new String[]{"Monsoon", "Winter"});
        termPanel.add(semesterComboBox);

        termPanel.add(new JLabel("Year:"));
        yearComboBox = new JComboBox<>(getYearList());
        termPanel.add(yearComboBox);

        // Set default to current term
        LocalDate now = LocalDate.now();
        semesterComboBox.setSelectedItem(now.getMonthValue() >= 7 ? "Monsoon" : "Winter");
        yearComboBox.setSelectedItem(now.getYear());

        // Add listeners to refresh data on term change
        semesterComboBox.addActionListener(e -> onTermChange());
        yearComboBox.addActionListener(e -> onTermChange());

        return termPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Section Builder Tab
        this.filterPanel = new TimetableFilterPanel(admin);
        this.timetablePanel = new TimetableView(admin, isMaintenanceMode, this::refreshData);
        filterPanel.setSelectionListener(this::showTimetableFor);

        JPanel builderPanel = new JPanel(new BorderLayout(15, 0));
        builderPanel.setOpaque(false);
        builderPanel.add(filterPanel, BorderLayout.WEST);
        builderPanel.add(timetablePanel, BorderLayout.CENTER);
        
        // Global Search Tab
        TimetableSearchPanel searchPanel = new TimetableSearchPanel(admin, timetablePanel);
        
        tabbedPane.addTab("Section Builder", builderPanel);
        tabbedPane.addTab("Global Search", searchPanel);
        
        return tabbedPane;
    }

    private void onTermChange() {
        LOGGER.info("Term changed to: " + getSemester() + " " + getYear());
        filterPanel.loadFilters();
        timetablePanel.clearScheduleView();
    }

    private void showTimetableFor(Object selection) {
        if (selection == null) {
            timetablePanel.clearScheduleView();
            return;
        }

        String viewType = "";
        String id = "";
        SectionResponse section = null;

        if (selection instanceof SectionResponse) {
            viewType = "Section";
            section = (SectionResponse) selection;
            id = section.sectionId();
        } else if (selection instanceof RoomResponse) {
            viewType = "Room";
            id = ((RoomResponse) selection).roomId();
        } else if (selection instanceof InstructorResponse) {
            viewType = "Instructor";
            id = ((InstructorResponse) selection).instructorCode();
        }
        
        timetablePanel.setView(viewType, id, section);
    }

    private Integer[] getYearList() {
        int currentYear = LocalDate.now().getYear();
        Integer[] years = new Integer[5];
        for (int i = 0; i < 5; i++) {
            years[i] = currentYear - 2 + i;
        }
        return years;
    }

    public String getSemester() {
        return (String) semesterComboBox.getSelectedItem();
    }

    public int getYear() {
        return (int) yearComboBox.getSelectedItem();
    }

    public void refreshData() {
        filterPanel.loadFilters();
        timetablePanel.clearScheduleView();
    }
}
