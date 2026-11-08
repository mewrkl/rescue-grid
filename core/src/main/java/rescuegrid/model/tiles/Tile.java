package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
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

    private static final Color GRID_LINE = Painter.rgba(0, 0, 0, 40);

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

    /** Base colour of the placeholder art (and of this tile on the minimap). */
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

    /** Is this the way out? Only Exit says yes. Used for delivering survivors. */
    public boolean isExit() {
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

    // ----- Drawing -----

    /** Draws the tile's image, or the placeholder art if the image is missing. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getImage(getImageKey());
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFallback(painter, x, y, size);
        }
    }

    /**
     * Placeholder art made of shapes, used until sprites exist. This version is a
     * plain coloured square; subclasses override it and add their own details
     * (bricks, cracks, flames...).
     */
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(getFallbackColor(), x, y, size, size);
        painter.outlineRect(GRID_LINE, x, y, size, size, 1);
    }

    /**
     * A random-number generator that gives the same numbers every time for this
     * tile's position. Used so placeholder details (speckles, rocks) stay in the
     * same place every frame instead of jumping around.
     */
    protected Random positionRandom() {
        return new Random(row * 7919L + col * 104729L);
    }
}
