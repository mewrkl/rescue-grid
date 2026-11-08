package rescuegrid.logic;

import java.util.Random;

import rescuegrid.model.GameBoard;

/**
 * Makes the building dangerous: fire damage, fire spreading and burning out,
 * and aftershocks that turn floor into rubble.
 *
 * Uses one Random with a fixed seed per level, so the same level plays out the
 * same way each time. That makes bugs reproducible.
 *
 * BEGINNER WARNING: when changing tiles, first COLLECT the changes in a list,
 * then APPLY them. If you change tiles while still looping over the grid, a
 * tile that just caught fire could spread again in the same tick.
 */
public class HazardManager {

    public static final double SPREAD_CHANCE = 0.25;  // tune in testing
    public static final int RANDOM_COLLAPSES = 3;     // tune in testing

    private final Random random;

    public HazardManager(long seed) {
        this.random = new Random(seed);
    }

    /** Step 1 of the tick: every active rescuer next to fire loses 1 health. */
    public void applyFireDamage(GameBoard board) {
        // TODO (Week 3, Member 3): for each rescuer that isn't downed, look at the 4
        //  neighbouring tiles. If any of them isBurning(), the rescuer takes 1 damage.
        //  (isBurning() is a Tile hook, so no instanceof is needed.)
    }

    /** Step 2: each fire tries to spread to each flammable neighbour (SPREAD_CHANCE each). */
    public void spreadFire(GameBoard board) {
        // TODO (Week 3, Member 3): collect positions first, then replaceTile(..., new Fire(...)).
        //  Use random.nextDouble() < SPREAD_CHANCE. Skip tiles with a rescuer on them.
        //  A waiting survivor on a tile that catches fire is lost (markLost()).
    }

    /** Step 3: call onWorldTick on every tile. Only Fire reacts (it burns out). */
    public void burnOutFires(GameBoard board) {
        // TODO (Week 3, Member 3): collect every tile into a list first, then call
        //  tile.onWorldTick(board) on each. Polymorphism does the rest.
    }

    /** Step 5: the aftershock (plan section 4.5). */
    public void triggerAftershock(GameBoard board) {
        // TODO (Week 3, Member 3):
        //  1. Call onAftershock(board) on every tile (CrackedFloor turns into Rubble).
        //  2. Up to RANDOM_COLLAPSES random tiles where canCollapse() is true become Rubble.
        //     Skip tiles with a rescuer or survivor on them, or next to the Exit.
        //  3. Every active rescuer takes 1 damage.
    }
}
