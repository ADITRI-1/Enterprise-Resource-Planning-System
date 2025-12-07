package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.AdminClient;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class CreateUserWindow extends JFrame {

    private final AdminClient admin;
    private final JFrame parent;

    public CreateUserWindow() { this(null, null); }

    public CreateUserWindow(AdminClient admin, JFrame parent) {
        this.admin = admin;
        this.parent = parent;

        setTitle("Create User");
        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(9, 53, 82)); g2.fillRect(0, 0, getWidth(), getHeight());
                int cy = getHeight() / 2; int left = 24;
                g2.setColor(Color.WHITE); int barW = 8, barH = 38, barR = 4, gap = 8, x = left, barY = cy - barH / 2;
                for (int i = 0; i < 4; i++) { g2.fillRoundRect(x, barY, barW, barH, barR, barR); x += barW + gap; }
                int dOuter = 36, dX = x - 15, dY = cy - dOuter / 2;
                g2.setColor(new Color(85, 172, 168)); g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);
                int textStartX = dX + dOuter + 24;
                g2.setColor(Color.WHITE); Font f = new Font("Segoe UI", Font.BOLD, 18); g2.setFont(f);
                g2.drawString("INDRAPRASTHA INSTITUTE of", textStartX, cy - 6);
                String line2 = "INFORMATION TECHNOLOGY"; g2.drawString(line2, textStartX, cy + 18);
                int delhiX = textStartX + g2.getFontMetrics().stringWidth(line2) + 8;
                g2.setColor(new Color(85, 172, 168)); g2.drawString("DELHI", delhiX, cy + 18);
            }
        };
        header.setPreferredSize(new Dimension(0, 90));
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topBar.setOpaque(false);
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            dispose();
            if (parent != null) parent.setVisible(true);
        });
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            if (parent != null) parent.dispose();
            dispose();
            new LoginUI().setVisible(true);
        });
        topBar.add(back); topBar.add(logout);
        topBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Create User");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(18, 40, 60));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(30, 0, 20, 0));

        JButton studentBtn = styledButton("Student");
        JButton instructorBtn = styledButton("Instructor");

        studentBtn.addActionListener(e -> {
            setVisible(false);
            new StudentAccountWindow(admin, this).setVisible(true);
        });

        instructorBtn.addActionListener(e -> {
            setVisible(false);
            new FacultyAccountWindow(admin, this).setVisible(true);
        });

        JPanel buttons = new JPanel();
        buttons.setBackground(Color.WHITE);
        buttons.add(studentBtn);
        buttons.add(Box.createHorizontalStrut(40));
        buttons.add(instructorBtn);

        center.add(topBar);
        center.add(title);
        center.add(buttons);

        add(center, BorderLayout.CENTER);
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(new Color(29, 85, 137));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 30, 12, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(20, 70, 120), 2, true));
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CreateUserWindow().setVisible(true));
    }
}
