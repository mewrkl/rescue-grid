package rescuegrid.model.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.GameBoard;

/**
 * A burning tile. Blocks movement, hurts rescuers standing next to it, and
 * spreads every world tick (HazardManager does the spreading).
 *
 * Each fire remembers how many ticks it has burned. After BURN_OUT_TICKS it
 * replaces itself with BurntFloor, which reopens the path.
 */
public class Fire extends Tile {

    public static final int BURN_OUT_TICKS = 6; // tune in testing

    private static final Color COLOR = Painter.rgb(110, 34, 18);
    private static final Color OUTER_FLAME = Painter.rgb(236, 96, 28);
    private static final Color INNER_FLAME = Painter.rgb(255, 206, 64);
    private static final int FLICKER_MILLIS = 140;

    private int age;

    public Fire(int row, int col) {
        super(row, col);
        this.age = 0;
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    /** Already burning, so fire can't spread onto it again. */
    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public boolean isBurning() {
        return true;
    }

    /** One more tick of burning; burn out when old enough. */
    @Override
    public void onWorldTick(GameBoard board) {
        age++;
        if (age >= BURN_OUT_TICKS) {
            board.replaceTile(row, col, new BurntFloor(row, col));
        }
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "fire";
    }

    /** Fire flickers: every FLICKER_MILLIS it switches between two frames. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getImage(isSecondFrame() ? "fire2" : "fire");
        if (image == null) {
            image = assets.getImage("fire");
        }
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFallback(painter, x, y, size);
        }
    }

    /** Dark scorched base with three flames whose heights change each frame. */
    @Override
    protected void drawFallback(Painter painter, float x, float y, float size) {
        super.drawFallback(painter, x, y, size);
        boolean second = isSecondFrame();
        drawFlame(painter, x + size * 3 / 10, y, size, second ? 0.45f : 0.55f);
        drawFlame(painter, x + size * 7 / 10, y, size, second ? 0.55f : 0.45f);
        drawFlame(painter, x + size / 2, y, size, second ? 0.70f : 0.62f);
    }

    private void drawFlame(Painter painter, float centerX, float top, float size, float height) {
        float bottom = top + size * 9 / 10;
        float width = size * 3 / 10;
        painter.fillPolygon(OUTER_FLAME, flameShape(centerX, bottom, width, size * height));
        painter.fillPolygon(INNER_FLAME, flameShape(centerX, bottom, width / 2, size * height * 0.55f));
    }

    /** Five corners: wide at the bottom, pointed at the top. */
    private float[] flameShape(float centerX, float bottom, float width, float height) {
        return new float[] {
            centerX - width / 2, bottom,
            centerX - width / 3, bottom - height / 2,
            centerX, bottom - height,
            centerX + width / 3, bottom - height / 2,
            centerX + width / 2, bottom
        };
    }

    /** Neighbouring fires flicker out of step, so it doesn't look like one big blink. */
    private boolean isSecondFrame() {
        long frame = System.currentTimeMillis() / FLICKER_MILLIS;
        return (frame + row + col) % 2 == 0;
    }
}
