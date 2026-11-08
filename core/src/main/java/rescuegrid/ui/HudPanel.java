package rescuegrid.ui;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The information panel to the right of the map: level, time, next aftershock,
 * rescued count, the whole team's health, and the active rescuer's special.
 *
 * It only DISPLAYS the game. It never changes the game state itself.
 * Extends Table, Scene2D's layout widget: rows of labels, one under the other.
 */
public class HudPanel extends Table {

    public static final int WIDTH = 256;

    private static final long MESSAGE_MILLIS = 4000;
    private static final double AFTERSHOCK_ALERT_SECONDS = 5.0;
    private static final float CONTENT_WIDTH = WIDTH - 48;
    private static final int TEAM_SIZE = 3;

    private final UiStyle style;
    private final Label levelLabel;
    private final Label timeLabel;
    private final Label aftershockLabel;
    private final Label rescuedLabel;
    private final Label[] teamLabels;
    private final HealthBar[] healthBars;
    private final Label activeNameLabel;
    private final Label specialLabel;
    private final Label carryingLabel;
    private final Label messageLabel;
    private long messageShownAt;

    public HudPanel(UiStyle style) {
        this.style = style;
        setBackground(style.solid(UiStyle.BACKGROUND));
        top().left().pad(24);
        defaults().left();

        levelLabel = style.pixelLabel("LEVEL 1", 16, UiStyle.TEXT);
        timeLabel = style.label("Time 0:00", UiStyle.TEXT_SECONDARY);
        aftershockLabel = style.label("Next aftershock", UiStyle.TEXT_SECONDARY);
        rescuedLabel = style.label("Rescued 0 / 0", UiStyle.TEXT);

        teamLabels = new Label[TEAM_SIZE];
        healthBars = new HealthBar[TEAM_SIZE];
        for (int i = 0; i < TEAM_SIZE; i++) {
            teamLabels[i] = style.label(" ", UiStyle.TEXT_SECONDARY);
            healthBars[i] = new HealthBar(style.getWhitePixel(), Rescuer.MAX_HEALTH);
        }

        activeNameLabel = style.label(" ", UiStyle.TEXT);
        specialLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        carryingLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        messageLabel = style.label(" ", UiStyle.WARNING);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.topLeft);
        messageShownAt = 0;

        add(levelLabel).row();
        add(timeLabel).padTop(8).row();
        add(aftershockLabel).padTop(4).row();
        add(rescuedLabel).padTop(4).row();

        add(style.pixelLabel("TEAM", 8, UiStyle.TEXT_SECONDARY)).padTop(24).row();
        for (int i = 0; i < TEAM_SIZE; i++) {
            Table teamRow = new Table();
            teamRow.add(teamLabels[i]).width(128).left();
            teamRow.add(healthBars[i]).left();
            add(teamRow).padTop(i == 0 ? 8 : 4).row();
        }

        add(style.pixelLabel("CONTROLLING", 8, UiStyle.TEXT_SECONDARY)).padTop(24).row();
        add(activeNameLabel).padTop(8).row();
        add(specialLabel).padTop(4).row();
        add(carryingLabel).padTop(4).row();
        add(messageLabel).width(CONTENT_WIDTH).height(64).padTop(24).row(); // room for 3 lines

        add().expandY().row(); // pushes the key hints to the bottom
        add(style.label("WASD move · 1/2/3 switch", UiStyle.TEXT_SECONDARY)).row();
        add(style.label("E ability · Esc pause", UiStyle.TEXT_SECONDARY)).padTop(4).row();
    }

    /** Updates every label from the current game. Called every frame by GamePanel. */
    public void refresh(GameBoard board, Rescuer active, TurnManager turns, LevelData level) {
        levelLabel.setText("LEVEL " + level.getNumber());
        timeLabel.setText("Time " + formatTime(turns.getElapsedSeconds()));

        double untilAftershock = turns.getSecondsUntilAftershock();
        aftershockLabel.setText("Next aftershock " + (int) Math.ceil(untilAftershock) + " s");
        aftershockLabel.setColor(untilAftershock <= AFTERSHOCK_ALERT_SECONDS
                ? UiStyle.WARNING : UiStyle.TEXT_SECONDARY);

        rescuedLabel.setText("Rescued " + board.getRescuedCount() + " / " + level.getTarget());

        List<Rescuer> team = board.getRescuers();
        for (int i = 0; i < TEAM_SIZE && i < team.size(); i++) {
            Rescuer rescuer = team.get(i);
            boolean isActive = rescuer == active;
            teamLabels[i].setText((i + 1) + "  " + rescuer.getName() + (rescuer.isDowned() ? "  down" : ""));
            teamLabels[i].setColor(isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
            healthBars[i].setVisible(!rescuer.isDowned());
            healthBars[i].setHealth(rescuer.getHealth(), isActive ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
        }

        activeNameLabel.setText(active.getName());
        String readiness = active.canUseSpecial()
                ? "ready"
                : String.format("%.1f s", active.getSpecialCooldown());
        specialLabel.setText("E: " + active.getSpecialName() + " (" + readiness + ")");
        carryingLabel.setText(active.isCarrying()
                ? "Carrying survivor  " + active.getCarriedSurvivor().getCondition()
                        + " / " + active.getCarriedSurvivor().getMaxCondition()
                : "Hands free");

        if (messageShownAt > 0 && System.currentTimeMillis() - messageShownAt > MESSAGE_MILLIS) {
            messageLabel.setText(" ");
            messageShownAt = 0;
        }
    }

    /** Shows a short message (wrapped to fit the panel) for a few seconds. */
    public void showMessage(String message) {
        if (message.isEmpty()) {
            messageLabel.setText(" ");
            messageShownAt = 0;
            return;
        }
        messageLabel.setText(message);
        messageShownAt = System.currentTimeMillis();
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
