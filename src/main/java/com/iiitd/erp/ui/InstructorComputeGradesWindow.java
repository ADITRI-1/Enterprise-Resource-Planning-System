package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Main instructor window for computing grades.
 * Shows a "Select Course" combo and a "Compute Grade" button.
 */
public class InstructorComputeGradesWindow extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new InstructorComputeGradesWindow().setVisible(true));
    }

    public InstructorComputeGradesWindow() {
        setTitle("Compute Grades");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // left sliding rail (instructor nav)
        main.add(new SlidingPanel(new LeftRail()), BorderLayout.WEST);

        // center content
        main.add(buildContent(), BorderLayout.CENTER);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        // Title
        JLabel title = new JLabel("COMPUTE GRADES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(31, 45, 61));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.setBorder(new EmptyBorder(24, 28, 10, 28));
        titleWrap.add(title, BorderLayout.WEST);

        root.add(titleWrap, BorderLayout.NORTH);

        // Card in middle with course selector + compute button
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(220, 225, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel instr = new JLabel("Select a course and click \"Compute Grade\" to view the grade sheet.");
        instr.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        instr.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        controlRow.setOpaque(false);

        JLabel courseLbl = new JLabel("Select Course:");
        courseLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        String[] courses = {
                "CS301 - Algorithms",
                "CS305 - Operating Systems",
                "CS401 - Machine Learning"
        };
        JComboBox<String> courseCombo = new JComboBox<>(courses);
        courseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        courseCombo.setPreferredSize(new Dimension(320, 32));

        JButton computeBtn = new JButton("Compute Grade");
        computeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        computeBtn.setForeground(Color.WHITE);
        computeBtn.setBackground(new Color(43, 123, 196));
        computeBtn.setFocusPainted(false);
        computeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        computeBtn.setBorder(new EmptyBorder(10, 18, 10, 18));

        computeBtn.addActionListener(e -> {
            String selected = (String) courseCombo.getSelectedItem();
//            new GradeComputationWindow(selected, courses).setVisible(true);
        });

        controlRow.add(courseLbl);
        controlRow.add(courseCombo);
        controlRow.add(computeBtn);

        card.add(instr);
        card.add(Box.createVerticalStrut(16));
        card.add(controlRow);

        JPanel cardWrap = new JPanel(new GridBagLayout());
        cardWrap.setOpaque(false);
        cardWrap.setBorder(new EmptyBorder(40, 40, 40, 40));
        cardWrap.add(card);

        root.add(cardWrap, BorderLayout.CENTER);

        return root;
    }

    /* ========= IIIT-D HEADER (same style as other windows) ========= */
    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 92)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight() / 2;
            int left = 26;

            // white bars
            g2.setColor(Color.WHITE);
            int barW = 10, barH = 44, gap = 10;
            int x = left;
            int barY = cy - barH / 2;
            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, barY, barW, barH, 6, 6);
                x += barW + gap;
            }

            // teal D arc
            g2.setColor(new Color(85, 172, 168));
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
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

    /* ================= SLIDING PANEL (left rail wrapper) ================= */
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

        @Override
        public void doLayout() {
            int h = (getParent() != null) ? getParent().getHeight() : getHeight();
            arrow.setBounds(0, 0, COL, h);
            if (expanded)
                content.setBounds(0, 0, EXP, h);
            else
                content.setBounds(COL, 0, EXP, h);
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
                if (expanded)
                    g2.fillPolygon(new int[]{20, 10, 20},
                                   new int[]{mid - 10, mid, mid + 10}, 3);
                else
                    g2.fillPolygon(new int[]{10, 20, 10},
                                   new int[]{mid - 10, mid, mid + 10}, 3);
                g2.dispose();
            }
        }
    }

    /* ================= LEFT RAIL ================= */
    static class LeftRail extends JPanel {
        LeftRail() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(18));
            add(pad(profileCard(), 16, 16, 6, 16));
            add(Box.createVerticalStrut(12));

            add(pad(navButton("Dashboard",
                    e -> JOptionPane.showMessageDialog(this,
                            "Dashboard navigation not wired in this sample.")),
                    0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(navButton("Enter Scores", e ->
                    JOptionPane.showMessageDialog(this,
                            "Hook this to EnterScoresWindow if desired.")),
                    0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(navButton("Compute Grades", e ->
                    JOptionPane.showMessageDialog(this,
                            "You are already on Compute Grades.")),
                    0, 16, 0, 16));
            add(Box.createVerticalStrut(10));

            add(pad(navButton("Reports",
                    e -> JOptionPane.showMessageDialog(this,
                            "Reports window not wired in this sample.")),
                    0, 16, 0, 16));

            add(Box.createVerticalGlue());
        }

        private JPanel profileCard() {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
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
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createVerticalStrut(6));
            card.add(avatar);
            card.add(Box.createVerticalStrut(8));
            card.add(name);

            return card;
        }

        private JButton navButton(String text, ActionListener al) {
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

        static class Avatar extends JComponent {
            private final String initial;
            Avatar(String i) { initial = i; setPreferredSize(new Dimension(120, 120)); }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
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

    /* ================= FOOTER ================= */
    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 56)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            String[] items = { "© 2024 IIT Delhi", "Privacy Policy", "Terms & Service" };
            int x = 28, y = getHeight() / 2 + 6;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + 28;
            }
            g2.dispose();
        }
    }
}
