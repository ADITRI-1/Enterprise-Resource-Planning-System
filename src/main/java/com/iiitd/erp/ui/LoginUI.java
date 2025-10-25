package com.iiitd.erp.ui;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JCheckBox rememberMe;
    private JButton loginButton;
    private JLabel newUserLabel, forgotPasswordLabel, helpLabel;

    public LoginUI() {
        setTitle("IIITD ERP Login");
        setSize(700, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main background
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(6, 59, 92));
        headerPanel.setPreferredSize(new Dimension(700, 80));

        JLabel instituteLabel = new JLabel(
                "<html><b>INDRAPRASTHA INSTITUTE of<br>INFORMATION TECHNOLOGY DELHI</b></html>");
        instituteLabel.setForeground(Color.WHITE);
        instituteLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        instituteLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 0));
        headerPanel.add(instituteLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER PANEL =================
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setLayout(new GridBagLayout());

        JPanel loginCard = new JPanel();
        loginCard.setBackground(new Color(16, 78, 110));
        loginCard.setLayout(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(420, 300));
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("IIITD ERP Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        loginCard.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        loginCard.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        loginCard.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(passwordField, gbc);

        // Role dropdown
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(Color.WHITE);
        loginCard.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = {"Student", "Faculty", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(roleComboBox, gbc);

        // Remember Me + Help link
        gbc.gridy = 4;
        gbc.gridx = 0;
        rememberMe = new JCheckBox("Remember Me");
        rememberMe.setForeground(Color.WHITE);
        rememberMe.setOpaque(false);
        loginCard.add(rememberMe, gbc);

        gbc.gridx = 1;
        helpLabel = new JLabel("<html><u>Help & Support</u></html>");
        helpLabel.setForeground(new Color(173, 216, 230));
        helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginCard.add(helpLabel, gbc);

        // Login Button
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginButton = new JButton("LOGIN");
        loginButton.setBackground(new Color(70, 161, 230));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        loginCard.add(loginButton, gbc);

        centerPanel.add(loginCard);
        add(centerPanel, BorderLayout.CENTER);

        // ================= FOOTER =================
        JPanel footer = new JPanel(new GridLayout(2, 1));
        footer.setBackground(new Color(245, 247, 250));

        JPanel linkPanel = new JPanel();
        linkPanel.setBackground(new Color(245, 247, 250));
        newUserLabel = new JLabel("<html><u>New User Registration</u></html>");
        forgotPasswordLabel = new JLabel("<html><u>Forgot Password?</u></html>");
        newUserLabel.setForeground(new Color(0, 51, 153));
        forgotPasswordLabel.setForeground(new Color(0, 51, 153));
        linkPanel.add(newUserLabel);
        linkPanel.add(Box.createHorizontalStrut(10));
        linkPanel.add(forgotPasswordLabel);
        footer.add(linkPanel);

        JLabel copyRight = new JLabel("© 2024 IIIT Delhi   Privacy Policy   Terms & Service", SwingConstants.CENTER);
        copyRight.setForeground(Color.GRAY);
        copyRight.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(copyRight);

        add(footer, BorderLayout.SOUTH);

        // ================= ACTION LISTENER =================
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText();
                String pass = new String(passwordField.getPassword());
                String role = roleComboBox.getSelectedItem().toString();

                JOptionPane.showMessageDialog(LoginUI.this,
                        "Username: " + user + "\nPassword: " + pass + "\nRole: " + role,
                        "Login Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}