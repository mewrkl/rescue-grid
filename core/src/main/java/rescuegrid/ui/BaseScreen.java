package rescuegrid.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import rescuegrid.graphics.Painter;
import rescuegrid.model.Drawable;

/**
 * What every screen has in common: a Scene2D Stage (the UI "scene" that holds
 * labels and menus), a root Table that lays them out, an optional MenuList the
 * keyboard controls, and icon slots where game art is drawn.
 *
 * Abstract: the game only ever creates a MenuPanel, HowToPlayPanel, GamePanel
 * or ResultPanel. Each subclass builds its own layout in its constructor.
 *
 * Each frame: drawBackground() (empty here, the title screen overrides it),
 * then the Scene2D UI, then the icons.
 *
 * Extends ScreenAdapter, libGDX's empty Screen, so we only override what we use.
 */
abstract class BaseScreen extends ScreenAdapter {

    protected final GameFrame game;
    protected final UiStyle style;
    protected final Stage stage;
    protected final Table root;
    private final ArrayList<IconSlot> icons;
    private final OrthographicCamera iconCamera;
    private MenuList menu;

    protected BaseScreen(GameFrame game) {
        this.game = game;
        this.style = game.getStyle();
        this.stage = new Stage(new ScreenViewport());
        this.root = new Table();
        this.root.setFillParent(true);
        this.stage.addActor(root);
        this.icons = new ArrayList<>();
        this.iconCamera = new OrthographicCamera();
        this.menu = null;
    }

    /** Creates this screen's menu; its options are added by the subclass. */
    protected MenuList createMenu() {
        menu = new MenuList(style, () -> game.playSound("click"));
        return menu;
    }

    /** A small grey line telling the player which keys work on this screen. */
    protected Label hint(String text) {
        return style.label(text, UiStyle.TEXT_SECONDARY);
    }

    /** A space in the layout where a game object will be drawn at the given size. */
    protected IconSlot icon(Drawable drawable, float size) {
        IconSlot slot = new IconSlot(drawable, size);
        icons.add(slot);
        return slot;
    }

    /** Where keyboard and mouse input go while this screen is shown. */
    protected InputProcessor createInput() {
        if (menu == null) {
            return stage;
        }
        return new InputMultiplexer(menu.keyboard(), stage);
    }

    @Override
    public void show() {
        if (menu != null) {
            menu.reset();
        }
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
        stage.getViewport().apply(true);
        drawBackground();
        drawStage(delta);
        drawIcons();
    }

    /** Art behind the UI. Nothing by default. */
    protected void drawBackground() {
        // Most screens have a plain background.
    }

    protected void drawStage(float delta) {
        stage.getViewport().apply(true);
        stage.act(delta);
        stage.draw();
    }

    /** Draws each icon where Scene2D placed its slot, with the same Painter the game uses. */
    private void drawIcons() {
        if (icons.isEmpty()) {
            return;
        }
        float height = stage.getViewport().getWorldHeight();
        Painter painter = game.getPainter();
        painter.begin(screenProjection().combined);
        Vector2 corner = new Vector2();
        for (IconSlot slot : icons) {
            if (slot.getStage() == null || !slot.ascendantsVisible() || !slot.isVisible()) {
                continue;
            }
            corner.set(0, 0);
            slot.localToStageCoordinates(corner);
            // Scene2D measures y from the bottom; our Painter measures from the top.
            float top = height - corner.y - slot.getHeight();
            slot.getDrawable().draw(painter, corner.x, top, slot.getWidth(), game.getAssets());
        }
        painter.end();
    }

    /** A y-down camera covering the whole window, for drawing game art on menu screens. */
    protected OrthographicCamera screenProjection() {
        iconCamera.setToOrtho(true, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        return iconCamera;
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
