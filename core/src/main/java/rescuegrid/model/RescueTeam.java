package rescuegrid.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rescuegrid.logic.InvalidMoveException;
import rescuegrid.model.rescuers.Rescuer;

/**
 * The three roles of the rescue team. Only one of them is on the map at a time
 * (the active one); switching role takes a short "changing gear" pause, and the
 * new role takes over the old one's place and whoever they were helping.
 * Each role keeps its own health and cooldown.
 */
public class RescueTeam {

    public static final double GEAR_CHANGE_SECONDS = 1.0;

    private final ArrayList<Rescuer> members;
    private int activeIndex;
    private double gearChangeLeft; // seconds until the new role can move and act

    public RescueTeam() {
        this.members = new ArrayList<>();
        this.activeIndex = 0;
        this.gearChangeLeft = 0;
    }

    /** Adds a role. The first one added starts on the map; the others wait at its spot. */
    public void add(Rescuer rescuer) {
        if (!members.isEmpty()) {
            rescuer.moveTo(getActive().getRow(), getActive().getCol());
        }
        members.add(rescuer);
    }

    /** Called every frame: cooldowns recharge for every role, even the ones off the map. */
    public void tick(double seconds) {
        for (Rescuer member : members) {
            member.tick(seconds);
        }
        gearChangeLeft = Math.max(0, gearChangeLeft - seconds);
    }

    /**
     * Switches to the role at this index (0 = Medic, 1 = Engineer, 2 = Dog Handler).
     * Throws if that role is already on the map or is down.
     */
    public void switchTo(int index) throws InvalidMoveException {
        Rescuer chosen = members.get(index);
        if (index == activeIndex) {
            throw new InvalidMoveException("You are already the " + chosen.getName());
        }
        if (chosen.isDowned()) {
            throw new InvalidMoveException("The " + chosen.getName() + " is down");
        }
        handOver(index);
    }

    /** Switches to the next role that is still standing (Tab). */
    public void switchToNext() throws InvalidMoveException {
        for (int step = 1; step < members.size(); step++) {
            int index = (activeIndex + step) % members.size();
            if (!members.get(index).isDowned()) {
                handOver(index);
                return;
            }
        }
        throw new InvalidMoveException("Nobody else can take over");
    }

    /**
     * If the role on the map was downed, the next standing role takes over at once.
     * Returns true if that happened.
     */
    public boolean replaceDowned() {
        if (!getActive().isDowned()) {
            return false;
        }
        for (int step = 1; step < members.size(); step++) {
            int index = (activeIndex + step) % members.size();
            if (!members.get(index).isDowned()) {
                handOver(index);
                return true;
            }
        }
        getActive().dropSurvivor(); // nobody left to take over
        return false;
    }

    private void handOver(int index) {
        Rescuer previous = getActive();
        activeIndex = index;
        getActive().takeOver(previous);
        gearChangeLeft = GEAR_CHANGE_SECONDS;
    }

    /** The role on the map. */
    public Rescuer getActive() {
        return members.get(activeIndex);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    /** Read-only view of all three roles, in key order. */
    public List<Rescuer> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** True while the new role is still putting on their gear (can't move or act). */
    public boolean isChangingGear() {
        return gearChangeLeft > 0;
    }

    /** 0 just after a switch, 1 when the gear change is done. */
    public double getGearChangeProgress() {
        return 1 - gearChangeLeft / GEAR_CHANGE_SECONDS;
    }

    /** True when every role is down: the mission fails. */
    public boolean isWipedOut() {
        for (Rescuer member : members) {
            if (!member.isDowned()) {
                return false;
            }
        }
        return true;
    }
}
