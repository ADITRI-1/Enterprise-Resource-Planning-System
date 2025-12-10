package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ViewAnalyticsPanel extends JPanel {

    private final InstructorClient client;

    public ViewAnalyticsPanel(InstructorClient client) {
        this.client = client;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("View Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // TODO: Add components for viewing analytics
    }
}
