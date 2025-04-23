import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.event.*;
import javax.swing.JFrame;

public class GameEngine implements ActionListener, KeyListener {
    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;
    private GamePanel panel;
    private Timer timer;

    public GameEngine(int width, int height) {
        // Compute square tile size and map offsets for centering
        int cols = Maze.COLS;
        int rows = Maze.ROWS;
        int tileSize = Math.min(width / cols, height / rows);
        int xOffset = (width - cols * tileSize) / 2;
        int yOffset = (height - rows * tileSize) / 2;
        // Initialize Pacman at center of the map
        int startX = xOffset + (cols * tileSize) / 2 - tileSize / 2;
        int startY = yOffset + (rows * tileSize) / 2 - tileSize / 2;
        pacman = new Pacman(startX, startY, tileSize);
        ghosts = new ArrayList<Ghost>();
        maze = new Maze(Maze.COLS, Maze.ROWS);
        level = 1;
        gameOver = false;
        gameWon = false;
    }
    public GameEngine(int width, int height, GamePanel panel, int delay) {
        this(width, height);
        this.panel = panel;
        timer = new Timer(delay, this);
        timer.start();
        panel.setFocusable(true);
        panel.addKeyListener(this);
    }

    public void update() {
        pacman.move();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        if (panel != null) {
            panel.repaint();
        }
    }

    public void movePacman(int dx, int dy) {
        pacman.setDirection(dx, dy);
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }

    public Pacman getPacman() {
        return pacman;
    }

    public ArrayList<Ghost> getGhosts() {
        return ghosts;
    }

    public Maze getMaze() {
        return maze;
    }

    public int getLevel() {
        return level;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    private void initializeGhosts() {
    }

    public void checkCollisions() {
    }

    private void resetGhosts() {
    }

    public void checkGameStatus() {
    }

    @Override
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
        movePacman(dx, dy);
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        int width = 600;
        int height = 600;
        int delay = 20;
        GamePanel panel = new GamePanel();
        JFrame frame = new JFrame("Pacman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}