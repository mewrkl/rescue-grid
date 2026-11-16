package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * What a fire leaves behind once it burns out. Walkable again, but it can never
 * catch fire a second time, so fire reopens paths instead of burning forever.
 */
public class BurntFloor extends Floor {

    private static final Color COLOR = Painter.rgb(74, 64, 58);
    private static final Color SOOT = Painter.rgba(20, 16, 14, 150);

    public BurntFloor(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public boolean canCollapse() {
        return false;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "burnt_floor";
    }

    @Override
    protected int getImageFrame() {
        return pickVariant(1, 1);
    }

    /** Dark floor with soot blotches. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        Random random = positionRandom();
        int tile = (int) size;
        for (int i = 0; i < 3; i++) {
            int w = tile / 4 + random.nextInt(tile / 4);
            int h = tile / 6 + random.nextInt(tile / 6);
            painter.fillOval(SOOT, x + random.nextInt(tile - w), y + random.nextInt(tile - h), w, h);
        }
    }
}
