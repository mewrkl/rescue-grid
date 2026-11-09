# 0006: Random survivor spots, chosen from spots we designed

- Status: Accepted
- Date: 2026-11-09
- Source: team playtest feedback ("so it's not redundant per level"); RESCUE_GRID_PLAN.md section 5.

## Context
Every survivor was at a fixed position in the level file. After one play you know where everyone is, so replays turn into running a memorised route. That also makes the Dog Handler's scent pointless. But levels were designed and checked so every survivor could be reached in time, including after the aftershock collapses the cracked shortcut.

## Options considered
- **Fully random:** place survivors on any floor tile. This gives the most variety and needs no map changes. But a survivor can land next to the start, inside a fire, or too far away to save in time, and fairness can no longer be checked ahead of time.
- **Candidates in the map (`?`):** the designer marks 6–7 possible spots per level, and each time the level starts the game picks as many as it needs. Plays still vary, and every possible pick can be checked by a script beforehand.
- **Keep fixed spots:** completely predictable and easiest to test, but the replay problem stays.

## Decision
Candidate spots. Each level keeps one fixed `S` (the survivor behind rubble, so the Engineer is always needed) and has 6–7 `?` spots. `LevelLoader` shuffles the spots with the `Random` it is given and takes as many as it needs. The game passes `new Random()`; the tests pass a seeded `Random`, so they always get the same spots.

## Consequences
Every play is different, but always fair. A route script checked every possible pick (15, 20 and 35 picks for the three levels), and even the slowest needs about 27, 44 and 52 s of movement against 60, 75 and 90 s of survivor time. **Given up:** the fully random option's endless variety. Each level now has a fixed, small set of layouts, and a player who plays enough will learn the spots. The fixed seed for fire and aftershocks is still the same every play, so the hazards repeat even when the survivors move. Also, bug reports now need to say where the survivors were.

## Generalizes to
**Procedural variety from authored candidates.** Let designers mark the possible positions and let the game choose among them at runtime. The variety stays inside a space you can check exhaustively. This does NOT hold when the candidate combinations grow too large to check (then you need runtime validation such as a reachability check), or when the content itself should be generated (whole maps rather than placements).
