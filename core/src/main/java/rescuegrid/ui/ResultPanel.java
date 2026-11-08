package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.ScoreKeeper;
import rescuegrid.model.GameBoard;

/**
 * Shown when a mission ends: success or failure, survivors rescued, the score
 * with its breakdown, and buttons for Next Level, Retry and Main Menu.
 */
public class ResultPanel extends BaseScreen {

    private final ScoreKeeper scoreKeeper;
    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label breakdownLabel;
    private final Label scoreLabel;
    private final TextButton nextLevel;
    private final TextButton retry;
    private final TextButton menu;
    private final Table buttonRow;
    private LevelData lastLevel;

    public ResultPanel(GameFrame game) {
        super(game);
        scoreKeeper = new ScoreKeeper();
        root.pad(96);
        root.defaults().left();

        titleLabel = style.label(" ", 32, true, UiStyle.TEXT);
        summaryLabel = style.label(" ", 14, false, UiStyle.TEXT_SECONDARY);
        breakdownLabel = style.label(" ", 14, false, UiStyle.TEXT_SECONDARY);
        scoreLabel = style.label(" ", 20, true, UiStyle.TEXT);

        nextLevel = style.primaryButton("Next Level");
        onPress(nextLevel, () -> game.startLevel(lastLevel.getNumber() + 1));
        retry = style.secondaryButton("Retry Level");
        onPress(retry, () -> game.startLevel(lastLevel.getNumber()));
        menu = style.secondaryButton("Main Menu");
        onPress(menu, game::showMenu);
        buttonKeys.setBackAction(game::showMenu);

        buttonRow = new Table();
        root.add(titleLabel).row();
        root.add(summaryLabel).padTop(8).row();
        root.add(breakdownLabel).padTop(24).row();
        root.add(scoreLabel).padTop(12).row();
        root.add(buttonRow).padTop(32).row();
    }

    public void showResult(boolean success, LevelData level, GameBoard board, double seconds) {
        lastLevel = level;
        boolean finishedGame = success && !level.hasNextLevel();
        titleLabel.setText(finishedGame ? "Every level cleared"
                : success ? "Mission complete" : "Mission failed");
        summaryLabel.setText("Level " + level.getNumber() + ": rescued " + board.getRescuedCount()
                + " of " + board.getSurvivors().size() + " survivors (needed " + level.getTarget() + ").");

        breakdownLabel.setText("Survivors rescued: " + scoreKeeper.rescuePoints(board) + "\n"
                + "Health remaining: " + scoreKeeper.healthPoints(board) + "\n"
                + "Everyone rescued bonus: " + scoreKeeper.allRescuedBonus(board) + "\n"
                + "Time bonus: " + scoreKeeper.timeBonus(seconds));
        scoreLabel.setText("Score " + scoreKeeper.calculateScore(board, seconds));

        // Next Level only after a success, and only if there is another level.
        boolean showNext = success && level.hasNextLevel();
        nextLevel.setVisible(showNext);
        retry.setText(success ? "Play Again" : "Retry Level");

        // Rebuild the row so a hidden Next Level button leaves no gap.
        buttonRow.clearChildren();
        buttonRow.defaults().size(UiStyle.BUTTON_WIDTH, UiStyle.BUTTON_HEIGHT);
        if (showNext) {
            buttonRow.add(nextLevel).padRight(12);
        }
        buttonRow.add(retry).padRight(12);
        buttonRow.add(menu);
    }
}
