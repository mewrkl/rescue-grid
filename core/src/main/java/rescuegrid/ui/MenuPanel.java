package rescuegrid.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.LevelLoader;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;

/**
 * The title screen. Everything is centred:
 *
 *                 RESCUE GRID
 *       An earthquake has collapsed the building...
 *            [Medic] [Engineer] [Dog Handler]
 *                >  START GAME  <
 *                  HOW TO PLAY
 *                     QUIT
 *        Arrow keys to choose · Enter to select
 *
 * Behind it, a dimmed piece of Level 1 is drawn with the real tiles (fire
 * flickers), so the first thing a player sees is the game's own world.
 */
public class MenuPanel extends BaseScreen {

    private static final int TILE = GamePanel.TILE_SIZE;
    private static final int BACKDROP_FIRST_ROW = 0;   // the part of Level 1 with fire and cracked floor
    private static final int BACKDROP_FIRST_COL = 0;   // the fire ends up on the right, away from the title
    private static final Color BACKDROP_DIM = Painter.rgba(10, 10, 10, 200);

    private final Label errorLabel;
    private final GameBoard backdrop;

    public MenuPanel(GameFrame game) {
        super(game);
        backdrop = loadBackdrop();

        Label title = style.pixelLabel("RESCUE GRID", 48, UiStyle.TEXT);
        Label tagline = style.label("An earthquake has collapsed the building. Get the survivors out.",
                UiStyle.TEXT_SECONDARY);
        Table team = new Table();
        team.add(icon(new Medic(0, 0), 64)).padRight(16);
        team.add(icon(new Engineer(0, 0), 64)).padRight(16);
        team.add(icon(new DogHandler(0, 0), 64));

        MenuList menu = createMenu();
        menu.addOption("START GAME", () -> game.startLevel(1));
        menu.addOption("HOW TO PLAY", game::showHowToPlay);
        menu.addOption("QUIT", () -> Gdx.app.exit());

        errorLabel = style.label("", UiStyle.ERROR);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        root.add(title).row();
        root.add(tagline).padTop(24).row();
        root.add(team).padTop(32).row();
        root.add(menu).padTop(32).row();
        root.add(errorLabel).width(640).padTop(16).row();
        root.add(hint("Arrow keys to choose  ·  Enter to select")).padTop(32).row();
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
                    backdrop.getTile(mapRow, mapCol).draw(painter, col * TILE, row * TILE, TILE, game.getAssets());
                }
            }
        }
        painter.fillRect(BACKDROP_DIM, 0, 0, width, height); // dim it so the menu reads clearly
        painter.end();
    }

    @Override
    public void show() {
        super.show();
        errorLabel.setText("");
    }

    /** Shown under the menu, e.g. when a level file is missing or broken. */
    public void showError(String message) {
        errorLabel.setText(message);
    }
}
