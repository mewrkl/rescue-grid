package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.model.Drawable;
import rescuegrid.model.Survivor;
import rescuegrid.model.survivors.CriticalSurvivor;
import rescuegrid.model.survivors.MinorSurvivor;
import rescuegrid.model.survivors.SeriousSurvivor;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.tiles.CrackedFloor;
import rescuegrid.model.tiles.Exit;
import rescuegrid.model.tiles.Fire;
import rescuegrid.model.tiles.Rubble;

/**
 * How to Play: controls, the roles, the survivor types and the building in four columns,
 * drawn with the real game art.
 */
public class HowToPlayPanel extends BaseScreen {

    private static final float COLUMN_WIDTH = 256;
    private static final float ICON_SIZE = 40;
    private static final float TEXT_WIDTH = COLUMN_WIDTH - ICON_SIZE - 16;

    public HowToPlayPanel(GameFrame game) {
        super(game);

        Label goal = style.label("Find the survivors hidden in the dark and get them to the EXIT "
                + "before they run out of time.\nOne rescuer, three roles: change role to give each survivor "
                + "what they need. Saved enough? Step on the EXIT to leave.", UiStyle.TEXT);
        goal.setAlignment(Align.center);

        Table columns = new Table();
        columns.top();
        columns.add(controlsColumn()).width(COLUMN_WIDTH).top().padRight(24);
        columns.add(teamColumn()).width(COLUMN_WIDTH).top().padRight(24);
        columns.add(survivorColumn()).width(COLUMN_WIDTH).top().padRight(24);
        columns.add(buildingColumn()).width(COLUMN_WIDTH).top();

        MenuList menu = createMenu();
        menu.addOption("BACK", game::showMenu);
        menu.setBackAction(game::showMenu);

        root.add(style.pixelLabel("HOW TO PLAY", 32, UiStyle.TEXT)).row();
        root.add(goal).padTop(24).row();
        root.add(columns).padTop(32).row();
        root.add(menu).padTop(32).row();
        root.add(hint("Enter or Esc to go back")).padTop(16).row();
    }

    private Table controlsColumn() {
        Table column = heading("CONTROLS");
        keyRow(column, "Walk (hold to keep going)", "W", "A", "S", "D");
        keyRow(column, "Change role (takes 1 s)", "1", "2", "3");
        keyRow(column, "Next role", "TAB");
        keyRow(column, "Use ability on the tile you face", "E");
        keyRow(column, "Call backup for a critical survivor", "Q");
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

    private Table teamColumn() {
        Table column = heading("YOUR ROLES");
        iconRow(column, new Medic(0, 0), "1  Medic", "Treats survivors. Stabilises critical ones.");
        iconRow(column, new Engineer(0, 0), "2  Engineer", "Clears rubble. Lifts debris off trapped people.");
        iconRow(column, new DogHandler(0, 0), "3  Dog Handler", "The dog sniffs out hidden survivors nearby.");
        Label health = style.label("Each role has its own health. Only the one on the map gets hurt.",
                UiStyle.TEXT_SECONDARY);
        health.setWrap(true);
        column.add(health).width(COLUMN_WIDTH).left().padTop(12).row();
        return column;
    }

    private Table survivorColumn() {
        Table column = heading("SURVIVORS");
        iconRow(column, new MinorSurvivor(0, 0, Survivor.DEFAULT_CONDITION), "Minor (green)",
                "Walk onto them: they follow you out.");
        iconRow(column, new SeriousSurvivor(0, 0, Survivor.DEFAULT_CONDITION), "Serious (yellow)",
                "Walk onto them to carry them (slower).");
        iconRow(column, new CriticalSurvivor(0, 0, Survivor.DEFAULT_CONDITION), "Critical (red)",
                "Medic treats them, then Q calls backup.");
        Survivor trapped = new SeriousSurvivor(0, 0, Survivor.DEFAULT_CONDITION);
        trapped.setTrapped(true);
        iconRow(column, trapped, "Trapped", "The Engineer lifts the debris first.");
        return column;
    }

    private Table buildingColumn() {
        Table column = heading("THE BUILDING");
        iconRow(column, new Exit(0, 0), "Exit", "Bring survivors here to save them.");
        iconRow(column, new Fire(0, 0), "Fire", "Spreads and burns anyone next to it.");
        iconRow(column, new CrackedFloor(0, 0), "Cracked floor", "Collapses in an aftershock. Tiles about to fall flash orange.");
        iconRow(column, new Rubble(0, 0), "Rubble", "Blocks the way until cleared.");
        return column;
    }

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
