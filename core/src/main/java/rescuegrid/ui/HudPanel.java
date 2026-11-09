package rescuegrid.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.Drawable;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.DogHandler;
import rescuegrid.model.rescuers.Engineer;
import rescuegrid.model.rescuers.Medic;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The panel right of the map: aftershock timer, survivors, team health and
 * the active ability, shown as bars and pictures. It only reads the game.
 */
public class HudPanel extends Table {

    public static final int WIDTH = 256;

    private static final float CONTENT_WIDTH = WIDTH - 48;
    private static final double AFTERSHOCK_ALERT_SECONDS = 5.0;
    private static final int TEAM_SIZE = 3;
    private static final int MAX_SURVIVORS = 5;
    private static final float ICON_SIZE = 32;

    private final Label levelLabel;
    private final Label timeLabel;
    private final ProgressBar aftershockBar;
    private final Label aftershockSeconds;
    private final SurvivorPip[] pips;
    private final IconSlot[] pipSlots;
    private final Label goalLabel;
    private final Label[] numberLabels;
    private final Label[] nameLabels;
    private final HealthBar[] healthBars;
    private final Label abilityLabel;
    private final ProgressBar cooldownBar;
    private final CarriedSurvivor carried;
    private final Table carryingRow;

    public HudPanel(UiStyle style, BaseScreen screen) {
        setBackground(style.solid(UiStyle.BACKGROUND));
        top().left().pad(24);
        defaults().left();

        levelLabel = style.pixelLabel("LEVEL 1", 16, UiStyle.TEXT);
        timeLabel = style.label("0:00", UiStyle.TEXT_SECONDARY);
        Table top = new Table();
        top.add(levelLabel).expandX().left();
        top.add(timeLabel).right();
        add(top).width(CONTENT_WIDTH).row();

        add(heading(style, "AFTERSHOCK")).padTop(24).row();
        aftershockBar = new ProgressBar(0, 1, 0.001f, false, style.meterStyle());
        aftershockSeconds = style.label("0", UiStyle.TEXT_SECONDARY);
        Table shock = new Table();
        shock.add(aftershockBar).expandX().fillX();
        shock.add(aftershockSeconds).width(24).padLeft(12).right();
        add(shock).width(CONTENT_WIDTH).padTop(8).row();

        add(heading(style, "SURVIVORS")).padTop(24).row();
        pips = new SurvivorPip[MAX_SURVIVORS];
        pipSlots = new IconSlot[MAX_SURVIVORS];
        Table pipRow = new Table();
        for (int i = 0; i < MAX_SURVIVORS; i++) {
            pips[i] = new SurvivorPip();
            pipSlots[i] = screen.icon(pips[i], ICON_SIZE);
            pipRow.add(pipSlots[i]).size(ICON_SIZE).padRight(8);
        }
        add(pipRow).padTop(8).row();
        goalLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        add(goalLabel).padTop(8).row();

        add(heading(style, "TEAM")).padTop(24).row();
        Drawable[] faces = {new Medic(0, 0), new Engineer(0, 0), new DogHandler(0, 0)};
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
            add(row).padTop(i == 0 ? 8 : 12).row();
        }

        add(heading(style, "ABILITY")).padTop(24).row();
        abilityLabel = style.label(" ", UiStyle.TEXT);
        cooldownBar = new ProgressBar(0, 1, 0.001f, false, style.meterStyle());
        Table words = new Table();
        words.add(abilityLabel).left().row();
        words.add(cooldownBar).width(120).left().padTop(4);
        Table ability = new Table();
        ability.add(style.keycap("E")).minWidth(32).height(32);
        ability.add(words).left().padLeft(12);
        add(ability).padTop(8).row();

        carried = new CarriedSurvivor();
        carryingRow = new Table();
        carryingRow.add(screen.icon(carried, ICON_SIZE)).size(ICON_SIZE);
        carryingRow.add(style.label("Carrying", UiStyle.TEXT_SECONDARY)).padLeft(12);
        add(carryingRow).padTop(12).row();

        add().expandY().row();
        Table pause = new Table();
        pause.add(style.keycap("ESC")).minWidth(32).height(32);
        pause.add(style.label("Pause", UiStyle.TEXT_SECONDARY)).padLeft(12);
        add(pause).row();
    }

    private static Label heading(UiStyle style, String text) {
        return style.pixelLabel(text, 8, UiStyle.TEXT_SECONDARY);
    }

    /** Updates everything from the current game. Called every frame by GamePanel. */
    public void refresh(GameBoard board, Rescuer active, TurnManager turns, LevelData level) {
        levelLabel.setText("LEVEL " + level.getNumber());
        timeLabel.setText(formatTime(turns.getElapsedSeconds()));
        refreshAftershock(turns, level);
        refreshSurvivors(board, turns, level);
        refreshTeam(board, active);
        refreshAbility(active);
    }

    /** The bar empties as the aftershock gets closer and turns orange in the last seconds. */
    private void refreshAftershock(TurnManager turns, LevelData level) {
        double left = turns.getSecondsUntilAftershock();
        double total = level.getAftershockTicks() * TurnManager.TICK_SECONDS;
        aftershockBar.setValue((float) (left / total));
        aftershockSeconds.setText(String.valueOf((int) Math.ceil(left)));
        Color color = left <= AFTERSHOCK_ALERT_SECONDS ? UiStyle.WARNING : UiStyle.TEXT_SECONDARY;
        aftershockBar.setColor(color);
        aftershockSeconds.setColor(color);
    }

    /** Saved survivors fill the slots from the left, lost ones from the right. */
    private void refreshSurvivors(GameBoard board, TurnManager turns, LevelData level) {
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
        if (turns.isTargetReached()) {
            goalLabel.setText("Leave by the EXIT");
            goalLabel.setColor(UiStyle.SELECTED);
        } else {
            goalLabel.setText("Save " + turns.getStillNeeded() + " more");
            goalLabel.setColor(UiStyle.TEXT_SECONDARY);
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
                nameLabels[i].setText(rescuer.getName());
                nameLabels[i].setColor(isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
            }
            healthBars[i].setVisible(!rescuer.isDowned());
            healthBars[i].setHealth(rescuer.getHealth(), isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
        }
    }

    /** The bar refills while the ability recharges; full and bright means ready. */
    private void refreshAbility(Rescuer active) {
        abilityLabel.setText(active.getSpecialName());
        boolean ready = active.canUseSpecial();
        float charged = 1 - (float) (active.getSpecialCooldown() / active.getCooldownSeconds());
        cooldownBar.setValue(ready ? 1 : charged);
        cooldownBar.setColor(ready ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
        abilityLabel.setColor(ready ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);

        carried.set(active.getCarriedSurvivor());
        carryingRow.setVisible(active.isCarrying());
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
