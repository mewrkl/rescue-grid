package rescuegrid.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import rescuegrid.assets.AssetManager;
import rescuegrid.assets.SoundManager;
import rescuegrid.logic.LevelData;

/**
 * The game window. It holds every screen in a CardLayout, which works like a
 * deck of cards: all screens exist, but only the one on top is shown.
 */
public class GameFrame extends JFrame {

    public static final String MENU = "menu";
    public static final String HOW_TO_PLAY = "howToPlay";
    public static final String GAME = "game";
    public static final String RESULT = "result";

    private final CardLayout cards;
    private final JPanel screens;
    private final AssetManager assets;
    private final SoundManager sounds;
    private final GamePanel gamePanel;
    private final ResultPanel resultPanel;

    public GameFrame() {
        super("Rescue Grid");

        assets = new AssetManager();
        assets.loadAll();
        sounds = new SoundManager();
        sounds.loadAll();

        HudPanel hudPanel = new HudPanel();
        gamePanel = new GamePanel(this, assets, hudPanel);
        resultPanel = new ResultPanel(this);

        // The game screen is the map on the left with the HUD on the right.
        JPanel gameScreen = new JPanel(new BorderLayout());
        gameScreen.add(gamePanel, BorderLayout.CENTER);
        gameScreen.add(hudPanel, BorderLayout.EAST);

        cards = new CardLayout();
        screens = new JPanel(cards);
        screens.add(new MenuPanel(this), MENU);
        screens.add(new HowToPlayPanel(this), HOW_TO_PLAY);
        screens.add(gameScreen, GAME);
        screens.add(resultPanel, RESULT);

        setContentPane(screens);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();                       // size the window to fit the biggest screen
        setLocationRelativeTo(null);  // centre it on the monitor
        showScreen(MENU);
    }

    public void showScreen(String name) {
        cards.show(screens, name);
    }

    /** Loads a level and switches to the game screen. */
    public void startLevel(int levelNumber) {
        try {
            gamePanel.startLevel(levelNumber);
        } catch (IllegalArgumentException e) {
            // A missing or broken level file: say exactly what is wrong instead of crashing.
            JOptionPane.showMessageDialog(this, e.getMessage(), "Could not load level",
                    JOptionPane.ERROR_MESSAGE);
            showScreen(MENU);
            return;
        }
        showScreen(GAME);
        gamePanel.requestFocusInWindow(); // keyboard input goes to the game, not a button
    }

    public void showResult(boolean success, int rescued, LevelData level, int score) {
        resultPanel.showResult(success, rescued, level, score);
        showScreen(RESULT);
    }
}
