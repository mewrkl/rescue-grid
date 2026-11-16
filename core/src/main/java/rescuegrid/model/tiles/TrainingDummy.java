package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.graphics.Painter;

/**
 * A first-aid training dummy in the training level's first room. It blocks the
 * way like a wall, and the Medic can practise Treat on it (isTreatable).
 * Written in level files as 'd'.
 */
public class TrainingDummy extends Tile {

    private static final Color FLOOR = Painter.rgb(196, 186, 166);
    private static final Color STAND = Painter.rgb(92, 72, 52);
    private static final Color BODY = Painter.rgb(222, 196, 150);
    private static final Color EDGE = Painter.rgb(110, 88, 60);
    private static final Color CROSS = Painter.rgb(220, 38, 38);

    public TrainingDummy(int row, int col) {
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

    /** The only tile the Medic can treat. */
    @Override
    public boolean isTreatable() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return FLOOR;
    }

    @Override
    public String getImageKey() {
        return "dummy";
    }

    /** A tan mannequin on a wooden stand, with a red first-aid cross on its chest. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        float cx = x + size / 2;
        painter.fillRect(STAND, cx - size * 0.2f, y + size * 0.86f, size * 0.4f, size * 0.06f); // base
        painter.fillRect(STAND, cx - size * 0.03f, y + size * 0.66f, size * 0.06f, size * 0.22f); // pole
        float bodyTop = y + size * 0.34f;
        painter.fillRect(BODY, cx - size * 0.2f, bodyTop, size * 0.4f, size * 0.34f);
        painter.outlineRect(EDGE, cx - size * 0.2f, bodyTop, size * 0.4f, size * 0.34f, 1);
        float head = size * 0.22f;
        painter.fillOval(BODY, cx - head / 2, y + size * 0.1f, head, head);
        painter.outlineOval(EDGE, cx - head / 2, y + size * 0.1f, head, head, 1);
        float arm = size * 0.04f;
        painter.fillRect(CROSS, cx - arm / 2, bodyTop + size * 0.07f, arm, size * 0.18f);
        painter.fillRect(CROSS, cx - size * 0.09f, bodyTop + size * 0.14f, size * 0.18f, arm);
    }
}
