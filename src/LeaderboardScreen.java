import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class LeaderboardScreen extends JPanel {
    private List<LeaderboardEntry> leaderboard;
    private JButton playAgainButton;
    private Font gameFont;
    private ActionListener playAgainListener;

    public LeaderboardScreen(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        gameFont = new Font("Arial", Font.BOLD, 24);

        playAgainButton = new JButton("PLAY AGAIN");
        playAgainButton.setFont(gameFont);
        playAgainButton.setBackground(Color.WHITE);
        playAgainButton.setForeground(Color.BLACK);
        playAgainButton.setFocusPainted(false);
        playAgainButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        playAgainButton.addActionListener(e -> {
            if (playAgainListener != null) playAgainListener.actionPerformed(e);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(playAgainButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setPlayAgainListener(ActionListener listener) {
        this.playAgainListener = listener;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(gameFont);
        g.setColor(Color.WHITE);
        
        String title = "LEADERBOARD";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, 60);

        int y = 120;
        g.drawString("Rank   Name         Time", 100, y);
        y += 40;
        
        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            String line = String.format("%2d.   %-10s   %s", i + 1, entry.name, entry.timeString);
            g.drawString(line, 100, y);
            y += 35;
        }
    }

    public static class LeaderboardEntry {
        public String name;
        public String timeString;
        public long timeMillis;
        
        public LeaderboardEntry(String name, String timeString, long timeMillis) {
            this.name = name;
            this.timeString = timeString;
            this.timeMillis = timeMillis;
        }
    }
} 