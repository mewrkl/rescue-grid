package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * The way out. Walking onto it while carrying a survivor delivers them.
 * It never burns and never collapses.
 */
public class Exit extends Tile {

    private static final Color COLOR = Painter.rgb(40, 140, 70);
    private static final Color DOOR = Painter.rgb(24, 96, 48);
    private static final Color SIGN = Painter.rgb(220, 255, 228);

    public Exit(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "exit";
    }

    /** A green door with an EXIT sign above it. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        float inset = size / 6;
        painter.fillRect(DOOR, x + inset, y + size / 3, size - 2 * inset, size - size / 3);
        painter.text("EXIT", SIGN, x + size / 2, y + size / 12);
    }
}
