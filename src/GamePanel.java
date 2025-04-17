import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private int width;
    private int height;
    private int delay;
    private GameEngine engine;

    public GamePanel() {
        width = 600;
        height = 600;
        delay = 20;
        engine = new GameEngine(width, height, this, delay);

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPacman(g);
    }

    private void drawPacman(Graphics g) {
        engine.getPacman().draw(g);
    }

    private void drawMaze(Graphics g) {
    }

    private void drawGhosts(Graphics g) {
    }

    private void drawStats(Graphics g) {
    }

    private void drawGameOver(Graphics g) {
    }

    private void drawGameWon(Graphics g) {
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man Proof-of-Concept");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new GamePanel());
        frame.pack();
        frame.setVisible(true);
    }
}
