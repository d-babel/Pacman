import javax.swing.*;
import java.awt.*;
import java.awt.Toolkit;

public class GamePanel extends JPanel {

    private int delay;
    private GameEngine engine;

    public GamePanel() {
        int cols = Maze.COLS;
        int rows = Maze.ROWS;
        int tileSize = Maze.TILE_SIZE;
        int w = cols * tileSize;
        int h = rows * tileSize;
        
        delay = 20;
        engine = new GameEngine(w, h, this, delay);
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        engine.draw(g);
    }



    private void drawGhosts(Graphics g) {
    }

    private void drawStats(Graphics g) {
    }

    private void drawGameOver(Graphics g) {
    }

    private void drawGameWon(Graphics g) {
    }

}
