package rescuegrid.ui;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;

/** A pixel-art star: gold when earned, a faint outline shape when not. */
class StarIcon implements Drawable {

    private static final String[] SHAPE = {
        "....#....",
        "...###...",
        "...###...",
        "#########",
        ".#######.",
        "..#####..",
        "..#####..",
        ".###.###.",
        ".##...##.",
    };
    private static final Color EARNED = Painter.hex(0xFACC15);
    private static final Color EARNED_SHADE = Painter.hex(0xCA8A04);
    private static final Color EMPTY = Painter.rgba(255, 255, 255, 40);

    private boolean earned;

    void setEarned(boolean earned) {
        this.earned = earned;
    }

    /** Whole pixels per cell, so the star stays crisp; centred in the slot. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        int cell = Math.max(1, (int) (size / SHAPE.length));
        float left = x + (size - cell * SHAPE.length) / 2;
        float top = y + (size - cell * SHAPE.length) / 2;
        for (int row = 0; row < SHAPE.length; row++) {
            for (int col = 0; col < SHAPE[row].length(); col++) {
                if (SHAPE[row].charAt(col) != '#') {
                    continue;
                }
                Color color = !earned ? EMPTY : row >= 6 ? EARNED_SHADE : EARNED; // a darker lower edge
                painter.fillRect(color, left + col * cell, top + row * cell, cell, cell);
            }
        }
    }
}
