package rescuegrid.model.tiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import rescuegrid.assets.AssetManager;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;

/**
 * One square of the building. Abstract: you can never create a plain "Tile",
 * only a specific kind (Floor, Wall, Fire, ...).
 *
 * Each subclass answers the abstract questions (can I walk here? does it burn?)
 * in its own way. The "hook" methods below have a default body that does
 * nothing; a subclass overrides only the hooks it cares about. This is how we
 * avoid "if (tile instanceof Fire)" checks anywhere in the game.
 */
public abstract class Tile implements Drawable {

    private static final Color GRID_LINE = new Color(0, 0, 0, 40);

    protected int row;
    protected int col;
    private boolean explored;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
        this.explored = false;
    }

    // ----- Every tile must answer these -----

    public abstract boolean isWalkable();

    public abstract boolean isFlammable();

    /** Colour used when the tile's image is missing. */
    public abstract Color getFallbackColor();

    /** Name of the image in AssetManager, e.g. "floor" loads images/floor.png. */
    public abstract String getImageKey();

    // ----- Hooks: default "no", overridden by the tiles that need them -----

    /** Can the Engineer clear this tile? Only Rubble says yes. */
    public boolean isClearable() {
        return false;
    }

    /** Is this tile on fire? Only Fire says yes. Used for fire damage and spreading. */
    public boolean isBurning() {
        return false;
    }

    /** Can a random aftershock turn this tile into rubble? Only plain Floor says yes. */
    public boolean canCollapse() {
        return false;
    }

    /** Called on every tile when an aftershock hits. CrackedFloor reacts. */
    public void onAftershock(GameBoard board) {
        // Most tiles do nothing.
    }

    /** Called on every tile on every world tick. Fire reacts (it burns out). */
    public void onWorldTick(GameBoard board) {
        // Most tiles do nothing.
    }

    // ----- Fog of war -----

    public void markExplored() {
        explored = true;
    }

    public boolean isExplored() {
        return explored;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /** Draws the tile's image, or a coloured square if the image is missing. */
    @Override
    public void draw(Graphics2D g, int x, int y, int size, AssetManager assets) {
        BufferedImage image = assets.getImage(getImageKey());
        if (image != null) {
            g.drawImage(image, x, y, size, size, null);
        } else {
            g.setColor(getFallbackColor());
            g.fillRect(x, y, size, size);
            g.setColor(GRID_LINE);
            g.drawRect(x, y, size - 1, size - 1);
        }
    }
}
