package rescuegrid.model;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;

/** Anything that can draw itself: tiles, rescuers, survivors, the minimap. */
public interface Drawable {

    /**
     * Draws this object with its top-left corner at (x, y).
     *
     * @param size   width and height in pixels (64 for tiles, pixels-per-tile for the minimap)
     * @param assets where to get images from; an image may be missing (null)
     */
    void draw(Painter painter, float x, float y, float size, AssetManager assets);
}
