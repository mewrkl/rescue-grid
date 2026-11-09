package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

/**
 * A short message near the bottom of the map that fades away after a few
 * seconds. The fade uses Scene2D actions: delay, then fadeOut.
 */
class Toast extends Label {

    private static final float SHOW_SECONDS = 3f;
    private static final float FADE_SECONDS = 0.4f;

    Toast(UiStyle style) {
        super(" ", style.toastStyle());
        setAlignment(Align.center);
        getColor().a = 0;
    }

    void show(String message) {
        if (message.isEmpty()) {
            return;
        }
        setText(message);
        clearActions();
        getColor().a = 1;
        addAction(Actions.sequence(Actions.delay(SHOW_SECONDS), Actions.fadeOut(FADE_SECONDS)));
    }

    void hideNow() {
        clearActions();
        getColor().a = 0;
    }
}
