# 0008: Survivors have triage injury types that need different responses

- Status: Accepted
- Date: 2026-11-15
- Source: teacher feedback ("a slightly injured person can be helped by anyone, a seriously injured one needs backup"; "what if a survivor is trapped?"), START triage, Flash Point: Fire Rescue

## Context
Every survivor worked the same way: walk onto them, carry them out. The teacher suggested injuries of different severity, where only some roles can help, plus survivors who are trapped. Real disaster triage (START) sorts people into green (minor, can walk), yellow (serious, can wait) and red (critical, needs help now). Once only one rescuer is on the map (0007), the roles have to matter through what each survivor needs.

## Options considered
- **Keep one survivor type:** the least code, and the rules are already tested.
- **Triage types as subclasses of an abstract `Survivor`:**
  - each type answers "a rescuer reached me", "treat me" and "call backup for me" in its own way;
  - this adds a second inheritance tree the teacher can see.
- **One `Survivor` class with an injury enum and switches:** fewer files.

## Decision
`Survivor` becomes abstract, with three subclasses:
- **`MinorSurvivor` (green):** walks behind the rescuer, one at a time. Their condition drains slowly.
- **`SeriousSurvivor` (yellow):** carried at half speed, as before.
- **`CriticalSurvivor` (red):** can't be moved. The Medic must stabilise them, which slows the drain, and then backup is called (0009). Their condition drains fast until stabilised.

**Trapped** is a flag that any survivor can have, not a subclass: the Engineer lifts the debris (E), and then the survivor behaves as their type.

## Consequences
- **Gained:**
  - Each role has a reason to be chosen.
  - Polymorphism shows up where survivors are reached, treated, and have backup called.
  - The game follows a real triage system, which suits the disaster-management theme.
- **Given up:**
  - The one-line rule "walk onto them, carry them out".
  - Part of the old survivor tests, which had to be rewritten.
  - The enum version's single place to read every rule.
- More for players to learn, so How to Play and the tutorial must teach the types.

## Generalizes to
**Varying behaviour by kind: subclasses vs. a type field.** Use subclasses when each kind reacts differently to the *same* few events, and the set of kinds is small and fixed. Use a flag or composition (like "trapped" here) for a state that can switch on and off at runtime, because an object can't change its class. This does not hold when kinds differ only in numbers (use data), or when new kinds are added often by non-programmers (use data files).
