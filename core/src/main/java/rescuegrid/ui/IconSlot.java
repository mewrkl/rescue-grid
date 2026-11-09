package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

import rescuegrid.model.Drawable;

/**
 * A space in a Scene2D layout where BaseScreen draws a game object,
 * so menus and the HUD use the real game art.
 */
class IconSlot extends Actor {

    private final Drawable drawable;

    IconSlot(Drawable drawable, float size) {
        this.drawable = drawable;
        setSize(size, size);
    }

    Drawable getDrawable() {
        return drawable;
    }
}
