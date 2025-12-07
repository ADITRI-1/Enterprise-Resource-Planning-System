package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.SectionCreateRequest;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class CreateSectionPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateSectionPanel.class.getName());

    private final JComboBox<CourseResponse> cbCourses = new JComboBox<>();
    private final JComboBox<InstructorResponse> cbInstructors = new JComboBox<>();
    private final JComboBox<String> cbCapacity = new JComboBox<>(new String[]{"30", "60", "100", "150"});
    private final JTextField txtDeadline = new JTextField(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), 20);
    private final JComboBox<String> cbSemester = new JComboBox<>(new String[]{"Monsoon", "Winter"});
    private final JComboBox<Integer> cbYear;

    public CreateSectionPanel(AdminClient admin, Runnable onBack) {
        this.admin = admin;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel(new BorderLayout(0, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(40, 50, 40, 50)
        ));

        JLabel title = new JLabel("Create New Section", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Integer[] years = IntStream.rangeClosed(Year.now().getValue(), Year.now().getValue() + 5).boxed().toArray(Integer[]::new);
        cbYear = new JComboBox<>(years);

        addField(formPanel, gbc, 0, "Course:", cbCourses);
        addField(formPanel, gbc, 1, "Instructor:", cbInstructors);
        addField(formPanel, gbc, 2, "Semester:", cbSemester);
        addField(formPanel, gbc, 3, "Year:", cbYear);
        addField(formPanel, gbc, 4, "Capacity:", cbCapacity);
        setupDeadlineField(formPanel, gbc, 5);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Section");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createSection());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        add(formContainer, mainGbc);

        loadDropdownData();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                resetForm();
            }
        });
    }

    private void resetForm() {
        if (cbCourses.getItemCount() > 0) cbCourses.setSelectedIndex(0);
        if (cbInstructors.getItemCount() > 0) cbInstructors.setSelectedIndex(0);
        if (cbCapacity.getItemCount() > 0) cbCapacity.setSelectedIndex(0);
        if (cbSemester.getItemCount() > 0) cbSemester.setSelectedIndex(0);
        if (cbYear.getItemCount() > 0) cbYear.setSelectedIndex(0);
        txtDeadline.setText(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    private void setupDeadlineField(JPanel panel, GridBagConstraints gbc, int y) {
        JLabel label = new JLabel("Registration Deadline:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        JPanel deadlinePanel = new JPanel(new BorderLayout(8, 0));
        deadlinePanel.setOpaque(false);
        styleTextField(txtDeadline);
        deadlinePanel.add(txtDeadline, BorderLayout.CENTER);
        
        JButton deadlineButton = new JButton("📅");
        deadlineButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        deadlineButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deadlineButton.addActionListener(e -> {
            DatePicker picker = new DatePicker((Frame) SwingUtilities.getWindowAncestor(this));
            picker.setVisible(true);
            String selectedDate = picker.getSelectedDateAsString();
            if (!selectedDate.isEmpty()) {
                txtDeadline.setText(selectedDate);
            }
        });
        deadlinePanel.add(deadlineButton, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(deadlinePanel, gbc);
    }

    private void loadDropdownData() {
        SwingWorker<List<CourseResponse>, Void> courseWorker = new SwingWorker<>() {
            @Override
            protected List<CourseResponse> doInBackground() throws Exception {
                return admin.listCourses();
            }
            @Override
            protected void done() {
                try {
                    cbCourses.removeAllItems();
                    get().forEach(cbCourses::addItem);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load courses for dropdown", e);
                }
            }
        };

        SwingWorker<List<InstructorResponse>, Void> instructorWorker = new SwingWorker<>() {
            @Override
            protected List<InstructorResponse> doInBackground() throws Exception {
                return admin.listInstructors();
            }
            @Override
            protected void done() {
                try {
                    cbInstructors.removeAllItems();
                    get().forEach(cbInstructors::addItem);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructors for dropdown", e);
                }
            }
        };

        courseWorker.execute();
        instructorWorker.execute();
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        if (component instanceof JTextField) styleTextField((JTextField) component);
        else if (component instanceof JComboBox) styleComboBox((JComboBox<?>) component);
        
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            new EmptyBorder(10, 12, 10, 12)
        ));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseResponse) {
                    setText(((CourseResponse) value).title());
                } else if (value instanceof InstructorResponse) {
                    setText(((InstructorResponse) value).name());
                }
                return this;
            }
        });
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(45, 125, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 35, 12, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(225, 225, 225));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 35, 12, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void createSection() {
        if (cbCourses.getSelectedItem() == null || cbInstructors.getSelectedItem() == null || txtDeadline.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CourseResponse selectedCourse = (CourseResponse) cbCourses.getSelectedItem();
        InstructorResponse selectedInstructor = (InstructorResponse) cbInstructors.getSelectedItem();

        String deadlineForBackend;
        try {
            LocalDate deadline = LocalDate.parse(txtDeadline.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            deadlineForBackend = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SectionCreateRequest request = new SectionCreateRequest(
                selectedInstructor.instructorCode(),
                cbSemester.getSelectedItem().toString(),
                (Integer) cbYear.getSelectedItem(),
                Integer.parseInt(cbCapacity.getSelectedItem().toString()),
                LocalDate.parse(deadlineForBackend),
                true
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating section for course " + selectedCourse.courseId() + " with request: " + request);
                    admin.createSection(selectedCourse.courseId(), request);
                    JOptionPane.showMessageDialog(CreateSectionPanel.this, "Section created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create section", ex);
                    JOptionPane.showMessageDialog(CreateSectionPanel.this, "Error creating section: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
