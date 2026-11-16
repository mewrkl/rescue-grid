package rescuegrid.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.LevelLoader;
import rescuegrid.model.GameBoard;

/**
 * The title screen: a dimmed piece of Level 1 behind a dark band, so the menu
 * reads clearly, with the three roles at 4x taking turns to step forward.
 */
public class MenuPanel extends BaseScreen {

    private static final int TILE = GamePanel.TILE_SIZE;
    private static final int BACKDROP_FIRST_ROW = 0;   // the part of Level 1 with fire and cracked floor
    private static final int BACKDROP_FIRST_COL = 0;   // the fire ends up on the right, away from the title
    private static final Color BACKDROP_DIM = Painter.rgba(10, 10, 10, 215);
    private static final Color BAND = Painter.rgba(10, 10, 10, 235);     // behind the title and menu
    private static final Color BAND_EDGE = Painter.rgba(255, 255, 255, 20);
    private static final float BAND_WIDTH = 640;

    private final Label errorLabel;
    private final GameBoard backdrop;
    private final MenuList menu;

    public MenuPanel(GameFrame game) {
        super(game);
        backdrop = loadBackdrop();

        Label title = style.pixelLabel("RESCUE GRID", 48, UiStyle.TEXT);
        Label tagline = style.label("An earthquake has collapsed the building. Get the survivors out.",
                UiStyle.TEXT_SECONDARY);
        IconSlot team = icon(new TeamLineup(), TeamLineup.LINEUP_HEIGHT);
        team.setSize(TeamLineup.LINEUP_WIDTH, TeamLineup.LINEUP_HEIGHT);

        menu = createMenu();
        buildOptions();

        errorLabel = style.label("", UiStyle.ERROR);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        root.add(title).row();
        root.add(tagline).padTop(16).row();
        root.add(team).size(TeamLineup.LINEUP_WIDTH, TeamLineup.LINEUP_HEIGHT).padTop(24).row();
        root.add(menu).padTop(8).row();
        root.add(errorLabel).width(640).padTop(16).row();
        root.add(hint("Arrow keys to choose  ·  Enter to select")).padTop(24).row();
    }

    /**
     * CONTINUE only once there is progress (it goes to the furthest open level);
     * PLAY asks about the training the first time only (GameFrame.play). Settings
     * live in OPTIONS, not on this screen.
     */
    private void buildOptions() {
        menu.clearOptions();
        if (game.getProgress().hasProgress()) {
            int level = game.getProgress().getContinueLevel();
            menu.addOption("CONTINUE - LEVEL " + level, game::continueGame);
        }
        menu.addOption("PLAY", game::play);
        menu.addOption("LEVEL SELECT", game::showLevelSelect);
        menu.addOption("OPTIONS", () -> game.showOptions(game::showMenu));
        menu.addOption("HOW TO PLAY", () -> game.showHowToPlay(game::showMenu));
        menu.addOption("QUIT", () -> Gdx.app.exit());
    }

    /** Level 1's map, used only as a picture. If it can't be loaded, the background stays plain. */
    private GameBoard loadBackdrop() {
        try {
            return new LevelLoader().load(LevelData.forLevel(1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void drawBackground() {
        if (backdrop == null) {
            return;
        }
        Painter painter = game.getPainter();
        float width = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        painter.begin(screenProjection().combined);
        for (int row = 0; row * TILE < height; row++) {
            for (int col = 0; col * TILE < width; col++) {
                int mapRow = BACKDROP_FIRST_ROW + row;
                int mapCol = BACKDROP_FIRST_COL + col;
                if (backdrop.inBounds(mapRow, mapCol)) {
                    backdrop.getTile(mapRow, mapCol).drawOnBoard(painter, col * TILE, row * TILE, TILE,
                            game.getAssets(), backdrop);
                }
            }
        }
        painter.fillRect(BACKDROP_DIM, 0, 0, width, height); // dim it so the menu reads clearly
        float bandX = (width - BAND_WIDTH) / 2;
        painter.fillRect(BAND, bandX, 0, BAND_WIDTH, height);
        painter.fillRect(BAND_EDGE, bandX - 1, 0, 1, height);
        painter.fillRect(BAND_EDGE, bandX + BAND_WIDTH, 0, 1, height);
        painter.end();
    }

    @Override
    public void show() {
        buildOptions(); // progress may have changed (CONTINUE appears, or goes after a reset)
        super.show();
        errorLabel.setText("");
    }

    /** Shown under the menu, e.g. when a level file is missing or broken. */
    public void showError(String message) {
        errorLabel.setText(message);
    }
}
