package rescuegrid.logic;

import rescuegrid.model.GameBoard;
import rescuegrid.model.Survivor;

/**
 * The training level's step-by-step prompts. Each step teaches one thing and
 * waits until the player has done it. Things done early still count (they are
 * remembered), so the player can never get stuck on a step.
 */
public class Tutorial {

    private static final String[] PROMPTS = {
        "Walk with W A S D or the arrow keys.",
        "Someone is calling for HELP. Walk towards the call.",
        "Green tag = minor injury. Walk onto them and they will follow you.",
        "Lead them back to the green EXIT door.",
        "Rubble blocks the door. Press 2 for the Engineer, face the rubble and press E.",
        "This survivor is trapped. As the Engineer, face them and press E to lift the debris.",
        "Yellow tag = serious injury. Walk onto them to carry them, then take them to the EXIT.",
        "Red tag = critical: they can't be moved. Press 1 for the Medic, face them, press E.",
        "Now press Q next to them to call backup. Backup carries them out, so you can move on.",
        "One more survivor is hidden in the dark. Press 3 for the Dog Handler, then E.",
        "Bring them out. Don't stand next to fire: it burns.",
        "Everyone is safe! Step onto the EXIT with empty hands to finish the training.",
    };
    private static final String AFTERSHOCK_TIP =
            "AFTERSHOCK coming! The orange tiles will collapse: step off them.";
    private static final int STEPS_TO_WALK = 3;

    private int step;
    private int stepsWalked;
    private boolean clearedRubble;
    private boolean treated;
    private boolean calledBackup;
    private boolean usedScent;

    public Tutorial() {
        this.step = 0;
        this.stepsWalked = 0;
        this.clearedRubble = false;
        this.treated = false;
        this.calledBackup = false;
        this.usedScent = false;
    }

    /** The rescuer took a step. */
    public void onStep() {
        stepsWalked++;
    }

    /** A special action worked; abilityName is the rescuer's getSpecialName(). */
    public void onAbility(String abilityName) {
        if (abilityName.equals("Clear Rubble")) {
            clearedRubble = true;
        } else if (abilityName.equals("Treat")) {
            treated = true;
        } else if (abilityName.equals("Scent")) {
            usedScent = true;
        }
    }

    public void onBackupCalled() {
        calledBackup = true;
    }

    /** Moves on past every step that is already done. Called every frame. */
    public void update(GameBoard board, TurnManager turns) {
        while (step < PROMPTS.length - 1 && isDone(step, board, turns)) {
            step++;
        }
    }

    private boolean isDone(int stepNumber, GameBoard board, TurnManager turns) {
        switch (stepNumber) {
            case 0:
                return stepsWalked >= STEPS_TO_WALK;
            case 1:
                return countRevealed(board) > 0;
            case 2:
                return board.getActiveRescuer().isLeading() || board.getRescuedCount() >= 1;
            case 3:
                return board.getRescuedCount() >= 1;
            case 4:
                return clearedRubble;
            case 5:
                return countTrapped(board) == 0;
            case 6:
                return board.getRescuedCount() >= 2;
            case 7:
                return treated;
            case 8:
                return calledBackup;
            case 9:
                return usedScent;
            case 10:
                return turns.isTargetReached();
            default:
                return false;
        }
    }

    private int countRevealed(GameBoard board) {
        int count = 0;
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isRevealed()) {
                count++;
            }
        }
        return count;
    }

    private int countTrapped(GameBoard board) {
        int count = 0;
        for (Survivor survivor : board.getSurvivors()) {
            if (survivor.isTrapped()) {
                count++;
            }
        }
        return count;
    }

    /** What to show right now. An aftershock warning interrupts the current step. */
    public String getPrompt(TurnManager turns) {
        if (turns.isAftershockComing()) {
            return AFTERSHOCK_TIP;
        }
        return PROMPTS[step];
    }

    /** 1-based, for "STEP 3 / 12". */
    public int getStepNumber() {
        return step + 1;
    }

    public int getStepCount() {
        return PROMPTS.length;
    }
}
