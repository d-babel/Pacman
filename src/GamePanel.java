import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel {

    private int delay;
    private GameEngine engine;
    private WelcomeScreen welcomeScreen;
    private Timer gameTimer;
    private Font gameFont;
    private boolean gameStarted;
    private LeaderboardScreen leaderboardScreen;
    private List<LeaderboardScreen.LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private boolean showLeaderboard = false;
    private double scale = 0.9;

    public GamePanel() {
        setLayout(new BorderLayout());
        int h = 1000;
        int w = 1690;

        delay = 20;
        engine = new GameEngine(w, h, this, delay);
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        gameFont = new Font("Arial", Font.BOLD, 24);
        
        welcomeScreen = new WelcomeScreen();
        welcomeScreen.addStartListener(e -> startGame());
        add(welcomeScreen, BorderLayout.CENTER);
        
        gameTimer = new Timer(1000, e -> repaint());
    }

    public void startGame() {
        String playerName = welcomeScreen.getPlayerName();
        if (playerName.isEmpty()) {
            playerName = "Player";
        }
        
        remove(welcomeScreen);
        if (leaderboardScreen != null) remove(leaderboardScreen);
        engine = new GameEngine(getWidth(), getHeight(), this, 20);
        engine.setPlayerName(playerName);
        engine.setStartTime(System.currentTimeMillis());
        gameStarted = true;
        showLeaderboard = false;
        gameTimer.start();
        requestFocusInWindow();
    }

    public void showLeaderboard(long timeMillis) {
        removeAll();
        String timeString = engine.getTimeString();
        String playerName = engine.getPlayerName();

        List<LeaderboardScreen.LeaderboardEntry> entries = LeaderboardManager.loadEntries();
        entries.add(new LeaderboardScreen.LeaderboardEntry(playerName, timeString, timeMillis));
        entries.sort((a, b) -> Long.compare(a.timeMillis, b.timeMillis));
        if (entries.size() > 10) {
            entries = entries.subList(0, 10);
        }
        LeaderboardManager.saveEntries(entries);
        leaderboardEntries = entries;
        leaderboardScreen = new LeaderboardScreen(leaderboardEntries);
        leaderboardScreen.setPlayAgainListener(e -> {
            removeAll();
            add(welcomeScreen, BorderLayout.CENTER);
            gameStarted = false;
            revalidate();
            repaint();
        });
        add(leaderboardScreen, BorderLayout.CENTER);
        showLeaderboard = true;
        revalidate();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!gameStarted) {
            return;
        }
        if (showLeaderboard) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(scale, scale);
        engine.draw(g2d);

        g2d.setTransform(new java.awt.geom.AffineTransform());

        g.setFont(gameFont);
        g.setColor(Color.WHITE);
        g.drawString("Time: " + engine.getTimeString(), 20, 30);
        g.drawString("Score: " + engine.getScore(), 20, 60);
        g.drawString("Player: " + engine.getPlayerName(), 20, 90);
    }

    public void setScore(int score) {
        engine.setScore(score);
    }
}
