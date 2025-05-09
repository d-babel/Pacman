import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class GamePanel extends JPanel {

    private int delay;
    private GameEngine engine;
    private StartPanel startPanel;
    private Timer gameTimer;
    private Font gameFont;
    private boolean gameStarted;
    private double scale = 0.9;

    // Constructs the GamePanel, setting up UI and initializing the game engine.
    public GamePanel() {
        setLayout(new BorderLayout());
        int h = 1000;
        int w = 1690;

        delay = 16; 
        engine = new GameEngine(w, h, this, delay);
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        setDoubleBuffered(true);
        
        gameFont = new Font("Arial", Font.BOLD, 24);
        
        startPanel = new StartPanel();
        startPanel.addPlayListener(e -> startGame());
        add(startPanel, BorderLayout.CENTER);
        
        gameTimer = new Timer(16, e -> repaint());
        gameTimer.setCoalesce(true);
    }

    // Initializes and starts a new game session.
    public void startGame() {
        remove(startPanel);
        engine = new GameEngine(getWidth(), getHeight(), this, delay);
        engine.setStartTime(System.currentTimeMillis());
        gameStarted = true;
        gameTimer.start();
        requestFocusInWindow();
    }

    // Returns to the start screen, typically after a game ends or is quit.
    public void returnToStartScreen() {
        // Prevent returning to start screen if game is active and score is positive.
        if (engine != null) {
            if (!engine.isGameOver() && !engine.isGameWon() && engine.getScore() > 0) {
                return;
            }
        }
        
        // Reset panel to show start screen.
        removeAll();
        add(startPanel, BorderLayout.CENTER);
        gameStarted = false;
        revalidate();
        repaint();
    }

    // Overrides paintComponent to draw the game elements.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!gameStarted) {
            return; // Don't draw if game hasn't started.
        }

        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother graphics.
        // https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Apply scaling to the game graphics.
        g2d.scale(scale, scale);
        engine.draw(g2d); // Delegate drawing of game entities to the engine.

        // Reset transform to draw UI elements like score and time without scaling.
        // https://docs.oracle.com/javase/8/docs/api/java/awt/geom/AffineTransform.html
        g2d.setTransform(new AffineTransform());

        g.setFont(gameFont);
        g.setColor(Color.WHITE);
        // Draw game time and score.
        g.drawString(engine.getTimeString(), 20, 30);
        g.drawString(engine.getScoreString(), 20, 60);
    }

}