package rescuegrid.model.tiles;

import java.awt.Color;

/**
 * Collapsed debris. Blocks movement and sight and does not burn. The Engineer
 * can clear it back into Floor, so rubble never traps a level forever.
 */
public class Rubble extends Tile {

    private static final Color COLOR = new Color(128, 108, 88);

    public Rubble(int row, int col) {
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

    /** The only tile the Engineer can clear. */
    @Override
    public boolean isClearable() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "rubble";
    }
}
