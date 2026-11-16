# 0023: Training merges related steps (17 to 11)

- Status: Accepted
- Date: 2026-11-16
- Source: my own critique of the game ("17 strict steps is long for players who know action games"). The user chose "Shorter training" in chat.

## Context
The training (decisions 0011, 0013, 0015) had 17 gated steps. Each finished step paused for a "DONE!" and a new card, even when the next step was the obvious continuation of the same idea:
- hear the call, walk into the survivor, lead them out;
- clear the rubble, lift the debris, carry them;
- treat the red survivor, call backup;
- the aftershock, then carry them out.

Walking also asked for all four directions. The three ability tests in the first room (decision 0015) were wanted by the user and had to stay.

## Options considered
- **Merge steps that teach one idea:** each merged step's card changes as the player goes ("parts"). The same map, gates and checks, but fewer "done, next" stops.
- **Cut lessons:** for example, drop the trapped survivor or the aftershock. Shorter still, but players then meet those rules untaught in Level 1.
- **Keep 17 steps and let players skip any step:** keeps the detail for those who want it, but skipping into a gated level needs extra rules.

## Decision
Merge steps:
- **Walking:** two directions, not four.
- **Green survivor:** one step (call, follow, lead).
- **Yellow survivor:** one step (rubble, trapped, carry).
- **Red survivor:** one step (treat, then Q); Q only unlocks once they are stabilised.
- **The end:** the aftershock and bring-out are one step.

That is 11 steps in the same 4 chapters. `Tutorial` works out each step's `part` every frame, and the card text and marker follow it. The three ability tests are unchanged.

## Consequences
Fewer stops and fewer "DONE!" flashes, with the same lessons and the same map. The progress dots now count 11. **Given up:**
- **Exact progress:** "step 5 of 11" now covers three actions, so the dots move less often.
- **Tests:** the training test no longer checks a separate step for each action; it checks the card's parts instead.
- **Time:** the walking is unchanged, so the real saving is in stops and reading, not distance. It is shorter, but not half as long in minutes.

## Generalizes to
**Tutorial granularity: one step per idea, not per action.** Gate a tutorial at the level of a concept, and let the instruction update inside it as the player does each action. Keep separate steps only where a new rule appears. This does not hold when each action is hard on its own (precise inputs, combos). There, a checkpoint per action keeps a struggling player from being sent back too far.
