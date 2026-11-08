package rescuegrid.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * The main menu: title, Start, How to Play, Quit. If a level file is broken,
 * the error is shown here instead of crashing the game.
 */
public class MenuPanel extends BaseScreen {

    private final Label errorLabel;

    public MenuPanel(GameFrame game) {
        super(game);
        root.pad(96);
        root.defaults().left();

        Label intro = style.label("An earthquake has collapsed the building. Find the trapped survivors "
                + "and carry them out before fire and aftershocks close the way.",
                14, false, UiStyle.TEXT_SECONDARY);
        intro.setWrap(true);

        TextButton start = style.primaryButton("Start Level 1");
        onPress(start, () -> game.startLevel(1));
        TextButton howToPlay = style.secondaryButton("How to Play");
        onPress(howToPlay, game::showHowToPlay);
        TextButton quit = style.secondaryButton("Quit");
        onPress(quit, () -> Gdx.app.exit());

        errorLabel = style.label("", 14, false, UiStyle.ERROR);
        errorLabel.setWrap(true);

        root.add(style.label("Rescue Grid", 32, true, UiStyle.TEXT)).row();
        root.add(intro).width(480).padTop(8).row();
        root.add(start).size(UiStyle.BUTTON_WIDTH, UiStyle.BUTTON_HEIGHT).padTop(32).row();
        root.add(howToPlay).size(UiStyle.BUTTON_WIDTH, UiStyle.BUTTON_HEIGHT).padTop(12).row();
        root.add(quit).size(UiStyle.BUTTON_WIDTH, UiStyle.BUTTON_HEIGHT).padTop(12).row();
        root.add(errorLabel).width(480).padTop(24).row();
    }

    @Override
    public void show() {
        super.show();
        errorLabel.setText("");
    }

    /** Shown under the buttons, e.g. when a level file is missing or broken. */
    public void showError(String message) {
        errorLabel.setText(message);
    }
}
