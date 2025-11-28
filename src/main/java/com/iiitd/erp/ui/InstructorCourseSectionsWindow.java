import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class InstructorCourseSectionsWindow extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorCourseSectionsWindow().setVisible(true));
    }

    public InstructorCourseSectionsWindow() {
        setTitle("Course Sections");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new InstructorDashboardWindow.HeaderBar(), BorderLayout.NORTH);
        add(new InstructorDashboardWindow.FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 251));
        add(main, BorderLayout.CENTER);

        main.add(new InstructorDashboardWindow.SlidingPanel(new InstructorDashboardWindow.LeftRail()),
                BorderLayout.WEST);
        main.add(centerContent(), BorderLayout.CENTER);
    }

    private JComponent centerContent() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("COURSE SECTIONS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setBorder(new EmptyBorder(20, 24, 10, 0));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Title", "Section", "Capacity", "Enrolled", "Schedule"};
        Object[][] data = {
                {"CSE201", "Data Structures", "A", "80", "76", "Mon/Wed 10–11"},
                {"CSE201", "Data Structures", "B", "80", "70", "Tue/Thu 12–1"},
                {"CSE301", "Algorithms", "A", "70", "68", "Mon/Wed 2–3"}
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(10, 24, 10, 24));
        p.add(sp, BorderLayout.CENTER);

        JButton add = new JButton("Add Section");
        JButton edit = new JButton("Edit Section");
        JButton delete = new JButton("Remove Section");
        JButton[] btns = {add, edit, delete};
        for (JButton b : btns) {
            b.setBackground(new Color(43,123,196));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);
        bottom.add(add);
        bottom.add(edit);
        bottom.add(delete);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }
}
