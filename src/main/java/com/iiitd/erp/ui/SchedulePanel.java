package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchedulePanel extends JPanel {

    private final AdminClient admin;
    private final boolean isMaintenanceMode;
    private static final Logger LOGGER = Logger.getLogger(SchedulePanel.class.getName());

    private final JComboBox<CourseResponse> cbCourses = new JComboBox<>();
    private final JComboBox<SectionResponse> cbSections = new JComboBox<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContent = new JPanel(cardLayout);
    private final JButton viewButton = new JButton("View/Edit Schedule");

    public SchedulePanel(AdminClient admin, boolean maintenanceMode) {
        this.admin = admin;
        this.isMaintenanceMode = maintenanceMode;
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Class Schedule Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        mainContent.setOpaque(false);
        mainContent.add(createSelectionPanel(), "selection");
        add(mainContent, BorderLayout.CENTER);

        cardLayout.show(mainContent, "selection");
        loadCourses();
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel formContainer = new JPanel(new BorderLayout(20, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel prompt = new JLabel("Select a Section to Begin");
        prompt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        prompt.setHorizontalAlignment(SwingConstants.CENTER);
        formContainer.add(prompt, BorderLayout.NORTH);

        JPanel dropdownPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dropdownPanel.setOpaque(false);
        
        dropdownPanel.add(new JLabel("Course:"));
        styleComboBox(cbCourses);
        dropdownPanel.add(cbCourses);
        
        dropdownPanel.add(new JLabel("Section:"));
        styleComboBox(cbSections);
        cbSections.setEnabled(false);
        dropdownPanel.add(cbSections);
        
        formContainer.add(dropdownPanel, BorderLayout.CENTER);

        stylePrimaryButton(viewButton);
        viewButton.setEnabled(false);
        formContainer.add(viewButton, BorderLayout.SOUTH);

        cbCourses.addActionListener(e -> {
            CourseResponse selected = (CourseResponse) cbCourses.getSelectedItem();
            if (selected != null) {
                loadSectionsForCourse(selected.courseId());
            } else {
                // Reset and disable the sections dropdown
                cbSections.removeAllItems();
                cbSections.addItem(null);
                cbSections.setEnabled(false);
            }
        });

        cbSections.addActionListener(e -> viewButton.setEnabled(cbSections.getSelectedItem() != null));

        viewButton.addActionListener(e -> {
            SectionResponse selectedSection = (SectionResponse) cbSections.getSelectedItem();
            if (selectedSection != null) {
                TimetableView timetablePanel = new TimetableView(admin, selectedSection, () -> cardLayout.show(mainContent, "selection"));
                mainContent.add(timetablePanel, "timetable");
                cardLayout.show(mainContent, "timetable");
            }
        });
        
        panel.add(formContainer);
        return panel;
    }

    private void loadCourses() {
        SwingWorker<List<CourseResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourseResponse> doInBackground() throws Exception {
                return admin.listCourses();
            }
            @Override
            protected void done() {
                try {
                    cbCourses.removeAllItems();
                    cbCourses.addItem(null);
                    get().forEach(cbCourses::addItem);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load courses", e);
                }
            }
        };
        worker.execute();
    }

    private void loadSectionsForCourse(String courseId) {
        SwingWorker<List<SectionResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SectionResponse> doInBackground() throws Exception {
                return admin.listSectionsForCourse(courseId, null, null);
            }
            @Override
            protected void done() {
                try {
                    cbSections.removeAllItems();
                    cbSections.addItem(null);
                    get().forEach(cbSections::addItem);
                    cbSections.setEnabled(true);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load sections", e);
                }
            }
        };
        worker.execute();
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseResponse) setText(((CourseResponse) value).title());
                else if (value instanceof SectionResponse) setText(((SectionResponse) value).sectionId());
                else setText("Select...");
                return this;
            }
        });
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
