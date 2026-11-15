package rescuegrid.ui;

import com.badlogic.gdx.graphics.Color;

import rescuegrid.assets.AssetManager;
import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;
import rescuegrid.model.Survivor;
import rescuegrid.model.survivors.SeriousSurvivor;

/**
 * One slot in the HUD's survivor row. A saved survivor shows their picture,
 * a survivor still inside is an empty box, and a lost one is a red cross.
 * Boxes for survivors you must save are solid; the optional ones are dashed.
 */
class SurvivorPip implements Drawable {

    enum State { INSIDE, SAVED, LOST }

    private static final Color NEEDED_BOX = Painter.rgba(255, 255, 255, 110);
    private static final Color OPTIONAL_BOX = Painter.rgba(255, 255, 255, 60);

    private final Survivor picture = new SeriousSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
    private State state = State.INSIDE;
    private boolean needed = true;

    void set(State state, boolean needed) {
        this.state = state;
        this.needed = needed;
    }

    @Override
    public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
        if (state == State.SAVED) {
            picture.draw(painter, x, y, size, assets);
            return;
        }
        if (needed) {
            painter.outlineRect(NEEDED_BOX, x + 2, y + 2, size - 4, size - 4, 1);
        } else {
            painter.dashedRect(OPTIONAL_BOX, x + 2, y + 2, size - 4, size - 4, 1, 4);
        }
        if (state == State.LOST) {
            float inset = size / 4;
            painter.line(UiStyle.ERROR, x + inset, y + inset, x + size - inset, y + size - inset, 2);
            painter.line(UiStyle.ERROR, x + size - inset, y + inset, x + inset, y + size - inset, 2);
        }
    }
}
