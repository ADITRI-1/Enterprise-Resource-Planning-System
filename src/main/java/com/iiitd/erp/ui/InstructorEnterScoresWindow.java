package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.InstructorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class InstructorEnterScoresWindow extends JFrame {

    private final InstructorClient client;
    private final boolean isMaintenanceMode;
    private JComboBox<String> courseCombo;
    private JComboBox<String> componentCombo;
    private JComboBox<Integer> maxCombo;

    private final String[] courses = {
            "CSE231 - Operating Systems",
            "CSE101 - Basic Programming",
            "MTH207 - Linear Algebra"
    };

    private final Map<String, Integer[]> componentMaxMap = new HashMap<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel tableCards = new JPanel(cardLayout);
    private final Map<String, ScoresTableModel> courseModels = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorEnterScoresWindow(null, false).setVisible(true));
    }

    public InstructorEnterScoresWindow(InstructorClient client, boolean maintenanceMode) {
        this.client = client;
        this.isMaintenanceMode = maintenanceMode;
        setTitle("Enter Scores");
        setSize(1250, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        componentMaxMap.put("Quiz",        new Integer[]{10, 20});
        componentMaxMap.put("Assignment",  new Integer[]{20, 25});
        componentMaxMap.put("Project",     new Integer[]{30});
        componentMaxMap.put("Midsem",      new Integer[]{30, 40});
        componentMaxMap.put("Endsem",      new Integer[]{60, 70});

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        main.add(new SlidingPanel(new LeftRail(client)), BorderLayout.WEST);
        main.add(buildContent(), BorderLayout.CENTER);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(18, 24, 6, 24));

        JLabel title = new JLabel("ENTER SCORES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(new Color(31, 45, 61));
        titleRow.add(title, BorderLayout.WEST);

        JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        coursePanel.setOpaque(false);
        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        courseCombo = new JComboBox<>(courses);
        courseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        coursePanel.add(courseLbl);
        coursePanel.add(courseCombo);
        titleRow.add(coursePanel, BorderLayout.CENTER);

        root.add(titleRow, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(4, 24, 4, 24));

        JLabel compLbl = new JLabel("Component:");
        compLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        componentCombo = new JComboBox<>(componentMaxMap.keySet().toArray(new String[0]));
        componentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel maxLbl = new JLabel("Max Marks:");
        maxLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        maxCombo = new JComboBox<>();
        maxCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        filterBar.add(compLbl);
        filterBar.add(componentCombo);
        filterBar.add(Box.createHorizontalStrut(24));
        filterBar.add(maxLbl);
        filterBar.add(maxCombo);

        tableCards.setOpaque(false);
        tableCards.setBorder(new EmptyBorder(4, 24, 24, 24));

        for (String c : courses) {
            ScoresTableModel model = new ScoresTableModel(studentsForCourse(c), isMaintenanceMode);
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
            table.getTableHeader().setReorderingAllowed(false);
            table.setFillsViewportHeight(true);

            JScrollPane sp = new JScrollPane(table);
            sp.getViewport().setBackground(Color.WHITE);

            tableCards.add(sp, c);
            courseModels.put(c, model);
        }

        updateMaxMarksCombo();
        componentCombo.addActionListener(e -> {
            updateMaxMarksCombo();
            applyComponentAndMaxToCurrentModel();
        });
        maxCombo.addActionListener(e -> applyComponentAndMaxToCurrentModel());

        courseCombo.addActionListener(e -> {
            String sel = (String) courseCombo.getSelectedItem();
            if (sel != null) {
                cardLayout.show(tableCards, sel);
                applyComponentAndMaxToCurrentModel();
            }
        });

        if (courses.length > 0) cardLayout.show(tableCards, courses[0]);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(filterBar, BorderLayout.NORTH);
        center.add(tableCards, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private void updateMaxMarksCombo() {
        String comp = (String) componentCombo.getSelectedItem();
        maxCombo.removeAllItems();
        if (comp != null) {
            Integer[] vals = componentMaxMap.get(comp);
            if (vals != null) {
                for (Integer v : vals) maxCombo.addItem(v);
            }
        }
        if (maxCombo.getItemCount() > 0) maxCombo.setSelectedIndex(0);
    }

    private void applyComponentAndMaxToCurrentModel() {
        String course = (String) courseCombo.getSelectedItem();
        String comp   = (String) componentCombo.getSelectedItem();
        Integer max   = (Integer) maxCombo.getSelectedItem();
        if (course == null || comp == null || max == null) return;
        ScoresTableModel model = courseModels.get(course);
        if (model != null) {
            model.setComponentAndMax(comp, max);
        }
    }

    private Object[][] studentsForCourse(String course) {
        if (course.startsWith("CSE231")) {
            return new Object[][] {
                    {"2024CS20101", "Rahul Sharma"},
                    {"2024CS20102", "Ananya Gupta"},
                    {"2024CS20103", "Arjun Mehta"},
                    {"2024CS20104", "Isha Verma"}
            };
        } else if (course.startsWith("CSE101")) {
            return new Object[][] {
                    {"2024CS10121", "Devansh Jain"},
                    {"2024CS10122", "Muskan Arora"},
                    {"2024CS10123", "Sanjana Rao"}
            };
        } else { // MTH207
            return new Object[][] {
                    {"2024MT20111", "Lakshay Singh"},
                    {"2024MT20112", "Priya Nair"},
                    {"2024MT20113", "Rohan Das"},
                    {"2024MT20114", "Meera Joshi"}
            };
        }
    }

    static class ScoresTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Student ID", "Student Name", "Component", "Max Marks", "Marks"
        };
        private final Object[][] data;
        private final boolean isMaintenanceMode;

        ScoresTableModel(Object[][] students, boolean maintenanceMode) {
            this.isMaintenanceMode = maintenanceMode;
            data = new Object[students.length][5];
            for (int i = 0; i < students.length; i++) {
                data[i][0] = students[i][0];
                data[i][1] = students[i][1];
                data[i][2] = "";
                data[i][3] = "";
                data[i][4] = "";
            }
        }

        @Override public int getRowCount() { return data.length; }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            data[row][col] = aValue;
            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 4 && !isMaintenanceMode;
        }

        public void setComponentAndMax(String component, int maxMarks) {
            for (int r = 0; r < data.length; r++) {
                data[r][2] = component;
                data[r][3] = maxMarks;
            }
            if (data.length > 0) {
                fireTableRowsUpdated(0, data.length - 1);
            }
        }
    }

    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 92)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(9, 53, 82));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cy = getHeight() / 2;
            int left = 26;

            g2.setColor(Color.WHITE);
            int barW = 10, barH = 44, gap = 10, x = left, barY = cy - barH / 2;
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
            g2.drawString("DELHI", textX + g2.getFontMetrics().stringWidth(line2) + 10, cy + 20);
        }
    }

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

        @Override
        public void doLayout() {
            int h = getParent() != null ? getParent().getHeight() : getHeight();
            arrow.setBounds(0, 0, COL, h);
            if (expanded) content.setBounds(0, 0, EXP, h);
            else          content.setBounds(COL, 0, EXP, h);
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
                    g2.fillPolygon(new int[]{20, 10, 20}, new int[]{mid - 10, mid, mid + 10}, 3);
                else
                    g2.fillPolygon(new int[]{10, 20, 10}, new int[]{mid - 10, mid, mid + 10}, 3);
            }
        }
    }

    static class LeftRail extends JPanel {
        private final InstructorClient client;
        LeftRail(InstructorClient client) {
            this.client = client;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalStrut(18));
            add(pad(studentCard(), 16, 16, 10, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("Course Sections", e -> new InstructorCourseSectionsWindow(client).setVisible(true)), 0, 16, 0, 16));
            add(Box.createVerticalStrut(10));
            add(pad(navButton("Enter Scores", e -> new InstructorEnterScoresWindow(client, false).setVisible(true)), 0, 16, 0, 16));
            add(Box.createVerticalGlue());
        }

        private static JPanel studentCard() {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(16, 0, 16, 0));

            JComponent avatar = new JComponent() {
                @Override protected void paintComponent(Graphics g) {
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
                    String s = "I";
                    g2.drawString(s, x + d / 2 - fm.stringWidth(s) / 2,
                            y + d / 2 + fm.getAscent() / 4);
                }

                @Override public Dimension getPreferredSize() { return new Dimension(120, 120); }
            };
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

        private static JButton navButton(String text, ActionListener al) {
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
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }

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
        }
    }
}
