# 0007: One rescuer on the map who switches role, not three rescuers to manage

- Status: Accepted
- Date: 2026-11-15
- Supersedes: 0002
- Source: teacher feedback after the first showing, team discussion with Claude

## Context
Decision 0002 put all three rescuers (Medic, Engineer, Dog Handler) on the map and let the player switch between them. In the showing, the teacher pointed out the cost: the rescuers you are not controlling stand still, get left behind, and can burn to death for no reason. Constantly switching just to move everyone along felt awkward. The teacher suggested either one character who can swap roles mid-round, or teammates who follow the player. The three role subclasses were promised in the course proposal and must stay.

## Options considered
- **One rescuer, swap role at any time** (like Trine): nobody can be left behind; the same three subclasses still exist, and swapping still shows polymorphism (the E key runs a different `specialAction()`).
- **One rescuer, swap only at the exit** (like Flash Point's crew change): forces planning before each trip.
- **Keep three, the others follow in a line** (like Pikmin or EarthBound): keeps the "team" look on screen.
- **Keep three, a Call Help key brings one to you**: the smallest change from 0002.

## Decision
Only one rescuer is on the map. Keys 1/2/3 (or Tab) change role after a 1-second "changing gear" pause, during which the rescuer can't move or act. The new role takes over the position, facing, and anyone being carried or led. Each role keeps its own health and cooldown. If the role on the map is downed, the next standing role takes over at once, and the downed role can't be picked again this level. The mission fails when all three are down.

## Consequences
- **Gained:** the "left behind" problem is gone by design. Controls are simpler: one character, one health bar to watch at a time. There is no pathfinding AI to build or debug.
- **Given up:**
  - **Being in two places at once:** levels can no longer ask one rescuer to hold a spot while another works elsewhere.
  - **The visible team:** you never see all three together on the map. The sidebar shows the team instead.
  - **Exit-only swapping:** the extra planning it would have forced.
  - **Following teammates:** the "alive" feel, and the teacher's follow-the-player idea in its literal form.
- Fire and aftershocks now only hurt the role on the map, so swapping to a healthy role is a real survival choice.

## Generalizes to
**Party control in single-input games: one body with swappable kits vs. several bodies.** Use one body with swappable abilities when the party's members differ in *abilities* rather than in *position*, and idle members only create upkeep. This does not hold when the puzzles need bodies in two places at once (pressure plates, guarding, splitting up), or when a co-op mode needs every member on screen.
