package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * Collapsed debris. Blocks movement and sight and does not burn. The Engineer
 * can clear it back into Floor, so rubble never traps a level forever.
 */
public class Rubble extends Tile {

    private static final Color COLOR = Painter.rgb(150, 136, 118);
    private static final Color[] ROCKS = {
        Painter.rgb(112, 100, 88), Painter.rgb(132, 120, 106), Painter.rgb(96, 86, 78)
    };
    private static final Color ROCK_EDGE = Painter.rgb(50, 44, 40);

    public Rubble(int row, int col) {
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

    /** The only tile the Engineer can clear. */
    /** Scent gets through the gaps in a heap of rubble. */
    @Override
    public boolean letsScentThrough() {
        return true;
    }

    @Override
    public boolean isClearable() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "rubble";
    }

    @Override
    protected int getImageFrame() {
        return pickVariant(1, 1);
    }

    /** A dusty base with a pile of irregular six-sided rocks. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        Random random = positionRandom();
        int tile = (int) size;
        for (int i = 0; i < 6; i++) {
            int radius = tile / 7 + random.nextInt(tile / 8);
            float cx = x + radius + random.nextInt(tile - 2 * radius);
            float cy = y + radius + random.nextInt(tile - 2 * radius);
            float[] rock = new float[12];
            for (int corner = 0; corner < 6; corner++) {
                double angle = corner * Math.PI / 3;
                double r = radius * (0.7 + random.nextDouble() * 0.3);
                rock[2 * corner] = cx + (float) (Math.cos(angle) * r);
                rock[2 * corner + 1] = cy + (float) (Math.sin(angle) * r);
            }
            painter.fillPolygon(ROCKS[i % ROCKS.length], rock);
            painter.outlinePolygon(ROCK_EDGE, 1, rock);
        }
    }
}
