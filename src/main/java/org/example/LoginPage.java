package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JPanel{
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPage() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(360, 640));
        setBackground(Color.BLACK);

        // Create main container with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(80, 40, 80, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0); // Consistent spacing

        // Add title
        JLabel titleLabel = new JLabel("SmartPillow");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.WHITE);
        gbc.insets = new Insets(0, 0, 30, 0);

        // Add spacing after title
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Add login label
        JLabel loginLabel = new JLabel("Login");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 20));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLabel.setForeground(Color.WHITE);
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(loginLabel, gbc);

        // Add spacing
        mainPanel.add(loginLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.BLACK);
        formPanel.setMaximumSize(new Dimension(280, 300)); // Increased height to accommodate button
        formPanel.setPreferredSize(new Dimension(280, 300));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

       //  Username field
       JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

      usernamePanel.setBackground(Color.BLACK);
       usernamePanel.setMaximumSize(new Dimension(280, 60));


        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameLabel.setForeground(Color.WHITE);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(280, 40));
        usernameField.setMaximumSize(new Dimension(280, 40));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 8)
        ));

        // Password field
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        passwordPanel.setBackground(Color.BLACK);
        passwordPanel.setMaximumSize(new Dimension(280, 60));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setForeground(Color.WHITE);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(280, 40));
        passwordField.setPreferredSize(new Dimension(280,40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 8)
        ));

        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button - Create the button first
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setMaximumSize(new Dimension(280, 80));


        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true); // This is important!
        loginButton.setContentAreaFilled(true); // This ensures the background is filled

        loginButton.setPreferredSize(new Dimension(160, 50));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button

        // Force a border to make it visible
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(12, 40, 12, 40)
        ));


        // Add action listener to login button
        loginButton.addActionListener(new LoginButtonListener());

        // Build form panel
        // Username
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));

        // Password
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(30));

        // Button
        buttonPanel.add(loginButton);
        formPanel.add(buttonPanel);

        // Add form panel to main panel
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(formPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Action listener for login button
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Basic validation
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Please enter both username and password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Here you would typically validate credentials
            if (authenticate(username, password)) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Login successful! Welcome to SmartPillow Sleep Analysis",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Here you would typically open the main application window
            } else {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private boolean authenticate(String username, String password) {
            // Replace this with your actual authentication logic
            return !username.isEmpty() && !password.isEmpty();
        }
    }
}

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the frame
            JFrame frame = new JFrame("SmartPillow");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


            // Set mobile-like dimensions (common phone size)
            frame.setPreferredSize(new Dimension(360, 640));

            // Create and add the login page
            LoginPage loginPage = new LoginPage();
            frame.add(loginPage);

            // Configure and show the frame
            frame.pack();
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);
        });
    }
}