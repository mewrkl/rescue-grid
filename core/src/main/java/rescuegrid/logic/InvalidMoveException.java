package rescuegrid.logic;

/**
 * Thrown when the player tries an action that isn't allowed right now, such as
 * using a special that is still recharging, or clearing a tile that isn't rubble.
 *
 * It is a checked exception (it extends Exception), so Java forces the caller to
 * handle it. GamePanel catches it and shows the message in the HUD.
 */
public class InvalidMoveException extends Exception {

    public InvalidMoveException(String message) {
        super(message);
    }
}
