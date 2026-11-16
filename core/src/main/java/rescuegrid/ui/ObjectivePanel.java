package rescuegrid.ui;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * One slim line in the top-left corner of the map: what to do right now
 * ("> Take them to the EXIT"). The full checklist lives in the side panel,
 * so this card covers as little of the map as possible.
 */
class ObjectivePanel extends Table {

    private static final float MAX_WIDTH = 520;

    private final Label nextStep;
    private final GlyphLayout layout;
    private boolean allowed;

    ObjectivePanel(UiStyle style) {
        setBackground(style.smallCard());
        nextStep = style.label(" ", UiStyle.SELECTED);
        nextStep.setWrap(true);
        layout = new GlyphLayout();
        allowed = true;
        add(nextStep).left();
    }

    /** False while the briefing is up: it says the same thing and they would overlap. */
    void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    /** Shows the next step, or hides the card when there is none (e.g. in training). */
    void refresh(String next) {
        setVisible(allowed && next != null);
        if (next == null) {
            return;
        }
        String text = "> " + next;
        if (!text.contentEquals(nextStep.getText())) {
            nextStep.setText(text);
            layout.setText(nextStep.getStyle().font, text);
            getCell(nextStep).width(Math.min(MAX_WIDTH, layout.width + 1)); // only as wide as its line
            invalidateHierarchy();
        }
    }
}
