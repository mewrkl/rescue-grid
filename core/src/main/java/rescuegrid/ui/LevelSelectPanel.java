package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import rescuegrid.logic.LevelData;
import rescuegrid.logic.MissionReport;
import rescuegrid.logic.Progress;

/**
 * Pick any level that is open, with its best stars beside it. Training and
 * Level 1 are always open; finishing a level opens the next (see Progress).
 */
public class LevelSelectPanel extends BaseScreen {

    private static final float STAR_SIZE = 18;
    private static final float ROW_HEIGHT = 40; // the same as MenuList's rows, so the stars line up

    private final Label[] options;
    private final StarIcon[][] stars;
    private final Label messageLabel;

    public LevelSelectPanel(GameFrame game) {
        super(game);
        int levels = LevelData.LEVEL_COUNT + 1; // training is level 0
        options = new Label[levels];
        stars = new StarIcon[levels][MissionReport.STAR_COUNT];

        MenuList menu = createMenu();
        menu.setBackAction(game::showMenu);
        Table starColumn = new Table();
        for (int level = 0; level < levels; level++) {
            int number = level;
            options[level] = menu.addOption(" ", () -> choose(number));
            Table row = new Table();
            for (int i = 0; i < MissionReport.STAR_COUNT; i++) {
                stars[level][i] = new StarIcon();
                if (level != LevelData.TUTORIAL) { // training has no stars
                    row.add(icon(stars[level][i], STAR_SIZE)).size(STAR_SIZE).padLeft(i == 0 ? 0 : 4);
                }
            }
            starColumn.add(row).height(ROW_HEIGHT).left().row();
        }
        menu.addOption("BACK", game::showMenu);
        starColumn.add().height(ROW_HEIGHT).row(); // beside BACK

        messageLabel = style.label(" ", UiStyle.TEXT_SECONDARY);
        messageLabel.setAlignment(Align.center);

        Table list = new Table();
        list.add(menu);
        list.add(starColumn).top().padLeft(24);

        root.add(style.pixelLabel("LEVEL SELECT", 32, UiStyle.TEXT)).row();
        root.add(list).padTop(32).row();
        root.add(messageLabel).width(640).padTop(24).row();
        root.add(hint("Arrow keys to choose  ·  Enter to play  ·  Esc to go back")).padTop(32).row();
    }

    /** The texts and stars are refreshed each time the screen is shown, from the saved progress. */
    @Override
    public void show() {
        super.show();
        Progress progress = game.getProgress();
        for (int level = 0; level < options.length; level++) {
            String name = level == LevelData.TUTORIAL ? "TRAINING" : "LEVEL " + level;
            options[level].setText(progress.isUnlocked(level) ? name : name + " - LOCKED");
            for (int i = 0; i < MissionReport.STAR_COUNT; i++) {
                stars[level][i].setEarned(i < progress.getBestStars(level));
            }
        }
        messageLabel.setText("Finish a level to open the next one.");
        messageLabel.setColor(UiStyle.TEXT_SECONDARY);
    }

    private void choose(int level) {
        if (!game.getProgress().isUnlocked(level)) {
            game.playSound("denied");
            messageLabel.setText("Finish Level " + (level - 1) + " first to open Level " + level + ".");
            messageLabel.setColor(UiStyle.WARNING);
            return;
        }
        game.startLevel(level);
    }
}
