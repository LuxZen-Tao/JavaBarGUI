# Driver Mechanics Implementation Summary

## What Was Done

In response to your request to understand the driver mechanics system (Service and Stability drivers), I've created comprehensive documentation that explains:

### 1. What Drivers Are
Drivers are diagnostic indicators displayed in nightly logs that show WHY your pub is succeeding or struggling. They appear as:
- **Service Drivers**: `workload, avgSpeed, quality` - affect service capacity, refunds, and reputation
- **Stability Drivers**: `workload, composure, reliability` - affect chaos, misconduct, and morale

### 2. How the System Works

#### Service Drivers → Service Quality
```
Example: Drivers -> Service: workload 0.27 (-), avgSpeed 57 (+), quality 63 (-)
```

**Workload (demand / capacity)**:
- Below 1.0: Under capacity (good)
- Above 1.0: Overloaded (bad) → reduces effective capacity exponentially
- Formula: `penalty = (workload - 1.0)^1.6`
- Effective capacity reduces by: `capacity / (1 + penalty × 0.85)`

**avgSpeed (20-100)**:
- Calculated from: `20 + (serveCapacity × 8) + (skill × 2)`
- Represents how fast staff can serve customers
- Trade-off: Speed Demons have high speed but risky reputation ranges

**Quality (20-100)**:
- Calculated from: `28 + (skill × 5) + (repMax × 2)`
- Affects refund pressure: `refundMult = (1 + penalty × 0.60) × (1 - qualityRelief)`
- Quality relief: `(avgQuality - 50) / 100`, clamped to -20% to +30%

#### Stability Drivers → Operational Stability
```
Example: Drivers -> Stability: workload 0.27 (-), composure 76 (-), reliability 57 (+)
```

**Composure (20-100)**:
- Calculated from: `20 + (chaosTolerance × 0.9) + (skill × 2)`
- Affects chaos accumulation: `chaosDelta = (penalty × 7.5) × (1 - composureRelief)`
- Composure relief: `(avgComposure - 50) / 100`, clamped to -25% to +35%

**Reliability (20-100)**:
- Calculated from: `25 + (morale × 0.55) + (securityBonus × 8)`
- Affects misconduct: `misconductMult = (1 + penalty × 0.55) × (1 - reliabilityRelief)`
- Reliability relief: `(avgReliability - 50) / 100`, clamped to -20% to +30%

### 3. Symbol Meanings
- **Workload**: (-) = below 1.0 (good), (+) = above 1.0 (overloaded, bad)
- **Performance Metrics**: (-) = at/above 60 (already good), (+) = below 60 (needs improvement)

### 4. How Players Can Influence Drivers

#### Reduce Workload
1. Hire more staff (Trainee, Experienced, Speed Demon bartenders)
2. Hire managers (capacity multipliers: 1.05x-1.35x)
3. Upgrade infrastructure (pub level upgrades)
4. Limit traffic (adjust pricing, control activities)

#### Improve Quality
1. Hire quality-focused staff (Charisma bartenders, Experienced staff)
2. Improve staff skills through level-ups and promotions
3. Train and retain staff long-term

#### Improve Composure
1. Hire experienced staff with higher chaos tolerance
2. Maintain low chaos through security investment
3. Upgrade facilities

#### Improve Reliability
1. **Most Important**: Maintain high morale (morale × 0.55 is the primary driver)
2. Pay wages on time (missing wages = morale collapse)
3. Hire security-focused staff (+8 reliability per security bonus point)
4. Keep chaos low (chaos damages morale → damages reliability)
5. Invest in morale upgrades (Staff Room, morale stability)

### 5. Feedback Loops to Watch

**Virtuous Cycles**:
- Quality → Reputation → Better Customers → Higher Tips → Better Morale → Higher Reliability
- Good Composure → Lower Chaos → Better Morale → Higher Reliability
- Low Workload → Clean Service → Good Morale → Staff Retention

**Death Spirals**:
- High Workload → Poor Service → Low Morale → Staff Quits → Higher Workload
- High Chaos → Low Morale → Poor Reliability → More Misconduct → Higher Chaos
- Low Quality → Refunds → Reputation Loss → Worse Customers → More Fights → More Chaos

## Files Created/Modified

1. **Created**: `UserGuide/DRIVER_MECHANICS_GUIDE.md`
   - Comprehensive 400+ line guide explaining all aspects of driver mechanics
   - Practical examples with analysis and action plans
   - Strategic advice for reading and responding to driver feedback

2. **Modified**: `UserGuide/PLAYER_GUIDE.md`
   - Added reference to Driver Mechanics Guide in staff section
   - Added link in introduction for specialized guides

3. **Modified**: `README.md`
   - Added Driver Mechanics Guide to documentation table

## Key Takeaways for Players

1. **Workload is King**: Keep it between 0.8-1.2 for optimal performance
2. **Quality Prevents Refunds**: Aim for 60+ average quality
3. **Reliability Prevents Misconduct**: Driven by morale—pay wages on time!
4. **Composure Prevents Chaos**: Hire skilled staff, keep chaos low
5. **Monitor Weekly**: Driver trends reveal problems before they become crises
6. **Fix Root Causes**: Don't treat symptoms—address underlying staffing or morale issues

## Example Scenario from Your Question

Your example: `workload 0.27 (-), avgSpeed 57 (+), quality 63 (-)`

**Analysis**:
- Workload 0.27 = only 27% capacity utilization (very under-staffed or low demand)
- avgSpeed 57 (+) = speed below 60, could be improved
- Quality 63 (-) = quality above 60, already good

**What This Means**:
- You have excess capacity (could handle more customers)
- Service speed is moderate but not optimal
- Service quality is good

**Impact on Systems**:
- Low workload = smooth operations, no stress penalties
- Moderate speed = adequate but not exceptional service
- Good quality = reduced refunds, better customer satisfaction

**How Player Can Affect It**:
- Increase traffic (lower prices, schedule activities, improve reputation)
- Improve speed by hiring Speed Demon bartenders or training existing staff
- Quality is already good, no immediate action needed

## Documentation Quality

All formulas and calculations have been verified against the source code in:
- `StaffSystem.java` (lines 70-156)
- `Staff.java` (staff attribute getters)
- `StaffFactory.java` (staff generation)

The documentation accurately reflects the implementation and provides actionable guidance for players.
