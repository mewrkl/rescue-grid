package rescuegrid.model.tiles;

import java.awt.Color;

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

    private static final Color COLOR = new Color(230, 92, 32);

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
}
