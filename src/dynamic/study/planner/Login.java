package dynamic.study.planner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends JFrame implements ActionListener {
    JLabel label1, label2, label3;
    JTextField textField2;
    JPasswordField passwordField3;
    JButton button1, button2, button3;
    JLabel image, iimage, image1;
    ImageIcon backgroundImageIcon;
    JPanel titleBar;
    JLabel titleLabel;

    Login() {
        
        setUndecorated(true);

        
        titleBar = new JPanel();
        titleBar.setBackground(Color.LIGHT_GRAY);
        titleBar.setPreferredSize(new Dimension(850, 30));

        titleLabel = new JLabel("Dynamic Study Planner - Login");
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

        ImageIcon ii1 = new ImageIcon(ClassLoader.getSystemResource("icon/booksvector.jpg"));
        Image ii2 = ii1.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon ii3 = new ImageIcon(ii2);
        iimage = new JLabel(ii3);
        iimage.setBounds(600, 450, 100, 100);
        iimage.setBorder(null);
        contentPane.add(iimage);

        label1 = new JLabel("Glad to have you here!");
        label1.setForeground(Color.DARK_GRAY);
        label1.setFont(new Font("Cooper Black", Font.ITALIC, 32));
        label1.setBounds(250, 155, 450, 40);
        contentPane.add(label1);

        label2 = new JLabel("Username or Email:");
        label2.setFont(new Font("Arial", Font.BOLD, 16));
        label2.setForeground(Color.DARK_GRAY);
        label2.setBounds(150, 220, 375, 30);
        contentPane.add(label2);

        textField2 = new JTextField(15);
        textField2.setBounds(325, 220, 230, 30);
        textField2.setFont(new Font("Arial", Font.BOLD, 16));
        contentPane.add(textField2);

        label3 = new JLabel("Password: ");
        label3.setFont(new Font("Arial", Font.BOLD, 16));
        label3.setForeground(Color.DARK_GRAY);
        label3.setBounds(150, 280, 375, 30);
        contentPane.add(label3);

        passwordField3 = new JPasswordField(15);
        passwordField3.setBounds(325, 280, 230, 30);
        passwordField3.setFont(new Font("Arial", Font.BOLD, 14));
        contentPane.add(passwordField3);

        button1 = new JButton("LOG IN");
        button1.setFont(new Font("Arial", Font.BOLD, 14));
        button1.setForeground(Color.WHITE);
        button1.setBackground(Color.BLACK);
        button1.setBounds(325, 330, 100, 30);
        button1.addActionListener(this);
        contentPane.add(button1);

        button2 = new JButton("CANCEL");
        button2.setFont(new Font("Arial", Font.BOLD, 14));
        button2.setForeground(Color.WHITE);
        button2.setBackground(Color.BLACK);
        button2.setBounds(450, 330, 100, 30);
        button2.addActionListener(this);
        contentPane.add(button2);

        button3 = new JButton("SIGN UP");
        button3.setFont(new Font("Arial", Font.BOLD, 14));
        button3.setForeground(Color.WHITE);
        button3.setBackground(Color.BLACK);
        button3.setBounds(325, 380, 230, 30);
        button3.addActionListener(this);
        contentPane.add(button3);

        backgroundImageIcon = new ImageIcon(ClassLoader.getSystemResource("icon/bluewh.jpg"));
        Image backgroundImage = backgroundImageIcon.getImage().getScaledInstance(850, 600, Image.SCALE_SMOOTH);
        backgroundImageIcon = new ImageIcon(backgroundImage);
        image1 = new JLabel(backgroundImageIcon);
        image1.setBounds(0, 0, 850, 620);
        contentPane.add(image1);

        setContentPane(contentPane);
        setSize(850, 620);
        setLocation(450, 100);
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
                Con con = new Con();
                String sql = "SELECT * FROM signup WHERE (username = ? OR email = ?)";
                PreparedStatement ps = con.conn.prepareStatement(sql);
                ps.setString(1, textField2.getText());
                ps.setString(2, textField2.getText());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (BCrypt.checkpw(String.valueOf(passwordField3.getPassword()), storedPassword)) {
                        String username = rs.getString("username");
                        int userId = rs.getInt("id"); // signup টেবিলের id ব্যবহার
                        new Dashboard(username, userId).setVisible(true);
                        this.setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Login failed! Incorrect password.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Login failed! User not found.");
                }
                rs.close();
                ps.close();
            } else if (e.getSource() == button2) {
                textField2.setText("");
                passwordField3.setText("");
            } else if (e.getSource() == button3) {
                new Signup().setVisible(true);
                this.setVisible(false);
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
        new Login();
    }
}
