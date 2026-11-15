package rescuegrid.logic;

/** One line of the objective checklist: what to do, and whether it is done. */
public class Objective {

    private final String text;
    private final boolean done;

    public Objective(String text, boolean done) {
        this.text = text;
        this.done = done;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }
}
