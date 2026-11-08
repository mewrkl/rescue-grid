package rescuegrid.model.tiles;

import java.awt.Color;

import rescuegrid.model.GameBoard;

/**
 * Damaged floor that is still walkable, but ALWAYS collapses into rubble on the
 * next aftershock. Level designers put it across shortcuts on purpose, so a
 * quake visibly cuts off a route the player was using.
 *
 * Extends Floor (two levels of inheritance: Tile -> Floor -> CrackedFloor), so it
 * is walkable and flammable without repeating that code.
 */
public class CrackedFloor extends Floor {

    private static final Color COLOR = new Color(168, 146, 116);

    public CrackedFloor(int row, int col) {
        super(row, col);
    }

    /** Not picked by random collapses, because it always collapses anyway. */
    @Override
    public boolean canCollapse() {
        return false;
    }

    /** The aftershock turns this tile into rubble. */
    @Override
    public void onAftershock(GameBoard board) {
        board.replaceTile(row, col, new Rubble(row, col));
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "cracked_floor";
    }
}
