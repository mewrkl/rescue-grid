package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * Solid wall. Nothing walks through it, nothing burns it, it never changes.
 * Sight also stops at walls.
 */
public class Wall extends Tile {

    private static final Color COLOR = Painter.rgb(70, 72, 84);
    private static final Color MORTAR = Painter.rgb(40, 41, 50);
    private static final Color HIGHLIGHT = Painter.rgba(255, 255, 255, 22);

    public Wall(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "wall";
    }

    /** A brick pattern: 4 rows of bricks, every other row shifted by half a brick. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(COLOR, x, y, size, size);
        float brickHeight = size / 4;
        float brickWidth = size / 2;
        for (int brickRow = 0; brickRow < 4; brickRow++) {
            float top = y + brickRow * brickHeight;
            painter.fillRect(MORTAR, x, top, size, 1);          // mortar line
            painter.fillRect(HIGHLIGHT, x, top + 1, size, 1);   // light edge under it
            float shift = (brickRow % 2 == 0) ? 0 : brickWidth / 2;
            for (float left = x + shift; left < x + size; left += brickWidth) {
                painter.fillRect(MORTAR, left, top, 1, brickHeight);
            }
        }
    }
}
