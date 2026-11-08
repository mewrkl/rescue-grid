package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import rescuegrid.model.Drawable;
import rescuegrid.model.Survivor;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.tiles.CrackedFloor;
import rescuegrid.model.tiles.Exit;
import rescuegrid.model.tiles.Fire;
import rescuegrid.model.tiles.Rubble;

/**
 * Explains the game in three columns instead of paragraphs, so a player can
 * take it in at a glance:
 *
 *   CONTROLS            YOUR TEAM                 THE BUILDING
 *   [W][A][S][D] Walk   (Medic) Treat ...         (Exit) Carry survivors here
 *   [1][2][3] Switch    (Engineer) Clear ...      (Fire) Spreads and burns
 *   ...                 ...                       ...
 *
 * Keys are drawn as keycaps, and the team and building columns use the real
 * game art (through IconSlot), so what you learn here is what you see in play.
 */
public class HowToPlayPanel extends BaseScreen {

    private static final float COLUMN_WIDTH = 320;
    private static final float ICON_SIZE = 48;
    private static final float TEXT_WIDTH = COLUMN_WIDTH - ICON_SIZE - 16;

    public HowToPlayPanel(GameFrame game) {
        super(game);

        Label goal = style.label("Find the survivors hidden in the dark and carry them to the EXIT "
                + "before they run out of time.", UiStyle.TEXT);

        Table columns = new Table();
        columns.top();
        columns.add(controlsColumn()).width(COLUMN_WIDTH).top().padRight(32);
        columns.add(teamColumn()).width(COLUMN_WIDTH).top().padRight(32);
        columns.add(buildingColumn()).width(COLUMN_WIDTH).top();

        MenuList menu = createMenu();
        menu.addOption("BACK", game::showMenu);
        menu.setBackAction(game::showMenu);

        root.add(style.pixelLabel("HOW TO PLAY", 32, UiStyle.TEXT)).row();
        root.add(goal).padTop(24).row();
        root.add(columns).padTop(48).row();
        root.add(menu).padTop(48).row();
        root.add(hint("Enter or Esc to go back")).padTop(16).row();
    }

    // ----- Column 1: the keys -----

    private Table controlsColumn() {
        Table column = heading("CONTROLS");
        keyRow(column, "Walk (hold to keep going)", "W", "A", "S", "D");
        keyRow(column, "Switch rescuer", "1", "2", "3");
        keyRow(column, "Next rescuer", "TAB");
        keyRow(column, "Use ability on the tile you face", "E");
        keyRow(column, "Pause", "ESC");
        return column;
    }

    private void keyRow(Table column, String meaning, String... keys) {
        Table caps = new Table();
        for (String key : keys) {
            caps.add(style.keycap(key)).minWidth(32).height(32).padRight(4);
        }
        Label text = style.label(meaning, UiStyle.TEXT_SECONDARY);
        text.setWrap(true);
        Table row = new Table();
        row.add(caps).left();
        row.add(text).expandX().fillX().padLeft(8);
        column.add(row).width(COLUMN_WIDTH).left().padTop(12).row();
    }

    // ----- Column 2: the team -----

    private Table teamColumn() {
        Table column = heading("YOUR TEAM");
        iconRow(column, new Medic(0, 0), "Medic", "Treats a survivor to give them more time.");
        iconRow(column, new Engineer(0, 0), "Engineer", "Clears rubble to open blocked paths.");
        iconRow(column, new DogHandler(0, 0), "Dog Handler", "The dog sniffs out hidden survivors nearby.");
        return column;
    }

    // ----- Column 3: the building -----

    private Table buildingColumn() {
        Table column = heading("THE BUILDING");
        iconRow(column, new Survivor(0, 0, Survivor.DEFAULT_CONDITION), "Survivor",
                "Walk onto them to pick them up.");
        iconRow(column, new Exit(0, 0), "Exit", "Carry survivors here to save them.");
        iconRow(column, new Fire(0, 0), "Fire", "Spreads and burns anyone next to it.");
        iconRow(column, new CrackedFloor(0, 0), "Cracked floor", "Turns into rubble in an aftershock.");
        iconRow(column, new Rubble(0, 0), "Rubble", "Blocks the way until cleared.");
        return column;
    }

    // ----- Shared pieces -----

    private Table heading(String text) {
        Table column = new Table();
        column.top().left();
        column.add(style.pixelLabel(text, 16, UiStyle.TEXT)).left().padBottom(4).row();
        return column;
    }

    private void iconRow(Table column, Drawable art, String name, String description) {
        Actor picture = icon(art, ICON_SIZE);
        Label nameLabel = style.label(name, UiStyle.TEXT);
        Label descriptionLabel = style.label(description, UiStyle.TEXT_SECONDARY);
        descriptionLabel.setWrap(true);

        Table words = new Table();
        words.add(nameLabel).left().row();
        words.add(descriptionLabel).width(TEXT_WIDTH).left();

        Table row = new Table();
        row.add(picture).size(ICON_SIZE).top();
        row.add(words).left().top().padLeft(16);
        column.add(row).width(COLUMN_WIDTH).left().padTop(12).row();
    }
}
