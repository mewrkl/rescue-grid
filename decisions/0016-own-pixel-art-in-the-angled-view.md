# 0016: Our own pixel art, in the angled RPG view, with animation and furniture

- Status: Accepted
- Date: 2026-11-16
- Source: the user asked for "final game worthy sprites", found online or made by us. I looked at Kenney's CC0 packs and LimeZu's Modern Interiors, and the user chose every option in chat. This builds on decision 0003 (make assets in-house) and replaces the plan's earlier one-pose, straight-from-above sprite plan.

## Context
The game drew everything as placeholder shapes in code. No free pack has the game's own pieces: rescuers in role gear, survivors in triage colours, fire, rubble, cracked floor and taped doors. The best pack found, LimeZu Modern Interiors ($1.50 for the full licence), is in the angled RPG view and covers rooms and people, but not those pieces. Kenney's CC0 packs are free, but generic or in a different style. Any choice meant drawing some sprites ourselves.

## Options considered
- **Source:**
  - *Paid pack plus ours:* the most polished rooms and people for the money, but our pieces have to imitate someone else's style, and the pack needs credit.
  - *Free CC0 packs plus ours:* nothing to pay or credit, but generic and with few interiors.
  - *All our own* (chosen): one consistent style, made for our mechanics, no licences, and anyone on the team can change a sprite in code. It looks less polished than a professional pack.
- **View:**
  - *Angled* (chosen): what players expect from a finished top-down RPG. Walls have fronts, and people stand up.
  - *Straight from above:* no new code, but walls are flat patterns and people are hard to read from the top.
- **Animation:** one pose each / facing 4 ways / 4 ways plus walking (chosen).
- **Size:** 16 × 16 at 4× (chosen), or 32 × 32 at 2× with four times the drawing.
- **Mood:** dusty disaster (chosen), bright and clean, or dark night.
- **Props:**
  - *Themed furniture that blocks* (chosen).
  - *Decoration only:* nothing changes on the maps.
  - *None.*

## Decision
`tools/SpriteMaker.java` draws every sprite from code (shapes, then an automatic 1 px outline) into sprite sheets:
- 19 sheets in total.
- Characters are 16 × 24, with the four directions as rows and stand, step A and step B as columns. Left-facing frames are mirrored from right-facing ones.
- The dog is its own sheet, drawn beside the Dog Handler.

The new code:
- **Walls:** a wall picks its look from its neighbours (`Wall.getPieces`): a brick front above floor, a dark top above more wall, and edges where it meets floor. Walls cast a soft shadow on the floor below them and to their right.
- **People** are drawn top row first, so their heads overlap the tile above.
- **Furniture:** 13 kinds (`Prop`) block walking but not sight or scent, and never burn or collapse.
- **Floor decorations:** 4 kinds (`DecoratedFloor`) behave exactly like floor.
- **Maps:** each level has a theme (training centre, home, office, hospital), and every map was re-checked by the balance tests.

## Consequences
The game has a complete, consistent look and nothing to license. The placeholder shapes stay as the fallback when a PNG is missing. **Given up:**
- The polish of a professional pack: our people are simple, and up close the furniture is plainer.
- Simplicity: sprites are code, so changing one means editing Java and re-running the tool, not painting in an art program.
- People are 1.5 tiles tall, so they can cover what is on the tile above them. Labels were moved up to clear their heads.
- Furniture changed the maps. Level 1's sofa had to move because it changed how the fire spreads, and the balance test now has to know which letters are furniture.
- No carrying pose: carrying is still shown by a badge.

## Generalizes to
**Game art: own procedural sprites vs an asset pack.** Draw your own, in code, when the game needs many pieces no pack has and the team has more programmers than artists. Shapes plus an automatic outline keep a consistent style, and rebuilding is repeatable. This does not hold when a pack already covers most of what you need, or when the look is the main selling point. There, buy or commission art and spend the time on gameplay.
