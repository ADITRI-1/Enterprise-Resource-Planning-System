package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.*;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchedulePanel extends JPanel {

    private final AdminClient admin;
    private final boolean isMaintenanceMode;
    private static final Logger LOGGER = Logger.getLogger(SchedulePanel.class.getName());
    private TimetableView timetablePanel;

    public SchedulePanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        TimetableFilterPanel filterPanel = new TimetableFilterPanel(admin);
        timetablePanel = new TimetableView(admin, isMaintenanceMode, this::refreshData);
        
        filterPanel.setSelectionListener(e -> {
            JComboBox<?> source = (JComboBox<?>) e.getSource();
            Object selected = source.getSelectedItem();
            if (selected instanceof CourseResponse) {
                // In a future enhancement, this could filter the sections dropdown
            } else {
                showTimetableFor(selected);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterPanel, timetablePanel);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
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

    public void refreshData() {
        // This can be called to refresh all data in the panel if needed
    }
}
