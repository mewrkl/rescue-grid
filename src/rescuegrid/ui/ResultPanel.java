package rescuegrid.ui;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rescuegrid.logic.LevelData;

/**
 * Shown when a mission ends: success or failure, survivors rescued, score,
 * and buttons for Next Level, Retry and Main Menu.
 */
public class ResultPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel detailsLabel;
    private final JButton nextLevel;
    private LevelData lastLevel;

    public ResultPanel(GameFrame frame) {
        setBackground(UiStyle.BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(96, 96, 96, 96));

        titleLabel = UiStyle.label(" ", 32, true, UiStyle.TEXT);
        detailsLabel = UiStyle.label(" ", 14, false, UiStyle.TEXT_SECONDARY);

        nextLevel = UiStyle.primaryButton("Next Level");
        nextLevel.addActionListener(e -> frame.startLevel(lastLevel.getNumber() + 1));
        JButton retry = UiStyle.secondaryButton("Retry");
        retry.addActionListener(e -> frame.startLevel(lastLevel.getNumber()));
        JButton menu = UiStyle.secondaryButton("Main Menu");
        menu.addActionListener(e -> frame.showScreen(GameFrame.MENU));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(nextLevel);
        buttons.add(retry);
        buttons.add(menu);

        add(titleLabel);
        add(Box.createVerticalStrut(8));
        add(detailsLabel);
        add(Box.createVerticalStrut(32));
        add(buttons);
        add(Box.createVerticalGlue());
    }

    public void showResult(boolean success, int rescued, LevelData level, int score) {
        lastLevel = level;
        titleLabel.setText(success ? "Mission complete" : "Mission failed");
        detailsLabel.setText("Level " + level.getNumber() + ": rescued " + rescued + " of "
                + level.getTarget() + " needed. Score " + score + ".");
        // Next Level only makes sense after a success, and only if there is another level.
        nextLevel.setVisible(success && level.hasNextLevel());
    }
}
