package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.AdminClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SchedulePanel extends JPanel {

    private final AdminClient admin;
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIMES = {"09:00 - 10:30", "10:45 - 12:15", "13:00 - 14:30", "14:45 - 16:15"};

    public SchedulePanel(AdminClient admin) {
        this.admin = admin;
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // Header
        JLabel title = new JLabel("Class Schedule Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Timetable Grid
        JPanel timetablePanel = new JPanel(new GridLayout(TIMES.length + 1, DAYS.length + 1, 5, 5));
        timetablePanel.setOpaque(false);
        
        // Add headers
        timetablePanel.add(new JLabel("")); // Corner
        for (String day : DAYS) {
            timetablePanel.add(createHeaderLabel(day));
        }
        for (String time : TIMES) {
            timetablePanel.add(createHeaderLabel(time));
            for (String day : DAYS) {
                JPanel slotPanel = new JPanel();
                slotPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                timetablePanel.add(slotPanel);
            }
        }

        add(new JScrollPane(timetablePanel), BorderLayout.CENTER);

        // Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton scheduleButton = new JButton("Schedule a Class");
        stylePrimaryButton(scheduleButton);
        // scheduleButton.addActionListener(e -> openScheduleDialog());
        actionsPanel.add(scheduleButton);
        add(actionsPanel, BorderLayout.SOUTH);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        return label;
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
