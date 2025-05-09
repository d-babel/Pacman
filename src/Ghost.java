import java.awt.Color;
import java.awt.Graphics;

public class Ghost extends Sprite {
    private Color color;
    private boolean isEdible;
    private boolean inCage = true;
    private long releaseDelay;
    private static final long FLASH_INTERVAL = 250;
    
    private boolean edibleFlashing = false;
    private boolean edibleFlashShow = true;
    private long edibleLastFlashTime = 0;
    private static final long FLASH_CHECK_INTERVAL = 100;
    private long lastFlashCheck = 0;
    
    private int speed;
    private int spawnX, spawnY;
    private int lastDx = 0;
    private int lastDy = 0;
    private int lastMoveCounter = 0;
    private int[][] recentTiles = new int[3][2];
    private int tileHistoryIndex = 0;

    // Constructs a new Ghost with specified parameters including its release delay from the cage.
    public Ghost(int x, int y, int size, Color color, long releaseDelaySeconds) {
        super(x, y, size, size);
        this.color = color;
        this.isEdible = false;
        this.spawnX = x;
        this.spawnY = y;
        this.speed = size / 4;
        this.releaseDelay = releaseDelaySeconds * 1000;
        
        for (int i = 0; i < recentTiles.length; i++) {
            recentTiles[i] = new int[]{-1, -1};
        }
    }
    
    // Draws the ghost, handling its appearance based on state (e.g., edible, flashing).
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();
        
        // Throttle flash updates to avoid excessive calculations.
        if (now - lastFlashCheck >= FLASH_CHECK_INTERVAL && isEdible && edibleFlashing) {
            if (now - edibleLastFlashTime >= FLASH_INTERVAL) {
                edibleFlashShow = !edibleFlashShow;
                edibleLastFlashTime = now;
            }
            lastFlashCheck = now;
        }
        
        // Set color based on whether the ghost is edible or its power-up is flashing.
        if (isEdible) {
            if (edibleFlashing) {
                g.setColor(edibleFlashShow ? Color.BLUE : Color.WHITE); // Alternate colors for flashing edible ghost
            } else {
                g.setColor(Color.BLUE); // Standard edible color
            }
        } else {
            g.setColor(color); // Normal ghost color
        }
        
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }

    public boolean isEdible() {
        return isEdible;
    }

    public void setEdible(boolean edible) {
        this.isEdible = edible;
    }

    public Color getColor() {
        return color;
    }
    
    public boolean isInCage() {
        return inCage;
    }

    public void setInCage(boolean inCage) {
        this.inCage = inCage;
    }

    public long getReleaseDelay() {
        return releaseDelay;
    }


    public int getSpeed() {
        return speed;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }
    
    public boolean isEdibleFlashing() {
        return edibleFlashing;
    }
    
    // Sets the edible flashing state, usually when Pacman's power-up is about to expire.
    public void setEdibleFlashing(boolean edibleFlashing) {
        this.edibleFlashing = edibleFlashing;
        if (edibleFlashing) {
            edibleLastFlashTime = System.currentTimeMillis();
            edibleFlashShow = true;
        }
    }
    
    public int getLastDx() {
        return lastDx;
    }
    
    public int getLastDy() {
        return lastDy;
    }
    
    // Updates the ghost's last movement direction and tracks consecutive moves in the same direction.
    public void setLastDirection(int dx, int dy) {
        // Increment counter if moving in the same direction, otherwise reset.
        if (dx == lastDx && dy == lastDy) {
            lastMoveCounter++;
        } else {
            lastMoveCounter = 0;
            lastDx = dx;
            lastDy = dy;
        }
    }
    
    public int getLastMoveCounter() {
        return lastMoveCounter;
    }
    
    // Records a tile visited by the ghost in a circular buffer for history tracking.
    public void addVisitedTile(int row, int col) {
        recentTiles[tileHistoryIndex][0] = row;
        recentTiles[tileHistoryIndex][1] = col;
        // Cycle through the history buffer.
        tileHistoryIndex = (tileHistoryIndex + 1) % recentTiles.length;
    }
    
    // Checks if a given tile matches a specific row and column.
    private boolean isValidTile(int[] tile, int row, int col) {
        return tile[0] >= 0 && tile[1] >= 0 && tile[0] == row && tile[1] == col;
    }
    
    // Determines if the ghost has visited a specific tile recently (at least minCount times).
    public boolean hasRecentlyVisited(int row, int col) {
        return countVisits(row, col) > 0;
    }
    
    // Checks if the ghost has visited a specific tile frequently (at least minCount times).
    public boolean hasVisitedFrequently(int row, int col, int minCount) {
        return countVisits(row, col) >= minCount;
    }
    
    // Helper method to count tile visits
    private int countVisits(int row, int col) {
        int count = 0;
        for (int[] tile : recentTiles) {
            if (isValidTile(tile, row, col)) {
                count++;
            }
        }
        return count;
    }
}