package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

import rescuegrid.model.Drawable;

/**
 * An empty, fixed-size space in a Scene2D layout that marks where a game
 * object (a rescuer, a tile, a survivor) should be drawn, e.g. next to its
 * explanation on the How to Play screen.
 *
 * Scene2D decides WHERE the slot goes; after the UI is drawn, BaseScreen asks
 * each slot's Drawable to draw itself there with our Painter. That way the menu
 * screens show the real game art (and later the real sprites) with no copies.
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
