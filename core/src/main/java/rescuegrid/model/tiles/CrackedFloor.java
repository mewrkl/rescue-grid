package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;
import rescuegrid.model.GameBoard;

/**
 * Walkable floor that collapses into rubble on every aftershock. Levels put it
 * across shortcuts so a quake cuts off a route.
 */
public class CrackedFloor extends Floor {

    private static final Color COLOR = Painter.rgb(176, 156, 126);
    private static final Color CRACK = Painter.rgba(60, 44, 30, 200);

    public CrackedFloor(int row, int col) {
        super(row, col);
    }

    /** Doesn't burn, so fire can't remove the shortcut before the quake does. */
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
