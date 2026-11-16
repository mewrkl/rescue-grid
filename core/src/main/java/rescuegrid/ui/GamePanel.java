package rescuegrid.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import rescuegrid.assets.AssetManager;
import rescuegrid.assets.SoundManager;
import rescuegrid.graphics.Painter;
import rescuegrid.logic.GameEvent;
import rescuegrid.logic.HazardManager;
import rescuegrid.logic.InvalidMoveException;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.Lesson;
import rescuegrid.logic.LevelLoader;
import rescuegrid.logic.MissionReport;
import rescuegrid.logic.MovementCalculator;
import rescuegrid.logic.ObjectiveTracker;
import rescuegrid.logic.TurnManager;
import rescuegrid.logic.Tutorial;
import rescuegrid.model.Direction;
import rescuegrid.model.GameBoard;
import rescuegrid.model.GameState;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.rescuers.Rescuer;
import rescuegrid.model.survivors.CriticalSurvivor;
import rescuegrid.model.survivors.MinorSurvivor;
import rescuegrid.model.survivors.SeriousSurvivor;
import rescuegrid.model.tiles.CrackedFloor;
import rescuegrid.model.tiles.Fire;
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
    private static final int CAMERA_TOP_MARGIN_ROWS = 3;  // the objective card's height, in tiles
    private static final float COVERING_ALPHA = 0.25f;   // the objective card while the exit is under it
    private static final Color EXIT_GUIDE = Painter.rgb(74, 222, 128); // same green as the exit on the minimap
    private static final Color PAW_READY = Painter.rgba(255, 255, 255, 46);
    private static final double AFTERSHOCK_SHAKE_SECONDS = 0.8;
    private static final int AFTERSHOCK_SHAKE_PIXELS = 10;
    private static final double WARNING_SHAKE_SECONDS = 1.0;
    private static final int WARNING_SHAKE_PIXELS = 2;

    private static final Color UNEXPLORED = Color.BLACK;
    private static final Color REMEMBERED = Painter.rgba(0, 0, 0, 150);
    private static final Color FACED_TILE = Painter.rgba(255, 255, 255, 150);
    private static final double RADIO_SECONDS = 1.6;     // the radio bubble after calling backup
    private static final long BUBBLE_FRAME_MILLIS = 250;
    private static final long FLARE_FRAME_MILLIS = 120;
    private static final Color FLARE_BOX = Painter.rgba(10, 10, 10, 200);
    private static final Color PAUSE_SCRIM = Painter.rgba(0, 0, 0, 170);
    private static final Color GEAR_BAR_BACK = Painter.rgba(0, 0, 0, 180);
    private static final double WARNING_PULSES_PER_SECOND = 1.5;
    private static final float EDGE_GLOW_WIDTH = 48;
    private static final float EDGE_GLOW_ALPHA = 0.22f;
    private static final Color HINT_BOX = Painter.rgba(0, 0, 0, 200);
    private static final Color DOT_TODO = Painter.rgba(255, 255, 255, 40);
    private static final double PAN_GLIDE_SECONDS = 0.6; // training: the camera glides to a door that opened...
    private static final double PAN_HOLD_SECONDS = 0.9;  // ...stays a moment, then glides back
    private static final float TIMER_LINE = 28;           // height of the "45 s" box under a survivor
    private static final double LESSON_MIN_SECONDS = 0.6;
    private static final double FIRE_DEMO_SECONDS = 1.2;  // the fire card: when the fire spreads // a card can't be skipped by a key already held
    private static final float LESSON_TEXT_WIDTH = 520;
    private static final float LESSON_PICTURE = 64;
    private static final double DONE_SECONDS = 0.9;      // training: how long "DONE!" shows after a step
    private static final float CHAPTER_SHOW_SECONDS = 1.8f;
    private static final double END_PAUSE_SECONDS = 1.6;  // "MISSION COMPLETE" over the map, then the results
    private static final double IDLE_HINT_SECONDS = 20;   // no progress for this long: a hint on the radio
    private static final float BRIEFING_WIDTH = 520;

    private final AssetManager assets;
    private final SoundManager sounds;
    private final Painter painter;
    private final HudPanel hud;
    private final ObjectivePanel objectivePanel;
    private final ObjectiveTracker objectives;
    private final Table tutorialCard;
    private final Label tutorialStep;
    private final Label tutorialPrompt;
    private final Table tutorialDots;
    private Image[] dots;
    private final Table chapterCard;
    private final Label chapterNumber;
    private final Label chapterName;
    private final Toast toast;
    private final Table pauseCard;
    private final Label pauseTitle;
    private final Label pauseMessage;
    private final Label pauseHint;
    private final MenuList pauseMenu;
    private final Table briefingCard;
    private final Table lessonCard;
    private final Label lessonTitle;
    private final Label lessonBody;
    private final LessonPicture lessonPicture;
    private Lesson lesson;            // the training card being shown (state LESSON), or null
    private double lessonSeconds;     // how long it has been up
    private boolean fireDemoDone;     // the fire card has shown the fire spreading
    private final Label briefingTitle;
    private final Label briefingGoal;
    private final Label briefingInfo;
    private final Label briefingNew;
    private final Label[] briefingStars;
    private final Table endCard;
    private final Label endTitle;
    private final Label endLine;
    private final Camera camera;
    private final OrthographicCamera overlayCamera; // for things that never scroll (edge glow, arrows)
    private final KeyboardInput keys;
    private final LevelLoader loader;
    private final MovementCalculator movement;
    private final Random shakeRandom; // only for the screen shake, never for game rules

    private GameBoard board;
    private LevelData level;
    private TurnManager turns;
    private Tutorial tutorial; // null except in the training level
    private int exitRow;
    private int exitCol;
    private GameState state;
    private double shakeSecondsLeft;
    private int shakePixels;
    private Survivor bumped;        // the survivor we last walked into (so holding the key asks only once)
    private Tile bumpedTile;        // the furniture we last walked into (so holding the key thuds once)
    private final Set<String> furnitureNamed = new HashSet<>(); // furniture already named this level
    private double panSeconds;      // training: time into the camera's look at a door, or -1
    private int panRow;
    private int panCol;
    private int shownStep;          // training: the step the card shows, to notice when one is done
    private int shownChapter;
    private double doneSecondsLeft;
    private float cardCover;        // training: how much of the map's bottom the card hides (eased)
    private double endSecondsLeft;  // ENDING: time left before the result screen
    private double idleSeconds;     // how long nothing has moved forward (for the radio hint)
    private double radioSecondsLeft; // the rescuer is on the radio to backup
    private final List<BackupTeam> backupTeams = new ArrayList<>(); // stretcher teams on their way
    private int lastProgressMark;

    public GamePanel(GameFrame game) {
        super(game);
        this.assets = game.getAssets();
        this.sounds = game.getSounds();
        this.painter = game.getPainter();
        this.hud = new HudPanel(style, this);
        this.objectivePanel = new ObjectivePanel(style);
        this.objectives = new ObjectiveTracker();
        this.tutorialStep = style.pixelLabel(" ", 8, UiStyle.TEXT_SECONDARY);
        this.tutorialPrompt = style.label(" ", UiStyle.TEXT);
        this.tutorialDots = new Table();
        this.tutorialCard = buildTutorialCard();
        this.chapterNumber = style.pixelLabel(" ", 8, UiStyle.TEXT_SECONDARY);
        this.chapterName = style.pixelLabel(" ", 24, UiStyle.TEXT);
        this.chapterCard = buildChapterCard();
        this.toast = new Toast(style);
        this.pauseTitle = style.pixelLabel("PAUSED", 24, UiStyle.TEXT);
        this.pauseMessage = style.label(" ", UiStyle.TEXT_SECONDARY);
        this.pauseHint = style.label(" ", UiStyle.TEXT_SECONDARY);
        this.pauseMenu = new MenuList(style, () -> game.playSound("click"));
        this.pauseCard = buildPauseCard();
        this.briefingTitle = style.pixelLabel(" ", 24, UiStyle.TEXT);
        this.briefingGoal = style.label(" ", UiStyle.TEXT);
        this.briefingInfo = style.label(" ", UiStyle.TEXT_SECONDARY);
        this.briefingNew = style.label(" ", UiStyle.TEXT);
        this.briefingStars = new Label[MissionReport.STAR_COUNT];
        this.briefingCard = buildBriefingCard();
        this.lessonTitle = style.pixelLabel(" ", 16, UiStyle.TEXT);
        this.lessonBody = style.label(" ", UiStyle.TEXT);
        this.lessonPicture = new LessonPicture();
        this.lessonCard = buildLessonCard();
        this.endTitle = style.pixelLabel(" ", 32, UiStyle.TEXT);
        this.endLine = style.label(" ", UiStyle.TEXT_SECONDARY);
        this.endCard = buildEndCard();
        this.camera = new Camera(VIEW_ROWS, VIEW_COLS, TILE_SIZE, CAMERA_TOP_MARGIN_ROWS);
        this.overlayCamera = new OrthographicCamera();
        this.overlayCamera.setToOrtho(true, VIEW_WIDTH, VIEW_HEIGHT);
        this.keys = new KeyboardInput();
        this.loader = new LevelLoader();
        this.movement = new MovementCalculator();
        this.shakeRandom = new Random();
        this.state = GameState.MENU;

        // Left: the map area, with the objectives top left, the pause card in the
        // middle and messages near the bottom. Right: a 1 px line and the HUD.
        Table mapArea = new Table();
        mapArea.add(objectivePanel).left().top().pad(16).row();
        Stack middle = new Stack(); // the chapter title and the pause card share the middle
        middle.add(chapterCard);
        Table pauseCentred = new Table(); // a Stack stretches its children; this keeps the card its own size
        pauseCentred.add(pauseCard);
        middle.add(pauseCentred);
        mapArea.add(middle).expand().fill().row();
        // The training card and the explanation card share the bottom slot (only one shows at a time),
        // so the explanation sits as low as it can and the map stays visible above it.
        Stack bottom = new Stack();
        Table tutorialAtBottom = new Table();
        tutorialAtBottom.bottom().add(tutorialCard);
        Table lessonAtBottom = new Table();
        lessonAtBottom.bottom().add(lessonCard);
        lessonAtBottom.setTouchable(Touchable.disabled);
        bottom.add(tutorialAtBottom);
        bottom.add(lessonAtBottom);
        mapArea.add(bottom).padBottom(8).row();
        mapArea.add(toast).padBottom(32);
        root.add(mapArea).width(VIEW_WIDTH).expandY().fillY();
        root.add(new Image(style.solid(UiStyle.BORDER))).width(1).fillY();
        root.add(hud).width(HudPanel.WIDTH - 1).fillY();

        // The briefing and end cards float over the middle of the map, on their own
        // layer, so a tall card never pushes the objectives or the HUD out of the window.
        Stack cards = new Stack();
        cards.add(briefingCard);
        cards.add(endCard);
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.childrenOnly);
        overlay.add(cards).expand().padRight(HudPanel.WIDTH);
        stage.addActor(overlay);
    }

    /** The training prompt near the bottom of the map: step number and what to do. */
    private Table buildTutorialCard() {
        Table card = new Table();
        card.setBackground(style.smallCard());
        card.defaults().left();
        tutorialPrompt.setWrap(true);
        card.add(tutorialStep).row();
        card.add(tutorialDots).padTop(8).row();
        card.add(tutorialPrompt).width(560).padTop(8).row();
        card.setVisible(false);
        return card;
    }

    /** One dot per training step, with a wider gap between chapters. Filled in as steps are done. */
    private void buildDots() {
        tutorialDots.clearChildren();
        dots = new Image[tutorial.getStepCount()];
        for (int i = 0; i < dots.length; i++) {
            boolean newChapter = i > 0 && tutorial.getChapterOf(i + 1) != tutorial.getChapterOf(i);
            dots[i] = new Image(style.solid(Color.WHITE));
            tutorialDots.add(dots[i]).size(8).padLeft(i == 0 ? 0 : newChapter ? 16 : 4);
        }
    }

    /** "CHAPTER 2 OF 4 / RESCUING" in the middle of the map, shown for a moment as a chapter starts. */
    private Table buildChapterCard() {
        Table card = new Table();
        card.setBackground(style.card());
        card.add(chapterNumber).row();
        card.add(chapterName).padTop(12).row();
        card.setTouchable(Touchable.disabled);
        Table centred = new Table(); // keeps the card its own size inside the Stack
        centred.add(card);
        centred.setTouchable(Touchable.disabled);
        centred.getColor().a = 0; // faded in and out as a whole (Scene2D multiplies the alpha down)
        return centred;
    }

    private void showChapter() {
        chapterNumber.setText("CHAPTER " + tutorial.getChapterNumber() + " OF " + tutorial.getChapterCount());
        chapterName.setText(tutorial.getChapterName());
        chapterCard.clearActions();
        chapterCard.getColor().a = 0;
        chapterCard.addAction(Actions.sequence(Actions.fadeIn(0.2f), Actions.delay(CHAPTER_SHOW_SECONDS),
                Actions.fadeOut(0.4f)));
    }

    /**
     * The pause menu, a list like every other menu (arrows, Enter, mouse; Esc
     * resumes). Restarting or leaving asks first. Hidden until Esc is pressed.
     */
    private Table buildPauseCard() {
        Table card = new Table();
        card.setBackground(style.card());
        pauseMessage.setWrap(true);
        pauseMessage.setAlignment(Align.center);
        card.add(pauseTitle).row();
        card.add(pauseMessage).width(360).padTop(8).row();
        card.add(pauseMenu).padTop(8).row();
        card.add(pauseHint).padTop(8).row();
        card.setVisible(false);
        showPauseOptions();
        return card;
    }

    /** RESUME, RESTART LEVEL, HOW TO PLAY, OPTIONS, MAIN MENU. */
    private void showPauseOptions() {
        pauseTitle.setText("PAUSED");
        pauseMessage.setText("");
        pauseHint.setText("Arrow keys and Enter  ·  Esc to resume");
        pauseMenu.clearOptions();
        pauseMenu.addOption("RESUME", this::togglePause);
        pauseMenu.addOption("RESTART LEVEL", () -> confirm("RESTART LEVEL?", "YES, RESTART",
                () -> game.startLevel(level.getNumber())));
        pauseMenu.addOption("HOW TO PLAY", () -> game.showHowToPlay(game::resumeGame));
        pauseMenu.addOption("OPTIONS", () -> game.showOptions(game::resumeGame));
        pauseMenu.addOption("MAIN MENU", () -> confirm("LEAVE THE LEVEL?", "YES, LEAVE", () -> {
            state = GameState.MENU;
            game.showMenu();
        }));
        pauseMenu.setBackAction(this::togglePause);
    }

    /** "Are you sure?" before throwing a level away; NO is first, so Enter is safe. */
    private void confirm(String title, String yes, Runnable action) {
        pauseTitle.setText(title);
        pauseMessage.setText("This level's progress is lost.");
        pauseHint.setText("Arrow keys and Enter  ·  Esc to go back");
        pauseMenu.clearOptions();
        pauseMenu.addOption("NO, GO BACK", this::showPauseOptions);
        pauseMenu.addOption(yes, action);
        pauseMenu.setBackAction(this::showPauseOptions);
    }

    /**
     * Before a level: its name, the target, what's new, and the three star goals.
     * The clock waits until Enter is pressed.
     */
    private Table buildBriefingCard() {
        Table card = new Table();
        card.setBackground(style.card());
        card.defaults().left().width(BRIEFING_WIDTH);
        briefingInfo.setWrap(true);
        briefingNew.setWrap(true);
        card.add(briefingTitle).row();
        card.add(briefingGoal).padTop(16).row();
        card.add(briefingInfo).padTop(4).row();
        card.add(style.pixelLabel("NEW HERE", 8, UiStyle.SELECTED)).padTop(16).row();
        card.add(briefingNew).padTop(8).row();
        card.add(style.pixelLabel("STARS", 8, UiStyle.TEXT_SECONDARY)).padTop(16).row();
        for (int i = 0; i < briefingStars.length; i++) {
            briefingStars[i] = style.label(" ", UiStyle.TEXT);
            Table line = new Table();
            line.left(); // a full-width row: keep the star and its goal at the left edge
            line.add(icon(new StarIcon(), 18)).size(18).padRight(8); // empty stars: still to earn
            line.add(briefingStars[i]).left();
            card.add(line).padTop(4).row();
        }
        Table start = new Table();
        start.left();
        start.add(style.keycap("Enter")).padRight(8);
        start.add(style.label("to start", UiStyle.TEXT_SECONDARY));
        card.add(start).padTop(24).row();
        Table centred = new Table(); // keeps the card its own size inside the Stack
        centred.add(card);
        centred.setVisible(false);
        return centred;
    }

    /** "MISSION COMPLETE" or "MISSION FAILED" in the middle of the map, for a moment at the end. */
    private Table buildEndCard() {
        Table card = new Table();
        card.setBackground(style.card());
        card.add(endTitle).row();
        card.add(endLine).padTop(12).row();
        Table centred = new Table();
        centred.add(card);
        centred.setVisible(false);
        return centred;
    }

    private void showBriefing() {
        briefingTitle.setText(level.getTitle());
        briefingGoal.setText("Rescue " + level.getTarget() + " of the " + level.getSurvivorCount()
                + " survivors, then leave by the EXIT.");
        briefingInfo.setText("Every survivor has a timer: the seconds under them count down from the start, "
                + "and at 0 they don't make it. The Medic's Treat (1, E) gives them more time. An aftershock "
                + "hits every " + (int) level.getAftershockSeconds() + " s.");
        briefingNew.setText(level.getWhatsNew());
        String[] goals = MissionReport.goalsFor(level);
        for (int i = 0; i < briefingStars.length; i++) {
            briefingStars[i].setText(goals[i]);
        }
        briefingCard.setVisible(true);
        objectivePanel.setAllowed(false); // the briefing says the same, and they would overlap
        state = GameState.BRIEFING;
    }

    /** Loads a level and starts playing. Throws IllegalArgumentException for a broken level. */
    public void startLevel(int levelNumber) {
        level = LevelData.forLevel(levelNumber);
        board = loader.load(level, new Random()); // new Random(): survivors in different spots each time
        turns = new TurnManager(board, new HazardManager(level), level);
        hud.setBoard(board);
        findExit();
        tutorial = level.isTutorial() ? new Tutorial() : null;
        if (tutorial != null) {
            tutorial.begin(turns); // calm until the timed part
        }
        tutorialCard.setVisible(tutorial != null);
        chapterCard.clearActions();
        chapterCard.getColor().a = 0;
        panSeconds = -1;
        bumped = null;
        bumpedTile = null;
        furnitureNamed.clear();
        cardCover = 0;
        camera.setBottomCover(0);
        doneSecondsLeft = 0;
        if (tutorial != null) {
            buildDots();
            shownStep = tutorial.getStepNumber();
            shownChapter = tutorial.getChapterNumber();
            showChapter();
        }
        keys.clear();
        shakeSecondsLeft = 0;
        pauseCard.setVisible(false);
        endCard.setVisible(false);
        briefingCard.setVisible(false);
        objectivePanel.setAllowed(true);
        idleSeconds = 0;
        radioSecondsLeft = 0;
        backupTeams.clear();
        fireDemoDone = false;
        lastProgressMark = objectives.getProgressMark(board);
        movement.computeSight(board, active());
        camera.follow(active().getRow(), active().getCol(), board.getRows(), board.getCols());
        refreshPanels();

        sounds.loopMusic("gameplay");
        state = GameState.PLAYING;
        if (tutorial == null) {
            showBriefing(); // training explains itself step by step instead
        }
    }

    /** Remembers where the exit is, so it can be pointed out. Every level has one (LevelLoader checks). */
    private void findExit() {
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(row, col).isExit()) {
                    exitRow = row;
                    exitCol = col;
                    return;
                }
            }
        }
    }

    /** True if the tile is on screen but (mostly) behind the objective card in the top-left. */
    private boolean isUnderObjectives(int row, int col) {
        if (!camera.isInView(row, col)) {
            return false;
        }
        // Scene2D measures from the bottom-left (y up); the map is drawn from the top-left (y down).
        Vector2 corner = objectivePanel.localToStageCoordinates(new Vector2(0, 0));
        float cardRight = corner.x + objectivePanel.getWidth();
        float cardBottom = stage.getHeight() - corner.y;
        float tileCenterX = camera.toScreenX(col) + TILE_SIZE / 2f;
        float tileCenterY = camera.toScreenY(row) + TILE_SIZE / 2f;
        return tileCenterX < cardRight && tileCenterY < cardBottom;
    }

    /** Point out the exit while someone needs taking there, or once enough are saved to leave. */
    private boolean isGuidingToExit() {
        return active().isCarrying() || active().isLeading() || turns.isTargetReached();
    }

    /** Keyboard goes to our KeyboardInput (the stage gets it first, but has no keyboard widgets). */
    @Override
    protected InputProcessor createInput() {
        InputProcessor pauseKeys = pauseMenu.keyboard();
        InputAdapter whilePaused = new InputAdapter() { // the pause menu only gets keys while paused
            @Override
            public boolean keyDown(int keycode) {
                return state == GameState.PAUSED && pauseKeys.keyDown(keycode);
            }
        };
        return new InputMultiplexer(whilePaused, stage, keys);
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
            keys.endFrame();
            return;
        }
        if (state == GameState.LESSON) {
            updateLesson(seconds);
            keys.endFrame();
            return;
        }
        if (state == GameState.BRIEFING) {
            if (keys.wasPressed(Input.Keys.ENTER) || keys.wasPressed(Input.Keys.NUMPAD_ENTER)
                    || keys.wasPressed(Input.Keys.SPACE)) {
                briefingCard.setVisible(false);
                objectivePanel.setAllowed(true);
                sounds.play("click");
                state = GameState.PLAYING;
            }
            keys.endFrame();
            return;
        }
        if (state == GameState.ENDING) {
            endSecondsLeft -= seconds;
            if (endSecondsLeft <= 0) {
                showResults();
            }
            keys.endFrame();
            return;
        }
        if (state != GameState.PLAYING) {
            keys.endFrame();
            return;
        }

        if (panSeconds >= 0) {
            updatePan(seconds); // training: the camera is showing a door; the game waits
            refreshPanels();
            keys.endFrame();
            return;
        }

        handleActionKeys();
        handleMovement();
        board.getTeam().tick(seconds);
        turns.update(seconds); // also sends in the next role if the one on the map was downed
        handleEvents(turns.takeEvents());
        for (BackupTeam team : backupTeams) {
            team.update(seconds);
        }
        backupTeams.removeIf(BackupTeam::isDone);
        radioSecondsLeft = Math.max(0, radioSecondsLeft - seconds);
        if (tutorial != null && tutorial.retryIfLost(board)) {
            sounds.play("denied");
            toast.show("They ran out of time. The timed part starts again: be quicker!");
        }
        movement.computeSight(board, active()); // the map may have changed, so look again
        shakeSecondsLeft = Math.max(0, shakeSecondsLeft - seconds);
        doneSecondsLeft = Math.max(0, doneSecondsLeft - seconds);

        updateCardCover(seconds);
        camera.follow(active().getDrawRow(), active().getDrawCol(), board.getRows(), board.getCols());
        refreshPanels();
        updateIdleHint(seconds);
        keys.endFrame();

        if (turns.isMissionOver()) {
            finishLevel();
        }
    }

    /**
     * Outside training: if nothing has moved forward for IDLE_HINT_SECONDS (nobody
     * found, freed, picked up or rescued), the radio says what to do next. Not
     * while carrying or leading someone: the green EXIT marker already shows the way.
     */
    private void updateIdleHint(float seconds) {
        if (tutorial != null) {
            return;
        }
        int mark = objectives.getProgressMark(board);
        if (mark != lastProgressMark || active().isCarrying() || active().isLeading()) {
            lastProgressMark = mark;
            idleSeconds = 0;
            return;
        }
        idleSeconds += seconds;
        if (idleSeconds >= IDLE_HINT_SECONDS) {
            idleSeconds = 0;
            sounds.play("radio");
            toast.show("Radio: " + objectives.getNextStep(board, turns));
        }
    }

    /**
     * Training: the prompt card covers the bottom of the map. Tell the camera, so
     * the rescuer (and the last rows of the map) stay in the part above it. Eased,
     * so the view glides instead of jumping when the prompt gets longer or shorter.
     */
    private void updateCardCover(float seconds) {
        float target = 0;
        if (tutorialCard.isVisible() && tutorialCard.getHeight() > 0) {
            // Scene2D measures up from the bottom; the card's top edge is how much it covers.
            float top = tutorialCard.localToStageCoordinates(new Vector2(0, tutorialCard.getHeight())).y;
            target = (top + 8) * VIEW_HEIGHT / stage.getHeight(); // 8 px gap above the card
        }
        cardCover += (target - cardCover) * Math.min(1f, seconds * 6);
        camera.setBottomCover(cardCover);
    }

    /** Training: starts the camera's look at a door that just opened. */
    private void startPan(int[] door) {
        panRow = door[0];
        panCol = door[1];
        panSeconds = 0;
    }

    /** Glide to the door, hold, glide back; then play goes on. */
    private void updatePan(float seconds) {
        panSeconds += seconds;
        doneSecondsLeft = Math.max(0, doneSecondsLeft - seconds);
        double total = 2 * PAN_GLIDE_SECONDS + PAN_HOLD_SECONDS;
        double toDoor;
        if (panSeconds < PAN_GLIDE_SECONDS) {
            toDoor = smooth(panSeconds / PAN_GLIDE_SECONDS);
        } else if (panSeconds < PAN_GLIDE_SECONDS + PAN_HOLD_SECONDS) {
            toDoor = 1;
        } else {
            toDoor = smooth(Math.max(0, (total - panSeconds) / PAN_GLIDE_SECONDS));
        }
        double row = active().getDrawRow() + (panRow - active().getDrawRow()) * toDoor;
        double col = active().getDrawCol() + (panCol - active().getDrawCol()) * toDoor;
        camera.follow(row, col, board.getRows(), board.getCols());
        if (panSeconds >= total) {
            panSeconds = -1;
        }
    }

    /** 0 to 1 with a gentle start and stop. */
    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    /** Updates the sidebar, the objectives and (in training) the tutorial prompt. */
    private void refreshPanels() {
        hud.refresh(board, active(), turns, level, objectives.getObjectives(board, turns, level));
        // In training the tutorial card says what to do, so the objectives show no "next step".
        String next = tutorial == null ? objectives.getNextStep(board, turns) : null;
        objectivePanel.refresh(next);
        // The card goes see-through while the exit is under it, so it never hides the way out.
        objectivePanel.getColor().a = isUnderObjectives(exitRow, exitCol) ? COVERING_ALPHA : 1f;
        if (tutorial != null) {
            refreshTraining();
        }
    }

    /** The training card: chapter, step, progress dots and what to do; plus "DONE!" and chapter titles. */
    private void refreshTraining() {
        tutorial.update(board, turns);
        int[] door = tutorial.takeDoorToShow();
        if (door != null && panSeconds < 0) {
            startPan(door);
        }
        if (tutorial.hasLesson() && panSeconds < 0 && state == GameState.PLAYING) {
            showLesson(tutorial.takeLesson());
        }
        if (tutorial.getStepNumber() != shownStep) {
            boolean newChapter = tutorial.getChapterNumber() != shownChapter;
            shownStep = tutorial.getStepNumber();
            shownChapter = tutorial.getChapterNumber();
            doneSecondsLeft = DONE_SECONDS;
            sounds.play(newChapter ? "success" : "click");
            if (newChapter) {
                showChapter();
            }
        }
        String clock = tutorial.isClockRunning() ? "    THE CLOCK IS RUNNING" : "";
        tutorialStep.setText(tutorial.getChapterName() + "   STEP " + tutorial.getStepNumber() + " / "
                + tutorial.getStepCount() + clock);
        tutorialStep.setColor(tutorial.isClockRunning() ? UiStyle.WARNING : UiStyle.TEXT_SECONDARY);
        boolean justDone = doneSecondsLeft > 0;
        tutorialPrompt.setText(justDone ? "DONE!" : tutorial.getPrompt());
        tutorialPrompt.setColor(justDone ? EXIT_GUIDE : UiStyle.TEXT);
        int current = tutorial.getStepNumber();
        for (int i = 0; i < dots.length; i++) {
            int stepNumber = i + 1;
            dots[i].setColor(stepNumber < current ? EXIT_GUIDE : stepNumber == current ? Color.WHITE : DOT_TODO);
        }
    }

    /** Real time in seconds, for pulses that should keep moving while the game is frozen. */
    private static double wallSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }

    // ----- Training: stop-and-read cards (decision 0025) -----

    /** The card: a picture, a title, the explanation, and "Enter to continue". */
    private Table buildLessonCard() {
        Table card = new Table();
        card.setBackground(style.card());
        lessonBody.setWrap(true);
        Table words = new Table();
        words.defaults().left();
        words.add(lessonTitle).row();
        words.add(lessonBody).width(LESSON_TEXT_WIDTH).padTop(8).row();
        Table go = new Table();
        go.add(style.keycap("Enter")).padRight(8);
        go.add(style.label("to continue", UiStyle.TEXT_SECONDARY));
        words.add(go).padTop(16).row();
        card.add(icon(lessonPicture, LESSON_PICTURE)).size(LESSON_PICTURE).top().padRight(24);
        card.add(words).top();
        card.setVisible(false);
        return card;
    }

    /** Freezes the training and shows a card; the camera glides to what it is about. */
    private void showLesson(Lesson next) {
        lesson = next;
        lessonSeconds = 0;
        state = GameState.LESSON;
        keys.clear();
        lessonTitle.setText(next.getTitle());
        lessonBody.setText(next.getBody());
        lessonPicture.show(next.getPicture());
        lessonCard.setVisible(true);
        tutorialCard.setVisible(false);
        toast.hideNow();
        sounds.play("radio");
    }

    /** While the card is up: nothing moves but the camera; Enter (or Space) carries on. */
    private void updateLesson(float seconds) {
        lessonSeconds += seconds;
        if (lesson.getPicture() == Lesson.Picture.FIRE && !fireDemoDone && lessonSeconds >= FIRE_DEMO_SECONDS) {
            fireDemoDone = true;                  // once the camera is there: watch it spread
            if (tutorial.spreadFireDemo(board) != null) {
                sounds.play("fire_spread");
                movement.computeSight(board, active());
            }
        }
        int[] focus = lesson.getFocus();
        if (focus != null) {
            double toFocus = smooth(Math.min(1, lessonSeconds / PAN_GLIDE_SECONDS));
            double row = active().getDrawRow() + (focus[0] - active().getDrawRow()) * toFocus;
            double col = active().getDrawCol() + (focus[1] - active().getDrawCol()) * toFocus;
            camera.follow(row, col, board.getRows(), board.getCols());
        }
        boolean go = keys.wasPressed(Input.Keys.ENTER) || keys.wasPressed(Input.Keys.NUMPAD_ENTER)
                || keys.wasPressed(Input.Keys.SPACE);
        if (go && lessonSeconds >= LESSON_MIN_SECONDS) {
            closeLesson();
        }
    }

    /** Back to playing: the camera glides back from what the card showed (the game waits for that). */
    private void closeLesson() {
        int[] focus = lesson.getFocus();
        lessonCard.setVisible(false);
        tutorialCard.setVisible(true);
        state = GameState.PLAYING;
        keys.clear();
        sounds.play("click");
        if (focus != null) {
            panRow = focus[0];
            panCol = focus[1];
            panSeconds = PAN_GLIDE_SECONDS + PAN_HOLD_SECONDS; // only the glide back is left
        }
        lesson = null;
    }

    /** The picture on the card: the role, the kind of survivor, fire or cracked floor. */
    private static final class LessonPicture implements rescuegrid.model.Drawable {

        private rescuegrid.model.Drawable picture;

        void show(Lesson.Picture kind) {
            switch (kind) {
                case MEDIC:
                    picture = new Medic(0, 0);
                    break;
                case ENGINEER:
                    picture = new Engineer(0, 0);
                    break;
                case DOG_HANDLER:
                    picture = new DogHandler(0, 0);
                    break;
                case MINOR:
                    picture = new MinorSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
                    break;
                case TRAPPED:
                    SeriousSurvivor trapped = new SeriousSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
                    trapped.setTrapped(true);
                    picture = trapped;
                    break;
                case CRITICAL:
                    picture = new CriticalSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
                    break;
                case FIRE:
                    picture = new Fire(0, 0);
                    break;
                case AFTERSHOCK:
                    picture = new CrackedFloor(0, 0);
                    break;
                default:                  // SERIOUS, and CLOCK (someone getting weaker)
                    picture = new SeriousSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
                    break;
            }
        }

        @Override
        public void draw(Painter painter, float x, float y, float size, AssetManager assets) {
            if (picture != null) {
                picture.draw(painter, x, y, size, assets);
            }
        }
    }

    /** The rescuer on the map right now. */
    private Rescuer active() {
        return board.getActiveRescuer();
    }

    /** One-shot keys: changing role and the special action. */
    private void handleActionKeys() {
        // wasPressed() uses the press up, so each key is read exactly once, here.
        boolean one = keys.wasPressed(Input.Keys.NUM_1);
        boolean two = keys.wasPressed(Input.Keys.NUM_2);
        boolean three = keys.wasPressed(Input.Keys.NUM_3);
        boolean tab = keys.wasPressed(Input.Keys.TAB);
        boolean backupKey = keys.wasPressed(Input.Keys.Q);
        boolean abilityKey = keys.wasPressed(Input.Keys.E);

        boolean roleKey = one || two || three || tab;
        if (roleKey && tutorial != null && !tutorial.canChangeRole()) {
            notYet();
        } else if (one) {
            switchRole(0);
        } else if (two) {
            switchRole(1);
        } else if (three) {
            switchRole(2);
        } else if (tab) {
            switchRole(-1);
        }

        if (backupKey && tutorial != null && !tutorial.canCallBackup()) {
            notYet();
        } else if (backupKey) {
            try {
                checkNotChangingGear();
                Survivor patient = active().findSurvivorNearby(board);
                toast.show(active().callBackup(board));
                sounds.play("radio");
                radioSecondsLeft = RADIO_SECONDS;
                backupTeams.add(new BackupTeam(patient, board, exitRow, exitCol));
                if (tutorial != null) {
                    tutorial.onBackupCalled();
                }
            } catch (InvalidMoveException e) {
                sounds.play("denied");
                toast.show(e.getMessage());
            }
        }

        if (abilityKey && tutorial != null && !tutorial.canUseAbility(active().getSpecialName())) {
            notYet();
        } else if (abilityKey) {
            try {
                checkNotChangingGear();
                // Runs the Medic's, Engineer's or Dog Handler's own version.
                String result = active().specialAction(board);
                sounds.play(active().getSpecialSound());
                toast.show(result);
                if (tutorial != null) {
                    tutorial.onAbility(active().getSpecialName());
                }
            } catch (InvalidMoveException e) {
                sounds.play("denied");
                toast.show(e.getMessage());
            }
        }
    }

    /** In training, a key that hasn't been taught yet does nothing but say so. */
    private void notYet() {
        sounds.play("denied");
        toast.show("Not yet: follow the training card");
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
            bumped = null; // let go of the key: walking into them again asks again
            bumpedTile = null;
            return;
        }
        if (tutorial != null) {
            tutorial.onDirection(direction);
        }
        if (board.getTeam().isChangingGear()) {
            return; // can't walk while putting on the new role's gear
        }
        Rescuer rescuer = active();
        rescuer.setFacing(direction); // turn even if the step is blocked
        if (!rescuer.canStep()) {
            return;
        }
        Survivor ahead = movement.survivorAhead(board, rescuer, direction);
        if (ahead != null) {
            if (ahead != bumped) { // holding the key into someone only asks them once
                walkInto(ahead);
            }
            return;
        }
        if (!movement.canStep(board, rescuer, direction)) {
            bumpInto(rescuer.getFacedTile(board));
            return;
        }
        bumped = null;
        bumpedTile = null;
        rescuer.moveTo(rescuer.getRow() + direction.getRowChange(), rescuer.getCol() + direction.getColChange());
        sounds.play("step");
        afterStep();
    }

    /**
     * Survivors are in the way like real people: walking into one helps them
     * (they follow, or are picked up), or says what they need first.
     */
    private void walkInto(Survivor survivor) {
        bumped = survivor;
        survivor.reveal();
        try {
            survivor.reachedBy(active());
            sounds.play("pickup");
        } catch (InvalidMoveException e) {
            sounds.play("denied");
            toast.show(e.getMessage());
        }
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
     * reached) leave the building.
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
        }
    }

    /**
     * Walking into furniture: a soft thud, and the first time in a level its name,
     * so it's clear it is solid ("Locker: furniture blocks the way").
     */
    private void bumpInto(Tile tile) {
        if (tile == null || tile == bumpedTile || tile.getBumpName() == null) {
            return;
        }
        bumpedTile = tile;
        sounds.play("denied");
        if (furnitureNamed.add(tile.getBumpName())) {
            toast.show(tile.getBumpName() + ": furniture blocks the way");
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
            sounds.play("switch"); // the dust puff, the bar and the HUD's "(you)" show the change
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
        if (state == GameState.PAUSED) {
            showPauseOptions();
            pauseMenu.reset();
        }
        pauseCard.setVisible(state == GameState.PAUSED);
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
                    continue; // the stretcher team and "Saved!" show it: no message
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
                case SURVIVOR_FADING:
                    sounds.play("time_warning");
                    if (isFadingSurvivorInView()) {
                        continue; // the red outline and seconds already show it: no message
                    }
                    break;
                default:
                    break;
            }
            if (!event.getMessage().isEmpty()) {
                toast.show(event.getMessage());
            }
        }
    }

    /** True if a survivor who is running out of time is on screen (so the map already shows it). */
    private boolean isFadingSurvivorInView() {
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && turns.isFading(survivor)
                    && camera.isInView(survivor.getRow(), survivor.getCol())) {
                return true;
            }
        }
        return false;
    }

    private void shake(double seconds, int pixels) {
        shakeSecondsLeft = seconds;
        shakePixels = pixels;
    }

    /** The mission is over: a moment with the result over the map, then the result screen. */
    private void finishLevel() {
        boolean success = turns.isSuccess();
        state = GameState.ENDING;
        endSecondsLeft = END_PAUSE_SECONDS;
        sounds.stopMusic();
        sounds.play(success ? "success" : "failure");
        String complete = level.isTutorial() ? "TRAINING COMPLETE" : "MISSION COMPLETE";
        endTitle.setText(success ? complete : "MISSION FAILED");
        endTitle.setColor(success ? EXIT_GUIDE : UiStyle.ERROR);
        endLine.setText("Rescued " + board.getRescuedCount() + " of " + board.getSurvivors().size());
        endCard.setVisible(true);
        toast.hideNow();
    }

    private void showResults() {
        boolean success = turns.isSuccess();
        state = success ? GameState.LEVEL_COMPLETE : GameState.GAME_OVER;
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
        // The map fills the left part of the scaled window (the same scale as the HUD).
        Viewport fit = stage.getViewport();
        int mapWidth = Math.round(fit.getScreenWidth() * (float) VIEW_WIDTH / GameFrame.WINDOW_WIDTH);
        HdpiUtils.glViewport(fit.getScreenX(), fit.getScreenY(), mapWidth, fit.getScreenHeight());
        painter.begin(camera.getProjection(shakeX, shakeY));
        drawTiles();
        drawCollapseWarning();
        drawFog();
        drawFacedTile();
        drawAbilityReach();
        drawPeople();
        drawBackup();
        drawTimers();
        drawHelpCalls();
        drawGuide();
        drawSurvivorHint();
        painter.end();
    }

    /** Things on top of the map that never scroll or shake: edge glow, arrows and the pause veil. */
    private void drawOverlay() {
        painter.begin(overlayCamera.combined);
        drawEdgeGlow();
        drawHelpArrows();
        drawGuideArrow();
        if (state == GameState.PAUSED || state == GameState.BRIEFING || state == GameState.ENDING) {
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
                board.getTile(row, col).drawOnBoard(painter, camera.toMapX(col), camera.toMapY(row), TILE_SIZE,
                        assets, board);
            }
        }
    }

    /** 0 to 1 and back, WARNING_PULSES_PER_SECOND times a second. Stops while paused. */
    private float warningPulse() {
        double cycles = wallSeconds() * WARNING_PULSES_PER_SECOND; // keeps pulsing on a frozen lesson card
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
        if (helpCallsHidden()) {
            return;
        }
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

    /** Training: the one thing to look at right now (a door, survivor or rubble), or null. */
    private int[] trainingMarker() {
        if (state == GameState.LESSON) {
            return lesson.getFocus(); // what the card is about
        }
        return tutorial == null ? null : tutorial.getMarker(board);
    }

    private String trainingMarkerLabel() {
        return state == GameState.LESSON ? lesson.getLabel() : tutorial.getMarkerLabel();
    }

    /**
     * One thing pulses at a time: in training, what this step is about (white);
     * otherwise the exit (green) while someone needs taking there.
     */
    private void drawGuide() {
        int[] marker = trainingMarker();
        if (marker != null) {
            drawBeacon(marker[0], marker[1], trainingMarkerLabel(), Color.WHITE);
        } else if (isGuidingToExit()) {
            drawBeacon(exitRow, exitCol, "EXIT", EXIT_GUIDE);
        }
    }

    /** The same guide, as an arrow at the edge of the view when it's off screen. */
    private void drawGuideArrow() {
        int[] marker = trainingMarker();
        if (marker != null) {
            if (!camera.isInView(marker[0], marker[1])) {
                drawEdgeArrow(marker[0], marker[1], trainingMarkerLabel(), Color.WHITE);
            }
        } else if (isGuidingToExit() && !camera.isInView(exitRow, exitCol)) {
            drawEdgeArrow(exitRow, exitCol, "EXIT", EXIT_GUIDE);
        }
    }

    /** Training: while a door is being pointed out, calls for help wait (one thing at a time). */
    private boolean helpCallsHidden() {
        return tutorial != null && tutorial.isShowingDoor();
    }

    /** Next to a survivor, a line under them says what they need (walk into them, Medic, Engineer...). */
    private void drawSurvivorHint() {
        Rescuer rescuer = active();
        for (Survivor survivor : board.getSurvivors()) {
            int distance = Math.abs(survivor.getRow() - rescuer.getRow())
                    + Math.abs(survivor.getCol() - rescuer.getCol());
            String need = survivor.getNeed();
            if (distance == 1 && survivor.isWaiting() && survivor.isRevealed() && need != null) {
                float centerX = camera.toMapX(survivor.getCol()) + TILE_SIZE / 2f;
                float top = camera.toMapY(survivor.getRow()) + TILE_SIZE + 6;
                if (turns.isSurvivorsWeakening() && !survivor.isBackupComing()) {
                    top += TIMER_LINE; // under their timer
                }
                painter.textOnBox(need, Color.WHITE, HINT_BOX, centerX, top, 4);
            }
        }
    }

    /** A pulsing outline round one tile, with a label above it. */
    private void drawBeacon(int row, int col, String label, Color color) {
        if (!camera.isInView(row, col)) {
            return;
        }
        float pulse = (float) (0.5 - 0.5 * Math.cos(wallSeconds() * 2 * Math.PI));
        Color edge = new Color(color.r, color.g, color.b, 0.5f + 0.5f * pulse);
        float x = camera.toMapX(col);
        float y = camera.toMapY(row);
        float grow = 4 * pulse;
        painter.outlineRect(edge, x - grow, y - grow, TILE_SIZE + 2 * grow, TILE_SIZE + 2 * grow, 3);
        painter.text(label, color, x + TILE_SIZE / 2f, y - TILE_SIZE * 1.2f); // above a head and its "!" bubble
    }

    /** For calls from off screen: an arrow at the edge of the view pointing towards them. */
    private void drawHelpArrows() {
        if (helpCallsHidden()) {
            return;
        }
        for (Survivor survivor : turns.getCallingSurvivors()) {
            if (!camera.isInView(survivor.getRow(), survivor.getCol())) {
                drawEdgeArrow(survivor.getRow(), survivor.getCol(), "HELP!", Color.WHITE);
            }
        }
    }

    /** An arrow at the edge of the view pointing towards an off-screen tile, with a label. */
    private void drawEdgeArrow(int row, int col, String label, Color color) {
        float centerX = VIEW_WIDTH / 2f;
        float centerY = (VIEW_HEIGHT - camera.getBottomCover()) / 2f; // keep arrows above the training card
        float margin = 40;
        float dx = camera.toScreenX(col) + TILE_SIZE / 2f - centerX;
        float dy = camera.toScreenY(row) + TILE_SIZE / 2f - centerY;
        // Walk out from the middle until we touch the edge of the view (minus a margin).
        float scale = Math.min((centerX - margin) / Math.max(1, Math.abs(dx)),
                (centerY - margin) / Math.max(1, Math.abs(dy)));
        float x = centerX + dx * scale;
        float y = centerY + dy * scale;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float ux = dx / length;
        float uy = dy / length;
        float tip = 14;
        painter.fillPolygon(color,
                x + ux * tip, y + uy * tip,
                x - ux * tip / 2 - uy * tip * 0.7f, y - uy * tip / 2 + ux * tip * 0.7f,
                x - ux * tip / 2 + uy * tip * 0.7f, y - uy * tip / 2 - ux * tip * 0.7f);
        painter.text(label, color, x - ux * 28, y - uy * 28 - 6);
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

    /**
     * The survivors (only once revealed, plus the one following you) and the
     * rescuer, top row first: people are taller than a tile in the angled view, so
     * someone lower down must overlap the head of someone above them.
     */
    private void drawPeople() {
        List<double[]> order = new ArrayList<>();       // {draw row, index}; index -1 is the rescuer
        List<Runnable> drawings = new ArrayList<>();
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isWaiting() && survivor.isRevealed()
                    && camera.isInView(survivor.getRow(), survivor.getCol())) {
                order.add(new double[] {survivor.getRow(), drawings.size()});
                // One marker each: running out of time, the red countdown replaces the "!" bubble.
                boolean bubble = !turns.isFading(survivor);
                drawings.add(() -> survivor.drawOnMap(painter, camera.toMapX(survivor.getCol()),
                        camera.toMapY(survivor.getRow()), TILE_SIZE, assets, false, bubble));
            }
        }
        Rescuer rescuer = active();
        Survivor follower = rescuer.getFollower();
        if (follower != null) {
            double progress = rescuer.stepProgress(); // slides along with the rescuer
            order.add(new double[] {follower.getDrawRow(progress), drawings.size()});
            drawings.add(() -> follower.drawOnMap(painter, camera.toMapX(follower.getDrawCol(progress)),
                    camera.toMapY(follower.getDrawRow(progress)), TILE_SIZE, assets, progress < 1.0));
        }
        if (!rescuer.isDowned()) {
            order.add(new double[] {rescuer.getDrawRow(), drawings.size()});
            drawings.add(this::drawRescuer);
        }
        order.sort((a, b) -> Double.compare(a[0], b[0]));
        for (double[] entry : order) {
            drawings.get((int) entry[1]).run();
        }
    }

    /**
     * Every found survivor shows the seconds they have left under them, so the
     * timer is always visible: white, orange from 30 s, red from 10 s. From 30 s
     * they also pulse red. The person following you shows theirs too. Not while
     * backup is coming (the flare shows that countdown), and not in training's
     * calm part, where nobody weakens.
     */
    private void drawTimers() {
        if (!turns.isSurvivorsWeakening()) {
            return;
        }
        float pulse = (float) (0.5 - 0.5 * Math.cos(wallSeconds() * 4 * Math.PI)); // 2 a second
        Color edge = new Color(UiStyle.ERROR.r, UiStyle.ERROR.g, UiStyle.ERROR.b, 0.4f + 0.6f * pulse);
        for (Survivor survivor : board.getSurvivors()) {
            if (!survivor.isWaiting() || !survivor.isRevealed() || survivor.isBackupComing()
                    || !camera.isInView(survivor.getRow(), survivor.getCol())) {
                continue;
            }
            float x = camera.toMapX(survivor.getCol());
            float y = camera.toMapY(survivor.getRow());
            if (turns.isFading(survivor)) {
                painter.outlineRect(edge, x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 3);
            }
            drawTimer(survivor, x, y);
        }
        Survivor follower = active().getFollower();
        if (follower != null) {
            double progress = active().stepProgress();
            drawTimer(follower, camera.toMapX(follower.getDrawCol(progress)),
                    camera.toMapY(follower.getDrawRow(progress)));
        }
    }

    /** "45 s" under a survivor, in white, orange (30 s) or red (10 s). */
    private void drawTimer(Survivor survivor, float x, float y) {
        double left = turns.getSecondsLeft(survivor);
        Color color = left <= TurnManager.FADING_WARNINGS[1] ? UiStyle.ERROR
                : left <= TurnManager.FADING_WARNINGS[0] ? UiStyle.WARNING : UiStyle.TEXT;
        painter.textOnBox((int) Math.ceil(left) + " s", color, HINT_BOX, x + TILE_SIZE / 2f, y + TILE_SIZE + 6, 4);
    }

    /**
     * Faint paw prints on every seen tile the dog's scent reaches, so the player
     * knows how far E works (decision 0021). Only while Scent is ready (decision 0022).
     * Unexplored tiles get none, so the prints never give the map away.
     */
    private void drawAbilityReach() {
        if (state != GameState.PLAYING) {
            return;
        }
        boolean[][] reach = active().getAbilityReach(board);
        if (reach == null || !active().canUseSpecial()) {
            return; // only while Scent is ready: fewer marks on the map
        }
        Color paw = PAW_READY;
        int lastRow = Math.min(camera.getFirstRow() + VIEW_ROWS + 1, board.getRows());
        int lastCol = Math.min(camera.getFirstCol() + VIEW_COLS + 1, board.getCols());
        for (int row = camera.getFirstRow(); row < lastRow; row++) {
            for (int col = camera.getFirstCol(); col < lastCol; col++) {
                Tile tile = board.getTile(row, col);
                if (reach[row][col] && tile.isExplored() && tile.isWalkable()
                        && !(row == active().getRow() && col == active().getCol())) {
                    drawPaw(paw, camera.toMapX(col), camera.toMapY(row), (row + col) % 2 == 0);
                }
            }
        }
    }

    /** One paw print: a pad and four toes, a little left or right of the tile's middle. */
    private void drawPaw(Color color, float x, float y, boolean left) {
        float s = TILE_SIZE / 16f; // one sprite pixel
        float cx = x + TILE_SIZE / 2f + (left ? -2 : 2) * s;
        float cy = y + TILE_SIZE / 2f + (left ? -1 : 1) * s;
        painter.fillOval(color, cx - 2 * s, cy, 4 * s, 3 * s);
        painter.fillOval(color, cx - 3 * s, cy - 2 * s, 1.5f * s, 1.5f * s);
        painter.fillOval(color, cx - 1.5f * s, cy - 3 * s, 1.5f * s, 1.5f * s);
        painter.fillOval(color, cx, cy - 3 * s, 1.5f * s, 1.5f * s);
        painter.fillOval(color, cx + 1.5f * s, cy - 2 * s, 1.5f * s, 1.5f * s);
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

    /**
     * Backup on its way: a red flare by each red survivor with the seconds left,
     * and the stretcher teams walking in and out (decision 0020).
     */
    private void drawBackup() {
        int flareFrame = (int) ((System.currentTimeMillis() / FLARE_FRAME_MILLIS) % 2);
        TextureRegion flare = assets.getFrame("flare", 0, flareFrame, AssetManager.TILE_PIXELS,
                AssetManager.TILE_PIXELS);
        for (Survivor survivor : board.getSurvivors()) {
            if (!survivor.isWaiting() || !survivor.isBackupComing() || !survivor.isRevealed()
                    || !camera.isInView(survivor.getRow(), survivor.getCol())) {
                continue;
            }
            float x = camera.toMapX(survivor.getCol());
            float y = camera.toMapY(survivor.getRow());
            if (flare != null) {
                painter.image(flare, x + TILE_SIZE * 0.55f, y - TILE_SIZE * 0.1f, TILE_SIZE * 0.6f,
                        TILE_SIZE * 0.6f);
            }
            String left = (int) Math.ceil(survivor.getBackupSecondsLeft()) + " s";
            painter.textOnBox(left, UiStyle.TEXT, FLARE_BOX, x + TILE_SIZE * 0.85f, y + TILE_SIZE * 0.5f, 4);
        }
        for (BackupTeam team : backupTeams) {
            team.draw(painter, camera, assets, board, TILE_SIZE);
        }
    }

    /**
     * The one rescuer on the map. While they change gear, a puff of dust hides
     * the swap and a bar fills above their head; after calling backup, a radio
     * bubble shows they are on the radio.
     */
    private void drawRescuer() {
        Rescuer rescuer = active();
        float x = camera.toMapX(rescuer.getDrawCol());
        float y = camera.toMapY(rescuer.getDrawRow());
        rescuer.drawOnMap(painter, x, y, TILE_SIZE, assets);
        float height = TILE_SIZE * AssetManager.CHARACTER_PIXELS / (float) AssetManager.TILE_PIXELS;
        if (board.getTeam().isChangingGear()) {
            int frame = Math.min(3, (int) (board.getTeam().getGearChangeProgress() * 4));
            TextureRegion puff = assets.getFrame("puff", 0, frame, AssetManager.TILE_PIXELS,
                    AssetManager.CHARACTER_PIXELS);
            if (puff != null) {
                painter.image(puff, x, y + TILE_SIZE - height, TILE_SIZE, height);
            }
        }
        if (radioSecondsLeft > 0) {
            int frame = 2 + (int) ((System.currentTimeMillis() / BUBBLE_FRAME_MILLIS) % 2);
            TextureRegion bubble = assets.getFrame("emote", 0, frame, AssetManager.TILE_PIXELS,
                    AssetManager.TILE_PIXELS);
            if (bubble != null) {
                painter.image(bubble, x + TILE_SIZE * 0.45f, y - TILE_SIZE * 1.05f, TILE_SIZE * 0.75f,
                        TILE_SIZE * 0.75f);
            }
        }
        if (board.getTeam().isChangingGear()) {
            float width = TILE_SIZE - 12;
            float progress = (float) board.getTeam().getGearChangeProgress();
            float barY = y - TILE_SIZE / 2f - 10;
            painter.fillRect(GEAR_BAR_BACK, x + 6, barY, width, 6);
            painter.fillRect(UiStyle.TEXT, x + 6, barY, width * progress, 6);
        }
    }
}
