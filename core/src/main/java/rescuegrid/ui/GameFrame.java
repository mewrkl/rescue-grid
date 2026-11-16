package rescuegrid.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import rescuegrid.assets.AssetManager;
import rescuegrid.assets.SaveFile;
import rescuegrid.assets.SoundManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.MissionReport;
import rescuegrid.logic.Progress;
import rescuegrid.model.GameBoard;

/**
 * The game: switches between screens and owns the shared images, sounds,
 * fonts, Painter and the player's saved progress (open levels, best stars).
 * F11 switches fullscreen on any screen; the choice is saved.
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
    private LevelSelectPanel levelSelectPanel;
    private OptionsPanel optionsPanel;
    private TrainingPromptPanel trainingPromptPanel;
    private SaveFile saveFile;
    private Progress progress;
    private Preferences settings;

    private static final String SETTINGS_NAME = "rescuegrid";
    private static final String FULLSCREEN_KEY = "fullscreen";

    /** Called once by libGDX when the window is ready. */
    @Override
    public void create() {
        assets = new AssetManager();
        assets.loadAll();
        sounds = new SoundManager();
        sounds.loadAll();
        style = new UiStyle();
        painter = new Painter(style.getSignFont());
        saveFile = new SaveFile();
        progress = saveFile.load();

        menuPanel = new MenuPanel(this);
        howToPlayPanel = new HowToPlayPanel(this);
        gamePanel = new GamePanel(this);
        resultPanel = new ResultPanel(this);
        levelSelectPanel = new LevelSelectPanel(this);
        optionsPanel = new OptionsPanel(this);
        trainingPromptPanel = new TrainingPromptPanel(this);
        settings = Gdx.app.getPreferences(SETTINGS_NAME);
        if (settings.getBoolean(FULLSCREEN_KEY, false)) {
            setFullscreen(true);
        }
        showMenu();
    }

    /** Every frame: F11 switches fullscreen on any screen, then the current screen draws. */
    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }
        super.render();
    }

    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    /** Fullscreen on or off, and saved for next time. The screens scale to fit (see BaseScreen). */
    public void toggleFullscreen() {
        setFullscreen(!isFullscreen());
        settings.putBoolean(FULLSCREEN_KEY, isFullscreen());
        settings.flush();
    }

    private void setFullscreen(boolean on) {
        if (on) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
    }

    /**
     * PLAY on the main menu (decision 0024). The first time, it asks whether to play the training;
     * after the player has finished or skipped it once, it goes straight to Level 1.
     */
    public void play() {
        if (progress.isTrainingDecided()) {
            startLevel(1);
        } else {
            setScreen(trainingPromptPanel);
        }
    }

    /** CONTINUE on the main menu: the furthest level that is open. */
    public void continueGame() {
        startLevel(progress.getContinueLevel());
    }

    /** The player chose to skip the training: don't ask again. */
    public void skipTraining() {
        progress.markTrainingDecided();
        saveFile.save(progress);
    }

    /** Options: forget all saved progress. */
    public void resetProgress() {
        progress.reset();
        saveFile.save(progress);
    }

    /** Options from the main menu or the pause menu; BACK returns to where it came from. */
    public void showOptions(Runnable back) {
        optionsPanel.setBack(back);
        setScreen(optionsPanel);
    }

    /** How to Play from the main menu or the pause menu; BACK returns to where it came from. */
    public void showHowToPlay(Runnable back) {
        howToPlayPanel.setBack(back);
        setScreen(howToPlayPanel);
    }

    /** Back to the paused level from Options or How to Play (the level carries on where it was). */
    public void resumeGame() {
        setScreen(gamePanel);
    }

    public void showMenu() {
        setScreen(menuPanel);
        sounds.loopMusic("menu");
    }

    public void showLevelSelect() {
        setScreen(levelSelectPanel);
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

    /** Saves the stars (and opens the next level on a success), then shows the result screen. */
    public void showResult(boolean success, LevelData level, GameBoard board, double seconds) {
        MissionReport report = new MissionReport(success, board, level);
        int stars = level.isTutorial() ? 0 : report.getStars();
        boolean newBest = progress.record(level.getNumber(), stars, success);
        saveFile.save(progress);
        resultPanel.showResult(success, level, board, seconds, report, newBest);
        setScreen(resultPanel);
    }

    public Progress getProgress() {
        return progress;
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
        levelSelectPanel.dispose();
        optionsPanel.dispose();
        trainingPromptPanel.dispose();
        painter.dispose();
        style.dispose();
        sounds.dispose();
        assets.dispose();
    }
}
