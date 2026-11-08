package rescuegrid.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

/**
 * Remembers which keys are being held down right now.
 *
 * Swing tells us when a key goes down (keyPressed) and comes up (keyReleased).
 * By keeping the held keys in a HashSet, the game loop can ask "is W held?"
 * every frame, which is what makes hold-to-walk work.
 *
 * wasPressed() is for one-shot keys like Space or Tab: it is true only once per
 * press, even if the key is held down.
 */
public class KeyboardInput implements KeyListener {

    private final HashSet<Integer> heldKeys;
    private final HashSet<Integer> newPresses;

    public KeyboardInput() {
        this.heldKeys = new HashSet<>();
        this.newPresses = new HashSet<>();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // add() returns false if the key was already held (keyboard auto-repeat),
        // so a held key only counts as ONE new press.
        if (heldKeys.add(e.getKeyCode())) {
            newPresses.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        heldKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed: we only care about keys going down and up.
    }

    public boolean isHeld(int keyCode) {
        return heldKeys.contains(keyCode);
    }

    /** True once for each new press of this key. */
    public boolean wasPressed(int keyCode) {
        return newPresses.remove(keyCode);
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
