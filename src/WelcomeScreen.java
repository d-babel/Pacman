import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class WelcomeScreen extends JPanel {
    private JTextField nameField;
    private JButton startButton;

    public WelcomeScreen() {
        setLayout(new GridLayout(4, 1, 10, 10));
        setBackground(Color.BLACK);
        
        JLabel title = new JLabel("PACMAN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.YELLOW);
        
        JLabel nameLabel = new JLabel("Enter your name:", JLabel.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameField = new JTextField(15);
        nameField.setBackground(Color.WHITE);
        nameField.setForeground(Color.BLACK);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        
        startButton = new JButton("START");
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setBackground(Color.WHITE);
        startButton.setForeground(Color.BLACK);
        
        JPanel namePanel = new JPanel();
        namePanel.setBackground(Color.BLACK);
        namePanel.add(nameField);
        
        add(title);
        add(nameLabel);
        add(namePanel);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(startButton);
        add(buttonPanel);
    }
    
    public void addStartListener(ActionListener listener) {
        startButton.addActionListener(listener);
    }
    
    public String getPlayerName() {
        return nameField.getText().trim();
    }
} 