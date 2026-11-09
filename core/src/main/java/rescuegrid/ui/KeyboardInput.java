package rescuegrid.ui;

import java.util.HashSet;

import com.badlogic.gdx.InputAdapter;

/** Tracks held keys (for hold-to-walk) and new presses (for one-shot keys like E). */
public class KeyboardInput extends InputAdapter {

    private final HashSet<Integer> heldKeys;
    private final HashSet<Integer> newPresses;

    public KeyboardInput() {
        this.heldKeys = new HashSet<>();
        this.newPresses = new HashSet<>();
    }

    @Override
    public boolean keyDown(int keycode) {
        // add() returns false if the key was already held, so a held key only
        // counts as one new press.
        if (heldKeys.add(keycode)) {
            newPresses.add(keycode);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        heldKeys.remove(keycode);
        return true;
    }

    public boolean isHeld(int keycode) {
        return heldKeys.contains(keycode);
    }

    /**
     * Held now, OR pressed since the last frame. A very quick tap can go down and
     * up between two frames; this still counts it, so a tap always turns the rescuer.
     */
    public boolean isDown(int keycode) {
        return heldKeys.contains(keycode) || newPresses.contains(keycode);
    }

    /** True once for each new press of this key. */
    public boolean wasPressed(int keycode) {
        return newPresses.remove(keycode);
    }

    /** Called at the end of every frame: forget presses nobody asked about. */
    public void endFrame() {
        newPresses.clear();
    }

    /** Forget everything (e.g. when the window loses focus mid-press). */
    public void clear() {
        heldKeys.clear();
        newPresses.clear();
    }
}
