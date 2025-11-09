import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class StudentAccountWindow extends JFrame {

    public StudentAccountWindow() {
        setTitle("Create Student Account");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        // Sliding left panel
        SlidingPanel sliding = new SlidingPanel(new LeftRail());
        main.add(sliding, BorderLayout.WEST);

        // Center content
        main.add(new ContentArea(), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------------------------
    // HEADER BAR (same as other windows)
    // ------------------------------------------------------------------------------------
    static class HeaderBar extends JPanel {
        private final Color NAVY = new Color(9, 53, 82);
        private final Color TEAL = new Color(85, 172, 168);
        private final Color WHITE = Color.WHITE;

        HeaderBar() {
            setPreferredSize(new Dimension(0, 70));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
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
            g2.setStroke(new BasicStroke(8f));
            g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);

            int textX = dX + dOuter + 26;
            g2.setColor(WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));

            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 6);

            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, textX, cy + 20);

            int delhiX = textX + g2.getFontMetrics().stringWidth(line2) + 8;
            g2.setColor(TEAL);
            g2.drawString("DELHI", delhiX, cy + 20);
        }
    }

    // ------------------------------------------------------------------------------------
    // SLIDING PANEL (same behavior as Admin dashboard)
    // ------------------------------------------------------------------------------------
    static class SlidingPanel extends JPanel {
        private final JPanel content;
        private final Arrow arrow;
        private final int expandedWidth = 310;
        private final int collapsedWidth = 36;

        private boolean expanded = false;
        private Timer animator;

        SlidingPanel(JPanel inner) {
            this.content = inner;
            setLayout(null);
            setPreferredSize(new Dimension(collapsedWidth, 0));
            setOpaque(false);

            arrow = new Arrow();
            add(arrow);

            inner.setOpaque(false);
            add(inner);

            arrow.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
            });

            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { collapseWhenOutside(); }
            });

            inner.addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { collapseWhenOutside(); }
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
            arrow.expanded = true;
            animate(getWidth(), expandedWidth);
        }

        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.expanded = false;
            animate(getWidth(), collapsedWidth);
        }

        private void collapseWhenOutside() {
            Timer t = new Timer(120, e -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, this);
                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight())
                    collapse();
            });
            t.setRepeats(false);
            t.start();
        }

        private void animate(int start, int end) {
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

        static class Arrow extends JPanel {
            boolean expanded = false;

            Arrow() { setOpaque(false); }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(80, 90, 105));
                int mid = getHeight() / 2;

                int[] x, y;
                if (!expanded) {
                    x = new int[]{10, 20, 10};
                    y = new int[]{mid - 10, mid, mid + 10};
                } else {
                    x = new int[]{20, 10, 20};
                    y = new int[]{mid - 10, mid, mid + 10};
                }
                g2.fillPolygon(x, y, 3);
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // LEFT RAIL (with clickable buttons + avatar)
    // ------------------------------------------------------------------------------------
    static class LeftRail extends JPanel {

        LeftRail() {
            setPreferredSize(new Dimension(310, 0));
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
            profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
            profileCard.setBorder(new EmptyBorder(25, 0, 10, 0));

            // Clean admin avatar circle
            AvatarCircle avatar = new AvatarCircle("A");
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel admin = new JLabel("Admin");
            admin.setFont(new Font("Segoe UI", Font.BOLD, 20));
            admin.setForeground(new Color(40, 50, 65));
            admin.setAlignmentX(Component.CENTER_ALIGNMENT);
            admin.setBorder(new EmptyBorder(12, 0, 0, 0));

            profileCard.add(avatar);
            profileCard.add(admin);

            add(center(profileCard), BorderLayout.NORTH);

            // Buttons
            JPanel nav = new JPanel();
            nav.setOpaque(false);
            nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

            nav.add(navButton("Create User", '\uE77B'));
            nav.add(Box.createVerticalStrut(20));
            nav.add(navButton("Create Courses", '\uE8F1'));
            nav.add(Box.createVerticalStrut(20));
            nav.add(navButton("Create Sections", '\uE8FD'));

            add(margin(nav, 10, 20, 10, 20), BorderLayout.CENTER);
        }

        private JPanel navButton(String text, char icon) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(240, 55));
            p.setBorder(new EmptyBorder(10, 15, 10, 15));

            JLabel ic = new JLabel(String.valueOf(icon));
            ic.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
            ic.setForeground(new Color(50, 60, 80));

            JLabel t = new JLabel(text);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            t.setForeground(new Color(40, 50, 65));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
            left.add(ic);
            left.add(Box.createHorizontalStrut(12));
            left.add(t);

            p.add(left, BorderLayout.WEST);

            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    p.setBackground(new Color(230, 239, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    p.setBackground(new Color(245, 248, 251));
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(null, text + " clicked");
                }
            });

            return p;
        }

        private JPanel center(JComponent c) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.add(c);
            return p;
        }

        private JPanel margin(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }

        static class AvatarCircle extends JComponent {
            private final String initial;

            AvatarCircle(String initial) {
                this.initial = initial;
                setPreferredSize(new Dimension(140, 140));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = 140;
                int x = (getWidth() - d) / 2;
                int y = 0;

                g2.setColor(new Color(245, 248, 251));
                g2.fillOval(x, y, d, d);

                g2.setColor(new Color(225, 230, 240));
                g2.setStroke(new BasicStroke(6f));
                g2.drawOval(x + 3, y + 3, d - 6, d - 6);

                g2.setColor(new Color(40, 50, 65));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 55));
                FontMetrics fm = g2.getFontMetrics();
                int tx = getWidth() / 2 - fm.stringWidth(initial) / 2;
                int ty = y + d / 2 + fm.getAscent() / 2 - 10;
                g2.drawString(initial, tx, ty);
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // CENTER CONTENT (Student Account Form)
    // ------------------------------------------------------------------------------------
    static class ContentArea extends JPanel {
        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel box = new JPanel();
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(new EmptyBorder(40, 100, 40, 100));

            JLabel title = new JLabel("CREATE STUDENT ACCOUNT");
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(new Color(30, 40, 60));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(title);

            box.add(Box.createVerticalStrut(25));

            // Form panel
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            int fieldWidth = 380;   // Reduced width
            int fieldHeight = 36;

            JComboBox<String> role = new JComboBox<>(new String[]{"Faculty", "Student"});
            role.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            JTextField fullName = new JTextField();
            fullName.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            JTextField rollNo = new JTextField();
            rollNo.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            JComboBox<String> program = new JComboBox<>(new String[]{"B.Tech", "M.Tech", "PhD"});
            program.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            JComboBox<String> branch = new JComboBox<>(new String[]{"CSE", "ECE", "CSAM"});
            branch.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            JTextField email = new JTextField();
            email.setPreferredSize(new Dimension(fieldWidth, fieldHeight));

            // Labels + fields
            addRow(form, gbc, 0, "Select Role", role);
            addRow(form, gbc, 1, "Full Name", fullName);
            addRow(form, gbc, 2, "Roll No", rollNo);
            addRow(form, gbc, 3, "Program", program);
            addRow(form, gbc, 4, "Branch", branch);
            addRow(form, gbc, 5, "Official Email", email);

            box.add(form);

            box.add(Box.createVerticalStrut(30));

            JButton createBtn = new JButton("Create Student Account");
            createBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
            createBtn.setForeground(Color.WHITE);
            createBtn.setBackground(new Color(29, 85, 180));
            createBtn.setFocusPainted(false);
            createBtn.setBorder(new EmptyBorder(12, 40, 12, 40));

            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.add(createBtn);

            box.add(btnPanel);

            add(box, BorderLayout.CENTER);
        }

        private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            p.add(new JLabel(label + "  "), gbc);

            gbc.gridx = 1;
            p.add(field, gbc);
        }
    }

    // ------------------------------------------------------------------------------------
    // FOOTER
    // ------------------------------------------------------------------------------------
    static class FooterBar extends JPanel {
        private final Color NAVY = new Color(9, 53, 82);

        FooterBar() { setPreferredSize(new Dimension(0, 54)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(NAVY);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            String[] items = {"© 2024 IIT Delhi", "Privacy Policy", "Terms & Service"};
            int gap = 30;
            int y = getHeight() / 2 + 6;

            int x = 30;
            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + gap;
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // MAIN (for running standalone)
    // ------------------------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentAccountWindow().setVisible(true);
        });
    }
}
