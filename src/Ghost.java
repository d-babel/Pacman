import java.awt.Color;
import java.awt.Graphics;

public class Ghost extends Sprite {
    private Color color;
    private boolean isEdible;
    private boolean inCage = true;
    private long releaseDelay;
    private static final long FLASH_DURATION = 3000;
    private static final long FLASH_INTERVAL = 500;
    private boolean flashing = false;
    private boolean flashShow = true;
    private long lastFlashTime = 0;
    private int speed;
    private int spawnX, spawnY;

    public Ghost(int x, int y, int size, Color color, long releaseDelaySeconds) {
        super(x, y, size, size);
        this.color = color;
        this.isEdible = false;
        this.spawnX = x;
        this.spawnY = y;
        this.speed = size / 4;
        this.releaseDelay = releaseDelaySeconds * 1000;
    }

    @Override
    public void draw(Graphics g) {
        if (inCage && flashing) {
            long now = System.currentTimeMillis();
            if (now - lastFlashTime >= FLASH_INTERVAL) {
                flashShow = !flashShow;
                lastFlashTime = now;
            }
            if (!flashShow) {
                return;
            }
        }
        if (isEdible) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(color);
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

    public boolean isFlashing() {
        return flashing;
    }

    public void setFlashing(boolean flashing) {
        this.flashing = flashing;
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
}
