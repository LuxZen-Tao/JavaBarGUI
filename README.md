# Java Bar Sim v3 — Complete Player Guide

Java Bar Sim v3 is a **management/tycoon simulation** where you run a neighborhood pub night after night, balancing cash flow, staffing, reputation, risk, and long-term growth. The game runs in Java Swing and combines short tactical decisions (this round, this night) with strategic planning (this week, this quarter).

### What’s new in this build

- Expanded **banking and debt gameplay** with configurable credit lines, invoice timing, trade credit, and loan-shark escalation.
- Deeper **security gameplay** with policy tuning, phased task resolution, and stronger links to morale/reputation outcomes.
- A richer **identity + rumor + truth pipeline** where weekly behavior shapes pub identity, perception, and long-run demand.
- More active **landlord action and activity planning** tools for round-level interventions and scheduled crowd shaping.
- Added **music profile/ambience management** that lets you tune venue tone and indirectly support identity and customer fit.
- Improved **reports/observation outputs** for trend-based diagnosis instead of only single-night reaction.

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

### How it interacts with other systems
- Economy is the backbone for every other decision: staffing, supplier orders, upgrades, and activity scheduling all pull from the same cash pool.
- Reputation, security incidents, and service quality feed demand and therefore revenue, so “non-finance” mistakes quickly become finance problems.
- Weekly reporting closes the loop by showing whether your tactical choices actually improved cash conversion over time.

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

### How it interacts with other systems
- Supplier invoices and payroll timing are major triggers for credit usage, so inventory/staff planning directly changes borrowing pressure.
- Credit score health influences your future flexibility, which affects how aggressively you can pursue upgrades or event-heavy growth.
- Loan-shark pressure compounds when operations are unstable, especially if security failures and poor nights reduce your ability to recover.

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

### How it interacts with other systems
- Pricing and reputation set demand shape, which should drive your stock strategy.
- Activities/events can spike traffic unexpectedly, so inventory planning must be coordinated with scheduling.
- Trade credit and invoice handling connect inventory success directly to your banking/debt risk.

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

### How it interacts with other systems
- Price sensitivity is filtered through punter mix and pub identity; the same multiplier can perform differently under different crowd profiles.
- Poor staffing/security execution reduces willingness to pay, weakening high-price strategies.
- Strong report reading helps you tell apart “good margin” from “temporary margin with hidden demand decay.”

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

### How it interacts with other systems
- Staffing quality affects service speed and customer satisfaction, which feed reputation, rumors, and future demand.
- Wage obligations are one of your largest fixed costs, making staff planning central to debt stability.
- Security policy and incident frequency strongly influence morale/retention, so labor and risk systems must be tuned together.

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

### How it interacts with other systems
- Pricing, activities, and music/ambience all shape who shows up and how they spend.
- Security incidents, wait times, and stockouts alter behavior in-night and influence return likelihood.
- Rumor/sentiment systems echo punter experience into future weeks, creating momentum or drag.

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

### How it interacts with other systems
- Security stabilizes staff morale and customer confidence, protecting both throughput and reputation.
- Activity/event intensity changes risk load, so your security posture should be adjusted in advance.
- Financially, reduced theft/fight losses improve net margins and reduce crisis borrowing.

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

### How it interacts with other systems
- Activities directly pressure inventory, staff throughput, and security, so they should be scheduled as a cross-system plan.
- Successful programming strengthens identity and can improve pricing power over time.
- Failed programming (underprepared nights) hurts sentiment and can create cashflow whiplash.

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

### How it interacts with other systems
- Landlord actions are tactical amplifiers: best used to support a broader plan in economy/security/activity systems.
- Their impact is larger when used at system stress points (cash-tight rounds, morale dips, incident spikes).
- Identity context can make certain actions more synergistic with your current pub direction.

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

### How it interacts with other systems
- Events stress-test your weakest subsystem first; imbalance in cash, stock, labor, or security is quickly exposed.
- Good risk prep turns random shocks into manageable variance rather than cascading failures.
- Event outcomes feed reports, rumors, and reputation, influencing future demand conditions.

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

### How it interacts with other systems
- Service reliability, security outcomes, and activity quality all generate the “raw data” that rumor/sentiment systems interpret.
- Truth-pressure mechanics gradually reward genuine operational improvement and punish cosmetic short-term fixes.
- Perception shifts feed punter demand and can alter how effective your pricing strategy remains.

### Player tooltip
> **Reputation Tip:** Narrative lag is real—today’s good choices may repair last week’s damage, but not instantly.

---

## 5.12 Pub identity system

### What it does
Tracks your pub’s evolving character profile (e.g., style/vibe archetypes), shaped by weekly behavior and outcomes.

### How to use it well
- Commit to a coherent direction; mixed signals dilute system benefits.
- Align upgrades, activities, pricing, and policy with target identity.

### How it interacts with other systems
- Identity is the strategic glue across activities, music profile, pricing posture, and upgrade priorities.
- Cohesion improves downstream systems (demand fit, sentiment resilience, reputation growth).
- Incoherence creates friction where individually good choices fail to compound.

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

### How it interacts with other systems
- Upgrades can shift economics, risk tolerance, and service ceilings simultaneously.
- Milestones/pub-level progression reward sustained system balance, not isolated one-night spikes.
- Prestige trajectory is strongest when identity, operational reliability, and finances are all aligned.

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

### How it interacts with other systems
- Inn operations share budget and labor with pub operations, so overexpansion can starve core service.
- Reputation and cleanliness feedback loops connect inn performance to overall venue perception.
- Room revenue can improve debt resilience when staffing and maintenance are kept stable.

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

### How they interact with other systems
- Reports are the integration layer: they reveal hidden coupling across pricing, staffing, inventory, security, and sentiment.
- Observation outputs help identify whether a problem is local (one subsystem) or systemic (cross-subsystem drift).
- Better diagnostics reduce overreactions and improve long-term consistency.

### Player tooltip
> **Report Tip:** If you only react to single-night spikes, you’ll over-correct. Use trend windows for strategic decisions.

---

## 5.16 Music & ambience profiles

### What it does
Lets you control venue soundtrack/ambience profiles to shape tone and customer fit.

### How to use it well
- Pick profiles that reinforce your current identity and target crowd.
- Avoid constant switching; use music direction as part of a stable strategy.

### How it interacts with other systems
- Music choices support identity coherence and can improve activity night consistency.
- Better crowd fit can indirectly support sentiment and pricing tolerance.

### Player tooltip
> **Music Tip:** Treat music as strategic framing—small per-night effects become meaningful over many weeks.

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

## 11) Suggested future systems/hooks to keep players engaged

If you want to extend replayability without bloating complexity, these hooks fit the current architecture well:

### Seasonal calendar + rotating demand modifiers
- Add monthly/seasonal periods (holidays, sports seasons, tourist waves).
- Shift punter mix, supplier prices, and event probabilities by season.
- **System interaction:** ties directly into `EventSystem`, `PunterSystem`, `SupplierSystem`, and pricing strategy.

### Rival pubs and district competition
- Simulate 2–4 nearby venues competing for similar audience segments.
- Rival quality/price/activity choices can steal or return your demand.
- **System interaction:** extends `PubIdentitySystem`, reputation loops, and weekly reports with market-share context.

### VIP regulars and relationship arcs
- Introduce named regulars with preferences, tolerance thresholds, and loyalty perks.
- Good nights build personal loyalty; repeated failures can create high-impact backlash.
- **System interaction:** couples with staffing quality, music profile, security outcomes, and rumor sentiment.

### Dynamic staff development trees
- Let staff specialize over time (speed, upselling, de-escalation, cleanliness leadership).
- Add optional training costs and temporary productivity dips during training windows.
- **System interaction:** deepens `StaffSystem`, `EconomySystem`, and long-run upgrade planning.

### Black-market / premium supply channel
- High-margin, high-risk supply options with legality/reputation tradeoffs.
- Rare stock can boost premium identity at the cost of event/security exposure.
- **System interaction:** links inventory, security incidents, rumor-truth pressure, and pricing power.

### Neighborhood influence and civic pressure
- Add local council/community pressure meter affected by noise, incidents, and charity events.
- High pressure can trigger fines/restrictions; strong civic standing unlocks bonuses.
- **System interaction:** bridges activities, security policy, events, and landlord actions.

### Live-ops challenge modes
- Weekly/biweekly challenge seeds (e.g., “Inflation Week”, “No-Loan Run”, “Festival Rush”).
- Post run summaries with scorecards for replay motivation.
- **System interaction:** uses existing report and milestone infrastructure with minimal gameplay rewrite.

### Meta-progression contracts
- Offer optional long-term contracts (brewery partnership, sports rights, jazz residency).
- Each contract adds persistent modifiers plus a commitment cost.
- **System interaction:** naturally complements identity, supplier economics, activity scheduling, and prestige goals.

---

## 12) Final quick-reference checklist (before opening each night)

- [ ] Cash buffer covers near-term obligations.
- [ ] Stock levels match expected traffic.
- [ ] Staff coverage is adequate for tonight’s plan.
- [ ] Security posture matches risk level.
- [ ] Pricing is aligned with current demand/reputation.
- [ ] Activities/actions selected intentionally.
- [ ] You know what metric you are trying to improve tonight.

Good luck, landlord.
