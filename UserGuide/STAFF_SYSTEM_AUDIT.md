# Staff System Audit (pre-rebalance map)

This note captures the staff→outcome pipeline before/while applying the rebalance.

## Current staff attributes
- Core per-staff values: `serveCapacity`, `skill`, `repMin/repMax`, `weeklyWage`, `capacityMultiplier`, `tipRate`, `tipBonus`, `securityBonus`, `chaosTolerance`, `morale`, `level`.
- Main sources: `Staff.java`, `StaffFactory.java`.

## Staff to outcome mapping
- Throughput / unserved:
  - FOH `serveCapacity` aggregated by `StaffSystem.totalServeCapacity()`.
  - Managers apply `capacityMultiplier`.
  - Round demand vs capacity resolved in `Simulation.playRound()`.
- Refunds / complaints:
  - Food refund chance in `Simulation.processFoodOrders()` depends on chef skill, kitchen quality, security, head chefs, and now workload pressure.
- Chaos / fights / incidents:
  - Chaos baseline from round pressure in `Simulation.recomputeChaos(...)` and `updateChaosFromRound(...)`.
  - Staff composure influence available via `chaosTolerance` and now centralized workload penalty + staff profile pressure in `StaffSystem`.
  - Staff incidents/misconduct chance in `Simulation.computeMisconductChance(...)` now also scales with staffing pressure.
- Morale / retention:
  - Round morale updates in `StaffSystem.adjustMoraleAfterRound(...)`.
  - Quits in `StaffSystem.weeklyMoraleCheck(...)`.
- Payroll:
  - Accrual/payment in `StaffSystem` (`accrueDailyWages`, `wagesDue`, `applyWagePayment`).

## Scaling points
- Week/rep/upgrades scale traffic and capacity indirectly in `Simulation` + `UpgradeSystem` effects.
- Staff quality/wage scaling now also uses week+rep in `StaffFactory.createStaff(..., week, reputation)` for candidate generation.
- Non-linear under-staff penalty is centralized in `StaffSystem.workloadProfile(...)` with exponent constant.
