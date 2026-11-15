package rescuegrid.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import rescuegrid.graphics.Painter;
import rescuegrid.logic.Objective;

/**
 * The objective checklist in the top-left corner of the map, like a quest
 * tracker: three steps with a box each (filled = done), and the next thing to do.
 */
class ObjectivePanel extends Table {

    private static final int LINES = 3;
    private static final float WIDTH = 248;
    private static final Color OPEN_BOX = Painter.rgba(255, 255, 255, 50);

    private final Drawable doneBox;
    private final Drawable openBox;
    private final Image[] boxes;
    private final Label[] lines;
    private final Label nextStep;

    ObjectivePanel(UiStyle style) {
        setBackground(style.smallCard());
        defaults().left();
        doneBox = style.solid(UiStyle.TEXT_SECONDARY);
        openBox = style.solid(OPEN_BOX);

        add(style.pixelLabel("OBJECTIVE", 8, UiStyle.TEXT_SECONDARY)).padBottom(8).row();
        boxes = new Image[LINES];
        lines = new Label[LINES];
        for (int i = 0; i < LINES; i++) {
            boxes[i] = new Image(openBox);
            lines[i] = style.label(" ", UiStyle.TEXT);
            Table row = new Table();
            row.add(boxes[i]).size(8);
            row.add(lines[i]).padLeft(8);
            add(row).padTop(i == 0 ? 0 : 4).row();
        }
        nextStep = style.label(" ", UiStyle.SELECTED);
        nextStep.setWrap(true);
        add(nextStep).width(WIDTH).padTop(8).row();
    }

    /** Done steps get a filled box and dim text; the next step is in the accent colour. */
    void refresh(List<Objective> objectives, String next) {
        for (int i = 0; i < LINES && i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            boxes[i].setDrawable(objective.isDone() ? doneBox : openBox);
            lines[i].setText(objective.getText());
            lines[i].setColor(objective.isDone() ? UiStyle.TEXT_SECONDARY : UiStyle.TEXT);
        }
        nextStep.setText("> " + next);
    }
}
