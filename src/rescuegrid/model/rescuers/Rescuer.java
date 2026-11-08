package rescuegrid.model.rescuers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import rescuegrid.assets.AssetManager;
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

    public static final int MAX_HEALTH = 5;            // tune in testing
    public static final double STEP_DELAY = 0.15;      // seconds per tile
    public static final double CARRY_STEP_DELAY = 0.30; // seconds per tile while carrying

    private int row;
    private int col;
    private int health;
    private Survivor carriedSurvivor;
    private Direction facing;
    private double specialCooldown; // seconds until the special can be used again
    private double moveTimer;       // seconds until the next step is allowed

    public Rescuer(int row, int col) {
        this.row = row;
        this.col = col;
        this.health = MAX_HEALTH;
        this.carriedSurvivor = null;
        this.facing = Direction.DOWN;
        this.specialCooldown = 0;
        this.moveTimer = 0;
    }

    // ----- Each subclass must provide these -----

    /** The rescuer's special ability. Throws if it can't be used right now. */
    public abstract void specialAction(GameBoard board) throws InvalidMoveException;

    public abstract String getSpecialName();

    public abstract double getCooldownSeconds();

    public abstract String getName();

    public abstract String getImageKey();

    public abstract Color getFallbackColor();

    // ----- Time -----

    /** Called every frame with the seconds since the last frame. */
    public void tick(double seconds) {
        // TODO (Week 1, Member 2): count moveTimer and specialCooldown down by
        //  `seconds`, but never below 0.
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
        // TODO (Week 1, Member 2): return CARRY_STEP_DELAY when carrying a survivor.
        return STEP_DELAY;
    }

    // ----- Movement -----

    /** Moves to (newRow, newCol). The caller has already checked the step is legal. */
    public void moveTo(int newRow, int newCol) {
        // TODO (Week 1, Member 2):
        //  1. Set row and col to the new position.
        //  2. Set moveTimer = getStepDelay(), so holding a key doesn't move 60 tiles a second.
        //  3. If carrying a survivor, call carriedSurvivor.moveWithCarrier(row, col).
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

    // ----- Health -----

    public void takeDamage(int amount) {
        // TODO (Week 3, Member 2): lower health by amount, but never below 0.
    }

    public boolean isDowned() {
        return health <= 0;
    }

    // ----- Carrying -----

    public void pickUp(Survivor survivor) {
        // TODO (Week 3, Member 2): only if not already carrying someone:
        //  remember the survivor in carriedSurvivor and call survivor.markCarried().
    }

    /** Puts the carried survivor down on this tile and returns them (or null). */
    public Survivor dropSurvivor() {
        // TODO (Week 3, Member 2): call markDropped(row, col) on the carried survivor,
        //  set carriedSurvivor back to null, and return the survivor.
        return null;
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

    /** Draws the rescuer image, or a coloured circle with their first letter. */
    @Override
    public void draw(Graphics2D g, int x, int y, int size, AssetManager assets) {
        BufferedImage image = assets.getImage(getImageKey());
        if (image != null) {
            g.drawImage(image, x, y, size, size, null);
            return;
        }
        int margin = size / 8;
        g.setColor(getFallbackColor());
        g.fillOval(x + margin, y + margin, size - 2 * margin, size - 2 * margin);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 3));
        FontMetrics metrics = g.getFontMetrics();
        String letter = getName().substring(0, 1);
        g.drawString(letter,
                x + (size - metrics.stringWidth(letter)) / 2,
                y + (size + metrics.getAscent() - metrics.getDescent()) / 2);
    }
}
