package rescuegrid.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The main menu: title, Start, How to Play, Quit.
 */
public class MenuPanel extends JPanel {

    public MenuPanel(GameFrame frame) {
        setBackground(UiStyle.BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(96, 96, 96, 96));

        JButton start = UiStyle.primaryButton("Start Level 1");
        start.addActionListener(e -> frame.startLevel(1));

        JButton howToPlay = UiStyle.secondaryButton("How to Play");
        howToPlay.addActionListener(e -> frame.showScreen(GameFrame.HOW_TO_PLAY));

        JButton quit = UiStyle.secondaryButton("Quit");
        quit.addActionListener(e -> System.exit(0));

        add(UiStyle.label("Rescue Grid", 32, true, UiStyle.TEXT));
        add(Box.createVerticalStrut(8));
        add(UiStyle.label("<html><div style='width:420px'>An earthquake has collapsed the building. "
                + "Find the trapped survivors and carry them out before fire and aftershocks "
                + "close the way.</div></html>", 14, false, UiStyle.TEXT_SECONDARY));
        add(Box.createVerticalStrut(32));
        add(start);
        add(Box.createVerticalStrut(12));
        add(howToPlay);
        add(Box.createVerticalStrut(12));
        add(quit);
        add(Box.createVerticalGlue());
    }
}
