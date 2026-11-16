# 0020: The backup team is seen walking in, as a picture only

- Status: Accepted
- Date: 2026-11-16
- Source: user feedback ("when you call for backup... nothing else happens. He just kinda sits there and then disappears"), then the user chose this option in chat. It builds on decision 0009, whose fixed 10 s countdown stays.

## Context
Decision 0009 made backup a fixed countdown and gave up "the visible team moving through the building". In play, pressing Q started a small number over the red survivor; ten seconds later the survivor vanished. Nothing showed that a call was made or that anyone came, so the rule felt like a timer, not a rescue.

## Options considered
- **The team walks in from the EXIT:**
  1. The rescuer raises a radio, and a red flare by the survivor shows the countdown.
  2. Two paramedics with a stretcher walk the shortest way from the EXIT so they arrive exactly at 10 s.
  3. They lift the survivor onto the stretcher, carry them back out, and "Saved!" rises from the EXIT.
  - The most alive of the options; it shows the route and the distance.
- **The team arrives on the spot:** the same radio and flare, then at 10 s two paramedics drop in beside the survivor in a puff of dust and fade out. Simpler, no path to find.

## Decision
The team walks in from the EXIT, as a picture only. The rescue is still decided by the countdown in `CriticalSurvivor`. `BackupTeam` (in `ui`) only reads it:
- It finds a path with breadth-first search over floor, fire and rubble.
- It picks a walking speed so it arrives at 10 s.
- It ignores fire, never blocks the player, and walks back empty-handed if the survivor dies first.
- If there is no way through, it appears on the survivor's tile.

The "!" bubble hides once backup is called, and the flare takes its place.

## Consequences
Calling backup now looks like a rescue: a radio call, a flare, people coming and leaving with the survivor. The rules and the balance tests are unchanged. **Given up:**
- **Honesty about danger:** the team walks through fire unharmed, which a careful player may notice.
- **The Engineer's job:** clearing a route for backup still doesn't matter (0009's give-up stands), even though the route is now visible.
- **Simplicity:** one more class and a path search to keep in step with the countdown.

## Generalizes to
**Showing an abstract timer as a diegetic event.** When a rule is "X happens after N seconds", show the world doing X: someone setting out, travelling and arriving on the beat. Keep the rule in the model and let the picture follow the clock, never the other way round, so the visuals can't change the outcome. This does not hold when the travelling thing should interact with the world (be blocked, be hurt, need escorting). Then it must be a real game object with its own rules, and the timer goes.
