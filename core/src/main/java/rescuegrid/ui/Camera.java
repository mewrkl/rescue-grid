package rescuegrid.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

/**
 * Decides which part of the map is on screen. The map can be much bigger than
 * the window, so the camera keeps the active rescuer in the middle.
 *
 * libGDX's OrthographicCamera does the screen maths (turning map pixels into
 * screen pixels). Our Camera class decides WHERE it looks: centred on the
 * rescuer, but never past the edges of the map.
 *
 * offsetX is how many pixels of the map are scrolled off the left edge.
 * Worked example: tiles are 64 px and the view is 15 tiles wide (960 px). The
 * rescuer is at column 20, so we want them in the middle:
 *   offsetX = 20 * 64 - (960 - 64) / 2 = 1280 - 448 = 832.
 * A tile at column 25 (map x = 1600) then appears at screen x = 1600 - 832 = 768.
 */
public class Camera {

    private final int viewRows;
    private final int viewCols;
    private final int tileSize;
    private final OrthographicCamera view;
    private float offsetX;
    private float offsetY;

    public Camera(int viewRows, int viewCols, int tileSize) {
        this.viewRows = viewRows;
        this.viewCols = viewCols;
        this.tileSize = tileSize;
        this.view = new OrthographicCamera();
        // true = y grows downwards, so row 0 of the level file is at the top.
        this.view.setToOrtho(true, viewCols * tileSize, viewRows * tileSize);
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Centres the view on a position (in tiles, may be fractional while sliding),
     * without showing anything past the map edges.
     */
    public void follow(double row, double col, int mapRows, int mapCols) {
        offsetX = clamp((float) (col * tileSize - (viewCols * tileSize - tileSize) / 2.0),
                (mapCols - viewCols) * tileSize);
        offsetY = clamp((float) (row * tileSize - (viewRows * tileSize - tileSize) / 2.0),
                (mapRows - viewRows) * tileSize);
    }

    /** Keeps value between 0 and max. (If the map is smaller than the view, max < 0, so use 0.) */
    private float clamp(float value, float max) {
        if (value > max) {
            value = max;
        }
        if (value < 0) {
            value = 0;
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

    /** The first map column that is at least partly on screen. */
    public int getFirstCol() {
        return (int) Math.floor(offsetX / tileSize);
    }

    public int getFirstRow() {
        return (int) Math.floor(offsetY / tileSize);
    }

    /** True if any part of the tile at (row, col) is on screen. */
    public boolean isInView(double row, double col) {
        float x = toMapX(col) - offsetX;
        float y = toMapY(row) - offsetY;
        return x > -tileSize && x < viewCols * tileSize && y > -tileSize && y < viewRows * tileSize;
    }
}
