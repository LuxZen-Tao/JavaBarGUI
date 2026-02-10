# Java Bar Sim v3 — Complete Player Guide

Java Bar Sim v3 is a **management/tycoon simulation** where you run a neighborhood pub night after night, balancing cash flow, staffing, reputation, risk, and long-term growth. The game runs in Java Swing and combines short tactical decisions (this round, this night) with strategic planning (this week, this quarter).

---

## 1) What the game is (at a glance)

You are the owner-operator of a pub.

Your job is to:
- Keep service running each night.
- Buy stock before you run out.
- Set prices that earn money without driving punters away.
- Manage staff quality, morale, and coverage.
- Control fights, theft, and operational chaos.
- Pay weekly obligations (wages, rent, debt, supplier invoices).
- Invest in upgrades, activities, and specialization.

The game is best understood as:
- **Night simulation** (operational execution), plus
- **Weekly business simulation** (financial discipline and growth).

---

## 2) Core loop and cadence

### Night cycle
1. Prepare before opening (stock, staff, policies, activities).
2. Open the pub.
3. Progress rounds and react to what happens.
4. Close the night and review outcomes.

### Weekly cycle
- Nights accumulate through the week.
- At week end, major bills and reports arrive.
- You absorb economic consequences, reputation shifts, and system-driven events.
- Then repeat with better (or worse) momentum.

---

## 3) Win condition / failure pressure

There is no single “press this button to win” ending.

Instead, success is measured by compounding outcomes:
- Stable positive cash flow.
- Controlled debt and healthy credit score.
- Strong reputation and pub identity momentum.
- Staff retention and capability growth.
- Progression unlocks (milestones, pub level, prestige trajectory).

Failure pressure comes from:
- Insolvency, debt spiral, or punitive lending costs.
- Wage/rent misses and escalating penalties.
- Security collapse (fights/theft/chaos loops).
- Service degradation from poor staffing and stockouts.

---

## 4) Interface map (what each main control area does)

The top-level HUD and grouped controls are organized around gameplay domains:

- **Night**: Open/advance/close the current service session.
- **Economy**: Price slider, supplier access, and financing tools.
- **Management**: Staff, inn, upgrades.
- **Risk**: Security posture and enforcement options.
- **Activities**: Event scheduling and landlord action plays.
- **Automation**: Toggle automation behavior where available.

Supporting windows and feeds:
- Inventory list.
- Report panels/dialogs.
- Event feed/log popups.
- Payday/billing dialogs.

---

## 5) Complete system guide + player tooltips

Below is a practical “how it works + how to use it” breakdown for each major system.

## 5.1 Economy & cashflow system

### What it does
Tracks all core money movement:
- Cash on hand.
- Weekly obligations.
- Debt servicing.
- Credit score health.
- Credit line utilization.

### How to use it well
- Treat cash as your first buffer; credit is emergency leverage, not normal operating oxygen.
- Avoid stacking multiple risk systems at once (high debt + low security + thin staffing).
- Keep reserve cash for weekly bills before pursuing aggressive expansion.

### Player tooltip
> **Economy Tip:** If your bar is “profitable” nightly but you still crash weekly, your fixed obligations are outrunning growth. Slow expansion and stabilize payroll/rent coverage first.

---

## 5.2 Credit lines, bank debt, and loan-shark pressure

### What it does
Provides external funding options with escalating consequences:
- Bank-style credit lines.
- Utilization and repayment pressure.
- Credit score feedback loops.
- Loan-shark threat escalation for missed obligations.

### How to use it well
- Draw credit to bridge timing gaps, not to fund permanent losses.
- Repay quickly to protect score and maintain future option value.
- Respect threat tiers; repeated misses can convert temporary distress into systemic penalties.

### Player tooltip
> **Debt Tip:** Credit buys time, not profit. If debt is covering wages every week, your underlying operating model needs correction.

---

## 5.3 Supplier & inventory system (wine + food)

### What it does
Controls stock acquisition and availability:
- Wine and food supplier channels.
- Deals, shortages, and delivery timing.
- Inventory caps and spoilage dynamics.
- Invoice/trade-credit interactions.

### How to use it well
- Buy to demand profile, not to max capacity by default.
- Use supplier deals when they match your actual throughput.
- Avoid overstock spoilage and understock stockout penalties.
- Keep enough depth for activity/event nights with demand spikes.

### Player tooltip
> **Stock Tip:** Empty shelves kill revenue instantly; overstock kills it slowly through waste and tied-up cash. Aim for controlled surplus, not extremes.

---

## 5.4 Pricing system (multiplier + demand reaction)

### What it does
Sets your price multiplier, affecting:
- Margins per sale.
- Affordability by punter segments.
- Complaint/negative reaction risk.

### How to use it well
- Raise prices gradually and watch behavior signals.
- High reputation can support stronger pricing, but only if service quality remains consistent.
- Combine premium pricing with premium execution (staffing, security, vibe).

### Player tooltip
> **Pricing Tip:** A price increase is only “real profit” if transaction volume and guest sentiment remain stable.

---

## 5.5 Staff system (hiring, coverage, morale, retention)

### What it does
Manages front-of-house and back-of-house workforce performance:
- Hiring/firing/roster composition.
- Role quality and throughput impact.
- Morale interactions with chaos and outcomes.
- Attrition risk under sustained stress.

### How to use it well
- Balance labor cost against service capacity, not against ideal staffing fantasies.
- Maintain role coverage before niche optimization.
- Protect morale during rough periods (security and workload management matter).

### Player tooltip
> **Staff Tip:** When nights feel random and messy, it is often not randomness—it is hidden staffing mismatch plus morale drag.

---

## 5.6 Punter/customer behavior system

### What it does
Simulates customer volume and behavior using:
- Reputation influence.
- Affordability constraints.
- Service and security responses.
- Feedback loops into future demand.

### How to use it well
- Match your offer to the customers you are currently attracting.
- Don’t chase all segments at once; specialize intentionally.
- Remember reputation amplifies both strengths and weaknesses.

### Player tooltip
> **Punter Tip:** Reputation increases opportunity, but also magnifies consequences when service slips.

---

## 5.7 Security system (policy, staffing, incidents)

### What it does
Handles risk-control mechanics:
- Security upgrades and quality.
- Policy stance selection.
- Fight/theft suppression.
- Task/event responses and deterrence.

### How to use it well
- Tune policy to current crowd profile and staffing reality.
- Invest before incidents become routine; reactive spending is less efficient.
- Use stronger security when running high-volume/high-chaos nights.

### Player tooltip
> **Security Tip:** Security is not just loss prevention—it protects staff morale, service consistency, and reputation compounding.

---

## 5.8 Activities & scheduled programming

### What it does
Lets you run themed activity nights (e.g., quiz/DJ/sports-style programming), which can shift:
- Demand volume.
- Spending behavior.
- Vibe/identity momentum.

### How to use it well
- Schedule activities your current systems can actually support.
- Prepare stock and staffing ahead of activity nights.
- Use activities to reinforce your strategic identity, not as random buttons.

### Player tooltip
> **Activity Tip:** A “good” event at the wrong time becomes a bad event. Capacity first, promotion second.

---

## 5.9 Landlord actions system

### What it does
Adds active tactical actions with cooldowns and tier gating:
- Choice-based interventions.
- Round-level availability logic.
- Identity/vibe-linked action context.

### How to use it well
- Spend actions where marginal impact is highest (critical rounds, unstable nights).
- Avoid low-impact usage just because the action is available.
- Coordinate with security and staffing state.

### Player tooltip
> **Action Tip:** Cooldown tools are strongest when saved for inflection points, not routine rounds.

---

## 5.10 Event system (random incidents and shocks)

### What it does
Injects environmental volatility:
- Good and bad incident cards/events.
- Operational disruptions.
- Financial/reputation side effects.

### How to use it well
- Build anti-fragility: liquidity buffer, staffing redundancy, and security readiness.
- Expect bad variance and plan for survivability instead of perfect nights.

### Player tooltip
> **Event Tip:** The best event strategy is not prediction—it is resilience.

---

## 5.11 Rumor / sentiment / truth-pressure systems

### What it does
Models narrative momentum around your venue:
- Rumor generation and spread.
- Sentiment drift.
- Truth-pressure dynamics from outcomes.

### How to use it well
- Consistent operations reduce rumor vulnerability over time.
- Sudden quality drops can damage sentiment disproportionately.
- Treat public perception as a managed resource.

### Player tooltip
> **Reputation Tip:** Narrative lag is real—today’s good choices may repair last week’s damage, but not instantly.

---

## 5.12 Pub identity system

### What it does
Tracks your pub’s evolving character profile (e.g., style/vibe archetypes), shaped by weekly behavior and outcomes.

### How to use it well
- Commit to a coherent direction; mixed signals dilute system benefits.
- Align upgrades, activities, pricing, and policy with target identity.

### Player tooltip
> **Identity Tip:** Identity is a multiplier system. Small aligned decisions become large long-term effects.

---

## 5.13 Upgrades, milestones, and pub level progression

### What it does
Provides long-term capability growth via:
- Unlockable upgrades.
- Milestone triggers.
- Pub-level bonuses from acquired power.

### How to use it well
- Prioritize upgrades that remove your current bottleneck.
- Sequence upgrades to support your chosen strategy (throughput, safety, premium, etc.).
- Don’t over-buy future power while current bills are unstable.

### Player tooltip
> **Upgrade Tip:** The best upgrade is the one that fixes the next failure mode—not the flashiest description.

---

## 5.14 Inn/hospitality expansion system

### What it does
Expands gameplay into room operations:
- Inn unlock and tiered room counts.
- Room pricing.
- Reputation/cleanliness interactions.
- Role-supported lodging operations and maintenance pressure.

### How to use it well
- Scale room pricing with demand sensitivity and service quality.
- Keep cleanliness/reputation in healthy bands to avoid compounding penalties.
- Ensure staff support exists before adding large room inventory.

### Player tooltip
> **Inn Tip:** Rooms add revenue potential and management burden at the same time—treat inn growth like opening a second business line.

---

## 5.15 Reports & observation systems

### What they do
Convert simulation outcomes into readable diagnostics:
- Nightly summaries.
- Weekly and multi-week reports.
- Observation strings for quick tactical interpretation.

### How to use them well
- Use nightly data for tactical fixes.
- Use weekly trends for strategic direction changes.
- Compare intent vs. outcomes (what you planned vs. what actually happened).

### Player tooltip
> **Report Tip:** If you only react to single-night spikes, you’ll over-correct. Use trend windows for strategic decisions.

---

## 6) Suggested learning path for new players

1. **Run conservative first week:** moderate pricing, stable stock, basic security.
2. **Learn your cost floor:** identify minimum weekly cash required for wages/rent/debt.
3. **Stabilize staffing and stock discipline:** remove avoidable chaos.
4. **Introduce one strategic lever at a time:** activity plan, identity direction, or inn expansion.
5. **Only then scale:** upgrades, bigger nights, and tighter margin plays.

---

## 7) Practical strategy patterns

### Safe-growth pattern
- Moderate price multiplier.
- Strong staffing coverage.
- Early security investment.
- Slow, debt-light expansion.

### High-risk/high-reward pattern
- Aggressive pricing.
- Heavy event/activity utilization.
- Leverage debt for fast scaling.
- Requires excellent incident control and cash timing discipline.

### Reputation-first pattern
- Service consistency prioritized over short-term margin.
- Identity and sentiment cultivation.
- Steadier long-run demand base.

---

## 8) Common mistakes and fixes

- **Mistake:** Using debt to mask structural losses.
  - **Fix:** Reduce fixed cost burden and rebalance staffing/price.

- **Mistake:** Running activity-heavy nights without prep.
  - **Fix:** Pre-buy stock and reinforce staff/security before scheduling.

- **Mistake:** Ignoring weekly reports when nights seem “fine.”
  - **Fix:** Track trend erosion (cashflow, morale, risk) before crisis appears.

- **Mistake:** Upgrading too fast after one good week.
  - **Fix:** Confirm repeatability over multiple weeks before scaling commitments.

---

## 9) How to run the game locally

From the project root:

```bash
javac *.java
java Main
```

If your environment requires explicit assertions for tests:

```bash
java -ea <TestClassName>
```

---

## 10) Project map (for modders/contributors)

Commonly useful files:
- `Main.java` — app entry point.
- `WineBarGUI.java` — Swing UI, controls, dialogs, HUD.
- `Simulation.java` — main game loop and cross-system orchestration.
- `GameState.java` — persistent runtime state container.
- `GameFactory.java` — new game initialization and starter data.

Major system files (non-exhaustive):
- `EconomySystem.java`, `CreditLineManager.java`, `Bank.java`, `LoanSharkAccount.java`
- `SupplierSystem.java`, `InventorySystem.java`, `WineRack.java`, `FoodRack.java`
- `StaffSystem.java`, `StaffFactory.java`
- `PunterSystem.java`, `Punter.java`
- `SecuritySystem.java`, `SecurityPolicy.java`
- `ActivitySystem.java`, `PubActivity.java`
- `EventSystem.java`, `EventCard.java`
- `RumorSystem.java`, `Rumor*.java`
- `UpgradeSystem.java`, `PubUpgrade.java`
- `MilestoneSystem.java`, `PubLevelSystem.java`, `PrestigeSystem.java`
- `PubIdentitySystem.java`
- `ReportSystem.java`, `ObservationEngine.java`

---

## 11) Final quick-reference checklist (before opening each night)

- [ ] Cash buffer covers near-term obligations.
- [ ] Stock levels match expected traffic.
- [ ] Staff coverage is adequate for tonight’s plan.
- [ ] Security posture matches risk level.
- [ ] Pricing is aligned with current demand/reputation.
- [ ] Activities/actions selected intentionally.
- [ ] You know what metric you are trying to improve tonight.

Good luck, landlord.
