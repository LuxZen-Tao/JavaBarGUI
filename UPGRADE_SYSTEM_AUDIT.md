# Upgrade System Audit

Upgrade pipeline (current):

1. **Definition**
   - Upgrade definitions are centralized in `PubUpgrade` (id/label/cost/tier + structured numeric effect payload).
   - Upgrade category/effect summarization is centralized in `UpgradeSystem`.

2. **Purchase**
   - Purchase is initiated from `Simulation.buyUpgrade(...)`.
   - Availability/lock reasons are resolved by `MilestoneSystem.getUpgradeAvailability(...)`.
   - Cost is charged immediately via `EconomySystem.tryPay(...)`.

3. **Install**
   - Purchases create `PendingUpgradeInstall` entries.
   - Installs resolve at night end in `Simulation.processPendingUpgradeInstallsAtNightEnd()`.

4. **Effect Application**
   - `UpgradeSystem.buildModifierSnapshot()` aggregates all owned-upgrade effects with deterministic ordering:
     - additive stats first
     - multiplicative/security-tier modifiers second
     - final clamping
   - `Simulation.applyPersistentUpgrades()` applies the snapshot into game-state runtime modifier fields consumed by simulation subsystems.

5. **UI Display**
   - Upgrades UI shows tier/cost/effect summary lines and lock reasons.
   - Lock reasons include prerequisites, milestones, pub-level gates, and insufficient funds.
