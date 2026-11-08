package rescuegrid.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * What every screen has in common: a Scene2D Stage (the UI "scene" that holds
 * labels and buttons), a root Table that lays them out, and keyboard control
 * of the buttons.
 *
 * Abstract: the game only ever creates a MenuPanel, HowToPlayPanel, GamePanel
 * or ResultPanel. Each subclass builds its own layout in its constructor.
 *
 * Extends ScreenAdapter, libGDX's empty Screen, so we only override what we use.
 */
abstract class BaseScreen extends ScreenAdapter {

    protected final GameFrame game;
    protected final UiStyle style;
    protected final Stage stage;
    protected final Table root;
    protected final ButtonKeys buttonKeys;

    protected BaseScreen(GameFrame game) {
        this.game = game;
        this.style = game.getStyle();
        this.stage = new Stage(new ScreenViewport());
        this.root = new Table();
        this.root.setFillParent(true);
        this.root.top().left();
        this.stage.addActor(root);
        this.buttonKeys = new ButtonKeys(stage);
    }

    /** Runs action when the button is clicked, or chosen with the keyboard. Plays the click sound. */
    protected void onPress(TextButton button, Runnable action) {
        Runnable withSound = () -> {
            game.playSound("click");
            action.run();
        };
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                withSound.run();
            }
        });
        buttonKeys.add(button, withSound);
    }

    /** Where keyboard and mouse input go while this screen is shown. */
    protected InputProcessor createInput() {
        return new InputMultiplexer(buttonKeys, stage);
    }

    @Override
    public void show() {
        buttonKeys.clearFocus();
        Gdx.input.setInputProcessor(createInput());
    }

    @Override
    public void hide() {
        style.resetCursor();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(UiStyle.BACKGROUND);
        drawStage(delta);
    }

    protected void drawStage(float delta) {
        stage.getViewport().apply(true);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
