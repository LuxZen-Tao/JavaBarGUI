# Driver Mechanics Guide

## What Are Drivers?

**Drivers** are diagnostic indicators that show you why your pub is succeeding or struggling. They appear in the nightly logs and end-of-night report, revealing the underlying forces affecting your service quality and operational stability.

Think of drivers as your pub's vital signs—they tell you what's working and what's breaking down.

---

## The Two Main Driver Categories

### 1. Service Drivers
**Purpose**: Explain service quality and customer satisfaction outcomes

**Format**: `Drivers -> Service: workload X.XX (±), avgSpeed XX (±), quality XX (±)`

**Example**: `Drivers -> Service: workload 0.27 (-), avgSpeed 57 (+), quality 63 (-)`

**What This Tells You**:
- **Workload 0.27 (-)**: Your staff is under-utilized (27% capacity). The (-) means lower workload is good for service
- **avgSpeed 57 (+)**: Your staff's average service speed is 57 (moderate). The (+) means higher speed would worsen service (because speed-focused staff can be risky)
- **Quality 63 (-)**: Your staff's average service quality is 63 (decent). The (-) means higher quality would improve service

### 2. Stability Drivers
**Purpose**: Explain operational chaos, fights, misconduct, and staff morale

**Format**: `Drivers -> Stability: workload X.XX (±), composure XX (±), reliability XX (±)`

**Example**: `Drivers -> Stability: workload 0.27 (-), composure 76 (-), reliability 57 (+)`

**What This Tells You**:
- **Workload 0.27 (-)**: Low workload reduces stress and chaos risk. The (-) means lower workload is good for stability
- **Composure 76 (-)**: Your staff's average composure is 76 (good). The (-) means higher composure would reduce chaos
- **Reliability 57 (+)**: Your staff's average reliability is 57 (moderate). The (+) means higher reliability would reduce misconduct

---

## Understanding the Numbers

### Workload
**Range**: 0.0 to 3.0+ (but typically 0.5 to 1.5)
**Calculation**: `demand / capacity`

- **< 0.8**: Under-staffed, smooth operations
- **0.8 - 1.2**: Optimal range, balanced utilization
- **1.2 - 1.8**: Over-capacity, service starts degrading
- **> 1.8**: Critical overload, cascading failures

**Impact**:
- When workload exceeds 1.0, your effective capacity drops due to pressure
- The penalty follows a power curve: `penalty = (workload - 1.0)^1.6`
- High workload increases refunds, chaos, and staff misconduct risk

### Staff Performance Metrics

#### Average Speed (20-100)
**Formula**: `20 + (serveCapacity × 8) + (skill × 2)`

**Derived From**:
- Staff serve capacity per round
- Staff skill level
- Staff experience and training

**What It Means**:
- **20-40**: Slow service, training required
- **40-60**: Adequate service speed
- **60-80**: Good service speed
- **80-100**: Exceptional speed (often from Speed Demon bartenders)

**Trade-off**: Speed Demon bartenders have high speed but risky reputation ranges (-6 to +2), meaning fast service but potential quality issues.

#### Quality (20-100)
**Formula**: `28 + (skill × 5) + (repMax × 2)`

**Derived From**:
- Staff skill level
- Staff maximum reputation contribution per round
- Staff type and training

**What It Means**:
- **20-40**: Poor quality, refunds likely
- **40-60**: Acceptable quality
- **60-80**: Good quality, customer satisfaction high
- **80-100**: Premium quality service

**Impact on Systems**:
- Quality reduces refund pressure: high quality = fewer refunds
- Quality improvement: `qualityRelief = (avgQuality - 50) / 100`
- Affects refund multiplier: `refundMult = (1 + workload_penalty × 0.60) × (1 - qualityRelief)`

#### Reliability (20-100)
**Formula**: `25 + (morale × 0.55) + (securityBonus × 8)`

**Derived From**:
- Staff morale (0-100)
- Security-focused staff bonuses
- Team stability

**What It Means**:
- **20-40**: Unreliable, misconduct risk high
- **40-60**: Moderately reliable
- **60-80**: Reliable staff
- **80-100**: Highly reliable, professional conduct

**Impact on Systems**:
- Reliability reduces misconduct (theft, poor behavior)
- Reliability improvement: `reliabilityRelief = (avgReliability - 50) / 100`
- Affects misconduct multiplier: `misconductMult = (1 + workload_penalty × 0.55) × (1 - reliabilityRelief)`

#### Composure (20-100)
**Formula**: `20 + (chaosTolerance × 0.9) + (skill × 2)`

**Derived From**:
- Staff chaos tolerance stat
- Staff skill level
- Staff experience

**What It Means**:
- **20-40**: Easily stressed, chaos escalates
- **40-60**: Moderate composure
- **60-80**: Good composure under pressure
- **80-100**: Exceptional calm, chaos resistant

**Impact on Systems**:
- Composure reduces chaos accumulation during busy rounds
- Composure improvement: `composureRelief = (avgComposure - 50) / 100`
- Affects chaos delta: `chaosDelta = (workload_penalty × 7.5) × (1 - composureRelief)`

---

## How Drivers Affect Game Systems

### Impact on Service Quality
**Primary Driver**: Service drivers (workload, avgSpeed, quality)

**Effects**:
1. **Effective Capacity Reduction**
   - When workload > 1.0, actual capacity drops
   - Formula: `effectiveCap = capacity / (1 + penalty × 0.85)`
   - More customers go unserved → reputation loss

2. **Refund Pressure**
   - Quality relief reduces refunds by up to 30%
   - Poor quality (< 50) increases refunds by up to 20%
   - Refund multiplier range: 0.75x to 2.25x

3. **Reputation Impact**
   - Unserved customers: immediate reputation hit
   - Refunds: reputation loss and revenue loss
   - High quality service: positive reputation per round

### Impact on Stability
**Primary Driver**: Stability drivers (workload, composure, reliability)

**Effects**:
1. **Chaos Accumulation**
   - Composure relief reduces chaos by up to 35%
   - Poor composure (< 50) increases chaos by up to 25%
   - Chaos delta range: 0.0 to 15.0 per round

2. **Staff Misconduct**
   - Reliability relief reduces misconduct by up to 30%
   - Poor reliability (< 50) increases misconduct by up to 20%
   - Misconduct multiplier range: 0.75x to 2.0x

3. **Staff Morale**
   - High chaos directly damages morale each round
   - Low morale reduces reliability → more misconduct
   - Creates negative feedback loop if unchecked

### Impact on Weekly Outcomes
**Compounding Effects**:
- Weekly staff morale check: fights → quit risk
- Quit chance scales with morale: 80+ morale = 0.2x risk, <30 morale = 2.0x risk
- Staff departures reduce capacity → higher workload next week

---

## How Players Can Influence Drivers

### Improving Service Drivers

#### Reduce Workload
1. **Hire More Staff**: Most direct solution
   - Trainee Bartenders: cheap, low capacity (1-2 serves/round)
   - Experienced Bartenders: balanced (2-4 serves/round)
   - Speed Demon Bartenders: high capacity (5-8 serves/round) but risky
2. **Hire Managers**: Capacity multipliers
   - Assistant Manager: 1.05x-1.15x capacity bonus
   - Manager: 1.10x-1.35x capacity bonus
3. **Upgrade Infrastructure**:
   - Pub level upgrades: +serve capacity bonus
   - Activities can provide temporary capacity boosts
4. **Limit Traffic**:
   - Adjust pricing to manage demand
   - Schedule activities strategically (they affect traffic)

#### Increase Average Speed
1. **Hire Speed-Focused Staff**:
   - Speed Demon Bartenders (highest serve capacity)
   - Train staff: level-ups increase serve capacity
2. **Improve Staff Skills**:
   - Weekly level-ups (automatic if retained)
   - Promotions every 4 levels
3. **Note**: Speed has trade-offs—Speed Demons have risky rep ranges

#### Increase Quality
1. **Hire Quality-Focused Staff**:
   - Charisma Bartenders: +tip bonus, good rep range (+1 to +5)
   - Experienced staff: better rep contributions
2. **Improve Staff Skills**:
   - Skill affects quality directly
   - Each skill point = +5 quality
3. **Train and Retain**:
   - Keep staff long enough to level up
   - Promote staff for better stats

### Improving Stability Drivers

#### Reduce Workload
(Same as Service drivers—workload affects both)

#### Increase Composure
1. **Hire Experienced Staff**:
   - Higher chaos tolerance stats
   - Better skills = higher composure
2. **Maintain Low Chaos**:
   - Composure calculations use chaos tolerance stat
   - Security Bartenders have higher chaos tolerance
3. **Upgrade Infrastructure**:
   - Better facilities = better staff performance

#### Increase Reliability
1. **Maintain High Morale**:
   - Morale is the primary driver of reliability
   - Each morale point = +0.55 reliability
2. **Pay Wages On Time**:
   - Missing wages: immediate morale collapse
   - Morale < 40: high quit risk
3. **Hire Security-Focused Staff**:
   - Security Bartenders: +1 security bonus
   - Security bonus = +8 reliability per point
4. **Keep Chaos Low**:
   - High chaos damages morale each round
   - Morale damage creates feedback loop
5. **Invest in Morale Upgrades**:
   - Staff Room upgrades: morale stability bonuses
   - Morale stabilization reduces negative delta

---

## Reading Driver Feedback Strategically

### Good Driver Patterns
✅ **Low workload + High quality + High reliability**
- Optimal: well-staffed, quality service, stable operations
- You can afford to scale up or take risks

✅ **Moderate workload (0.8-1.2) + Good composure (60+) + Good reliability (60+)**
- Balanced: efficient utilization without overload
- Maintain this equilibrium

### Warning Patterns
⚠️ **Workload > 1.2 + Any low metric**
- Overload: service degrading, chaos building
- Immediate action: hire staff or reduce traffic

⚠️ **Quality < 50 or Reliability < 50**
- Quality issue: expect refunds and reputation loss
- Reliability issue: expect misconduct and morale damage
- Fix: improve staff quality through hiring or training

⚠️ **Composure < 50 with chaos > 40**
- Chaos spiral: staff can't handle pressure
- Urgent: reduce chaos (security, bouncer, policy changes)

### Critical Patterns
🚨 **Workload > 1.5 + Low composure + Low reliability**
- Crisis: cascading failures imminent
- Multiple systems failing simultaneously
- Emergency response: hire immediately, close early, reduce prices

🚨 **All metrics low (< 50) + high chaos (> 60)**
- Death spiral: quit risk high, reputation tanking
- Survival mode: stabilize before growth
- Consider closing for a week to rebuild

---

## Advanced Driver Analysis

### Feedback Loops to Watch

#### Positive Loops (Virtuous Cycles)
1. **Quality → Reputation → Better Customers → Higher Tips → Better Morale → Higher Reliability**
2. **Good Composure → Lower Chaos → Better Morale → Higher Reliability**
3. **Low Workload → Clean Service → Good Morale → Staff Retention**

#### Negative Loops (Death Spirals)
1. **High Workload → Poor Service → Low Morale → Staff Quits → Higher Workload**
2. **High Chaos → Low Morale → Poor Reliability → More Misconduct → Higher Chaos**
3. **Low Quality → Refunds → Reputation Loss → Worse Customers → More Fights → More Chaos**

### Breaking Negative Loops

**If Trapped in Workload Spiral**:
1. Hire aggressively (even if it hurts cash flow short-term)
2. Use landlord actions to boost capacity temporarily
3. Reduce prices to slow demand until staffed

**If Trapped in Chaos Spiral**:
1. Invest in security (bouncer, CCTV, policy changes)
2. Close early or skip nights if chaos > 70
3. Focus on morale recovery before growth

**If Trapped in Quality Spiral**:
1. Replace low-skill staff with experienced hires
2. Train existing staff (wait for level-ups)
3. Avoid Speed Demons until quality is stable

---

## Practical Examples

### Example 1: Under-Staffed Pub
**Drivers**: `Service: workload 1.45 (+), avgSpeed 55 (+), quality 62 (-)`

**Analysis**:
- Workload 1.45 = 45% over capacity
- Speed and quality are decent but irrelevant—workload is the bottleneck
- Effective capacity is reduced by ~40%

**Action Plan**:
1. Hire 2-3 bartenders immediately
2. Target workload of 0.8-1.0
3. Accept short-term cash strain for long-term stability

### Example 2: Poor Quality Staff
**Drivers**: `Service: workload 0.85 (-), avgSpeed 75 (+), quality 42 (+)`

**Analysis**:
- Workload is fine
- Speed is high (Speed Demons?)
- Quality is poor (< 50) → expect refunds

**Action Plan**:
1. Replace Speed Demons with Experienced or Charisma bartenders
2. Wait for level-ups to improve skill
3. Accept lower capacity for better quality

### Example 3: Chaos and Morale Crisis
**Drivers**: `Stability: workload 1.2 (+), composure 48 (+), reliability 38 (+)`

**Analysis**:
- Moderate overload
- Composure below 50 = chaos amplification
- Reliability below 50 = misconduct risk high
- Likely cause: low morale (morale drives reliability)

**Action Plan**:
1. Check staff morale immediately
2. If morale < 40: emergency intervention
3. Pay wages early if possible
4. Reduce workload to give staff breathing room
5. Invest in security to reduce chaos
6. Fix morale through calm nights and tip bonuses

### Example 4: Optimal Operations
**Drivers**: 
- `Service: workload 0.95 (-), avgSpeed 68 (+), quality 72 (-)`
- `Stability: workload 0.95 (-), composure 74 (-), reliability 71 (+)`

**Analysis**:
- Workload near optimal (0.95)
- All performance metrics in good range (60-80)
- Systems are balanced

**Action Plan**:
1. Maintain current staffing
2. Can afford to scale up slightly
3. Consider strategic investments (activities, upgrades)
4. Monitor for drift—this is your target state

---

## Summary: Key Takeaways

1. **Workload is King**: Keep it between 0.8-1.2 for optimal performance
2. **Quality Prevents Refunds**: Aim for 60+ average quality
3. **Reliability Prevents Misconduct**: Driven by morale—pay wages on time
4. **Composure Prevents Chaos**: Hire skilled staff, keep chaos low
5. **Monitor Weekly**: Driver trends reveal problems before they become crises
6. **Fix Root Causes**: Don't treat symptoms—address underlying staffing or morale issues
7. **Balance Trade-offs**: Speed vs Quality, Capacity vs Cost, Growth vs Stability

**The drivers are your diagnostic dashboard. Learn to read them, and you'll master pub management.**
