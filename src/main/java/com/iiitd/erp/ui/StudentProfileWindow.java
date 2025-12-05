package com.iiitd.erp.ui;

import javax.swing.*;
import java.awt.*;

public class StudentProfileWindow extends JFrame {

    public StudentProfileWindow() {
        setTitle("Student Profile");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel label = new JLabel("Student Profile Window (UI Coming Soon)");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        add(label);
        setVisible(true);
    }
}
