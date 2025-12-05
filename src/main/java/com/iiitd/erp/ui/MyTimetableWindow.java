package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * MyTimetableWindow
 * - Self-contained testable window for student's timetable.
 * - Save as MyTimetableWindow.java
 */
public class MyTimetableWindow extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MyTimetableWindow().setVisible(true));
    }

    public MyTimetableWindow() {
        setTitle("My Timetable");
        setSize(1250, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // header + footer reuse pattern used in other windows
        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // sliding left rail (hover expands)
        main.add(new SlidingPanel(new LeftRail()), BorderLayout.WEST);

        // content area
        main.add(new ContentArea(), BorderLayout.CENTER);
    }

    /* ---------------- HEADER ---------------- */
    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 92)); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight()/2;
            int left = 26;

            // White bars logo
            g2.setColor(Color.WHITE);
            int barW = 10, barH = 44, barR = 6, gap = 10;
            int x = left;
            int barY = cy - barH/2;
            for (int i=0;i<4;i++) {
                g2.fillRoundRect(x, barY, barW, barH, barR, barR);
                x += barW + gap;
            }
            // teal arc
            int dOuter = 42;
            int dX = x - 18;
            int dY = cy - dOuter/2;
            g2.setColor(new Color(85,172,168));
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);

            // text
            int textX = dX + dOuter + 28;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 6);
            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, textX, cy + 18);
            g2.setColor(new Color(85,172,168));
            g2.drawString("DELHI", textX + g2.getFontMetrics().stringWidth(line2) + 10, cy + 18);

            g2.dispose();
        }
    }

    /* ---------------- FOOTER ---------------- */
    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 60)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(9,53,82));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            String[] items = { "© 2024 IIT Delhi", "Privacy Policy", "Terms & Service" };
            int x = 40;
            int y = getHeight()/2 + 6;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + 40;
            }
            g2.dispose();
        }
    }

    /* ---------------- SLIDING PANEL (hover to expand) ---------------- */
    static class SlidingPanel extends JPanel {
        private final JPanel content;
        private final ArrowIndicator arrow;
        private final int EXP = 300;
        private final int COL = 38;
        private boolean expanded = false;
        private Timer animator;

        SlidingPanel(JPanel inner) {
            setLayout(null);
            setOpaque(false);
            setPreferredSize(new Dimension(COL, 0));

            content = inner;
            content.setOpaque(false);

            arrow = new ArrowIndicator();
            add(arrow);
            add(content);

            // hover behavior (MouseAdapter requires java.awt.event import)
            MouseAdapter hover = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
                @Override public void mouseExited(MouseEvent e) { scheduleCollapse(); }
            };

            arrow.addMouseListener(hover);
            addMouseListener(hover);
            content.addMouseListener(hover);
        }

        private void scheduleCollapse() {
            Timer t = new Timer(140, e -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, this);
                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) collapse();
            });
            t.setRepeats(false);
            t.start();
        }

        private void expand() {
            if (expanded) return;
            expanded = true;
            arrow.setExpanded(true);
            animateSize(getWidth(), EXP);
        }
        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.setExpanded(false);
            animateSize(getWidth(), COL);
        }

        private void animateSize(int start, int end) {
            if (animator != null && animator.isRunning()) animator.stop();
            animator = new Timer(5, null);
            animator.addActionListener(e -> {
                int curr = getWidth();
                int step = (end > curr) ? 12 : -12;
                int next = curr + step;
                if ((step > 0 && next >= end) || (step < 0 && next <= end)) {
                    next = end;
                    animator.stop();
                }
                setPreferredSize(new Dimension(next, getHeight()));
                revalidate();
                repaint();
            });
            animator.start();
        }

        @Override public void doLayout() {
            int h = getParent() != null ? getParent().getHeight() : getHeight();
            arrow.setBounds(0, 0, COL, h);
            if (expanded) content.setBounds(0, 0, EXP, h);
            else content.setBounds(COL, 0, EXP, h);
        }

        static class ArrowIndicator extends JPanel {
            private boolean expanded = false;
            ArrowIndicator() { setOpaque(false); }
            void setExpanded(boolean b) { expanded = b; repaint(); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(230,233,240));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(70,80,95));
                int mid = getHeight()/2;
                if (expanded) g2.fillPolygon(new int[]{20,10,20}, new int[]{mid-10, mid, mid+10}, 3);
                else g2.fillPolygon(new int[]{10,20,10}, new int[]{mid-10, mid, mid+10}, 3);
                g2.dispose();
            }
        }
    }

    /* ---------------- LEFT RAIL ---------------- */
    static class LeftRail extends JPanel {
        LeftRail() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalStrut(18));

            // card (avatar + name) styled like course windows
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(16, 0, 16, 0));

            // circular S icon
            JComponent avatar = new JComponent() {
                @Override public Dimension getPreferredSize() { return new Dimension(120,120); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int d = Math.min(getWidth(), getHeight()) - 20;
                    int x = (getWidth()-d)/2, y=(getHeight()-d)/2;
                    g2.setColor(new Color(245,248,251));
                    g2.fillOval(x,y,d,d);
                    g2.setColor(new Color(225,231,238));
                    g2.setStroke(new BasicStroke(6f));
                    g2.drawOval(x+3,y+3,d-6,d-6);
                    g2.setColor(new Color(41,52,67));
                    Font f = new Font("Segoe UI", Font.BOLD, d/2);
                    g2.setFont(f);
                    FontMetrics fm = g2.getFontMetrics();
                    String s = "S";
                    g2.drawString(s, getWidth()/2 - fm.stringWidth(s)/2, getHeight()/2 + fm.getAscent()/2 - 6);
                    g2.dispose();
                }
            };
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel name = new JLabel("Student");
            name.setFont(new Font("Segoe UI", Font.BOLD, 20));
            name.setForeground(new Color(33,43,54));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(avatar);
            card.add(Box.createVerticalStrut(8));
            card.add(name);

            add(pad(card, 12, 16, 8, 16));

            add(Box.createVerticalStrut(10));
            add(nav("My Courses", e -> new MyCoursesWindow().setVisible(true)));
            add(Box.createVerticalStrut(10));
            add(nav("Register Courses", e -> new RegisterCoursesWindow().setVisible(true)));
            add(Box.createVerticalStrut(10));
            add(nav("My Timetable", e -> new MyTimetableWindow().setVisible(true)));
            add(Box.createVerticalStrut(10));
            add(nav("Grades", e -> new ViewGradesWindow().setVisible(true)));

            add(Box.createVerticalGlue());
        }

        private JPanel nav(String text, ActionListener al) {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(new Color(220,225,230));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(240, 62));
            p.setBorder(new EmptyBorder(10, 16, 10, 16));

            JButton b = new JButton(text);
            b.setFocusPainted(false);
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorder(null);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            b.setForeground(new Color(46,56,66));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(al);

            p.add(b, BorderLayout.WEST);
            return pad(p, 0, 12, 0, 12);
        }

        private static JPanel pad(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }

    /* ---------------- CONTENT ---------------- */
    static class ContentArea extends JPanel {
        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            // Title placed to align with form column (like other windows)
            JLabel title = new JLabel("MY TIMETABLE");
            title.setFont(new Font("Segoe UI", Font.BOLD, 38));
            title.setForeground(new Color(31,45,61));
            JPanel titleWrap = new JPanel(new BorderLayout());
            titleWrap.setOpaque(false);
            titleWrap.setBorder(new EmptyBorder(20, 24, 6, 24));
            titleWrap.add(title, BorderLayout.WEST);
            add(titleWrap, BorderLayout.NORTH);

            // Example timetable using JTable
            String[] cols = {"Day / Time", "09:00-10:30", "10:45-12:15", "13:00-14:30", "14:45-16:15"};
            Object[][] data = {
                    {"Mon", "CSE201", "----", "CSE212", "----"},
                    {"Tue", "----", "CSE301", "----", "CSE201"},
                    {"Wed", "CSE212", "----", "CSE301", "----"},
                    {"Thu", "----", "CSE201", "----", "CSE212"},
                    {"Fri", "CSE301", "----", "----", "----"}
            };
            JTable table = new JTable(data, cols);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            table.setRowHeight(42);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(null);
            sp.setOpaque(false);
            add(sp, BorderLayout.CENTER);
        }
    }

    /* -------------------- Placeholder windows  --------------------
       If you already have MyCoursesWindow, RegisterCoursesWindow,
       ViewGradesWindow classes in your project, remove the stubs below.
       These stubs exist so the file compiles standalone.
    */
    public static class MyCoursesWindow extends JFrame {
        public MyCoursesWindow() {
            setTitle("My Courses"); setSize(900,600); setLocationRelativeTo(null);
            add(new JLabel("MyCoursesWindow - stub"), BorderLayout.CENTER);
        }
    }
    public static class RegisterCoursesWindow extends JFrame {
        public RegisterCoursesWindow() {
            setTitle("Register Courses"); setSize(900,600); setLocationRelativeTo(null);
            add(new JLabel("RegisterCoursesWindow - stub"), BorderLayout.CENTER);
        }
    }
    public static class ViewGradesWindow extends JFrame {
        public ViewGradesWindow() {
            setTitle("View Grades"); setSize(900,600); setLocationRelativeTo(null);
            add(new JLabel("ViewGradesWindow - stub"), BorderLayout.CENTER);
        }
    }
}
