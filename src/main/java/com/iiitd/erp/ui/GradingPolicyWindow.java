package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Simple window that shows the grading policy / weighting rules.
 */
public class GradingPolicyWindow extends JFrame {

    public GradingPolicyWindow() {
        setTitle("Grading Policy");
        setSize(550, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        JLabel title = new JLabel("GRADING POLICY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(31, 45, 61));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(18, 24, 8, 24));
        top.add(title, BorderLayout.WEST);
        main.add(top, BorderLayout.NORTH);

        String[] cols = { "Component", "Weight (%)", "Max Marks" };
        Object[][] data = {
                {"Quiz",       10,  10},
                {"Assignment", 20,  20},
                {"Project",    20,  20},
                {"Midsem",     20,  25},
                {"Endsem",     30,  25}
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(0, 24, 16, 24));
        main.add(sp, BorderLayout.CENTER);
    }

    static class HeaderBar extends InstructorComputeGradesWindow.HeaderBar {}
    static class FooterBar extends InstructorComputeGradesWindow.FooterBar {}
}
