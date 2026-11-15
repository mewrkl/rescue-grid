package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.ScoreKeeper;
import rescuegrid.model.GameBoard;

/** The end-of-mission screen: result, score breakdown and what to do next. */
public class ResultPanel extends BaseScreen {

    private static final float BREAKDOWN_WIDTH = 320;

    private final ScoreKeeper scoreKeeper;
    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label[] pointLabels;
    private final Label scoreLabel;
    private final MenuList menu;

    public ResultPanel(GameFrame game) {
        super(game);
        scoreKeeper = new ScoreKeeper();

        titleLabel = style.pixelLabel(" ", 32, UiStyle.TEXT);
        summaryLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        scoreLabel = style.pixelLabel(" ", 24, UiStyle.TEXT);

        // Score breakdown: name on the left, points on the right.
        String[] names = {"Survivors rescued", "Health remaining", "Everyone rescued bonus"};
        pointLabels = new Label[names.length];
        Table breakdown = new Table();
        for (int i = 0; i < names.length; i++) {
            pointLabels[i] = style.label("0", UiStyle.TEXT);
            pointLabels[i].setAlignment(Align.right);
            breakdown.add(style.label(names[i], UiStyle.TEXT_SECONDARY)).left().expandX().padTop(4);
            breakdown.add(pointLabels[i]).right().padTop(4).row();
        }

        menu = createMenu();
        menu.setBackAction(game::showMenu);

        root.add(titleLabel).row();
        root.add(summaryLabel).padTop(16).row();
        root.add(breakdown).width(BREAKDOWN_WIDTH).padTop(32).row();
        root.add(scoreLabel).padTop(24).row();
        root.add(menu).padTop(32).row();
        root.add(hint("Arrow keys to choose  ·  Enter to select")).padTop(32).row();
    }

    public void showResult(boolean success, LevelData level, GameBoard board, double seconds) {
        boolean finishedGame = success && !level.hasNextLevel();
        String complete = level.isTutorial() ? "TRAINING COMPLETE" : "MISSION COMPLETE";
        titleLabel.setText(finishedGame ? "ALL LEVELS CLEARED"
                : success ? complete : "MISSION FAILED");
        String name = level.isTutorial() ? "Training" : "Level " + level.getNumber();
        int total = (int) seconds;
        String time = String.format("%d:%02d", total / 60, total % 60);
        summaryLabel.setText(name + " in " + time + ": rescued " + board.getRescuedCount()
                + " of " + board.getSurvivors().size() + " survivors (needed " + level.getTarget() + ")");

        pointLabels[0].setText(String.valueOf(scoreKeeper.rescuePoints(board)));
        pointLabels[1].setText(String.valueOf(scoreKeeper.healthPoints(board)));
        pointLabels[2].setText(String.valueOf(scoreKeeper.allRescuedBonus(board)));
        scoreLabel.setText("SCORE " + scoreKeeper.calculateScore(board));

        // The first option is what the player most likely wants next.
        menu.clearOptions();
        if (success && level.hasNextLevel()) {
            menu.addOption(level.isTutorial() ? "START LEVEL 1" : "NEXT LEVEL",
                    () -> game.startLevel(level.getNumber() + 1));
        }
        menu.addOption(success ? "PLAY AGAIN" : "TRY AGAIN", () -> game.startLevel(level.getNumber()));
        menu.addOption("MAIN MENU", game::showMenu);
    }
}
