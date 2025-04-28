import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.event.*;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Queue;
import java.util.LinkedList;

public class GameEngine implements ActionListener, KeyListener {

    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;
    private GamePanel panel;
    private Timer timer;
    private int tileSize;
    private int xOffset;
    private int yOffset;
    private boolean phaseMode;
    private int prefDirX;
    private int prefDirY;
    private int currDirX;
    private int currDirY;

    public GameEngine(int width, int height) {
        int cols = Maze.COLS;
        int rows = Maze.ROWS;
        this.tileSize = Math.max(1, Math.min(width / cols, height / rows));
        this.xOffset = (width - cols * tileSize) / 2;
        this.yOffset = (height - rows * tileSize) / 2;
        int startX = xOffset + (cols * tileSize) / 2 - tileSize / 2;
        int startY = yOffset + (rows * tileSize) / 2 - tileSize / 2;

        pacman = new Pacman(startX, startY, tileSize);
        ghosts = new ArrayList<Ghost>();
        maze = new Maze(Maze.COLS, Maze.ROWS);
        level = 1;
        gameOver = false;
        gameWon = false;
    }
    private boolean collides(int testX, int testY) {
        int pw = pacman.getWidth();
        int ph = pacman.getHeight();
        int left = testX;
        int right = testX + pw - 1;
        int top = testY;
        int bottom = testY + ph - 1;
        int leftCol = (left - xOffset) / tileSize;
        int rightCol = (right - xOffset) / tileSize;
        int topRow = (top - yOffset) / tileSize;
        int bottomRow = (bottom - yOffset) / tileSize;
        int[][] map = maze.getMap();
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] == 1) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean canMove(int dirX, int dirY) {
        if (dirX == 0 && dirY == 0) {
            return false;
        }
        return !collides(pacman.getX() + dirX, pacman.getY() + dirY);
    }
    private void snapPacmanToNearestFree() {
        int[][] map = maze.getMap();
        int rows = map.length;
        int cols = map[0].length;
        int pw = pacman.getWidth();
        int ph = pacman.getHeight();
        int cx = pacman.getX() + pw / 2;
        int cy = pacman.getY() + ph / 2;
        int startCol = (cx - xOffset) / tileSize;
        int startRow = (cy - yOffset) / tileSize;
        if (startRow < 0) startRow = 0;
        else if (startRow >= rows) startRow = rows - 1;
        if (startCol < 0) startCol = 0;
        else if (startCol >= cols) startCol = cols - 1;
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        int foundRow = -1, foundCol = -1;
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];
            if (map[r][c] == 0) {
                foundRow = r;
                foundCol = c;
                break;
            }
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                    visited[nr][nc] = true;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        if (foundRow >= 0) {
            int newX = xOffset + foundCol * tileSize;
            int newY = yOffset + foundRow * tileSize;
            pacman.setX(newX);
            pacman.setY(newY);
        }
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
        if (!phaseMode && collides(pacman.getX(), pacman.getY())) {
            snapPacmanToNearestFree();
        }
        if (prefDirX != 0 || prefDirY != 0) {
            if (phaseMode || canMove(prefDirX, prefDirY)) {
                currDirX = prefDirX;
                currDirY = prefDirY;
            }
        }
        if (!phaseMode && (currDirX != 0 || currDirY != 0)) {
            if (!canMove(currDirX, currDirY)) {
                currDirX = 0;
                currDirY = 0;
            }
        }
        pacman.setDirection(currDirX, currDirY);
        pacman.move();
        if (!phaseMode) {
            checkCollisions();
        }
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
        int px = pacman.getX();
        int py = pacman.getY();
        int pw = pacman.getWidth();
        int ph = pacman.getHeight();
        int dx = pacman.getDx();
        int dy = pacman.getDy();

        int left = px;
        int right = px + pw - 1;
        int top = py;
        int bottom = py + ph - 1;

        int leftCol = (left - xOffset) / tileSize;
        int rightCol = (right - xOffset) / tileSize;
        int topRow = (top - yOffset) / tileSize;
        int bottomRow = (bottom - yOffset) / tileSize;

        int[][] map = maze.getMap();
        boolean collision = false;
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length
                        || map[row][col] == 1) {
                    collision = true;
                    break;
                }
            }
            if (collision) {
                break;
            }
        }

        if (collision) {
            int sdx = Integer.signum(dx);
            int sdy = Integer.signum(dy);
            int newX = px;
            int newY = py;
            while (true) {
                int currLeft = newX;
                int currRight = newX + pw - 1;
                int currTop = newY;
                int currBottom = newY + ph - 1;
                int currLeftCol = (currLeft - xOffset) / tileSize;
                int currRightCol = (currRight - xOffset) / tileSize;
                int currTopRow = (currTop - yOffset) / tileSize;
                int currBottomRow = (currBottom - yOffset) / tileSize;
                boolean stillColliding = false;
                for (int row = currTopRow; row <= currBottomRow; row++) {
                    for (int col = currLeftCol; col <= currRightCol; col++) {
                        if (row < 0 || row >= map.length
                                || col < 0 || col >= map[0].length
                                || map[row][col] == 1) {
                            stillColliding = true;
                            break;
                        }
                    }
                    if (stillColliding) {
                        break;
                    }
                }
                if (!stillColliding || (sdx == 0 && sdy == 0)) {
                    break;
                }
                newX -= sdx;
                newY -= sdy;
            }
            pacman.setX(newX);
            pacman.setY(newY);
            pacman.setDx(0);
            pacman.setDy(0);
        }
    }

    private void resetGhosts() {
    }

    public void checkGameStatus() {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            phaseMode = true;
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            prefDirX = -1;
            prefDirY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            prefDirX = 1;
            prefDirY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            prefDirX = 0;
            prefDirY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            prefDirX = 0;
            prefDirY = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            phaseMode = false;
        }
    }

    public void draw(Graphics g) {
        int[][] map = maze.getMap();
        g.setColor(Color.BLUE);
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 1) {
                    g.fillRect(xOffset + c * tileSize, yOffset + r * tileSize, tileSize, tileSize);
                }
            }
        }
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
        pacman.draw(g);
    }

    public static void main(String[] args) {
        int width = 600;
        int height = 600;
        int delay = 20;

        GamePanel panel = new GamePanel();
        JFrame frame = new JFrame("");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
