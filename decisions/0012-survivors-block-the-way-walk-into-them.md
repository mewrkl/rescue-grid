# 0012: Survivors block the way; you walk into them to help

- Status: Accepted
- Date: 2026-11-15
- Source: second hand playtest of the training level; the user chose this option in chat

## Context
Until now a waiting survivor sat on a walkable tile. Stepping onto that tile asked them to come along (decision 0008). A minor survivor followed and a serious one was picked up. A critical or trapped survivor refused, showed a message, and was walked over. In the playtest this felt wrong: you could stroll across injured people without knowing what to do, and walking over a red survivor looked like nothing happened. Survivors are people, and the player's question at that moment is "what does this person need?"

## Options considered
- **Solid, walk into them:** a waiting survivor blocks the tile like a wall. Walking into them does what stepping onto them used to do (follow / be carried / say what they need). No new key. It reads like bumping into someone, as in roguelikes.
- **Solid, press a key to help:** face them and press a new key (Space) with a "[SPACE] Carry" prompt. This is the most explicit, and common in action games, but it adds a fifth action key to learn.
- **Keep walking onto them, add labels:** the least change, with nothing to rebalance. It fixes the "what do I do" but not the "why can I walk over them".

## Decision
Solid, walk into them. `MovementCalculator.canStep` refuses a tile with a waiting survivor, and `survivorAhead` returns them. `GamePanel` calls `reachedBy` when you walk into one, once per key press (holding the key doesn't repeat the message). Followers, carried and rescued survivors don't block. Standing next to a survivor shows their need (`getNeed()`) in a box under them. This applies to every level, not only training.

## Consequences
The rule matches how E already works ("face it and act"), and a red survivor now visibly stops you and says what they need. **Given up:**
- A survivor can now block a path. A test (`survivorsNeverBlockTheWay`) fills every possible survivor spot in every level and checks nothing gets cut off. Future maps must keep survivor spots out of doorways and corridors.
- The interact key's extra clarity: walking into someone is less obvious than a prompt, which is why the need label was added.
- Picking someone up "on the way past" without stopping. You now stop one tile short. The balance numbers barely move (one step less per trip), so they weren't re-tuned.

## Generalizes to
**Contact interactions: walk-over trigger vs. bump vs. explicit key.** Make interactable characters solid when the thing being touched should feel like a being, and when the "use" action already means "act on what you face". Bump-to-interact keeps the key count low. It does not hold for fast-paced games where stopping costs too much (pick-ups in a platformer should stay walk-over), or when the same neighbour supports several different actions: then an explicit key with a contextual prompt is clearer.
