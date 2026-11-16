# 0026: Furniture reads as furniture

- Status: Accepted
- Date: 2026-11-16
- Source: user feedback ("I tried to run into a couple of closets because I didn't realise they were closets", "a black box around them making it feel unfinished"), a bug hunt, and the user's choices in chat

## Context
Furniture (`Prop`, decision 0016) was drawn as one 16 x 16 sprite in its tile. Two things went wrong:
- **The black box:** the sprite is see-through around its edges, and `Prop` never painted floor first, so each piece sat on a black square. The bug hunt also found the taped door's shadows replacing floor pixels instead of shading them.
- **Hard to read:** tall pieces like lockers were squashed into one tile seen from the front, so they looked like doors or walls. And Level 2 and 3 had over 50 pieces each.

## Options considered
- **Redraw tall furniture 1.5 tiles tall:** it stands up like a person, with a lit top, a side and handles. The same size as the people next to it, so it reads as an object.
- **Keep furniture against the walls:** the middle of rooms stays open, but it changes the maps and their balance.
- **Bump feedback:** walking into a piece names it, so it's clear it is solid.
- **Fewer pieces:** less to misread, at the cost of rooms looking emptier.

## Decision
- Fix the black box: `Prop` paints its floor first, and the taped door blends its shadows.
- Draw lockers, bookshelves, filing cabinets and medicine cabinets standing 16 x 24 (`props_tall.png`; damaged versions stay upright).
- The first bump into each kind of furniture in a level plays a thud and says "Locker: furniture blocks the way" (`Tile.getBumpName`).
- Remove about half the furniture in Levels 1-3 (26/51/54 to 12/28/25 pieces), keeping groups like a two-tile sofa together.

The user didn't choose moving furniture against walls.

## Consequences
Furniture now sits on the floor, the tall pieces stand up, and walking into one explains itself. `SpriteSheetTest` fails if a stand-alone tile has see-through pixels again. **Given up:**
- **Detail:** fewer pieces means rooms say "home / office / hospital" less loudly.
- **Draw order:** tall pieces are drawn with the floor, so a person standing directly behind one is drawn over its top. That is rare, since they stand against walls.
- **Bump reminders:** the bump message shows once per kind per level, so a player who misses it gets only the thud afterwards.

## Generalizes to
**Readable obstacles in a top-down view: match the height of the things around them.** Objects that block movement should stand up in the same 3/4 projection as the characters, sit visibly on the floor, and announce themselves on first contact. This does not hold for flat floor items (rugs, papers) that never block. Draw those flat on purpose, so the difference itself says "you can walk here".
