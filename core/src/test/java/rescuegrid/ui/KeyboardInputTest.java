package rescuegrid.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Input;

/**
 * wasPressed() uses the press up. Asking twice in one frame gets false the second
 * time, so GamePanel must read each one-shot key once (asking twice broke the
 * role keys, Q and E).
 */
class KeyboardInputTest {

    @Test
    void aPressCountsOnlyOnce() {
        KeyboardInput keys = new KeyboardInput();
        keys.keyDown(Input.Keys.NUM_2);
        assertTrue(keys.wasPressed(Input.Keys.NUM_2));
        assertFalse(keys.wasPressed(Input.Keys.NUM_2), "a second read in the same frame gets nothing");
        assertTrue(keys.isHeld(Input.Keys.NUM_2), "the key is still held");
    }

    @Test
    void holdingAKeyIsNotANewPress() {
        KeyboardInput keys = new KeyboardInput();
        keys.keyDown(Input.Keys.E);
        keys.endFrame();
        keys.keyDown(Input.Keys.E); // key repeat while held
        assertFalse(keys.wasPressed(Input.Keys.E));
    }
}
