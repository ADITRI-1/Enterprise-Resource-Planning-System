package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class InstructorDashboardWindow extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorDashboardWindow().setVisible(true));
    }

    public InstructorDashboardWindow() {
        setTitle("IIITD Instructor Dashboard");
        setSize(1250, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        main.add(new SlidingPanel(new LeftRail()), BorderLayout.WEST);
        main.add(new ContentArea(), BorderLayout.CENTER);
    }

    /* ------------ HEADER (IIITD LOGO) ------------ */
    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 90)); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight() / 2;
            int left = 26;

            g2.setColor(Color.WHITE);
            int barW = 10, barH = 44, gap = 10;
            int x = left, barY = cy - barH / 2;
            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, barY, barW, barH, 6, 6);
                x += barW + gap;
            }

            g2.setColor(new Color(85, 172, 168));
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x - 18, cy - 21, 42, 42, -90, 180);

            int textX = x - 18 + 42 + 28;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 6);

            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, textX, cy + 20);

            g2.setColor(new Color(85, 172, 168));
            g2.drawString("DELHI",
                    textX + g2.getFontMetrics().stringWidth(line2) + 10,
                    cy + 20);

            g2.dispose();
        }
    }

    /* ------------ SLIDING PANEL (same pattern as student flow) ------------ */
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
                @Override public void mouseExited(MouseEvent e)  { scheduleCollapse(); }
            };
            arrow.addMouseListener(hover);
            addMouseListener(hover);
            inner.addMouseListener(hover);
        }

        private void scheduleCollapse() {
            Timer t = new Timer(120, e -> {
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
            animate(getWidth(), EXP);
        }

        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.setExpanded(false);
            animate(getWidth(), COL);
        }

        private void animate(int s, int e) {
            if (anim != null && anim.isRunning()) anim.stop();
            anim = new Timer(6, null);
            anim.addActionListener(ev -> {
                int c = getWidth();
                int step = (e > c) ? 12 : -12;
                int n = c + step;
                if ((step > 0 && n >= e) || (step < 0 && n <= e)) {
                    n = e;
                    anim.stop();
                }
                setPreferredSize(new Dimension(n, getHeight()));
                revalidate();
                repaint();
            });
            anim.start();
        }

        @Override public void doLayout() {
            int h = getParent() != null ? getParent().getHeight() : getHeight();
            arrow.setBounds(0, 0, COL, h);
            if (expanded) content.setBounds(0, 0, EXP, h);
            else          content.setBounds(COL, 0, EXP, h);
        }

        static class ArrowIndicator extends JPanel {
            private boolean expanded = false;
            ArrowIndicator(){ setOpaque(false); }
            void setExpanded(boolean b){ expanded = b; repaint(); }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(70, 80, 95));
                int mid = getHeight() / 2;
                if (expanded)
                    g2.fillPolygon(new int[]{20,10,20}, new int[]{mid-10, mid, mid+10}, 3);
                else
                    g2.fillPolygon(new int[]{10,20,10}, new int[]{mid-10, mid, mid+10}, 3);
            }
        }
    }

    /* ------------ LEFT RAIL ------------ */
    static class LeftRail extends JPanel {

        LeftRail() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalStrut(18));
            add(pad(instructorCard(), 16, 16, 6, 16));
            add(Box.createVerticalStrut(12));

            add(pad(nav("Course Sections",
                    e -> new InstructorCourseSectionsWindow().setVisible(true)), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(nav("Enter Scores",
                    e -> new InstructorEnterScoresWindow().setVisible(true)), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(nav("Compute Grades",
                    e -> new InstructorComputeGradesWindow().setVisible(true)), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(nav("View Statistics",
                    e -> new InstructorViewStatsWindow().setVisible(true)), 0, 16, 0, 16));

            add(Box.createVerticalGlue());
        }

        private JPanel instructorCard() {
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

            Avatar a = new Avatar("I");
            a.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel name = new JLabel("Instructor");
            name.setFont(new Font("Segoe UI", Font.BOLD, 18));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createVerticalStrut(6));
            card.add(a);
            card.add(Box.createVerticalStrut(8));
            card.add(name);
            return card;
        }

        private JButton nav(String text, ActionListener al) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            b.setBackground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(223, 228, 234)),
                    new EmptyBorder(10, 14, 10, 14)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(al);
            return b;
        }

        private static JPanel pad(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c);
            return p;
        }

        /** Avatar fixed so the circle + "I" are perfectly centered */
        static class Avatar extends JComponent {
            private final String initial;
            Avatar(String i) {
                initial = i;
                setPreferredSize(new Dimension(120, 120));
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Center the circle inside the component
                int d = Math.min(getWidth(), getHeight()) - 20;   // diameter with some padding
                int x = (getWidth()  - d) / 2;
                int y = (getHeight() - d) / 2;

                g2.setColor(new Color(245, 248, 251));
                g2.fillOval(x, y, d, d);

                g2.setColor(new Color(225, 231, 238));
                g2.setStroke(new BasicStroke(6f));
                g2.drawOval(x + 3, y + 3, d - 6, d - 6);

                // Center the initial inside the circle
                g2.setColor(new Color(46, 56, 66));
                Font f = new Font("Segoe UI", Font.BOLD, d / 2);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int tx = getWidth()  / 2 - fm.stringWidth(initial) / 2;
                int ty = getHeight() / 2 + fm.getAscent() / 2 - 4;
                g2.drawString(initial, tx, ty);

                g2.dispose();
            }
        }
    }

    /* ------------ DASHBOARD CONTENT ------------ */
    static class ContentArea extends JPanel {
        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("INSTRUCTOR DASHBOARD");
            title.setFont(new Font("Segoe UI", Font.BOLD, 32));
            title.setForeground(new Color(31, 45, 61));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(margin(title, 22, 24, 8, 24));

            add(top, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(12, 12, 12, 12);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;

            Color blue = new Color(43, 123, 196);

            gbc.gridx = 0; gbc.gridy = 0;
            grid.add(card("COURSE SECTIONS", "Manage your class sections",
                    "View Sections", blue,
                    e -> new InstructorCourseSectionsWindow().setVisible(true)), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            grid.add(card("ENTER SCORES", "Enter assignment / exam marks",
                    "Go to Gradebook", blue,
                    e -> new InstructorEnterScoresWindow().setVisible(true)), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            grid.add(card("COMPUTE GRADES", "Compute final grades",
                    "Finalize Grades", blue,
                    e -> new InstructorComputeGradesWindow().setVisible(true)), gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            grid.add(card("VIEW STATISTICS", "Analyze class performance",
                    "Access Reports", blue,
                    e -> new InstructorViewStatsWindow().setVisible(true)), gbc);

            add(margin(grid, 0, 18, 18, 24), BorderLayout.CENTER);
        }

        private static JPanel card(String title, String subtitle,
                                   String buttonText, Color blue, ActionListener al) {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0,0,0,30));
                    g2.fillRoundRect(8, 8, getWidth()-16, getHeight()-16, 18, 18);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth()-16, getHeight()-16, 18, 18);
                    super.paintComponent(g);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BorderLayout());

            JPanel txt = new JPanel();
            txt.setOpaque(false);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 20));
            t.setForeground(new Color(31,45,61));

            JLabel s = new JLabel(subtitle);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            s.setForeground(new Color(95,108,121));

            txt.add(margin(t, 18, 20, 2, 20));
            txt.add(margin(s, 2, 20, 8, 20));

            JButton b = new JButton(buttonText);
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setForeground(Color.WHITE);
            b.setBackground(blue);
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(10, 18, 10, 18));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(al);

            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
            btnWrap.setOpaque(false);
            btnWrap.add(b);

            card.add(txt, BorderLayout.NORTH);
            card.add(btnWrap, BorderLayout.CENTER);

            return card;
        }

        private static JPanel margin(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }

    /* ------------ FOOTER ------------ */
    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 56)); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            String[] items = {"© 2024 IIT Delhi", "Privacy Policy", "Terms & Service"};
            int x = 30, y = getHeight()/2 + 6;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + 30;
            }
        }
    }
}
