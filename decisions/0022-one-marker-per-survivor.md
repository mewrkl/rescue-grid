# 0022: One marker per survivor, and fewer repeated messages

- Status: Accepted
- Date: 2026-11-16
- Source: my own critique of how I had been adding features: each one added its own marker without counting what was already on screen. The user chose "Calm the map" in chat.

## Context
By decision 0021 a busy room could show, all at once:
- a "!" bubble over each found survivor;
- a red pulsing outline with "6 s" under anyone running out of time;
- a flare with a number by a red survivor waiting for backup;
- paw prints on every nearby tile;
- HELP arrows at the screen edge;
- a message at the bottom of the map, the next-step line and, in training, the training card.

Several of these said the same thing twice. For example, "Changing to the Dog Handler" appeared while the dust puff, the gear bar and the HUD's "(you)" already showed it. "A critical survivor has 10 s left!" appeared while that survivor was on screen with a red countdown.

## Options considered
- **One marker per survivor, and cut repeats:**
  - Each survivor shows at most one call to action.
  - A message only appears when nothing on screen already says it.
- **Leave it, but make markers smaller or fainter:** keeps every cue, but a crowd of faint markers is still a crowd.
- **A setting to hide hints:** lets experts turn things off, but the default stays busy (the user didn't pick this option).

## Decision
- **One marker per survivor:**
  - Running out of time, the red countdown replaces the "!" bubble (`Survivor.drawOnMap(..., showBubble)`).
  - With backup on its way, the flare replaces it (decision 0020).
- **Paw prints:** shown only while Scent is ready, not while it recharges.
- **No message for:**
  - a role change;
  - backup arriving (the team and "Saved!" show it);
  - a low-time warning about a survivor who is on screen. The beep still plays, and the message still appears when they are off screen.

## Consequences
At a glance each survivor carries one signal. Messages now mostly report things you can't see. **Given up:**
- **Redundancy:** a player who doesn't notice the red outline no longer also gets a message.
- **The scent range while it recharges:** paw prints vanish, so the range can't be planned until Scent is ready again.
- **More rules to remember:** "which marker wins" is now one more thing to keep consistent when adding new ones.

## Generalizes to
**A visual budget for markers: one signal per object, and messages only for what's off screen.** When many systems want to mark the same object, rank them and show only the most urgent. Report things in text only when the player can't already see them. This does not hold for accessibility needs, where a second channel (sound, text) on purpose doubles a visual signal for players who may miss it. Keep those doubles, but on a separate, switchable layer.
