package rescuegrid.model;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;

/**
 * Anything that can draw itself on the screen.
 *
 * Tiles, rescuers, survivors and the minimap all implement this, so the game
 * screen can draw them without knowing what kind of object each one is.
 */
public interface Drawable {

    /**
     * Draws this object with its top-left corner at (x, y).
     *
     * @param size   width and height in pixels (64 for tiles, pixels-per-tile for the minimap)
     * @param assets where to get images from; an image may be missing (null)
     */
    void draw(Painter painter, float x, float y, float size, AssetManager assets);
}
