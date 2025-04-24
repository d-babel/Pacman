import java.awt.Color;
import java.awt.Graphics;

public class Pacman extends Sprite {
    private int speed;

    public Pacman(int x, int y, int size) {
        super(x, y, size, size);
        speed = size / 4;
    }

    public void setDirection(int dx, int dy) {
        setDx(dx * speed);
        setDy(dy * speed);
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }
    public int getSpeed() {
        return speed;
    }
}
