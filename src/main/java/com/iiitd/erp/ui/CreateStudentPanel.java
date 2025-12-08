package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import com.iiitd.erp.io.dto.response.ProgramResponse;
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

public class CreateStudentPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateStudentPanel.class.getName());

    private final IconTextField rollNoField = new IconTextField();
    private final IconTextField nameField = new IconTextField();
    private final JComboBox<ProgramResponse> programIdField = new JComboBox<>();
    private final JTextField yearField = new JTextField(Year.now().toString());
    private final IconTextField dobField = new IconTextField();

    public CreateStudentPanel(AdminClient admin, Runnable onBack) {
        this.admin = admin;
        this.onBack = onBack;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel(new BorderLayout(0, 20));
        formContainer.setOpaque(true);
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new DropShadowBorder());

        JLabel title = new JLabel("Create New Student", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Set icons for text fields
        // Note: You would need to have these icons in your project resources
        // For now, I'll use placeholder characters.
        // rollNoField.setIcon(new ImageIcon(getClass().getResource("/icons/id.png")));
        // nameField.setIcon(new ImageIcon(getClass().getResource("/icons/user.png")));
        // dobField.setIcon(new ImageIcon(getClass().getResource("/icons/calendar.png")));


        addField(formPanel, gbc, 0, "Roll Number:", rollNoField);
        addField(formPanel, gbc, 1, "Full Name:", nameField);
        addField(formPanel, gbc, 2, "Program:", programIdField);
        
        yearField.setEditable(false);
        yearField.setBackground(new Color(240, 240, 240));
        addField(formPanel, gbc, 3, "Year of Registration:", yearField);
        
        setupDobField(formPanel, gbc, 4);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Student");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createStudent());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        add(formContainer, mainGbc);
        
        loadPrograms();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                resetForm();
            }
        });
    }

    private void resetForm() {
        rollNoField.setText("");
        nameField.setText("");
        dobField.setText("");
        if (programIdField.getItemCount() > 0) programIdField.setSelectedIndex(0);
        yearField.setText(Year.now().toString());
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
                    programIdField.removeAllItems();
                    get().forEach(programIdField::addItem);
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

    private void createStudent() {
        if (rollNoField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || dobField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ProgramResponse selectedProgram = (ProgramResponse) programIdField.getSelectedItem();
        if (selectedProgram == null) {
            JOptionPane.showMessageDialog(this, "Please select a program.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dobForBackend;
        try {
            LocalDate dob = LocalDate.parse(dobField.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            dobForBackend = dob.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StudentCreateRequest request = StudentCreateRequest.builder()
                .rollNo(Integer.parseInt(rollNoField.getText().trim()))
                .name(nameField.getText().trim())
                .programId(selectedProgram.programId())
                .yearOfRegistration(yearField.getText())
                .dateOfBirth(dobForBackend)
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating student: " + request);
                    admin.createStudent(request);
                    JOptionPane.showMessageDialog(CreateStudentPanel.this, "Student created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create student", ex);
                    JOptionPane.showMessageDialog(CreateStudentPanel.this, "Error creating student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
