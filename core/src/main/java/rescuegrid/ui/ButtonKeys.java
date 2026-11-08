package rescuegrid.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Lets the menu screens be used without a mouse:
 *   Up / Down or Tab / Shift+Tab  move between buttons (the chosen one gets a blue ring),
 *   Enter or Space                presses the chosen button (Enter alone presses the first one),
 *   Esc                           goes back, if the screen has a "back" action.
 */
class ButtonKeys extends InputAdapter {

    private final Stage stage;
    private final ArrayList<TextButton> buttons;
    private final ArrayList<Runnable> actions;
    private Runnable backAction;

    ButtonKeys(Stage stage) {
        this.stage = stage;
        this.buttons = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.backAction = null;
    }

    void add(TextButton button, Runnable action) {
        buttons.add(button);
        actions.add(action);
    }

    void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    void clearFocus() {
        stage.setKeyboardFocus(null);
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean shift = isShiftHeld();
        if (keycode == Input.Keys.DOWN || (keycode == Input.Keys.TAB && !shift)) {
            moveFocus(1);
            return true;
        }
        if (keycode == Input.Keys.UP || (keycode == Input.Keys.TAB && shift)) {
            moveFocus(-1);
            return true;
        }
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER || keycode == Input.Keys.SPACE) {
            int focused = focusedIndex();
            if (focused >= 0) {
                actions.get(focused).run();
                return true;
            }
            int first = nextVisible(-1, 1);
            if (keycode != Input.Keys.SPACE && first >= 0) {
                actions.get(first).run();
                return true;
            }
            return false;
        }
        if (keycode == Input.Keys.ESCAPE && backAction != null) {
            backAction.run();
            return true;
        }
        return false;
    }

    private boolean isShiftHeld() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    /** Moves the blue ring to the next (or previous) visible button, wrapping around. */
    private void moveFocus(int step) {
        int next = nextVisible(focusedIndex(), step);
        if (next >= 0) {
            stage.setKeyboardFocus(buttons.get(next));
        }
    }

    /** The index of the next visible button after "from" (-1 = nothing chosen yet), or -1 if none. */
    private int nextVisible(int from, int step) {
        int count = buttons.size();
        int start = from >= 0 ? from : (step > 0 ? -1 : count);
        for (int i = 1; i <= count; i++) {
            int index = Math.floorMod(start + i * step, count);
            if (buttons.get(index).isVisible()) {
                return index;
            }
        }
        return -1;
    }

    private int focusedIndex() {
        Actor focused = stage.getKeyboardFocus();
        return focused == null ? -1 : buttons.indexOf(focused);
    }
}
