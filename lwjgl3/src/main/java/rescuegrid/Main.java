package rescuegrid;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

import rescuegrid.ui.GameFrame;

/**
 * Starting point of the program on desktop. Opens the game window with
 * libGDX's LWJGL3 backend and hands control to GameFrame (in the core module).
 *
 * From then on libGDX runs the game loop: it calls render() on the current
 * screen about 60 times a second until the window is closed.
 */
public class Main {

    public static void main(String[] args) {
        GameFrame game = new GameFrame();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Rescue Grid");
        config.setWindowedMode(GameFrame.WINDOW_WIDTH, GameFrame.WINDOW_HEIGHT);
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);
        // For testing: RESCUEGRID_SAVE_DIR keeps settings and progress in another folder,
        // so a test run never changes a player's real save (or opens in their fullscreen).
        String saveDir = System.getenv("RESCUEGRID_SAVE_DIR");
        if (saveDir != null && !saveDir.isEmpty()) {
            config.setPreferencesConfig(saveDir, Files.FileType.Absolute);
        }
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void focusLost() {
                game.windowLostFocus(); // pause, and forget keys that were held down
            }
        });

        new Lwjgl3Application(game, config);
    }
}
