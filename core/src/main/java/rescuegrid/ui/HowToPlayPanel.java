package rescuegrid.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Explains the rules and controls: four short sections, each a bold heading
 * with a paragraph under it.
 */
public class HowToPlayPanel extends BaseScreen {

    private static final float TEXT_WIDTH = 640;

    private static final String[][] SECTIONS = {
        {"Goal", "Explore the dark building, find the trapped survivors and carry them to the green "
                + "EXIT. Each level needs a minimum number rescued. Survivors grow weaker over time: "
                + "watch the bar under each one."},
        {"Controls", "WASD or the arrow keys to walk (hold to keep walking). 1, 2, 3 or Tab to switch "
                + "rescuer. Space uses the special action on the tile you face (dashed outline). "
                + "Esc pauses."},
        {"Your team", "The Medic treats the survivor she carries or faces. The Engineer clears rubble. "
                + "The Dog Handler's dog sniffs out survivors nearby, even through rubble, and marks "
                + "them on the minimap."},
        {"Danger", "Every few seconds fire spreads and burns anyone standing next to it. Fires "
                + "eventually burn out. Aftershocks hurt everyone and turn cracked floor into rubble, "
                + "so the way you came in may not be there any more. Rescuers you are not controlling "
                + "stay where they are and can still get hurt."},
    };

    public HowToPlayPanel(GameFrame game) {
        super(game);
        root.pad(64, 96, 64, 96);
        root.defaults().left();

        root.add(style.label("How to Play", 24, true, UiStyle.TEXT)).padBottom(8).row();
        for (String[] section : SECTIONS) {
            Label body = style.label(section[1], 14, false, UiStyle.TEXT_SECONDARY);
            body.setWrap(true);
            root.add(style.label(section[0], 14, true, UiStyle.TEXT)).padTop(16).row();
            root.add(body).width(TEXT_WIDTH).padTop(4).row();
        }

        TextButton back = style.secondaryButton("Back to Menu");
        onPress(back, game::showMenu);
        buttonKeys.setBackAction(game::showMenu);
        root.add(back).size(UiStyle.BUTTON_WIDTH, UiStyle.BUTTON_HEIGHT).padTop(32).row();
    }
}
