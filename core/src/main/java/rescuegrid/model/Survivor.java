package rescuegrid.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.rescuers.Rescuer;

/**
 * A person trapped in the building. Hidden in the fog at first, loses condition
 * every world tick, and is lost at 0. Condition only changes through
 * tickCondition() and treat().
 *
 * Each injury type (triage colour) is a subclass that reacts in its own way when
 * a rescuer reaches them: a MinorSurvivor walks behind you, a SeriousSurvivor is
 * carried, and a CriticalSurvivor must be stabilised and fetched by backup.
 * Any survivor can also be trapped under debris until the Engineer frees them.
 */
public abstract class Survivor implements Drawable {

    public static final int DEFAULT_CONDITION = 20; // world ticks a serious survivor lasts (60 s)
    /** Condition points per world tick of life, so types can drain at 1, 2 or 3 points a tick. */
    public static final int POINTS_PER_TICK = 2;

    private static final Color SKIN = Painter.rgb(232, 190, 150);
    private static final Color OUTLINE = Painter.rgb(40, 32, 24);
    private static final Color BAR_BACK = Painter.rgba(0, 0, 0, 160);
    private static final Color BAR_GOOD = Painter.rgb(74, 222, 128);
    private static final Color BAR_LOW = Painter.rgb(250, 204, 21);
    private static final Color BAR_CRITICAL = Painter.rgb(239, 68, 68);
    private static final Color DEBRIS = Painter.rgb(120, 104, 88);
    private static final Color DEBRIS_DARK = Painter.rgb(72, 62, 52);

    private int row;
    private int col;
    private int prevRow;           // the tile before the last move (for sliding while following)
    private int prevCol;
    private final int maxCondition;
    private int condition;
    private boolean revealed;
    private boolean carried;
    private boolean following;
    private boolean trapped;
    private boolean rescued;
    private boolean lost;

    /** ticksToLast: how many world ticks a serious survivor of this level would last. */
    public Survivor(int row, int col, int ticksToLast) {
        this.row = row;
        this.col = col;
        this.prevRow = row;
        this.prevCol = col;
        this.maxCondition = ticksToLast * POINTS_PER_TICK;
        this.condition = maxCondition;
        this.revealed = false;
        this.carried = false;
        this.following = false;
        this.trapped = false;
        this.rescued = false;
        this.lost = false;
    }

    /** "Minor", "Serious" or "Critical". */
    public abstract String getInjuryName();

    /** The triage tag colour: green, yellow or red. */
    public abstract Color getTagColor();

    /** How many condition points this survivor loses every world tick. */
    public abstract int getConditionLossPerTick();

    /** What this survivor does when a rescuer reaches them (follow, be carried, or refuse). */
    protected abstract void goWith(Rescuer rescuer) throws InvalidMoveException;

    /** A rescuer walked onto this survivor. Trapped survivors can't go anywhere yet. */
    public void reachedBy(Rescuer rescuer) throws InvalidMoveException {
        if (trapped) {
            throw new InvalidMoveException("Trapped under debris: the Engineer can lift it (E)");
        }
        goWith(rescuer);
    }

    /** The Medic treats this survivor. Returns a message for the HUD. */
    public String treat(int amount) {
        condition = Math.min(maxCondition, condition + amount);
        return getInjuryName() + " survivor treated";
    }

    /** Someone radioed for backup to carry this survivor out. Only critical survivors need it. */
    public void callBackup() throws InvalidMoveException {
        throw new InvalidMoveException("Only critical (red) survivors need backup");
    }

    /** Called every frame. Most survivors do nothing; CriticalSurvivor counts down its backup. */
    public void update(double seconds) {
        // nothing by default
    }

    /** Called once per world tick. */
    public void tickCondition() {
        if (rescued || lost) {
            return;
        }
        condition -= getConditionLossPerTick();
        if (condition <= 0) {
            condition = 0;
            lost = true;
        }
    }

    /** The Engineer lifts the debris off a trapped survivor. */
    public void freeFromDebris() throws InvalidMoveException {
        if (!trapped) {
            throw new InvalidMoveException("Nothing is pinning them down");
        }
        trapped = false;
    }

    /** Used by LevelLoader for survivors that start pinned under debris. */
    public void setTrapped(boolean trapped) {
        this.trapped = trapped;
    }

    public void reveal() {
        revealed = true;
    }

    public void markCarried() {
        carried = true;
    }

    public void markFollowing() {
        following = true;
    }

    /** The rescuer put this survivor down (or let go of them) at (row, col). */
    public void markDropped(int row, int col) {
        carried = false;
        following = false;
        moveTo(row, col);
        prevRow = row;
        prevCol = col;
    }

    /** Changes position: following or carried by a rescuer, or pushed by a collapse. */
    public void moveTo(int row, int col) {
        this.prevRow = this.row;
        this.prevCol = this.col;
        this.row = row;
        this.col = col;
    }

    public void markRescued() {
        carried = false;
        following = false;
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

    /** Where to draw a following survivor part-way through a step (0 = start, 1 = done). */
    public double getDrawRow(double progress) {
        return prevRow + (row - prevRow) * progress;
    }

    public double getDrawCol(double progress) {
        return prevCol + (col - prevCol) * progress;
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

    public boolean isFollowing() {
        return following;
    }

    public boolean isTrapped() {
        return trapped;
    }

    public boolean isRescued() {
        return rescued;
    }

    public boolean isLost() {
        return lost;
    }

    /** Still lying in the building: not carried, not following, not rescued or lost. */
    public boolean isWaiting() {
        return !carried && !following && !rescued && !lost;
    }

    /** Draws the survivor (image or a simple figure), debris if trapped, and a condition bar. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getImage(getImageKey());
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFigure(painter, x, y, size);
        }
        if (trapped) {
            drawDebris(painter, x, y, size);
        }
        drawTag(painter, x, y, size);
        drawExtras(painter, x, y, size);
        drawConditionBar(painter, x, y, size);
    }

    /** The sprite to use if one exists: survivor_minor, survivor_serious or survivor_critical. */
    public String getImageKey() {
        return "survivor_" + getInjuryName().toLowerCase();
    }

    /** A person waving for help, with a shirt in their triage colour. CriticalSurvivor lies down instead. */
    protected void drawFigure(Painter painter, float x, float y, float size) {
        float head = size / 4;
        float cx = x + size / 2;
        float bodyX = cx - size / 6;
        float bodyY = y + size * 2 / 5;
        painter.fillRect(getTagColor(), bodyX, bodyY, size / 3, size * 2 / 5);
        painter.fillRect(getTagColor(), cx + size / 6 - 2, y + size / 5, size / 12, size / 4);   // raised arm
        painter.outlineRect(OUTLINE, bodyX, bodyY, size / 3, size * 2 / 5, 1);
        painter.fillOval(SKIN, cx - head / 2, y + size / 6, head, head);
        painter.outlineOval(OUTLINE, cx - head / 2, y + size / 6, head, head, 1);
    }

    /** Subclasses draw extra badges here (the critical survivor's backup countdown). */
    protected void drawExtras(Painter painter, float x, float y, float size) {
        // nothing by default
    }

    /** A small triage tag in the top-left corner, in the survivor's colour. */
    private void drawTag(Painter painter, float x, float y, float size) {
        float tag = size / 5;
        painter.fillRect(getTagColor(), x + 3, y + 3, tag, tag);
        painter.outlineRect(OUTLINE, x + 3, y + 3, tag, tag, 1);
    }

    /** Broken blocks over the survivor's legs. */
    private void drawDebris(Painter painter, float x, float y, float size) {
        float block = size / 4;
        float top = y + size * 3 / 5;
        painter.fillRect(DEBRIS, x + size / 8, top, block * 1.4f, block);
        painter.fillRect(DEBRIS_DARK, x + size / 8 + block, top - block / 3, block * 1.2f, block);
        painter.fillRect(DEBRIS, x + size / 2, top + block / 6, block * 1.5f, block * 0.9f);
        painter.outlineRect(OUTLINE, x + size / 8, top, block * 1.4f, block, 1);
        painter.outlineRect(OUTLINE, x + size / 2, top + block / 6, block * 1.5f, block * 0.9f, 1);
    }

    /** Green when healthy, yellow when low, red when nearly gone. */
    private void drawConditionBar(Painter painter, float x, float y, float size) {
        float barWidth = size * 3 / 4;
        float barX = x + (size - barWidth) / 2;
        float barY = y + size - size / 10;
        float ratio = (float) condition / maxCondition;
        painter.fillRect(BAR_BACK, barX - 1, barY - 1, barWidth + 2, size / 16 + 2);
        painter.fillRect(ratio > 0.5f ? BAR_GOOD : ratio > 0.25f ? BAR_LOW : BAR_CRITICAL,
                barX, barY, barWidth * ratio, size / 16);
    }

    protected static Color getSkinColor() {
        return SKIN;
    }

    protected static Color getOutlineColor() {
        return OUTLINE;
    }
}
