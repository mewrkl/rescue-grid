# 0021: Abilities show when and how far they work

- Status: Accepted
- Date: 2026-11-16
- Source: my own audit of the game. In Level 3, the start hint said "Dog Handler (3): press E"; pressing it gave "No scent nearby" and cost the full 8 s recharge. The user chose these fixes in chat.

## Context
Three problems shared one cause: the game didn't show whether an action could work before the player tried it.
- The Dog Handler's scent reaches 8 walking steps, but nothing on screen showed that, and the start-of-level hint sent players to sniff where nothing could be found.
- A miss cost the same 8 s as a find, so learning the range was punished.
- "Q Call backup" was always shown at full brightness, even with nobody to call it for.

## Options considered
- **Show the range and make misses cheap:**
  - Faint paw prints on every seen tile the scent reaches.
  - A miss recharges in 2 s, not 8.
  - The hint says how far the dog smells instead of "press E".
- **Keep the full cost but show the range:** still teaches the range, and keeps sniffing a careful choice.
- **Hot-or-cold hint** ("someone is in range, sniff now"): the most helpful, but it gives away hidden survivors without sniffing.
- **For the keys:**
  - *Dim E and Q while they can't act* (half opacity, the usual disabled look).
  - *Hide them:* removes clutter, but a hidden action reads as missing, not as "not yet".

## Decision
- **Range:** show the range with paw prints. They are drawn only on tiles already explored, so they never give the map away, and are brighter when Scent is ready.
- **Misses:** a sniff that finds nobody new recharges in `DogHandler.MISS_COOLDOWN_SECONDS` (2 s).
- **Hint:** the start hint explains the range rather than telling the player to press E.
- **Dimmed keys:** each role answers `hasSpecialTarget(board)`, and the HUD dims the E row while it recharges or has nothing to act on. `canCallBackup(board)` dims the Q row unless a stabilised red survivor is right there.

## Consequences
Players can see where Scent works and when E and Q will do something, and probing costs little. **Given up:**
- **Some of the Dog Handler's tension:** spamming Scent every 2 s while walking is now a valid habit.
- **A tidy map:** paw prints add marks to every floor tile nearby while you are the Dog Handler.
- **Tutorial text:** it still teaches by trying, so one more message ("No scent within 8 steps") has to match the new rule.

## Generalizes to
**Afford before you punish.** Show the reach and readiness of an action before the player commits: range markers, dimmed buttons, a target outline. Once that is shown, keep the cost of a miss low, so trying stays cheap while players learn. This does not hold when uncertainty is the point (a sonar ping in a horror game, a gamble), or when showing range gives away hidden information. Then show only the range over known ground, as here, or nothing at all.
