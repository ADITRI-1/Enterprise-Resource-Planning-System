package com.iiitd.erp.ui;

import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    private final JTextField[] fields;
    private boolean confirmed = false;

    public UserDialog(JFrame parent, String title, String[] labels, String[] values) {
        super(parent, title, true);
        fields = new JTextField[labels.length];
        JPanel panel = new JPanel(new GridLayout(labels.length + 1, 2, 5, 5));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            fields[i] = new JTextField(values != null ? values[i] : "");
            panel.add(fields[i]);
        }

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        panel.add(okButton);
        panel.add(cancelButton);
        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    public String[] getValues() {
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = fields[i].getText();
        }
        return values;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
