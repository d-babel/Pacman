import java.awt.Color;
import java.awt.Graphics;

public class Pacman extends Sprite {
    private int speed;

    // Constructs a new Pacman object at a specified position and size.
    public Pacman(int x, int y, int size) {
        super(x, y, size, size);
        // Pacman's movement speed is a fraction of its size.
        speed = size / 4;
    }

    // Sets Pacman's movement direction and scales it by speed.
    public void setDirection(int dx, int dy) {
        setDx(dx * speed);
        setDy(dy * speed);
    }

    // Draws Pacman on the screen.
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }

    public int getSpeed() {
        return speed;
    }
}