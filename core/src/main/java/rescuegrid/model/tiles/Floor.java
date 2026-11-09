package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/** Normal floor: walkable, can catch fire, and can collapse in an aftershock. */
public class Floor extends Tile {

    private static final Color COLOR = Painter.rgb(196, 186, 166);
    private static final Color SPECKLE = Painter.rgba(0, 0, 0, 30);

    public Floor(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean isFlammable() {
        return true;
    }

    @Override
    public boolean canCollapse() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "floor";
    }

    /** Base colour plus a few small speckles so the floor looks like concrete. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        Random random = positionRandom();
        int tile = (int) size;
        for (int i = 0; i < 5; i++) {
            int speck = tile / 16 + random.nextInt(tile / 16 + 1);
            painter.fillRect(SPECKLE, x + random.nextInt(tile - speck), y + random.nextInt(tile - speck),
                    speck, speck);
        }
    }
}
