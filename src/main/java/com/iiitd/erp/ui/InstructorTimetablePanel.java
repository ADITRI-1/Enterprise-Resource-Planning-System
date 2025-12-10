package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.TimetableSlot;
import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class InstructorTimetablePanel extends JPanel {

    private final InstructorClient client;
    private static final Logger LOGGER = Logger.getLogger(InstructorTimetablePanel.class.getName());
    private final JTable table;

    public InstructorTimetablePanel(InstructorClient client) {
        this.client = client;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("My Timetable");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Day", "Start", "End", "Course", "Title", "Room"}, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadTimetable();
    }

    public void loadTimetable() {
        SwingWorker<List<TimetableSlot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TimetableSlot> doInBackground() throws Exception {
                LocalDate now = LocalDate.now();
                String semester = now.getMonthValue() >= 7 ? "Monsoon" : "Winter";
                int year = now.getYear();
                return client.getMyTimetable(semester, year);
            }

            @Override
            protected void done() {
                try {
                    List<TimetableSlot> slots = get();
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setRowCount(0);
                    if (slots != null) {
                        for (TimetableSlot slot : slots) {
                            model.addRow(new Object[]{
                                    slot.dayOfWeek(),
                                    slot.startTime(),
                                    slot.endTime(),
                                    slot.courseCode(),
                                    slot.courseTitle(),
                                    slot.room()
                            });
                        }
                    }
                } catch (Exception e) {
                    LOGGER.severe("Failed to load timetable: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
