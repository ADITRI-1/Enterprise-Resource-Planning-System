import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CreateCoursesWindow extends JFrame {

    private JPanel sidebar;
    private boolean sidebarVisible = false;
    private final int SIDEBAR_WIDTH = 260;

    public CreateCoursesWindow() {

        setTitle("Create Courses");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        /* ---------------------------------------------------------
           TOP HEADER (same as Faculty/Student windows)
        --------------------------------------------------------- */
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(9, 53, 82));
                g2.fillRect(0, 0, getWidth(), getHeight());

                int cy = getHeight() / 2;
                int left = 30;

                g2.setColor(Color.WHITE);
                int barW = 10, barH = 46, barR = 4, gap = 10;
                int x = left;
                int barY = cy - barH / 2;

                for (int i = 0; i < 4; i++) {
                    g2.fillRoundRect(x, barY, barW, barH, barR, barR);
                    x += barW + gap;
                }

                int dOuter = 42;
                int dX = x - 12;
                int dY = cy - dOuter / 2;

                g2.setColor(new Color(85, 172, 168));
                g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);

                int textX = dX + dOuter + 25;

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 8);

                String line2 = "INFORMATION TECHNOLOGY";
                g2.drawString(line2, textX, cy + 18);

                int delhiX = textX + g2.getFontMetrics().stringWidth(line2) + 10;

                g2.setColor(new Color(85, 172, 168));
                g2.drawString("DELHI", delhiX, cy + 18);
            }
        };
        header.setBounds(0, 0, 1100, 90);
        add(header);

        /* ---------------------------------------------------------
           SLIDING SIDEBAR
        --------------------------------------------------------- */
        sidebar = new JPanel();
        sidebar.setLayout(null);
        sidebar.setBackground(new Color(240, 242, 248));
        sidebar.setBounds(-SIDEBAR_WIDTH + 20, 90, SIDEBAR_WIDTH, 590);
        add(sidebar);

        addSidebarContent();

        // Arrow trigger
        JLabel arrow = new JLabel("❱");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setBounds(10, 350, 30, 40);
        arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(arrow);

        sidebar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { slideSidebar(true); }
            @Override public void mouseExited(MouseEvent e) { slideSidebar(false); }
        });

        arrow.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { slideSidebar(true); }
            @Override public void mouseExited(MouseEvent e) { slideSidebar(false); }
        });

        /* ---------------------------------------------------------
           PAGE TITLE
        --------------------------------------------------------- */
        JLabel pageTitle = new JLabel("CREATE COURSES");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pageTitle.setForeground(new Color(18, 40, 60));
        pageTitle.setBounds(350, 120, 500, 50);
        add(pageTitle);

        /* ---------------------------------------------------------
           FORM PANEL (aligned properly)
        --------------------------------------------------------- */
        JPanel form = new JPanel(null);
        form.setBackground(Color.WHITE);
        form.setBounds(350, 180, 700, 350);
        add(form);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 17);

        JLabel lblTitle = new JLabel("Course Title");
        lblTitle.setFont(labelFont);
        lblTitle.setBounds(0, 10, 150, 30);
        form.add(lblTitle);

        JTextField txtTitle = new JTextField();
        txtTitle.setBounds(170, 10, 400, 35);
        form.add(txtTitle);

        JLabel lblCode = new JLabel("Course Code");
        lblCode.setFont(labelFont);
        lblCode.setBounds(0, 60, 150, 30);
        form.add(lblCode);

        JTextField txtCode = new JTextField();
        txtCode.setBounds(170, 60, 400, 35);
        form.add(txtCode);

        JLabel lblDept = new JLabel("Department");
        lblDept.setFont(labelFont);
        lblDept.setBounds(0, 110, 150, 30);
        form.add(lblDept);

        JComboBox<String> cbDept = new JComboBox<>(new String[]{"CSE", "ECE", "CSAM", "CSAI"});
        cbDept.setBounds(170, 110, 400, 35);
        form.add(cbDept);

        JLabel lblCredits = new JLabel("Credits");
        lblCredits.setFont(labelFont);
        lblCredits.setBounds(0, 160, 150, 30);
        form.add(lblCredits);

        JComboBox<String> cbCredits = new JComboBox<>(new String[]{"2", "3", "4"});
        cbCredits.setBounds(170, 160, 400, 35);
        form.add(cbCredits);

        JLabel lblSem = new JLabel("Semester");
        lblSem.setFont(labelFont);
        lblSem.setBounds(0, 210, 150, 30);
        form.add(lblSem);

        JComboBox<String> cbSem = new JComboBox<>(new String[]{"Monsoon", "Winter"});
        cbSem.setBounds(170, 210, 400, 35);
        form.add(cbSem);

        JButton btnCreate = new JButton("Create Courses");
        btnCreate.setBounds(170, 270, 400, 40);
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCreate.setBackground(new Color(25, 92, 160));
        btnCreate.setForeground(Color.WHITE);
        form.add(btnCreate);

        /* ---------------------------------------------------------
           FOOTER
        --------------------------------------------------------- */
        JLabel footer = new JLabel("© 2024 IIT Delhi     Privacy Policy     Terms & Service");
        footer.setBounds(250, 620, 700, 30);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setForeground(new Color(50, 50, 70));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(footer);
    }

    /* ---------------------------------------------------------
       SIDEBAR CONTENT
    --------------------------------------------------------- */
    private void addSidebarContent() {

        JPanel profileCard = new JPanel(null);
        profileCard.setBackground(Color.WHITE);
        profileCard.setBounds(20, 20, 220, 210);
        profileCard.setBorder(BorderFactory.createLineBorder(new Color(230, 231, 235), 2));
        sidebar.add(profileCard);

        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(225, 230, 238));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(100, 110, 130));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 50));
                g2.drawString("A", getWidth()/2 - 15, getHeight()/2 + 20);
            }
        };
        circle.setBounds(35, 12, 150, 150);
        profileCard.add(circle);

        JLabel lblAdmin = new JLabel("Admin", SwingConstants.CENTER);
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAdmin.setBounds(0, 165, 220, 40);
        profileCard.add(lblAdmin);

        /* Left buttons */
        JButton b1 = sidebarButton("Create User", 260);
        sidebar.add(b1);

        JButton b2 = sidebarButton("Create Courses", 320);
        sidebar.add(b2);

        JButton b3 = sidebarButton("Create Sections", 380);
        sidebar.add(b3);
    }

    private JButton sidebarButton(String text, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(20, y, 220, 40);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /* ---------------------------------------------------------
       SLIDING ANIMATION
    --------------------------------------------------------- */
    private void slideSidebar(boolean show) {
        int start = sidebar.getX();
        int end = show ? 0 : -SIDEBAR_WIDTH + 20;

        new Timer(5, new ActionListener() {
            int current = start;

            @Override public void actionPerformed(ActionEvent e) {
                if (show && current < end) { current += 10; }
                else if (!show && current > end) { current -= 10; }
                else { ((Timer) e.getSource()).stop(); }

                sidebar.setLocation(current, sidebar.getY());
            }
        }).start();
    }

    /* ---------------------------------------------------------
       MAIN FOR TESTING
    --------------------------------------------------------- */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CreateCoursesWindow().setVisible(true));
    }
}
