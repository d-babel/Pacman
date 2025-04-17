import java.util.ArrayList;

public class GameEngine {
    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;

    public GameEngine(int width, int height) {
        pacman = new Pacman(width / 2, height / 2, 30);
        ghosts = new ArrayList<Ghost>();
        maze = new Maze(width, height);
        level = 1;
        gameOver = false;
        gameWon = false;
    }

    public void update() {
        pacman.move();
    }

    public void movePacman(int dx, int dy) {
        pacman.setDirection(dx, dy);
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

    public void nextLevel() {
    }
}
