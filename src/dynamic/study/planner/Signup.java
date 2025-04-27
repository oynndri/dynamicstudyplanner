package dynamic.study.planner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.ResultSet;
import java.sql.Statement;

public class Signup extends JFrame implements ActionListener {
    JLabel label1, label2, label3, label4, label5, label6, label7, label8, label9, image1;
    JTextField firstNameTextField, lastNameTextField, usernameTextField, emailTextField, textField4, instituteTextField;
    JPasswordField passwordField3, passwordField4;
    JButton button1, button2;
    JCheckBox termsCheckBox;
    JComboBox<String> genderComboBox;
    JLabel image, iimage;
    ImageIcon backgroundImageIcon;
    JPanel titleBar;
    JLabel titleLabel;

    Signup() {
    
        setUndecorated(true);

    
        titleBar = new JPanel();
        titleBar.setBackground(Color.LIGHT_GRAY);
        titleBar.setPreferredSize(new Dimension(850, 30));

   
        titleLabel = new JLabel("Signup - Dynamic Study Planner");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleBar.add(titleLabel);

  
        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);
        contentPane.add(titleBar);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/booksvector.jpg"));
        Image i2 = i1.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        image = new JLabel(i3);
        image.setBounds(350, 40, 100, 100);
        image.setBorder(null);
        contentPane.add(image);

        ImageIcon ii1 = new ImageIcon(ClassLoader.getSystemResource("icon/book.jpg"));
        Image ii2 = ii1.getImage().getScaledInstance(150, 100, Image.SCALE_DEFAULT);
        ImageIcon ii3 = new ImageIcon(ii2);
        iimage = new JLabel(ii3);
        iimage.setBounds(600, 450, 150, 100);
        iimage.setBorder(null);
        contentPane.add(iimage);

        label1 = new JLabel("Create a New Account");
        label1.setForeground(Color.DARK_GRAY);
        label1.setFont(new Font("Cooper Black", Font.ITALIC, 32));
        label1.setBounds(200, 155, 500, 40);
        contentPane.add(label1);

        label2 = new JLabel("First Name:");
        label2.setFont(new Font("Arial", Font.BOLD, 16));
        label2.setForeground(Color.DARK_GRAY);
        label2.setBounds(150, 220, 150, 30);
        contentPane.add(label2);

        firstNameTextField = new JTextField(15);
        firstNameTextField.setBounds(325, 220, 230, 30);
        firstNameTextField.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(firstNameTextField);

        label3 = new JLabel("Last Name:");
        label3.setFont(new Font("Arial", Font.BOLD, 16));
        label3.setForeground(Color.DARK_GRAY);
        label3.setBounds(150, 260, 150, 30);
        contentPane.add(label3);

        lastNameTextField = new JTextField(15);
        lastNameTextField.setBounds(325, 260, 230, 30);
        lastNameTextField.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(lastNameTextField);

        label4 = new JLabel("Username:");
        label4.setFont(new Font("Arial", Font.BOLD, 16));
        label4.setForeground(Color.DARK_GRAY);
        label4.setBounds(150, 300, 375, 30);
        contentPane.add(label4);

        usernameTextField = new JTextField(15);
        usernameTextField.setBounds(325, 300, 230, 30);
        usernameTextField.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(usernameTextField);

        label9 = new JLabel("Email:");
        label9.setFont(new Font("Arial", Font.BOLD, 16));
        label9.setForeground(Color.DARK_GRAY);
        label9.setBounds(150, 340, 375, 30);
        contentPane.add(label9);

        emailTextField = new JTextField(15);
        emailTextField.setBounds(325, 340, 230, 30);
        emailTextField.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(emailTextField);

        label5 = new JLabel("Password: ");
        label5.setFont(new Font("Arial", Font.BOLD, 16));
        label5.setForeground(Color.DARK_GRAY);
        label5.setBounds(150, 380, 375, 30);
        contentPane.add(label5);

        passwordField3 = new JPasswordField(15);
        passwordField3.setBounds(325, 380, 230, 30);
        passwordField3.setFont(new Font("Arial", Font.BOLD, 14));
        contentPane.add(passwordField3);

        label6 = new JLabel("Confirm Password: ");
        label6.setFont(new Font("Arial", Font.BOLD, 16));
        label6.setForeground(Color.DARK_GRAY);
        label6.setBounds(150, 420, 375, 30);
        contentPane.add(label6);

        passwordField4 = new JPasswordField(15);
        passwordField4.setBounds(325, 420, 230, 30);
        passwordField4.setFont(new Font("Arial", Font.BOLD, 14));
        contentPane.add(passwordField4);

        label7 = new JLabel("Mobile Number: ");
        label7.setFont(new Font("Arial", Font.BOLD, 16));
        label7.setForeground(Color.DARK_GRAY);
        label7.setBounds(150, 460, 375, 30);
        contentPane.add(label7);

        textField4 = new JTextField(15);
        textField4.setBounds(325, 460, 230, 30);
        textField4.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(textField4);

        label8 = new JLabel("Gender: ");
        label8.setFont(new Font("Arial", Font.BOLD, 16));
        label8.setForeground(Color.DARK_GRAY);
        label8.setBounds(150, 500, 375, 30);
        contentPane.add(label8);

        String[] genders = {"", "Male", "Female", "Other"};
        genderComboBox = new JComboBox<>(genders);
        genderComboBox.setBounds(325, 500, 230, 30);
        contentPane.add(genderComboBox);

        JLabel instituteLabel = new JLabel("Institute: ");
        instituteLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instituteLabel.setForeground(Color.DARK_GRAY);
        instituteLabel.setBounds(150, 540, 375, 30);
        contentPane.add(instituteLabel);

        instituteTextField = new JTextField(15);
        instituteTextField.setBounds(325, 540, 230, 30);
        instituteTextField.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(instituteTextField);

        termsCheckBox = new JCheckBox("I agree to the Terms and Conditions & Privacy Policy");
        termsCheckBox.setBounds(150, 580, 400, 30);
        contentPane.add(termsCheckBox);

        button1 = new JButton("SIGN UP");
        button1.setFont(new Font("Arial", Font.BOLD, 14));
        button1.setForeground(Color.WHITE);
        button1.setBackground(Color.BLACK);
        button1.setBounds(325, 620, 100, 30);
        button1.addActionListener(this);
        contentPane.add(button1);

        button2 = new JButton("CANCEL");
        button2.setFont(new Font("Arial", Font.BOLD, 14));
        button2.setForeground(Color.WHITE);
        button2.setBackground(Color.BLACK);
        button2.setBounds(450, 620, 100, 30);
        button2.addActionListener(this);
        contentPane.add(button2);

        backgroundImageIcon = new ImageIcon(ClassLoader.getSystemResource("icon/bluewh.jpg"));
        Image backgroundImage = backgroundImageIcon.getImage().getScaledInstance(850, 650, Image.SCALE_SMOOTH);
        backgroundImageIcon = new ImageIcon(backgroundImage);
        image1 = new JLabel(backgroundImageIcon);
        image1.setBounds(0, 0, 850, 650);
        contentPane.add(image1);

        setContentPane(contentPane);
        setSize(850, 650);
        setLocation(450, 50);
        setVisible(true);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Image scaledImage = backgroundImageIcon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                image1.setIcon(new ImageIcon(scaledImage));
                titleBar.setSize(getWidth(), 30);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == button1) {
                // 1. ভ্যালিডেশন চেক (আপনার আগের কোডের মতোই)
                if (firstNameTextField.getText().isEmpty() || lastNameTextField.getText().isEmpty() ||
                        usernameTextField.getText().isEmpty() || emailTextField.getText().isEmpty() ||
                        passwordField3.getPassword().length == 0 || passwordField4.getPassword().length == 0 ||
                        textField4.getText().isEmpty() || genderComboBox.getSelectedIndex() == 0 ||
                        instituteTextField.getText().isEmpty() || !termsCheckBox.isSelected()) {

                    JOptionPane.showMessageDialog(this, "Please fill in all fields and accept the terms and conditions.");
                    return;
                }

                // 2. পাসওয়ার্ড ম্যাচিং চেক
                if (!String.valueOf(passwordField3.getPassword()).equals(String.valueOf(passwordField4.getPassword()))) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match.");
                    return;
                }

                Con con = new Con();

                // 3. ইউনিক ইউজারনেম/ইমেইল চেক (নতুন যোগ করা)
                String checkSql = "SELECT id FROM signup WHERE username = ? OR email = ?";
                PreparedStatement checkPs = con.conn.prepareStatement(checkSql);
                checkPs.setString(1, usernameTextField.getText());
                checkPs.setString(2, emailTextField.getText());

                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username or email already exists!");
                    return;
                }

                // 4. সাইনআপ প্রসেস (আপনার আগের কোড + user_id হ্যান্ডলিং)
                String insertSql = "INSERT INTO signup (first_name, last_name, username, email, password, mobile_number, gender, institute) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement insertPs = con.conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

                insertPs.setString(1, firstNameTextField.getText());
                insertPs.setString(2, lastNameTextField.getText());
                insertPs.setString(3, usernameTextField.getText());
                insertPs.setString(4, emailTextField.getText());

                String plainPassword = String.valueOf(passwordField3.getPassword());
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                insertPs.setString(5, hashedPassword);

                insertPs.setString(6, textField4.getText());
                insertPs.setString(7, (String) genderComboBox.getSelectedItem());
                insertPs.setString(8, instituteTextField.getText());

                int rowsInserted = insertPs.executeUpdate();

                if (rowsInserted > 0) {
                    // নতুন যোগ করা: জেনারেটেড ইউজার আইডি পাওয়া
                    ResultSet generatedKeys = insertPs.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newUserId = generatedKeys.getInt(1);
                        JOptionPane.showMessageDialog(this, "Signup successful! Your User ID: " + newUserId);
                    }

                    new Login().setVisible(true);
                    this.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Signup failed!");
                }

                insertPs.close();
                checkPs.close();

            } else if (e.getSource() == button2) {
                // ফর্ম ক্লিয়ার (আপনার আগের কোডের মতোই)
                firstNameTextField.setText("");
                lastNameTextField.setText("");
                usernameTextField.setText("");
                emailTextField.setText("");
                passwordField3.setText("");
                passwordField4.setText("");
                textField4.setText("");
                genderComboBox.setSelectedIndex(0);
                instituteTextField.setText("");
                termsCheckBox.setSelected(false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new Signup();
    }
}
