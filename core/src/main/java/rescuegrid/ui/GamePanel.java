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
import rescuegrid.logic.ObjectiveTracker;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.GameState;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.tiles.Tile;

/**
 * The game screen. libGDX calls render(delta) about 60 times a second;
 * update() moves the game forward by delta and the draw methods paint it.
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
    private static final Color GEAR_BAR_BACK = Painter.rgba(0, 0, 0, 180);
    private static final double WARNING_PULSES_PER_SECOND = 1.5;
    private static final float EDGE_GLOW_WIDTH = 48;
    private static final float EDGE_GLOW_ALPHA = 0.22f;

    private final AssetManager assets;
    private final SoundManager sounds;
    private final Painter painter;
    private final HudPanel hud;
    private final ObjectivePanel objectivePanel;
    private final ObjectiveTracker objectives;
    private final Toast toast;
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
    private GameState state;
    private double shakeSecondsLeft;
    private int shakePixels;

    public GamePanel(GameFrame game) {
        super(game);
        this.assets = game.getAssets();
        this.sounds = game.getSounds();
        this.painter = game.getPainter();
        this.hud = new HudPanel(style, this);
        this.objectivePanel = new ObjectivePanel(style);
        this.objectives = new ObjectiveTracker();
        this.toast = new Toast(style);
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

        // Left: the map area, with the objectives top left, the pause card in the
        // middle and messages near the bottom. Right: a 1 px line and the HUD.
        Table mapArea = new Table();
        mapArea.add(objectivePanel).left().top().pad(16).row();
        mapArea.add(pauseCard).expand().row();
        mapArea.add(toast).padBottom(32);
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
        board = loader.load(level, new Random()); // new Random(): survivors in different spots each time
        turns = new TurnManager(board, new HazardManager(level), level);
        minimap.setBoard(board);
        keys.clear();
        shakeSecondsLeft = 0;
        pauseCard.setVisible(false);
        movement.computeSight(board, active());
        camera.follow(active().getRow(), active().getCol(), board.getRows(), board.getCols());
        toast.show("Save " + level.getTarget() + " of the " + level.getSurvivorCount() + " survivors");
        hud.refresh(board, active(), turns, level);
        objectivePanel.refresh(objectives.getObjectives(board, turns, level), objectives.getNextStep(board, turns));

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
        drawIcons();
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
        board.getTeam().tick(seconds);
        turns.update(seconds); // also sends in the next role if the one on the map was downed
        handleEvents(turns.takeEvents());
        movement.computeSight(board, active()); // the map may have changed, so look again
        shakeSecondsLeft = Math.max(0, shakeSecondsLeft - seconds);

        camera.follow(active().getDrawRow(), active().getDrawCol(), board.getRows(), board.getCols());
        hud.refresh(board, active(), turns, level);
        objectivePanel.refresh(objectives.getObjectives(board, turns, level), objectives.getNextStep(board, turns));
        keys.endFrame();

        if (turns.isMissionOver()) {
            finishLevel();
        }
    }

    /** The rescuer on the map right now. */
    private Rescuer active() {
        return board.getActiveRescuer();
    }

    /** One-shot keys: changing role and the special action. */
    private void handleActionKeys() {
        if (keys.wasPressed(Input.Keys.NUM_1)) {
            switchRole(0);
        } else if (keys.wasPressed(Input.Keys.NUM_2)) {
            switchRole(1);
        } else if (keys.wasPressed(Input.Keys.NUM_3)) {
            switchRole(2);
        } else if (keys.wasPressed(Input.Keys.TAB)) {
            switchRole(-1);
        }

        if (keys.wasPressed(Input.Keys.Q)) {
            try {
                checkNotChangingGear();
                toast.show(active().callBackup(board));
                sounds.play("radio");
            } catch (InvalidMoveException e) {
                sounds.play("denied");
                toast.show(e.getMessage());
            }
        }

        if (keys.wasPressed(Input.Keys.E)) {
            try {
                checkNotChangingGear();
                // Runs the Medic's, Engineer's or Dog Handler's own version.
                String result = active().specialAction(board);
                sounds.play(active().getSpecialSound());
                toast.show(result);
            } catch (InvalidMoveException e) {
                sounds.play("denied");
                toast.show(e.getMessage());
            }
        }
    }

    private void checkNotChangingGear() throws InvalidMoveException {
        if (board.getTeam().isChangingGear()) {
            throw new InvalidMoveException("Changing gear...");
        }
    }

    /** Hold-to-walk with WASD or the arrow keys. */
    private void handleMovement() {
        Direction direction = heldDirection();
        if (direction == null) {
            return;
        }
        if (board.getTeam().isChangingGear()) {
            return; // can't walk while putting on the new role's gear
        }
        Rescuer rescuer = active();
        rescuer.setFacing(direction); // turn even if the step is blocked
        if (!rescuer.canStep() || !movement.canStep(board, rescuer, direction)) {
            return;
        }
        rescuer.moveTo(rescuer.getRow() + direction.getRowChange(), rescuer.getCol() + direction.getColChange());
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

    /**
     * After every step: at the exit, deliver a survivor or (hands free, goal
     * reached) leave the building. Anywhere else, pick up a survivor on this tile.
     */
    private void afterStep() {
        Rescuer rescuer = active();
        int row = rescuer.getRow();
        int col = rescuer.getCol();
        Tile here = board.getTile(row, col);
        if (here.isExit() && (rescuer.isCarrying() || rescuer.isLeading())) {
            rescuer.deliverAll();
            sounds.play("rescued");
            if (turns.isTargetReached() && board.getStillInsideCount() > 0) {
                toast.show("Step off and back on to leave");
            }
            return;
        }
        if (here.isExit()) {
            // Hands free on the exit: leave if enough survivors are out (update() then ends the level).
            if (!turns.evacuate()) {
                toast.show("Save " + turns.getStillNeeded() + " more before you leave");
            }
            return;
        }
        Survivor survivor = board.getSurvivorAt(row, col);
        if (survivor != null) {
            survivor.reveal();
            try {
                survivor.reachedBy(rescuer); // follows, is picked up, or says what they need
                sounds.play("pickup");
            } catch (InvalidMoveException e) {
                toast.show(e.getMessage());
            }
        }
    }

    /** Changes the role on the map (index 0/1/2, or -1 for the next one). */
    private void switchRole(int index) {
        try {
            if (index < 0) {
                board.getTeam().switchToNext();
            } else {
                board.getTeam().switchTo(index);
            }
            sounds.play("switch");
            toast.show("Changing to the " + active().getName());
        } catch (InvalidMoveException e) {
            sounds.play("denied");
            toast.show(e.getMessage());
        }
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
                case ROLE_TAKEOVER:
                    sounds.play("switch");
                    break;
                case FIRE_SPREAD:
                    sounds.play("fire_spread");
                    break;
                case SURVIVOR_LOST:
                    sounds.play("survivor_lost");
                    break;
                case BACKUP_ARRIVED:
                    sounds.play("rescued");
                    break;
                case HELP_CALL:
                    sounds.play("help_call");
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
                toast.show(event.getMessage());
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
        drawCollapseWarning();
        drawFog();
        drawSurvivors();
        drawFacedTile();
        drawRescuer();
        drawHelpCalls();
        painter.end();
    }

    /** Things on top of the map that never scroll or shake: the minimap and the pause veil. */
    private void drawOverlay() {
        painter.begin(overlayCamera.combined);
        // Bottom-right corner: the objectives use the top-left, and the exit is often at the top.
        float width = board.getCols() * MINIMAP_SCALE;
        float height = board.getRows() * MINIMAP_SCALE;
        minimap.draw(painter, VIEW_WIDTH - width - MINIMAP_MARGIN, VIEW_HEIGHT - height - MINIMAP_MARGIN,
                MINIMAP_SCALE, assets);
        drawEdgeGlow();
        drawHelpArrows();
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
                // Each tile draws itself.
                board.getTile(row, col).draw(painter, camera.toMapX(col), camera.toMapY(row), TILE_SIZE, assets);
            }
        }
    }

    /** 0 to 1 and back, WARNING_PULSES_PER_SECOND times a second. Stops while paused. */
    private float warningPulse() {
        double cycles = turns.getElapsedSeconds() * WARNING_PULSES_PER_SECOND;
        return (float) (0.5 - 0.5 * Math.cos(cycles * 2 * Math.PI));
    }

    /** Before an aftershock, the tiles that are about to collapse pulse orange. */
    private void drawCollapseWarning() {
        if (!turns.isAftershockComing()) {
            return;
        }
        float pulse = warningPulse();
        Color fill = new Color(UiStyle.WARNING.r, UiStyle.WARNING.g, UiStyle.WARNING.b, 0.15f + 0.30f * pulse);
        Color edge = new Color(UiStyle.WARNING.r, UiStyle.WARNING.g, UiStyle.WARNING.b, 0.5f + 0.5f * pulse);
        int lastRow = Math.min(camera.getFirstRow() + VIEW_ROWS + 1, board.getRows());
        int lastCol = Math.min(camera.getFirstCol() + VIEW_COLS + 1, board.getCols());
        for (int row = camera.getFirstRow(); row < lastRow; row++) {
            for (int col = camera.getFirstCol(); col < lastCol; col++) {
                if (turns.willCollapse(row, col)) {
                    float x = camera.toMapX(col);
                    float y = camera.toMapY(row);
                    painter.fillRect(fill, x, y, TILE_SIZE, TILE_SIZE);
                    painter.outlineRect(edge, x, y, TILE_SIZE, TILE_SIZE, 2);
                }
            }
        }
    }

    /**
     * A survivor calling for help shows a ring and "HELP!" where they are, even in
     * the dark. You know roughly where to go, but you still have to find the way.
     */
    private void drawHelpCalls() {
        float ring = (float) ((turns.getElapsedSeconds() * 1.5) % 1.0); // grows from 0 to 1, then restarts
        Color edge = new Color(1, 1, 1, 0.9f * (1 - ring));
        for (Survivor survivor : turns.getCallingSurvivors()) {
            if (!camera.isInView(survivor.getRow(), survivor.getCol())) {
                continue;
            }
            float cx = camera.toMapX(survivor.getCol()) + TILE_SIZE / 2f;
            float cy = camera.toMapY(survivor.getRow()) + TILE_SIZE / 2f;
            float size = TILE_SIZE * (0.4f + 1.2f * ring);
            painter.outlineOval(edge, cx - size / 2, cy - size / 2, size, size, 2);
            painter.text("HELP!", Color.WHITE, cx, cy - TILE_SIZE * 0.8f);
        }
    }

    /** For calls from off screen: an arrow at the edge of the view pointing towards them. */
    private void drawHelpArrows() {
        float centerX = VIEW_WIDTH / 2f;
        float centerY = VIEW_HEIGHT / 2f;
        float margin = 40;
        for (Survivor survivor : turns.getCallingSurvivors()) {
            if (camera.isInView(survivor.getRow(), survivor.getCol())) {
                continue;
            }
            float dx = camera.toScreenX(survivor.getCol()) + TILE_SIZE / 2f - centerX;
            float dy = camera.toScreenY(survivor.getRow()) + TILE_SIZE / 2f - centerY;
            // Walk out from the middle until we touch the edge of the view (minus a margin).
            float scale = Math.min((centerX - margin) / Math.max(1, Math.abs(dx)),
                    (centerY - margin) / Math.max(1, Math.abs(dy)));
            float x = centerX + dx * scale;
            float y = centerY + dy * scale;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float ux = dx / length;
            float uy = dy / length;
            float tip = 14;
            painter.fillPolygon(Color.WHITE,
                    x + ux * tip, y + uy * tip,
                    x - ux * tip / 2 - uy * tip * 0.7f, y - uy * tip / 2 + ux * tip * 0.7f,
                    x - ux * tip / 2 + uy * tip * 0.7f, y - uy * tip / 2 - ux * tip * 0.7f);
            painter.text("HELP!", Color.WHITE, x - ux * 28, y - uy * 28 - 6);
        }
    }

    /** A faint orange glow at the edges of the map that pulses until the aftershock. */
    private void drawEdgeGlow() {
        if (!turns.isAftershockComing()) {
            return;
        }
        Color glow = new Color(UiStyle.WARNING.r, UiStyle.WARNING.g, UiStyle.WARNING.b, EDGE_GLOW_ALPHA * warningPulse());
        Color clear = new Color(UiStyle.WARNING.r, UiStyle.WARNING.g, UiStyle.WARNING.b, 0);
        float w = EDGE_GLOW_WIDTH;
        painter.gradientRect(0, 0, VIEW_WIDTH, w, glow, glow, clear, clear);                              // top
        painter.gradientRect(0, VIEW_HEIGHT - w, VIEW_WIDTH, w, clear, clear, glow, glow);                // bottom
        painter.gradientRect(0, 0, w, VIEW_HEIGHT, glow, clear, clear, glow);                             // left
        painter.gradientRect(VIEW_WIDTH - w, 0, w, VIEW_HEIGHT, clear, glow, glow, clear);                // right
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

    /** Survivors only appear once revealed (seen, or found by the dog), plus the one following you. */
    private void drawSurvivors() {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && survivor.isRevealed()
                    && camera.isInView(survivor.getRow(), survivor.getCol())) {
                survivor.draw(painter, camera.toMapX(survivor.getCol()), camera.toMapY(survivor.getRow()),
                        TILE_SIZE, assets);
            }
        }
        Survivor follower = active().getFollower();
        if (follower != null) {
            double progress = active().stepProgress(); // slides along with the rescuer
            follower.draw(painter, camera.toMapX(follower.getDrawCol(progress)),
                    camera.toMapY(follower.getDrawRow(progress)), TILE_SIZE, assets);
        }
    }

    /** A dashed outline on the tile the active rescuer faces: that's where E acts. */
    private void drawFacedTile() {
        Tile faced = active().getFacedTile(board);
        if (faced == null || state != GameState.PLAYING) {
            return;
        }
        painter.dashedRect(FACED_TILE, camera.toMapX(faced.getCol()) + 3, camera.toMapY(faced.getRow()) + 3,
                TILE_SIZE - 6, TILE_SIZE - 6, 2, 6);
    }

    /** The one rescuer on the map, with a bar above them while they change gear. */
    private void drawRescuer() {
        Rescuer rescuer = active();
        if (rescuer.isDowned()) {
            return;
        }
        float x = camera.toMapX(rescuer.getDrawCol());
        float y = camera.toMapY(rescuer.getDrawRow());
        rescuer.draw(painter, x, y, TILE_SIZE, assets);
        if (board.getTeam().isChangingGear()) {
            float width = TILE_SIZE - 12;
            float progress = (float) board.getTeam().getGearChangeProgress();
            painter.fillRect(GEAR_BAR_BACK, x + 6, y - 10, width, 6);
            painter.fillRect(UiStyle.TEXT, x + 6, y - 10, width * progress, 6);
        }
    }
}
