# Investigation Report: Press Tone and Pub Identity in HUD Badge

## Date: 2026-02-13
## Investigator: GitHub Copilot Agent

---

## Executive Summary

This investigation identified **two separate but related issues** with the Pub Identity system:

1. **HUD Badge Identity Display Issue**: The reputation badge in the HUD shows `state.pubIdentity` (old/legacy system) instead of `state.currentIdentity` (new system used everywhere else)
2. **Press Tone Mechanic**: Exists in the data model but is **only displayed in weekly reports** - it has no gameplay hooks or effects

---

## Issue #1: Identity Display Inconsistency

### The Problem

The game has **TWO separate identity fields**:
- `state.pubIdentity` - Old/legacy identity system (set in `Simulation.java` line 2676)
- `state.currentIdentity` - New identity system (set in `PubIdentitySystem.java` line 54)

**The HUD reputation badge displays the WRONG one.**

### Code Analysis

#### Location: `WineBarGUI.java`, line 3350
```java
private String buildReputationBadgeText(String mood) {
    String identity = formatIdentityLabel(state.pubIdentity);  // <-- WRONG FIELD
    RumorInstance featuredRumor = findFeaturedRumor();
    String rumorLine = featuredRumor != null ? featuredRumor.type().getLabel() : "None";
    return "<html>Reputation: " + state.reputation + " (" + mood + ")"
            + "<br>Identity: " + identity
            + "<br>Rumor: " + rumorLine + "</html>";
}
```

#### Comparison with Mission Control Tab

**Mission Control Tab** (correct - uses new system):
```java
// Simulation.java, line 3688
String identityLine = s.pubIdentity.name().replace('_', ' ') + " " + s.identityDrift;
```

Wait - this also uses `pubIdentity`! But the system has two update methods:

1. **Old System** (`Simulation.java`, line 2674-2686):
   - Sets `s.pubIdentity`
   - Uses legacy scoring based on chaos, fights, events
   - Called from older code path

2. **New System** (`PubIdentitySystem.java`, line 17-68):
   - Sets `s.currentIdentity`
   - Uses sophisticated scoring with 4-week history
   - Tracks multiple factors: profit, refunds, fights, food quality, morale, etc.
   - Called via `updateWeeklyIdentity()` method

### The Root Cause

**Two identity systems coexist**:
- The **old system** updates `pubIdentity` (simple, legacy)
- The **new system** updates `currentIdentity` (sophisticated, modern)

**The inconsistency**: Different parts of the UI read from different fields!
- **HUD Badge**: Reads `state.pubIdentity` (old system)
- **Mission Control Tab**: Reads `state.pubIdentity` (old system) 
- **Press Tone calculation**: Reads `state.currentIdentity` (new system)

### Usage Analysis

Searching the codebase for which field is used where:

**`pubIdentity` (Old System) Used In:**
- `WineBarGUI.java:3350` - HUD reputation badge display
- `Simulation.java:3688` - Mission Control identity line
- `PunterSystem.java:421-423, 551-552, 784-787` - Gameplay effects (mood bias, family-friendliness, tip bias)
- `ReportSystem.java:230` - Identity traffic multiplier calculation
- `Simulation.java:2676` - Assignment of new value

**`currentIdentity` (New System) Used In:**
- `PubIdentitySystem.java:52-54` - Assignment and tracking
- `ReportSystem.java:260` - **Press tone calculation only**

### The Issue

The **HUD never updates** because:
1. HUD reads `state.pubIdentity`
2. If the old identity system isn't being called, `pubIdentity` stays at its initial value (NEUTRAL)
3. OR if both systems run, they may calculate different results
4. The new sophisticated identity system (`currentIdentity`) is not reflected in the HUD

---

## Issue #2: Press Tone Mechanic

### What Is Press Tone?

Press Tone is a **flavor text mechanic** that describes how the local press/media perceives your pub based on identity.

### Implementation

**Data Model** (`PubIdentity.java`, line 17):
```java
private final double pressToneBias;
```

Each identity has a press tone value:
- NEUTRAL: 0.00
- RESPECTABLE: 0.50 (Positive)
- ROWDY: -0.40 (Negative)
- ARTSY: 0.15 (Neutral/Slightly Positive)
- SHADY: -0.60 (Very Negative)
- FAMILY_FRIENDLY: 0.55 (Very Positive)
- UNDERGROUND: -0.20 (Slightly Negative)

### Current Usage

**Only used in one place** (`ReportSystem.java`, line 259-264):
```java
private static String pressTone(GameState s){
    double tone = s.currentIdentity!=null? s.currentIdentity.getPressToneBias():0.0;
    if(tone>=0.25) return "Positive";
    if(tone<=-0.25) return "Negative";
    return "Neutral";
}
```

This is **only called** from:
- `buildWeeklyReportText()` at line 140: `sb.append("Press tone: ").append(pressTone(s)).append("\n");`

### What Press Tone Does

**Current Effects**: NOTHING - It's purely display text in weekly reports

**No Gameplay Hooks Found**:
- ✗ No effect on reputation
- ✗ No effect on traffic
- ✗ No effect on punter mood
- ✗ No effect on events
- ✗ No effect on rumors
- ✗ Not displayed in HUD
- ✗ Not used in any calculations

**It's a "dead" mechanic** - defined in data, calculated, displayed in reports, but has zero gameplay impact.

---

## Detailed Findings

### File: `WineBarGUI.java`

**HUD Structure** (lines 119-140):
- `repLabel` (line 119) - Reputation badge label, displayed in `repBadge` (line 442)
- `reportLabel` (line 127) - Report badge label, displayed in `reportBadge` (line 450)

**Badge Update** (line 3270):
```java
repLabel.setText(buildReputationBadgeText(mood));
```

**Report Badge Update** (line 3300):
```java
reportLabel.setText("Report: " + state.reports().summaryLine());
```

### File: `GameState.java`

**Identity Fields** (lines 460, 471):
```java
public PubIdentity pubIdentity = PubIdentity.NEUTRAL;           // Old system
public PubIdentity currentIdentity = PubIdentity.RESPECTABLE;   // New system
```

### File: `PubIdentitySystem.java`

**Modern Identity System** (line 52-54):
```java
PubIdentity previous = s.currentIdentity;
PubIdentity next = dominantIdentity();
s.currentIdentity = next;
```

Uses sophisticated 4-week historical analysis with weighted scoring across 12+ metrics.

### File: `Simulation.java`

**Legacy Identity System** (line 2674-2676):
```java
PubIdentity previous = s.pubIdentity;
PubIdentity next = pickDominantIdentity(3.0);
s.pubIdentity = next;
```

Uses simpler chaos/event-based scoring.

---

## Potential Fixes and Improvements

### Fix Option 1: Use New Identity System in HUD (Recommended)

**Change**: Update HUD to read `state.currentIdentity` instead of `state.pubIdentity`

**Location**: `WineBarGUI.java`, line 3350
```java
// BEFORE:
String identity = formatIdentityLabel(state.pubIdentity);

// AFTER:
String identity = formatIdentityLabel(state.currentIdentity);
```

**Pros**:
- Minimal change (1 line)
- Uses the more sophisticated identity system
- Consistent with press tone calculation

**Cons**:
- Need to verify if `currentIdentity` is always set
- May require updating other systems that rely on `pubIdentity`

### Fix Option 2: Deprecate Old System Completely

**Change**: Remove legacy identity system, migrate all `pubIdentity` references to `currentIdentity`

**Files to Update**:
- `WineBarGUI.java` (1 reference)
- `Simulation.java` (multiple references)
- `PunterSystem.java` (8 references)
- `ReportSystem.java` (1 reference)

**Pros**:
- Cleaner codebase
- Single source of truth
- Better maintainability

**Cons**:
- Larger change scope
- More testing required
- May affect game balance if systems behave differently

### Fix Option 3: Keep Both, Sync Them

**Change**: Make `pubIdentity` = `currentIdentity` after identity updates

**Location**: `PubIdentitySystem.java`, after line 54
```java
s.currentIdentity = next;
s.pubIdentity = next;  // <-- ADD THIS
```

**Pros**:
- Minimal risk
- Preserves both systems
- Simple fix

**Cons**:
- Doesn't address root cause
- Maintains code duplication
- Confusing for future developers

---

## Press Tone Enhancement Options

### Option 1: Add Press Tone to HUD Badge

**Change**: Display press tone in reputation badge alongside identity

```java
private String buildReputationBadgeText(String mood) {
    String identity = formatIdentityLabel(state.currentIdentity);
    String pressTone = formatPressTone(state.currentIdentity);  // NEW
    RumorInstance featuredRumor = findFeaturedRumor();
    String rumorLine = featuredRumor != null ? featuredRumor.type().getLabel() : "None";
    return "<html>Reputation: " + state.reputation + " (" + mood + ")"
            + "<br>Identity: " + identity + " (" + pressTone + ")"  // MODIFIED
            + "<br>Rumor: " + rumorLine + "</html>";
}

private String formatPressTone(PubIdentity identity) {
    if (identity == null) return "";
    double tone = identity.getPressToneBias();
    if (tone >= 0.25) return "📰+";  // Positive press
    if (tone <= -0.25) return "📰−";  // Negative press
    return "📰";  // Neutral press
}
```

**Impact**: Visual only, no gameplay effect

### Option 2: Add Gameplay Effects to Press Tone

**Potential Effects**:
1. **Reputation Drift Rate**: Positive press = slower reputation decay
2. **Rumor Spread**: Press tone affects how quickly rumors spread
3. **Event Probability**: Positive press = more positive events
4. **VIP Attraction**: Positive press attracts more VIP customers
5. **Traffic Modifier**: Slight traffic adjustment based on press tone

**Example Implementation** (in `Simulation.java` traffic calculation):
```java
private double pressTrafficMultiplier() {
    if (s.currentIdentity == null) return 1.0;
    double tone = s.currentIdentity.getPressToneBias();
    return 1.0 + (tone * 0.05);  // ±5% traffic based on press tone
}
```

### Option 3: Remove Press Tone Entirely

**If it serves no purpose**: Remove `pressToneBias` from `PubIdentity` and related code

**Pros**: Cleaner codebase
**Cons**: Loses potential future feature

---

## Recommendations

### Priority 1: Fix Identity Display Bug

**Action**: Update `WineBarGUI.java` line 3350 to use `state.currentIdentity`

**Rationale**: 
- This is a clear bug - HUD doesn't reflect actual identity
- Minimal change, low risk
- Aligns with the more sophisticated identity system

### Priority 2: Verify Identity System Architecture

**Action**: Determine if both identity systems should coexist or consolidate

**Questions to Answer**:
1. Is the legacy `pubIdentity` system still needed?
2. Does `PubIdentitySystem.updateWeeklyIdentity()` always run?
3. Are there gameplay reasons to maintain both systems?

**Recommended Approach**:
- If new system is fully active: Migrate all code to use `currentIdentity`
- If both needed: Clearly document why and keep them in sync

### Priority 3: Press Tone Decision

**Action**: Decide on press tone's future

**Options**:
1. **Display only**: Add to HUD for player awareness (low effort)
2. **Make functional**: Add gameplay effects (medium effort)
3. **Remove**: Clean up unused code (low effort)

**Recommendation**: Display only (Option 1) - provides value with minimal effort

---

## Testing Recommendations

If fixes are implemented:

1. **Identity Display Test**:
   - Start new game
   - Play several weeks
   - Verify HUD badge updates when identity changes
   - Verify Mission Control tab matches HUD

2. **Identity System Consistency Test**:
   - Add logging to compare `pubIdentity` vs `currentIdentity` values
   - Verify they stay in sync throughout gameplay
   - Check all systems reading identity get correct value

3. **Press Tone Test** (if enhanced):
   - Verify press tone displays correctly
   - Verify press tone changes with identity
   - If gameplay effects added, verify they work

---

## Code References

### Key Files
- `WineBarGUI.java` - Line 3350 (HUD bug)
- `PubIdentity.java` - Lines 17, 44 (pressToneBias definition)
- `PubIdentitySystem.java` - Lines 52-54 (new identity system)
- `Simulation.java` - Line 2676 (old identity system), Line 3688 (identity display)
- `ReportSystem.java` - Lines 259-264 (press tone usage)
- `GameState.java` - Lines 460, 471 (dual identity fields)

### Search Commands Used
```bash
grep -n "pubIdentity\|currentIdentity" GameState.java
grep -n "pressToneBias" *.java
grep -n "buildReputationBadgeText" WineBarGUI.java
```

---

## Conclusion

**The Investigation Revealed**:

1. **Identity Label Bug**: HUD reputation badge reads `state.pubIdentity` (old system) instead of the actively-maintained `state.currentIdentity` (new system), causing it to show stale or incorrect values

2. **Press Tone Status**: Fully implemented in data model, calculated in reports, but has **zero gameplay hooks** - it's display-only text with no functional purpose

**Recommended Actions**:
- Fix the HUD to use `state.currentIdentity` (1-line change)
- Audit and consolidate the dual identity system architecture
- Decide whether to enhance, display, or remove press tone mechanic

**No Changes Made**: As requested, this is a report-only iteration. No code has been modified.
