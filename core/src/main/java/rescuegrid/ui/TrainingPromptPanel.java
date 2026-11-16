package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import rescuegrid.logic.LevelData;

/**
 * Asked once, the first time the player presses PLAY: "Play the training
 * first?". Either answer is remembered, so PLAY goes straight to Level 1 after
 * that. The training stays in Level Select for anyone who wants it later.
 */
public class TrainingPromptPanel extends BaseScreen {

    public TrainingPromptPanel(GameFrame game) {
        super(game);
        Label question = style.pixelLabel("PLAY THE TRAINING FIRST?", 24, UiStyle.TEXT);
        Label detail = style.label("A short, guided level that teaches each role and each kind of survivor. "
                + "You can always play it later from Level Select.", UiStyle.TEXT_SECONDARY);
        detail.setAlignment(Align.center);
        detail.setWrap(true);

        MenuList menu = createMenu();
        menu.addOption("YES (RECOMMENDED)", () -> game.startLevel(LevelData.TUTORIAL));
        menu.addOption("SKIP TO LEVEL 1", () -> {
            game.skipTraining();
            game.startLevel(1);
        });
        menu.setBackAction(game::showMenu);

        root.add(question).row();
        root.add(detail).width(560).padTop(16).row();
        root.add(menu).padTop(32).row();
        root.add(hint("Arrow keys to choose  ·  Enter to select  ·  Esc to go back")).padTop(24).row();
    }
}
