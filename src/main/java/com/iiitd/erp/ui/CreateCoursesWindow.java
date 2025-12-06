package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.CourseCreateRequest;
import com.iiitd.erp.ui.client.AdminClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CreateCoursesWindow extends JFrame {

    private final AdminClient admin;
    private final int SIDEBAR_WIDTH = 260;

    private JTextField txtTitle;
    private JTextField txtCode;
    private JComboBox<String> cbDept;
    private JComboBox<String> cbCredits;
    private JButton btnCreate;

    public CreateCoursesWindow(AdminClient admin) {
        this.admin = admin;

        setTitle("Create Courses");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(9, 53, 82)); g2.fillRect(0, 0, getWidth(), getHeight());
                int cy = getHeight() / 2; int left = 30;
                g2.setColor(Color.WHITE); int barW = 10, barH = 46, barR = 4, gap = 10, x = left, barY = cy - barH / 2;
                for (int i = 0; i < 4; i++) { g2.fillRoundRect(x, barY, barW, barH, barR, barR); x += barW + gap; }
                int dOuter = 42, dX = x - 12, dY = cy - dOuter / 2;
                g2.setColor(new Color(85, 172, 168)); g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);
                int textX = dX + dOuter + 25;
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 8);
                String line2 = "INFORMATION TECHNOLOGY"; g2.drawString(line2, textX, cy + 18);
                int delhiX = textX + g2.getFontMetrics().stringWidth(line2) + 10;
                g2.setColor(new Color(85, 172, 168)); g2.drawString("DELHI", delhiX, cy + 18);
            }
        };
        header.setBounds(0, 0, 1100, 90);
        add(header);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(null);
        sidebar.setBackground(new Color(240, 242, 248));
        sidebar.setBounds(-SIDEBAR_WIDTH + 20, 90, SIDEBAR_WIDTH, 590);
        add(sidebar);

        JLabel arrow = new JLabel("❱");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setBounds(10, 350, 30, 40);
        arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(arrow);

        MouseAdapter slideHandler = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { slideSidebar(sidebar, true); }
            @Override public void mouseExited(MouseEvent e) { slideSidebar(sidebar, false); }
        };
        sidebar.addMouseListener(slideHandler);
        arrow.addMouseListener(slideHandler);

        JLabel pageTitle = new JLabel("CREATE COURSES");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pageTitle.setForeground(new Color(18, 40, 60));
        pageTitle.setBounds(350, 120, 500, 50);
        add(pageTitle);

        JPanel form = new JPanel(null);
        form.setBackground(Color.WHITE);
        form.setBounds(350, 180, 700, 350);
        add(form);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 17);

        JLabel lblTitle = new JLabel("Course Title"); lblTitle.setFont(labelFont); lblTitle.setBounds(0, 10, 150, 30); form.add(lblTitle);
        txtTitle = new JTextField(); txtTitle.setBounds(170, 10, 400, 35); form.add(txtTitle);

        JLabel lblCode = new JLabel("Course Code"); lblCode.setFont(labelFont); lblCode.setBounds(0, 60, 150, 30); form.add(lblCode);
        txtCode = new JTextField(); txtCode.setBounds(170, 60, 400, 35); form.add(txtCode);

        JLabel lblDept = new JLabel("Department"); lblDept.setFont(labelFont); lblDept.setBounds(0, 110, 150, 30); form.add(lblDept);
        cbDept = new JComboBox<>(new String[]{"CSE", "ECE", "CSAM", "CSAI"}); cbDept.setBounds(170, 110, 400, 35); form.add(cbDept);

        JLabel lblCredits = new JLabel("Credits"); lblCredits.setFont(labelFont); lblCredits.setBounds(0, 160, 150, 30); form.add(lblCredits);
        cbCredits = new JComboBox<>(new String[]{"2", "3", "4"}); cbCredits.setBounds(170, 160, 400, 35); form.add(cbCredits);

        JLabel lblSem = new JLabel("Semester (info only)"); lblSem.setFont(labelFont); lblSem.setBounds(0, 210, 200, 30); form.add(lblSem);
        JComboBox<String> cbSem = new JComboBox<>(new String[]{"Monsoon", "Winter"}); cbSem.setBounds(170, 210, 400, 35); form.add(cbSem);

        btnCreate = new JButton("Create Courses");
        btnCreate.setBounds(170, 270, 400, 40);
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCreate.setBackground(new Color(25, 92, 160));
        btnCreate.setForeground(Color.WHITE);
        form.add(btnCreate);

        JLabel footer = new JLabel("© 2024 IIT Delhi     Privacy Policy     Terms & Service");
        footer.setBounds(250, 620, 700, 30);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setForeground(new Color(50, 50, 70));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(footer);

        btnCreate.addActionListener(e -> doCreateCourse());
    }

    private void doCreateCourse() {
        if (admin == null) {
            JOptionPane.showMessageDialog(this, "No admin session. Please log in as admin.", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("CreateCourse aborted: admin null");
            return;
        }
        System.out.println("CreateCourse calling API courseId=" + txtCode.getText().trim());
        btnCreate.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    CourseCreateRequest req = CourseCreateRequest.builder()
                            .courseId(txtCode.getText().trim())
                            .title(txtTitle.getText().trim())
                            .deptId(cbDept.getSelectedItem().toString())
                            .credits(Integer.parseInt(cbCredits.getSelectedItem().toString()))
                            .build();
                    admin.createCourse(req);
                    System.out.println("CreateCourse success");
                    JOptionPane.showMessageDialog(CreateCoursesWindow.this, "Course created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    System.out.println("CreateCourse error: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CreateCoursesWindow.this, "Error: " + ex.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { btnCreate.setEnabled(true); }
        };
        worker.execute();
    }

    private void slideSidebar(JPanel sidebar, boolean show) {
        int target = show ? 0 : -SIDEBAR_WIDTH + 20;
        sidebar.setLocation(target, sidebar.getY());
        sidebar.revalidate();
        sidebar.repaint();
    }
}
