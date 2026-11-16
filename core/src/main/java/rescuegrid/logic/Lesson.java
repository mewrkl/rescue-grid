package rescuegrid.logic;

/**
 * One "stop and read" moment in the training (decision 0025): the game
 * freezes, the camera goes to the thing being explained, and a card says what
 * it is and what to do. The player presses Enter to carry on. Tutorial decides
 * when each lesson is due; GamePanel shows it.
 */
public class Lesson {

    /** The picture on the card. */
    public enum Picture { MEDIC, ENGINEER, DOG_HANDLER, MINOR, TRAPPED, CRITICAL, SERIOUS, FIRE, AFTERSHOCK, CLOCK }

    private final String title;
    private final String body;
    private final Picture picture;
    private final int[] focus;      // {row, col} the camera shows, or null to stay on the rescuer
    private final String label;     // the word over the focus while the card is up

    public Lesson(String title, String body, Picture picture, int[] focus, String label) {
        this.title = title;
        this.body = body;
        this.picture = picture;
        this.focus = focus;
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Picture getPicture() {
        return picture;
    }

    /** {row, col} for the camera to show, or null if the card is about the rescuer or the area around them. */
    public int[] getFocus() {
        return focus;
    }

    public String getLabel() {
        return label;
    }
}
