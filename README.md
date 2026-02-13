# Java Bar Sim v3

**A sophisticated pub management simulation game built in Java Swing.**

Run a neighborhood pub night after night, balancing customer service, staff management, inventory control, security, reputation, and financial sustainability. Make strategic decisions that compound over time as you grow from a struggling startup to a prestigious establishment.

---

## Quick Start

### Running the Game
```bash
javac *.java
java Main
```

**Requirements**: JDK 17+ (uses Java records)

### First Time Playing?
Read the **[Player Guide](UserGuide/PLAYER_GUIDE.md)** for complete gameplay instructions, system explanations, and strategy advice.

---

## What Makes This Game Unique

### Deep System Integration
- **17+ interconnected systems**: Economy, Staff, Customers, Security, Reputation, VIPs, Rivals, Seasons, and more
- **Compound decision-making**: Your choices echo across multiple systems with delayed consequences
- **Anti-fragility rewards**: Build resilience through cash reserves, staff redundancy, and supplier trust

### Progression & Mastery
- **19 achievement milestones** across 5 tiers unlock capabilities and rewards
- **Named VIP regulars** with loyalty arcs and personality-based preferences
- **Pub identity system**: Consistent strategic direction compounds exponentially
- **Multiple victory paths**: Safe-growth, high-risk leverage, reputation-first, or specialist strategies

### Realistic Business Simulation
- **Weekly cash flow management**: Wages, rent, debt service, supplier invoices
- **Staff morale & retention**: Low morale causes departures and misconduct
- **Customer behavior modeling**: 4 tiers from Lowlife to Big Spender with varied spending and trouble levels
- **Security risk**: Fights, theft, and chaos cascade into reputation and morale damage
- **Seasonal effects**: Tourist waves, exam seasons, winter slumps affect demand

---

## Core Game Loop

### Nightly Cycle
1. **Prepare**: Check stock, staff, pricing, security policy
2. **Open**: Customers arrive based on reputation and demand
3. **Serve**: Progress through rounds (6-10), serving drinks and food
4. **React**: Handle incidents, use landlord actions, manage chaos
5. **Close**: Review night report and adjust strategy

### Weekly Cycle (7 Nights)
- **Payday**: Pay wages, rent, supplier invoices, credit interest
- **Reports**: Analyze P&L, reputation changes, staff departures
- **Evaluate**: Check milestone progress, VIP loyalty shifts, identity trends
- **Strategic Decisions**: Purchase upgrades, expand operations, adjust direction

---

## Key Features

### Systems at a Glance
- ✅ **Economy**: Cash flow, credit lines, debt, credit score tracking
- ✅ **Customers**: Dynamic generation with names, tiers, behaviors, descriptors
- ✅ **VIP System**: 3 named regulars with loyalty arcs (ADVOCATE/BACKLASH consequences)
- ✅ **Staff**: Hiring, morale, wages, retention across 5 roles
- ✅ **Inventory**: Wine and food management with spoilage and capacity
- ✅ **Security**: Bouncer quality, policies, CCTV, incident prevention
- ✅ **Activities**: 9+ event types (karaoke, quiz nights, DJ sets, etc.)
- ✅ **Upgrades**: 40+ infrastructure improvements (kitchen, cellar, security, etc.)
- ✅ **Milestones**: 19 achievement-based unlocks across 5 tiers
- ✅ **Identity**: Pub personality system (Community Hub, Sports Bar, Premium Venue, etc.)
- ✅ **Rumors & Sentiment**: Public perception with truth-pressure mechanics
- ✅ **Rivals**: District competition affecting market conditions
- ✅ **Seasons**: Calendar effects on demand and customer mix
- ✅ **Inn Expansion**: Secondary lodging revenue stream (3-12 rooms)
- ✅ **Reports**: Nightly, weekly, and 4-week trend analysis

---

## Documentation

| Document | Purpose | Audience |
|----------|---------|----------|
| **[PLAYER_GUIDE.md](UserGuide/PLAYER_GUIDE.md)** | Complete gameplay guide with strategies and system breakdowns | Players |
| **[DRIVER_MECHANICS_GUIDE.md](UserGuide/DRIVER_MECHANICS_GUIDE.md)** | Deep dive into service/stability drivers and staff performance | Players (Advanced) |
| **[GAME_DESCRIPTION.md](UserGuide/GAME_DESCRIPTION.md)** | Detailed design document explaining all mechanics | Players & Designers |
| **[DEVELOPER_GUIDE.md](UserGuide/DEVELOPER_GUIDE.md)** | Technical reference for code architecture and modding | Developers |

---

## Game Highlights

### Victory is Multi-Dimensional
There's no single "win screen"—success is measured through compounding metrics:
- Stable positive cash flow and controlled debt
- High reputation (75+) with strong customer base
- Pub level progression (5 tiers)
- Milestone completion (19 achievements)
- VIP advocate relationships
- Prestige trajectory

### Strategic Depth
- **Identity Mastery**: Aligned decisions (pricing + activities + upgrades + music) compound exponentially
- **System Coupling**: Poor staffing → high chaos → low morale → bad service → reputation loss → revenue decline
- **Delayed Feedback**: One bad night doesn't doom you; consistent patterns create spirals (positive or negative)
- **Multiple Strategies**: Safe-growth vs aggressive-leverage vs reputation-first all viable

### Risk & Reward
- **Credit System**: Use debt strategically for timing gaps, not structural losses
- **Security Investment**: Prevents cascading failures (chaos → morale → reputation → demand)
- **Activity Scheduling**: Force multiplier that amplifies strengths or exposes weaknesses
- **VIP Cultivation**: Named regulars provide narrative depth and compound effects

---

## What's New in v3

This build includes:
- **Expanded banking & debt gameplay**: Credit lines, invoice timing, loan shark escalation
- **Deeper security system**: Policy tuning, phased task resolution, morale links
- **VIP system**: Named regulars with loyalty arcs and consequence triggers
- **Richer identity + rumor pipeline**: Weekly behavior shapes pub personality and perception
- **Active landlord actions**: Cooldown-based tactical abilities with tier unlocks
- **Music profile management**: Venue ambience affects customer fit and identity
- **Seasonal effects**: Calendar-based demand modifiers (tourist waves, winter slumps, etc.)
- **Rival system**: District competition creates market pressure
- **Improved reports**: Trend analysis and cross-system diagnostics

### Recent Fixes (Latest Commit)
- ✅ **Restored punter name system**: Added `names/` directory with 110+ first names and 110+ last names
- ✅ **Fixed milestone bugs**: M8 (Order Restored) and M11 (Narrative Recovery) flag resets
- ✅ **Enabled feature flags**: VIPs, Seasons, and Rivals now active by default
- ✅ **Complete documentation**: Player Guide, Game Description, Developer Guide

---

## Technical Details

### Architecture
- **Language**: Java 17+ (records, switch expressions)
- **Framework**: Pure Java Swing (no external dependencies)
- **Design**: Hub-and-spoke with central GameState container
- **Persistence**: Java Serialization for save/load
- **Lines of Code**: ~30,000+ across 100+ classes

### Key Classes
- `Main.java` - Entry point
- `GameFactory.java` - New game initialization
- `GameState.java` - Central data container (~600 fields)
- `Simulation.java` - Orchestrator connecting all systems
- `WineBarGUI.java` - Swing UI and event handlers
- System classes: `EconomySystem`, `PunterSystem`, `StaffSystem`, `VIPSystem`, etc.

### Compilation
No build system required—simple javac compilation:
```bash
javac *.java
java Main
```

**Note**: Compiled `.class` files are excluded from git. If you see "java cups" (`.class` files) instead of source files in your IDE, ensure:
- `JavaBarGUI.iml` exists at the project root
- The `.idea` folder is properly configured
- Rebuild the project in your IDE (Build → Rebuild Project)

### Resource Files
- `names/first_names.txt` - Customer first names
- `names/last_names.txt` - Customer last names
- `MusicProfiles/*.txt` - Music profile definitions
- `Art/BootSequence/phrases.txt` - Boot sequence text

---

## For Players

### Learning Curve
- **Beginner** (Nights 1-10): Learn basic loop, avoid collapse
- **Intermediate** (Nights 11-40): System literacy, operational consistency
- **Advanced** (Nights 41-80): Strategic coherence, identity mastery
- **Expert** (Nights 80+): Prestige pursuit, dual operations (pub + inn)

### Tips for Success
1. **Cash is king**: Never sacrifice liquidity for growth
2. **Reputation compounds**: Protect it aggressively
3. **Systems interconnect**: Poor staffing causes security issues causes reputation loss
4. **Trends > spikes**: One bad night isn't doom; patterns matter
5. **Identity multiplies**: Aligned decisions compound exponentially

### Common Pitfalls
- Borrowing to cover losses instead of fixing operations
- Over-scheduling activities without operational capacity
- Ignoring staff morale until departures start
- Reactive security (waiting for chaos before investing)
- Forcing milestones that don't match your situation

---

## For Developers

### Modding Support
The codebase is designed for extension:
- **Feature Flags**: Gate experimental features (`FeatureFlags.java`)
- **Enum-based Content**: Easy to add milestones, upgrades, activities
- **System Modularity**: New systems integrate via Simulation
- **Clear Architecture**: Data (GameState) / Logic (Systems) / Orchestration (Simulation) / UI (WineBarGUI)

### Adding Content
- **New Customer Tier**: Update `Punter.java` enum and generation logic
- **New Milestone**: Add to `MilestoneSystem.java` enum, condition, and reward
- **New Upgrade**: Add to `PubUpgrade.java` with stats and unlock conditions
- **New Activity**: Add to `PubActivity.java` and wire unlock requirements
- **New System**: Create system class, inject into `Simulation.java`, call from game loop

See **[DEVELOPER_GUIDE.md](UserGuide/DEVELOPER_GUIDE.md)** for complete technical documentation.

---

## Credits

**Design & Development**: LuxZen-Tao  
**Game Type**: Management/Tycoon Simulation  
**Platform**: Java Swing (cross-platform)  
**License**: [Add your license here]

---

## Version History

### v3.0 (Current)
- VIP system with named regulars and loyalty arcs
- Seasonal calendar effects
- District rival competition
- Expanded banking and credit gameplay
- Music profile and ambience management
- Landlord actions with tier progression
- 19 milestone achievement system
- Complete documentation suite

### v2.x (Previous)
- Core pub simulation systems
- Staff, inventory, security mechanics
- Reputation and rumor systems
- Basic progression

---

## Contact & Support

For bugs, suggestions, or contributions:
- Open an issue on GitHub
- [Add contact method here]

**Your pub awaits. Make your choices count.**
