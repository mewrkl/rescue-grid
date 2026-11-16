# 0015: Test each role's ability in the first room

- Status: Accepted
- Date: 2026-11-16
- Source: the user suggested letting players try the abilities before leaving the first room ("switch to this, test your ability"), and chose "all three" in chat

## Context
The training taught changing role with one step: press 2, press 3, press 1. The abilities were first used later, each at the step that needs it: Engineer at step 6, Medic at step 9, Dog Handler at step 11. So for most of the training a player knew there were three roles but not what any of them did. Decision 0013 had just made each ability really needed later on. That was a reaction to an earlier playtest where the Dog Handler's ability never had to be used.

## Options considered
- **All three test their E in the first room:** practice rubble for the Engineer, a sniff for the Dog Handler, and a first-aid dummy (a new tile) for the Medic. Every role is tried straight after it is named, which makes the key stick, and the card says "Test your ability now". This is like a training dummy or practice range in other games.
- **Engineer and Dog Handler only:** the same, but the Medic first uses Treat on the red survivor. No new tile is needed, because Treat needs a patient and the first room has none.
- **No practice:** each ability is first used where it is needed. The training stays shortest, and every use has a real purpose.

## Decision
All three. The single "try every role" step becomes three: 2 → clear the marked practice rubble; 3 → sniff (nobody is in the room, and the next prompt explains why the dog found nothing); 1 → treat the marked dummy. Each role's E is unlocked only for its own test step, then locked again until the step that uses it for real. The dummy is `TrainingDummy`, a solid tile whose `isTreatable()` hook (false on every other `Tile`) lets the Medic treat it. The training grows from 15 to 17 steps.

## Consequences
After four steps the player has used every key the game has, except Q, in a room where nothing can go wrong. The marker appears only once the player is the right role, so there is still one thing to do at a time. **Given up:**
- Two more steps of training. Each one is about 10 s, but that is time before the first survivor.
- The practice uses have no real purpose. The sniff even finds nothing, which a player could read as the ability failing; the prompt has to explain it.
- One more tile class and a hook on `Tile` that exists only for training.
- The "no practice" option's purity, where every ability is first met as the only way forward. The later steps still give each ability that moment, so it is repeated rather than lost.

## Generalizes to
**Onboarding abilities: a safe sandbox try vs first use in context.** Let the player try each new verb straight after it is named, in a space where nothing can fail, when there are several verbs on similar keys to keep apart. Then give each verb a real use soon after. This does not hold when there are many verbs (a long sandbox becomes a lecture), or when the verb only makes sense with its situation, like stealth or timing-based skills. There, teach it in context.
