package rescuegrid.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

/**
 * Keeps the active rescuer in the middle of the view without scrolling past
 * the map edges, except at the top: there the view can go a few rows past the
 * edge, so the objective card (top-left) never covers the top rows of the map,
 * where the exit often is. libGDX's OrthographicCamera does the projection.
 */
public class Camera {

    private final int viewRows;
    private final int viewCols;
    private final int tileSize;
    private final int topMarginRows;
    private final OrthographicCamera view;
    private float offsetX;
    private float offsetY;
    private float bottomCover; // pixels at the bottom of the view hidden by a card (the training prompt)

    /** topMarginRows: how many empty rows the view may show above row 0. */
    public Camera(int viewRows, int viewCols, int tileSize, int topMarginRows) {
        this.viewRows = viewRows;
        this.viewCols = viewCols;
        this.tileSize = tileSize;
        this.topMarginRows = topMarginRows;
        this.view = new OrthographicCamera();
        // true = y grows downwards, so row 0 of the level file is at the top.
        this.view.setToOrtho(true, viewCols * tileSize, viewRows * tileSize);
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Something covers the bottom `pixels` of the view (the training card). The
     * camera then centres the rescuer in the part above it, and may scroll past
     * the bottom of the map so the last rows can be seen above the card.
     */
    public void setBottomCover(float pixels) {
        bottomCover = Math.max(0, pixels);
    }

    public float getBottomCover() {
        return bottomCover;
    }

    /**
     * Centres the view on a position (in tiles, may be fractional while sliding),
     * without showing anything past the map edges (apart from the top margin, and
     * the bottom cover).
     */
    public void follow(double row, double col, int mapRows, int mapCols) {
        float shownHeight = viewRows * tileSize - bottomCover;
        offsetX = clamp((float) (col * tileSize - (viewCols * tileSize - tileSize) / 2.0),
                0, (mapCols - viewCols) * tileSize);
        offsetY = clamp((float) (row * tileSize - (shownHeight - tileSize) / 2.0),
                -topMarginRows * tileSize, mapRows * tileSize - shownHeight);
    }

    /** Keeps value between min and max. (If the map is smaller than the view, max < min, so use min.) */
    private float clamp(float value, float min, float max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    /**
     * Points the libGDX camera at the current offset, plus a shake (in pixels).
     * Positions are rounded to whole pixels so tiles never land between pixels.
     */
    public Matrix4 getProjection(float shakeX, float shakeY) {
        view.position.set(Math.round(offsetX + shakeX) + viewCols * tileSize / 2f,
                Math.round(offsetY + shakeY) + viewRows * tileSize / 2f, 0);
        view.update();
        return view.combined;
    }

    /** Where a column is on the map, in pixels. The camera turns that into a screen position. */
    public float toMapX(double col) {
        return (float) (col * tileSize);
    }

    public float toMapY(double row) {
        return (float) (row * tileSize);
    }

    /** Where a column is on the screen right now, in pixels (can be off screen). */
    public float toScreenX(double col) {
        return toMapX(col) - offsetX;
    }

    public float toScreenY(double row) {
        return toMapY(row) - offsetY;
    }

    /** The first map column that is at least partly on screen. */
    public int getFirstCol() {
        return Math.max(0, (int) Math.floor(offsetX / tileSize));
    }

    /** The first map row on screen (never below 0, even while the top margin shows). */
    public int getFirstRow() {
        return Math.max(0, (int) Math.floor(offsetY / tileSize));
    }

    /** True if any part of the tile at (row, col) is on screen and not behind the bottom cover. */
    public boolean isInView(double row, double col) {
        float x = toMapX(col) - offsetX;
        float y = toMapY(row) - offsetY;
        return x > -tileSize && x < viewCols * tileSize && y > -tileSize && y < viewRows * tileSize - bottomCover;
    }
}
