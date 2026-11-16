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

    /** Can the Medic practise Treat on this tile? Only the training dummy says yes. */
    public boolean isTreatable() {
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

    /** Will the next aftershock collapse this tile no matter what? Only CrackedFloor says yes. */
    public boolean collapsesInAftershock() {
        return false;
    }

    /**
     * Does the dog's scent pass through this tile? Wherever you can see through,
     * and also rubble and taped doors (Rubble and Barrier say yes).
     */
    public boolean letsScentThrough() {
        return isSeeThrough();
    }

    /** What to call this tile when someone walks into it ("Locker"), or null for plain walls etc. */
    public String getBumpName() {
        return null;
    }

    /** Does opening this training gate number remove this tile? Only Barrier says yes. */
    public boolean opensWithGate(int gate) {
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

    /** Draws the tile's sprite (frame getImageFrame() of its sheet), or the placeholder art if it's missing. */
    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        TextureRegion image = assets.getFrame(getImageKey(), 0, getImageFrame(),
                AssetManager.TILE_PIXELS, AssetManager.TILE_PIXELS);
        if (image != null) {
            painter.image(image, x, y, size, size);
        } else {
            drawFallback(painter, x, y, size);
        }
    }

    /**
     * Draws the tile on the map, where it can see its neighbours. By default: the
     * tile, then the soft shadow a wall casts on the tile below it or to its right
     * (light comes from the top-left). Wall overrides this to pick its shape.
     */
    public void drawOnBoard(Painter painter, float x, float y, float size, AssetManager assets, GameBoard board) {
        draw(painter, x, y, size, assets);
        if (isWall()) {
            return;
        }
        drawShadow(painter, x, y, size, assets, board, -1, 0, Wall.SHADOW_BELOW);
        drawShadow(painter, x, y, size, assets, board, 0, -1, Wall.SHADOW_RIGHT);
    }

    private void drawShadow(Painter painter, float x, float y, float size, AssetManager assets, GameBoard board,
                            int rowChange, int colChange, int frame) {
        Tile next = board.getTile(row + rowChange, col + colChange);
        if (next != null && next.isWall()) {
            TextureRegion shadow = assets.getFrame("wall", 0, frame, AssetManager.TILE_PIXELS,
                    AssetManager.TILE_PIXELS);
            if (shadow != null) {
                painter.image(shadow, x, y, size, size);
            }
        }
    }

    /** Which frame of the sprite sheet to draw: a variant, or an animation frame. 0 by default. */
    protected int getImageFrame() {
        return 0;
    }

    /** Is this a wall (it gets the wall's shape and casts shadows)? Only Wall says yes. */
    public boolean isWall() {
        return false;
    }

    /**
     * Can sight and the dog's scent pass over this tile? Anything you can walk on,
     * plus low furniture (Prop) that blocks walking but not looking.
     */
    public boolean isSeeThrough() {
        return isWalkable();
    }

    /** Picks one of several looks for this tile, the same one every frame (weights: how common each is). */
    protected int pickVariant(int... weights) {
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }
        int roll = positionRandom().nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return i;
            }
        }
        return 0;
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
