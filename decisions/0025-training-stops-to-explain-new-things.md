# 0025: Training stops to explain each new thing

- Status: Accepted
- Date: 2026-11-16
- Source: user feedback after playing ("a person could easily skip that by just running over the person", "we can't use it right now since no one's in the other room doesn't read well", "I didn't realise they were on a timer"), then the user's choices in chat

## Context
The training's card at the bottom of the map said what to do. It never stopped the game, so a player could walk into the green survivor, or straight past the aftershock warning, before reading it. The ability tests went Engineer, Dog Handler, Medic. The dog's test found nobody on purpose ("nobody is hiding in this room"), which taught that the ability fails. Fire and the survivors' timers were mentioned only in passing. One player only found out about the timers when "a survivor didn't make it".

## Options considered
- **How a pause works:**
  - *Freeze, glide the camera to the thing, show a card; Enter carries on (chosen).*
  - *Freeze and show the card, camera stays:* simpler, but the thing explained may be off to the side.
- **The dog test:**
  - *Smell the green survivor through the taped door (chosen):* the ability really works, and the person found is the one rescued next.
  - *A hidden practice dummy:* it works, but it's a fake target.
- **How roles are introduced:**
  - *A card per role before its test (chosen).*
  - *A longer bottom card:* less interruption, easier to skip.

## Decision
A `Lesson` (title, text, picture, camera focus) is queued by `Tutorial` and shown by `GamePanel` in a new `GameState.LESSON`. Nothing moves until Enter, and a key held from walking can't skip it. Each card shows once:
- **Each role**, when you become it, explaining what E does and when it works. The tests now go Medic (the role you start as), then Engineer, then Dog Handler.
- **Each survivor type**, the moment they come into sight (sight reaches 4 steps): green, yellow and trapped, red, and the one the dog finds.
- **Fire**, when first seen; the fire visibly spreads one tile while the card is up.
- **The timers**, when the clock starts. Found survivors now always show their seconds left (orange from 30 s, red from 10 s), in every level.
- **The aftershock:** the floor about to fall flashes, and the countdown only starts after Enter.
- **The dog test:** you stand by the taped door. Scent passes taped doors and rubble, not sight (`Tile.letsScentThrough`), so the dog finds the green survivor in the next room.

## Consequences
Nobody can skip past a new idea without its explanation, and every ability test succeeds. **Given up:**
- **Flow:** around ten more Enter presses in the training.
- **Exploring:** the camera is taken away briefly each time.
- **A gameplay change:** scent passing taped doors also applies wherever a taped door is closed.
- **Calls for help:** they are no longer taught in the training's first room, since the dog finds the green survivor first. The timer card mentions them instead.

## Generalizes to
**Gate the first encounter, not the instruction.** Trigger an explanation the moment the player first meets the thing (in sight, about to happen), freeze until it is acknowledged, and point the camera at it. Make practice actions succeed rather than demonstrate failure. This does not hold for players who already know the game (offer a skip, as the main menu now does), or for genres where discovering rules is the fun (puzzle and immersive sims).
