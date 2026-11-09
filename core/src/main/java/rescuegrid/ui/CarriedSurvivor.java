package rescuegrid.ui;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;
import rescuegrid.model.Survivor;

/** Draws whoever the active rescuer is carrying, condition bar included, or nothing. */
class CarriedSurvivor implements Drawable {

    private Survivor survivor;

    void set(Survivor survivor) {
        this.survivor = survivor;
    }

    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        if (survivor != null) {
            survivor.draw(painter, x, y, size, assets);
        }
    }
}
