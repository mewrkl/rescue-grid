package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.MissionReport;
import rescuegrid.model.GameBoard;

/**
 * The end-of-mission screen: the result, stars for the three goals (filled in
 * one by one), why it failed and a tip, and what to do next. Stars are the only
 * grade (decision 0018). The game waits here until the player picks an option.
 */
public class ResultPanel extends BaseScreen {

    private static final float COLUMN_WIDTH = 300;
    private static final float BIG_STAR = 54;    // 9 x 6 px cells
    private static final float SMALL_STAR = 18;  // 9 x 2 px cells
    private static final float FIRST_STAR_DELAY = 0.5f;
    private static final float STAR_GAP_SECONDS = 0.35f;

    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label newBestLabel;
    private final Label reasonLabel;
    private final Label tipLabel;
    private final Label[] goalLabels;
    private final StarIcon[] bigStars;
    private final StarIcon[] goalStars;
    private final Table starRow;
    private final Table goalList;
    private final MenuList menu;

    public ResultPanel(GameFrame game) {
        super(game);
        titleLabel = style.pixelLabel(" ", 32, UiStyle.TEXT);
        summaryLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        newBestLabel = style.pixelLabel("NEW BEST", 8, UiStyle.SELECTED);
        reasonLabel = style.label(" ", UiStyle.ERROR);
        tipLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        tipLabel.setWrap(true);
        tipLabel.setAlignment(Align.center);

        // Three big stars in a row.
        bigStars = new StarIcon[MissionReport.STAR_COUNT];
        starRow = new Table();
        for (int i = 0; i < bigStars.length; i++) {
            bigStars[i] = new StarIcon();
            starRow.add(icon(bigStars[i], BIG_STAR)).size(BIG_STAR).padLeft(i == 0 ? 0 : 16);
        }

        // Left column: each goal with a small star.
        goalStars = new StarIcon[MissionReport.STAR_COUNT];
        goalLabels = new Label[MissionReport.STAR_COUNT];
        goalList = new Table();
        goalList.add(style.pixelLabel("STARS", 8, UiStyle.TEXT_SECONDARY)).colspan(2).left().padBottom(8).row();
        for (int i = 0; i < goalStars.length; i++) {
            goalStars[i] = new StarIcon();
            goalLabels[i] = style.label(" ", UiStyle.TEXT);
            goalList.add(icon(goalStars[i], SMALL_STAR)).size(SMALL_STAR).padRight(8).padTop(4);
            goalList.add(goalLabels[i]).left().expandX().padTop(4).row();
        }

        menu = createMenu();
        menu.setBackAction(game::showMenu);
    }

    public void showResult(boolean success, LevelData level, GameBoard board, double seconds,
                           MissionReport report, boolean newBest) {
        boolean finishedGame = success && !level.hasNextLevel();
        String complete = level.isTutorial() ? "TRAINING COMPLETE" : "MISSION COMPLETE";
        titleLabel.setText(finishedGame ? "ALL LEVELS CLEARED" : success ? complete : "MISSION FAILED");
        String name = level.isTutorial() ? "Training" : "Level " + level.getNumber();
        int total = (int) seconds;
        String time = String.format("%d:%02d", total / 60, total % 60);
        summaryLabel.setText(name + " in " + time + ": rescued " + board.getRescuedCount()
                + " of " + board.getSurvivors().size() + " survivors (needed " + level.getTarget() + ")");

        for (int i = 0; i < MissionReport.STAR_COUNT; i++) {
            bigStars[i].setEarned(false); // filled in one by one below
            goalStars[i].setEarned(report.isGoalMet(i));
            goalLabels[i].setText(report.getGoalName(i));
            goalLabels[i].setColor(report.isGoalMet(i) ? UiStyle.TEXT : UiStyle.TEXT_SECONDARY);
        }

        String reason = report.getFailReason();
        reasonLabel.setText(reason == null ? "" : reason);
        String tip = level.isTutorial() ? "You're ready for Level 1: the clock runs from the start there."
                : report.getTip();
        tipLabel.setText(tip == null ? "A perfect rescue!" : tip);

        // The first option is what the player most likely wants next.
        menu.clearOptions();
        if (success && level.hasNextLevel()) {
            menu.addOption(level.isTutorial() ? "START LEVEL 1" : "NEXT LEVEL",
                    () -> game.startLevel(level.getNumber() + 1));
        }
        menu.addOption(success ? "PLAY AGAIN" : "TRY AGAIN", () -> game.startLevel(level.getNumber()));
        menu.addOption("LEVEL SELECT", game::showLevelSelect);
        menu.addOption("MAIN MENU", game::showMenu);

        layOut(level.isTutorial(), reason != null, newBest && report.getStars() > 0);
        revealStars(level.isTutorial() ? 0 : report.getStars());
    }

    /** Rebuilt for each result, so the parts that don't apply (stars in training) take no space. */
    private void layOut(boolean tutorial, boolean failed, boolean newBest) {
        root.clearChildren();
        root.add(titleLabel).row();
        root.add(summaryLabel).padTop(12).row();
        if (!tutorial) {
            root.add(starRow).padTop(24).row();
            root.add(newBest ? newBestLabel : style.label(" ", UiStyle.TEXT)).padTop(8).row();
        }
        if (!tutorial) {
            root.add(goalList).width(COLUMN_WIDTH).padTop(16).row();
        }
        if (failed) {
            root.add(reasonLabel).padTop(24).row();
        }
        root.add(tipLabel).width(640).padTop(failed ? 8 : 24).row();
        root.add(menu).padTop(24).row();
        root.add(hint("Arrow keys to choose  ·  Enter to select")).padTop(16).row();
    }

    /** Fills the earned stars one at a time, with a ding each. */
    private void revealStars(int stars) {
        root.clearActions();
        SequenceAction sequence = Actions.sequence(Actions.delay(FIRST_STAR_DELAY));
        for (int i = 0; i < stars; i++) {
            StarIcon star = bigStars[i];
            sequence.addAction(Actions.run(() -> {
                star.setEarned(true);
                game.playSound("star");
            }));
            sequence.addAction(Actions.delay(STAR_GAP_SECONDS));
        }
        root.addAction(sequence);
    }
}
