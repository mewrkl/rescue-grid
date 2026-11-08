package rescuegrid;

import javax.swing.SwingUtilities;

import rescuegrid.ui.GameFrame;

/**
 * Starting point of the program. Opens the game window.
 *
 * Swing is not thread-safe, so every window must be created on the Swing event
 * thread. SwingUtilities.invokeLater(...) runs our code on that thread.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
