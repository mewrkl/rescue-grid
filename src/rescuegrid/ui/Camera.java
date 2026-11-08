package rescuegrid.ui;

import rescuegrid.model.rescuers.Rescuer;

/**
 * Decides which part of the map is on screen. The map can be much bigger than
 * the window, so the camera keeps the active rescuer in the middle.
 *
 * Worked example: the map is 40 columns wide and the view is 15 wide. The
 * rescuer is at column 20, so offsetCol = 20 - 7 = 13. A tile at column 25 is
 * drawn at x = (25 - 13) * 64 = 768 pixels.
 */
public class Camera {

    private final int viewRows;
    private final int viewCols;
    private final int tileSize;
    private int offsetRow;
    private int offsetCol;

    public Camera(int viewRows, int viewCols, int tileSize) {
        this.viewRows = viewRows;
        this.viewCols = viewCols;
        this.tileSize = tileSize;
        this.offsetRow = 0;
        this.offsetCol = 0;
    }

    /** Centres the view on the rescuer, without showing anything past the map edges. */
    public void follow(Rescuer rescuer, int mapRows, int mapCols) {
        offsetRow = clamp(rescuer.getRow() - viewRows / 2, mapRows - viewRows);
        offsetCol = clamp(rescuer.getCol() - viewCols / 2, mapCols - viewCols);
    }

    /** Keeps value between 0 and max. (If the map is smaller than the view, max < 0, so use 0.) */
    private int clamp(int value, int max) {
        if (value > max) {
            value = max;
        }
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    public boolean isInView(int row, int col) {
        return row >= offsetRow && row < offsetRow + viewRows
                && col >= offsetCol && col < offsetCol + viewCols;
    }

    /** Pixel x on screen for a map column. */
    public int toScreenX(int col) {
        return (col - offsetCol) * tileSize;
    }

    /** Pixel y on screen for a map row. */
    public int toScreenY(int row) {
        return (row - offsetRow) * tileSize;
    }

    public int getOffsetRow() {
        return offsetRow;
    }

    public int getOffsetCol() {
        return offsetCol;
    }
}
