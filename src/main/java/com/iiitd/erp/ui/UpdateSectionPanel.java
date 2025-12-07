package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.request.SectionUpdateRequest;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.SectionResponse;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateSectionPanel extends JPanel {

    private final AdminClient admin;
    private final Runnable onBack;
    private final SectionResponse section;
    private static final Logger LOGGER = Logger.getLogger(UpdateSectionPanel.class.getName());

    private final JComboBox<InstructorResponse> cbInstructors = new JComboBox<>();
    private final JComboBox<String> cbCapacity = new JComboBox<>(new String[]{"30", "60", "100", "150"});

    public UpdateSectionPanel(AdminClient admin, Runnable onBack, SectionResponse section) {
        this.admin = admin;
        this.onBack = onBack;
        this.section = section;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 150, 20, 150));

        // Header
        JLabel title = new JLabel("Update Section: " + section.sectionId(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 30, 0));
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        cbCapacity.setSelectedItem(String.valueOf(section.capacity()));

        addField(formPanel, gbc, 0, "Instructor:", cbInstructors);
        addField(formPanel, gbc, 1, "Capacity:", cbCapacity);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        JButton updateButton = new JButton("Save Changes");
        stylePrimaryButton(updateButton);
        updateButton.addActionListener(e -> updateSection());

        JButton backButton = new JButton("Back to List");
        styleSecondaryButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        buttonPanel.add(backButton);
        buttonPanel.add(updateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadInstructors();
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
                    instructors.forEach(cbInstructors::addItem);
                    // Set the initial selection
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

        if (component instanceof JComboBox) {
            styleComboBox((JComboBox<?>) component);
        }

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
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
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void updateSection() {
        InstructorResponse selectedInstructor = (InstructorResponse) cbInstructors.getSelectedItem();
        if (selectedInstructor == null) {
            JOptionPane.showMessageDialog(this, "Please select an instructor.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SectionUpdateRequest request = SectionUpdateRequest.builder()
                .instructorCode(selectedInstructor.instructorCode())
                .semester(section.semester())
                .year(section.year())
                .capacity(Integer.parseInt(cbCapacity.getSelectedItem().toString()))
                .registrationDeadline(section.registrationDeadline())
                .active(section.active())
                .build();


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
