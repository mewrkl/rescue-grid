package rescuegrid.model;

/**
 * The four directions a rescuer can walk or face.
 *
 * Each direction knows how it changes the row and column, so movement code can
 * write "row + direction.getRowChange()" instead of four separate if-statements.
 */
public enum Direction {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int rowChange;
    private final int colChange;

    Direction(int rowChange, int colChange) {
        this.rowChange = rowChange;
        this.colChange = colChange;
    }

    public int getRowChange() {
        return rowChange;
    }

    public int getColChange() {
        return colChange;
    }
}
