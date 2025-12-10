package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EnterScoresPanel extends JPanel {

    private final InstructorClient client;
    private final boolean isMaintenanceMode;

    public EnterScoresPanel(InstructorClient client, boolean maintenanceMode) {
        this.client = client;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Enter Scores");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // TODO: Add components for entering scores
    }
}
