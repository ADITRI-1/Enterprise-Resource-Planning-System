package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.StudentUpdateRequest;
import com.iiitd.erp.io.dto.response.ProgramResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateStudentPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private final StudentResponse student;
    private static final Logger LOGGER = Logger.getLogger(UpdateStudentPanel.class.getName());

    private final JTextField nameField;
    private final JComboBox<ProgramResponse> programIdField = new JComboBox<>();
    private final JTextField yearField;
    private final JTextField dobField;

    public UpdateStudentPanel(AdminClient admin, Runnable onBack, StudentResponse student) {
        this.admin = admin;
        this.onBack = onBack;
        this.student = student;

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

        JLabel title = new JLabel("Update Student: " + student.name(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(student.name(), 20);
        yearField = new JTextField(student.yearOfRegistration(), 20);
        yearField.setEditable(false);
        yearField.setBackground(new Color(240, 240, 240));
        
        String displayDob = "";
        if (student.dateOfBirth() != null && !student.dateOfBirth().isEmpty()) {
            try {
                LocalDate dob = LocalDate.parse(student.dateOfBirth(), DateTimeFormatter.ISO_LOCAL_DATE);
                displayDob = dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e) {
                displayDob = student.dateOfBirth(); // Fallback to original string if parsing fails
            }
        }
        dobField = new JTextField(displayDob, 20);

        addField(formPanel, gbc, 0, "Full Name:", nameField);
        addField(formPanel, gbc, 1, "Program:", programIdField);
        addField(formPanel, gbc, 2, "Year of Registration:", yearField);
        setupDobField(formPanel, gbc, 3);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton updateButton = new JButton("Save Changes");
        stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateStudent());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(updateButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);
        
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1;
        mainGbc.weighty = 1;
        mainGbc.anchor = GridBagConstraints.CENTER;
        add(formContainer, mainGbc);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadPrograms();
            }
        });
    }
    
    private void setupDobField(JPanel panel, GridBagConstraints gbc, int y) {
        JLabel label = new JLabel("Date of Birth (DD-MM-YYYY):");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(label, gbc);

        JPanel dobPanel = new JPanel(new BorderLayout(8, 0));
        dobPanel.setOpaque(false);
        styleTextField(dobField);
        dobPanel.add(dobField, BorderLayout.CENTER);
        
        JButton dobButton = new JButton("📅");
        dobButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        dobButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dobButton.addActionListener(e -> {
            DatePicker picker = new DatePicker((Frame) SwingUtilities.getWindowAncestor(this));
            picker.setVisible(true);
            String selectedDate = picker.getSelectedDateAsString();
            if (!selectedDate.isEmpty()) {
                dobField.setText(selectedDate);
            }
        });
        dobPanel.add(dobButton, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dobPanel, gbc);
    }

    private void loadPrograms() {
        SwingWorker<List<ProgramResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProgramResponse> doInBackground() throws Exception {
                return admin.listPrograms();
            }
            @Override
            protected void done() {
                try {
                    List<ProgramResponse> programs = get();
                    programIdField.removeAllItems();
                    programs.forEach(programIdField::addItem);
                    for (int i = 0; i < programs.size(); i++) {
                        if (programs.get(i).programId().equals(student.programId())) {
                            programIdField.setSelectedIndex(i);
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load programs", e);
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
                if (value instanceof ProgramResponse) {
                    setText(((ProgramResponse) value).programId());
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

    private void updateStudent() {
        ProgramResponse selectedProgram = (ProgramResponse) programIdField.getSelectedItem();
        if (selectedProgram == null) {
            JOptionPane.showMessageDialog(this, "Please select a program.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dobForBackend = "";
        try {
            LocalDate dob = LocalDate.parse(dobField.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            dobForBackend = dob.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StudentUpdateRequest request = StudentUpdateRequest.builder()
                .name(nameField.getText())
                .programId(selectedProgram.programId())
                .yearOfRegistration(yearField.getText())
                .dateOfBirth(dobForBackend)
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Updating student: " + student.rollNo());
                    admin.updateStudent(String.valueOf(student.rollNo()), request);
                    JOptionPane.showMessageDialog(UpdateStudentPanel.this, "Student updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update student", ex);
                    JOptionPane.showMessageDialog(UpdateStudentPanel.this, "Error updating student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
