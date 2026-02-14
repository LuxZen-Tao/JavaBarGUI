# Java Bar Sim v3 — Complete Game Description

## Executive Summary

**Java Bar Sim v3** is a sophisticated pub management simulation that combines tactical night-to-night operations with strategic long-term business planning. You are the owner-operator of a neighborhood pub, making hundreds of interconnected decisions across economy, staffing, customer service, security, reputation, and growth systems.

Built in Java Swing, the game delivers a deep tycoon experience where short-term execution (serving customers through rounds each night) feeds into long-term consequences (weekly bills, reputation shifts, milestone progression, and prestige advancement).

---

## Core Concept

### What You Do
Run a pub night after night, balancing:
- **Operational Execution**: Stock management, staff scheduling, pricing, security
- **Customer Service**: Serve punters before they leave angry or cause trouble
- **Financial Management**: Pay weekly wages, rent, and debt while maintaining cash flow
- **Growth**: Unlock upgrades, activities, and systems through achievement milestones
- **Reputation**: Build a positive narrative through consistent quality service

### Win Condition
There's no single "victory" screen. Success is measured through compounding progress:
- Sustainable positive cash flow
- High reputation and customer satisfaction
- Milestone achievement and system unlocks
- Pub level advancement (5 tiers)
- VIP relationships and identity mastery
- Prestige trajectory and long-term dominance

### Failure States
You can lose through:
- **Bankruptcy**: Cash and credit exhausted
- **Bailiff Seizure**: Repeated rent/wage misses
- **Debt Spiral**: Loan shark penalties become unsustainable
- **Reputation Collapse**: No customers want to visit
- **Staff Exodus**: Complete workforce loss
- **Trading Standards Closure**: 9+ underage service violations in a single week (instant game over)

---

## Game Structure

### Time Cycles

**Nightly Cycle** (6-10 rounds per night):
1. **Prepare**: Check stock, staff, pricing, activities before opening
2. **Serve**: Progress through rounds, serving customers and handling incidents
3. **React**: Use landlord actions, respond to events, manage chaos
4. **Review**: Analyze night report, adjust strategy

**Weekly Cycle** (7 nights = 1 week):
- Week ends trigger **Payday Report**
- Pay wages, rent, supplier invoices, credit interest, loan shark fees
- Review weekly P&L, reputation changes, staff departures
- Evaluate milestones, identity shifts, VIP arcs
- Make strategic decisions on upgrades and expansion

### Progression Arc
- **Nights 1-20**: Survival phase—learn systems, stabilize operations
- **Nights 21-60**: Growth phase—build reputation, unlock capabilities
- **Nights 61+**: Mastery phase—optimize operations, pursue prestige

---

## Core Systems (Deep Dive)

### 1. Economy System
**Purpose**: Track all money flow—revenue, costs, debt, credit

**Revenue Sources**:
- Wine sales (primary income)
- Food sales (requires kitchen unlock)
- Room revenue (requires inn expansion)

**Cost Structure**:
- **Fixed Weekly**: Wages, rent, debt interest
- **Variable**: Stock purchases, activity costs, upgrades
- **Emergency**: Loan shark penalties, bailiff seizures

**Credit System**:
- Multiple credit lines with different terms
- Credit score (0-100) affects borrowing conditions
- Interest accrues weekly on outstanding balances
- Loan shark threat escalates with missed payments

**Key Mechanic**: Cash-first payment priority. When paying bills, game attempts cash first, then draws credit if needed. Over-reliance on credit damages score and invites predatory lending.

---

### 2. Customer (Punter) System
**Purpose**: Generate diverse customers with varied behaviors and spending patterns

**Customer Attributes**:
- **Tier**: Lowlife, Regular, Decent, Big Spender (affects wallet size and trouble probability)
- **Wallet**: Available spending money (tier-dependent range)
- **Trouble Level**: 0 (calm), 1 (rowdy), 2 (menace)
- **State**: CHILL → ROWDY → MENACE (escalates if not served)
- **Descriptors**: CALM, LOUD, FANCY, SHIFTY, etc. (affect chaos contribution)
- **Name**: Generated from name lists (first + last name combinations)

**Behavior Cycle**:
1. Customers arrive based on reputation and demand modifiers
2. Each round, they attempt to purchase (wine/food)
3. Unserved customers become frustrated and escalate state
4. Satisfied customers spend and may stay multiple rounds
5. Customers leave naturally or are forced out (fights/bans)

**Reputation Impact**:
- Unserved customers hurt reputation immediately
- Service quality affects future demand
- Refunds and fights create negative sentiment

---

### 3. VIP System (Named Regulars)
**Purpose**: Create persistent named customers with loyalty arcs and narrative consequences

**VIP Mechanics**:
- **Roster Size**: Maximum 3 VIPs
- **Selection**: Chosen from actual punter names seen in service
- **Archetypes**: 
  - Social Butterfly (prefers SERVICE, EVENTS, CALM)
  - Connoisseur (prefers QUALITY, SERVICE, CALM)
  - Value Seeker (prefers VALUE, SERVICE)
  - Night Owl (prefers EVENTS, VALUE, CALM)

**Loyalty System**:
- Loyalty score 0-100 tracked per VIP
- Each night evaluates VIP satisfaction based on preferences
- Loyalty adjusts ±1 to ±5 points per night

**Arc Stages**:
- **Positive Path**: NEUTRAL (46-64) → WARMING (65-84) → LOYAL (85-100) → ADVOCATE (85+, consequence fires)
- **Negative Path**: NEUTRAL → ANNOYED (31-45) → DISGRUNTLED (16-30) → BACKLASH (0-15, consequence fires)

**Consequences**:
- **Advocate** (loyalty 85+): +5% demand boost, reputation shield, positive rumors
- **Backlash** (loyalty 15-): Reputation penalty, negative rumors, civic pressure

**Strategic Value**: VIPs provide narrative depth and compound reputation effects when cultivated properly.

---

### 4. Staff System
**Purpose**: Manage workforce across multiple roles with quality, morale, and retention

**Roles**:
- **Bartender**: Primary drink service (most critical for throughput)
- **Server**: Food and drink assistance
- **Kitchen**: Food preparation (requires kitchen unlock)
- **Security/Bouncer**: Fight and theft prevention, ID checking (affects Trading Standards)
- **Manager**: Efficiency boost and morale support
- **Marshall**: Inn operations, reduces inn event severity by 25%
- **Duty Manager**: Inn operations, reduces inn event severity by 50%

**Staff Attributes**:
- **Quality**: Individual skill level (affects speed and effectiveness)
- **Type**: Role specialization
- **Wages**: Individual wage cost (paid weekly in aggregate)

**Morale System** (team-wide):
- **Range**: 0-100 (cap affected by Staff Room upgrades)
- **Healthy**: 65+ (stable operations)
- **Critical**: <40 (departure risk, misconduct increase)

**Morale Drivers**:
- **Positive**: Calm nights, timely wage payment, manager presence, low chaos
- **Negative**: High chaos, fights, wage delays, overwork, security incidents

**Retention**: Low morale causes unexpected departures. Staff turnover disrupts service and costs recruitment.

---

### 5. Inventory System
**Purpose**: Manage wine and food stock with capacity, spoilage, and supplier relationships

**Wine Rack**:
- **Base Capacity**: 40 units
- **Expandable**: Via cellar upgrades (+50, +100, etc.)
- **Wine Types**: Cheap, Decent, Premium (different tiers appeal to different customers)
- **Spoilage**: Slow degradation over time

**Food Rack**:
- **Base Capacity**: 20 meals
- **Expandable**: Via kitchen upgrades
- **Food Types**: Burgers, stews, fish & chips, etc.
- **Spoilage**: Faster than wine; requires careful ordering

**Supplier System**:
- **Weekly Deals**: Rotating discounts on bulk orders
- **Trade Credit**: Buy now, pay invoice later (due at payday)
- **Trust Level**: Poor/Fair/Good/Excellent (affects deal quality)
- **Bulk Unlocks**: Milestones unlock x100, x300, x500 order sizes

**Strategic Tension**: Balance between stockouts (lost revenue) and overstock (waste, tied capital).

---

### 6. Pricing System
**Purpose**: Set revenue multiplier affecting margins and customer affordability

**Multiplier Range**: 0.80x to 1.40x base prices

**Customer Response**:
- **Lowlife**: Struggle at 1.20x+, may leave or complain
- **Regular**: Comfortable 1.00-1.25x
- **Decent/Big Spender**: Tolerate 1.30x+ if service matches

**Reputation Interaction**: High reputation supports premium pricing; low reputation limits pricing power.

**VIP Interaction**: VIPs with VALUE preference penalize pricing above 1.10x.

**Risk-Reward**: Higher margins increase profit per transaction but reduce volume and increase complaint risk.

---

### 7. Security System
**Purpose**: Prevent fights, theft, operational chaos, and Trading Standards violations through deterrence and response

**Security Components**:
- **Bouncer Quality**: 0-3 (from upgrades)
- **Security Policy**: RELAXED, STANDARD, VIGILANT, HARDLINE
- **Security Tasks**: 15+ tasks across 4 categories (SOFT, BALANCED, STRICT, staffing)
- **CCTV**: Passive deterrence and theft detection
- **Reinforced Doors**: Break-in prevention
- **Lighting**: Deters sketchy behavior

**Incident Types**:
- **Fights**: Reputation loss, chaos spike, potential injuries
- **Theft**: Cash loss, staff morale damage
- **Break-ins**: Large cash loss, security policy override
- **Underage Service Violations**: Trading Standards penalties (see Trading Standards System)

**Chaos Metric** (0-100):
- **<25**: Calm operations
- **25-60**: Normal risk
- **60+**: High-chaos state (triggers recovery milestone when dropped back to <25)

**Trading Standards Mitigation**:
- Security Level: -5% violations per security level
- Bouncer Quality 1-3: -3% to -8% violations
- Strict Security Policy: -15% violations

**Strategic Value**: Security investment prevents cascading failures. Chaos damages morale, morale damages service, bad service damages reputation. **Most critically**: Good security prevents game-ending Trading Standards violations.

---

### 8. Activities & Programming System
**Purpose**: Schedule events to drive traffic, revenue, and pub identity

**Activity Types** (20+ activities):
- **Social**: Karaoke, Open Mic, Quiz Night (community building)
- **Entertainment**: DJ Night, Live Band Night, Acoustic Set (high energy)
- **Competitive**: Darts Tournament, Pool Tournament (engagement)
- **Specialty**: Charity Night, Brewery Takeover, Wine Tasting, Tasting Menu (premium experiences)
- **Family**: Family Lunch, Sunday Roast (daytime revenue, different demographic)
- **Food-Focused**: Food Service Night, Special Menu (quality emphasis)
- **Cultural**: Poetry Night, Art Show (artsy identity)

**Activity Mechanics**:
- **Cost**: £60-220 depending on scale
- **Crowd Boost**: Attracts 10-40% additional customers
- **Identity Impact**: Repeated activities strengthen pub identity
- **Effectiveness Multipliers**: Pub level (up to 1.25x) and identity alignment (up to 1.60x)
- **Requirements**: Adequate stock, staff, security to handle surge

**Unlock Progression**: Most activities locked behind milestones (rewards operational excellence).

**Strategic Use**: Activities are force multipliers—amplify strengths or expose weaknesses. Don't schedule activities you can't operationally support. Identity-aligned activities are 60% more effective.

---

### 9. Landlord Actions System
**Purpose**: Provide active cooldown abilities for tactical round-level interventions

**Mechanic**: Spend action (limited uses, cooldown timers) during service for immediate effect.

**Action Categories** (15+ actions):
- **Crowd**: "Buy a Round", "Work the Room", "VIP Treatment"
- **Staff**: "Rally the Team", "Comped Meals", "Staff Pep Talk"
- **Security**: "Show of Force", "Smooth Talk", "Tight Door"
- **Marketing**: "Run a Special", "Flash Sale", "Happy Hour"
- **Reputation**: "Plant a Rumor", "Counter Rumor"
- **Rival Actions**: "Sabotage Rival" (risky but can shift market pressure)

**Action Mechanics**:
- **Cost**: Cash cost per use (varies by action)
- **Execution Time**: Most actions resolve over 1-2 rounds
- **Effect Range**: Targeted (single customer/staff), Area (multiple), or Broadcast (whole venue)
- **Cooldown**: Actions have cooldown periods before reuse
- **State Tracking**: PENDING → EXECUTING → RESOLVED

**Tier Unlock**:
- Tier 1: Start
- Tier 2: M8 (Order Restored)
- Tier 3: M12 (Booked Out)
- Tier 4: M17 (Golden Quarter)
- Tier 5: M19 (Headliner Venue)

**Strategic Use**: Save actions for inflection points—critical rounds, chaos spikes, reputation recovery moments.

---

### 10. Pub Identity System
**Purpose**: Track and reward consistent strategic direction through personality archetype

**Identity Types**:
- **Rowdy**: High-energy, tolerates chaos, rough crowd, loud vibe
- **Respectable**: Quality-focused, orderly, professional operation
- **Artsy**: Creative activities, sophisticated crowd, cultural events
- **Underground**: Edgy vibe, music-focused, alternative crowd, late-night energy
- **Family-Friendly**: Daytime focus, food quality, welcoming atmosphere, low chaos
- **Unknown**: No clear identity (default starting state)

**Identity Mechanics**:
- **4-Week Rolling Calculation**: Based on profit margins, refunds, fights, food quality, morale, pricing, activities
- **Dynamic System**: Identity shifts based on recent operational patterns
- **Dominant Identity**: Held for multiple weeks creates streak
- **Cohesion Bonus**: Aligned decisions compound reputation faster

**Benefits**:
- Attracts matching customer segments
- Improves sentiment resilience
- Landlord actions aligned with identity have +12% effectiveness
- Activity effectiveness bonuses up to 1.60x
- VIPs and rivals respond to your identity
- Milestone M9 (Known For Something) rewards 2-week identity hold

**Strategic Value**: Identity is a multiplier system. Random choices produce linear results; aligned choices compound exponentially.

---

### 11. Rumors, Sentiment & Truth System
**Purpose**: Model public perception, word-of-mouth, and narrative momentum

**Rumor Generation**:
- **6 Rumor Topics**: Staff Gossip, Theft, Favoritism, Food Quality, Price Fairness, Safety
- **2 Source Types**: STAFF (internal) or PUNTERS (customer-facing)
- **3 Sentiment Types**: NEGATIVE, NEUTRAL, POSITIVE
- Based on service quality, incidents, morale, activity outcomes
- Spreads through neighborhood affecting future demand
- Featured rumor appears in HUD and weekly reports

**Sentiment Layers**:
- **Short-term**: Weekly rumors (can be volatile)
- **Medium-term**: Reputation score (0-100, slower to change)
- **Long-term**: Truth pressure (actual quality eventually overrides false rumors)

**Truth Pressure Mechanic**:
- Consistent good operations gradually correct negative perception
- Consistent poor operations expose inflated reputation
- Creates lag between action and narrative recognition

**Strategic Insight**: One bad week doesn't doom you; patterns matter. Recovery takes time but truth wins long-term.

---

### 12. Milestone System
**Purpose**: Gate unlocks and progression through 19 achievement milestones across 5 tiers. Also gates pub level progression.

**Pub Level Time-Gating**:
- **Level 0→1**: 2 milestones + 2 weeks minimum
- **Level 1→2**: 5 cumulative milestones + 3 weeks minimum
- **Level 2→3**: 9 cumulative milestones + 4 weeks minimum
- **Level 3→4**: 14 cumulative milestones + 5 weeks minimum
- **Level 4→5**: 20 cumulative milestones + 6 weeks minimum

**Why Time-Gating**: Prevents power-gaming, ensures sustained operational experience at each tier, adds strategic patience requirement. Pub level affects upgrade unlocks and activity effectiveness multipliers (up to 1.25x).

**Tier Structure**:
- **Tier 1** (M1-M4): Survival and basics
- **Tier 2** (M5-M8): Operational mastery
- **Tier 3** (M9-M12): Strategic excellence
- **Tier 4** (M13-M16): Financial discipline
- **Tier 5** (M17-M19): Prestige

**Evaluation Triggers**:
- Night end (streaks, perfect service)
- Week end (aggregate metrics)
- Payday resolution (payment success)
- Reputation change (sentiment shifts)
- Activity scheduling (programming events)
- Supplier orders (bulk purchases)

**Rewards**:
- Activity unlocks (most common)
- Upgrade unlocks (CCTV, Staff Rooms, Door Teams)
- Landlord action tier increases
- Supplier bulk order unlocks
- Cash bonuses (rare; M6 grants £100)

**Strategic Value**: Milestones guide natural progression. They reward operational excellence rather than arbitrary grinding.

---

### 13. Upgrade System
**Purpose**: Purchase permanent capability improvements across infrastructure, security, and efficiency

**Upgrade Categories**:
- **Bar**: Extended Bar, Wine Cellar (+capacity)
- **Kitchen**: Kitchen Base, Upgrades I-III, Equipment, Staffing
- **Security**: CCTV, Reinforced Doors, Alarms, Door Teams
- **Staff**: Staff Rooms I-III (morale cap boost)
- **Attractions**: Pool Table, Darts, TVs, Beer Garden
- **Inn**: Room expansion (3 → 6 → 9 → 12 rooms)

**Gating Mechanisms**:
- **Cost**: £50-1000 (major financial commitment)
- **Pub Level**: Some upgrades require minimum pub level
- **Chain Prerequisites**: Higher tiers require previous tier ownership
- **Milestones**: Certain upgrades locked behind achievements

**Strategic Decisions**: Upgrades are permanent but expensive. Choose upgrades that solve current bottlenecks, not future fantasies.

---

### 14. Inn / Lodging Expansion
**Purpose**: Add secondary revenue stream through room operations

**Room Mechanics**:
- **Pricing**: £30-70 per room (balance occupancy vs revenue)
- **Cleanliness**: 0-100 (decays with use, requires cleaning staff)
- **Maintenance**: £2.60 per room per night upkeep
- **Reputation**: Separate inn reputation affects overall venue perception

**Operational Complexity**:
- Shares staff pool with pub (cleaning staff required)
- Marshalls and Duty Managers reduce inn event severity
- Price volatility (frequent changes) damages inn reputation
- Neglected cleanliness hurts overall reputation
- Adds fixed costs and management burden
- Random events trigger when rooms are booked (see Inn Events System)

**Strategic Value**: Inn provides steady cash flow buffer when managed well, but it's essentially a second business line. Ensure pub is stable first.

---

### 15. Inn Events System
**Purpose**: Add narrative depth and operational challenges to inn operations

**Event Frequency**:
- Based on Inn Reputation
- Low Inn Rep: 30-40% chance per booked room
- High Inn Rep: 5-10% chance per booked room

**Event Tone Distribution**:
- Low Rep: 85% negative, 15% positive
- High Rep: 75% positive, 25% negative

**Positive Events** (10+ types):
- Generous tips, positive reviews, referrals, quiet guests
- Effects: Reputation +2 to +8, sometimes cash bonuses

**Negative Events** (10+ types):
- Room damages, theft, noise complaints, bad reviews
- Effects: Reputation -3 to -12, cash penalties £10-50

**Staff Mitigation**:
- Marshalls: Reduce event severity by 25%
- Duty Managers: Reduce event severity by 50%
- Both significantly reduce negative event impact

**Strategic Implications**: Build inn reputation early to reduce negative event frequency. Hire Marshalls and Duty Managers to mitigate impacts. Bad events compound if inn reputation stays low.

---

### 16. Trading Standards System
**Purpose**: Track regulatory compliance with underage service violations

**Violation Tracking**:
- Violations occur when staff fail ID checks
- Counter resets weekly at payday
- Visible in HUD security badge with warning symbol (⚠️) when violations ≥ 2

**Penalty Tiers**:
- **Tier 1** (2-4 violations): -30 Reputation at week end
- **Tier 2** (5-8 violations): -50 Reputation + £300 fine at week end
- **Tier 3** (9+ violations): **INSTANT GAME OVER** - Trading Standards shuts down your pub

**Mitigation Factors**:
- Security Level: -5% violation chance per security level
- Bouncer Quality:
  - Quality 1: -3% violations
  - Quality 2: -5% violations
  - Quality 3: -8% violations
- Security Policy: Strict door policy reduces violations by ~15%
- CCTV: Additional deterrent effect

**Critical Warnings**:
- Trading Standards violations are THE most dangerous failure state
- Unlike bankruptcy or reputation loss, Tier 3 is instant game over (no recovery possible)
- Violations can accumulate rapidly on high-volume nights
- Low security + high crowd volume = extreme violation risk
- Security investment mandatory for long-term survival

**Strategic Priority**: Trading Standards compliance should be a TOP priority from early game. Security infrastructure pays for itself by preventing £300 fines and game-ending closures.

---

### 17. Seasonal Effects System
**Purpose**: Calendar-based demand modifiers creating realistic annual cycles

**Season Tags**:
- **Tourist Wave** (June-Aug): +Big Spender %, +volume
- **Exam Season** (May-June): Student demographic shift
- **Winter Slump** (Nov-Jan): -volume, pricing pressure
- **Derby Week** (March): Event spike, rowdier crowd

**Effects**:
- Punter tier mix adjustments
- Supplier pricing fluctuations
- Event weighting changes
- Narrative flavor in reports

**Strategic Adaptation**: Anticipate seasonal shifts. Stock up before high seasons, tighten security during rowdy periods, adjust pricing for demand.

---

### 18. Rival System (District Competition)
**Purpose**: Simulate competitive neighborhood bars affecting market conditions

**Rival Stances** (weekly):
- **Price War**: Competitor undercuts, margin pressure
- **Quality Push**: Competitor invests, raises expectations
- **Event Spam**: Heavy programming, splits entertainment crowd
- **Lay Low**: Neutral, no significant impact
- **Chaos Recovery**: Competitor struggling, opportunity window

**Market Pressure Effects**:
- **Traffic Multiplier**: Rivals steal or boost district traffic
- **Punter Mix Bias**: Competition affects customer tier distribution
- **Rumor Sentiment Bias**: District chatter influenced by rivals

**Strategic Response**: Monitor weekly rival update. Sometimes react directly, sometimes stay the course. Competitor weakness periods are growth opportunities.

---

### 19. Reports & Observation System
**Purpose**: Convert raw simulation data into actionable intelligence

**Report Types**:
- **Night Summary**: Post-service metrics (revenue, costs, incidents, service quality)
- **Weekly Report**: 7-night aggregate (P&L, rep delta, staff changes, VIP arcs)
- **4-Week Report**: Long trends (identity, milestone progress, cash flow patterns)
- **Mission Control**: Strategic dashboard (prestige, pub level, district status)

**Observation Feed**:
- Real-time tactical commentary during rounds
- Pattern recognition (e.g., "Chaos spiking—consider security action")
- Cross-system diagnostics

**Strategic Value**: Reports are the integration layer revealing hidden system coupling. Use nightly for tactics, weekly for strategy, 4-week for direction.

---

## Interconnected Systems

### Example: Low Morale Cascade
1. Miss weekly wage payment → staff morale drops 15-25 points
2. Low morale → increased misconduct chance (theft, arguments)
3. Misconduct → service quality drops, chaos increases
4. High chaos → more fights, customer dissatisfaction
5. Customer dissatisfaction → refunds, reputation loss
6. Reputation loss → fewer customers next week
7. Fewer customers → less revenue → harder to pay next week's wages
8. **Spiral continues unless intervention breaks cycle**

### Example: Identity Compounding
1. Schedule social activities (karaoke, quiz night) repeatedly
2. Identity trends toward "Respectable" or "Family-Friendly"
3. This identity attracts REGULAR and DECENT tier customers
4. Use moderate pricing (1.05-1.15x) matching expectations
5. Identity-aligned landlord actions (+12% effectiveness) smooth rough edges
6. VIPs with SERVICE preference thrive in this atmosphere
7. Achieve M9 (Known For Something) milestone
8. Reputation compounds faster with identity cohesion
9. Strong identity buffers negative rumors
10. **Aligned decisions multiply each other's effects**

---

## Learning Curve & Mastery

### Beginner Phase (Nights 1-10)
**Focus**: Understand basic loop and avoid immediate collapse
- Learn to serve customers during rounds
- Understand weekly bill timing
- Grasp reputation-demand connection
- Avoid running out of cash

### Intermediate Phase (Nights 11-40)
**Focus**: System literacy and operational consistency
- Balance stock ordering vs spoilage
- Manage staff morale proactively
- Invest in security before chaos spirals
- Use activities strategically
- Pursue early milestones (M1-M6)

### Advanced Phase (Nights 41-80)
**Focus**: Strategic coherence and identity mastery
- Develop clear pub identity
- Optimize pricing for customer mix
- Cultivate VIP relationships
- Master credit usage (timing, not avoidance)
- Pursue tier 3-4 milestones

### Expert Phase (Nights 80+)
**Focus**: Prestige pursuit and compound optimization
- Run dual operations (pub + inn)
- Navigate rival competition actively
- Leverage seasonal cycles
- Pursue tier 5 milestones
- Maximize prestige trajectory

---

## Design Philosophy

### Interconnected Consequences
No system operates in isolation. Staff morale affects service quality affects reputation affects demand affects revenue affects ability to pay staff. Every decision has ripple effects.

### Delayed Feedback
Many consequences take time to manifest. One bad night doesn't doom you; consistent poor choices create spirals. Similarly, good choices compound slowly.

### Strategic Coherence
Random optimal-per-system choices produce linear results. Aligned choices (identity, activities, pricing, upgrades all supporting same direction) produce exponential compounding.

### Anti-Fragility Rewards
The game rewards building resilience over chasing perfection. Cash buffers, staff redundancy, security investment, and supplier trust all pay off during crisis weeks.

### Depth Over Complexity
Core loop is simple (serve customers, pay bills). Depth emerges from system interactions and strategic choices over time.

---

## Technical Details

### Platform
- **Language**: Java (JDK 17+, uses records)
- **Framework**: Java Swing (no external dependencies)
- **Architecture**: Single-package compilation, all systems in one codebase
- **Lines of Code**: ~35,000+ across 120+ classes
- **Test Coverage**: 8+ comprehensive test suites

### Save System
- Full game state serialization
- Save/load via `GameStatePersistence` and `SaveManager`
- ~700 persistent fields in `GameState` object
- Backward compatible with existing saves

### Performance
- Turn-based, no real-time pressure
- Simulation runs deterministically (seeded random)
- UI updates only on player action

---

## Replayability Factors

1. **Multiple Strategy Paths**: Safe-growth vs high-risk vs reputation-first vs specialist
2. **Milestone Variety**: 19 achievements with different unlock benefits
3. **Identity Options**: 5+ distinct pub personality archetypes
4. **VIP Variability**: Random archetype assignment creates different relationship challenges
5. **Seasonal Cycles**: Annual patterns create rhythm and variety
6. **Rival Dynamics**: District competition adds unpredictability
7. **Event System**: Random incidents ensure no two runs identical

---

## Summary

**Java Bar Sim v3** is a **deep tycoon simulation** where operational excellence, strategic consistency, and system mastery determine success. 

The game rewards:
- Understanding system interconnections
- Making coherent, aligned decisions
- Building anti-fragility through reserves and redundancy
- Learning from trends rather than overreacting to variance
- Balancing short-term survival with long-term growth

Whether pursuing safe expansion, aggressive leverage, reputation cultivation, or niche specialization, the game supports multiple valid paths to success. The challenge lies not in learning individual systems but in mastering how they interact—and using that knowledge to build a thriving pub that can weather any storm.

**Your pub awaits. Make your choices count.**
