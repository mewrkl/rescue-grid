package rescuegrid.logic;

/**
 * Something that happened during a world tick, such as a rescuer getting
 * burned. The logic records events; GamePanel turns them into sounds and messages.
 */
public class GameEvent {

    /** The kinds of things that can happen. */
    public enum Type {
        RESCUER_HURT,
        RESCUER_DOWNED,
        ROLE_TAKEOVER,
        FIRE_SPREAD,
        SURVIVOR_LOST,
        BACKUP_ARRIVED,
        HELP_CALL,
        AFTERSHOCK_WARNING,
        AFTERSHOCK,
        SURVIVOR_FADING  // a survivor you have found is running out of time
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
