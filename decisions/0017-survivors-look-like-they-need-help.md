# 0017: Survivors look like they need help

- Status: Accepted
- Date: 2026-11-16
- Source: user feedback on the first sprites ("the survivors don't really look like they need help"). I studied how Stardew Valley, Pokémon, RimWorld and Into the Breach draw people, plus pixel art guides (SLYNYRD, Pixnote), and the user chose every option in chat.

## Context
The first sprites (decision 0016) gave survivors the same healthy standing body as the rescuers, in their triage colour. Only the serious one sat down, and the minor one wore a headband. On the map they read as bystanders, not as people in trouble. The references point the same way:
- RimWorld shows downed people lying down, crawling and leaving a blood trail.
- Into the Breach lets the people at stake call out in speech bubbles.
- Hurt-state sprite packs use separate slumped, on-the-floor poses.
- Stardew and Pokémon keep characters readable at 16 px with a big head and only the most telling features.

## Options considered
- **Signal:**
  - *A bouncing "!" bubble* (chosen): readable across a room, small.
  - *A "HELP!" text bubble:* the clearest, but wide, and it covers the tile above.
  - *Pose only:* the cleanest, but easy to miss in a busy room.
- **Injuries:** *mild blood and bandages* (chosen), or *no blood at all*. No blood is fully family-friendly, but a red-tag survivor then looks merely asleep.
- **Poses:**
  - *A different pose per triage colour* (chosen).
  - *Everyone on the ground:* all read as helpless, but green and yellow then differ only by colour.
- **Props:**
  - *Polish plus damaged versions* (chosen).
  - *Polish only.*
  - *Polish plus 2-tile furniture:* the most realistic, but needs new map symbols and a rebalance.

## Decision
- **Green:** limps, with one arm in a sling and a bloodied head bandage, and waves the other arm over their head while shouting (two waving frames).
- **Yellow:** slumped on the floor with both hands on a bleeding, bandaged leg and a pained face, rocking (with a drop of sweat).
- **Red:** flat on their back, pale and bruised, eyes closed, one arm reaching up, a small pool of blood beside them, breathing slowly.
- **All three:** dusty, torn clothes and worried brows.
- **The bubble:** a found survivor who is still waiting shows a white bubble with a red "!" that bounces by their head. It goes once you reach them.
- **Everyone:**
  - Shading is hue-shifted: shadows lean blue-violet, lights lean warm.
  - Outlines are a dark tone of the colour they touch, not flat black.
- **Props:** redrawn with a lit top and a shaded front. Each has a damaged version (torn sofa, sagging table, fallen bookshelf, tipped plant, cracked screen, spilled cooler and more), and about a third of the furniture is damaged.

## Consequences
At a glance, a survivor is now someone in trouble. The shape alone (standing and waving, slumped, lying) tells the three triage colours apart, which also helps colour-blind players. **Given up:**
- The cleaner look of fewer elements: the bubble adds one more moving thing per survivor.
- A fully family-friendly look: there is a little blood.
- Training markers now sit higher, above the bubble.
- The bubble shows for every found survivor, so a room with several can feel busy.
- Telling green from yellow in the HUD's small icons still depends partly on colour.

## Generalizes to
**Making a character's need readable: pose, effect and indicator.** When players must tell at a glance who needs what, give each state its own silhouette, such as standing, slumped or lying. Add one small animated indicator for "act here". Keep the triage colour as a second cue, not the only one. This does not hold for games where finding people is the challenge, like hidden-object or stealth games. There, a loud indicator gives the game away, so rely on subtle poses and sounds.
