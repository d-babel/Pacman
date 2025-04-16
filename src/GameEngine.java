public class GameEngine {
    private PacMan pacman;
    private ArrayList<Ghost> ghosts;
    private Maze maze;
    private int level;
    private boolean gameOver;
    private boolean gameWon;

    public GameEngine(int width, int height) {}

    private void initializeGhosts() {}

    public void update() {}

    public void movePacman(int dx, int dy) {}

    public void checkCollisions() {}

    private void resetGhosts() {}

    public void checkGameStatus() {}

    public void nextLevel() {}

    public PacMan getPacman() { return null; }

    public ArrayList<Ghost> getGhosts() { return null; }

    public Maze getMaze() { return null; }

    public int getLevel() { return 0; }

    public boolean isGameOver() { return false; }

    public boolean isGameWon() { return false; }
}
