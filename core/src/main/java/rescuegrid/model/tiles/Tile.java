package rescuegrid.model.tiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;

/**
 * One square of the building. Subclasses answer the abstract questions
 * (walkable? flammable?) and override only the hook methods they need,
 * so the game never has to check which kind of tile it has.
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

    public abstract boolean isWalkable();

    public abstract boolean isFlammable();

    /** Base colour of the placeholder art (and of this tile on the minimap). */
    public abstract Color getFallbackColor();

    /** Name of the image in AssetManager, e.g. "floor" loads images/floor.png. */
    public abstract String getImageKey();

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

    /** Shape art used until sprites exist. Subclasses add their own details. */
    protected void drawFallback(Painter painter, float x, float y, float size) {
        painter.fillRect(getFallbackColor(), x, y, size, size);
        painter.outlineRect(GRID_LINE, x, y, size, size, 1);
    }

    /** Same random numbers every frame for this tile, so speckles don't flicker. */
    protected Random positionRandom() {
        return new Random(row * 7919L + col * 104729L);
    }
}
