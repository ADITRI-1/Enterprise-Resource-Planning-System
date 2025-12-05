package com.iiitd.erp.ui;//import university_erp.CreateUserWindow;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * AdminDashboardExact
 * - Recreates the reference UI and draws the IIITD logo precisely in vector form.
 * - Now includes a sliding-left-panel behavior on hover.
 */
public class AdminDashboardExact extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Crisp text on Windows/Linux
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception ignored) {}
            new AdminDashboardExact().setVisible(true);
        });
    }

    public AdminDashboardExact() {
        setTitle("IIIT ERP — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // ✅ Wrapped LeftRail inside SlidingPanel
        SlidingPanel slidingLeft = new SlidingPanel(new LeftRail());
        main.add(slidingLeft, BorderLayout.WEST);

        main.add(new ContentArea(), BorderLayout.CENTER);
    }

    // ---------------- SLIDING PANEL WRAPPER ----------------
    // ---------------- SLIDING PANEL (FIXED VERSION) ----------------
// ---------------- SLIDING PANEL WITH ARROW + FULL HEIGHT FIX ----------------
static class SlidingPanel extends JPanel {

    private final JPanel content;
    private final ArrowIndicator arrow;
    private final int expandedWidth = 320;
    private final int collapsedWidth = 36;

    private boolean expanded = false;
    private Timer animator;

    SlidingPanel(JPanel inner) {
        this.content = inner;
        setLayout(null);                  // Needed for sliding animation
        setPreferredSize(new Dimension(collapsedWidth, 0));
        setOpaque(false);

        // -------- ARROW INDICATOR (always visible) --------
        arrow = new ArrowIndicator();
        arrow.setBounds(0, 0, collapsedWidth, 60);
        add(arrow);

        // -------- CONTENT PANEL (LeftRail) --------
        inner.setOpaque(false);
        add(inner);

        // Hover to expand
        arrow.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { expand(); }
        });

        // Collapse when mouse leaves full panel
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { collapseWhenOutside(); }
        });

        inner.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { collapseWhenOutside(); }
        });
    }

    @Override
    public void doLayout() {
        int h = getParent().getHeight();    // ✅ correctly follow screen height

        arrow.setBounds(0, 0, collapsedWidth, h);

        if (expanded) {
            content.setBounds(0, 0, expandedWidth, h);
        } else {
            content.setBounds(collapsedWidth, 0, expandedWidth, h);
        }
    }

    private void expand() {
        if (expanded) return;
        expanded = true;
        arrow.setExpanded(true);
        animateWidth(getWidth(), expandedWidth);
    }

    private void collapseWhenOutside() {
        Timer t = new Timer(120, ev -> {
            Point p = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(p, this);

            if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) {
                collapse();
            }
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

    // ---------- Arrow indicator (rotates when expanded) ----------
    static class ArrowIndicator extends JPanel {

        private boolean expanded = false;

        ArrowIndicator() {
            setOpaque(false);
        }

        void setExpanded(boolean b) {
            expanded = b;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background strip
            g2.setColor(new Color(230, 233, 240));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // arrow triangle
            g2.setColor(new Color(70, 80, 95));
            int mid = getHeight() / 2;

            int[] x, y;

            if (expanded) {
                // pointing left (collapse)
                x = new int[]{20, 10, 20};
                y = new int[]{mid - 10, mid, mid + 10};
            } else {
                // pointing right (expand)
                x = new int[]{10, 20, 10};
                y = new int[]{mid - 10, mid, mid + 10};
            }

            g2.fillPolygon(x, y, 3);
        }
    }
}



    // ---------------- HEADER BAR ----------------
    static class HeaderBar extends JPanel {
        private final Color NAVY = new Color(9, 53, 82);
        private final Color TEAL = new Color(85, 172, 168);
        private final Color WHITE = Color.WHITE;

        HeaderBar() { setPreferredSize(new Dimension(0, 64)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(NAVY);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int left = 24;
            int cy = getHeight() / 2;

            g2.setColor(WHITE);
            int barW = 8, barH = 38, barR = 4, gap = 8;
            int x = left;
            int barY = cy - barH / 2;

            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, barY, barW, barH, barR, barR);
                x += barW + gap;
            }

            int dOuter = 36;
            int dX = x - 15;
            int dY = cy - dOuter / 2;

            g2.setColor(TEAL);
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);

            g2.setColor(new Color(255, 255, 255, 170));
            int sepX = dX + dOuter + 16;
            g2.fillRect(sepX, 12, 2, getHeight() - 24);

            g2.setColor(WHITE);
            Font base = new Font("Segoe UI", Font.BOLD, 16);
            g2.setFont(base);
            int textX = sepX + 16;
            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 4);

            String line2 = "INFORMATION TECHNOLOGY";
            int y2 = cy + 18;
            g2.drawString(line2, textX, y2);

            FontMetrics fm = g2.getFontMetrics(base);
            int delhiX = textX + fm.stringWidth(line2) + 8;
            g2.setColor(TEAL);
            g2.drawString("DELHI", delhiX, y2);

            g2.dispose();
        }
    }

    // ---------------- LEFT RAIL ----------------
    static class LeftRail extends JPanel {
        LeftRail() {
            setPreferredSize(new Dimension(320, 0));
            setBackground(new Color(245, 248, 251));
            setLayout(new BorderLayout());

            JPanel profileCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            profileCard.setOpaque(false);
            profileCard.setBorder(new EmptyBorder(20, 0, 16, 0));
            profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));

            Avatar avatar = new Avatar("A");
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel adminLabel = label("Admin", 18, Font.BOLD, new Color(46, 56, 66));
            adminLabel.setBorder(new EmptyBorder(12, 0, 4, 0));
            adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            profileCard.add(Box.createVerticalStrut(4));
            profileCard.add(centerWrap(avatar));
            profileCard.add(adminLabel);

            add(margin(profileCard, 16, 16, 0, 16), BorderLayout.NORTH);

            JPanel nav = new JPanel();
            nav.setOpaque(false);
            nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
            nav.add(navButton("Create User", '\uE77B'));
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("Create Courses", '\uE8F1'));
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("Create Sections", '\uE8FD'));

            add(margin(cardWrap(nav), 16, 16, 16, 16), BorderLayout.CENTER);
        }

        private JPanel navButton(String text, char icon) {
            Color border = new Color(220, 225, 230);
            Color bg = Color.WHITE;

            JPanel p = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(260, 64));
            p.setBorder(new EmptyBorder(10, 16, 10, 16));

            JLabel ic = new JLabel(String.valueOf(icon));
            ic.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
            ic.setForeground(new Color(60, 72, 88));

            JLabel t = new JLabel(text);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            t.setForeground(new Color(46, 56, 66));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
            left.add(ic);
            left.add(Box.createHorizontalStrut(14));
            left.add(t);

            p.add(left, BorderLayout.WEST);

            p.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    p.setBackground(new Color(245, 248, 255));
                }
                @Override public void mouseExited(MouseEvent e) {
                    p.setCursor(Cursor.getDefaultCursor());
                    p.setBackground(bg);
                }
                @Override public void mouseClicked(MouseEvent e) {

    switch (text) {

        case "Create User":
            new CreateUserWindow().setVisible(true);
            break;

        case "Create Courses":
            new CreateCoursesWindow().setVisible(true);
            break;

        case "Create Sections":
            new CreateSectionWindow().setVisible(true);
            break;
    }
}
            });

            return p;
        }

        private static JPanel centerWrap(JComponent c) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.add(c);
            return p;
        }

        private static JPanel margin(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }

        private static JPanel cardWrap(JComponent inner) {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);
            outer.add(inner, BorderLayout.CENTER);
            return outer;
        }

        private static JLabel label(String s, int sz, int st, Color col) {
            JLabel l = new JLabel(s);
            l.setFont(new Font("Segoe UI", st, sz));
            l.setForeground(col);
            return l;
        }

        static class Avatar extends JComponent {
            private final String initial;

            Avatar(String initial) {
                this.initial = initial;
                setPreferredSize(new Dimension(120, 120));
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = Math.min(getWidth(), getHeight());
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;

                g2.setColor(new Color(245, 248, 251));
                g2.fillOval(x, y, d, d);

                g2.setStroke(new BasicStroke(6f));
                g2.setColor(new Color(230, 234, 239));
                g2.drawOval(x + 3, y + 3, d - 6, d - 6);

                g2.setColor(new Color(46, 56, 66));
                Font f = new Font("Segoe UI", Font.BOLD, d / 2);
                g2.setFont(f);

                FontMetrics fm = g2.getFontMetrics();
                int tx = getWidth() / 2 - fm.stringWidth(initial) / 2;
                int ty = getHeight() / 2 + fm.getAscent() / 2 - 6;

                g2.drawString(initial, tx, ty);
            }
        }
    }

    // ---------------- CENTER CONTENT ----------------
    static class ContentArea extends JPanel {
        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("ADMIN DASHBOARD");
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(new Color(31, 45, 61));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel crumb = new JLabel("Home / Dashboard");
            crumb.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            crumb.setForeground(new Color(90, 104, 120));
            crumb.setBorder(new EmptyBorder(8, 2, 12, 0));
            crumb.setAlignmentX(Component.LEFT_ALIGNMENT);

            top.add(margin(title, 20, 18, 0, 18));
            top.add(margin(crumb, 0, 18, 0, 18));

            add(top, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1; gbc.weighty = 1;

            gbc.gridx = 0; gbc.gridy = 0;
            grid.add(cardCreateUser(), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            grid.add(cardCreateCourses(), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            grid.add(cardCreateSections(), gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            grid.add(cardMaintenance(), gbc);

            add(margin(grid, 0, 8, 0, 16), BorderLayout.CENTER);
        }

        private static JPanel cardCreateUser() {
            return card("CREATE USER", "Enroll new students or staff", "Create Account",
                    e -> new CreateUserWindow().setVisible(true));
        }

        private static JPanel cardCreateCourses() {
    return card("CREATE COURSES", "Define your offerings and sections", "Manage Courses",
        e -> new CreateCoursesWindow().setVisible(true));
}

private static JPanel cardCreateSections() {
    return card("CREATE SECTIONS", "Organize course instances", "Refine Sections",
        e -> new CreateSectionWindow().setVisible(true));
}

        private static JPanel cardMaintenance() {
            JPanel card = baseCard();
            card.setLayout(new BorderLayout());
            card.add(cardTexts("MAINTENANCE MODE", "Toggle system availability"), BorderLayout.NORTH);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
            row.setOpaque(false);

            JToggleButton toggle = new IOSSwitch();
            JLabel label = new JLabel("Enabled/Enabled");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(new Color(60, 72, 88));

            row.add(toggle);
            row.add(label);

            card.add(margin(row, 8, 20, 20, 20), BorderLayout.CENTER);
            return card;
        }

        private static JPanel card(String title, String subtitle, String cta, ActionListener al) {
            JPanel card = baseCard();
            card.setLayout(new BorderLayout());
            card.add(cardTexts(title, subtitle), BorderLayout.NORTH);

            JButton btn = primaryButton(cta);
            btn.addActionListener(al);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            wrapper.setOpaque(false);
            wrapper.add(btn);

            card.add(margin(wrapper, 4, 0, 12, 0), BorderLayout.CENTER);
            return card;
        }

        private static JPanel baseCard() {
            return new ShadowCard();
        }

        private static JPanel cardTexts(String title, String subtitle) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            t.setForeground(new Color(31, 45, 61));

            JLabel s = new JLabel(subtitle);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            s.setForeground(new Color(101, 113, 126));

            p.add(margin(t, 20, 20, 0, 20));
            p.add(margin(s, 6, 20, 6, 20));

            return p;
        }

        private static JButton primaryButton(String text) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 18));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(43, 123, 196));
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(12, 18, 12, 18));
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

        private static JPanel margin(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }

    // ---------------- SHADOWED CARD ----------------
    static class ShadowCard extends JPanel {
        ShadowCard() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 16, 16));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 28));
            g2.fillRoundRect(10, 10, getWidth() - 26, getHeight() - 26, 18, 18);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(520, 240); }
    }

    // ---------------- IOS SWITCH ----------------
    static class IOSSwitch extends JToggleButton {
        IOSSwitch() {
            setPreferredSize(new Dimension(88, 32));
            setFocusable(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setModel(new ToggleButtonModel());
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int r = h;

            g2.setColor(isSelected() ? new Color(48, 141, 219) : new Color(206, 212, 218));
            g2.fillRoundRect(0, 0, w, h, r, r);

            int pad = 3;
            int d = h - pad * 2;
            int x = isSelected() ? w - d - pad : pad;

            g2.setColor(Color.WHITE);
            g2.fillOval(x, pad, d, d);

            g2.dispose();
        }
    }

    // ---------------- FOOTER BAR ----------------
    static class FooterBar extends JPanel {
        private final Color NAVY = new Color(9, 53, 82);

        FooterBar() { setPreferredSize(new Dimension(0, 54)); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setColor(NAVY);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            String[] items = { "© 2024 IIT Delhi", "Privacy Policy", "Terms & Service" };
            int gap = 28;
            int y = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 4;

            int x = 30;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + gap;
            }

            g2.dispose();
        }
    }
}
