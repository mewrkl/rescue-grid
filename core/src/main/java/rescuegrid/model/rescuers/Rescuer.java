package rescuegrid.model.rescuers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.Direction;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;
import rescuegrid.model.tiles.Tile;

/**
 * A member of the rescue team. Abstract: the game only ever creates a Medic,
 * an Engineer or a DogHandler.
 *
 * Encapsulation: health, the special cooldown and the move timer are private.
 * They only change through takeDamage(), startCooldown() and tick(), so no
 * other class can ever set an impossible value like health = -3.
 *
 * Polymorphism: the game calls specialAction(board) on whichever rescuer is
 * active, and Java runs that subclass's own version.
 */
public abstract class Rescuer implements Drawable {

    public static final int MAX_HEALTH = 5;             // tune in testing
    public static final double STEP_DELAY = 0.15;       // seconds per tile
    public static final double CARRY_STEP_DELAY = 0.30; // seconds per tile while carrying

    private static final double HURT_FLASH_SECONDS = 0.4;
    private static final Color SHADOW = Painter.rgba(0, 0, 0, 70);
    private static final Color OUTLINE = Painter.rgb(20, 20, 24);
    private static final Color HURT = Painter.rgb(255, 40, 40);
    private static final Color CARRY_BADGE = Painter.rgb(236, 196, 40);

    private int row;
    private int col;
    private int prevRow;            // where the last step started (for smooth sliding)
    private int prevCol;
    private int health;
    private Survivor carriedSurvivor;
    private Direction facing;
    private double specialCooldown; // seconds until the special can be used again
    private double moveTimer;       // seconds until the next step is allowed
    private double stepDuration;    // how long the current step takes
    private double hurtFlash;       // seconds left of the red "ouch" flash

    public Rescuer(int row, int col) {
        this.row = row;
        this.col = col;
        this.prevRow = row;
        this.prevCol = col;
        this.health = MAX_HEALTH;
        this.carriedSurvivor = null;
        this.facing = Direction.DOWN;
        this.specialCooldown = 0;
        this.moveTimer = 0;
        this.stepDuration = 0;
        this.hurtFlash = 0;
    }

    // ----- Each subclass must provide these -----

    /**
     * The rescuer's special ability. Returns a short message for the HUD on
     * success, and throws InvalidMoveException if it can't be used right now.
     */
    public abstract String specialAction(GameBoard board) throws InvalidMoveException;

    public abstract String getSpecialName();

    /** Name of the sound to play when the special works. */
    public abstract String getSpecialSound();

    public abstract double getCooldownSeconds();

    public abstract String getName();

    public abstract String getImageKey();

    public abstract Color getFallbackColor();

    /** Draws the small symbol that shows who this is (red cross, hard hat, paw). */
    protected abstract void drawRoleIcon(Painter painter, float x, float y, float size);

    // ----- Time -----

    /** Called every frame with the seconds since the last frame. */
    public void tick(double seconds) {
        moveTimer = Math.max(0, moveTimer - seconds);
        specialCooldown = Math.max(0, specialCooldown - seconds);
        hurtFlash = Math.max(0, hurtFlash - seconds);
    }

    public boolean canStep() {
        return moveTimer <= 0;
    }

    public boolean canUseSpecial() {
        return specialCooldown <= 0;
    }

    /** Subclasses call this after a successful special action. */
    protected void startCooldown() {
        specialCooldown = getCooldownSeconds();
    }

    /** How long one step takes: slower while carrying someone. */
    public double getStepDelay() {
        return isCarrying() ? CARRY_STEP_DELAY : STEP_DELAY;
    }

    // ----- Movement -----

    /** Moves to (newRow, newCol). The caller has already checked the step is legal. */
    public void moveTo(int newRow, int newCol) {
        prevRow = row;
        prevCol = col;
        row = newRow;
        col = newCol;
        stepDuration = getStepDelay();
        moveTimer = stepDuration;
        if (carriedSurvivor != null) {
            carriedSurvivor.moveTo(row, col);
        }
    }

    /** Overloaded version of moveTo: same name, different parameters. */
    public void moveTo(Tile target) {
        moveTo(target.getRow(), target.getCol());
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public Direction getFacing() {
        return facing;
    }

    /** The tile directly in front of the rescuer (null if off the map). */
    public Tile getFacedTile(GameBoard board) {
        return board.getTile(row + facing.getRowChange(), col + facing.getColChange());
    }

    /**
     * Where to DRAW the rescuer. During a step this is part-way between the old
     * and new tile, so movement slides smoothly instead of jumping.
     * Example: halfway through a step from row 5 to row 6, this returns 5.5.
     */
    public double getDrawRow() {
        return prevRow + (row - prevRow) * stepProgress();
    }

    public double getDrawCol() {
        return prevCol + (col - prevCol) * stepProgress();
    }

    /** 0.0 at the start of a step, 1.0 when it's finished. */
    private double stepProgress() {
        if (stepDuration <= 0) {
            return 1.0;
        }
        return 1.0 - moveTimer / stepDuration;
    }

    // ----- Health -----

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
        hurtFlash = HURT_FLASH_SECONDS;
    }

    public boolean isDowned() {
        return health <= 0;
    }

    // ----- Carrying -----

    public void pickUp(Survivor survivor) {
        if (carriedSurvivor != null) {
            return;
        }
        carriedSurvivor = survivor;
        survivor.markCarried();
        survivor.moveTo(row, col);
    }

    /** Puts the carried survivor down on this tile and returns them (or null). */
    public Survivor dropSurvivor() {
        Survivor dropped = carriedSurvivor;
        if (dropped != null) {
            dropped.markDropped(row, col);
            carriedSurvivor = null;
        }
        return dropped;
    }

    /** Hands the carried survivor over at the Exit. Returns them (or null). */
    public Survivor deliverSurvivor() {
        Survivor delivered = carriedSurvivor;
        if (delivered != null) {
            delivered.markRescued();
            carriedSurvivor = null;
        }
        return delivered;
    }

    public boolean isCarrying() {
        return carriedSurvivor != null;
    }

    // ----- Getters -----

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getHealth() {
        return health;
    }

    public Survivor getCarriedSurvivor() {
        return carriedSurvivor;
    }

    public double getSpecialCooldown() {
        return specialCooldown;
    }

    // ----- Drawing -----

    /** Draws the rescuer image (or placeholder), then the carry badge and hurt flash. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getImage(getImageKey());
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFallback(painter, x, y, size);
        }
        if (isCarrying()) {
            drawCarryBadge(painter, x, y, size, assets);
        }
        if (hurtFlash > 0) {
            Color flash = new Color(HURT);
            flash.a = (float) (0.63 * hurtFlash / HURT_FLASH_SECONDS); // fades out
            painter.fillOval(flash, x + size / 8, y + size / 8, size * 3 / 4, size * 3 / 4);
        }
    }

    /** Placeholder: shadow, coloured body, role icon, and a dot showing which way they face. */
    private void drawFallback(Painter painter, float x, float y, float size) {
        float margin = size / 8;
        float body = size - 2 * margin;
        painter.fillOval(SHADOW, x + size / 5, y + size * 3 / 4, size * 3 / 5, size / 5);
        painter.fillOval(getFallbackColor(), x + margin, y + margin, body, body);
        painter.outlineOval(OUTLINE, x + margin, y + margin, body, body, 2);
        drawRoleIcon(painter, x, y, size);

        float dot = size / 8;
        float dotX = x + size / 2 + facing.getColChange() * size * 0.34f - dot / 2;
        float dotY = y + size / 2 + facing.getRowChange() * size * 0.34f - dot / 2;
        painter.fillOval(Color.WHITE, dotX, dotY, dot, dot);
        painter.outlineOval(OUTLINE, dotX, dotY, dot, dot, 1);
    }

    /** A small yellow badge in the corner while carrying a survivor. */
    private void drawCarryBadge(Painter painter, float x, float y, float size, AssetManager assets) {
        float badge = size / 3;
        float badgeX = x + size - badge;
        TextureRegion icon = assets.getImage("carry_icon");
        if (icon != null) {
            painter.image(icon, badgeX, y, badge, badge);
            return;
        }
        painter.fillOval(CARRY_BADGE, badgeX, y, badge, badge);
        painter.outlineOval(OUTLINE, badgeX, y, badge, badge, 1);
        painter.fillOval(OUTLINE, badgeX + badge / 3, y + badge / 5, badge / 3, badge / 3);
    }
}
