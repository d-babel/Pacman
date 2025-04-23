import javax.swing.*;
import java.awt.*;
import java.awt.Toolkit;

public class GamePanel extends JPanel {
    private static final double MAP_ZOOM = 0.9;      // map tiles scaled to 90%
    private static final double PACMAN_ZOOM = 0.75;  // pacman scaled to 75% of map tile
    private int delay;
    private GameEngine engine;

    public GamePanel() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int h = screen.height;
        int w = h * 3 / 2;
        if (w > screen.width) {
            w = screen.width;
            h = w * 2 / 3;
        }
        delay = 20;
        engine = new GameEngine(w, h, this, delay);
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.BLACK);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMaze(g);
        drawPacman(g);
    }

    private void drawPacman(Graphics g) {
        Pacman p = engine.getPacman();
        int px = p.getX(), py = p.getY();
        int pw = p.getWidth(), ph = p.getHeight();
        int panelW = getWidth(), panelH = getHeight();
        int[][] map = engine.getMaze().getMap();
        int cols = map[0].length, rows = map.length;
        int fullTile = Math.min(panelW / cols, panelH / rows);
        int mapTile = (int)(fullTile * MAP_ZOOM);
        double scaleMap = (double) mapTile / fullTile;
        int fullOffsetX = (panelW - fullTile * cols) / 2;
        int fullOffsetY = (panelH - fullTile * rows) / 2;
        int mapOffsetX = (panelW - mapTile * cols) / 2;
        int mapOffsetY = (panelH - mapTile * rows) / 2;
        double relX = px - fullOffsetX;
        double relY = py - fullOffsetY;
        double baseX = mapOffsetX + relX * scaleMap;
        double baseY = mapOffsetY + relY * scaleMap;
        double baseW = pw * scaleMap;
        double baseH = ph * scaleMap;
        double drawW = baseW * PACMAN_ZOOM;
        double drawH = baseH * PACMAN_ZOOM;
        double drawX = baseX + (baseW - drawW) / 2;
        double drawY = baseY + (baseH - drawH) / 2;
        g.setColor(Color.RED);
        g.fillOval((int)Math.round(drawX), (int)Math.round(drawY),
                   (int)Math.round(drawW), (int)Math.round(drawH));
    }

    private void drawMaze(Graphics g) {
        int[][] map = engine.getMaze().getMap();
        int panelW = getWidth();
        int panelH = getHeight();
        int cols = map[0].length;
        int rows = map.length;
        int fullTile = Math.min(panelW / cols, panelH / rows);
        int mapTile = (int)(fullTile * MAP_ZOOM);
        int xOffset = (panelW - mapTile * cols) / 2;
        int yOffset = (panelH - mapTile * rows) / 2;
        g.setColor(Color.BLUE);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] == 1) {
                    int x = xOffset + c * mapTile;
                    int y = yOffset + r * mapTile;
                    g.fillRect(x, y, mapTile, mapTile);
                }
            }
        }
    }

    private void drawGhosts(Graphics g) {
    }

    private void drawStats(Graphics g) {
    }

    private void drawGameOver(Graphics g) {
    }

    private void drawGameWon(Graphics g) {
    }


}