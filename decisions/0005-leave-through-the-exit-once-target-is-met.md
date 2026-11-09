# 0005: End a level by leaving through the exit once the target is met

- Status: Accepted
- Date: 2026-11-09
- Source: first playtest by the team; RESCUE_GRID_PLAN.md section 4.6. Amends the earlier end rule "the mission ends when no survivors are left in the building, or all rescuers are downed".

## Context
Each level has more survivors than its target (Level 1: 3 survivors, target 2). Under the old end rule the mission only ended when every survivor had been rescued or had run out of time. In the first playtest, the player rescued the 2 survivors that were needed, stood the rescuers on the exit, and nothing happened. The level kept running until the third survivor's timer ran out, up to a minute later. The HUD showed "Rescued 2 / 2", so the player could not tell why the level had not ended.

## Options considered
- **End as soon as the target is met:** the simplest rule, with nothing to learn. But the extra survivors become pointless, and the "everyone rescued" score bonus can never be earned.
- **Leave through the exit:** once the target is met, the active rescuer steps onto the exit with empty hands to end the level. The player chooses between leaving now and going back for the rest. This is the familiar "extraction" pattern from action games.
- **Keep the old rule and only explain it better:** no code changes to the rules. But it still means waiting out a survivor's timer, which feels like the game is stuck.

## Decision
Leave through the exit. `TurnManager.evacuate()` ends the mission, and it is refused (returns false) until the target is met. `GamePanel` calls it when the active rescuer steps onto the exit with empty hands. Before the target is met, the HUD says how many more are needed.

## Consequences
The level ends when the player decides, the bonus keeps its purpose, and the rule is covered by a unit test. It adds one rule the player has to learn, so the HUD now has a SURVIVORS section and a "Goal met! Go to the EXIT" line. Right after a delivery the rescuer is already standing on the exit, so leaving takes one step off and one step back on. That costs a little smoothness. **Given up:** the zero-explanation simplicity of ending automatically at the target. It also no longer takes the whole team to the exit; only the active rescuer has to be there, which is less realistic.

## Generalizes to
**Optional objectives need an explicit "I'm done" action.** When a goal has a required minimum and an optional surplus, let the player end the run on purpose (extraction, "leave level", "submit"), and show the minimum on screen. An automatic end hides the surplus or makes the player wait. This does NOT hold when there is no optional surplus (end automatically), or when ending early should be impossible by design (survival timers, boss fights).
