# 0010: Start at the exit, with survivors grouped in rooms

- Status: Accepted
- Date: 2026-11-15
- Source: playtest feedback ("survivors are really spread out", "unclear what to do"), team discussion

## Context
In every level the team started far from the exit, and the possible survivor spots (0006) were spread over every corner of the map. Each rescue was a long walk across the whole building, and with no clear objective players didn't know where to go.

## Options considered
- **Keep the layouts, add only an objective panel:** no level work.
- **Start at the exit, group the spots into 2 or 3 room clusters:**
  - real rescuers enter by the way out;
  - each trip is "go to that area, bring people back";
  - the random spots from 0006 still vary each play, within a cluster.
- **Fully hand-placed survivors, no randomness:** the easiest to balance.

## Decision
The team starts beside the exit (a single `P` in the level file). The random `?` spots are grouped into 2 or 3 clusters. Each level still keeps its cracked-floor shortcut on the way *back*, and one survivor behind rubble. This comes with three helpers:
- survivors you haven't found yet call for help, with a direction marker;
- an objective checklist shows on the map;
- a tutorial level comes before Level 1.

## Consequences
- **Gained:** shorter, readable trips. Fewer places to search. The first minute of each level is obvious: leave the exit and head for the nearest calls.
- **Given up:**
  - The tension of a long trek through unknown building.
  - Some of the variety 0006 bought: clustered spots vary less between plays.
  - The old shortcut placement, which was designed for a start far from the exit.
  - Fully hand-placed survivors, which would have been the easiest to balance.
- All three maps had to be re-edited and the reachability check rerun.

## Generalizes to
**Hub-and-spoke level flow.** Put the safe point (spawn, exit, base) at the centre or start, and cluster objectives so each outing has one destination. This does not hold when exploration and getting lost *is* the experience (horror, survival), or when the map is meant to be crossed once, start to finish.
