package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.MaintenanceStatusResponse;
import com.iiitd.erp.ui.client.AdminClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboardExact extends JFrame {

    private final AdminClient admin;
    private final static Logger LOGGER = Logger.getLogger(AdminDashboardExact.class.getName());

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);
    private final LeftRail leftRail;
    
    private final Map<String, JPanel> panels = new HashMap<>();
    private JCheckBox maintenanceModeBox;
    private final boolean isMaintenanceMode;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception ignored) {}
            new AdminDashboardExact().setVisible(true);
        });
    }

    public AdminDashboardExact() { this(null, false); }

    public AdminDashboardExact(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        this.leftRail = new LeftRail();
        LOGGER.info("Admin dashboard initialized with new design");

        setTitle("IIIT ERP — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(new FooterBar(), BorderLayout.SOUTH);

        // Set initial view
        cardLayout.show(mainContentPanel, "Dashboard");
        leftRail.setSelected("Dashboard");
        
        if (admin != null) {
            fetchMaintenanceStatus();
        }
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.add(new HeaderBar(), BorderLayout.CENTER);

        maintenanceModeBox = new JCheckBox("Maintenance Mode");
        maintenanceModeBox.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceModeBox.setOpaque(false);
        maintenanceModeBox.addActionListener(e -> toggleMaintenanceMode());
        maintenanceModeBox.setEnabled(!isMaintenanceMode); // Disable if in maintenance mode

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            LOGGER.info("Logout button clicked");
            dispose();
            new LoginUI().setVisible(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        right.setOpaque(false);
        right.add(maintenanceModeBox);
        right.add(logout);

        header.add(right, BorderLayout.EAST);
        header.setPreferredSize(new Dimension(0, 70));
        return header;
    }

    private JComponent buildMain() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 248, 251));

        mainContentPanel.setOpaque(false);
        
        panels.put("Dashboard", new DashboardPanel());
        panels.put("Users", new UsersPanel(admin, isMaintenanceMode));
        panels.put("Courses", new CoursesPanel(admin, isMaintenanceMode));
        panels.put("Programs", new ProgramsPanel(admin, isMaintenanceMode));
        panels.put("Departments", new DepartmentsPanel(admin, isMaintenanceMode));
        panels.put("Rooms", new RoomsPanel(admin, isMaintenanceMode));
        panels.put("Sections", new SectionsPanel(admin, isMaintenanceMode));
        panels.put("Class Schedule", new SchedulePanel(admin, isMaintenanceMode));

        for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
            mainContentPanel.add(entry.getValue(), entry.getKey());
        }

        mainPanel.add(new SlidingPanel(leftRail), BorderLayout.WEST);
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private void showView(String viewName) {
        LOGGER.info("Switching to view: " + viewName);
        
        JPanel panel = panels.get(viewName);
        // The refresh logic can be simplified if each panel handles its own data loading
        // For now, this explicit check remains.
        if (panel instanceof UsersPanel) {
            ((UsersPanel) panel).loadData();
        } else if (panel instanceof CoursesPanel) {
             ((CoursesPanel) panel).loadData();
        } else if (panel instanceof ProgramsPanel) {
             ((ProgramsPanel) panel).loadData();
        } else if (panel instanceof DepartmentsPanel) {
             ((DepartmentsPanel) panel).loadData();
        } else if (panel instanceof RoomsPanel) {
             ((RoomsPanel) panel).loadData();
        } else if (panel instanceof SectionsPanel) {
             ((SectionsPanel) panel).loadData();
        }

        cardLayout.show(mainContentPanel, viewName);
        leftRail.setSelected(viewName);
    }
    
    private void fetchMaintenanceStatus() {
        SwingWorker<MaintenanceStatusResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected MaintenanceStatusResponse doInBackground() throws Exception {
                return admin.maintenanceStatus();
            }
            @Override
            protected void done() {
                try {
                    MaintenanceStatusResponse status = get();
                    maintenanceModeBox.setSelected(status.maintenanceOn());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to fetch maintenance status", e);
                }
            }
        };
        worker.execute();
    }

    private void toggleMaintenanceMode() {
        boolean isEnabled = maintenanceModeBox.isSelected();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                admin.toggleMaintenance(isEnabled);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get(); // To catch any exceptions
                    JOptionPane.showMessageDialog(AdminDashboardExact.this,
                            "Maintenance mode is now " + (isEnabled ? "ON" : "OFF") + ". Please restart the application to see changes.",
                            "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to toggle maintenance mode", e);
                    JOptionPane.showMessageDialog(AdminDashboardExact.this,
                            "Failed to update maintenance status: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    maintenanceModeBox.setSelected(!isEnabled);
                }
            }
        };
        worker.execute();
    }

    // ---------- Inner classes (DashboardPanel, LeftRail, etc.) remain the same ----------

    class DashboardPanel extends JPanel {
        DashboardPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(20, 20, 20, 20);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1; gbc.weighty = 1;

            gbc.gridx = 0; gbc.gridy = 0;
            add(createDashboardCard("Manage Users", "Create, update, and view all users", () -> showView("Users")), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            add(createDashboardCard("Manage Courses", "Add, edit, and manage course catalog", () -> showView("Courses")), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(createDashboardCard("Manage Programs", "Define and structure academic programs", () -> showView("Programs")), gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            add(createDashboardCard("Manage Sections", "Create and assign course sections", () -> showView("Sections")), gbc);
        }

        private JPanel createDashboardCard(String title, String subtitle, Runnable action) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
                    new EmptyBorder(25, 25, 25, 25)
            ));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(new Color(30, 40, 60));

            JLabel subtitleLabel = new JLabel("<html><p style='width:200px'>" + subtitle + "</p></html>");
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            subtitleLabel.setForeground(new Color(100, 110, 125));

            JButton openButton = new JButton("Open");
            openButton.setBackground(new Color(45, 125, 200));
            openButton.setForeground(Color.WHITE);
            openButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            openButton.setFocusPainted(false);
            openButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            openButton.addActionListener(e -> action.run());

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(10));
            textPanel.add(subtitleLabel);

            card.add(textPanel, BorderLayout.CENTER);
            card.add(openButton, BorderLayout.SOUTH);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }
            });
            return card;
        }
    }

    class LeftRail extends JPanel {
        private final Map<String, JPanel> navButtons = new HashMap<>();

        LeftRail() {
            setPreferredSize(new Dimension(320, 0));
            setBackground(new Color(245, 248, 251));
            setLayout(new BorderLayout());

            JPanel profileCard = createProfileCard();
            add(margin(profileCard, 16, 16, 0, 16), BorderLayout.NORTH);

            JPanel navPanel = new JPanel();
            navPanel.setOpaque(false);
            navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

            String[] navItems = {"Dashboard", "Users", "Courses", "Programs", "Departments", "Rooms", "Sections", "Class Schedule"};
            char[] icons = {'\uE80F', '\uE716', '\uE7BE', '\uE71D', '\uE71D', '\uE740', '\uE716', '\uE787'};

            for (int i = 0; i < navItems.length; i++) {
                String item = navItems[i];
                JPanel navButton = createNavButton(item, icons[i], () -> showView(item));
                navButtons.put(item, navButton);
                navPanel.add(navButton);
                navPanel.add(Box.createVerticalStrut(12));
            }

            add(margin(navPanel, 16, 16, 16, 16), BorderLayout.CENTER);
        }

        void setSelected(String itemName) {
            for (Map.Entry<String, JPanel> entry : navButtons.entrySet()) {
                if (entry.getKey().equals(itemName)) {
                    entry.getValue().setBackground(new Color(230, 240, 255));
                    entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(45, 125, 200)),
                        new EmptyBorder(10, 12, 10, 16)
                    ));
                } else {
                    entry.getValue().setBackground(Color.WHITE);
                    entry.getValue().setBorder(new EmptyBorder(10, 16, 10, 16));
                }
            }
        }

        private JPanel createProfileCard() {
            JPanel card = new JPanel();
            card.setOpaque(false);
            card.setBorder(new EmptyBorder(20, 0, 16, 0));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            Avatar avatar = new Avatar("A");
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel adminLabel = new JLabel("Admin");
            adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            adminLabel.setForeground(new Color(40, 50, 65));
            adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            adminLabel.setBorder(new EmptyBorder(12, 0, 4, 0));

            card.add(avatar);
            card.add(adminLabel);
            return card;
        }

        private JPanel createNavButton(String text, char icon, Runnable action) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(true);
            p.setBackground(Color.WHITE);
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.setBorder(new EmptyBorder(10, 16, 10, 16));

            JLabel iconLabel = new JLabel(String.valueOf(icon));
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
            iconLabel.setForeground(new Color(50, 60, 80));

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
            textLabel.setForeground(new Color(50, 60, 80));

            JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            content.setOpaque(false);
            content.add(iconLabel);
            content.add(textLabel);

            p.add(content, BorderLayout.CENTER);
            p.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }
            });
            return p;
        }

        private JPanel margin(JComponent c, int t, int l, int b, int r) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(t, l, b, r));
            p.add(c, BorderLayout.CENTER);
            return p;
        }
    }
    static class HeaderBar extends JPanel { 
        HeaderBar() { setPreferredSize(new Dimension(0, 64)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(9, 53, 82)); g2.fillRect(0, 0, getWidth(), getHeight());
            int left = 24; int cy = getHeight() / 2;
            g2.setColor(Color.WHITE);
            int barW = 8, barH = 38, barR = 4, gap = 8, x = left, barY = cy - barH / 2;
            for (int i = 0; i < 4; i++) { g2.fillRoundRect(x, barY, barW, barH, barR, barR); x += barW + gap; }
            int dOuter = 36, dX = x - 15, dY = cy - dOuter / 2;
            g2.setColor(new Color(85, 172, 168)); g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);
            g2.setColor(new Color(255, 255, 255, 170)); int sepX = dX + dOuter + 16; g2.fillRect(sepX, 12, 2, getHeight() - 24);
            g2.setColor(Color.WHITE); Font base = new Font("Segoe UI", Font.BOLD, 16); g2.setFont(base);
            int textX = sepX + 16; g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 4);
            String line2 = "INFORMATION TECHNOLOGY"; int y2 = cy + 18; g2.drawString(line2, textX, y2);
            FontMetrics fm = g2.getFontMetrics(base); int delhiX = textX + fm.stringWidth(line2) + 8;
            g2.setColor(new Color(85, 172, 168)); g2.drawString("DELHI", delhiX, y2);
            g2.dispose();
        }
    }
    static class FooterBar extends JPanel {
        private final Color NAVY = new Color(9, 53, 82);
        FooterBar() { setPreferredSize(new Dimension(0, 54)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(NAVY); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            String[] items = {"© 2024 IIT Delhi", "Privacy Policy", "Terms & Service"};
            int gap = 30, y = getHeight() / 2 + 6, x = 30;
            for (String s : items) { g2.drawString(s, x, y); x += g2.getFontMetrics().stringWidth(s) + gap; }
        }
    }
    static class SlidingPanel extends JPanel {
        private final JPanel content;
        private final ArrowIndicator arrow;
        private final int expandedWidth = 320;
        private final int collapsedWidth = 36;
        private boolean expanded = false;
        private Timer animator;

        SlidingPanel(JPanel inner) {
            this.content = inner;
            setLayout(null);
            setPreferredSize(new Dimension(collapsedWidth, 0));
            setOpaque(false);

            arrow = new ArrowIndicator();
            add(arrow);

            inner.setOpaque(false);
            add(inner);

            arrow.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { expand(); }
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseExited(java.awt.event.MouseEvent e) { collapseWhenOutside(); }
            });
            inner.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseExited(java.awt.event.MouseEvent e) { collapseWhenOutside(); }
            });
        }

        @Override public void doLayout() {
            int h = getParent().getHeight();
            arrow.setBounds(0, 0, collapsedWidth, h);
            if (expanded) content.setBounds(0, 0, expandedWidth, h);
            else content.setBounds(collapsedWidth, 0, expandedWidth, h);
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
                if (p.x < 0 || p.x > getWidth() || p.y < 0 || p.y > getHeight()) collapse();
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
                if ((step > 0 && next >= end) || (step < 0 && next <= end)) { next = end; animator.stop(); }
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
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(70, 80, 95));
                int mid = getHeight() / 2;
                int[] x, y;
                if (expanded) { x = new int[]{20, 10, 20}; y = new int[]{mid - 10, mid, mid + 10}; }
                else { x = new int[]{10, 20, 10}; y = new int[]{mid - 10, mid, mid + 10}; }
                g2.fillPolygon(x, y, 3);
            }
        }
    }
    static class Avatar extends JComponent {
        private final String letter;
        Avatar(String letter) { this.letter = letter; setPreferredSize(new Dimension(120, 120)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = 120; int x = (getWidth() - d) / 2; int y = 0;
            g2.setColor(new Color(245, 248, 251)); g2.fillOval(x, y, d, d);
            g2.setColor(new Color(225, 230, 240)); g2.setStroke(new BasicStroke(6f)); g2.drawOval(x + 3, y + 3, d - 6, d - 6);
            g2.setColor(new Color(40, 50, 65)); g2.setFont(new Font("Segoe UI", Font.BOLD, 46));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, x + (d - fm.stringWidth(letter)) / 2, y + d / 2 + fm.getAscent() / 2 - 6);
        }
    }
}
