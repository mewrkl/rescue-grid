package rescuegrid.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * A game-style menu: a centred column of options, one of which is always
 * selected. The selected option is bigger, in the highlight colour, with an
 * arrow on each side:
 *
 *              >  START  <
 *               HOW TO PLAY
 *                  QUIT
 *
 * Keyboard: Up/Down (or W/S) moves, Enter or Space chooses, Esc goes back.
 * Mouse: hovering selects, clicking chooses.
 *
 * Extends Scene2D's Table: each option is one row.
 */
public class MenuList extends Table {

    private static final int OPTION_SIZE = 16;   // pixel font sizes (multiples of 8)
    private static final int SELECTED_SIZE = 24;
    private static final float ROW_HEIGHT = 40;
    private static final float ARROW_GAP = 16;

    private final UiStyle style;
    private final Runnable clickSound;
    private final ArrayList<Label> labels;
    private final ArrayList<Label[]> arrows;
    private final ArrayList<Runnable> actions;
    private Runnable backAction;
    private int selected;

    public MenuList(UiStyle style, Runnable clickSound) {
        this.style = style;
        this.clickSound = clickSound;
        this.labels = new ArrayList<>();
        this.arrows = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.backAction = null;
        this.selected = 0;
    }

    /** Adds an option at the bottom of the list. */
    public void addOption(String text, Runnable action) {
        int index = labels.size();
        Label label = style.pixelLabel(text, OPTION_SIZE, UiStyle.TEXT_SECONDARY);
        label.setAlignment(Align.center);
        Label left = style.pixelLabel(">", OPTION_SIZE, UiStyle.SELECTED);
        Label right = style.pixelLabel("<", OPTION_SIZE, UiStyle.SELECTED);
        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) { // -1 means "the mouse", not a touch
                    select(index);
                    style.handCursor();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    style.resetCursor();
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                choose(index);
            }
        });
        labels.add(label);
        arrows.add(new Label[] {left, right});
        actions.add(action);

        // Each option is its own little row, so the arrows sit right beside its text.
        // Hidden arrows still take up their space, so the text stays centred.
        Table row = new Table();
        row.add(left).padRight(ARROW_GAP);
        row.add(label);
        row.add(right).padLeft(ARROW_GAP);
        add(row).height(ROW_HEIGHT).row();
        select(selected);
    }

    /** Removes every option (used when the result screen rebuilds its menu). */
    public void clearOptions() {
        clearChildren();
        labels.clear();
        arrows.clear();
        actions.clear();
        selected = 0;
    }

    /** What Esc does, e.g. go back to the main menu. */
    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    /** Selects the first option again (called whenever the screen is shown). */
    public void reset() {
        select(0);
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < labels.size(); i++) {
            boolean isSelected = i == selected;
            labels.get(i).setStyle(style.pixelStyle(isSelected ? SELECTED_SIZE : OPTION_SIZE));
            labels.get(i).setColor(isSelected ? UiStyle.SELECTED : UiStyle.TEXT_SECONDARY);
            arrows.get(i)[0].setVisible(isSelected);
            arrows.get(i)[1].setVisible(isSelected);
        }
    }

    private void choose(int index) {
        clickSound.run();
        actions.get(index).run();
    }

    /** The keyboard controls for this menu; BaseScreen plugs this into libGDX's input. */
    public InputProcessor keyboard() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (labels.isEmpty()) {
                    return false;
                }
                if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                    select((selected + 1) % labels.size());
                    return true;
                }
                if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                    select((selected - 1 + labels.size()) % labels.size());
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER
                        || keycode == Input.Keys.SPACE) {
                    choose(selected);
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE && backAction != null) {
                    clickSound.run();
                    backAction.run();
                    return true;
                }
                return false;
            }
        };
    }
}
