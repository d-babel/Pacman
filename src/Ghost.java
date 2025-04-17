import java.awt.Color;
import java.awt.Graphics;

public class Ghost extends Sprite {
    private Color color;
    private boolean isEdible;

    public Ghost(int x, int y, int size, Color color) {
        super(x, y, size, size);
        this.color = color;
        this.isEdible = false;
    }

    public void draw(Graphics g) {
        g.setColor(color);
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
}
