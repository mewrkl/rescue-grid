package rescuegrid.ui;

import com.badlogic.gdx.Game;

import rescuegrid.assets.AssetManager;
import rescuegrid.assets.SoundManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.LevelData;
import rescuegrid.model.GameBoard;

/**
 * The game: switches between screens and owns the shared images, sounds,
 * fonts and Painter.
 */
public class GameFrame extends Game {

    public static final int WINDOW_WIDTH = GamePanel.VIEW_WIDTH + HudPanel.WIDTH; // 1216
    public static final int WINDOW_HEIGHT = GamePanel.VIEW_HEIGHT;                // 704

    private AssetManager assets;
    private SoundManager sounds;
    private UiStyle style;
    private Painter painter;
    private MenuPanel menuPanel;
    private HowToPlayPanel howToPlayPanel;
    private GamePanel gamePanel;
    private ResultPanel resultPanel;

    /** Called once by libGDX when the window is ready. */
    @Override
    public void create() {
        assets = new AssetManager();
        assets.loadAll();
        sounds = new SoundManager();
        sounds.loadAll();
        style = new UiStyle();
        painter = new Painter(style.getSignFont());

        menuPanel = new MenuPanel(this);
        howToPlayPanel = new HowToPlayPanel(this);
        gamePanel = new GamePanel(this);
        resultPanel = new ResultPanel(this);
        showMenu();
    }

    public void showMenu() {
        setScreen(menuPanel);
        sounds.loopMusic("menu");
    }

    public void showHowToPlay() {
        setScreen(howToPlayPanel);
        sounds.loopMusic("menu");
    }

    /** Loads a level and switches to the game screen. */
    public void startLevel(int levelNumber) {
        try {
            gamePanel.startLevel(levelNumber);
        } catch (IllegalArgumentException e) {
            // A missing or broken level file: say exactly what is wrong instead of crashing.
            showMenu();
            menuPanel.showError("Could not load level " + levelNumber + ". " + e.getMessage());
            return;
        }
        setScreen(gamePanel);
    }

    public void showResult(boolean success, LevelData level, GameBoard board, double seconds) {
        resultPanel.showResult(success, level, board, seconds);
        setScreen(resultPanel);
    }

    /** Lets the screens play a sound (e.g. button clicks). */
    public void playSound(String key) {
        sounds.play(key);
    }

    /** Called by the desktop launcher when the player clicks away from the window. */
    public void windowLostFocus() {
        if (getScreen() == gamePanel) {
            gamePanel.windowLostFocus();
        }
    }

    public AssetManager getAssets() {
        return assets;
    }

    public SoundManager getSounds() {
        return sounds;
    }

    public UiStyle getStyle() {
        return style;
    }

    public Painter getPainter() {
        return painter;
    }

    /**
     * Called once when the window closes. Textures, fonts and sounds live outside
     * Java's normal memory, so they must be freed by hand.
     */
    @Override
    public void dispose() {
        super.dispose();
        menuPanel.dispose();
        howToPlayPanel.dispose();
        gamePanel.dispose();
        resultPanel.dispose();
        painter.dispose();
        style.dispose();
        sounds.dispose();
        assets.dispose();
    }
}
