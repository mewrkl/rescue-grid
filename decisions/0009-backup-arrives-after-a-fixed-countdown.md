# 0009: Backup for critical survivors arrives after a fixed countdown

- Status: Accepted
- Date: 2026-11-15
- Source: team discussion (0008)

## Context
A stabilised critical survivor is carried out by a backup stretcher team that the player calls with Q, the teacher's "call help" idea. We had to decide how that team arrives.

## Options considered
- **A stretcher team walks in from the exit:**
  - you can see the "automatic moving around" the teacher mentioned;
  - fire and rubble on its route would matter, giving the Engineer a job;
  - it needs a pathfinding search we write ourselves.
- **Arrive after a countdown that depends on the distance, and pauses while the route is blocked:** keeps the Engineer's job without anything walking on screen.
- **Arrive after a fixed countdown:** the simplest to build and explain.

## Decision
A fixed countdown (10 s, to be tuned) starts when backup is called. The player is free to leave and keep searching. When it reaches 0, the survivor is rescued, as long as they are still alive. The survivor's own condition keeps draining (slowly, since they are stabilised), and fire can still reach them.

## Consequences
- **Gained:**
  - About a day of work saved, with no pathfinding or blocked-route cases to build.
  - It's easy to explain and to test: after 10 s, they're rescued.
- **Given up:**
  - The visible team moving through the building.
  - Clearing a route for backup as a reason to use the Engineer.
  - Distance mattering: a critical survivor deep in the building is as quick to save as one near the exit, so they must be placed with that in mind.

## Generalizes to
**Simulated agents vs. abstracted timers for off-screen help.** Use a timer when the help's route isn't something the player can influence in an interesting way, or when the build budget is tight. This does not hold when the player's actions should change how fast help arrives (clearing paths, escorting), because then the route *is* the gameplay and it needs to be simulated.
