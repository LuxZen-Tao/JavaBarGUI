# Game Tester Playtesting Guide
**JavaBarSim v3 - Quality Assurance Framework**

Generated: 2026-02-16

---

## Overview

This guide provides structured test scenarios for validating JavaBarSim's gameplay, balance, and system integration. Use this for regression testing, balance validation, and exploit discovery.

**Target Audience**: QA testers, balance designers, playtesters

**Test Environment**: Use fresh save or specific save states for reproducibility.

---

## 1. Core Loop Validation Tests

### 1.1 Pricing Effect on Demand
**Objective**: Verify that price changes affect punter behavior as expected.

**Test Steps**:
1. Start new game
2. Open first night with default pricing (priceMultiplier = 1.0)
3. Note bar occupancy and revenue for 3 nights
4. Increase pricing to 1.5x
5. Observe for 3 nights - should see:
   - More refusals (punters turn away)
   - Lower bar occupancy
   - Similar or slightly higher revenue per served punter
6. Decrease pricing to 0.7x
7. Observe for 3 nights - should see:
   - Fewer refusals
   - Higher bar occupancy
   - Lower margin per punter

**Pass Criteria**:
- Pricing directly correlates with refusal rate
- Revenue per punter scales with price
- No game-breaking issues at extreme prices (0.5x or 2.0x)

**Locations to Check**:
- HUD: Bar occupancy changes
- Economy tab: Week revenue trends
- Observation feed: Refusal messages

---

### 1.2 Chaos Escalation
**Objective**: Verify chaos accumulates from unserved punters and triggers incidents.

**Test Steps**:
1. Start new game, hire minimal staff (1-2 people)
2. Open service with low serve capacity
3. Let punters arrive without serving them fully
4. Monitor chaos value in Operations tab
5. Chaos should:
   - Increase each round with unserved punters
   - Trigger incidents around chaos 10-15
   - Show "High Chaos" mood at 15+

**Pass Criteria**:
- Chaos increases predictably from unserved
- Incidents occur above threshold
- Chaos decays slowly when serving well
- Security reduces incident chance

**Locations to Check**:
- Operations tab: Chaos value and breakdown
- Risk & Security tab: Incident history
- Event log: Fight/complaint events

---

### 1.3 Morale Impact on Staff Behavior
**Objective**: Verify staff morale affects serve capacity and staff retention.

**Test Steps**:
1. Start new game, hire 3-4 staff
2. Run normally for 2 weeks, paying wages on time
3. Check Staff tab: Morale should be 60-80 (healthy)
4. Miss wage payment 2 weeks in a row
5. Check Staff tab: Morale should drop to 20-40 range
6. Continue for 1 more week without payment
7. Observe staff quit events and morale collapse

**Pass Criteria**:
- Morale starts healthy
- Missed wages lower morale significantly
- Low morale (<20) triggers quit risk
- Morale recovery when wages paid

**Locations to Check**:
- Staff tab: Morale values (FOH, BOH, team)
- Payday tab: Wage status
- Event log: Staff quit notifications

---

### 1.4 Seasonal Punter Mix Shifts
**Objective**: Verify seasonal effects change punter tier distribution.

**Test Steps**:
1. Start new game
2. Fast-forward to June (Tourist Wave + Exam Season overlap)
3. Check Traffic & Punters tab: Should see "TOURIST WAVE" and "EXAM SEASON" active
4. Check tier mix during service: Should see increased DECENT and BIG_SPENDER arrivals
5. Fast-forward to December (Winter Slump)
6. Check Traffic & Punters tab: Should see "WINTER SLUMP" active
7. Check tier mix: Should see more LOWLIFE, fewer BIG_SPENDER

**Pass Criteria**:
- Seasonal effects display in Traffic & Punters tab
- Tier mix percentages shift as documented:
  - Tourist Wave: Big Spenders +8%, Decent +5%, Lowlife -6%
  - Winter Slump: Lowlife +8%, Decent -4%, Big Spenders -8%

**Locations to Check**:
- Traffic & Punters tab: "Seasonal Effects" section
- HUD Calendar: Verify date matches expected season
- Tier mix line: Count actual punter distribution

---

### 1.5 Rival Influence on Market Pressure
**Objective**: Verify rival pubs affect traffic and punter mix.

**Test Steps**:
1. Start new game
2. Check Pub Progression tab: Note initial "District update"
3. Play through one week
4. At end of week, check Pub Progression tab: Should see new district update
5. Dominant stance affects next week:
   - PRICE_WAR: Traffic down, more Lowlifes
   - QUALITY_PUSH: Traffic down slightly, more Big Spenders
   - LAY_LOW: Neutral
   - CHAOS_RECOVERY: Traffic up slightly
6. Check Traffic & Punters tab: Rival traffic multiplier should match

**Pass Criteria**:
- District update changes weekly
- Rival traffic multiplier visible in Traffic & Punters tab
- Observable effect on traffic and tier mix

**Locations to Check**:
- Pub Progression tab: District update text
- Traffic & Punters tab: Rival traffic line

---

### 1.6 VIP Arc Triggers
**Objective**: Verify VIP regulars gain/lose loyalty and trigger consequences.

**Test Steps**:
1. Start new game
2. Play until VIP regulars appear (2-3 weeks)
3. Check Traffic & Punters tab: VIP Status section should list VIPs with loyalty scores
4. Run high-quality service (low chaos, good pricing) for 3 nights
5. Check VIP loyalty: Should increase
6. Run poor service (high chaos, terrible pricing) for 3 nights
7. Check VIP loyalty: Should decrease
8. If VIP hits ADVOCATE (85+): +4 rep consequence
9. If VIP hits BACKLASH (<15): -6 rep consequence

**Pass Criteria**:
- VIP roster visible in Traffic & Punters tab
- Loyalty changes based on service quality
- ADVOCATE and BACKLASH consequences trigger correctly
- VIP traffic boost visible when active

**Locations to Check**:
- Traffic & Punters tab: VIP Status section
- Reputation & Identity tab: Rep changes from VIP events
- Event log: VIP consequence notifications

---

## 2. Stress Tests

### 2.1 Max Chaos Scenario
**Objective**: Push chaos to extreme levels and verify system stability.

**Test Steps**:
1. Start new game
2. Hire zero staff or fire all staff
3. Open service, let punters arrive
4. Do NOT serve anyone for entire night
5. Monitor chaos value in Operations tab
6. Chaos should hit 30-50+ range
7. Verify game does not crash
8. Check for incident spam in event log
9. Close night and verify recovery possible

**Pass Criteria**:
- Chaos can exceed 30 without crash
- Incidents trigger but do not lock game
- Chaos decays over multiple nights with good service
- No infinite loop or freeze

**Edge Cases to Check**:
- Does chaos cap at a maximum value?
- Can you recover from chaos 50+?
- Do incidents affect cash/reputation correctly?

---

### 2.2 No-Staff Scenario
**Objective**: Verify gameplay with zero staff hired.

**Test Steps**:
1. Start new game, do NOT hire any staff
2. Serve capacity should be 0
3. Attempt to open service
4. Verify punters can arrive but cannot be served
5. Chaos should accumulate rapidly
6. Close night - verify game state remains valid
7. Hire staff next day and verify recovery

**Pass Criteria**:
- Serve capacity correctly shows 0
- Game does not crash with zero staff
- Punters arrive but accumulate as unserved
- Hiring staff immediately restores functionality

**Edge Cases to Check**:
- Can you run multiple nights with zero staff?
- Does morale system handle zero staff gracefully?
- Can you fire mid-service?

---

### 2.3 Price War Extremes
**Objective**: Test extreme pricing scenarios.

**Test Steps**:
1. **Extreme High**: Set priceMultiplier to 3.0x
   - Observe: Almost all punters should refuse
   - Revenue per sale is high but volume is near zero
   - Verify no crash or divide-by-zero
2. **Extreme Low**: Set priceMultiplier to 0.3x
   - Observe: Almost no refusals
   - Bar fills to capacity quickly
   - Revenue per sale is very low but volume is high
   - Verify profitability calculation
3. **Free Pricing**: Set priceMultiplier to 0.0x
   - Observe: Revenue should be zero
   - Verify game handles zero-revenue nights
   - Should trigger economic crisis quickly

**Pass Criteria**:
- No crashes at extreme prices
- Revenue calculation remains correct
- Economic consequences apply logically

**Edge Cases to Check**:
- Negative pricing? (Should be clamped to 0)
- Can you survive on volume at 0.3x pricing?

---

### 2.4 High-Rep vs Low-Rep Contrast
**Objective**: Compare gameplay at opposite reputation extremes.

**Test Steps**:
1. **High-Rep Run (70+)**:
   - Play until reputation is 75+
   - Check traffic multiplier: Should be 1.28x+
   - Tier mix should favor Big Spenders and Decent
   - Observe: Higher revenue, better customers
2. **Low-Rep Run (<-60)**:
   - Manipulate or play poorly until rep is -70
   - Check traffic multiplier: Should be 0.72x or lower
   - Tier mix should favor Lowlifes
   - Observe: Lower traffic, worse customers, more chaos

**Pass Criteria**:
- Reputation clearly affects traffic multiplier
- Tier mix shifts are observable
- High-rep gameplay feels rewarding
- Low-rep gameplay is challenging but not impossible

**Edge Cases to Check**:
- Can you recover from -80 rep?
- Does reputation have a floor/ceiling?

---

### 2.5 Bankruptcy Edge Case
**Objective**: Verify bankruptcy consequences and recovery path.

**Test Steps**:
1. Start new game
2. Take maximum credit lines
3. Spend recklessly, miss payments
4. Trigger bankruptcy (via Declare Bankruptcy button in Finance tab)
5. Verify consequences apply:
   - All upgrades removed
   - Pub level reset to 0
   - Credit score reset to 0
   - Banks locked for period
   - Supplier trust minimum
   - Invoice credit cap = GBP 400
6. Attempt to recover:
   - Play with constraints for 5-10 weeks
   - Verify recovery is possible but difficult

**Pass Criteria**:
- Bankruptcy button works correctly
- All consequences apply as documented
- Recovery path exists
- Game does not become unplayable

**Edge Cases to Check**:
- Can you declare bankruptcy multiple times?
- Do milestone unlocks persist through bankruptcy?

---

### 2.6 Extreme Seasonal Modifiers
**Objective**: Test gameplay during overlapping negative seasons.

**Test Steps**:
1. Fast-forward to a period with no positive seasons (baseline)
2. Check tier mix: Should be balanced
3. Fast-forward to overlapping negative seasons (if any exist)
4. Check tier mix: Should shift significantly
5. Verify traffic and revenue impacts

**Pass Criteria**:
- Seasonal effects stack correctly
- Game remains playable during negative seasons
- Recovery when seasons change

---

## 3. Exploit Testing

### 3.1 Infinite Profit Loops
**Objective**: Search for infinite money exploits.

**Test Scenarios**:
1. **Credit Line Cycling**:
   - Open credit line A
   - Open credit line B
   - Use B to pay off A
   - Check: Does credit score decay prevent this?
   - Pass: Credit utilization and score decay should make this unprofitable

2. **Price Manipulation**:
   - Set price to 0.1x
   - Fill bar to capacity every night
   - Check: Does this generate infinite traffic?
   - Pass: Serve capacity should bottleneck arrivals

3. **Supplier Invoice Delay**:
   - Max out supplier invoice credit
   - Delay payment indefinitely
   - Check: Do late fees compound?
   - Pass: Late fees and trust decay should escalate costs

**Pass Criteria**:
- No infinite money exploits found
- All tested scenarios have economic limits
- Credit score and trust systems provide negative feedback

---

### 3.2 Chaos Suppression Stacking
**Objective**: Test if chaos can be completely eliminated.

**Test Steps**:
1. Max out security upgrades
2. Hire max bouncers at highest quality
3. Install CCTV upgrade
4. Select strictest security policy
5. Install security task if available
6. Run service with deliberately bad pricing (to provoke chaos)
7. Monitor chaos level and incident rate

**Pass Criteria**:
- Chaos can be significantly reduced but not eliminated completely
- Security has diminishing returns
- High security costs are balanced by incident prevention

**Edge Cases to Check**:
- Can you achieve zero incidents for 10+ nights?
- Is the cost sustainable?

---

### 3.3 Reputation Farming Exploits
**Objective**: Test if reputation can be farmed easily.

**Test Scenarios**:
1. **VIP Consequence Farming**:
   - Get VIPs to ADVOCATE status
   - Repeatedly trigger +4 rep events
   - Check: Is VIP roster size limited?
   - Check: Does loyalty decay after consequences?
   - Pass: VIP system should have cooldowns or diminishing returns

2. **Event Manipulation**:
   - Search for events that give easy rep gains
   - Attempt to trigger them repeatedly
   - Pass: Events should have randomness or cooldowns

**Pass Criteria**:
- No easy reputation farming loops
- Reputation gains have costs or risks
- VIP consequences are limited by roster size and loyalty maintenance

---

### 3.4 Upgrade Stacking Imbalance
**Objective**: Test if certain upgrade combinations are overpowered.

**Test Steps**:
1. Identify upgrades that provide similar bonuses
2. Stack as many as possible (e.g., multiple capacity upgrades)
3. Check: Does capacity become unlimited?
4. Check: Do costs scale appropriately?
5. Test specific combos:
   - All capacity upgrades
   - All security upgrades
   - All traffic boost upgrades

**Pass Criteria**:
- Upgrade stacking has diminishing returns
- Cost scales with power level
- No single combo makes game trivial

**Edge Cases to Check**:
- Bankruptcy removes all upgrades - does this make recovery impossible?
- Do some upgrades synergize too strongly?

---

## 4. Regression Checklist

### 4.1 Mission Control Display Accuracy
**Objective**: Verify all Mission Control tabs display correct live data.

**Test Steps**:
1. Open Mission Control dialog
2. For each tab, verify data matches actual game state:
   - **Overview**: Cash, debt, rep, identity, chaos, morale, security
   - **Finance & Banking**: Credit lines, balances, utilization
   - **Payday**: Wage accrual, payment status
   - **Suppliers**: Deals, balances, trust
   - **Pub Progression**: Pub level, milestones, rivals, district update
   - **Security**: Security level, bouncers, policy, task
   - **Staff**: Full roster, morale, levels, shifts
   - **Economy**: Revenue, costs, profit, supplier trust
   - **Operations**: Serve cap, bar cap, traffic mult, chaos, upgrades, activities
   - **Inn**: Rooms, bookings, maintenance (if unlocked)
   - **Risk & Security**: Incidents, fights, refunds, unserved
   - **Reputation & Identity**: Rep, identity drift, narrative
   - **Rumors**: Active rumors, heat, effects
   - **Traffic & Punters**: Traffic breakdown, tier mix, **seasonal effects**, **VIP status**, **rival traffic**
   - **Music**: Profile, effects, tracks
   - **Inventory**: Wine/food stock
   - **Loans**: Credit line summary, shark threat
   - **Log / Events**: Event counts, last between-night
   - **Prestige / Stars**: Star count, legacy bonuses

**Pass Criteria**:
- All displayed values match actual GameState fields
- No null or stale values
- New additions (seasonal, VIP, rival traffic) display correctly

---

### 4.2 Observation Feed Matches Simulation Events
**Objective**: Verify observation feed messages reflect actual game events.

**Test Steps**:
1. Play through a night
2. Read observation line in HUD
3. Cross-reference with:
   - Staff system events (relationships, morale changes)
   - Security incidents in log
   - VIP events
   - Seasonal flavor text
4. Verify messages are accurate and contextual

**Pass Criteria**:
- Observation line updates per round
- Messages match actual events
- No generic or stale observations

---

### 4.3 Weekly Reports Align with Real Numbers
**Objective**: Verify weekly report data matches actual week performance.

**Test Steps**:
1. Track manually during week:
   - Total revenue
   - Total costs
   - Fights count
   - Unserved count
   - Staff events
2. At end of week, compare with weekly report
3. Verify all numbers match

**Pass Criteria**:
- Revenue and costs match tracked values
- Event counts match observations
- No rounding errors or data loss

---

### 4.4 No UI Elements Referencing Null or Stale State
**Objective**: Catch UI bugs from null references or uninitialized state.

**Test Steps**:
1. Perform rapid actions:
   - Open/close dialogs quickly
   - Change tabs in Mission Control rapidly
   - Trigger events while dialogs open
2. Check for:
   - NullPointerException in console
   - "null" displayed in UI text
   - Empty or uninitialized fields
   - UI not updating after state changes

**Pass Criteria**:
- No NPE errors in console
- All UI text displays valid values
- UI updates reflect current state
- Dialogs handle concurrent state changes gracefully

---

## 5. 10-Run Evaluation Framework

### Objective
Execute 10 complete playthroughs with different strategies to identify balance issues, boredom points, and tension curves.

### Run Variants
1. **Aggressive Expansion**: Max credit, rush upgrades, high risk
2. **Conservative Growth**: Minimal debt, slow upgrade path, low risk
3. **High-Rep Focus**: Prioritize reputation over profit
4. **Chaos Management**: Run with low security, high chaos tolerance
5. **Staff-Centric**: Max staff count, morale-focused
6. **Price War**: Extreme low pricing, volume strategy
7. **Premium Service**: Extreme high pricing, quality over volume
8. **Seasonal Opportunist**: Time expansions to favorable seasons
9. **VIP Cultivation**: Focus on VIP loyalty and consequences
10. **Bankruptcy Recovery**: Intentionally bankrupt, then recover

### Metrics to Track Per Run
- **Weeks to profitability**: How long until consistent positive cash flow?
- **Peak traffic multiplier**: Highest combined multiplier achieved
- **Worst chaos event**: Maximum chaos reached and consequences
- **Staff turnover**: Total staff hired/fired/quit over run
- **Credit utilization peak**: Highest utilization percentage
- **Bankruptcy events**: Count of bankruptcy declarations
- **Final pub level**: Pub level at end of run (e.g., week 20)
- **Final reputation**: Rep score at end
- **Final cash**: Cash balance at end
- **Subjective tension**: 1-10 scale, player-reported tension level per week

### Indicators of Balance Collapse
- **Too Easy**: All runs reach profitability by week 3-5 with minimal effort
- **Too Hard**: Majority of runs end in bankruptcy before week 10
- **Dominant Strategy**: All successful runs use same approach
- **Dead Zones**: Specific week ranges where nothing interesting happens

### Indicators of Boredom vs Tension
- **Boredom**: Player reports low tension for 5+ consecutive weeks
- **Tension**: Player reports high tension (7+) with clear risk/reward decisions
- **Optimal**: Tension oscillates between 4-8 across run

### Recommended Framework Schedule
- Run 1: Baseline playthrough, no special strategy
- Runs 2-10: Test each variant
- Compare metrics across all runs
- Identify outliers and balance issues
- Iterate on problem areas

---

## Test Result Reporting Template

### Test Case: [Name]
- **Tester**: [Name]
- **Date**: [YYYY-MM-DD]
- **Build/Commit**: [Git commit hash]
- **Test Result**: ✅ PASS / ❌ FAIL / ⚠️ PARTIAL
- **Notes**: [Observations, edge cases discovered, recommendations]
- **Bugs Found**: [List any bugs discovered]
- **Screenshots**: [Attach if applicable]

---

## Conclusion

This playtesting guide provides a structured framework for validating JavaBarSim's gameplay, balance, and system integration. Use it as a living document - add new test cases as systems evolve.

**Priority Tests for Next QA Pass**:
1. Mission Control regression check (verify new seasonal/VIP/rival data displays)
2. Traffic multiplier stack validation
3. Debt spiral progression stress test
4. Exploit search (credit cycling, rep farming, chaos suppression)

**Estimated Test Time**:
- Core Loop Validation: 2-3 hours
- Stress Tests: 1-2 hours
- Exploit Testing: 2-3 hours
- Regression Checklist: 1 hour
- 10-Run Evaluation: 10-15 hours total (1-1.5 hours per run)

**Total: ~18-25 hours for complete test pass**

Good luck, testers! 🎮
