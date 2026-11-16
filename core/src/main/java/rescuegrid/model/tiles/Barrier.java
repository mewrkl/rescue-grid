package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * A taped-off doorway in the training level. It blocks walking and sight like a
 * wall until the Tutorial opens its gate number, then it becomes Floor.
 * Written in level files as a digit: '1' is gate 1, '2' is gate 2, and so on.
 */
public class Barrier extends Tile {

    private static final Color COLOR = Painter.rgb(60, 52, 40);
    private static final Color TAPE = Painter.rgb(251, 191, 36);
    private static final Color TAPE_DARK = Painter.rgb(30, 26, 20);
    private static final int STRIPES = 6;

    private final int gate;

    public Barrier(int row, int col, int gate) {
        super(row, col);
        this.gate = gate;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    /** Opens when the Tutorial reaches the step that uses this gate. */
    @Override
    public boolean opensWithGate(int gateNumber) {
        return gateNumber == gate;
    }

    public int getGate() {
        return gate;
    }

    @Override
    public Color getFallbackColor() {
        return TAPE;
    }

    @Override
    public String getImageKey() {
        return "barrier";
    }

    /** A dark doorway crossed by two bands of yellow and black caution tape. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(COLOR, x, y, size, size);
        float band = size / 6;
        float stripe = size / STRIPES;
        float[] bandTops = {y + size * 0.25f, y + size * 0.6f};
        for (float top : bandTops) {
            for (int i = 0; i < STRIPES; i++) {
                painter.fillRect(i % 2 == 0 ? TAPE : TAPE_DARK, x + i * stripe, top, stripe, band);
            }
        }
    }
}
