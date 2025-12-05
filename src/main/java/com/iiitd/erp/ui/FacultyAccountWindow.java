package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class FacultyAccountWindow extends JFrame {

    public FacultyAccountWindow() {

        setTitle("Create Faculty Account");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // ✅ Sliding left panel exactly like AdminDashboard
        SlidingPanel slidingLeft = new SlidingPanel(new LeftRail());
        main.add(slidingLeft, BorderLayout.WEST);

        main.add(new ContentPanel(), BorderLayout.CENTER);
    }

    /* ============================================================
                      SLIDING PANEL (unchanged)
       ============================================================ */
    static class SlidingPanel extends JPanel {

        private final JPanel content;
        private final ArrowIndicator arrow;
        private final int expandedWidth = 300;
        private final int collapsedWidth = 36;

        private boolean expanded = false;
        private Timer animator;

        SlidingPanel(JPanel inner) {
            this.content = inner;
            setLayout(null);
            setPreferredSize(new Dimension(collapsedWidth, 0));
            setOpaque(false);

            arrow = new ArrowIndicator();
            arrow.setBounds(0, 0, collapsedWidth, 80);
            add(arrow);

            inner.setOpaque(false);
            add(inner);

            arrow.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
            });

            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { collapseLater(); }
            });

            inner.addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { collapseLater(); }
            });
        }

        @Override
        public void doLayout() {
            int h = getParent().getHeight();

            arrow.setBounds(0, 0, collapsedWidth, h);

            if (expanded)
                content.setBounds(0, 0, expandedWidth, h);
            else
                content.setBounds(collapsedWidth, 0, expandedWidth, h);
        }

        private void expand() {
            if (expanded) return;
            expanded = true;
            arrow.setExpanded(true);
            animateWidth(getWidth(), expandedWidth);
        }

        private void collapseLater() {
            Timer t = new Timer(120, ev -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, this);
                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight())
                    collapse();
            });
            t.setRepeats(false);
            t.start();
        }

        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.setExpanded(false);
            animateWidth(getWidth(), collapsedWidth);
        }

        private void animateWidth(int start, int end) {
            if (animator != null && animator.isRunning()) animator.stop();

            animator = new Timer(5, null);
            animator.addActionListener(e -> {
                int curr = getWidth();
                int step = (end > start) ? 12 : -12;
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

        static class ArrowIndicator extends JPanel {

            private boolean expanded = false;

            ArrowIndicator() { setOpaque(false); }

            void setExpanded(boolean b) { expanded = b; repaint(); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(70, 80, 95));
                int mid = getHeight() / 2;

                int[] x, y;

                if (expanded) {
                    x = new int[]{20, 10, 20};
                    y = new int[]{mid - 10, mid, mid + 10};
                } else {
                    x = new int[]{10, 20, 10};
                    y = new int[]{mid - 10, mid, mid + 10};
                }

                g2.fillPolygon(x, y, 3);
            }
        }
    }

    /* ============================================================
                      LEFT PANEL (Avatar FIXED)
       ============================================================ */
    static class LeftRail extends JPanel {
        LeftRail() {
            setPreferredSize(new Dimension(300, 0));
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel profileCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.WHITE);
                    ((Graphics2D) g).fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            profileCard.setOpaque(false);
            profileCard.setBorder(new EmptyBorder(20, 0, 20, 0));
            profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));

            // ✅ NEW AVATAR (exact same design as AdminDashboard)
            Avatar avatar = new Avatar("A");
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel adminLabel = new JLabel("Admin");
            adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            adminLabel.setForeground(new Color(40, 50, 65));
            adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            profileCard.add(avatar);
            profileCard.add(Box.createVerticalStrut(10));
            profileCard.add(adminLabel);

            add(wrap(profileCard, 16), BorderLayout.NORTH);

            JPanel nav = new JPanel();
            nav.setOpaque(false);
            nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

            nav.add(navButton("Create User"));
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("Create Courses"));
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("Create Sections"));

            add(wrap(nav, 16), BorderLayout.CENTER);
        }

        private JPanel navButton(String text) {
            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                    g2.setColor(new Color(220, 225, 230));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                }
            };

            p.setOpaque(false);
            p.setPreferredSize(new Dimension(250, 64));
            p.setBorder(new EmptyBorder(10, 16, 10, 16));

            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            lbl.setForeground(new Color(50, 60, 80));

            p.add(lbl, BorderLayout.WEST);

            return p;
        }

        private JPanel wrap(JComponent c, int margin) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(margin, margin, margin, margin));
            p.add(c);
            return p;
        }

        // ✅ Circular avatar identical to your provided UI
        static class Avatar extends JComponent {
            private final String letter;

            Avatar(String letter) {
                this.letter = letter;
                setPreferredSize(new Dimension(140, 140));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = 120;
                int x = (getWidth() - d) / 2;

                g2.setColor(new Color(245, 248, 251));
                g2.fillOval(x, 10, d, d);

                g2.setStroke(new BasicStroke(6f));
                g2.setColor(new Color(220, 225, 235));
                g2.drawOval(x + 3, 13, d - 6, d - 6);

                g2.setColor(new Color(40, 50, 65));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 46));

                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(letter,
                        x + (d - fm.stringWidth(letter)) / 2,
                        10 + (d + fm.getAscent()) / 2 - 10);
            }
        }
    }

    /* ============================================================
                      MAIN CONTENT PANEL
       ============================================================ */
    static class ContentPanel extends JPanel {
        ContentPanel() {

            setLayout(null); // ✅ absolute layout to control widths visually
            setOpaque(false);

            int baseX = 360;
            int fieldWidth = 450;  // ✅ reduced width
            int labelWidth = 180;
            int rowH = 40;

            // TITLE
            JLabel title = new JLabel("CREATE FACULTY ACCOUNT");
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setBounds(baseX, 40, 700, 50);
            add(title);

            /* ---------- SELECT ROLE ---------- */

            JLabel roleLabel = new JLabel("Select Role");
            roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            roleLabel.setBounds(baseX, 120, labelWidth, 30);
            add(roleLabel);

            JComboBox<String> roleBox = new JComboBox<>(new String[]{"Faculty", "Student"});
            roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            roleBox.setBounds(baseX + labelWidth + 20, 120, fieldWidth, rowH);
            add(roleBox);

            /* ---------- DETAILS TITLE ---------- */
            JLabel details = new JLabel("Details");
            details.setFont(new Font("Segoe UI", Font.BOLD, 22));
            details.setBounds(baseX, 180, 300, 30);
            add(details);

            /* ---------- FORM FIELDS ---------- */

            // Full Name
            addField("Full Name", baseX, 240, labelWidth, rowH, fieldWidth);

            // User ID
            addField("User ID", baseX, 300, labelWidth, rowH, fieldWidth);

            // Department dropdown
            JLabel depLabel = label("Department");
            depLabel.setBounds(baseX, 360, labelWidth, rowH);
            add(depLabel);

            JComboBox<String> depBox = new JComboBox<>(new String[]{"CSE", "ECE", "SSH", "AI"});
            depBox.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            depBox.setBounds(baseX + labelWidth + 20, 360, fieldWidth, rowH);
            add(depBox);

            // Year
            JLabel yearLabel = label("Year of Joining");
            yearLabel.setBounds(baseX, 420, labelWidth, rowH);
            add(yearLabel);

            JComboBox<String> yearBox = new JComboBox<>(new String[]{"2019", "2020", "2021", "2022", "2023"});
            yearBox.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            yearBox.setBounds(baseX + labelWidth + 20, 420, fieldWidth, rowH);
            add(yearBox);

            // Official Email
            addField("Official Email", baseX, 480, labelWidth, rowH, fieldWidth);

            JButton submit = new JButton("Create Faculty Account");
            submit.setFont(new Font("Segoe UI", Font.BOLD, 20));
            submit.setBackground(new Color(43, 123, 196));
            submit.setForeground(Color.WHITE);
            submit.setFocusPainted(false);
            submit.setBounds(baseX, 560, fieldWidth + labelWidth + 20, 50);
            add(submit);
        }

        private void addField(String name, int x, int y, int lw, int h, int fw) {
            JLabel lbl = label(name);
            lbl.setBounds(x, y, lw, h);
            add(lbl);

            JTextField tf = new JTextField(name);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            tf.setBounds(x + lw + 20, y, fw, h);
            add(tf);
        }

        private JLabel label(String t) {
            JLabel l = new JLabel(t);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            l.setForeground(new Color(50, 60, 80));
            return l;
        }
    }

    /* ============================================================
                      HEADER & FOOTER (same style)
       ============================================================ */

    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 72)); }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int x = 30;
            int cy = getHeight() / 2;

            g2.setColor(Color.WHITE);
            int barW = 8, barH = 36, gap = 8;
            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, cy - 18, barW, barH, 6, 6);
                x += barW + gap;
            }

            g2.setColor(new Color(85, 172, 168));
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x - 12, cy - 18, 36, 36, -90, 180);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("INDRAPRASTHA INSTITUTE of", 120, cy - 4);

            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, 120, cy + 18);

            g2.setColor(new Color(85, 172, 168));
            g2.drawString("DELHI", 120 + g2.getFontMetrics().stringWidth(line2) + 8, cy + 18);
        }
    }

    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 50)); }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(9, 53, 82));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            String[] items = {"© 2024 IIT Delhi", "Privacy Policy", "Terms & Service"};
            int x = 20;

            for (String s : items) {
                g.drawString(s, x, 32);
                x += g.getFontMetrics().stringWidth(s) + 28;
            }
        }
    }
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new FacultyAccountWindow().setVisible(true);
    });
}
}
