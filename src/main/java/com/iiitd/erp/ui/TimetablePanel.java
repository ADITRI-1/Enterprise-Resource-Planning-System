package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Logger;

public class TimetablePanel extends JPanel {

    private final StudentClient student;
    private static final Logger LOGGER = Logger.getLogger(TimetablePanel.class.getName());
    private final TimetableView timetableView;

    public TimetablePanel(StudentClient student) {
        this.student = student;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("My Timetable");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        timetableView = new TimetableView();
        add(new JScrollPane(timetableView), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadTimetable();
    }

    private void loadTimetable() {
        SwingWorker<com.iiitd.erp.io.dto.response.Timetable, Void> worker = new SwingWorker<>() {
            @Override
            protected com.iiitd.erp.io.dto.response.Timetable doInBackground() throws Exception {
                return student.getMyTimetable();
            }

            @Override
            protected void done() {
                try {
                    com.iiitd.erp.io.dto.response.Timetable timetable = get();
                    timetableView.setTimetable(timetable);
                } catch (Exception e) {
                    LOGGER.severe("Failed to load timetable: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
