package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.StudentResponse;
import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentDashboardWindow extends JFrame {

    private final static Logger LOGGER = Logger.getLogger(StudentDashboardWindow.class.getName());
    private final StudentClient client;

    public StudentDashboardWindow(StudentClient client) {
        this.client = client;
        LOGGER.info("StudentDashboardWindow initialized");
        setTitle("Student Dashboard");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        main.add(new SlidingPanel(new LeftRail(client)), BorderLayout.WEST);
        main.add(new DashboardArea(), BorderLayout.CENTER);

        setVisible(true);
    }

    /* ---------------------------------------------------------------------- */
    /* HEADER (With IIIT-D logo, same as other windows)                       */
    /* ---------------------------------------------------------------------- */

    static class HeaderBar extends JPanel {
        HeaderBar() {
            setPreferredSize(new Dimension(0, 90));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82)); // blue bar
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight() / 2;
            int x = 26;

            // IIITD Logo bars
            g2.setColor(Color.WHITE);
            for (int i = 0; i < 4; i++) {
                g2.fillRoundRect(x, cy - 22, 10, 44, 6, 6);
                x += 20;
            }

            // Half arc
            g2.setColor(new Color(85, 172, 168));
            g2.setStroke(new BasicStroke(8f));
            g2.drawArc(x - 20, cy - 21, 42, 42, -90, 180);

            // Institute text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("INDRAPRASTHA INSTITUTE of", x + 50, cy - 6);

            String line2 = "INFORMATION TECHNOLOGY";
            g2.drawString(line2, x + 50, cy + 20);

            g2.setColor(new Color(85, 172, 168));
            g2.drawString("DELHI",
                    x + 60 + g2.getFontMetrics().stringWidth(line2),
                    cy + 20);
        }
    }

    /* ---------------------------------------------------------------------- */
    /* SLIDING PANEL                                                          */
    /* ---------------------------------------------------------------------- */

    static class SlidingPanel extends JPanel {

        private final JPanel content;
        private final Arrow arrow;
        private final int FULL = 300, COLLAPSED = 38;
        private boolean expanded = false;
        private Timer animator;

        SlidingPanel(JPanel p) {

            setLayout(null);
            setOpaque(false);
            setPreferredSize(new Dimension(COLLAPSED, 0));

            content = p;
            content.setOpaque(false);

            arrow = new Arrow();

            add(arrow);
            add(content);

            MouseAdapter hover = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
                @Override public void mouseExited(MouseEvent e) { collapseDelay(); }
            };

            arrow.addMouseListener(hover);
            addMouseListener(hover);
            content.addMouseListener(hover);
        }

        private void collapseDelay() {
            Timer t = new Timer(150, e -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, this);

                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight())
                    collapse();
            });
            t.setRepeats(false);
            t.start();
        }

        private void expand() {
            if (expanded) return;
            expanded = true;
            arrow.expanded = true;
            animate(COLLAPSED, FULL);
        }

        private void collapse() {
            if (!expanded) return;
            expanded = false;
            arrow.expanded = false;
            animate(getWidth(), COLLAPSED);
        }

        private void animate(int start, int end) {
            if (animator != null && animator.isRunning()) animator.stop();

            animator = new Timer(6, null);

            animator.addActionListener(e -> {
                int current = getWidth();
                int step = (end > start) ? 12 : -12;
                int next = current + step;

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

        @Override
        public void doLayout() {
            int h = getParent().getHeight();
            arrow.setBounds(0, 0, COLLAPSED, h);
            if (expanded)
                content.setBounds(0, 0, FULL, h);
            else
                content.setBounds(COLLAPSED, 0, FULL, h);
        }

        static class Arrow extends JPanel {

            boolean expanded = false;

            Arrow() { setOpaque(false); }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(70, 80, 95));
                int mid = getHeight() / 2;

                if (expanded)
                    g2.fillPolygon(new int[]{20,10,20}, new int[]{mid-10,mid,mid+10}, 3);
                else
                    g2.fillPolygon(new int[]{10,20,10}, new int[]{mid-10,mid,mid+10}, 3);
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /* LEFT RAIL WITH AVATAR + STUDENT RIBBON + NAV BUTTONS                   */
    /* ---------------------------------------------------------------------- */

    class LeftRail extends JPanel {
        private JLabel studentNameLabel;
        private JLabel studentIdLabel;
        private final StudentClient client;

        LeftRail(StudentClient client) {
            this.client = client;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(20));

            // ⇩ NEW Circular S Avatar
            add(center(new Avatar("S")));
            add(Box.createVerticalStrut(10));

            // ⇩ Student Ribbon
            add(center(studentRibbon()));
            add(Box.createVerticalStrut(25));

            nav("My Courses", e -> {
                LOGGER.info("Navigating to MyCoursesWindow");
                new MyCoursesWindow(client).setVisible(true);
            });
            nav("Register Courses", e -> {
                LOGGER.info("Navigating to RegisterCoursesWindow");
                new RegisterCoursesWindow(client).setVisible(true);
            });
            nav("My Timetable", e -> {
                LOGGER.info("Navigating to MyTimetableWindow");
                new MyTimetableWindow(client).setVisible(true);
            });
            nav("Grades", e -> {
                LOGGER.info("Navigating to ViewGradesWindow");
                new ViewGradesWindow(client).setVisible(true);
            });

            add(Box.createVerticalGlue());
            loadStudentInfo();
        }

        private void loadStudentInfo() {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                StudentResponse student;
                @Override protected Void doInBackground() {
                    try {
                        LOGGER.info("Loading student info");
                        student = client.getMyInfo();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to load student info", e);
                    }
                    return null;
                }
                @Override protected void done() {
                    if (student != null) {
                        studentNameLabel.setText(student.name());
                        studentIdLabel.setText("ID: " + student.rollNo());
                    }
                }
            };
            w.execute();
        }

        private JPanel center(JComponent c) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.add(c);
            return p;
        }

        private JPanel studentRibbon() {
            JPanel p = new JPanel();
            p.setBackground(new Color(235, 239, 246));
            p.setBorder(new EmptyBorder(8, 18, 8, 18));
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

            studentNameLabel = new JLabel("Student Name");
            studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

            studentIdLabel = new JLabel("ID: STU12345");
            studentIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            p.add(studentNameLabel);
            p.add(studentIdLabel);

            return p;
        }

        private void nav(String text, ActionListener al) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            b.setBackground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(223,228,234)),
                    new EmptyBorder(10,14,10,14)
            ));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(al);

            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(0, 20, 12, 20));
            p.add(b);

            add(p);
        }

        static class Avatar extends JComponent {
            private final String initial;

            Avatar(String i) {
                initial = i;
                setPreferredSize(new Dimension(120,120));
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(245,248,251));
                g2.fillOval(10,10,100,100);

                g2.setColor(new Color(225,231,238));
                g2.setStroke(new BasicStroke(6f));
                g2.drawOval(10,10,100,100);

                g2.setColor(new Color(46,56,66));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(initial,
                        10 + 50 - fm.stringWidth(initial)/2,
                        10 + 50 + fm.getAscent()/4);
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /* DASHBOARD CENTER AREA (Blue cards instead of white)                    */
    /* ---------------------------------------------------------------------- */

    class DashboardArea extends JPanel {

        DashboardArea() {

            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("STUDENT DASHBOARD");
            title.setFont(new Font("Segoe UI", Font.BOLD, 34));
            title.setForeground(new Color(31, 45, 61));
            title.setBorder(new EmptyBorder(30, 0, 20, 0));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(title);

            JPanel row1 = new JPanel();
            row1.setOpaque(false);
            row1.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));

            row1.add(card("MY COURSES", "View registered courses",
                    () -> {
                        LOGGER.info("Navigating to MyCoursesWindow");
                        new MyCoursesWindow(client).setVisible(true);
                    }));

            row1.add(card("REGISTER COURSES", "Browse and add courses",
                    () -> {
                        LOGGER.info("Navigating to RegisterCoursesWindow");
                        new RegisterCoursesWindow(client).setVisible(true);
                    }));

            JPanel row2 = new JPanel();
            row2.setOpaque(false);
            row2.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));

            row2.add(card("MY TIMETABLE", "View weekly schedule",
                    () -> {
                        LOGGER.info("Navigating to MyTimetableWindow");
                        new MyTimetableWindow(client).setVisible(true);
                    }));

            row2.add(card("GRADES", "Check your performance",
                    () -> {
                        LOGGER.info("Navigating to ViewGradesWindow");
                        new ViewGradesWindow(client).setVisible(true);
                    }));

            add(row1);
            add(row2);
        }

        private JPanel card(String title, String desc, Runnable onClick) {

            Color cardBlue = new Color(72, 121, 231);   // EXACT blue shade

            JPanel c = new JPanel();
            c.setBackground(Color.WHITE);
            c.setPreferredSize(new Dimension(340, 160));
            c.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 242), 2));
            c.setLayout(new BorderLayout());
            c.setOpaque(true);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(18, 18, 18, 18));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 20));

            JLabel d = new JLabel(desc);
            d.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            JButton b = new JButton("Open");
            b.setBackground(cardBlue);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> onClick.run());

            b.setBorder(new EmptyBorder(8, 12, 8, 12));

            body.add(t);
            body.add(Box.createVerticalStrut(8));
            body.add(d);
            body.add(Box.createVerticalGlue());
            body.add(b);

            c.add(body);

            return c;
        }
    }

    /* ---------------------------------------------------------------------- */
    /* FOOTER                                                                 */
    /* ---------------------------------------------------------------------- */

    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 60)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 17));

            String[] items = { "© 2024 IIIT Delhi", "Privacy Policy", "Terms & Service" };
            int x = 40, y = getHeight() / 2 + 6;

            for (String s : items) {
                g2.drawString(s, x, y);
                x += g2.getFontMetrics().stringWidth(s) + 40;
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDashboardWindow(null).setVisible(true));
    }
}
