# Java Bar Sim v3 — Developer Guide

**Complete technical reference for developers working on or modding the Java Bar Sim codebase.**

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Entry Point & Game Loop](#entry-point--game-loop)
3. [Core Systems (Technical Deep Dive)](#core-systems-technical-deep-dive)
4. [Data Structures](#data-structures)
5. [System Interactions](#system-interactions)
6. [Event Flow](#event-flow)
7. [Save/Load System](#saveload-system)
8. [Feature Flags](#feature-flags)
9. [Testing Infrastructure](#testing-infrastructure)
10. [Build & Compilation](#build--compilation)
11. [Modding Guide](#modding-guide)

---

## Architecture Overview

### Technology Stack
- **Language**: Java 17+ (uses records, switch expressions, text blocks)
- **UI**: Java Swing (no external UI frameworks)
- **Persistence**: Java Serialization
- **Dependencies**: None (pure JDK)

### Code Organization
- **Single Package**: All .java files in project root
- **No Package Declarations**: Simplifies compilation and classpath
- **File Count**: ~100+ Java files
- **LOC**: ~30,000+ lines of game logic

### Architecture Pattern
**Hub-and-Spoke with Central State**:
- `GameState.java` = central data store (~600 fields)
- `Simulation.java` = orchestrator connecting all systems
- Individual system files = specialized logic modules
- `WineBarGUI.java` = UI layer, event handlers, display logic

---

## Entry Point & Game Loop

### Application Launch Sequence

```java
// Main.java
public static void main(String[] args) {
    GameState state = GameFactory.newGame();  // 1. Initialize fresh game state
    SwingUtilities.invokeLater(() -> {
        UiTheme.apply();                      // 2. Apply UI theme
        new WineBarGUI(state).show();         // 3. Launch Swing GUI
    });
}
```

### Initialization Flow

**1. GameFactory.newGame()** creates:
- Fresh `GameState` with default values
- Initial cash (£500)
- Starting inventory (20 wine units)
- Basic staff (2 bartenders)
- Default settings (reputation 50, chaos 0, etc.)

**2. WineBarGUI Constructor**:
- Instantiates all system objects:
  - `Simulation` (main orchestrator)
  - `EconomySystem`, `PunterSystem`, `StaffSystem`, etc.
- Builds Swing UI components
- Registers button listeners
- Displays initial state

### Game Loop Architecture

**Not Real-Time**: Player-driven turn advancement.

**Nightly Loop**:
```
1. Player clicks "Open Pub" → Simulation.openNight()
2. Player clicks "Advance Round" → Simulation.advanceRound() (6-10 times)
3. Player clicks "Close Night" → Simulation.closeNight()
4. Repeat
```

**Weekly Trigger**:
```
closeNight() {
    dayIndex = (dayIndex + 1) % 7;
    if (dayIndex == 0) {
        endOfWeek();  // Trigger payday, reports, evaluations
    }
}
```

**No Update Loop**: All state changes occur via explicit method calls from UI events.

---

## Core Systems (Technical Deep Dive)

### 1. GameState.java
**Purpose**: Persistent data container for entire game state.

**Key Sections**:
```java
// Economy
public double cash;
public double creditScore;
public List<CreditLine> creditLines;
public int sharkThreatTier;

// Nightly metrics
public int nightRevenue, nightCosts, nightFights;
public int nightUnserved, nightRefunds;

// Weekly aggregates
public double weekRevenue, weekCosts;
public double weeklyRepDeltaNet;

// Staff
public List<Staff> staffPool;
public double teamMorale;
public int weeksNoStaffDepartures;

// Inventory
public WineRack rack;
public FoodRack foodRack;

// Progression
public Set<Milestone> achievedMilestones;
public int pubLevel;
public Set<PubUpgrade> ownedUpgrades;

// VIPs (when FEATURE_VIPS enabled)
public double vipDemandBoostMultiplier;
public double vipRumorShield;

// And ~550 more fields...
```

**Design Note**: `GameState` is a **data class**, not a logic class. All game logic lives in system classes.

**Serialization**:
```java
// GameState implements Serializable
private static final long serialVersionUID = 1L;
```

All fields must be serializable or transient.

---

### 2. Simulation.java
**Purpose**: Central orchestrator connecting all systems.

**Dependencies** (injected via constructor):
```java
private final GameState s;
private final UILogger log;
private final EconomySystem eco;
private final InventorySystem inv;
private final SupplierSystem supplierSystem;
private final StaffSystem staff;
private final PunterSystem punters;
private final SecuritySystem security;
private final EventSystem events;
private final RumorSystem rumors;
private final ActivitySystem activities;
private final PubIdentitySystem identity;
private final MilestoneSystem milestones;
private final UpgradeSystem upgrades;
private final VIPSystem vipSystem;
private final RivalSystem rivalSystem;
private final SeasonCalendar seasonCalendar;
// ...etc
```

**Key Methods**:

```java
// Night cycle
public void openNight()
public void advanceRound()
public void closeNight()

// Weekly cycle
private void endOfWeek()
private void preparePaydayBills()

// Economic actions
public void buyWine(int quantity)
public void buyFood(int quantity)
public void hireStaff(Staff.Type type, int quality)
public void buyUpgrade(PubUpgrade upgrade)

// Configuration
public void setPriceMultiplier(double mult)
public void setSecurityPolicy(SecurityPolicy policy)
public void scheduleActivity(PubActivity activity)
```

**Design Pattern**: **Mediator**. Simulation doesn't implement logic; it delegates to specialized systems and coordinates their interactions.

---

### 3. EconomySystem.java
**Purpose**: Handle all cash flow, credit, and financial operations.

**Key Methods**:
```java
// Primary payment method
public boolean tryPay(double amount, TransactionType type, String description)

// Credit management
public void drawCredit(double amount)
public void repayCredit(CreditLine line, double amount)
public void adjustCreditScore(int delta, String reason)

// Reputation
public void applyRep(int delta, String reason)

// Weekly processing
public void endOfWeekPayBills(List<PaydayBill> bills)
public void applyWeeklyCreditInterest()
```

**Payment Waterfall** in `tryPay()`:
1. Check if cash >= amount → pay from cash
2. If insufficient cash, attempt credit line draw
3. If credit unavailable, payment fails
4. Record transaction in history

**Credit Score Calculation**:
```java
// Factors:
// - Payment history (missed payments = -score)
// - Utilization ratio (>50% = pressure)
// - Repayment speed (fast = +score)
// Range: 0-100
```

---

### 4. PunterSystem.java
**Purpose**: Generate and manage customer entities and behavior.

**Customer Generation** (`Punter.randomPunter()`):
```java
public static Punter randomPunter(int id, Random random, Tier tier) {
    int age = 16 + random.nextInt(35);
    
    // Wallet based on tier
    double wallet = switch (tier) {
        case LOWLIFE -> 3 + random.nextDouble() * 22;
        case REGULAR -> 8 + random.nextDouble() * 50;
        case DECENT -> 18 + random.nextDouble() * 90;
        case BIG_SPENDER -> 35 + random.nextDouble() * 140;
    };
    
    // Trouble probability
    int troubleBase = switch (tier) {
        case LOWLIFE -> 55;  // 45% trouble
        case REGULAR -> 70;  // 30% trouble
        case DECENT -> 80;   // 20% trouble
        case BIG_SPENDER -> 88;  // 12% trouble
    };
    int trouble = (random.nextInt(100) < troubleBase) ? 0 : 
                  (random.nextInt(100) < 92 ? 1 : 2);
    
    // Name generation
    String name = NameGenerator.randomName(random);
    
    return new Punter(id, name, age, wallet, trouble, tier);
}
```

**Behavior During Service**:
```java
// Each round:
for (Punter p : inBarShuffled()) {
    if (canAfford(drink) && !stockedOut()) {
        p.spend(drinkCost);
        revenue += drinkCost;
        p.resetNoBuyStreak();
    } else {
        p.incrementNoBuy();  // Track unserved
        if (p.noBuyRounds >= 3) {
            p.markKickedOut();  // Auto-ban after 3 unserved rounds
        }
    }
}
```

**State Escalation**:
```java
public boolean escalateIfStaying() {
    return switch (state) {
        case CHILL -> { state = State.ROWDY; yield false; }
        case ROWDY -> { state = State.MENACE; yield false; }
        case MENACE -> true;  // Escalation complete, potential fight
    };
}
```

---

### 5. VIPSystem.java
**Purpose**: Track named regular customers with loyalty arcs.

**Roster Management**:
```java
public void ensureRosterFromNames(List<String> punterNames, Random random) {
    if (!FeatureFlags.FEATURE_VIPS) return;
    
    // Deduplicate names
    LinkedHashSet<String> unique = new LinkedHashSet<>(punterNames);
    List<String> candidates = new ArrayList<>(unique);
    Collections.shuffle(candidates, random);
    
    // Fill roster up to TARGET_VIPS (3)
    for (String candidate : candidates) {
        if (roster.size() >= TARGET_VIPS) break;
        if (containsName(candidate)) continue;
        roster.add(newVip(candidate, random));
    }
}
```

**Loyalty Evaluation**:
```java
int loyaltyDelta(VIPRegular vip, VIPNightOutcome outcome) {
    int delta = 0;
    for (VIPPreferenceTag tag : vip.getPreferenceTags()) {
        switch (tag) {
            case SERVICE -> delta += (outcome.unservedCount() <= 1) ? 2 : -2;
            case VALUE -> delta += (outcome.priceMultiplier() <= 1.10) ? 1 : -1;
            case CALM -> delta += (outcome.fightCount() == 0) ? 1 : -2;
            case EVENTS -> delta += (outcome.eventCount() > 0) ? 1 : 0;
            case QUALITY -> delta += (outcome.foodQualitySignal() >= 0.6) ? 1 : -1;
        }
    }
    return Math.max(-5, Math.min(5, delta));  // Clamp ±5
}
```

**Consequence Trigger**:
```java
// In evaluateNightWithConsequences():
VIPArcStage previous = vip.adjustLoyalty(delta);
VIPArcStage current = vip.getArcStage();

if (current != previous && 
    (current == VIPArcStage.ADVOCATE || current == VIPArcStage.BACKLASH) &&
    !vip.isConsequenceTriggered(current)) {
    
    vip.markConsequenceTriggered(current);  // Fire once per stage
    consequences.add(buildConsequence(vip, current));
}
```

**VIP Consequence Application** (in `Simulation.java`):
```java
private void applyVipConsequences(List<VIPSystem.VIPConsequence> consequences) {
    for (VIPConsequence c : consequences) {
        if (c.stage() == VIPArcStage.ADVOCATE) {
            s.vipDemandBoostMultiplier *= 1.05;  // Compound boost
            s.vipRumorShield += 0.02;
            eco.applyRep(+4, "VIP advocate: " + c.vip().getName());
        } else if (c.stage() == VIPArcStage.BACKLASH) {
            eco.applyRep(-6, "VIP backlash: " + c.vip().getName());
            s.vipRumorNegativePressure += 0.08;
        }
    }
}
```

---

### 6. StaffSystem.java
**Purpose**: Manage hiring, morale, wages, and retention.

**Hiring**:
```java
public Staff hireStaff(Staff.Type type, int quality, Random random) {
    String name = StaffNameGenerator.randomName(random);
    double wage = calculateWage(type, quality);  // Quality affects wage
    Staff s = new Staff(name, type, quality, wage);
    return s;
}
```

**Morale Calculation** (team-wide):
```java
// Factors affecting morale:
// - Chaos level (high chaos = -morale)
// - Wage payment timeliness (missed = -15 to -25)
// - Incident frequency (fights/theft = -morale)
// - Manager presence (+morale recovery)
// - Staff Room upgrades (increase morale cap)

public void updateMorale(double chaosDelta, int incidentCount) {
    double chaosPenalty = chaosDelta * 0.15;
    double incidentPenalty = incidentCount * 2.0;
    morale = Math.max(0, Math.min(moraleCAP, morale - chaosPenalty - incidentPenalty));
}
```

**Departure Risk**:
```java
// Low morale triggers departure chance each week
if (morale < 40) {
    double departureChance = (40 - morale) / 100.0;  // 0-40% based on morale
    for (Staff s : staffPool) {
        if (random.nextDouble() < departureChance) {
            staffPool.remove(s);
            departures.add(s);
        }
    }
}
```

---

### 7. MilestoneSystem.java
**Purpose**: Evaluate and grant achievement milestones.

**Evaluation Triggers**:
```java
public void onNightEnd() { evaluateMilestones(EvaluationReason.NIGHT_END); }
public void onWeekEnd() { evaluateMilestones(EvaluationReason.WEEK_END); }
public void onPaydayResolved() { evaluateMilestones(EvaluationReason.PAYDAY); }
public void onRepChanged() { evaluateMilestones(EvaluationReason.REPUTATION_CHANGE); }
public void onActivityScheduled(PubActivity a) { evaluateMilestones(EvaluationReason.ACTIVITY); }
```

**Condition Checking**:
```java
private boolean isMet(Milestone id) {
    return switch (id) {
        case M1_OPEN_FOR_BUSINESS -> 
            s.nightCount >= 3 && !s.businessCollapsed;
            
        case M8_ORDER_RESTORED -> 
            s.chaosRecoveryPending && s.chaos <= CHAOS_SAFE_THRESHOLD;
            
        case M19_HEADLINER_VENUE -> 
            averageWeekPrice() >= 1.22 && 
            s.reputation >= 75 && 
            s.topTierActivityRanThisWeek && 
            refundRate() <= 0.02;
            
        // ... 16 more conditions
    };
}
```

**Grant Flow**:
```java
private void grant(MilestoneDefinition def, EvaluationReason reason) {
    s.achievedMilestones.add(def.id());  // Mark achieved
    s.prestigeMilestones.add(def.id());  // Track for prestige
    applyReward(def.id());               // Apply unlock/bonus
    s.milestonePopups.add(message);      // Queue UI popup
    log.event("Milestone achieved: " + def.title());
}
```

**Reward Application**:
```java
private void applyReward(Milestone id) {
    switch (id) {
        case M6_MARGIN_WITH_MANNERS -> grantCashBonus(100, "...");
        case M8_ORDER_RESTORED -> {
            s.unlockedLandlordActionTier = Math.max(s.unlockedLandlordActionTier, 2);
            s.chaosRecoveryPending = false;  // BUGFIX: Reset flag
        }
        case M11_NARRATIVE_RECOVERY -> {
            s.negativeRumorRecoveryPending = false;  // BUGFIX: Reset flag
        }
        // ... more rewards
    }
}
```

---

### 8. SupplierSystem.java
**Purpose**: Manage inventory purchasing, deals, invoices, and supplier trust.

**Deal Generation** (weekly):
```java
public void generateWeeklyDeal(Random random) {
    // Rotate through deal types
    SupplierDeal.Type dealType = random.nextBoolean() ? 
        SupplierDeal.Type.WINE_DISCOUNT : 
        SupplierDeal.Type.FOOD_DISCOUNT;
    
    double discountPercent = 0.10 + random.nextDouble() * 0.15;  // 10-25%
    currentDeal = new SupplierDeal(dealType, discountPercent);
}
```

**Trade Credit**:
```java
// Allow buying now, pay later
public void buyWithTradeCredit(int quantity, double cost) {
    s.supplierTradeCreditWine += cost;  // Add to invoice
    s.rack.add(quantity);
    // Invoice due at week end
}
```

**Trust Calculation**:
```java
// Trust affected by:
// - Payment timeliness (on-time = +trust)
// - Invoice payment success (missed = -trust)
// - Volume of orders (regular orders = +trust)
// Range: 0-100

public String supplierTrustLabel() {
    if (trust >= 80) return "Excellent";
    if (trust >= 60) return "Good";
    if (trust >= 40) return "Fair";
    return "Poor";
}
```

---

### 9. SecuritySystem.java
**Purpose**: Manage security policy, bouncer quality, and incident prevention.

**Policy Effect**:
```java
public double getDeterrenceFactor() {
    return switch (currentPolicy) {
        case RELAXED -> 0.70;    // Low deterrence
        case STANDARD -> 1.00;   // Normal
        case VIGILANT -> 1.35;   // High deterrence
        case HARDLINE -> 1.70;   // Maximum deterrence
    };
}
```

**Bouncer Intervention**:
```java
// During fight check:
public boolean bouncerIntervenes() {
    int bouncerQuality = s.getBouncerQuality();  // 0-3 from upgrades
    double baseChance = 0.20 + (bouncerQuality * 0.15);  // 20-65%
    return random.nextDouble() < baseChance;
}
```

**Theft Prevention**:
```java
public double theftRisk() {
    double base = 0.08;  // 8% base risk
    base *= (1.0 - (s.getCCTVLevel() * 0.20));  // CCTV reduces risk
    base *= getDeterrenceFactor();  // Policy affects risk
    return Math.max(0.01, base);
}
```

---

### 10. NameGenerator.java
**Purpose**: Load and provide random punter names.

**Resource Loading**:
```java
private static final String FIRST_NAMES_RESOURCE = "names/first_names.txt";
private static final String LAST_NAMES_RESOURCE = "names/last_names.txt";

private static void loadList(String resource, List<String> target) {
    try (InputStream input = NameGenerator.class.getClassLoader()
            .getResourceAsStream(resource)) {
        if (input == null) return;  // Silently fail if missing
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String cleaned = line.trim();
                if (cleaned.isEmpty() || cleaned.startsWith("#")) continue;
                target.add(cleaned);
            }
        }
    } catch (Exception ignored) {}
}
```

**Name Generation**:
```java
public static String randomName(Random random) {
    loadIfNeeded();  // Lazy load
    if (FIRST_NAMES.isEmpty()) return "Punter";  // Fallback
    
    String first = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
    
    if (!LAST_NAMES.isEmpty() && random.nextInt(100) < 85) {  // 85% use last name
        String last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        return first + " " + last;
    }
    return first;
}
```

**Resource Location**: Names must be in `names/` directory at runtime, accessible via classpath.

---

## Data Structures

### Key Classes

**Punter** (customer entity):
```java
public class Punter implements Serializable {
    private final String name;
    private final int age;
    private double wallet;
    private final int trouble;  // 0-2
    private final Tier tier;
    private final int id;
    private int noBuyRounds;
    private boolean banned;
    private boolean leftBar;
    private State state;  // CHILL, ROWDY, MENACE
    private List<Descriptor> descriptors;
}
```

**Staff**:
```java
public class Staff implements Serializable {
    private final String name;
    private final Type type;  // BARTENDER, SERVER, KITCHEN, SECURITY, MANAGER
    private final int quality;  // 1-5
    private final double wage;
}
```

**CreditLine**:
```java
public class CreditLine implements Serializable {
    private final String label;
    private final double limit;
    private double balance;
    private final double interestRate;
}
```

**VIPRegular**:
```java
public final class VIPRegular {
    private final String name;
    private final VIPArchetype archetype;
    private final List<VIPPreferenceTag> preferenceTags;
    private int loyalty;  // 0-100
    private VIPArcStage arcStage;
}
```

### Enums

**Milestone** (19 values):
```java
public enum Milestone {
    M1_OPEN_FOR_BUSINESS,
    M2_NO_EMPTY_SHELVES,
    // ... M3-M18 ...
    M19_HEADLINER_VENUE
}
```

**PubActivity** (activity events):
```java
public enum PubActivity {
    KARAOKE, QUIZ_NIGHT, DJ_SET, OPEN_MIC, 
    COCKTAIL_PROMO, CHARITY_NIGHT, FAMILY_LUNCH, 
    BREWERY_TAKEOVER, SPORTS_SCREENING
}
```

**PubUpgrade** (infrastructure):
```java
public enum PubUpgrade {
    POOL_TABLE, DARTS, TVS, BEER_GARDEN,
    KITCHEN_SETUP, KITCHEN, KITCHEN_EQUIPMENT,
    CCTV, REINFORCED_DOOR_I, DOOR_TEAM_I,
    STAFF_ROOM_I, WINE_CELLAR, EXTENDED_BAR
    // ... ~40 total upgrades
}
```

---

## System Interactions

### Example: Night Opening Sequence

```
User clicks "Open Pub"
    ↓
WineBarGUI.openPubButton.actionPerformed()
    ↓
Simulation.openNight()
    ↓
├─ PunterSystem.seedNightPunters(poolSize)
│   ├─ Calculate pool size (reputation × time × season × rival)
│   └─ Generate Punter objects with names
│
├─ VIPSystem.ensureRosterFromNames(currentPunterNames())
│   ├─ Extract unique names from punter list
│   ├─ Fill VIP roster (up to 3)
│   └─ Assign archetypes based on name hash
│
├─ AudioManager.setPubOpen(true)
│
├─ UILogger.header("OPEN - Day X | Week Y")
│
└─ Update UI displays
```

### Example: Round Advancement

```
User clicks "Advance Round"
    ↓
WineBarGUI.advanceRoundButton.actionPerformed()
    ↓
Simulation.advanceRound()
    ↓
├─ PunterSystem.inBarShuffled()  // Get active customers
│
├─ For each punter:
│   ├─ Attempt drink purchase
│   │   ├─ Check stock availability
│   │   ├─ Check affordability
│   │   ├─ Process payment
│   │   └─ Update punter wallet
│   │
│   └─ If not served:
│       ├─ Punter.incrementNoBuy()
│       └─ Escalate state (CHILL→ROWDY→MENACE)
│
├─ SecuritySystem.evaluateIncidents()
│   ├─ Check for fights (based on chaos + menace punters)
│   ├─ Check for theft (based on security policy)
│   └─ Apply bouncer interventions
│
├─ EventSystem.maybeFireEvent()
│   └─ Random event cards (positive/negative)
│
├─ StaffSystem.checkMisconduct()
│   └─ Morale-based misconduct rolls
│
├─ Update chaos metric
│
├─ PunterSystem.applyNaturalDepartures()
│
└─ Update UI (revenue, chaos, incidents)
```

### Example: Week End Processing

```
Simulation.closeNight() when dayIndex == 0
    ↓
Simulation.endOfWeek()
    ↓
├─ preparePaydayBills()
│   ├─ Calculate wages (all staff)
│   ├─ Calculate rent
│   ├─ Calculate supplier invoices (trade credit)
│   ├─ Calculate credit interest
│   └─ Calculate loan shark payments
│
├─ RumorSystem.generateWeeklyRumors()
│   ├─ Aggregate week's service quality
│   ├─ Calculate rumor tone (POSITIVE/MIXED/NEGATIVE)
│   └─ Apply sentiment pressure
│
├─ PubIdentitySystem.evaluateWeeklyIdentity()
│   ├─ Calculate identity weights
│   ├─ Determine dominant identity
│   └─ Track identity streak
│
├─ VIPSystem.evaluateNight() × 7  // Week's VIP evaluations
│   └─ Apply loyalty changes, fire consequences
│
├─ MilestoneSystem.onWeekEnd()
│   └─ Evaluate all weekly milestone conditions
│
├─ StaffSystem.evaluateDepartures()
│   └─ Morale-based quit checks
│
├─ RivalSystem.runWeekly()  (if FEATURE_RIVALS)
│   ├─ Assign rival stances
│   └─ Calculate market pressure
│
├─ SeasonCalendar.advance()  (if FEATURE_SEASONS)
│   └─ Update active season tags
│
├─ SupplierSystem.generateWeeklyDeal()
│
├─ Reset weekly counters
│
└─ Show Payday Report dialog
```

---

## Event Flow

### UI Event Handlers (WineBarGUI.java)

```java
// Open night button
openButton.addActionListener(e -> {
    sim.openNight();
    updateDisplay();
});

// Advance round button
advanceButton.addActionListener(e -> {
    sim.advanceRound();
    updateDisplay();
    checkForPopups();
});

// Close night button
closeButton.addActionListener(e -> {
    sim.closeNight();
    updateDisplay();
    if (s.dayIndex == 0) {
        showPaydayDialog();
    }
});

// Buy wine button
buyWineButton.addActionListener(e -> {
    int qty = getQuantityFromDialog();
    sim.buyWine(qty);
    updateInventoryDisplay();
});

// Hire staff button
hireStaffButton.addActionListener(e -> {
    Staff.Type type = getTypeFromDialog();
    int quality = getQualityFromDialog();
    sim.hireStaff(type, quality);
    updateStaffDisplay();
});
```

### State Change Propagation

All state changes flow through:
1. **User Action** → UI event handler
2. **UI Handler** → Simulation method call
3. **Simulation** → Delegates to system(s)
4. **System** → Modifies GameState fields
5. **Simulation** → Returns to UI
6. **UI** → Calls `updateDisplay()` to refresh

**No direct GameState modification from UI**. All changes via Simulation methods.

---

## Save/Load System

### Serialization Architecture

**GameState** implements `Serializable`:
```java
public class GameState implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    // All fields are either:
    // - Primitive types (int, double, boolean)
    // - Serializable objects (String, List, Set, etc.)
    // - Enum values (inherently serializable)
    // - transient (excluded from save)
}
```

### Save Process

```java
// SaveManager.java
public static void saveGame(GameState state, String filename) {
    try (ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(filename))) {
        out.writeObject(state);
    } catch (IOException e) {
        // Handle error
    }
}
```

### Load Process

```java
// SaveManager.java
public static GameState loadGame(String filename) {
    try (ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(filename))) {
        return (GameState) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
        // Handle error
        return null;
    }
}
```

### Save Compatibility

**Versioning**: `serialVersionUID = 1L` ensures version tracking.

**Breaking Changes**: Adding/removing fields breaks save compatibility unless handled with custom `readObject()` / `writeObject()` methods.

**Migration Strategy**: If schema changes, implement custom deserialization:
```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    // Migrate old fields to new structure
    if (newField == null) {
        newField = defaultValue;
    }
}
```

---

## Feature Flags

### Purpose
Gate incomplete or experimental features to allow incremental development without breaking main gameplay.

### Implementation (FeatureFlags.java)

```java
public final class FeatureFlags {
    private FeatureFlags() {}  // Static utility class
    
    public static boolean FEATURE_SEASONS = true;   // Calendar effects
    public static boolean FEATURE_RIVALS = true;    // District competition
    public static boolean FEATURE_VIPS = true;      // Named regulars
    public static boolean DEBUG_MODIFIER_LOGS = false;  // Debug output
}
```

### Usage Pattern

**Guarding Code**:
```java
// In Simulation.openNight()
if (FeatureFlags.FEATURE_VIPS) {
    vipSystem.ensureRosterFromNames(currentPunterNames(), s.random);
}

// In Simulation.endOfWeek()
if (FeatureFlags.FEATURE_RIVALS) {
    rivalSystem.runWeekly(s, eco, rumors, s.random);
}

// In SeasonCalendar
if (FeatureFlags.FEATURE_SEASONS) {
    return getActiveSeasonTags(currentDate);
}
return List.of();  // Empty if disabled
```

**Benefits**:
- Features can be fully implemented but disabled for testing
- Allows staged rollout of complex systems
- Debugging can be enabled via flag without code changes
- Easy A/B testing of feature impact

### Current Flag States (as of latest commit)
- `FEATURE_SEASONS`: **ENABLED** ✅
- `FEATURE_RIVALS`: **ENABLED** ✅
- `FEATURE_VIPS`: **ENABLED** ✅
- `DEBUG_MODIFIER_LOGS`: **DISABLED** (debug only)

---

## Testing Infrastructure

### Test Organization

**Test Files**: All test classes end with `*Tests.java`.

**Examples**:
- `VIPSystemTests.java` - VIP loyalty and arc tests
- `MilestoneSystemTests.java` - Milestone condition tests  (hypothetical, not confirmed in repo)
- `RivalSystemTests.java` - Rival system integration tests
- `SaveLoadReliabilityTests.java` - Serialization round-trip tests

### Test Execution

**No Test Framework**: Tests use Java assertions, not JUnit.

**Running Tests**:
```bash
# Compile with test file
javac VIPSystemTests.java

# Run with assertions enabled
java -ea VIPSystemTests
```

**Assertion Pattern**:
```java
public static void main(String[] args) {
    testVIPLoyaltyDelta();
    testVIPArcTransitions();
    testVIPConsequenceFiring();
    System.out.println("All tests passed!");
}

private static void testVIPLoyaltyDelta() {
    VIPSystem system = new VIPSystem();
    VIPNightOutcome outcome = new VIPNightOutcome(0, 0, 1, 0, 1.05, 0.8);
    
    // Test: SERVICE preference satisfied
    int delta = system.loyaltyDelta(vipWithServicePreference, outcome);
    assert delta > 0 : "Expected positive delta for good service";
}
```

### Integration Tests

**Example: Save/Load Round-Trip**:
```java
// SaveLoadReliabilityTests.java
private static void testSaveLoadRoundTrip() {
    GameState original = GameFactory.newGame();
    original.cash = 1234.56;
    original.reputation = 78.9;
    
    SaveManager.saveGame(original, "test.save");
    GameState loaded = SaveManager.loadGame("test.save");
    
    assert Math.abs(loaded.cash - 1234.56) < 0.01 : "Cash not preserved";
    assert Math.abs(loaded.reputation - 78.9) < 0.01 : "Reputation not preserved";
}
```

### Adding New Tests

1. Create `YourFeatureTests.java`
2. Implement `main()` method with test calls
3. Use `assert` for conditions
4. Compile and run with `-ea` flag

---

## Build & Compilation

### Standard Compilation

From project root:
```bash
javac *.java
```

All classes compile to current directory (no package structure).

### Running the Game

```bash
java Main
```

### Compilation Requirements

- **JDK Version**: 17+ (uses records, switch expressions)
- **External Libs**: None
- **Classpath**: Current directory (default)

### Resource Files

**Must be accessible via classpath**:
- `names/first_names.txt`
- `names/last_names.txt`
- `MusicProfiles/*.txt` (music profile definitions)
- `Art/BootSequence/phrases.txt` (boot text)

**Ensuring Resources Are Found**:
```bash
# Option 1: Compile and run from same directory
javac *.java
java Main

# Option 2: Set classpath explicitly
javac -cp .:resources *.java
java -cp .:resources Main
```

### Build Artifacts to Ignore

Add to `.gitignore`:
```
*.class
/out/
/build/
*.save
temp/
```

---

## Modding Guide

### Adding a New Customer Tier

**1. Update Punter.java**:
```java
public enum Tier { 
    LOWLIFE, REGULAR, DECENT, BIG_SPENDER, 
    ELITE  // New tier
}
```

**2. Update randomPunter() method**:
```java
double wallet = switch (tier) {
    case LOWLIFE -> 3 + random.nextDouble() * 22;
    case REGULAR -> 8 + random.nextDouble() * 50;
    case DECENT -> 18 + random.nextDouble() * 90;
    case BIG_SPENDER -> 35 + random.nextDouble() * 140;
    case ELITE -> 100 + random.nextDouble() * 300;  // New
};

int troubleBase = switch (tier) {
    case LOWLIFE -> 55;
    case REGULAR -> 70;
    case DECENT -> 80;
    case BIG_SPENDER -> 88;
    case ELITE -> 95;  // New: 5% trouble chance
};
```

**3. Update PunterSystem.createPunterForReputation()**:
```java
// Adjust tier probability based on reputation
Tier tier;
if (s.reputation >= 90 && roll < 15) {
    tier = Tier.ELITE;  // New: 15% for rep 90+
} else if (s.reputation >= 70 && roll < 35) {
    tier = Tier.BIG_SPENDER;
}
// ... etc
```

### Adding a New Milestone

**1. Update MilestoneSystem.java**:
```java
public enum Milestone {
    M1_OPEN_FOR_BUSINESS,
    // ... existing milestones ...
    M20_YOUR_NEW_MILESTONE  // Add at end
}
```

**2. Add definition in buildDefinitions()**:
```java
definitions.add(new MilestoneDefinition(
    Milestone.M20_YOUR_NEW_MILESTONE,
    5,  // Tier
    "Your Milestone Name",
    "Description of requirement",
    "Reward description"
));
```

**3. Add condition in isMet()**:
```java
case M20_YOUR_NEW_MILESTONE -> 
    s.yourMetric >= threshold && s.anotherCondition;
```

**4. Add reward in applyReward()**:
```java
case M20_YOUR_NEW_MILESTONE -> {
    s.unlockedSomething = true;
    grantCashBonus(500, "Your Milestone");
}
```

### Adding a New Upgrade

**1. Update PubUpgrade.java enum**:
```java
YOUR_UPGRADE("Label", cost, weeklyUpkeep, repBonus, identityBonus, 
             barCap, innRooms, inventoryCap, foodCap, securityBonus, 
             staffCap, moraleBonus, serveBonus, kitchenBonus, foodQuality,
             misconductReduction, theftReduction, foodSpeedBonus, 
             fightReduction, breakInReduction, refundReduction)
```

**2. Add unlock logic in MilestoneSystem.getUpgradeAvailability()**:
```java
if (upgrade == PubUpgrade.YOUR_UPGRADE && 
    !s.achievedMilestones.contains(Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE)) {
    missing.add("Requires milestone: Balanced Books, Busy House");
}
```

**3. Apply effects in GameState or relevant system**:
```java
// GameState.java
public int getYourUpgradeBonus() {
    int bonus = 0;
    for (PubUpgrade u : ownedUpgrades) {
        bonus += u.getYourBonusField();
    }
    return bonus;
}
```

### Adding a New Activity

**1. Update PubActivity.java**:
```java
public enum PubActivity {
    // ... existing ...
    YOUR_ACTIVITY
}
```

**2. Define properties**:
```java
YOUR_ACTIVITY("Label", cost, crowdBoost, identityTag, requiredUpgrade)
```

**3. Add unlock requirement in MilestoneSystem**:
```java
activityMilestoneRequirements.put(
    PubActivity.YOUR_ACTIVITY, 
    Milestone.M10_MIXED_CROWD_WHISPERER
);
```

### Creating Custom Reports

**1. Add method to ReportSystem.java**:
```java
public String generateCustomReport() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== Your Custom Report ===\n");
    sb.append("Metric 1: ").append(s.metric1).append("\n");
    sb.append("Metric 2: ").append(s.metric2).append("\n");
    return sb.toString();
}
```

**2. Add UI button in WineBarGUI.java**:
```java
JButton customReportBtn = new JButton("Custom Report");
customReportBtn.addActionListener(e -> {
    String report = reportSystem.generateCustomReport();
    showMessageDialog(report);
});
```

---

## Common Development Tasks

### Debugging State Issues

**Add Debug Logging**:
```java
// In any system method
if (FeatureFlags.DEBUG_MODIFIER_LOGS) {
    System.out.println("DEBUG: metric=" + value + ", condition=" + check);
}
```

**Enable Flag**:
```java
// FeatureFlags.java
public static boolean DEBUG_MODIFIER_LOGS = true;
```

### Adding a New System

**1. Create YourSystem.java**:
```java
public class YourSystem {
    private final GameState s;
    private final UILogger log;
    
    public YourSystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
    }
    
    public void yourMethod() {
        // System logic
    }
}
```

**2. Instantiate in Simulation.java**:
```java
private final YourSystem yourSystem;

public Simulation(...) {
    // ...
    this.yourSystem = new YourSystem(s, log);
}
```

**3. Call from game loop**:
```java
public void advanceRound() {
    // ...
    yourSystem.yourMethod();
}
```

### Profiling Performance

**Measure Round Time**:
```java
public void advanceRound() {
    long start = System.nanoTime();
    
    // ... existing logic ...
    
    long end = System.nanoTime();
    long durationMs = (end - start) / 1_000_000;
    if (durationMs > 100) {
        System.out.println("WARNING: Round took " + durationMs + "ms");
    }
}
```

---

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (`PunterSystem`, `GameState`)
- **Methods**: camelCase (`advanceRound`, `applyRep`)
- **Variables**: camelCase (`nightRevenue`, `teamMorale`)
- **Constants**: SCREAMING_SNAKE_CASE (`CHAOS_MAX`, `TARGET_VIPS`)
- **Enum Values**: SCREAMING_SNAKE_CASE (`BIG_SPENDER`, `OPEN_FOR_BUSINESS`)

### Method Organization
1. Constructor
2. Public methods (API)
3. Package-private methods (system interaction)
4. Private methods (internal logic)
5. Getters/setters (if necessary)

### Comment Style
- **Javadoc**: For public API methods
- **Inline**: For complex logic
- **Block**: For section headers within large files

### System Design Principles
1. **Single Responsibility**: Each system class handles one domain
2. **No Direct State Mutation from UI**: All changes via Simulation
3. **Serializable State**: Keep GameState serializable for save/load
4. **Fail Gracefully**: Missing resources shouldn't crash (see NameGenerator)

---

## Troubleshooting

### Issue: Names Not Loading
**Symptom**: All punters named "Punter 1", "Punter 2", etc.

**Cause**: `names/first_names.txt` or `names/last_names.txt` not found via classpath.

**Solution**:
1. Ensure files exist in `names/` directory
2. Run from directory containing `names/` folder
3. Check file permissions (must be readable)

### Issue: Compilation Errors on Records
**Symptom**: `record` keyword not recognized.

**Cause**: JDK version < 16.

**Solution**: Upgrade to JDK 17+.

### Issue: Save Files Won't Load
**Symptom**: `InvalidClassException` when loading save.

**Cause**: GameState schema changed, breaking serialization compatibility.

**Solution**:
1. Increment `serialVersionUID` and add migration logic
2. Or delete old save files and start fresh

### Issue: VIP System Not Working
**Symptom**: No VIPs appear despite feature flag enabled.

**Cause 1**: Names not loading (see first issue).
**Cause 2**: Feature flag disabled.

**Solution**: Verify `FeatureFlags.FEATURE_VIPS = true` and names load correctly.

---

## Performance Considerations

### Optimization Targets
- **advanceRound()**: Most frequently called; keep under 50ms
- **endOfWeek()**: Complex but infrequent; acceptable up to 500ms
- **UI updates**: Batch state reads; avoid O(n²) loops in display logic

### Memory Usage
- **GameState size**: ~10KB serialized (thousands of saves OK)
- **Punter list**: Max ~50 punters per night (trivial memory)
- **Event history**: Bounded collections (`addFirst()` + `removeLast()` pattern)

### Algorithmic Complexity
- **PunterSystem.inBarShuffled()**: O(n) filter + O(n log n) shuffle
- **MilestoneSystem.evaluateMilestones()**: O(m) where m=19 milestones
- **Staff morale calculation**: O(s) where s=staff count (~5-15)

**All operations are linear or log-linear; no performance bottlenecks expected.**

---

## Future Extension Points

### Potential Systems to Add
1. **Weather System**: Daily weather affecting demand and mood
2. **Employee Skills**: Individual staff progression with XP/levels
3. **Marketing System**: Advertising spend affecting demand
4. **Competitor AI**: More sophisticated rival behavior modeling
5. **Event Authoring**: External JSON event card definitions

### Architectural Improvements
1. **Dependency Injection**: Replace manual system wiring with DI framework
2. **Event Bus**: Decouple systems via publish/subscribe pattern
3. **UI Separation**: Move to MVC with proper model/view split
4. **Save Format**: Switch from Java serialization to JSON (human-readable, versionable)

---

## Conclusion

Java Bar Sim v3 is a **well-architected simulation** with clear separation between:
- **Data** (GameState.java)
- **Logic** (System classes)
- **Orchestration** (Simulation.java)
- **Presentation** (WineBarGUI.java)

The codebase supports **extension** through:
- Feature flags for gradual rollout
- Enum-based content (easy to add milestones, upgrades, activities)
- System modularity (new systems integrate via Simulation)

**Key developer principles**:
1. All state changes flow through Simulation
2. Systems are injected dependencies, not god objects
3. Serialization requirements constrain GameState design
4. UI is stateless; GameState is source of truth

**Good luck modding!**
