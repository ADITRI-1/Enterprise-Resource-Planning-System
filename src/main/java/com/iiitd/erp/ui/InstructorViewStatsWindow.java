package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * InstructorViewStatsWindow
 * - Shows per-course summary statistics.
 * - Includes placeholder actions for charts, breakdown, and export.
 */
public class InstructorViewStatsWindow extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorViewStatsWindow().setVisible(true));
    }

    public InstructorViewStatsWindow() {
        setTitle("Class Statistics");
        setSize(1250, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // Left sliding navigation panel (if you already have a shared one you can reuse it)
        main.add(new SlidingPanel(new LeftRail()), BorderLayout.WEST);

        // Center content
        main.add(new ContentArea(), BorderLayout.CENTER);
    }

    /* ============================== HEADER ============================== */

    /** Same IIITD logo header as other instructor windows. */
    static class HeaderBar extends JPanel {
        HeaderBar() {
            setPreferredSize(new Dimension(0, 92));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight() / 2;
            int left = 26;

            // 4 white vertical bars
            g2.setColor(Color.WHITE);
            int barW = 10, barH = 44, gap = 10;
            int x = left;
            int barY = cy - barH / 2;
            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, barY, barW, barH, 6, 6);
                x += barW + gap;
            }

            // Teal half-D
            int dOuter = 42;
            int dX = x - 18;
            int dY = cy - dOuter / 2;
            g2.setColor(new Color(85, 172, 168));
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);

            // Text
            int textX = dX + dOuter + 28;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 6);

            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, textX, cy + 20);

            g2.setColor(new Color(85, 172, 168));
            g2.drawString("DELHI", textX + g2.getFontMetrics().stringWidth(line2) + 10, cy + 20);

            g2.dispose();
        }
    }

    /* ========================== SLIDING PANEL ========================== */

    static class SlidingPanel extends JPanel {
        private final JPanel content;
        private final ArrowIndicator arrow;
        private final int EXP = 300, COL = 38;
        private boolean expanded = false;
        private Timer anim;

        SlidingPanel(JPanel inner) {
            content = inner;
            setLayout(null);
            setOpaque(false);
            setPreferredSize(new Dimension(COL, 0));

            arrow = new ArrowIndicator();
            add(arrow);
            add(inner);

            MouseAdapter hover = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
                @Override public void mouseExited(MouseEvent e) { scheduleCollapse(); }
            };
            arrow.addMouseListener(hover);
            addMouseListener(hover);
            inner.addMouseListener(hover);
        }

        private void scheduleCollapse() {
            Timer t = new Timer(120, e -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, this);
                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) {
                    collapse();
                }
            });
            t.setRepeats(false);
            t.start();
        }

        private void expand() {
            if (expanded) return;
            expanded = true;
            arrow.setExpanded(true);
            animate(getWidth(), EXP);
        }

        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.setExpanded(false);
            animate(getWidth(), COL);
        }

        private void animate(int start, int end) {
            if (anim != null && anim.isRunning()) anim.stop();
            anim = new Timer(6, null);
            anim.addActionListener(ev -> {
                int curr = getWidth();
                int step = (end > curr) ? 12 : -12;
                int next = curr + step;
                if ((step > 0 && next >= end) || (step < 0 && next <= end)) {
                    next = end;
                    anim.stop();
                }
                setPreferredSize(new Dimension(next, getHeight()));
                revalidate();
                repaint();
            });
            anim.start();
        }

        @Override
        public void doLayout() {
            int h = (getParent() != null) ? getParent().getHeight() : getHeight();
            arrow.setBounds(0, 0, COL, h);
            if (expanded) content.setBounds(0, 0, EXP, h);
            else content.setBounds(COL, 0, EXP, h);
        }

        static class ArrowIndicator extends JPanel {
            private boolean expanded = false;
            ArrowIndicator() { setOpaque(false); }
            void setExpanded(boolean b) { expanded = b; repaint(); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(70, 80, 95));
                int mid = getHeight() / 2;
                if (expanded) {
                    g2.fillPolygon(new int[]{20, 10, 20},
                                   new int[]{mid - 10, mid, mid + 10}, 3);
                } else {
                    g2.fillPolygon(new int[]{10, 20, 10},
                                   new int[]{mid - 10, mid, mid + 10}, 3);
                }
                g2.dispose();
            }
        }
    }

    /* ============================ LEFT RAIL ============================ */

    static class LeftRail extends JPanel {
        LeftRail() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(18));
            add(pad(profileCard(), 16, 16, 6, 16));
            add(Box.createVerticalStrut(12));

            add(pad(navButton("Dashboard"), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("Course Sections"), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("Enter Scores"), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("Compute Grades"), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("View Statistics"), 0, 16, 0, 16));

            add(Box.createVerticalGlue());
        }

        private static JPanel profileCard() {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(16, 0, 16, 0));

            Avatar avatar = new Avatar("I");
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel name = new JLabel("Instructor");
            name.setFont(new Font("Segoe UI", Font.BOLD, 18));
            name.setForeground(new Color(46, 56, 66));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createVerticalStrut(6));
            card.add(avatar);
            card.add(Box.createVerticalStrut(8));
            card.add(name);

            return card;
        }

        private static JButton navButton(String text) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            b.setBackground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(223, 228, 234)),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // Hook for navigation later
            b.addActionListener(e -> JOptionPane.showMessageDialog(
                    null,
                    "Navigation for \"" + text + "\" not wired yet.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            ));
            return b;
        }

        private static JPanel pad(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }

        /** Simple circular avatar with initial. */
        static class Avatar extends JComponent {
            private final String initial;
            Avatar(String i) { initial = i; setPreferredSize(new Dimension(120, 120)); }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = 100, x = 10, y = 10;
                g2.setColor(new Color(245, 248, 251));
                g2.fillOval(x, y, d, d);
                g2.setColor(new Color(225, 231, 238));
                g2.setStroke(new BasicStroke(6f));
                g2.drawOval(x + 3, y + 3, d - 6, d - 6);

                g2.setColor(new Color(46, 56, 66));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                        x + d / 2 - fm.stringWidth(initial) / 2,
                        y + d / 2 + fm.getAscent() / 4);
                g2.dispose();
            }
        }
    }

    /* ============================ CONTENT ============================ */

    static class ContentArea extends JPanel {
        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            // Title
            JLabel title = new JLabel("VIEW STATISTICS");
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(new Color(31, 45, 61));
            JPanel titleWrap = new JPanel(new BorderLayout());
            titleWrap.setOpaque(false);
            titleWrap.setBorder(new EmptyBorder(20, 24, 12, 24));
            titleWrap.add(title, BorderLayout.WEST);
            add(titleWrap, BorderLayout.NORTH);

            // Table
            // ----- UPDATED TABLE (ONLY THIS PART IS CHANGED) -----

String[] cols = {
        "Course Code",
        "Course Title",
        "Pass Percentage",
        "Average",
        "Median",
        "Highest",
        "Lowest"
};

// Sample static data (you can later connect this to real backend data)
Object[][] data = {
        {"CSE201", "Operating Systems", "91%", 67.5, 68, 95, 32},
        {"CSE301", "Algorithms", "94%", 71.2, 72, 99, 40}
};

DefaultTableModel model = new DefaultTableModel(data, cols) {
    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // statistics table should be read-only
    }
};

JTable table = new JTable(model);
table.setFont(new Font("Segoe UI", Font.PLAIN, 17));
table.setRowHeight(32);
table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));

JScrollPane sp = new JScrollPane(table);
sp.setBorder(new EmptyBorder(12, 28, 12, 28));

add(sp, BorderLayout.CENTER);


            // Bottom info strip: tip + action buttons (hooks for future charts / export)
            JPanel bottomStrip = new JPanel(new BorderLayout());
            bottomStrip.setBackground(new Color(245, 248, 251));
            bottomStrip.setBorder(new EmptyBorder(12, 24, 12, 24));

            JLabel tip = new JLabel(
                    "Tip: You can extend this window later to show charts, per-assignment breakdown, or export statistics."
            );
            tip.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tip.setForeground(new Color(90, 104, 120));
            bottomStrip.add(tip, BorderLayout.WEST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);

            JButton chartsBtn = primaryButton("Show Charts");
            chartsBtn.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,
                            "Hook for charts / graphs.\nLater you can open a new window or panel with charts here.",
                            "Charts (Placeholder)", JOptionPane.INFORMATION_MESSAGE));

            JButton breakdownBtn = primaryButton("Assignment Breakdown");
            breakdownBtn.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,
                            "Hook for per-assignment breakdown.\nYou can later display another table or chart.",
                            "Breakdown (Placeholder)", JOptionPane.INFORMATION_MESSAGE));

            JButton exportBtn = primaryButton("Export Statistics");
            exportBtn.addActionListener(e ->
                    JOptionPane.showMessageDialog(this,
                            "Hook for export (CSV / Excel / PDF).\nLater you can write the actual export logic here.",
                            "Export (Placeholder)", JOptionPane.INFORMATION_MESSAGE));

            actions.add(chartsBtn);
            actions.add(breakdownBtn);
            actions.add(exportBtn);

            bottomStrip.add(actions, BorderLayout.EAST);

            add(bottomStrip, BorderLayout.SOUTH);
        }

        /** Blue primary-style button used in many instructor windows. */
        private static JButton primaryButton(String text) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 15));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(43, 123, 196));
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(8, 14, 8, 14));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    b.setBackground(new Color(35, 111, 182));
                }
                @Override public void mouseExited(MouseEvent e) {
                    b.setBackground(new Color(43, 123, 196));
                }
            });
            return b;
        }
    }

    /* ============================= FOOTER ============================= */

    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 56)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            String[] items = {"© 2024 IIT Delhi", "Privacy Policy", "Terms & Service"};
            int x = 28;
            int y = getHeight() / 2 + 6;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + 28;
            }
            g2.dispose();
        }
    }
}
