package rescuegrid.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;

/**
 * A trapped person. Hidden in the fog at first, loses condition every world
 * tick, and is lost at 0. Condition only changes through tickCondition() and heal().
 */
public class Survivor implements Drawable {

    public static final int DEFAULT_CONDITION = 20; // 20 ticks x 3 s = 60 s

    private static final Color SKIN = Painter.rgb(232, 190, 150);
    private static final Color SHIRT = Painter.rgb(236, 196, 40);
    private static final Color OUTLINE = Painter.rgb(40, 32, 24);
    private static final Color BAR_BACK = Painter.rgba(0, 0, 0, 160);
    private static final Color BAR_GOOD = Painter.rgb(74, 222, 128);
    private static final Color BAR_LOW = Painter.rgb(250, 204, 21);
    private static final Color BAR_CRITICAL = Painter.rgb(239, 68, 68);

    private int row;
    private int col;
    private final int maxCondition;
    private int condition;
    private boolean revealed;
    private boolean carried;
    private boolean rescued;
    private boolean lost;

    public Survivor(int row, int col, int maxCondition) {
        this.row = row;
        this.col = col;
        this.maxCondition = maxCondition;
        this.condition = maxCondition;
        this.revealed = false;
        this.carried = false;
        this.rescued = false;
        this.lost = false;
    }

    /** Called once per world tick. */
    public void tickCondition() {
        if (rescued || lost) {
            return;
        }
        condition--;
        if (condition <= 0) {
            condition = 0;
            lost = true;
        }
    }

    /** Called by the Medic's special action. Never goes above the maximum. */
    public void heal(int amount) {
        condition = Math.min(maxCondition, condition + amount);
    }

    public void reveal() {
        revealed = true;
    }

    public void markCarried() {
        carried = true;
    }

    /** The rescuer put this survivor down (or was downed) at (row, col). */
    public void markDropped(int row, int col) {
        carried = false;
        moveTo(row, col);
    }

    /** Changes position: following the rescuer carrying them, or pushed by a collapse. */
    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void markRescued() {
        carried = false;
        rescued = true;
    }

    public void markLost() {
        lost = true;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getCondition() {
        return condition;
    }

    public int getMaxCondition() {
        return maxCondition;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isCarried() {
        return carried;
    }

    public boolean isRescued() {
        return rescued;
    }

    public boolean isLost() {
        return lost;
    }

    /** Still lying in the building, waiting to be picked up. */
    public boolean isWaiting() {
        return !carried && !rescued && !lost;
    }

    /** Draws the survivor (image or a simple figure) with a condition bar underneath. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getImage("survivor");
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFigure(painter, x, y, size);
        }
        drawConditionBar(painter, x, y, size);
    }

    /** A person waving for help: head, body and one raised arm. */
    private void drawFigure(Painter painter, float x, float y, float size) {
        float head = size / 4;
        float cx = x + size / 2;
        float bodyX = cx - size / 6;
        float bodyY = y + size * 2 / 5;
        painter.fillRect(SHIRT, bodyX, bodyY, size / 3, size * 2 / 5);
        painter.fillRect(SHIRT, cx + size / 6 - 2, y + size / 5, size / 12, size / 4);   // raised arm
        painter.outlineRect(OUTLINE, bodyX, bodyY, size / 3, size * 2 / 5, 1);
        painter.fillOval(SKIN, cx - head / 2, y + size / 6, head, head);
        painter.outlineOval(OUTLINE, cx - head / 2, y + size / 6, head, head, 1);
    }

    /** Green when healthy, yellow when low, red when critical. */
    private void drawConditionBar(Painter painter, float x, float y, float size) {
        float barWidth = size * 3 / 4;
        float barX = x + (size - barWidth) / 2;
        float barY = y + size - size / 10;
        float ratio = (float) condition / maxCondition;
        painter.fillRect(BAR_BACK, barX - 1, barY - 1, barWidth + 2, size / 16 + 2);
        painter.fillRect(ratio > 0.5f ? BAR_GOOD : ratio > 0.25f ? BAR_LOW : BAR_CRITICAL,
                barX, barY, barWidth * ratio, size / 16);
    }
}
