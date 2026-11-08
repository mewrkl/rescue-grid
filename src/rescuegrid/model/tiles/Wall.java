package rescuegrid.model.tiles;

import java.awt.Color;

/**
 * Solid wall. Nothing walks through it, nothing burns it, it never changes.
 * Sight also stops at walls.
 */
public class Wall extends Tile {

    private static final Color COLOR = new Color(58, 60, 70);

    public Wall(int row, int col) {
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

    @Override
    public Color getFallbackColor() {
        return COLOR;
    }

    @Override
    public String getImageKey() {
        return "wall";
    }
}
