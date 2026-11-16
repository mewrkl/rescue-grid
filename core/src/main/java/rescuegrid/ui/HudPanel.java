package rescuegrid.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.LevelData;
import rescuegrid.logic.Objective;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The panel right of the map: time, next aftershock, survivors and the
 * objective checklist, team health, the abilities (dimmed while they can't be
 * used), who is with you, and the minimap. Everything has a label, so nothing
 * is told by colour alone. It only reads the game.
 */
public class HudPanel extends Table {

    public static final int WIDTH = 256;

    private static final float CONTENT_WIDTH = WIDTH - 48;
    private static final float MAP_MAX_HEIGHT = 96;
    private static final double AFTERSHOCK_ALERT_SECONDS = 5.0;
    private static final int TEAM_SIZE = 3;
    private static final int MAX_SURVIVORS = 6;
    private static final int OBJECTIVE_LINES = 3;
    private static final float ICON_SIZE = 32;
    private static final float PIP_SIZE = 28;
    private static final float SECTION_GAP = 12;
    private static final float DIMMED = 0.5f;       // an action that can't be used right now
    private static final Color OPEN_BOX = Painter.rgba(255, 255, 255, 50);

    private final Label levelLabel;
    private final Label timeLabel;
    private final ProgressBar aftershockBar;
    private final Label aftershockSeconds;
    private final SurvivorPip[] pips;
    private final IconSlot[] pipSlots;
    private final Drawable doneBox;
    private final Drawable openBox;
    private final Image[] objectiveBoxes;
    private final Label[] objectiveLines;
    private final Label[] numberLabels;
    private final Label[] nameLabels;
    private final HealthBar[] healthBars;
    private final Table abilityRow;
    private final Label abilityLabel;
    private final ProgressBar cooldownBar;
    private final Table backupRow;
    private final CarriedSurvivor carried;
    private final Label carryingLabel;
    private final CarriedSurvivor follower;
    private final Label leadingLabel;
    private final Minimap minimap;
    private final IconSlot mapSlot;
    private final Cell<IconSlot> mapCell;
    private int mapScale = 4;

    public HudPanel(UiStyle style, BaseScreen screen) {
        setBackground(style.solid(UiStyle.BACKGROUND));
        top().left().pad(12, 24, 12, 24);
        defaults().left();

        // Level name, and the time taken so far.
        levelLabel = style.pixelLabel("LEVEL 1", 16, UiStyle.TEXT);
        timeLabel = style.label("Time 0:00", UiStyle.TEXT_SECONDARY);
        Table top = new Table();
        top.add(levelLabel).expandX().left();
        top.add(timeLabel).right();
        add(top).width(CONTENT_WIDTH).row();

        add(heading(style, "NEXT AFTERSHOCK")).padTop(SECTION_GAP).row();
        aftershockBar = new ProgressBar(0, 1, 0.001f, false, style.meterStyle());
        aftershockSeconds = style.label("0 s", UiStyle.TEXT_SECONDARY);
        Table shock = new Table();
        shock.add(aftershockBar).expandX().fillX();
        shock.add(aftershockSeconds).width(32).padLeft(8).right();
        add(shock).width(CONTENT_WIDTH).padTop(8).row();

        // Survivors: one box each (solid = needed, dashed = extra), then the checklist.
        Table survivorHeading = new Table();
        survivorHeading.add(heading(style, "SURVIVORS")).expandX().left();
        survivorHeading.add(style.label("dashed = extra", UiStyle.TEXT_SECONDARY)).right();
        add(survivorHeading).width(CONTENT_WIDTH).padTop(SECTION_GAP).row();
        pips = new SurvivorPip[MAX_SURVIVORS];
        pipSlots = new IconSlot[MAX_SURVIVORS];
        Table pipRow = new Table();
        for (int i = 0; i < MAX_SURVIVORS; i++) {
            pips[i] = new SurvivorPip();
            pipSlots[i] = screen.icon(pips[i], PIP_SIZE);
            pipRow.add(pipSlots[i]).size(PIP_SIZE).padRight(4);
        }
        add(pipRow).padTop(8).row();
        doneBox = style.solid(UiStyle.TEXT_SECONDARY);
        openBox = style.solid(OPEN_BOX);
        objectiveBoxes = new Image[OBJECTIVE_LINES];
        objectiveLines = new Label[OBJECTIVE_LINES];
        for (int i = 0; i < OBJECTIVE_LINES; i++) {
            objectiveBoxes[i] = new Image(openBox);
            objectiveLines[i] = style.label(" ", UiStyle.TEXT);
            Table line = new Table();
            line.add(objectiveBoxes[i]).size(8);
            line.add(objectiveLines[i]).padLeft(8);
            add(line).padTop(i == 0 ? 8 : 0).row();
        }

        Table teamHeading = new Table();
        teamHeading.add(heading(style, "TEAM HEALTH")).expandX().left();
        teamHeading.add(style.label("1 2 3 to switch", UiStyle.TEXT_SECONDARY)).right();
        add(teamHeading).width(CONTENT_WIDTH).padTop(SECTION_GAP).row();
        rescuegrid.model.Drawable[] faces = {new Medic(0, 0), new Engineer(0, 0), new DogHandler(0, 0)};
        numberLabels = new Label[TEAM_SIZE];
        nameLabels = new Label[TEAM_SIZE];
        healthBars = new HealthBar[TEAM_SIZE];
        for (int i = 0; i < TEAM_SIZE; i++) {
            numberLabels[i] = style.label(String.valueOf(i + 1), UiStyle.TEXT_SECONDARY);
            nameLabels[i] = style.label(" ", UiStyle.TEXT_SECONDARY);
            healthBars[i] = new HealthBar(style.getWhitePixel(), Rescuer.MAX_HEALTH);
            Table words = new Table();
            words.add(nameLabels[i]).left().row();
            words.add(healthBars[i]).left().padTop(4);
            Table row = new Table();
            row.add(numberLabels[i]).width(16);
            row.add(screen.icon(faces[i], ICON_SIZE)).size(ICON_SIZE);
            row.add(words).left().padLeft(12);
            add(row).padTop(i == 0 ? 8 : 4).row();
        }

        // Abilities: dimmed while they can't do anything, bright when they can.
        add(heading(style, "ABILITIES")).padTop(SECTION_GAP).row();
        abilityLabel = style.label(" ", UiStyle.TEXT);
        cooldownBar = new ProgressBar(0, 1, 0.001f, false, style.meterStyle());
        Table words = new Table();
        words.add(abilityLabel).left().row();
        words.add(cooldownBar).width(120).left().padTop(4);
        abilityRow = new Table();
        abilityRow.add(style.keycap("E")).minWidth(32).height(32);
        abilityRow.add(words).left().padLeft(12);
        add(abilityRow).padTop(8).row();
        backupRow = new Table();
        backupRow.add(style.keycap("Q")).minWidth(32).height(32);
        backupRow.add(style.label("Call backup (red)", UiStyle.TEXT)).left().padLeft(12);
        add(backupRow).padTop(8).row();

        // Always shown, so the limits are clear: one carried (yellow) and one following (green).
        add(heading(style, "WITH YOU")).padTop(SECTION_GAP).row();
        carried = new CarriedSurvivor();
        carryingLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        follower = new CarriedSurvivor();
        leadingLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        Table withYou = new Table();
        withYou.add(screen.icon(carried, ICON_SIZE)).size(ICON_SIZE);
        withYou.add(carryingLabel).padLeft(4).padRight(8);
        withYou.add(screen.icon(follower, ICON_SIZE)).size(ICON_SIZE);
        withYou.add(leadingLabel).padLeft(4);
        add(withYou).padTop(8).row();

        // The minimap, sized to the level (see setBoard), with the pause key beside its heading.
        Table mapHeading = new Table();
        mapHeading.add(heading(style, "MAP")).expandX().left();
        Table pause = new Table();
        pause.add(style.keycap("ESC")).minWidth(32).height(24);
        pause.add(style.label("Pause", UiStyle.TEXT_SECONDARY)).padLeft(8);
        mapHeading.add(pause).right();
        add(mapHeading).width(CONTENT_WIDTH).padTop(SECTION_GAP).row();
        minimap = new Minimap();
        mapSlot = screen.icon((painter, x, y, size, assets) -> minimap.draw(painter, x, y, mapScale, assets),
                ICON_SIZE);
        mapCell = add(mapSlot).padTop(8);
        row();
        add().expandY();
    }

    private static Label heading(UiStyle style, String text) {
        return style.pixelLabel(text, 8, UiStyle.TEXT_SECONDARY);
    }

    /** A new level: the minimap gets the biggest whole-pixel scale that fits the panel. */
    public void setBoard(GameBoard board) {
        minimap.setBoard(board);
        int byWidth = (int) ((CONTENT_WIDTH - 2) / board.getCols());
        int byHeight = (int) ((MAP_MAX_HEIGHT - 2) / board.getRows());
        mapScale = Math.max(1, Math.min(byWidth, byHeight));
        float width = board.getCols() * mapScale;
        float height = board.getRows() * mapScale;
        mapSlot.setSize(width, height);
        mapCell.size(width, height).padLeft((CONTENT_WIDTH - width) / 2);
        invalidateHierarchy();
    }

    /** Updates everything from the current game. Called every frame by GamePanel. */
    public void refresh(GameBoard board, Rescuer active, TurnManager turns, LevelData level,
                        List<Objective> objectives) {
        levelLabel.setText(level.getTitle());
        timeLabel.setText("Time " + formatTime(turns.getElapsedSeconds()));
        refreshAftershock(turns, level);
        refreshSurvivors(board, level, objectives);
        refreshTeam(board, active);
        refreshAbilities(board, active);
    }

    /** The bar empties as the aftershock gets closer and turns orange in the last seconds. */
    private void refreshAftershock(TurnManager turns, LevelData level) {
        if (!turns.isAftershockCounting()) {      // training: none until the planned one
            aftershockBar.setValue(0);
            aftershockSeconds.setText("off");
            aftershockBar.setColor(UiStyle.TEXT_SECONDARY);
            aftershockSeconds.setColor(UiStyle.TEXT_SECONDARY);
            return;
        }
        double left = turns.getSecondsUntilAftershock();
        double total = level.getAftershockTicks() * TurnManager.TICK_SECONDS;
        aftershockBar.setValue((float) (left / total));
        aftershockSeconds.setText((int) Math.ceil(left) + " s");
        Color color = left <= AFTERSHOCK_ALERT_SECONDS ? UiStyle.WARNING : UiStyle.TEXT_SECONDARY;
        aftershockBar.setColor(color);
        aftershockSeconds.setColor(color);
    }

    /** Saved survivors fill the boxes from the left, lost ones from the right; then the checklist. */
    private void refreshSurvivors(GameBoard board, LevelData level, List<Objective> objectives) {
        int count = board.getSurvivors().size();
        int saved = board.getRescuedCount();
        int lost = board.getLostCount();
        for (int i = 0; i < MAX_SURVIVORS; i++) {
            pipSlots[i].setVisible(i < count);
            SurvivorPip.State state = SurvivorPip.State.INSIDE;
            if (i < saved) {
                state = SurvivorPip.State.SAVED;
            } else if (i >= count - lost) {
                state = SurvivorPip.State.LOST;
            }
            pips[i].set(state, i < level.getTarget());
        }
        for (int i = 0; i < OBJECTIVE_LINES && i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            objectiveBoxes[i].setDrawable(objective.isDone() ? doneBox : openBox);
            objectiveLines[i].setText(objective.getText());
            objectiveLines[i].setColor(objective.isDone() ? UiStyle.TEXT_SECONDARY : UiStyle.TEXT);
        }
    }

    private void refreshTeam(GameBoard board, Rescuer active) {
        List<Rescuer> team = board.getRescuers();
        for (int i = 0; i < TEAM_SIZE && i < team.size(); i++) {
            Rescuer rescuer = team.get(i);
            boolean isActive = rescuer == active;
            numberLabels[i].setColor(isActive ? UiStyle.SELECTED : UiStyle.TEXT_SECONDARY);
            if (rescuer.isDowned()) {
                nameLabels[i].setText(rescuer.getName() + "  down");
                nameLabels[i].setColor(UiStyle.ERROR);
            } else {
                nameLabels[i].setText(rescuer.getName() + (isActive ? "  (you)" : ""));
                nameLabels[i].setColor(isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
            }
            healthBars[i].setVisible(!rescuer.isDowned());
            healthBars[i].setHealth(rescuer.getHealth(), isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
        }
    }

    /**
     * E: the bar refills while the ability recharges. The whole row is dimmed
     * while it recharges or has nothing to act on. Q is dimmed unless a
     * stabilised red survivor is right here.
     */
    private void refreshAbilities(GameBoard board, Rescuer active) {
        boolean ready = active.canUseSpecial();
        abilityLabel.setText(active.getSpecialName() + (ready ? "" : "  recharging"));
        float charged = 1 - (float) (active.getSpecialCooldown() / active.getCooldownSeconds());
        cooldownBar.setValue(ready ? 1 : charged);
        abilityRow.getColor().a = ready && active.hasSpecialTarget(board) ? 1f : DIMMED;
        backupRow.getColor().a = active.canCallBackup(board) ? 1f : DIMMED;

        carried.set(active.getCarriedSurvivor());
        showSlot(carryingLabel, "Carry", active.isCarrying());
        follower.set(active.getFollower());
        showSlot(leadingLabel, "Follow", active.isLeading());
    }

    /** "Carry 0/1" in grey when the slot is free, "Carry 1/1" in white when it is taken. */
    private void showSlot(Label label, String name, boolean taken) {
        label.setText(name + " " + (taken ? 1 : 0) + "/1");
        label.setColor(taken ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
