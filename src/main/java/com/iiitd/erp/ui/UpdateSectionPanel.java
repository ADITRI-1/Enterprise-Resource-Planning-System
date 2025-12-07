package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.SectionUpdateRequest;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class UpdateSectionPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private final SectionResponse section;
    private static final Logger LOGGER = Logger.getLogger(UpdateSectionPanel.class.getName());

    private final JComboBox<InstructorResponse> cbInstructors = new JComboBox<>();
    private final JComboBox<String> cbCapacity = new JComboBox<>(new String[]{"30", "60", "100", "150"});
    private final JComboBox<String> cbSemester = new JComboBox<>(new String[]{"Monsoon", "Winter"});
    private final JComboBox<Integer> cbYear;
    private final JTextField txtDeadline;

    public UpdateSectionPanel(AdminClient admin, Runnable onBack, SectionResponse section) {
        this.admin = admin;
        this.onBack = onBack;
        this.section = section;

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

        JLabel title = new JLabel("Update Section: " + section.sectionId(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Integer[] years = IntStream.rangeClosed(Year.now().getValue() - 2, Year.now().getValue() + 5).boxed().toArray(Integer[]::new);
        cbYear = new JComboBox<>(years);

        cbCapacity.setSelectedItem(String.valueOf(section.capacity()));
        cbSemester.setSelectedItem(section.semester());
        cbYear.setSelectedItem(section.year());
        
        String displayDeadline = "";
        if (section.registrationDeadline() != null) {
            displayDeadline = section.registrationDeadline().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        txtDeadline = new JTextField(displayDeadline, 20);

        addField(formPanel, gbc, 0, "Instructor:", cbInstructors);
        addField(formPanel, gbc, 1, "Semester:", cbSemester);
        addField(formPanel, gbc, 2, "Year:", cbYear);
        addField(formPanel, gbc, 3, "Capacity:", cbCapacity);
        setupDeadlineField(formPanel, gbc, 4);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton updateButton = new JButton("Save Changes");
        stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateSection());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(updateButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        add(formContainer, mainGbc);

        loadInstructors();
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

    private void loadInstructors() {
        SwingWorker<List<InstructorResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InstructorResponse> doInBackground() throws Exception {
                return admin.listInstructors();
            }

            @Override
            protected void done() {
                try {
                    List<InstructorResponse> instructors = get();
                    cbInstructors.removeAllItems();
                    instructors.forEach(cbInstructors::addItem);
                    for (int i = 0; i < instructors.size(); i++) {
                        if (instructors.get(i).instructorCode().equals(section.instructorCode())) {
                            cbInstructors.setSelectedIndex(i);
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load instructors for dropdown", e);
                }
            }
        };
        worker.execute();
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
                if (value instanceof InstructorResponse) {
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

    private void updateSection() {
        InstructorResponse selectedInstructor = (InstructorResponse) cbInstructors.getSelectedItem();
        if (selectedInstructor == null) {
            JOptionPane.showMessageDialog(this, "Please select an instructor.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String deadlineForBackend = "";
        try {
            LocalDate deadline = LocalDate.parse(txtDeadline.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            deadlineForBackend = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SectionUpdateRequest request = new SectionUpdateRequest(
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
                    LOGGER.info("Updating section: " + section.sectionId());
                    admin.updateSection(section.sectionId(), request);
                    JOptionPane.showMessageDialog(UpdateSectionPanel.this, "Section updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update section", ex);
                    JOptionPane.showMessageDialog(UpdateSectionPanel.this, "Error updating section: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
