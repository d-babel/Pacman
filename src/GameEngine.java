import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.Timer;

public class GameEngine implements KeyListener {
    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;
    private int score;
    private int totalPellets;
    private long startTime;
    private String timeString;
    private boolean phaseMode;
    private int prefDirX;
    private int prefDirY;
    private int currDirX;
    private int currDirY;
    private GamePanel panel;
    private int tileSize;
    private int xOffset;
    private int yOffset;
    private int updateCounter = 0;
    private boolean[][] pellets;
    private boolean[][] bigPellets;
    private int powerTimer = 0;
    private static final int POWER_DURATION = 400;
    private static final int FLASH_START = 150;

    private static final int GHOST_SPAWN_INTERVAL_SECONDS = 5;
    private static final int GHOST_MOVE_FREQUENCY = 5;

    // Constructs the game engine, initializing maze, Pacman, ghosts, and pellets.
    public GameEngine(int width, int height) {
        int cols = Maze.COLS;
        int rows = Maze.ROWS;
        // Calculate tile size and offsets to center the maze.
        this.tileSize = Math.max(1, Math.min(width / cols, height / rows));
        this.xOffset = (width - cols * tileSize) / 2;
        this.yOffset = (height - rows * tileSize) / 2;
        
        maze = new Maze(Maze.COLS, Maze.ROWS);
        
        initializeWallCache();
        
        pellets = new boolean[rows][cols];
        totalPellets = initializePellets(pellets, maze.getMap(), rows, cols);
        
        // Collect all valid non-ghost-spawn starting positions for Pacman.
        ArrayList<int[]> validPositions = new ArrayList<>();
        int[][] map = maze.getMap();
        for (int r = 0; r < rows; r++) {
            // Skip rows within the ghost spawn area for Pacman's starting position.
            if (r >= 10 && r <= 20) {
                continue;
            }
            for (int c = 0; c < cols; c++) {
                if (map[r][c] == 0 && !isGhostSpawnArea(r, c)) {
                    validPositions.add(new int[]{r, c});
                }
            }
        }
        
        int startX = 0;
        int startY = 0;
        int startRow = 0, startCol = 0;
        boolean found = false;
        
        final int[][] BIG_PELLET_POSITIONS = {
            {1, 1}, {1, Maze.COLS - 2}, {Maze.ROWS - 2, 1}, {Maze.ROWS - 2, Maze.COLS - 2}
        };
        
        // Find a random valid starting position for Pacman, avoiding big pellet locations.
        if (!validPositions.isEmpty()) {
            while (!found && !validPositions.isEmpty()) {
                int randomIndex = (int)(Math.random() * validPositions.size());
                int[] position = validPositions.get(randomIndex);
                startRow = position[0];
                startCol = position[1];
                
                // Check if the chosen starting spot is a big pellet location.
                boolean isBigPellet = false;
                for (int[] pelletPos : BIG_PELLET_POSITIONS) {
                    if (pelletPos[0] == startRow && pelletPos[1] == startCol) {
                        isBigPellet = true;
                        break;
                    }
                }
                
                if (!isBigPellet) {
                    found = true;
                    startX = xOffset + startCol * tileSize;
                    startY = yOffset + startRow * tileSize;
                } else {
                    validPositions.remove(randomIndex); // Remove big pellet spot and retry.
                }
            }
        }
        
        // Determine reachable areas for pellets using BFS from Pacman's start.
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
                // Handle horizontal wrapping (tunnel effect).
                if (nc < 0) nc = cols - 1;
                if (nc >= cols) nc = 0;
                if (nr >= 0 && nr < rows && !reachable[nr][nc] && map[nr][nc] == 0 && !isGhostSpawnArea(nr, nc)) {
                    reachable[nr][nc] = true;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }

        // Manually mark specific rows within the ghost house as reachable for pellets if they are open paths.
        int[] extraRows = {11, 17}; // Rows inside ghost house area to check for pellet placement.
        for (int er : extraRows) {
            if (er >= 0 && er < rows) {
                for (int c = 10; c <= 17; c++) { // Columns within ghost house area.
                    if (c >= 0 && c < cols && map[er][c] == 0) {
                        reachable[er][c] = true;
                    }
                }
            }
        }

        // Finalize pellet placement based on reachability, excluding edge columns.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                pellets[r][c] = reachable[r][c] && c != 0 && c != 27; // Exclude tunnel entrance/exit columns.
            }
        }
        
        // Recalculate total pellets after reachability check.
        totalPellets = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (pellets[r][c]) {
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
        
        // Place big pellets only in reachable locations.
        bigPellets = new boolean[rows][cols];
        for (int[] pos : BIG_PELLET_POSITIONS) {
            if (reachable[pos[0]][pos[1]]) {
                bigPellets[pos[0]][pos[1]] = true;
            }
        }
        
        // Initialize ghosts with their starting positions, colors, and release delays.
        int[][] ghostPositions = {{13, 14}, {14, 14}, {13, 15}, {14, 15}}; // Positions within ghost house.
        List<Color> ghostColors = Arrays.asList(Color.GRAY, Color.PINK, Color.CYAN, Color.YELLOW);
        for (int i = 0; i < ghostPositions.length; i++) {
            int gx = xOffset + ghostPositions[i][0] * tileSize;
            int gy = yOffset + ghostPositions[i][1] * tileSize;
            long delaySec = (i + 1) * GHOST_SPAWN_INTERVAL_SECONDS;
            ghosts.add(new Ghost(gx, gy, tileSize, ghostColors.get(i), delaySec));
        }
    }

    // Checks if a given tile (row, col) is within the ghost spawn area.
    public boolean isGhostSpawnArea(int row, int col) {
        return row >= 11 && row <= 19 && col >= 10 && col <= 17;
    }

    // Checks if a given tile (row, col) is within the maze boundaries.
    public boolean isInsideMazeBoundary(int row, int col) {
        return row >= 0 && row < Maze.ROWS && col >= 0 && col < Maze.COLS;
    }

    private boolean[][] wallCache;
    
    // Initializes a cache for wall positions to optimize collision checks.
    private void initializeWallCache() {
        int rows = Maze.ROWS;
        int cols = Maze.COLS;
        wallCache = new boolean[rows][cols];
        int[][] map = maze.getMap();
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                wallCache[r][c] = (map[r][c] == 1); // Mark true if it's a wall.
            }
        }
    }
    
    // Checks if a given rectangular area collides with any walls using the wall cache.
    private boolean collidesWithWalls(int testX, int testY, int width, int height) {
        if (wallCache == null) {
            initializeWallCache(); // Ensure cache is initialized.
        }
        
        // Convert pixel coordinates to tile coordinates for collision checking.
        int left = testX;
        int right = testX + width - 1;
        int top = testY;
        int bottom = testY + height - 1;
        int leftCol = (left - xOffset) / tileSize;
        int rightCol = (right - xOffset) / tileSize;
        int topRow = (top - yOffset) / tileSize;
        int bottomRow = (bottom - yOffset) / tileSize;
        
        // Check if the area is outside maze boundaries.
        if (leftCol < 0 || rightCol >= Maze.COLS || topRow < 0 || bottomRow >= Maze.ROWS) {
            return true; // Collision if outside boundaries.
        }
        
        // Iterate over tiles covered by the area and check against wall cache.
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (wallCache[row][col]) {
                    return true; // Collision if any part of the area overlaps a wall tile.
                }
            }
        }
        return false; // No collision with walls.
    }
    
    // Checks if a sprite collides with walls at a test position.
    public boolean collides(int testX, int testY) {
        return collidesWithWalls(testX, testY, pacman.getWidth(), pacman.getHeight());
    }
    
    private boolean collides(Sprite s, int testX, int testY) {
        return collidesWithWalls(testX, testY, s.getWidth(), s.getHeight());
    }

    // Determines if a sprite can move in a given direction by a certain speed without colliding.
    private boolean canMove(Sprite s, int dirX, int dirY, int speed) {
        if (dirX == 0 && dirY == 0) {
            return false; // Cannot move if no direction is specified.
        }
        // Check for collision at the potential new position.
        return !collides(s, s.getX() + dirX * speed, s.getY() + dirY * speed);
    }
    
    // Determines if Pacman can move in a given direction.
    public boolean canMove(int dirX, int dirY) {
        if (dirX == 0 && dirY == 0) {
            return false;
        }
        return !collides(pacman.getX() + dirX, pacman.getY() + dirY);
    }
    
    // Snaps Pacman to the center of the nearest free (non-wall) tile using BFS.
    public void snapPacmanToNearestFree() {
        int[][] map = maze.getMap();
        int rows = map.length;
        int cols = map[0].length;
        int pw = pacman.getWidth();
        int ph = pacman.getHeight();
        // Current center of Pacman in tile coordinates.
        int cx = pacman.getX() + pw / 2;
        int cy = pacman.getY() + ph / 2;
        int startCol = (cx - xOffset) / tileSize;
        int startRow = (cy - yOffset) / tileSize;
        // Clamp starting tile to be within maze bounds.
        if (startRow < 0) startRow = 0;
        else if (startRow >= rows) startRow = rows - 1;
        if (startCol < 0) startCol = 0;
        else if (startCol >= cols) startCol = cols - 1;
        
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        int foundRow = -1, foundCol = -1;
        
        // BFS to find the closest non-wall tile.
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0], c = cell[1];
            if (map[r][c] == 0) { // Found a free tile.
                foundRow = r;
                foundCol = c;
                break;
            }
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                // Explore adjacent valid, unvisited tiles.
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                    visited[nr][nc] = true;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        // If a free tile is found, move Pacman to its center.
        if (foundRow >= 0) {
            int newX = xOffset + foundCol * tileSize;
            int newY = yOffset + foundRow * tileSize;
            pacman.setX(newX);
            pacman.setY(newY);
        }
    }

    // Constructs the game engine with UI panel integration and starts game timer.
    public GameEngine(int width, int height, GamePanel panel, int delay) {
        this(width, height);
        this.panel = panel;
        
        int logicFPS = 30;
        int logicDelay = 1000 / logicFPS;
        
        // Timer for game logic updates (e.g., movement, AI).
        Timer logicTimer = new Timer(logicDelay, e -> update());
        logicTimer.setCoalesce(true); // Combine multiple pending events.
        logicTimer.start();
        
        panel.setFocusable(true);
        panel.addKeyListener(this);
    }

    // Main game update loop, called periodically by the logicTimer.
    public void update() {
        updateCounter = (updateCounter + 1) % GHOST_MOVE_FREQUENCY; // Counter for ghost move timing.

        updatePacmanPositionAndCollisions();
        handlePelletConsumption();
        handlePowerPelletEffects();
        updatePacmanDirection();
        movePacmanAndWrapAround();
        updateGhosts();
        checkPacmanGhostCollisions();
    }

    // Ensures Pacman is not stuck in a wall if not in phase mode.
    private void updatePacmanPositionAndCollisions() {
        if (!phaseMode && collides(pacman.getX(), pacman.getY())) {
            snapPacmanToNearestFree(); // Move Pacman to a valid spot if stuck.
        }
    }

    // Handles Pacman eating pellets and big pellets.
    private void handlePelletConsumption() {
        int pacmanRow = (pacman.getY() - yOffset) / tileSize;
        int pacmanCol = (pacman.getX() - xOffset) / tileSize;

        boolean validPosition = pacmanRow >= 0 && pacmanRow < Maze.ROWS &&
                               pacmanCol >= 0 && pacmanCol < Maze.COLS;

        if (validPosition) {
            // Check for regular pellet consumption.
            if (pellets[pacmanRow][pacmanCol]) {
                pellets[pacmanRow][pacmanCol] = false;
                score--;

                // Check for game win condition (all pellets eaten).
                if (score <= 0) {
                    gameWon = true;
                    if (panel != null) {
                        panel.returnToStartScreen();
                    }
                }
            }

            // Check for big pellet consumption.
            if (bigPellets[pacmanRow][pacmanCol]) {
                bigPellets[pacmanRow][pacmanCol] = false;
                 // Activate power-up mode.
                powerTimer = POWER_DURATION;
                for (Ghost ghost : ghosts) {
                    ghost.setEdible(true);
                }
            }
        }
    }

    // Manages the power pellet effects, including timer and ghost flashing.
    private void handlePowerPelletEffects() {
        if (powerTimer > 0) {
            powerTimer--;

            // When power-up ends, revert ghosts to normal.
            if (powerTimer == 0) {
                for (Ghost ghost : ghosts) {
                    ghost.setEdible(false);
                    ghost.setEdibleFlashing(false);
                }
            // When power-up is about to end, make edible ghosts flash.
            } else if (powerTimer <= FLASH_START) {
                for (Ghost ghost : ghosts) {
                    if (ghost.isEdible() && !ghost.isEdibleFlashing()) {
                        ghost.setEdibleFlashing(true);
                    }
                }
            }
        }
    }

    // Updates Pacman's current direction based on preferred direction and wall collisions.
    private void updatePacmanDirection() {
        // If a preferred direction is set, try to switch to it.
        if (prefDirX != 0 || prefDirY != 0) {
            if (phaseMode || canMove(prefDirX, prefDirY)) {
                currDirX = prefDirX;
                currDirY = prefDirY;
            }
        }

        // If not in phase mode and current direction leads to collision, stop Pacman.
        if (!phaseMode && (currDirX != 0 || currDirY != 0)) {
            if (!canMove(currDirX, currDirY)) {
                currDirX = 0;
                currDirY = 0;
            }
        }
        pacman.setDirection(currDirX, currDirY);
    }

    // Moves Pacman and handles screen wrapping (tunnels) and wall collisions.
    private void movePacmanAndWrapAround() {
        pacman.move();
        // Handle horizontal screen wrapping.
        if (pacman.getX() < xOffset) {
            pacman.setX(xOffset + (Maze.COLS - 1) * tileSize);
        } else if (pacman.getX() >= xOffset + (Maze.COLS - 1) * tileSize) {
            pacman.setX(xOffset);
        }
        if (!phaseMode) {
            checkCollisions();
        }
    }

    // Updates all ghosts, including cage logic and movement AI.
    private void updateGhosts() {
        // Ghosts move at a fraction of Pacman's update rate.
        boolean moveGhosts = (updateCounter != 0); 
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        for (Ghost ghost : ghosts) {
            handleGhostCageLogic(ghost, elapsed);
            if (moveGhosts && !ghost.isInCage()) {
                moveSingleGhost(ghost);
            }
        }
    }

    // Manages a ghost's state within the starting cage, including release timing.
    private void handleGhostCageLogic(Ghost ghost, long elapsed) {
        if (ghost.isInCage()) {
            // Release ghost from cage if its delay has passed.
            if (elapsed >= ghost.getReleaseDelay()) {
                ghost.setInCage(false);
                int spawnCol = (ghost.getSpawnX() - xOffset) / tileSize;
                int exitRow = 11;
                ghost.setX(xOffset + spawnCol * tileSize);
                ghost.setY(yOffset + exitRow * tileSize);
                ghost.setDx(0);
                ghost.setDy(0);
            // Make ghost flash shortly before release.
            }
        }
    }

    // Determines and applies movement for a single ghost based on its AI.
    private void moveSingleGhost(Ghost ghost) {
        int hx = ghost.getX();
        int hy = ghost.getY();
        int speed = ghost.getSpeed();

        // Only change direction when ghost is aligned with a tile center.
        if ((hx - xOffset) % tileSize == 0 && (hy - yOffset) % tileSize == 0) {
            int currentRow = (hy - yOffset) / tileSize;
            int currentCol = (hx - xOffset) / tileSize;
            ghost.addVisitedTile(currentRow, currentCol); // Record visited tile for AI.

            int dx = 0, dy = 0;
            int px = pacman.getX();
            int py = pacman.getY();

            int[] possibleDx = {1, -1, 0, 0}; // Right, Left, Down, Up (order might matter for tie-breaking)
            int[] possibleDy = {0, 0, 1, -1};
            int[] directionScores = new int[4];

            calculateGhostDirectionScores(ghost, px, py, currentRow, currentCol, speed, possibleDx, possibleDy, directionScores);

            // Find the direction with the highest score.
            int bestScore = -10000;
            int bestIndex = -1;
            for (int i = 0; i < 4; i++) {
                if (directionScores[i] > bestScore) {
                    bestScore = directionScores[i];
                    bestIndex = i;
                }
            }

            if (bestIndex >= 0) {
                dx = possibleDx[bestIndex];
                dy = possibleDy[bestIndex];
            } else {
                // Fallback: if no valid scored move, try random valid directions.
                // Shuffle directions to pick a random valid one.
                for (int i = possibleDx.length - 1; i > 0; i--) {
                    int j = (int)(Math.random() * (i + 1));
                    int temp = possibleDx[i]; possibleDx[i] = possibleDx[j]; possibleDx[j] = temp;
                    temp = possibleDy[i]; possibleDy[i] = possibleDy[j]; possibleDy[j] = temp;
                }
                for (int i = 0; i < possibleDx.length; i++) {
                    if (canMove(ghost, possibleDx[i], possibleDy[i], speed)) {
                        dx = possibleDx[i];
                        dy = possibleDy[i];
                        break;
                    }
                }
            }
            ghost.setDx(dx * speed);
            ghost.setDy(dy * speed);
            ghost.setLastDirection(dx, dy); // Record this move for oscillation detection.
        }
        ghost.move();

        // Handle horizontal screen wrapping for ghosts.
        if (ghost.getX() < xOffset) {
            ghost.setX(xOffset + (Maze.COLS - 1) * tileSize);
        } else if (ghost.getX() >= xOffset + (Maze.COLS - 1) * tileSize) {
            ghost.setX(xOffset);
        }
    }

    // Calculates scores for each possible movement direction for a ghost.
    private void calculateGhostDirectionScores(Ghost ghost, int pacmanX, int pacmanY, int ghostCurrentRow, int ghostCurrentCol, int speed, int[] possibleDx, int[] possibleDy, int[] directionScores) {
        int deltaX = pacmanX - ghost.getX();
        int deltaY = pacmanY - ghost.getY();
        boolean isOscillating = ghost.getLastMoveCounter() > 2;

        for (int i = 0; i < 4; i++) {
            int testDx = possibleDx[i];
            int testDy = possibleDy[i];

            // Heavily penalize moves into walls.
            if (!canMove(ghost, testDx, testDy, speed)) {
                directionScores[i] = -1000;
                continue;
            }

            int pacmanDirectionScore = 0;
            // Score based on whether moving towards (chasing) or away (fleeing) from Pacman.
            if (ghost.isEdible()) {
                if (testDx != 0) pacmanDirectionScore += (deltaX * testDx < 0) ? 50 : -20; // Prefer moving opposite to Pacman's X.
                if (testDy != 0) pacmanDirectionScore += (deltaY * testDy < 0) ? 50 : -20; // Prefer moving opposite to Pacman's Y.
            } else { // Chasing logic.
                if (testDx != 0) pacmanDirectionScore += (deltaX * testDx > 0) ? 50 : -20; // Prefer moving towards Pacman's X.
                if (testDy != 0) pacmanDirectionScore += (deltaY * testDy > 0) ? 50 : -20; // Prefer moving towards Pacman's Y.
            }

            // Penalize reversing direction if oscillating to prevent getting stuck.
            if (isOscillating && testDx == -ghost.getLastDx() && testDy == -ghost.getLastDy()) {
                directionScores[i] = -500;
                continue;
            }

            int continuityScore = 0;
            // Reward continuing in the same direction if not oscillating.
            if (!isOscillating && testDx == ghost.getLastDx() && testDy == ghost.getLastDy()) {
                continuityScore = 10;
            }

            int destRow = ghostCurrentRow + testDy;
            int destCol = ghostCurrentCol + testDx;
            int historyPenalty = 0;
            // Penalize recently or frequently visited tiles to encourage exploration.
            if (ghost.hasVisitedFrequently(destRow, destCol, 2)) {
                historyPenalty = -100;
            } else if (ghost.hasRecentlyVisited(destRow, destCol)) {
                historyPenalty = -50;
            }
            if (ghost.isEdible()) {
                historyPenalty *= 1.5;
            }

            int randomScore = (int)(Math.random() * 15);

            directionScores[i] = pacmanDirectionScore + continuityScore + historyPenalty + randomScore;
        }
    }


    // Checks for and handles collisions between Pacman and ghosts.
    private void checkPacmanGhostCollisions() {
        if (gameOver) return;

        for (Ghost ghost : ghosts) {
            if (ghost.isInCage()) continue;

            int ghostRow = (ghost.getY() - yOffset) / tileSize;
            int ghostCol = (ghost.getX() - xOffset) / tileSize;
            int pacRow = (pacman.getY() - yOffset) / tileSize;
            int pacCol = (pacman.getX() - xOffset) / tileSize;

            // Ensure ghost is truly out of the spawn cage area before checking collision.
            boolean ghostOutOfCage = !isGhostSpawnArea(ghostRow, ghostCol);
            if (!ghostOutOfCage) {
                continue;
            }

            // Broad phase: Check if Pacman and ghost are on the same or adjacent tiles.
            // int buffer = Math.max(3, tileSize / 4); // Collision buffer size.
            // More precise collision check using bounding boxes with a buffer.
            int buffer = Math.max(3, tileSize / 4); 
            int gLeft = ghost.getX() + buffer;
            int gRight = ghost.getX() + ghost.getWidth() - buffer;
            int gTop = ghost.getY() + buffer;
            int gBottom = ghost.getY() + ghost.getHeight() - buffer;

            int pLeft = pacman.getX() + buffer;
            int pRight = pacman.getX() + pacman.getWidth() - buffer;
            int pTop = pacman.getY() + buffer;
            int pBottom = pacman.getY() + pacman.getHeight() - buffer;

            boolean coreOverlap = (gRight > pLeft && gLeft < pRight && gBottom > pTop && gTop < pBottom);

            if (coreOverlap) {
                // Narrow phase: More precise distance check if core bounding boxes overlap.
                int gCenterX = ghost.getX() + ghost.getWidth() / 2;
                int gCenterY = ghost.getY() + ghost.getHeight() / 2;
                int pCenterX = pacman.getX() + pacman.getWidth() / 2;
                int pCenterY = pacman.getY() + pacman.getHeight() / 2;

                double distance = Math.sqrt(Math.pow(gCenterX - pCenterX, 2) + Math.pow(gCenterY - pCenterY, 2));
                boolean collision = (distance <= ghost.getWidth() * 0.65);

                if (collision) {
                    if (!ghost.isEdible()) {
                        gameOver = true;
                        if (panel != null) panel.returnToStartScreen();
                        return;
                    } else {
                        ghost.setInCage(true);
                        ghost.setEdible(false);
                        ghost.setEdibleFlashing(false);
                        ghost.setDx(0);
                        ghost.setDy(0);
                        ghost.setX(ghost.getSpawnX());
                        ghost.setY(ghost.getSpawnY());
                        // score += GHOST_EAT_SCORE; // Potential future enhancement: score for eating ghosts.
                    }
                }
            }
        }
    }

    public void movePacman(int dx, int dy) {
        pacman.setDirection(dx, dy);
    }

    public void keyTyped(KeyEvent e) {
    }

    // Performs collision detection and response for Pacman against walls.
    public void checkCollisions() {
        int px = pacman.getX();
        int py = pacman.getY();
        int pw = pacman.getWidth();
        int ph = pacman.getHeight();
        int dx = pacman.getDx();
        int dy = pacman.getDy();

        boolean collision = collidesWithWalls(px, py, pw, ph);

        if (collision) {
            // If collision occurs, nudge Pacman back step-by-step until free.
            int sdx = Integer.signum(dx);
            int sdy = Integer.signum(dy);
            int newX = px;
            int newY = py;
            
            // Move Pacman back opposite to its last movement until no longer colliding.
            while (collidesWithWalls(newX, newY, pw, ph)) {
                if (sdx == 0 && sdy == 0) {
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

    // Handles key press events for game controls.
    @Override
    public void keyPressed(KeyEvent e) {
        // 'F' key: Debug feature to instantly win the game by clearing all pellets.
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
                panel.returnToStartScreen();
            }
            return;
        }
        // Space key: Debug feature to toggle phase mode (no-clip).
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            phaseMode = true;
            return;
        }
        // Arrow keys or WASD for Pacman movement.
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

    // Handles key release events, specifically for toggling phase mode off.
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            phaseMode = false; // Deactivate phase mode on space release.
        }
    }

    // Draws a single pellet or big pellet at a given tile location.
    private void drawPellet(Graphics g, int row, int col, double sizeFraction) {
        int pelletSize = (int)(tileSize * sizeFraction);
        int pelletX = xOffset + col * tileSize + (tileSize - pelletSize) / 2;
        int pelletY = yOffset + row * tileSize + (tileSize - pelletSize) / 2;
        g.fillOval(pelletX, pelletY, pelletSize, pelletSize);
    }
    
    // Main drawing method for the game, called by GamePanel.
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
        
        // Draw regular pellets.
        g.setColor(Color.WHITE);
        for (int r = 0; r < pellets.length; r++) {
            for (int c = 0; c < pellets[0].length; c++) {
                if (pellets[r][c]) {
                    drawPellet(g, r, c, 0.25);
                }
            }
        }
        
        // Draw big pellets.
        g.setColor(Color.WHITE);
        for (int r = 0; r < bigPellets.length; r++) {
            for (int c = 0; c < bigPellets[0].length; c++) {
                if (bigPellets[r][c]) {
                    drawPellet(g, r, c, 0.5);
                }
            }
        }
        
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
        
        pacman.draw(g);
    }

    public int getScore() {
        return score;
    }

    public String getTimeString() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timeString = String.format("%02d:%02d", minutes, seconds);
        return timeString;
    }
    
    public String getScoreString() {
        return "Pellets: " + score;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // Initializes the pellet locations on the map, ensuring they are not in walls or ghost spawn areas.
    private int initializePellets(boolean[][] pellets, int[][] map, int rows, int cols) {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Place pellet if tile is a path, not in ghost spawn, and within maze boundaries.
                if (map[r][c] == 0 && !isGhostSpawnArea(r, c) && isInsideMazeBoundary(r, c)) {
                    pellets[r][c] = true;
                    count++;
                }
            }
        }
        return count;
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