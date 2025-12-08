package com.iiitd.erp.ui;

import com.iiitd.erp.ui.client.AuthClient;
import com.iiitd.erp.ui.client.AdminClient;
import com.iiitd.erp.ui.client.AuthClient.LoginResult;
import com.iiitd.erp.ui.client.InstructorClient;
import com.iiitd.erp.ui.client.StudentClient;
import com.iiitd.erp.ui.AdminDashboardExact;
import com.iiitd.erp.ui.InstructorDashboardWindow;
import com.iiitd.erp.ui.StudentDashboard;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;

public class LoginUI extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleComboBox;
    private final JCheckBox rememberMe;
    private final JButton loginButton;
    private final JLabel newUserLabel, forgotPasswordLabel, helpLabel;

    private final AuthClient authClient;
    private final StudentClient studentClient;
    private final InstructorClient instructorClient;
    private final String baseUrl;
    private final static Logger LOGGER = Logger.getLogger(LoginUI.class.getName());


    public LoginUI() {
        this("http://localhost:8080");
    }

    public LoginUI(String baseUrl) {
        this.baseUrl = baseUrl;
        this.authClient = new AuthClient(baseUrl);
        this.studentClient = new StudentClient(baseUrl);
        this.instructorClient = new InstructorClient(baseUrl);


        LOGGER.info("LoginUI initialized");
        setTitle("IIITD ERP Login");
        setSize(700, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(6, 59, 92));
        headerPanel.setPreferredSize(new Dimension(700, 80));
        JLabel instituteLabel = new JLabel(
                "<html><b>INDRAPRASTHA INSTITUTE OF<br>INFORMATION TECHNOLOGY DELHI</b></html>");
        instituteLabel.setForeground(Color.WHITE);
        instituteLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        instituteLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 0));
        headerPanel.add(instituteLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // CENTER
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setLayout(new GridBagLayout());

        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(new Color(16, 78, 110));
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
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 10, 0);
        loginCard.add(title, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        loginCard.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setName("username");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        loginCard.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setName("password");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(passwordField, gbc);

        // Role dropdown
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(Color.WHITE);
        loginCard.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = {"STUDENT", "INSTRUCTOR", "ADMIN"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setName("role");
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginCard.add(roleComboBox, gbc);

        // Remember Me + Help link
        gbc.gridy = 4; gbc.gridx = 0;
        rememberMe = new JCheckBox("Remember Me");
        rememberMe.setForeground(Color.WHITE);
        rememberMe.setOpaque(false);
        rememberMe.setName("remember");
        loginCard.add(rememberMe, gbc);

        gbc.gridx = 1;
        helpLabel = new JLabel("<html><u>Help & Support</u></html>");
        helpLabel.setForeground(new Color(173, 216, 230));
        helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginCard.add(helpLabel, gbc);

        // Login Button
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        loginButton = new JButton("LOGIN");
        loginButton.setName("login");
        loginButton.setBackground(new Color(70, 161, 230));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        loginCard.add(loginButton, gbc);

        centerPanel.add(loginCard);
        add(centerPanel, BorderLayout.CENTER);

        // FOOTER
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

        loginButton.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String role = Objects.requireNonNull(roleComboBox.getSelectedItem()).toString();
        LOGGER.info("Login attempt for user: " + user + " with role: " + role);

        loginButton.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                try {
                    LoginResult result = authClient.login(user, pass, role);
                    if (result.success()) {
                        LOGGER.info("Login successful for user: " + user);
                        JOptionPane.showMessageDialog(LoginUI.this,
                                result.message(),
                                "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                        openDashboard(result);
                    } else {
                        LOGGER.warning("Login failed for user: " + user + " - " + result.message());
                        JOptionPane.showMessageDialog(LoginUI.this,
                                "Login failed: " + result.message(),
                                "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    LOGGER.severe("Login error for user: " + user + " - " + ex.getMessage());
                    JOptionPane.showMessageDialog(LoginUI.this,
                            "Login error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            @Override protected void done() { loginButton.setEnabled(true); }
        };
        worker.execute();
    }

    private void openDashboard(LoginResult result) {
        LOGGER.info("Opening " + result.role() + " dashboard for user: " + result.cookieHeader());
        switch (result.role().toUpperCase()) {
            case "STUDENT" -> SwingUtilities.invokeLater(() -> {
                studentClient.setCookieHeader(result.cookieHeader());
                new StudentDashboard(studentClient, result.maintenanceMode()).setVisible(true);
            });
            case "INSTRUCTOR" -> SwingUtilities.invokeLater(() -> {
                instructorClient.setCookieHeader(result.cookieHeader());
                new InstructorDashboardWindow(instructorClient, result.maintenanceMode()).setVisible(true);
            });
            case "ADMIN" -> SwingUtilities.invokeLater(() -> {
                AdminClient admin = new AdminClient(baseUrl, result.cookieHeader());
                new AdminDashboardExact(admin, result.maintenanceMode()).setVisible(true);
            });
            default -> JOptionPane.showMessageDialog(this,
                    "Unknown role: " + result.role(), "Login", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
