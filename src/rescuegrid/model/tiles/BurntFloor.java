package rescuegrid.model.tiles;

import java.awt.Color;

/**
 * What a fire leaves behind once it burns out. Walkable again, but it can never
 * catch fire a second time, so fire reopens paths instead of burning forever.
 */
public class BurntFloor extends Floor {

    private static final Color COLOR = new Color(74, 64, 58);

    public BurntFloor(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isFlammable() {
        return false;
    }

    @Override
    public boolean canCollapse() {
        return false;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "burnt_floor";
    }
}
