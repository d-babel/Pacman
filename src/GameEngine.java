import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.Timer;

public class GameEngine implements ActionListener, KeyListener {
    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;
    private int score;
    private int totalPellets;
    private String playerName;
    private long startTime;
    private String timeString;
    private boolean phaseMode;
    private int prefDirX;
    private int prefDirY;
    private int currDirX;
    private int currDirY;
    private GamePanel panel;
    private Timer timer;
    private int tileSize;
    private int xOffset;
    private int yOffset;
    private boolean[][] pellets;
    private boolean[][] bigPellets;
    private int powerTimer = 0;
    private static final int POWER_DURATION = 400;

    private static final int GHOST_SPAWN_INTERVAL_SECONDS = 5;
    private static final double GHOST_SPEED_MULTIPLIER = 0.9;

    public GameEngine(int width, int height) {
        int cols = Maze.COLS;
        int rows = Maze.ROWS;
        this.tileSize = Math.max(1, Math.min(width / cols, height / rows));
        this.xOffset = (width - cols * tileSize) / 2;
        this.yOffset = (height - rows * tileSize) / 2;
        
        maze = new Maze(Maze.COLS, Maze.ROWS);
        
        pellets = new boolean[rows][cols];
        totalPellets = 0;
        
        int[][] map = maze.getMap();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] == 0 && !isGhostSpawnArea(r, c) && isInsideMazeBoundary(r, c)) {
                    pellets[r][c] = true;
                    totalPellets++;
                }
            }
        }
        
        score = totalPellets;
        
        int startX = 0;
        int startY = 0;
        int startRow = 0, startCol = 0;
        boolean found = false;
        for (int r = 0; r < rows && !found; r++) {
            for (int c = 0; c < cols && !found; c++) {
                if (map[r][c] == 0 && !isGhostSpawnArea(r, c)) {
                    startX = xOffset + c * tileSize;
                    startY = yOffset + r * tileSize;
                    startRow = r;
                    startCol = c;
                    found = true;
                }
            }
        }
        
        boolean[][] reachable = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        reachable[startRow][startCol] = true;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nc < 0) nc = cols - 1;
                if (nc >= cols) nc = 0;
                if (nr >= 0 && nr < rows && !reachable[nr][nc] && map[nr][nc] == 0 && !isGhostSpawnArea(nr, nc)) {
                    reachable[nr][nc] = true;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }

        int[] extraRows = {11, 17};
        for (int er : extraRows) {
            if (er >= 0 && er < rows) {
                for (int c = 10; c <= 17; c++) {
                    if (c >= 0 && c < cols && map[er][c] == 0) {
                        reachable[er][c] = true;
                    }
                }
            }
        }

        pellets = new boolean[rows][cols];
        totalPellets = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (reachable[r][c] && c != 0 && c != 27) {
                    pellets[r][c] = true;
                    totalPellets++;
                }
            }
        }
        score = totalPellets;
        
        pacman = new Pacman(startX, startY, tileSize);
        
        ghosts = new ArrayList<Ghost>();
        level = 1;
        gameOver = false;
        gameWon = false;
        
        bigPellets = new boolean[rows][cols];
        int[][] bigPelletPositions = {
            {1, 1}, {1, Maze.COLS - 2}, {Maze.ROWS - 2, 1}, {Maze.ROWS - 2, Maze.COLS - 2}
        };
        for (int[] pos : bigPelletPositions) {
            if (reachable[pos[0]][pos[1]]) {
                bigPellets[pos[0]][pos[1]] = true;
            }
        }
        
        int[][] ghostPositions = {{13, 14}, {14, 14}, {13, 15}, {14, 15}};
        List<Color> ghostColors = Arrays.asList(Color.GRAY, Color.PINK, Color.CYAN, Color.YELLOW);
        for (int i = 0; i < ghostPositions.length; i++) {
            int gx = xOffset + ghostPositions[i][0] * tileSize;
            int gy = yOffset + ghostPositions[i][1] * tileSize;
            long delaySec = (i + 1) * GHOST_SPAWN_INTERVAL_SECONDS;
            ghosts.add(new Ghost(gx, gy, tileSize, ghostColors.get(i), delaySec));
        }
    }

    public boolean isGhostSpawnArea(int row, int col) {
        return row >= 11 && row <= 19 && col >= 10 && col <= 17;
    }

    public boolean isInsideMazeBoundary(int row, int col) {
        return row >= 0 && row < Maze.ROWS && col >= 0 && col < Maze.COLS;
    }

    public boolean collides(int testX, int testY) {
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

    private boolean collides(Sprite s, int testX, int testY) {
        int w = s.getWidth();
        int h = s.getHeight();
        int left = testX;
        int right = testX + w - 1;
        int top = testY;
        int bottom = testY + h - 1;
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
    /**
     * Can the given sprite move in the given direction by the given speed?
     */
    private boolean canMove(Sprite s, int dirX, int dirY, int speed) {
        if (dirX == 0 && dirY == 0) {
            return false;
        }
        return !collides(s, s.getX() + dirX * speed, s.getY() + dirY * speed);
    }
    
    public boolean canMove(int dirX, int dirY) {
        if (dirX == 0 && dirY == 0) {
            return false;
        }
        return !collides(pacman.getX() + dirX, pacman.getY() + dirY);
    }
    
    public void snapPacmanToNearestFree() {
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
        
        int pacmanRow = (pacman.getY() - yOffset) / tileSize;
        int pacmanCol = (pacman.getX() - xOffset) / tileSize;
        
        if (pacmanRow >= 0 && pacmanRow < Maze.ROWS && 
            pacmanCol >= 0 && pacmanCol < Maze.COLS && 
            pellets[pacmanRow][pacmanCol]) {
            pellets[pacmanRow][pacmanCol] = false;
            score--;
            if (score == 0) {
                gameWon = true;
                if (panel != null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    panel.showLeaderboard(elapsed, false);
                }
            }
        }
        
        if (pacmanRow >= 0 && pacmanRow < Maze.ROWS && 
            pacmanCol >= 0 && pacmanCol < Maze.COLS && 
            bigPellets[pacmanRow][pacmanCol]) {
            bigPellets[pacmanRow][pacmanCol] = false;
            powerTimer = POWER_DURATION;
            for (Ghost ghost : ghosts) {
                ghost.setEdible(true);
            }
        }
        
        if (powerTimer > 0) {
            powerTimer--;
            if (powerTimer == 0) {
                for (Ghost ghost : ghosts) {
                    ghost.setEdible(false);
                }
            }
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
        if (pacman.getX() < xOffset) {
            pacman.setX(xOffset + (Maze.COLS - 1) * tileSize);
        } else if (pacman.getX() >= xOffset + (Maze.COLS - 1) * tileSize) {
            pacman.setX(xOffset);
        }
        if (!phaseMode) {
            checkCollisions();
        }
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        for (Ghost ghost : ghosts) {
            if (ghost.isInCage()) {
                if (elapsed >= ghost.getReleaseDelay()) {
                    ghost.setInCage(false);
                    int spawnCol = (ghost.getSpawnX() - xOffset) / tileSize;
                    int exitRow = 11;
                    ghost.setX(xOffset + spawnCol * tileSize);
                    ghost.setY(yOffset + exitRow * tileSize);
                    ghost.setDx(0);
                    ghost.setDy(0);
                } else if (elapsed >= ghost.getReleaseDelay() - 3000) {
                    ghost.setFlashing(true);
                } else {
                    ghost.setFlashing(false);
                }
            }
            int hx = ghost.getX();
            int hy = ghost.getY();
            int speed = ghost.getSpeed();
            if ((hx - xOffset) % tileSize == 0 && (hy - yOffset) % tileSize == 0) {
                int dx = 0, dy = 0;
                int px = pacman.getX();
                int py = pacman.getY();
                int deltaX = px - hx;
                int deltaY = py - hy;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    dx = Integer.signum(deltaX);
                    if (!canMove(ghost, dx, 0, speed)) {
                        dx = 0;
                        dy = Integer.signum(deltaY);
                        if (!canMove(ghost, 0, dy, speed)) {
                            dy = 0;
                        }
                    }
                } else {
                    dy = Integer.signum(deltaY);
                    if (!canMove(ghost, 0, dy, speed)) {
                        dy = 0;
                        dx = Integer.signum(deltaX);
                        if (!canMove(ghost, dx, 0, speed)) {
                            dx = 0;
                        }
                    }
                }
                ghost.setDx(dx * speed);
                ghost.setDy(dy * speed);
            }
            ghost.move();
            if (ghost.getX() < xOffset) {
                ghost.setX(xOffset + (Maze.COLS - 1) * tileSize);
            } else if (ghost.getX() >= xOffset + (Maze.COLS - 1) * tileSize) {
                ghost.setX(xOffset);
            }
            int gLeft = ghost.getX();
            int gRight = gLeft + ghost.getWidth();
            int gTop = ghost.getY();
            int gBottom = gTop + ghost.getHeight();
            int pLeft = pacman.getX();
            int pRight = pLeft + pacman.getWidth();
            int pTop = pacman.getY();
            int pBottom = pTop + pacman.getHeight();
            boolean intersect = (gRight > pLeft && gLeft < pRight && gBottom > pTop && gTop < pBottom);
            if (intersect) {
                if (!ghost.isEdible()) {
                    gameOver = true;
                    timer.stop();
                    if (panel != null) {
                        panel.showLeaderboard(elapsed, true);
                    }
                    return;
                } else {
                    ghost.setInCage(true);
                    ghost.setFlashing(false);
                    ghost.setEdible(false);
                    ghost.setDx(0);
                    ghost.setDy(0);
                    ghost.setX(ghost.getSpawnX());
                    ghost.setY(ghost.getSpawnY());
                }
            }
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

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F) {
            for (int r = 0; r < pellets.length; r++) {
                for (int c = 0; c < pellets[0].length; c++) {
                    pellets[r][c] = false;
                    bigPellets[r][c] = false;
                }
            }
            score = 0;
            gameWon = true;
            if (panel != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                panel.showLeaderboard(elapsed, false);
            }
            return;
        }
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
        
        g.setColor(Color.WHITE);
        for (int r = 0; r < pellets.length; r++) {
            for (int c = 0; c < pellets[0].length; c++) {
                if (pellets[r][c]) {
                    int pelletSize = tileSize / 4;
                    int pelletX = xOffset + c * tileSize + (tileSize - pelletSize) / 2;
                    int pelletY = yOffset + r * tileSize + (tileSize - pelletSize) / 2;
                    g.fillOval(pelletX, pelletY, pelletSize, pelletSize);
                }
            }
        }
        
        g.setColor(Color.WHITE);
        for (int r = 0; r < bigPellets.length; r++) {
            for (int c = 0; c < bigPellets[0].length; c++) {
                if (bigPellets[r][c]) {
                    int pelletSize = tileSize / 2;
                    int pelletX = xOffset + c * tileSize + (tileSize - pelletSize) / 2;
                    int pelletY = yOffset + r * tileSize + (tileSize - pelletSize) / 2;
                    g.fillOval(pelletX, pelletY, pelletSize, pelletSize);
                }
            }
        }
        
        for (Ghost ghost : ghosts) {
            if (ghost.isEdible()) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(ghost.getColor());
            }
            ghost.draw(g);
        }
        
        pacman.draw(g);
    }

    public int getScore() {
        return score;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getTimeString() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timeString = String.format("%02d:%02d", minutes, seconds);
        return timeString;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setScore(int score) {
        this.score = score;
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
