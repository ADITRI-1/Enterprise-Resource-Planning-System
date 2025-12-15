package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.InstructorCreateRequest;
import com.iiitd.erp.io.dto.response.DepartmentResponse;
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

public class CreateInstructorPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private static final Logger LOGGER = Logger.getLogger(CreateInstructorPanel.class.getName());

    private final JTextField codeField = new JTextField(20);
    private final JTextField nameField = new JTextField(20);
    private final JComboBox<DepartmentResponse> deptIdField = new JComboBox<>();
    private final JTextField dobField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);

    public CreateInstructorPanel(AdminClient admin, Runnable onBack) {
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

        JLabel title = new JLabel("Create New Instructor", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        formContainer.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addField(formPanel, gbc, 0, "Instructor Code:", codeField);
        addField(formPanel, gbc, 1, "Full Name:", nameField);
        addField(formPanel, gbc, 2, "Department:", deptIdField);
        setupDobField(formPanel, gbc, 3);
        addField(formPanel, gbc, 4, "Email (Optional):", emailField);
        addField(formPanel, gbc, 5, "Phone (Optional):", phoneField);

        formContainer.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = new JButton("Create Instructor");
        stylePrimaryButton(createButton);
        createButton.addActionListener(e -> createInstructor());

        JButton backButton = new JButton("Cancel");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(createButton);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        GridBagConstraints mainGbc = new GridBagConstraints();
        add(formContainer, mainGbc);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                resetForm();
                loadDepartments();
            }
        });
    }

    private void resetForm() {
        codeField.setText("");
        nameField.setText("");
        dobField.setText("");
        emailField.setText("");
        phoneField.setText("");
        if (deptIdField.getItemCount() > 0) deptIdField.setSelectedIndex(0);
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

    private void loadDepartments() {
        SwingWorker<List<DepartmentResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DepartmentResponse> doInBackground() throws Exception {
                return admin.listDepartments();
            }
            @Override
            protected void done() {
                try {
                    deptIdField.removeAllItems();
                    get().forEach(deptIdField::addItem);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load departments", e);
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
                if (value instanceof DepartmentResponse) {
                    setText(((DepartmentResponse) value).deptId());
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

    private void createInstructor() {
        if (codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || dobField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DepartmentResponse selectedDept = (DepartmentResponse) deptIdField.getSelectedItem();
        if (selectedDept == null) {
            JOptionPane.showMessageDialog(this, "Please select a department.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        InstructorCreateRequest request = InstructorCreateRequest.builder()
                .instructorCode(codeField.getText().trim())
                .name(nameField.getText().trim())
                .deptId(selectedDept.deptId())
                .dateOfBirth(dobForBackend)
                .email(emailField.getText().trim())
                .phone(phoneField.getText().trim())
                .build();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    LOGGER.info("Creating instructor: " + request);
                    admin.createInstructor(request);
                    JOptionPane.showMessageDialog(CreateInstructorPanel.this, "Instructor created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    onBack.run();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to create instructor", ex);
                    JOptionPane.showMessageDialog(CreateInstructorPanel.this, "Error creating instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        };
        worker.execute();
    }
}
