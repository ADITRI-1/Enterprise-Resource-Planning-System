package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.SectionCreateRequest;
import com.iiitd.erp.ui.client.AdminClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

public class CreateSectionWindow extends JFrame {

    private final AdminClient admin;

    public static void main(String[] args) { SwingUtilities.invokeLater(CreateSectionWindow::new); }

    public CreateSectionWindow() { this(null); }

    public CreateSectionWindow(AdminClient admin) {
        this.admin = admin;

        setTitle("Create Section");
        setSize(1250, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        main.add(new SlidingPanel(new LeftPanel()), BorderLayout.WEST);
        main.add(new ContentArea(), BorderLayout.CENTER);

        setVisible(true);
    }

    static class HeaderBar extends JPanel {
        HeaderBar() { setPreferredSize(new Dimension(0, 92)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(9, 53, 82)); g2.fillRect(0, 0, getWidth(), getHeight());
            int cy = getHeight() / 2; int left = 26;
            g2.setColor(Color.WHITE); int barW = 10, barH = 44, barR = 6, gap = 10, x = left, barY = cy - barH / 2;
            for (int i = 0; i < 4; i++) { g2.fillRoundRect(x, barY, barW, barH, barR, barR); x += barW + gap; }
            int dOuter = 42, dX = x - 18, dY = cy - dOuter / 2;
            g2.setColor(new Color(85, 172, 168)); g2.setStroke(new BasicStroke(8f)); g2.drawArc(dX, dY, dOuter, dOuter, -90, 180);
            int textX = dX + dOuter + 28;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20)); g2.setColor(Color.WHITE);
            g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy - 6);
            String line2 = "INFORMATION TECHNOLOGY"; g2.drawString(line2, textX, cy + 20);
            g2.setColor(new Color(85, 172, 168)); g2.drawString("DELHI", textX + g2.getFontMetrics().stringWidth(line2) + 10, cy + 20);
        }
    }

    static class SlidingPanel extends JPanel {
        private final JPanel content;
        private final Arrow arrow;
        private boolean expanded = false;
        private static final int FULL = 300;
        private static final int COLLAPSED = 38;

        SlidingPanel(JPanel inner) {
            setOpaque(false); setLayout(null); setPreferredSize(new Dimension(COLLAPSED, 0));
            content = inner; arrow = new Arrow();
            add(arrow); add(content);
            MouseAdapter listener = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { expand(); }
                @Override public void mouseExited(MouseEvent e) { collapse(); }
            };
            arrow.addMouseListener(listener); content.addMouseListener(listener); this.addMouseListener(listener);
        }

        private void expand() { if (expanded) return; expanded = true; arrow.setExpanded(true); setPreferredSize(new Dimension(FULL, getHeight())); revalidate(); repaint(); }
        private void collapse() { if (!expanded) return; expanded = false; arrow.setExpanded(false); setPreferredSize(new Dimension(COLLAPSED, getHeight())); revalidate(); repaint(); }

        @Override public void doLayout() {
            int h = getParent().getHeight();
            arrow.setBounds(0, 0, COLLAPSED, h);
            content.setBounds(expanded ? 0 : COLLAPSED, 0, FULL, h);
        }

        static class Arrow extends JPanel {
            private boolean exp = false;
            void setExpanded(boolean e) { exp = e; repaint(); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(230, 233, 240)); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(60, 70, 80)); int mid = getHeight() / 2;
                if (exp) g2.fillPolygon(new int[]{20, 10, 20}, new int[]{mid - 10, mid, mid + 10}, 3);
                else g2.fillPolygon(new int[]{10, 20, 10}, new int[]{mid - 10, mid, mid + 10}, 3);
            }
        }
    }

    static class LeftPanel extends JPanel {
        LeftPanel() {
            setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalStrut(20));
            add(adminCard()); add(Box.createVerticalStrut(20));
            add(button("Create User")); add(Box.createVerticalStrut(10));
            add(button("Create Courses")); add(Box.createVerticalStrut(10));
            add(button("Create Sections")); add(Box.createVerticalGlue());
        }

        private JPanel adminCard() {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g; g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(20, 0, 20, 0));
            card.add(avatar()); card.add(Box.createVerticalStrut(10));
            JLabel admin = new JLabel("Admin"); admin.setAlignmentX(Component.CENTER_ALIGNMENT); admin.setFont(new Font("Segoe UI", Font.BOLD, 20));
            card.add(admin);
            JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0, 18, 0, 18)); wrap.add(card);
            return wrap;
        }

        private JComponent avatar() {
            return new JComponent() {
                @Override public Dimension getPreferredSize() { return new Dimension(150, 150); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int d = 120; int x = (getWidth() - d) / 2; int y = 10;
                    g2.setColor(new Color(240, 244, 248)); g2.fillOval(x, y, d, d);
                    g2.setColor(new Color(220, 225, 232)); g2.setStroke(new BasicStroke(6f)); g2.drawOval(x + 3, y + 3, d - 6, d - 6);
                    g2.setColor(new Color(40, 50, 60)); g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
                    FontMetrics fm = g2.getFontMetrics(); String s = "A";
                    g2.drawString(s, getWidth()/2 - fm.stringWidth(s)/2, y + d/2 + fm.getAscent()/2 - 8);
                }
            };
        }

        private JPanel button(String text) {
            JPanel panel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(new Color(225, 225, 225)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                }
            };
            panel.setOpaque(false);
            panel.setLayout(new BorderLayout());
            panel.setPreferredSize(new Dimension(250, 60));
            panel.setBorder(new EmptyBorder(12, 20, 12, 20));
            JLabel txt = new JLabel(text); txt.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            panel.add(txt, BorderLayout.WEST);
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { System.out.println("LeftPanel nav click (no-op): " + text); }
            });
            JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0, 18, 0, 18)); wrap.add(panel);
            return wrap;
        }
    }

    class ContentArea extends JPanel {
        private static final int FIELD_WIDTH = 520;
        private static final int FIELD_HEIGHT = 36;

        private JTextField courseTitle;
        private JTextField courseCode;
        private JComboBox<String> instructor;
        private JComboBox<String> capacity;
        private JTextField deadline;
        private JButton submit;

        ContentArea() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel title = new JLabel("CREATE SECTION");
            title.setFont(new Font("Segoe UI", Font.BOLD, 38));
            title.setForeground(new Color(30, 40, 55));

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(30, 280, 10, 20));
            wrap.add(title, BorderLayout.WEST);
            add(wrap, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            form.setBorder(new EmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(14, 20, 14, 20);
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            courseTitle = textField("");
            courseCode = textField("");
            instructor = comboBox("", "inst1");
            capacity = comboBox("", "30", "60", "100");
            deadline = textField("2024-07-31");

            addRow(form, gbc, row++, "Course Title", courseTitle);
            addRow(form, gbc, row++, "Course Code", courseCode);
            addRow(form, gbc, row++, "Instructor Code", instructor);
            addRow(form, gbc, row++, "Capacity", capacity);
            addRow(form, gbc, row++, "Registration Deadline", deadline);

            submit = new JButton("Create Section");
            submit.setFont(new Font("Segoe UI", Font.BOLD, 20));
            submit.setBackground(new Color(32, 118, 196));
            submit.setForeground(Color.WHITE);
            submit.setFocusPainted(false);
            submit.setBorder(new EmptyBorder(12, 20, 12, 20));
            submit.setPreferredSize(new Dimension(FIELD_WIDTH, 45));

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; form.add(submit, gbc);

            add(form, BorderLayout.CENTER);

            submit.addActionListener(e -> doCreateSection());
        }

        private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = row;
            JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            p.add(l, gbc);
            gbc.gridx = 1;
            field.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            field.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            field.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            if (field instanceof JComboBox) {
                ((JComboBox<?>) field).setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
                ((JComboBox<?>) field).setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
                ((JComboBox<?>) field).setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            }
            p.add(field, gbc);
        }

        private JTextField textField(String placeholder) {
            JTextField f = new JTextField(30);
            f.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            f.setText(placeholder);
            return f;
        }

        private JComboBox<String> comboBox(String def, String... items) {
            String[] data = new String[items.length + 1];
            data[0] = def;
            System.arraycopy(items, 0, data, 1, items.length);
            JComboBox<String> cb = new JComboBox<>(data);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            return cb;
        }

        private void doCreateSection() {
            if (admin == null) {
                JOptionPane.showMessageDialog(CreateSectionWindow.this, "No admin session. Please log in as admin.", "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println("CreateSection aborted: admin null");
                return;
            }
            String courseId = courseCode.getText().trim();
            String instr = instructor.getSelectedItem() == null ? "" : instructor.getSelectedItem().toString();
            String capStr = capacity.getSelectedItem() == null ? "0" : capacity.getSelectedItem().toString();
            String regDeadline = deadline.getText().trim();
            System.out.println("CreateSection calling API courseId=" + courseId + " instructor=" + instr + " capacity=" + capStr + " deadline=" + regDeadline);
            submit.setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        SectionCreateRequest req = new SectionCreateRequest(
                                instr,
                                "Monsoon",
                                2024,
                                Integer.parseInt(capStr),
                                LocalDate.parse(regDeadline),
                                true
                        );
                        admin.createSection(courseId, req);
                        System.out.println("CreateSection success");
                        JOptionPane.showMessageDialog(CreateSectionWindow.this, "Section created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        System.out.println("CreateSection error: " + ex.getMessage());
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(CreateSectionWindow.this, "Error: " + ex.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override protected void done() { submit.setEnabled(true); }
            };
            worker.execute();
        }
    }

    static class FooterBar extends JPanel {
        FooterBar() { setPreferredSize(new Dimension(0, 60)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(9, 53, 82)); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            g2.setColor(Color.WHITE);
            String[] items = { "© 2024 IIT Delhi", "Privacy Policy", "Terms & Service" };
            int x = 40, y = getHeight() / 2 + 6;
            for (String s : items) { g2.drawString(s, x, y); x += g2.getFontMetrics().stringWidth(s) + 40; }
        }
    }
}
