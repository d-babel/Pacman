public class Maze {

    public static final int TILE_SIZE = 8;
    public static final int COLS = 28;
    public static final int ROWS = 31;

    private static final String[] BASE_MAP = {
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXX",
            "X............XX............X",
            "X.XXXX.XXXXX.XX.XXXXX.XXXX.X",
            "XoXXXX.XXXXX.XX.XXXXX.XXXXoX",
            "X.XXXX.XXXXX.XX.XXXXX.XXXX.X",
            "X..........................X",
            "X.XXXX.XX.XXXXXXXX.XX.XXXX.X",
            "X.XXXX.XX.XXXXXXXX.XX.XXXX.X",
            "X......XX....XX....XX......X",
            "XXXXXX.XXXXX XX XXXXX.XXXXXX",
            "     X.XXXXX XX XXXXX.X     ",
            "     X.XX          XX.X     ",
            "     X.XX XXXXXXXX XX.X     ",
            "XXXXXX.XX X      X XX.XXXXXX",
            "      .   X      X   .      ",
            "XXXXXX.XX X      X XX.XXXXXX",
            "     X.XX XXXXXXXX XX.X     ",
            "     X.XX          XX.X     ",
            "     X.XX XXXXXXXX XX.X     ",
            "XXXXXX.XX XXXXXXXX XX.XXXXXX",
            "X............XX............X",
            "X.XXXX.XXXXX.XX.XXXXX.XXXX.X",
            "X.XXXX.XXXXX.XX.XXXXX.XXXX.X",
            "Xo..XX.......  .......XX..oX",
            "XXX.XX.XX.XXXXXXXX.XX.XX.XXX",
            "XXX.XX.XX.XXXXXXXX.XX.XX.XXX",
            "X......XX....XX....XX......X",
            "X.XXXXXXXXXX.XX.XXXXXXXXXX.X",
            "X.XXXXXXXXXX.XX.XXXXXXXXXX.X",
            "X..........................X",
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    };

    private final int width;
    private final int height;
    private final int[][] map;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[height][width];

        for (int r = 0; r < height; r++) {
            String row = r < BASE_MAP.length ? BASE_MAP[r] : "";
            for (int c = 0; c < width; c++) {
                char ch = c < row.length() ? row.charAt(c) : 'X';
                this.map[r][c] = (ch == 'X' ? 1 : 0);
            }
        }
    }

    public int[][] getMap() {
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
