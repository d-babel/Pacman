import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private int width;
    private int height;
    private int delay;
    private GameEngine engine;
    private Timer timer;

    public GamePanel() {
        width = 600;
        height = 600;
        delay = 20;

        engine = new GameEngine(width, height);

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(delay, this);
        timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        engine.update();
        repaint();
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

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        int dx = 0;
        int dy = 0;

        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            dx = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            dx = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            dy = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            dy = 1;
        }

        engine.movePacman(dx, dy);
    }

    public void keyReleased(KeyEvent e) {
        engine.movePacman(0, 0);
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
