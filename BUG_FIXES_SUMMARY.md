# Bug Fixes Summary

## Issues Found and Fixed

### 1. Cover System Calculation Bug (FIXED)

**Problem**: Covers were being calculated incorrectly in the end-of-night report.

**Location**: `Simulation.java`, line 5903

**Bug**: 
```java
int covers = Math.min(s.nightSales + s.nightUnserved, s.maxBarOccupancy);
```

This calculation was:
- Capping covers at the bar's maximum occupancy
- Using sales + unserved instead of unique punters served
- Causing the user to always see covers equal to bar size

**Fix**:
```java
int covers = s.servedPuntersThisService.size();
```

**Result**: Covers now correctly represent the number of unique punters served during a service, regardless of bar capacity.

**Testing**: All CoversTests pass, correctly tracking unique punters even when they leave and return.

---

### 2. Serialization Bug Preventing Save/Load (FIXED)

**Problem**: Save/load operations were failing with `NotSerializableException` for `PubIdentitySystem$WeeklyIdentitySnapshot`.

**Location**: `PubIdentitySystem.java`, line 227

**Bug**: The `WeeklyIdentitySnapshot` record was not implementing `Serializable`:
```java
public record WeeklyIdentitySnapshot(
    double profit,
    ...
    EnumMap<PubIdentity, Double> identitySignals
) {}
```

This caused:
- Manual saves to fail silently or with errors
- Auto-saves at week start to fail
- Unable to load saved games

**Fix**: Made the record implement `Serializable`:
```java
public record WeeklyIdentitySnapshot(
    double profit,
    ...
    EnumMap<PubIdentity, Double> identitySignals
) implements Serializable {}
```

Also added the required import:
```java
import java.io.Serializable;
```

**Result**: Save/load operations now work correctly, including:
- Manual saves from the options menu
- Auto-saves at the start of each week
- Loading previously saved games

**Testing**: 
- SaveLoadReliabilityTests pass
- QuickSaveTest confirms covers are saved and loaded correctly
- AutosaveIntegrationTest confirms week-start auto-save works

---

## Summary

Both bugs have been fixed with minimal changes:
1. **Covers** now accurately reflect unique punters served, not capped by bar size
2. **Save/Load** now works correctly, including auto-save at week boundaries

The user should now be able to:
- See accurate cover counts that change as punters enter and leave
- Save their game manually and have it persist
- Benefit from automatic saves at the end of each week
