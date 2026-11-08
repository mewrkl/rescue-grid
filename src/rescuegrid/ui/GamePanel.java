package rescuegrid.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

import rescuegrid.assets.AssetManager;
import rescuegrid.logic.HazardManager;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.LevelLoader;
import rescuegrid.logic.MovementCalculator;
import rescuegrid.logic.ScoreKeeper;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.GameBoard;
import rescuegrid.model.GameState;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The game screen: owns the game loop and draws the part of the building the
 * camera is looking at.
 *
 * THE GAME LOOP: one javax.swing.Timer fires about 60 times a second. Each time,
 * update() moves the game forward a little, then repaint() asks Swing to call
 * paintComponent(). The Timer runs on Swing's own thread, so the whole game is
 * single-threaded and we never have to think about thread safety.
 */
public class GamePanel extends JPanel {

    public static final int TILE_SIZE = 64;
    public static final int VIEW_COLS = 15;
    public static final int VIEW_ROWS = 11;

    private static final int FRAME_MILLIS = 16; // about 60 frames per second
    private static final int MINIMAP_SCALE = 4; // pixels per tile
    private static final int MINIMAP_MARGIN = 16;

    private final GameFrame frame;
    private final AssetManager assets;
    private final HudPanel hud;
    private final Camera camera;
    private final KeyboardInput keys;
    private final Minimap minimap;
    private final LevelLoader loader;
    private final MovementCalculator movement;
    private final Timer gameLoop;

    private GameBoard board;
    private LevelData level;
    private TurnManager turns;
    private Rescuer activeRescuer;
    private GameState state;
    private long lastFrameNanos;

    public GamePanel(GameFrame frame, AssetManager assets, HudPanel hud) {
        this.frame = frame;
        this.assets = assets;
        this.hud = hud;
        this.camera = new Camera(VIEW_ROWS, VIEW_COLS, TILE_SIZE);
        this.keys = new KeyboardInput();
        this.minimap = new Minimap();
        this.loader = new LevelLoader();
        this.movement = new MovementCalculator();
        this.state = GameState.MENU;

        setPreferredSize(new Dimension(VIEW_COLS * TILE_SIZE, VIEW_ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false); // lets Tab reach our KeyListener instead of moving focus
        addKeyListener(keys);

        gameLoop = new Timer(FRAME_MILLIS, e -> {
            update();
            repaint();
        });
    }

    /** Loads a level and starts the game loop. */
    public void startLevel(int levelNumber) {
        level = LevelData.forLevel(levelNumber);
        board = loader.load(level);
        turns = new TurnManager(board, new HazardManager(level.getSeed()), level);
        activeRescuer = board.getRescuers().get(0);
        minimap.setBoard(board);
        keys.clear();
        hud.showMessage("");
        camera.follow(activeRescuer, board.getRows(), board.getCols());
        hud.refresh(board, activeRescuer, turns, level);

        state = GameState.PLAYING;
        lastFrameNanos = System.nanoTime();
        gameLoop.start();
    }

    /** Called about 60 times a second by the game loop. */
    private void update() {
        // How much real time passed since the last frame? (nanoTime is in billionths of a second)
        long now = System.nanoTime();
        double seconds = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;

        // TODO (Week 4, Member 3): Esc toggles between PLAYING and PAUSED (show a pause overlay).
        if (state != GameState.PLAYING) {
            keys.endFrame();
            return;
        }

        handleActionKeys();
        handleMovement();
        for (Rescuer rescuer : board.getRescuers()) {
            rescuer.tick(seconds);
        }
        turns.update(seconds);

        camera.follow(activeRescuer, board.getRows(), board.getCols());
        hud.refresh(board, activeRescuer, turns, level);
        keys.endFrame();

        if (turns.isMissionOver()) {
            finishLevel();
        }
    }

    /** One-shot keys: switching rescuer and the special action. */
    private void handleActionKeys() {
        // TODO (Week 2, Member 2): switching rescuers.
        //  keys.wasPressed(KeyEvent.VK_1) -> activeRescuer = board.getRescuers().get(0), same for 2 and 3.
        //  keys.wasPressed(KeyEvent.VK_TAB) -> the next rescuer in the list that isn't downed.
        //  After switching, call movement.computeSight(board, activeRescuer).

        if (keys.wasPressed(KeyEvent.VK_SPACE)) {
            try {
                // Polymorphism: Java runs the Medic's, Engineer's or Dog Handler's version.
                activeRescuer.specialAction(board);
                hud.showMessage("");
            } catch (InvalidMoveException e) {
                hud.showMessage(e.getMessage());
            }
        }
    }

    /** Hold-to-walk with WASD or the arrow keys. */
    private void handleMovement() {
        // TODO (Week 1, Member 2):
        //  1. Work out which Direction is held:
        //       keys.isHeld(KeyEvent.VK_W) || keys.isHeld(KeyEvent.VK_UP)  -> Direction.UP, and so on.
        //  2. If no direction key is held, return.
        //  3. activeRescuer.setFacing(direction)  (turn even if the step is blocked).
        //  4. If activeRescuer.canStep() && movement.canStep(board, activeRescuer, direction):
        //       activeRescuer.moveTo(row + direction.getRowChange(), col + direction.getColChange()).
        //  Later (Weeks 2-3): after moving, computeSight, pick up a survivor on the new tile,
        //  and deliver the carried survivor if the new tile is the Exit.
    }

    /** The mission is over: stop the loop and show the result screen. */
    private void finishLevel() {
        gameLoop.stop();
        boolean success = turns.isSuccess();
        state = success ? GameState.LEVEL_COMPLETE : GameState.GAME_OVER;
        int score = new ScoreKeeper().calculateScore(board, turns.getElapsedSeconds());
        frame.showResult(success, board.getRescuedCount(), level, score);
    }

    // ----- Drawing -----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        drawTiles(g2);
        drawSurvivors(g2);
        drawRescuers(g2);
        drawFog(g2);
        drawMinimap(g2);
    }

    /** Only the tiles inside the camera window are drawn (about 165, not the whole map). */
    private void drawTiles(Graphics2D g) {
        int lastRow = Math.min(camera.getOffsetRow() + VIEW_ROWS, board.getRows());
        int lastCol = Math.min(camera.getOffsetCol() + VIEW_COLS, board.getCols());
        for (int row = camera.getOffsetRow(); row < lastRow; row++) {
            for (int col = camera.getOffsetCol(); col < lastCol; col++) {
                // Polymorphism: every tile draws itself, we never check what kind it is.
                board.getTile(row, col).draw(g, camera.toScreenX(col), camera.toScreenY(row), TILE_SIZE, assets);
            }
        }
    }

    private void drawSurvivors(Graphics2D g) {
        for (Survivor survivor : board.getSurvivors()) {
            // TODO (Week 2): only draw survivors that are revealed: add survivor.isRevealed() here.
            if (survivor.isWaiting() && camera.isInView(survivor.getRow(), survivor.getCol())) {
                survivor.draw(g, camera.toScreenX(survivor.getCol()), camera.toScreenY(survivor.getRow()),
                        TILE_SIZE, assets);
            }
        }
    }

    private void drawRescuers(Graphics2D g) {
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isDowned() || !camera.isInView(rescuer.getRow(), rescuer.getCol())) {
                continue;
            }
            int x = camera.toScreenX(rescuer.getCol());
            int y = camera.toScreenY(rescuer.getRow());
            rescuer.draw(g, x, y, TILE_SIZE, assets);
            if (rescuer == activeRescuer) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawRect(x + 1, y + 1, TILE_SIZE - 3, TILE_SIZE - 3);
            }
        }
        // TODO (Week 2): outline the tile the active rescuer is facing, so players know
        //  where Space will act.
    }

    private void drawFog(Graphics2D g) {
        // TODO (Week 2, Member 1): for every tile in the camera window:
        //  - not explored             -> fill it black
        //  - explored but not visible -> fill it with new Color(0, 0, 0, 150) (dimmed)
        //  - visible                  -> draw nothing
    }

    private void drawMinimap(Graphics2D g) {
        int width = board.getCols() * MINIMAP_SCALE;
        minimap.draw(g, getWidth() - width - MINIMAP_MARGIN, MINIMAP_MARGIN, MINIMAP_SCALE, assets);
    }
}
