package rescuegrid.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Explains the rules and controls.
 * TODO (Week 4, Member 3): add pictures of each tile and rescuer once the sprites exist.
 */
public class HowToPlayPanel extends JPanel {

    private static final String RULES = "<html><div style='width:560px'>"
            + "<p><b>Goal.</b> Carry survivors to the green Exit. Rescue at least the level's "
            + "target before they run out of time.</p><br>"
            + "<p><b>Controls.</b> WASD or arrow keys to walk. 1, 2, 3 or Tab to switch rescuer. "
            + "Space for the special action on the tile you are facing. Esc to pause.</p><br>"
            + "<p><b>Your team.</b> The Medic treats survivors. The Engineer clears rubble. "
            + "The Dog Handler's dog sniffs out survivors hidden in the dark.</p><br>"
            + "<p><b>Danger.</b> Every few seconds fire spreads and hurts anyone standing next to it. "
            + "Aftershocks turn cracked floor into rubble, so the way you came in may not be "
            + "there any more. Rescuers you are not controlling can still get hurt.</p>"
            + "</div></html>";

    public HowToPlayPanel(GameFrame frame) {
        setBackground(UiStyle.BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(96, 96, 96, 96));

        JButton back = UiStyle.secondaryButton("Back to Menu");
        back.addActionListener(e -> frame.showScreen(GameFrame.MENU));

        add(UiStyle.label("How to Play", 24, true, UiStyle.TEXT));
        add(Box.createVerticalStrut(24));
        add(UiStyle.label(RULES, 14, false, UiStyle.TEXT_SECONDARY));
        add(Box.createVerticalStrut(32));
        add(back);
        add(Box.createVerticalGlue());
    }
}
