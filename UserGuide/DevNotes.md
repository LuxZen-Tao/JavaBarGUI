# Dev Notes

## System Map (Economy + Weekly Cycles)
- **Entry point:** `Main.java` launches `WineBarGUI` with a fresh `GameState`.
- **Economy state owner:** `GameState` (cash, credit score, credit lines, supplier trade credit for wine + food, wage escalation flags, shark threat tier).
- **Central payment path:** `EconomySystem.tryPay(...)` (cash-first, then credit line draw).
- **Credit lines + banks:** `CreditLineManager` (open lines, apply credit, weekly interest + repayment checks, credit score adjustments).
- **Loan shark escalation:** `Simulation.processSharkThreatWeekly()` + `Simulation.applySharkTierEffects(...)`.
- **Payday:** Weekly bills (supplier trade credit, wages, rent, credit lines, loan shark) are assembled in `Simulation.preparePaydayBills()` and paid manually in `WineBarGUI`’s Payday Report dialog.
- **Wages + collapse:** weekly wage payment in `Simulation.endOfWeek()` via `EconomySystem.endOfWeekPayBills(...)`; escalation in `Simulation.handleWageMiss()` / `Simulation.applyWageMissEffects(...)`.
- **Weekly tick trigger:** `Simulation.closeNight(...)` → `endOfWeek()` when `dayIndex == 0`.
- **Reports + metrics:** `Simulation.buildMetricsSnapshot()` (Mission Control) and `ReportSystem` (weekly + 4-week reports).

## Trading Standards System (v3.1)
- **Purpose:** Track underage service violations with escalating penalties, including instant game over at Tier 3.
- **Implementation:** Integrated into `SecuritySystem` and checked during customer service.
- **Weekly Reset:** Violation counter resets at payday via `Simulation.endOfWeek()`.
- **Penalty Evaluation:** `Simulation.evaluateTradingStandards()` checks violations and applies penalties.
- **Mitigation:** Security level, bouncer quality, and security policy all reduce violation chance.
- **Critical:** Tier 3 (9+ violations) triggers instant game over—no recovery possible.

## Inn Events System (v3.1)
- **Purpose:** Add narrative depth to inn operations with random events when rooms are booked.
- **Frequency:** Based on inn reputation (low rep: 30-40% chance, high rep: 5-10% chance).
- **Tone Distribution:** Low rep: 85% negative, high rep: 75% positive.
- **Event Types:** 10+ positive events (tips, reviews, referrals) and 10+ negative events (damages, theft, complaints).
- **Staff Mitigation:** Marshalls reduce event severity by 25%, Duty Managers by 50%.
- **Implementation:** Part of inn system, evaluated nightly when rooms are booked.

## Time-Gated Pub Levels (v3.1)
- **Purpose:** Prevent power-gaming by requiring both milestone completion AND minimum weeks at each level.
- **Implementation:** `PubLevelSystem` checks both `achievedMilestones.size()` and weeks passed.
- **Requirements:**
  - Level 0→1: 2 milestones + 2 weeks
  - Level 1→2: 5 milestones + 3 weeks
  - Level 2→3: 9 milestones + 4 weeks
  - Level 3→4: 14 milestones + 5 weeks
  - Level 4→5: 20 milestones + 6 weeks
- **Effects:** Pub level affects upgrade unlocks and activity effectiveness multipliers (up to 1.25x).

## Recent Additions Summary
- **v3.1 (February 2026):**
  - Trading Standards System with 3-tier penalties
  - Inn Events System with reputation-based frequency
  - Time-Gated Pub Level progression
  - Enhanced staff system (Marshalls, Duty Managers)
  - Expanded activities (20+ types)
  - Extended security tasks (15+ tasks)
  - 6 pub identity types with 4-week rolling history
  - 6 rumor topics with staff/punter sources
  - 15+ landlord actions with detailed mechanics
  - CodeQL clean (0 vulnerabilities)
  - 8+ comprehensive test suites
