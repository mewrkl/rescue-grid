# 0013: Training points at one thing at a time and marks progress

- Status: Accepted
- Date: 2026-11-15
- Source: second hand playtest of training, a short look at how Super Mario Bros. 1-1, Portal, Nintendo's four-beat level structure and Into the Breach teach; the user chose each option in chat

## Context
The gated training (decision 0011) stopped players skipping ahead, but the playtest found it "hard to follow, with no strong indicators, and the flow feels flat":
- A door would open off screen ("a door has opened") with no idea where.
- The Dog Handler was never needed, because the hidden survivor could be seen just by walking down the hall.
- Fourteen cards in a row, with no sense of finishing anything.

The references agree on a few things:
- Teach one idea per space (Portal's chambers; Mario 1-1).
- Players don't look where they aren't prompted to; Valve noted players "rarely look up".
- Show an outcome before the player acts (Into the Breach).
- After introducing an ability, put it in a spot where it is actually needed (the "develop/twist" beats).

## Options considered
- **Door reveal:** a camera glide to the door, then a marker; or a marker and edge arrow only. The marker alone is cheaper and never takes control away, but the glide shows *where* in one look.
- **Dog Handler:** survivor behind rubble in one of three rooms (scent goes through rubble, sight doesn't); invisible to sight in training only; or a big dark area with a scent compass. Behind rubble uses existing rules and combines two roles. The others are simpler but either add a training-only rule or a new mechanic.
- **Flow:** any of a "DONE!" moment per step, chapters with title cards, one marker at a time, and progress dots.

## Decision
- Camera glide (about 2 s, the game waits) to a door that opens. The door stays marked "OPEN DOOR" until walked through.
- Three rubble-blocked rooms below the hall, one with the survivor. The Engineer's E is locked until the dog has found them, then a new step marks that room's rubble.
- All four flow items: one white marker (or the green EXIT) at a time, with calls for help held back while a door is marked; "DONE!" plus a click per step; 4 chapters with title cards; 15 progress dots grouped by chapter.

## Consequences
At any moment the player has one thing to walk to and can see how far they are. The Dog Handler now has a real job. **Given up:**
- Control for about 2 s at each of 3 doors, which a returning player may find slow (there's no skip).
- Extra screen furniture (dots, title cards, labels) competing with the map for attention.
- One more step (15) and a longer timed part (40 s of 75 s in the balance test, from 17 s).
- Tutorial-specific code in `GamePanel` (camera glide, dots, chapter card).

## Generalizes to
**Onboarding signposting: single focus vs. ambient hints.** In a tutorial, mark exactly one goal at a time, take the camera to off-screen changes the player caused, and close each step with feedback. Introduce an ability in a spot where it is the only way forward. This does not hold for sandbox or discovery-led games, or for experienced players, where forced camera moves and constant markers feel patronising. Make them skippable or limit them to the first run.
