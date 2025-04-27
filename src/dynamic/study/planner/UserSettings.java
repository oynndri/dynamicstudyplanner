package dynamic.study.planner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class UserSettings {
    private int userId;
    private Dashboard dashboard;
    private JPanel settingsPanel;
    private Map<String, JTextField> fieldMap = new HashMap<>();
    private JComboBox<String> genderCombo;

    public UserSettings(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;
        initializeUI();
    }

    private void initializeUI() {
        settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(dashboard.secondaryColor);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(dashboard.secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Account Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));

        JLabel infoLabel = new JLabel("Update your personal information");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(120, 120, 120));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(infoLabel, BorderLayout.EAST);
        settingsPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(dashboard.secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        JTextField firstNameField = new JTextField(20);
        fieldMap.put("first_name", firstNameField);
        formPanel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        JTextField lastNameField = new JTextField(20);
        fieldMap.put("last_name", lastNameField);
        formPanel.add(lastNameField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        fieldMap.put("username", usernameField);
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        fieldMap.put("email", emailField);
        formPanel.add(emailField, gbc);

        // Password (with show/hide toggle)
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        JButton showPasswordBtn = new JButton("👁");
        showPasswordBtn.setMargin(new Insets(0, 5, 0, 5));
        showPasswordBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
                showPasswordBtn.setText("👁");
            } else {
                passwordField.setEchoChar('\u2022');
                showPasswordBtn.setText("👁");
            }
        });
        passwordPanel.add(showPasswordBtn, BorderLayout.EAST);
        fieldMap.put("password", passwordField);
        formPanel.add(passwordPanel, gbc);

        // Mobile Number
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Mobile Number:"), gbc);

        gbc.gridx = 1;
        JTextField mobileField = new JTextField(20);
        fieldMap.put("mobile_number", mobileField);
        formPanel.add(mobileField, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Gender:"), gbc);

        gbc.gridx = 1;
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(genderCombo, gbc);

        // Institute
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Institute:"), gbc);

        gbc.gridx = 1;
        JTextField instituteField = new JTextField(20);
        fieldMap.put("institute", instituteField);
        formPanel.add(instituteField, gbc);

        // Registration Date (read-only)
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Registration Date:"), gbc);

        gbc.gridx = 1;
        JTextField regDateField = new JTextField(20);
        regDateField.setEditable(false);
        fieldMap.put("registration_date", regDateField);
        formPanel.add(regDateField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(dashboard.secondaryColor);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dashboard.updateContentPanel("Settings"));

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(dashboard.primaryColor);
        saveBtn.setForeground(dashboard.secondaryColor);
        saveBtn.addActionListener(e -> saveUserSettings());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        settingsPanel.add(scrollPane, BorderLayout.CENTER);

        // Load user data if not in guest mode
        if (userId > 0) {
            loadUserData();
        } else {
            setGuestMode();
        }
    }

    private void loadUserData() {
        Map<String, String> userData = DatabaseHelper.getUserData(userId);
        if (userData != null) {
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (fieldMap.containsKey(key)) {
                    fieldMap.get(key).setText(value);
                } else if (key.equals("gender")) {
                    genderCombo.setSelectedItem(value);
                }
            }
        }
    }

    private void setGuestMode() {
        for (JTextField field : fieldMap.values()) {
            field.setEnabled(false);
            field.setBackground(new Color(240, 240, 240));
        }
        genderCombo.setEnabled(false);

        JOptionPane.showMessageDialog(settingsPanel,
                "Please login to access account settings",
                "Guest Mode",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveUserSettings() {
        if (userId == 0) {
            JOptionPane.showMessageDialog(settingsPanel,
                    "Guest users cannot save settings. Please login first.",
                    "Guest Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> userData = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            userData.put(entry.getKey(), entry.getValue().getText());
        }
        userData.put("gender", (String) genderCombo.getSelectedItem());

        try {
            boolean success = DatabaseHelper.updateUserData(userId, userData);
            if (success) {
                JOptionPane.showMessageDialog(settingsPanel,
                        "Your account information has been updated successfully!",
                        "Update Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // Update dashboard username if username was changed
                if (fieldMap.containsKey("username")) {
                    dashboard.updateUsername(fieldMap.get("username").getText());
                }
            } else {
                JOptionPane.showMessageDialog(settingsPanel,
                        "Failed to update account information. Please try again.",
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(settingsPanel,
                    "Error updating account: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return settingsPanel;
    }
}
