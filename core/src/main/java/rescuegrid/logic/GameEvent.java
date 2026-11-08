package rescuegrid.logic;

/**
 * Something that happened during a world tick that the player should hear or
 * see: a rescuer got burned, an aftershock hit, a survivor was lost...
 *
 * The logic classes only RECORD events. GamePanel reads them afterwards and
 * decides which sound to play and what message to show. This keeps sound and
 * screen code out of the game rules.
 */
public class GameEvent {

    /** The kinds of things that can happen. */
    public enum Type {
        RESCUER_HURT,
        RESCUER_DOWNED,
        FIRE_SPREAD,
        SURVIVOR_LOST,
        AFTERSHOCK_WARNING,
        AFTERSHOCK
    }

    private final Type type;
    private final String message;

    public GameEvent(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    /** Text for the HUD, or "" if this event needs no message. */
    public String getMessage() {
        return message;
    }
}
