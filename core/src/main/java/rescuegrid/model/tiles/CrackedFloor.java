package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.model.GameBoard;

/**
 * Damaged floor that is still walkable, but ALWAYS collapses into rubble on the
 * next aftershock. Level designers put it across shortcuts on purpose, so a
 * quake visibly cuts off a route the player was using.
 *
 * Extends Floor (two levels of inheritance: Tile -> Floor -> CrackedFloor), so it
 * is walkable without repeating that code.
 */
public class CrackedFloor extends Floor {

    private static final Color COLOR = Painter.rgb(176, 156, 126);
    private static final Color CRACK = Painter.rgba(60, 44, 30, 200);

    public CrackedFloor(int row, int col) {
        super(row, col);
    }

    /**
     * Cracked floor does not burn. Otherwise fire could turn the shortcut into
     * burnt floor before the quake, and the designed "path collapses" moment
     * would never happen.
     */
    @Override
    public boolean isFlammable() {
        return false;
    }

    /** Not picked by random collapses, because it always collapses anyway. */
    @Override
    public boolean canCollapse() {
        return false;
    }

    /** The aftershock turns this tile into rubble. */
    @Override
    public void onAftershock(GameBoard board) {
        board.replaceTile(row, col, new Rubble(row, col));
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "cracked_floor";
    }

    /** Floor art plus two jagged dark cracks running from top to bottom. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        Random random = positionRandom();
        int tile = (int) size;
        float thickness = Math.max(2, size / 24f);
        for (int crack = 0; crack < 2; crack++) {
            float px = x + random.nextInt(tile);
            float py = y;
            for (int step = 1; step <= 4; step++) {
                float nx = x + Math.max(2, Math.min(tile - 2, px - x + random.nextInt(tile / 2) - tile / 4));
                float ny = y + step * size / 4;
                painter.line(CRACK, px, py, nx, ny, thickness);
                px = nx;
                py = ny;
            }
        }
    }
}
