package rescuegrid.ui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.TurnManager;
import rescuegrid.model.GameBoard;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The information panel to the right of the map: level, time, next aftershock,
 * rescued count, the whole team's health, and the active rescuer's special.
 *
 * It only DISPLAYS the game. It never changes the game state itself.
 */
public class HudPanel extends JPanel {

    private static final int WIDTH = 256;

    private final JLabel levelLabel;
    private final JLabel timeLabel;
    private final JLabel aftershockLabel;
    private final JLabel rescuedLabel;
    private final JLabel[] teamLabels;
    private final JLabel activeNameLabel;
    private final JLabel specialLabel;
    private final JLabel carryingLabel;
    private final JLabel messageLabel;

    public HudPanel() {
        setBackground(UiStyle.BACKGROUND);
        setPreferredSize(new Dimension(WIDTH, GamePanel.VIEW_ROWS * GamePanel.TILE_SIZE));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UiStyle.BORDER),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        levelLabel = UiStyle.label("Level 1", 20, true, UiStyle.TEXT);
        timeLabel = UiStyle.label("Time 0:00", 14, false, UiStyle.TEXT_SECONDARY);
        aftershockLabel = UiStyle.label("Next aftershock 45 s", 14, false, UiStyle.TEXT_SECONDARY);
        rescuedLabel = UiStyle.label("Rescued 0 / 0", 14, true, UiStyle.TEXT);

        teamLabels = new JLabel[3];
        for (int i = 0; i < teamLabels.length; i++) {
            teamLabels[i] = UiStyle.label(" ", 14, false, UiStyle.TEXT_SECONDARY);
        }

        activeNameLabel = UiStyle.label(" ", 16, true, UiStyle.TEXT);
        specialLabel = UiStyle.label(" ", 14, false, UiStyle.TEXT_SECONDARY);
        carryingLabel = UiStyle.label(" ", 14, false, UiStyle.TEXT_SECONDARY);
        messageLabel = UiStyle.label(" ", 14, true, UiStyle.WARNING);

        add(levelLabel);
        add(Box.createVerticalStrut(8));
        add(timeLabel);
        add(Box.createVerticalStrut(4));
        add(aftershockLabel);
        add(Box.createVerticalStrut(4));
        add(rescuedLabel);
        add(Box.createVerticalStrut(24));
        add(UiStyle.label("TEAM", 12, true, UiStyle.TEXT_SECONDARY));
        add(Box.createVerticalStrut(8));
        for (JLabel teamLabel : teamLabels) {
            add(teamLabel);
            add(Box.createVerticalStrut(4));
        }
        add(Box.createVerticalStrut(20));
        add(UiStyle.label("CONTROLLING", 12, true, UiStyle.TEXT_SECONDARY));
        add(Box.createVerticalStrut(8));
        add(activeNameLabel);
        add(Box.createVerticalStrut(4));
        add(specialLabel);
        add(Box.createVerticalStrut(4));
        add(carryingLabel);
        add(Box.createVerticalStrut(24));
        add(messageLabel);
        add(Box.createVerticalGlue());
        add(UiStyle.label("WASD move · 1/2/3 switch", 12, false, UiStyle.TEXT_SECONDARY));
        add(Box.createVerticalStrut(4));
        add(UiStyle.label("Space special · Esc pause", 12, false, UiStyle.TEXT_SECONDARY));
    }

    /** Updates every label from the current game. Called every frame by GamePanel. */
    public void refresh(GameBoard board, Rescuer active, TurnManager turns, LevelData level) {
        levelLabel.setText("Level " + level.getNumber());
        timeLabel.setText("Time " + formatTime(turns.getElapsedSeconds()));
        aftershockLabel.setText("Next aftershock " + (int) Math.ceil(turns.getSecondsUntilAftershock()) + " s");
        rescuedLabel.setText("Rescued " + board.getRescuedCount() + " / " + level.getTarget());

        List<Rescuer> team = board.getRescuers();
        for (int i = 0; i < teamLabels.length && i < team.size(); i++) {
            Rescuer rescuer = team.get(i);
            String health = rescuer.isDowned() ? "downed" : rescuer.getHealth() + " / " + Rescuer.MAX_HEALTH;
            teamLabels[i].setText((i + 1) + "  " + rescuer.getName() + "   " + health);
            teamLabels[i].setForeground(rescuer == active ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
            teamLabels[i].setFont(UiStyle.font(14, rescuer == active));
        }

        activeNameLabel.setText(active.getName());
        String readiness = active.canUseSpecial()
                ? "ready"
                : String.format("%.1f s", active.getSpecialCooldown());
        specialLabel.setText("Space: " + active.getSpecialName() + " (" + readiness + ")");
        carryingLabel.setText(active.isCarrying()
                ? "Carrying survivor, condition " + active.getCarriedSurvivor().getCondition()
                : "Hands free");
    }

    /** Shows a short warning, e.g. "Nothing to clear here". Empty text clears it. */
    public void showMessage(String message) {
        messageLabel.setText(message.isEmpty() ? " " : message);
        // TODO (Week 2, Member 3): make messages disappear by themselves after about 3 seconds.
    }

    private String formatTime(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
