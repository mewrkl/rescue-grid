package rescuegrid.model.tiles;

import java.awt.Color;

/**
 * Normal floor. Rescuers can walk on it, fire can spread onto it, and a random
 * aftershock can collapse it into rubble.
 *
 * CrackedFloor and BurntFloor extend this class, because they are floors too.
 */
public class Floor extends Tile {

    private static final Color COLOR = new Color(196, 186, 166);

    public Floor(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean isFlammable() {
        return true;
    }

    @Override
    public boolean canCollapse() {
        return true;
    }

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "floor";
    }
}
