package rescuegrid.ui;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import rescuegrid.assets.AssetManager;
import rescuegrid.assets.SoundManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.GameEvent;
import rescuegrid.logic.HazardManager;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.LevelLoader;
import rescuegrid.logic.MovementCalculator;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.GameState;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * The game screen: runs the game loop, reads the keyboard, and draws the part
 * of the building the camera is looking at, with the HUD on the right.
 *
 * THE GAME LOOP: libGDX calls render(delta) about 60 times a second, where
 * delta is the seconds since the last frame. Each time, update() moves the game
 * forward by delta, then the draw methods paint the new picture. Everything runs
 * on one thread, so we never have to think about thread safety.
 */
public class GamePanel extends BaseScreen {

    public static final int TILE_SIZE = 64;
    public static final int VIEW_COLS = 15;
    public static final int VIEW_ROWS = 11;
    public static final int VIEW_WIDTH = VIEW_COLS * TILE_SIZE;   // 960
    public static final int VIEW_HEIGHT = VIEW_ROWS * TILE_SIZE;  // 704

    private static final float MAX_FRAME_SECONDS = 0.1f;  // ignore huge gaps (e.g. window dragged)
    private static final int MINIMAP_SCALE = 4;           // pixels per tile
    private static final int MINIMAP_MARGIN = 16;
    private static final double AFTERSHOCK_SHAKE_SECONDS = 0.8;
    private static final int AFTERSHOCK_SHAKE_PIXELS = 10;
    private static final double WARNING_SHAKE_SECONDS = 1.0;
    private static final int WARNING_SHAKE_PIXELS = 2;

    private static final Color UNEXPLORED = Color.BLACK;
    private static final Color REMEMBERED = Painter.rgba(0, 0, 0, 150);
    private static final Color FACED_TILE = Painter.rgba(255, 255, 255, 150);
    private static final Color PAUSE_SCRIM = Painter.rgba(0, 0, 0, 170);

    private final AssetManager assets;
    private final SoundManager sounds;
    private final Painter painter;
    private final HudPanel hud;
    private final Table pauseCard;
    private final Camera camera;
    private final OrthographicCamera overlayCamera; // for things that never scroll (the minimap)
    private final KeyboardInput keys;
    private final Minimap minimap;
    private final LevelLoader loader;
    private final MovementCalculator movement;
    private final Random shakeRandom; // only for the screen shake, never for game rules

    private GameBoard board;
    private LevelData level;
    private TurnManager turns;
    private Rescuer activeRescuer;
    private GameState state;
    private double shakeSecondsLeft;
    private int shakePixels;

    public GamePanel(GameFrame game) {
        super(game);
        this.assets = game.getAssets();
        this.sounds = game.getSounds();
        this.painter = game.getPainter();
        this.hud = new HudPanel(style);
        this.pauseCard = buildPauseCard();
        this.camera = new Camera(VIEW_ROWS, VIEW_COLS, TILE_SIZE);
        this.overlayCamera = new OrthographicCamera();
        this.overlayCamera.setToOrtho(true, VIEW_WIDTH, VIEW_HEIGHT);
        this.keys = new KeyboardInput();
        this.minimap = new Minimap();
        this.loader = new LevelLoader();
        this.movement = new MovementCalculator();
        this.shakeRandom = new Random();
        this.state = GameState.MENU;

        // Left: the map area (the pause card floats in its middle). Right: a 1 px line and the HUD.
        Table mapArea = new Table();
        mapArea.add(pauseCard);
        root.add(mapArea).width(VIEW_WIDTH).expandY().fillY();
        root.add(new Image(style.solid(UiStyle.BORDER))).width(1).fillY();
        root.add(hud).width(HudPanel.WIDTH - 1).fillY();
    }

    /** A small card listing the pause keys. Hidden until Esc is pressed. */
    private Table buildPauseCard() {
        Table card = new Table();
        card.setBackground(style.card());
        card.defaults().left();
        card.add(style.pixelLabel("PAUSED", 24, UiStyle.TEXT)).colspan(2).padBottom(16).row();
        String[][] keysAndActions = {{"Esc", "Resume"}, {"R", "Restart level"}, {"M", "Main menu"}};
        for (String[] line : keysAndActions) {
            card.add(style.label(line[0], UiStyle.TEXT)).width(64).padTop(8);
            card.add(style.label(line[1], UiStyle.TEXT_SECONDARY)).width(160).padTop(8).row();
        }
        card.setVisible(false);
        return card;
    }

    /** Loads a level and starts playing. Throws IllegalArgumentException for a broken level. */
    public void startLevel(int levelNumber) {
        level = LevelData.forLevel(levelNumber);
        board = loader.load(level);
        turns = new TurnManager(board, new HazardManager(level.getSeed()), level);
        activeRescuer = board.getRescuers().get(0);
        minimap.setBoard(board);
        minimap.setActiveRescuer(activeRescuer);
        keys.clear();
        shakeSecondsLeft = 0;
        pauseCard.setVisible(false);
        movement.computeSight(board, activeRescuer);
        camera.follow(activeRescuer.getRow(), activeRescuer.getCol(), board.getRows(), board.getCols());
        hud.showMessage("Find the survivors and bring them to the EXIT");
        hud.refresh(board, activeRescuer, turns, level);

        sounds.loopMusic("gameplay");
        state = GameState.PLAYING;
    }

    /** Keyboard goes to our KeyboardInput (the stage gets it first, but has no keyboard widgets). */
    @Override
    protected InputProcessor createInput() {
        return new InputMultiplexer(stage, keys);
    }

    /** The window lost focus: forget held keys (or they'd stay "stuck") and pause. */
    public void windowLostFocus() {
        keys.clear();
        if (state == GameState.PLAYING) {
            togglePause();
        }
    }

    // ----- The game loop -----

    @Override
    public void render(float delta) {
        if (board == null) {
            return;
        }
        update(Math.min(delta, MAX_FRAME_SECONDS));
        if (state == GameState.LEVEL_COMPLETE || state == GameState.GAME_OVER || state == GameState.MENU) {
            return; // we just switched to another screen
        }
        ScreenUtils.clear(Color.BLACK);
        drawWorld();
        drawOverlay();
        drawStage(delta);
    }

    /** Moves the game forward by "seconds". */
    private void update(float seconds) {
        if (keys.wasPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }
        if (state == GameState.PAUSED) {
            handlePauseKeys();
            keys.endFrame();
            return;
        }
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
        handleEvents(turns.takeEvents());
        if (activeRescuer.isDowned()) {
            switchToNext();
        }
        movement.computeSight(board, activeRescuer); // the map may have changed, so look again
        shakeSecondsLeft = Math.max(0, shakeSecondsLeft - seconds);

        camera.follow(activeRescuer.getDrawRow(), activeRescuer.getDrawCol(), board.getRows(), board.getCols());
        hud.refresh(board, activeRescuer, turns, level);
        keys.endFrame();

        if (turns.isMissionOver()) {
            finishLevel();
        }
    }

    // ----- Keyboard -----

    /** One-shot keys: switching rescuer and the special action. */
    private void handleActionKeys() {
        if (keys.wasPressed(Input.Keys.NUM_1)) {
            switchTo(0);
        } else if (keys.wasPressed(Input.Keys.NUM_2)) {
            switchTo(1);
        } else if (keys.wasPressed(Input.Keys.NUM_3)) {
            switchTo(2);
        } else if (keys.wasPressed(Input.Keys.TAB)) {
            switchToNext();
        }

        // E is the ability key, as in most PC games (Q and E are the usual ability keys).
        if (keys.wasPressed(Input.Keys.E)) {
            try {
                // Polymorphism: Java runs the Medic's, Engineer's or Dog Handler's version.
                String result = activeRescuer.specialAction(board);
                sounds.play(activeRescuer.getSpecialSound());
                hud.showMessage(result);
            } catch (InvalidMoveException e) {
                sounds.play("denied");
                hud.showMessage(e.getMessage());
            }
        }
    }

    /** Hold-to-walk with WASD or the arrow keys. */
    private void handleMovement() {
        Direction direction = heldDirection();
        if (direction == null) {
            return;
        }
        activeRescuer.setFacing(direction); // turn even if the step is blocked
        if (!activeRescuer.canStep() || !movement.canStep(board, activeRescuer, direction)) {
            return;
        }
        activeRescuer.moveTo(activeRescuer.getRow() + direction.getRowChange(),
                activeRescuer.getCol() + direction.getColChange());
        sounds.play("step");
        afterStep();
    }

    /** Which direction key is held (or was just tapped), or null if none. */
    private Direction heldDirection() {
        if (keys.isDown(Input.Keys.W) || keys.isDown(Input.Keys.UP)) {
            return Direction.UP;
        }
        if (keys.isDown(Input.Keys.S) || keys.isDown(Input.Keys.DOWN)) {
            return Direction.DOWN;
        }
        if (keys.isDown(Input.Keys.A) || keys.isDown(Input.Keys.LEFT)) {
            return Direction.LEFT;
        }
        if (keys.isDown(Input.Keys.D) || keys.isDown(Input.Keys.RIGHT)) {
            return Direction.RIGHT;
        }
        return null;
    }

    /** After every step: deliver at the exit, or pick up a survivor on this tile. */
    private void afterStep() {
        int row = activeRescuer.getRow();
        int col = activeRescuer.getCol();
        Tile here = board.getTile(row, col);
        if (here.isExit() && activeRescuer.isCarrying()) {
            activeRescuer.deliverSurvivor();
            sounds.play("rescued");
            hud.showMessage("Survivor rescued! " + board.getRescuedCount() + " of " + level.getTarget());
            return;
        }
        if (!activeRescuer.isCarrying()) {
            Survivor survivor = board.getSurvivorAt(row, col);
            if (survivor != null) {
                survivor.reveal();
                activeRescuer.pickUp(survivor);
                sounds.play("pickup");
                hud.showMessage("Picked up a survivor. Carry them to the EXIT");
            }
        }
    }

    private void switchTo(int index) {
        Rescuer chosen = board.getRescuers().get(index);
        if (chosen == activeRescuer) {
            return;
        }
        if (chosen.isDowned()) {
            sounds.play("denied");
            hud.showMessage(chosen.getName() + " is down");
            return;
        }
        setActive(chosen);
    }

    /** Switches to the next rescuer in the list who is still standing. */
    private void switchToNext() {
        List<Rescuer> team = board.getRescuers();
        int current = team.indexOf(activeRescuer);
        for (int step = 1; step <= team.size(); step++) {
            Rescuer candidate = team.get((current + step) % team.size());
            if (!candidate.isDowned()) {
                if (candidate != activeRescuer) {
                    setActive(candidate);
                }
                return;
            }
        }
    }

    private void setActive(Rescuer rescuer) {
        activeRescuer = rescuer;
        minimap.setActiveRescuer(rescuer);
        movement.computeSight(board, rescuer);
        sounds.play("switch");
    }

    private void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            keys.clear();
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
        pauseCard.setVisible(state == GameState.PAUSED);
    }

    private void handlePauseKeys() {
        if (keys.wasPressed(Input.Keys.R)) {
            game.startLevel(level.getNumber());
        } else if (keys.wasPressed(Input.Keys.M)) {
            state = GameState.MENU;
            game.showMenu();
        }
    }

    // ----- Events from the world tick -----

    /** Turns what happened in the world tick into sounds, messages and screen shake. */
    private void handleEvents(List<GameEvent> events) {
        for (GameEvent event : events) {
            switch (event.getType()) {
                case RESCUER_HURT:
                    sounds.play("hurt");
                    break;
                case RESCUER_DOWNED:
                    sounds.play("downed");
                    break;
                case FIRE_SPREAD:
                    sounds.play("fire_spread");
                    break;
                case SURVIVOR_LOST:
                    sounds.play("survivor_lost");
                    break;
                case AFTERSHOCK_WARNING:
                    sounds.play("aftershock_warning");
                    shake(WARNING_SHAKE_SECONDS, WARNING_SHAKE_PIXELS);
                    break;
                case AFTERSHOCK:
                    sounds.play("aftershock");
                    shake(AFTERSHOCK_SHAKE_SECONDS, AFTERSHOCK_SHAKE_PIXELS);
                    break;
                default:
                    break;
            }
            if (!event.getMessage().isEmpty()) {
                hud.showMessage(event.getMessage());
            }
        }
    }

    private void shake(double seconds, int pixels) {
        shakeSecondsLeft = seconds;
        shakePixels = pixels;
    }

    /** The mission is over: show the result screen. */
    private void finishLevel() {
        boolean success = turns.isSuccess();
        state = success ? GameState.LEVEL_COMPLETE : GameState.GAME_OVER;
        sounds.stopMusic();
        sounds.play(success ? "success" : "failure");
        game.showResult(success, level, board, turns.getElapsedSeconds());
    }

    // ----- Drawing -----

    /** The building, seen through the camera. Shaken during aftershocks. */
    private void drawWorld() {
        float shakeX = 0;
        float shakeY = 0;
        if (shakeSecondsLeft > 0) {
            shakeX = shakeRandom.nextInt(2 * shakePixels + 1) - shakePixels;
            shakeY = shakeRandom.nextInt(2 * shakePixels + 1) - shakePixels;
        }
        // Only the left part of the window shows the map; the HUD is on the right.
        HdpiUtils.glViewport(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        painter.begin(camera.getProjection(shakeX, shakeY));
        drawTiles();
        drawFog();
        drawSurvivors();
        drawFacedTile();
        drawRescuers();
        painter.end();
    }

    /** Things on top of the map that never scroll or shake: the minimap and the pause veil. */
    private void drawOverlay() {
        painter.begin(overlayCamera.combined);
        float width = board.getCols() * MINIMAP_SCALE;
        minimap.draw(painter, VIEW_WIDTH - width - MINIMAP_MARGIN, MINIMAP_MARGIN, MINIMAP_SCALE, assets);
        if (state == GameState.PAUSED) {
            painter.fillRect(PAUSE_SCRIM, 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        }
        painter.end();
    }

    /** Only the tiles inside the camera window are drawn (about 190, not the whole map). */
    private void drawTiles() {
        int lastRow = Math.min(camera.getFirstRow() + VIEW_ROWS + 1, board.getRows());
        int lastCol = Math.min(camera.getFirstCol() + VIEW_COLS + 1, board.getCols());
        for (int row = camera.getFirstRow(); row < lastRow; row++) {
            for (int col = camera.getFirstCol(); col < lastCol; col++) {
                // Polymorphism: every tile draws itself, we never check what kind it is.
                board.getTile(row, col).draw(painter, camera.toMapX(col), camera.toMapY(row), TILE_SIZE, assets);
            }
        }
    }

    /** Black over unexplored tiles, a dark veil over tiles you remember but can't see now. */
    private void drawFog() {
        int lastRow = Math.min(camera.getFirstRow() + VIEW_ROWS + 1, board.getRows());
        int lastCol = Math.min(camera.getFirstCol() + VIEW_COLS + 1, board.getCols());
        for (int row = camera.getFirstRow(); row < lastRow; row++) {
            for (int col = camera.getFirstCol(); col < lastCol; col++) {
                if (board.isVisible(row, col)) {
                    continue;
                }
                Color veil = board.getTile(row, col).isExplored() ? REMEMBERED : UNEXPLORED;
                painter.fillRect(veil, camera.toMapX(col), camera.toMapY(row), TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /** Survivors only appear once revealed (seen, or found by the dog). */
    private void drawSurvivors() {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && survivor.isRevealed()
                    && camera.isInView(survivor.getRow(), survivor.getCol())) {
                survivor.draw(painter, camera.toMapX(survivor.getCol()), camera.toMapY(survivor.getRow()),
                        TILE_SIZE, assets);
            }
        }
    }

    /** A dashed outline on the tile the active rescuer faces: that's where E acts. */
    private void drawFacedTile() {
        Tile faced = activeRescuer.getFacedTile(board);
        if (faced == null || state != GameState.PLAYING) {
            return;
        }
        painter.dashedRect(FACED_TILE, camera.toMapX(faced.getCol()) + 3, camera.toMapY(faced.getRow()) + 3,
                TILE_SIZE - 6, TILE_SIZE - 6, 2, 6);
    }

    private void drawRescuers() {
        for (Rescuer rescuer : board.getRescuers()) {
            if (rescuer.isDowned() || !camera.isInView(rescuer.getDrawRow(), rescuer.getDrawCol())) {
                continue;
            }
            float x = camera.toMapX(rescuer.getDrawCol());
            float y = camera.toMapY(rescuer.getDrawRow());
            if (rescuer == activeRescuer) {
                painter.outlineRect(Color.WHITE, x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 2);
            }
            rescuer.draw(painter, x, y, TILE_SIZE, assets);
        }
    }
}
