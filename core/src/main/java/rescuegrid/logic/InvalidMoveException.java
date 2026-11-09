package rescuegrid.logic;

/**
 * Thrown when an action isn't allowed right now, like using a special that is
 * still recharging. GamePanel catches it and shows the message.
 */
public class InvalidMoveException extends Exception {

    public InvalidMoveException(String message) {
        super(message);
    }
}
