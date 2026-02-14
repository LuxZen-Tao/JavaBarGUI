# Java Bar Sim v3 — Complete Player Guide

**Welcome, Landlord!** This guide will help you master the art of running a successful pub in this sophisticated management simulation. Whether you're just starting out or looking to optimize your operations, this guide covers everything you need to know.

**📚 Specialized Guides:**
- **[Driver Mechanics Guide](DRIVER_MECHANICS_GUIDE.md)** — Deep dive into service/stability drivers and staff performance metrics
- **[Game Description](GAME_DESCRIPTION.md)** — Detailed system mechanics and design philosophy
- **[Developer Guide](DEVELOPER_GUIDE.md)** — Technical reference for modders

---

## Table of Contents
1. [Getting Started](#getting-started)
2. [Core Game Loop](#core-game-loop)
3. [Victory and Failure](#victory-and-failure)
4. [Complete System Breakdown](#complete-system-breakdown)
5. [Advanced Strategies](#advanced-strategies)
6. [Tips for Success](#tips-for-success)

---

## Getting Started

### Installation and Running
From the project directory:
```bash
javac *.java
java Main
```

### First Steps
1. **Understand your starting position**: You begin with basic stock, minimal staff, and a small cash reserve
2. **Learn the interface**: The main HUD shows your cash, reputation, chaos level, and time
3. **Start conservative**: Your first few nights should focus on stability, not growth
4. **Watch your weekly bills**: Wages, rent, and debt service are due every week

### Key Resources to Monitor
- **Cash**: Your lifeline. Never let it hit zero.
- **Reputation**: Affects customer volume and quality. Guard it carefully.
- **Chaos**: High chaos causes fights, theft, and staff morale problems.
- **Staff Morale**: Low morale leads to poor service and departures.
- **Inventory**: Balance between stockouts and waste.
- **Trading Standards Violations**: Most dangerous metric—9+ violations = game over.

---

## Core Game Loop

### The Nightly Cycle

#### 1. **Preparation Phase (Before Opening)**
- **Check Stock**: Ensure wine and food inventory matches expected demand
- **Review Staff**: Confirm you have adequate coverage (bartenders, servers, kitchen, security)
- **Set Pricing**: Adjust your price multiplier based on reputation and competition
- **Schedule Activities**: Plan any special events (karaoke, quiz night, etc.)
- **Choose Security Policy**: Match your security stance to expected crowd behavior

#### 2. **Service Phase (While Open)**
- **Advance Rounds**: Progress through the night (typically 6-8 rounds)
- **Monitor Metrics**: Watch unserved customers, fights, refunds, chaos
- **Use Landlord Actions**: Deploy cooldown abilities at critical moments
- **React to Events**: Handle random incidents and security tasks

#### 3. **Closing Phase (Night End)**
- **Review Night Report**: Analyze revenue, costs, incidents, customer satisfaction
- **Note Trends**: Track reputation changes, staff morale shifts, inventory usage
- **Plan Adjustments**: Decide what to change for tomorrow

### The Weekly Cycle

Every 7 nights triggers the **Payday Report**:
- **Bills Due**: Wages, rent, supplier invoices, credit interest, loan shark payments
- **Revenue vs Costs**: Week-long P&L statement
- **Reputation Changes**: Weekly sentiment and rumor effects
- **Staff Changes**: Departures, morale updates, potential new hires
- **Milestone Progress**: Track achievement toward unlocks
- **Strategic Decisions**: Major purchases, upgrades, identity shifts

---

## Victory and Failure

### Success Indicators
There's no single "win" button, but success is measured by:
- **Financial Stability**: Consistent positive cash flow, manageable debt
- **Strong Reputation**: High reputation attracts better customers and supports premium pricing
- **Pub Level**: Progress through 5 tiers via milestones and prestige
- **Unlock Progression**: Access to all activities, upgrades, and advanced systems
- **VIP Advocates**: Named regulars who champion your venue
- **Identity Mastery**: Clear, consistent pub personality that compounds benefits

### Failure Triggers
You can fail through:
- **Bankruptcy**: Cash and credit exhausted, unable to pay critical bills
- **Bailiff Visit**: Repeated rent/wage misses lead to asset seizure
- **Debt Spiral**: Loan shark penalties escalate into unsustainable costs
- **Reputation Collapse**: Sustained negative sentiment drives away all customers
- **Staff Exodus**: Complete loss of workforce due to low morale
- **Trading Standards Closure**: 9+ underage service violations in a single week triggers instant game over

### The Path to Prestige
- **Early Game (Nights 1-20)**: Survive and stabilize
- **Mid Game (Nights 21-60)**: Build reputation and unlock systems
- **Late Game (Nights 60+)**: Master identity, optimize operations, pursue prestige

---

## Complete System Breakdown

### 1. Economy & Cash Flow System

**What It Does:**
Manages all money movement: revenue, costs, debt, credit, cash reserves.

**Key Mechanics:**
- **Revenue**: Customer spending on drinks and food (affected by volume × price × spend behavior)
- **Fixed Costs**: Wages, rent, debt interest (due weekly)
- **Variable Costs**: Stock purchases, activity costs, upgrade investments
- **Credit System**: Draw from credit lines when cash is low; pay interest weekly
- **Credit Score**: Ranges 0-100; affects lending terms and loan shark threat

**How to Master It:**
- Keep 2-3 weeks of operating expenses in cash reserve
- Use credit for timing gaps, not to fund losses
- Monitor credit utilization; staying below 50% keeps score healthy
- Repay credit quickly to minimize interest
- Track weekly P&L trends, not just nightly wins

**Common Mistakes:**
- Borrowing to cover structural losses rather than fixing operations
- Overexpanding before stabilizing cash flow
- Ignoring credit score until it's already damaged

---

### 2. Customer (Punter) System

**What It Does:**
Generates customers with varied behaviors, spending power, and trouble levels.

**Customer Tiers:**
- **Lowlife**: Low wallet (£3-25), high trouble chance (45%), cause chaos
- **Regular**: Moderate wallet (£8-58), normal trouble (30%), core demographic
- **Decent**: Good wallet (£18-108), low trouble (20%), solid spenders
- **Big Spender**: High wallet (£35-175), very low trouble (12%), premium customers

**Behavior Mechanics:**
- **Wallet**: Amount they can spend before leaving
- **State**: CHILL → ROWDY → MENACE (affected by service quality)
- **Descriptors**: Personality traits (CALM, LOUD, FANCY, etc.) affect chaos contribution
- **Leaving**: Natural departures maintain turnover; forced exits (banned/fights) hurt reputation

**How to Master It:**
- Reputation affects which tiers arrive more often
- Higher reputation = more Decent/Big Spender customers
- Unserved customers escalate and may fight or leave angry
- Match your offerings (pricing, activities, vibe) to your dominant customer tier

**VIP System** (Named Regulars):
- 3 VIP slots filled from actual customer names
- Each VIP has an archetype (Social Butterfly, Connoisseur, Value Seeker, Night Owl)
- Loyalty tracks from 0-100 based on their preferences:
  - SERVICE: Want low unserved counts
  - VALUE: Prefer lower pricing
  - CALM: Dislike fights
  - EVENTS: Enjoy activities
  - QUALITY: Value good food service
- **Advocate Status** (loyalty 85+): +5% demand boost, reputation shield, positive rumors
- **Backlash Status** (loyalty 15-): Reputation hit, negative rumors, civic pressure

---

### 3. Staff System

**What It Does:**
Manages hiring, roles, morale, wages, and retention.

**Staff Roles:**
- **Bartenders**: Serve drinks, most important for throughput
- **Servers**: Serve food and assist with service capacity
- **Kitchen Staff**: Prepare food orders
- **Security/Bouncers**: Reduce fight/theft incidents, check IDs (affects Trading Standards)
- **Managers**: Improve efficiency and morale recovery
- **Marshalls**: Inn operations, reduce inn event severity by 25%
- **Duty Managers**: Inn operations, reduce inn event severity by 50%

**Key Metrics:**
- **Quality**: Individual staff skill (affects service speed and quality)
- **Morale**: Team-wide morale (65+ is healthy, <40 causes problems)
- **Coverage**: Match staff count to expected business volume
- **Wages**: Paid weekly; missing payments causes immediate morale collapse

**Morale Drivers:**
- **Positive**: Clean nights, good tips, wage bonuses, manager presence
- **Negative**: High chaos, fights, wage delays, security incidents, overwork

**How to Master It:**
- Hire slightly ahead of growth to avoid service bottlenecks
- Invest in Staff Room upgrades to boost morale cap
- Never miss wages; the morale hit is devastating
- Balance quality vs quantity; one great bartender > two mediocre ones
- Track departure risk; low morale leads to unexpected quits

**📊 Understanding Staff Performance:**
See **[Driver Mechanics Guide](DRIVER_MECHANICS_GUIDE.md)** for a complete breakdown of:
- Service Drivers (workload, avgSpeed, quality) and how they affect refunds and capacity
- Stability Drivers (workload, composure, reliability) and how they affect chaos and misconduct
- How to read driver feedback in nightly logs
- Strategies for optimizing each performance metric

---

### 4. Inventory & Supplier System

**What It Does:**
Manages wine and food stock, spoilage, supplier relationships, and ordering.

**Wine Rack:**
- **Capacity**: Base 40, expandable via upgrades
- **Spoilage**: Wine ages slowly; old stock decreases quality
- **Variety**: Different wine tiers (cheap to premium) appeal to different customers

**Food Rack:**
- **Capacity**: Base 20, expandable via kitchen upgrades
- **Spoilage**: Food spoils faster than wine
- **Meals**: Burgers, stews, fish & chips, etc.; quality affects customer satisfaction

**Supplier System:**
- **Deals**: Weekly rotating deals (discounts on bulk orders)
- **Trade Credit**: Buy now, pay on invoice later (increases weekly bills)
- **Trust Level**: Affected by payment history; good trust unlocks better deals
- **Bulk Unlocks**: Milestones unlock larger order sizes (x100, x300, x500)

**How to Master It:**
- Order based on expected demand, not max capacity
- Use trade credit strategically for timing, not avoidance
- Watch spoilage; buy fresh before events, not days early
- Build supplier trust; it pays off in crisis weeks
- Take advantage of deals when they match your needs

---

### 5. Pricing System

**What It Does:**
Sets your price multiplier (0.80x to 1.40x base prices), affecting margins and customer reactions.

**Mechanics:**
- **Low Pricing** (0.80-0.95x): Attracts volume, thin margins, reputation-neutral
- **Normal Pricing** (1.00-1.15x): Balanced approach, most stable
- **Premium Pricing** (1.20-1.40x): High margins, risks complaints if service doesn't match

**Affordability:**
- Lowlife customers struggle at 1.20x+
- Regular customers comfortable at 1.00-1.25x
- Decent/Big Spender customers tolerate 1.30x+
- VIPs with VALUE preference penalize pricing above 1.10x

**How to Master It:**
- Match pricing to reputation; high rep supports higher prices
- Adjust based on customer tier mix
- Premium pricing requires premium execution (good staffing, low chaos, events)
- Test increases gradually; sudden jumps damage sentiment
- Consider VIP preferences if you have VALUE-focused regulars

---

### 6. Security System

**What It Does:**
Controls fight/theft prevention, bouncer quality, surveillance, incident response, and Trading Standards compliance.

**Security Components:**
- **Bouncer Quality**: Base 0-3 (from upgrades), affects intervention success and ID checking
- **Security Policy**: Stance from RELAXED to HARDLINE (affects deterrence vs atmosphere)
- **Security Tasks**: 15+ task types across 4 categories (SOFT, BALANCED, STRICT, staffing)
- **CCTV**: Improves theft detection and deterrence
- **Reinforced Doors**: Reduces break-in risk
- **Lighting**: Deters sketchy behavior

**Policy Options:**
- **Relaxed**: Low deterrence, good atmosphere, higher incident risk, poor ID checking
- **Standard**: Balanced approach, suitable for most nights
- **Vigilant**: Higher deterrence, slight atmosphere cost, better ID checking
- **Hardline**: Maximum deterrence, best ID checking, may feel oppressive

**Incident Types:**
- **Fights**: Reputation hit, potential injuries, chaos spike
- **Theft**: Cash loss, staff morale hit
- **Refunds**: Customer dissatisfaction, revenue loss
- **Underage Service Violations**: Trading Standards penalties (see section 15)

**How to Master It:**
- Invest in security early; reactive spending is less efficient
- Match policy to crowd profile (rough crowd = tighter policy)
- Bouncer quality scales better than policy alone
- Security stabilizes staff morale by preventing chaos
- CCTV provides passive benefits with no atmosphere cost
- **CRITICAL**: Good security prevents Trading Standards violations (game-ending at 9+)

---

### 7. Activities & Event Programming

**What It Does:**
Scheduled events (karaoke, quiz nights, DJ sets, etc.) to drive traffic and shape identity.

**Activity Categories:**
- **Social**: Karaoke, Open Mic, Quiz Night (community vibe)
- **Entertainment**: DJ Night, Live Band Night, Acoustic Set (energy and volume)
- **Competitive**: Darts Tournament, Pool Tournament (engagement)
- **Specialty**: Charity Night, Brewery Takeover, Wine Tasting, Tasting Menu (premium experiences)
- **Family**: Family Lunch, Sunday Roast (daytime revenue, different demographic)
- **Food-Focused**: Food Service Night, Special Menu (quality emphasis)
- **Cultural**: Poetry Night, Art Show (artsy identity)

**Mechanics:**
- **Cost**: £60-220 depending on scale
- **Crowd Boost**: Attracts 10-40% additional customers
- **Identity Reinforcement**: Repeated activities strengthen pub identity
- **Effectiveness Multipliers**: Pub level (up to 1.25x) and identity alignment (up to 1.60x)
- **Prep Requirements**: Need adequate staff, stock, security for the crowd surge

**How to Master It:**
- Schedule activities you can actually support operationally
- Prepare extra stock and staff before activity nights
- Use activities strategically to build identity
- Don't over-schedule; budget and bandwidth matter
- Track which activities work for your crowd
- Identity-aligned activities are 60% more effective

**Activity Unlocks:**
- M1 (Open For Business): Karaoke, Open Mic
- M2 (No Empty Shelves): Cocktail Promo
- M4 (Payroll Guardian): Quiz Night, Food Service Night
- M5 (Calm House): DJ Night, Live Band Night
- M9 (Known For Something): Charity Night, Wine Tasting
- M10 (Mixed Crowd Whisperer): Family Lunch, Sunday Roast
- M11 (Narrative Recovery): Brewery Takeover, Tasting Menu
- Additional unlocks via upgrades (Pool Table → Pool Tournament, etc.)

---

### 8. Landlord Actions

**What It Does:**
Active cooldown abilities you can play during service rounds for immediate impact.

**Action Tiers** (unlock via milestones):
- **Tier 1**: Basic actions available from start
- **Tier 2**: M8 (Order Restored)
- **Tier 3**: M12 (Booked Out)
- **Tier 4**: M17 (Golden Quarter)
- **Tier 5**: M19 (Headliner Venue)

**Action Types** (15+ actions):
- **Crowd Management**: "Buy a Round", "Work the Room", "VIP Treatment"
- **Staff Support**: "Rally the Team", "Comped Meals", "Staff Pep Talk"
- **Security**: "Show of Force", "Smooth Talk", "Tight Door"
- **Marketing**: "Run a Special", "Flash Sale", "Happy Hour"
- **Reputation**: "Plant a Rumor", "Counter Rumor"
- **Rival Actions**: "Sabotage Rival" (risky but can shift market pressure)

**Action Mechanics:**
- **Cost**: Cash cost per use (varies by action)
- **Execution Time**: Most actions resolve over 1-2 rounds
- **Effect Range**: Targeted (single customer/staff), Area (multiple), or Broadcast (whole venue)
- **Cooldown**: Actions have cooldown periods before reuse
- **State Tracking**: PENDING → EXECUTING → RESOLVED

**How to Master It:**
- Save actions for high-leverage moments (critical rounds, chaos spikes)
- Coordinate with security and staff state
- Identity-aligned actions provide 12% better outcomes
- Don't waste cooldowns on stable nights
- Higher tiers unlock actions with stronger effects

---

### 9. Pub Identity System

**What It Does:**
Tracks your pub's personality based on weekly behavior patterns.

**Identity Categories:**
- **Rowdy**: High-energy, tolerates chaos, rough crowd, loud vibe
- **Respectable**: Quality-focused, orderly, professional operation
- **Artsy**: Creative activities, sophisticated crowd, cultural events
- **Underground**: Edgy vibe, music-focused, alternative crowd, late-night energy
- **Family-Friendly**: Daytime focus, food quality, welcoming atmosphere, low chaos
- **Unknown**: No clear identity (default starting state)

**Identity Calculation:**
- Based on 4-week rolling history of behavior
- Tracks: profit margins, refunds, fights, food quality, morale, pricing, activities
- Dynamic system—identity shifts based on recent operational patterns

**Identity Benefits:**
- **Cohesion Bonus**: Aligned choices compound reputation faster
- **Customer Fit**: Identity attracts matching customer segments
- **Sentiment Resilience**: Strong identity buffers negative rumors
- **Action Synergy**: Landlord actions aligned with identity work better (12% bonus)
- **NPC Reactions**: VIPs and rivals respond to your identity

**How to Build Identity:**
- Make consistent choices across pricing, activities, music, upgrades
- Hold dominant identity for 2+ weeks to trigger benefits
- Don't chase all demographics; specialize intentionally
- Use activities and music profiles to reinforce chosen identity

**Milestone: M9 (Known For Something)** unlocks at 2-week identity streak.

---

### 10. Rumors, Sentiment & Truth System

**What It Does:**
Models public perception through word-of-mouth, online reviews, and narrative momentum.

**Rumor Generation:**
- **6 Rumor Topics**: Staff Gossip, Theft, Favoritism, Food Quality, Price Fairness, Safety
- **2 Source Types**: STAFF (internal) or PUNTERS (customer-facing)
- **3 Sentiment Types**: NEGATIVE, NEUTRAL, or POSITIVE
- Based on service quality, incidents, staff morale, activity success
- Spreads through neighborhood affecting future demand
- Featured rumor appears in HUD and weekly reports

**Sentiment Mechanics:**
- Current sentiment affects customer arrival volume and tier mix
- Positive sentiment attracts better customers
- Negative sentiment can spiral if not addressed
- Truth-pressure: Real operational improvement gradually overrides false rumors

**Truth vs Perception:**
- **Short-term**: Rumors can damage or boost faster than deserved
- **Long-term**: Actual quality wins; truth pressure corrects perception
- **Recovery**: Consistent good nights eventually fix negative narrative

**How to Master It:**
- Treat reputation as a managed resource
- One bad week doesn't doom you; trends matter
- Address root causes (service, security, morale) not just symptoms
- VIP advocates provide rumor shielding
- Recovery takes time; don't overreact to single-night reputation hits

---

### 11. Milestone System

**What It Does:**
19 milestones across 5 tiers that unlock capabilities and reward achievement. Also gates pub level progression via time-gated requirements.

**Pub Level Progression** (Time-Gated):
- **Level 0→1**: 2 milestones + 2 weeks minimum
- **Level 1→2**: 5 cumulative milestones + 3 weeks minimum
- **Level 2→3**: 9 cumulative milestones + 4 weeks minimum
- **Level 3→4**: 14 cumulative milestones + 5 weeks minimum
- **Level 4→5**: 20 cumulative milestones + 6 weeks minimum

**Why Time-Gating Matters:**
- Prevents power-gaming by forcing strategic patience
- Ensures you experience full system complexity at each tier
- Milestone achievements alone aren't enough—must prove sustained operation
- Pub level affects upgrade unlocks, activity effectiveness (up to 1.25x), and prestige

**All Milestones:**

**Tier 1 (Survival & Basics)**
- **M1: Open For Business** - Survive 3 nights → Unlocks Karaoke
- **M2: No Empty Shelves** - 2 consecutive nights no stockouts → Unlocks Cocktail Promo
- **M3: No One Leaves Angry** - Perfect night (0 refunds, 0 unserved) → Unlocks Staff Room II
- **M4: Payroll Guardian** - Pay wages + rent on time → Unlocks Quiz Night

**Tier 2 (Operational Mastery)**
- **M5: Calm House** - 3 calm nights running activity → Unlocks Open Mic
- **M6: Margin With Manners** - Avg price ≥1.15 + positive rep → Unlocks CCTV + £100 cash
- **M7: Crew That Stays** - 2 weeks no departures + morale 65+ → Unlocks Staff Room III
- **M8: Order Restored** - Recover from chaos >60 to <25 in 2 nights → Unlocks Landlord Tier 2

**Tier 3 (Strategic Excellence)**
- **M9: Known For Something** - Hold dominant identity 2 weeks → Unlocks Charity Night
- **M10: Mixed Crowd Whisperer** - 3 different activities, no collapse → Unlocks Family Lunch
- **M11: Narrative Recovery** - Recover from negative rumor week → Unlocks Brewery Takeover
- **M12: Booked Out** - 3 near-capacity nights in one week → Unlocks Landlord Tier 3

**Tier 4 (Financial Discipline)**
- **M13: Bridge Don't Bleed** - Use credit, clear same week, no misses → Supplier bulk x100
- **M14: Debt Diet** - 3 consecutive zero-debt weeks → Supplier bulk x300
- **M15: Balanced Books Busy House** - Profit £250+ with high wages/security → Door Team II
- **M16: Supplier's Favourite** - Good trust + large bulk order → Premium supplier catalog

**Tier 5 (Prestige)**
- **M17: Golden Quarter** - 4 strong weeks in a row → Landlord Tier 4
- **M18: Stormproof Operator** - Profitable week + positive rep + 3 negative events → Door Team III
- **M19: Headliner Venue** - Premium pricing + rep 75+ + top activity → Landlord Tier 5 + bulk x500

**How to Master Milestones:**
- Check progress regularly in milestone report
- Some milestones trigger others (completing M8 helps toward M17)
- Don't force milestones; pursue operational excellence naturally
- Milestone unlocks often solve your next bottleneck

---

### 12. Upgrade System

**What It Does:**
Purchase permanent improvements to capacity, efficiency, and capabilities.

**Upgrade Categories:**
- **Bar Infrastructure**: Extended Bar, Wine Cellar, Fridge Extensions
- **Kitchen**: Kitchen Base, Kitchen Upgrades I-III, Equipment, Staffing
- **Security**: CCTV, Reinforced Doors, Burglar Alarms, Door Teams
- **Staff**: Staff Rooms I-III (morale cap increases)
- **Attractions**: Pool Table, Darts, TVs, Beer Garden
- **Rooms**: Inn expansion (additional revenue stream)

**Upgrade Gating:**
- **Pub Level**: Some upgrades require minimum pub level
- **Chain Progression**: Many upgrades require previous tier (e.g., Kitchen II needs Kitchen I)
- **Milestones**: CCTV needs M6, Door Team II needs M15, etc.
- **Cost**: Upgrades range £50-1000; plan major purchases carefully

**How to Master Upgrades:**
- Buy upgrades that solve current bottlenecks, not future fantasies
- Extended Bar / Wine Cellar solve capacity issues
- Kitchen unlocks food revenue but adds complexity
- Security upgrades pay off through incident prevention
- Staff Room upgrades increase morale ceiling (critical for retention)

---

### 13. Inn / Lodging System

**What It Does:**
Unlockable room operations providing additional nightly revenue.

**Mechanics:**
- **Room Tiers**: Start with 3 rooms, expand to 6, 9, 12
- **Pricing**: £30-70 per room; balance occupancy vs revenue
- **Cleanliness**: Decays with use; low cleanliness hurts reputation
- **Maintenance**: Costs £2.60 per room per night
- **Staffing**: Requires cleaning staff (shares labor pool with pub)

**Inn Reputation:**
- Separate from pub reputation but influences it
- Clean rooms boost overall venue reputation
- Neglected rooms damage overall reputation
- Price volatility (frequent changes) hurts inn reputation

**How to Master Inn:**
- Don't expand rooms faster than you can staff them
- Balance room pricing with occupancy (too high = empty rooms)
- Keep cleanliness above 75% minimum
- Inn is a second business line; ensure pub is stable first
- Good inn operations provide steady cash flow buffer
- Employ Marshalls and Duty Managers to reduce event severity

---

### 14. Inn Events System

**What It Does:**
Random events trigger when rooms are booked, creating narrative moments and operational challenges.

**Event Mechanics:**
- **Frequency**: Based on Inn Reputation
  - Low Inn Rep: 30-40% chance per booked room
  - High Inn Rep: 5-10% chance per booked room
- **Event Tone**: Based on Inn Reputation
  - Low Rep: 85% negative, 15% positive
  - High Rep: 75% positive, 25% negative

**Event Types:**
- **Positive Events** (10+ types): Generous tips, positive reviews, referrals, quiet guests → Reputation +2 to +8, sometimes cash bonuses
- **Negative Events** (10+ types): Damages, theft, noise complaints, bad reviews → Reputation -3 to -12, cash penalties £10-50

**Staff Mitigation:**
- **Marshalls**: Reduce event severity by 25%
- **Duty Managers**: Reduce event severity by 50%
- Both reduce negative event impact significantly

**How to Master It:**
- Build inn reputation early to reduce negative event frequency
- Hire Marshalls and Duty Managers to mitigate event impacts
- Monitor events in night reports
- Bad events compound if inn reputation stays low
- Good events can boost overall venue reputation

---

### 15. Trading Standards System

**What It Does:**
Tracks underage service violations with escalating penalties, including potential game-ending consequences.

**Violation Tracking:**
- Violations occur when staff fail to check IDs properly
- Counter resets weekly at payday
- Visible in HUD security badge with warning symbol (⚠️) when violations ≥ 2

**Penalty Tiers:**
- **Tier 1** (2-4 violations): -30 Reputation penalty at week end
- **Tier 2** (5-8 violations): -50 Reputation + £300 fine at week end
- **Tier 3** (9+ violations): **GAME OVER** - Trading Standards shuts down your pub

**Mitigation Factors:**
- **Security Level**: Each point reduces violation chance by 5%
- **Bouncer Quality**: 
  - Quality 1: -3% violations
  - Quality 2: -5% violations
  - Quality 3: -8% violations
- **Security Policy**: Strict door policy reduces violations by ~15%
- **CCTV**: Additional deterrent effect

**How to Master It:**
- Invest in security infrastructure early
- Use VIGILANT or HARDLINE security policy on busy nights
- Hire quality bouncers (Quality 2-3)
- Monitor violation count in HUD
- Never let violations reach 7+ (approaching game over)
- Security investment pays for itself by avoiding £300 fines

**Critical Warnings:**
- Trading Standards violations are THE most dangerous failure state
- Unlike bankruptcy or reputation loss, Tier 3 is instant game over
- Violations can accumulate faster on high-volume nights
- Low security + high crowd = high violation risk

---

### 16. Music & Ambience System

**What It Does:**
Sets venue soundtrack/vibe to influence atmosphere and customer fit.

**Music Profiles:**
- Profiles stored in `MusicProfiles/` directory
- Each profile has mood tags and identity alignment
- Background music affects customer comfort and fit

**How It Helps:**
- Reinforces pub identity
- Improves crowd fit for target demographic
- Small per-night effects compound over weeks
- Coordinating music + activities + pricing creates cohesive experience

**How to Master It:**
- Choose profile matching your identity goal
- Don't switch constantly; consistency matters
- Use as strategic framing, not just decoration

---

### 17. Seasonal Effects System

**What It Does:**
Calendar-based demand modifiers affecting customer mix and behavior.

**Season Tags:**
- **Tourist Wave** (June-Aug): Higher Big Spender %, increased volume
- **Exam Season** (May-June): Student demographic shift
- **Winter Slump** (Nov-Jan): Lower volume, pricing pressure
- **Derby Week** (March): Event-driven spike, rowdier crowd

**Effects:**
- Punter tier mix adjustments
- Supplier pricing fluctuations
- Event weighting changes
- Subtle narrative flavor in reports

**How to Master It:**
- Anticipate seasonal shifts in advance
- Stock up before high seasons
- Tighten security during rowdy seasons
- Adjust pricing based on seasonal demand

---

### 18. Rival System (District Competition)

**What It Does:**
Simulates competing bars in your neighborhood affecting your business.

**Rival Stances:**
- **Price War**: Competitor lowers prices, pressure on your margins
- **Quality Push**: Competitor invests in service, raises expectations
- **Event Spam**: Heavy activity scheduling, splits entertainment-seeking crowd
- **Lay Low**: Competitor quiet, neutral impact
- **Chaos Recovery**: Competitor dealing with own problems, opportunity for you

**Market Pressure Effects:**
- **Demand Traffic Multiplier**: Rivals can steal or boost overall district traffic
- **Punter Mix Bias**: Competition affects which customer tiers go where
- **Rumor Sentiment Bias**: District chatter influenced by competitor performance

**How to Master It:**
- Monitor weekly district update in Mission Control
- Adjust strategy based on dominant rival stance
- Use competitor weakness periods to gain market share
- Don't always react directly; sometimes staying course wins

---

### 19. Reports & Observation System

**What It Does:**
Converts simulation outcomes into readable diagnostics and trend analysis.

**Report Types:**
- **Night Summary**: Immediate post-night metrics (revenue, incidents, service quality)
- **Weekly Report**: 7-night aggregate (P&L, reputation delta, staff changes)
- **4-Week Report**: Long-term trends (identity shifts, milestone progress)
- **Mission Control**: Strategic dashboard (prestige, pub level, VIP status)

**Observation Feed:**
- Real-time tactical commentary during service
- Quick diagnosis strings (e.g., "Chaos spiking", "Staff morale low")
- Pattern recognition for cross-system issues

**How to Master It:**
- Use nightly reports for tactical adjustments
- Use weekly reports for strategic direction
- Compare intent vs outcome (what you planned vs what happened)
- Trends matter more than single-night spikes
- Reports reveal hidden system coupling (e.g., low morale → high refunds → reputation loss)

---

## Advanced Strategies

### Strategy 1: Safe Growth Pattern
**Philosophy**: Minimize risk, compound slowly, never collapse.

**Approach:**
- Moderate pricing (1.00-1.15x)
- Strong staff coverage (slightly overstaffed)
- Early security investment
- Slow, debt-light expansion
- Build reputation through consistency

**Milestones to Target**: M3, M4, M7, M14, M16
**Works Well For**: New players, risk-averse playstyle

---

### Strategy 2: High-Risk / High-Reward
**Philosophy**: Aggressive growth funded by leverage and tight margins.

**Approach:**
- Premium pricing (1.25-1.40x)
- Heavy activity scheduling
- Use credit for fast scaling
- Pursue high-turnover, high-margin nights
- Requires excellent incident control

**Milestones to Target**: M6, M8, M12, M19
**Works Well For**: Experienced players, optimal-play seekers

---

### Strategy 3: Reputation-First
**Philosophy**: Service consistency above short-term profit.

**Approach:**
- Price at or below market (0.95-1.10x)
- Over-invest in staffing and security
- Identity cultivation through activities
- Steady long-run demand growth
- Premium pricing becomes viable later

**Milestones to Target**: M3, M5, M9, M11
**Works Well For**: Players who enjoy narrative and customer relationships

---

### Strategy 4: Specialist Venue
**Philosophy**: Commit to a niche identity and dominate it.

**Approach:**
- Choose identity early (e.g., Sports Bar, Premium Venue)
- All decisions reinforce that identity
- Attract narrow but loyal customer base
- Upgrades and activities align with theme
- VIP system particularly strong here

**Milestones to Target**: M9, M10, M17
**Works Well For**: Focused, thematic players

---

## Tips for Success

### Early Game (Nights 1-20)
1. **Don't expand too fast**: Survive first, grow later
2. **Watch weekly bills**: Know your cost floor
3. **Build cash buffer**: Target 2-3 weeks operating expense reserve
4. **Stabilize staff**: Hire adequate coverage, protect morale
5. **Learn your customers**: Understand tier behaviors
6. **Invest in security early**: Prevents Trading Standards violations (game-ending at 9+)

### Mid Game (Nights 21-60)
1. **Establish identity**: Choose direction and commit
2. **Unlock strategically**: Pursue milestones that solve bottlenecks
3. **Manage debt carefully**: Use credit for timing, not losses
4. **Invest in security**: Chaos prevention and Trading Standards compliance pay compound returns
5. **Track trends**: Weekly reports > nightly wins
6. **Monitor Trading Standards**: Keep violations below 5 to avoid major penalties

### Late Game (Nights 60+)
1. **Optimize operations**: Fine-tune pricing, staffing, stock
2. **Pursue prestige**: Target tier 4-5 milestones
3. **Master VIPs**: Cultivate advocate relationships
4. **Balance expansion**: Inn + pub operations simultaneously
5. **Weather storms**: Resilience matters more than perfection

### Universal Principles
- **Cash is king**: Never sacrifice liquidity for growth
- **Reputation compounds**: Protect it aggressively
- **Systems interconnect**: Poor staffing causes security issues causes reputation loss
- **Trends > spikes**: One bad night isn't doom; patterns matter
- **Identity multiplies**: Aligned decisions compound exponentially
- **Trading Standards is game-ending**: 9+ violations = instant game over; prioritize security investment

### Common Pitfalls to Avoid
1. **Debt spiral**: Borrowing to cover losses instead of fixing operations
2. **Over-scheduling activities**: Without operational capacity to support them
3. **Ignoring morale**: Until staff start quitting
4. **Reactive security**: Waiting for chaos before investing
5. **Random pricing**: Changing multiplier every night
6. **Forcing milestones**: Pursuing achievements that don't match your situation
7. **Neglecting reports**: Flying blind on trends
8. **Ignoring Trading Standards**: Letting violations accumulate to 7-8 (approaching game over)

---

## Quick Reference Tables

### Customer Tiers
| Tier | Wallet | Trouble % | Notes |
|------|--------|-----------|-------|
| Lowlife | £3-25 | 45% | High chaos, low spend |
| Regular | £8-58 | 30% | Core demographic |
| Decent | £18-108 | 20% | Solid spenders |
| Big Spender | £35-175 | 12% | Premium customers |

### Staff Morale Thresholds
| Morale | Status | Effects |
|--------|--------|---------|
| 80+ | Excellent | Peak performance, retention |
| 65-79 | Good | Stable, normal service |
| 50-64 | Fair | Slight risk of issues |
| 40-49 | Poor | Increased misconduct, departure risk |
| <40 | Critical | Service degradation, mass departures |

### Chaos Levels
| Chaos | Status | Effects |
|-------|--------|---------|
| 0-15 | Calm | Ideal operating conditions |
| 16-35 | Normal | Occasional incidents |
| 36-59 | Elevated | Frequent issues, staff stress |
| 60-79 | High | Fight risk, morale damage |
| 80-100 | Critical | Cascading failures |

### Credit Score Ranges
| Score | Grade | Effects |
|-------|-------|---------|
| 80-100 | Excellent | Best rates, high limits |
| 60-79 | Good | Normal terms |
| 40-59 | Fair | Higher rates, lower limits |
| 20-39 | Poor | Punitive terms |
| 0-19 | Critical | Loan shark territory |

---

## Conclusion

Java Bar Sim v3 rewards **operational excellence**, **strategic consistency**, and **system mastery**. There's no single path to success—whether you pursue safe growth, aggressive expansion, reputation-building, or niche specialization, the game supports your choices.

Key to success:
- Understand how systems interconnect
- Make aligned decisions that compound
- Balance short-term survival with long-term growth
- Learn from reports and trends
- Adapt to challenges without overreacting

**Good luck, landlord. Your pub awaits.**
