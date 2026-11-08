package rescuegrid.model.tiles;

import java.awt.Color;

/**
 * The way out. Walking onto it while carrying a survivor delivers them.
 * It never burns and never collapses.
 */
public class Exit extends Tile {

    private static final Color COLOR = new Color(56, 168, 84);

    public Exit(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isWalkable() {
        return true;
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
        return "exit";
    }
}
