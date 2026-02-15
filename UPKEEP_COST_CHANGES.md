# Upkeep Cost Increase Implementation

## Overview
This change implements increased upkeep costs as specified:
- Security upkeep increased by 20%
- Inn room maintenance cost increased by 50%

## Changes Made

### 1. SecuritySystem.java
**File:** `SecuritySystem.java`
**Line:** 3
**Change:** Updated `SECURITY_UPKEEP_PER_LEVEL` constant
- **Old value:** 1.575
- **New value:** 1.89
- **Calculation:** 1.575 × 1.20 = 1.89
- **Impact:** Security upkeep cost per level per day increased by 20%

### 2. Simulation.java
**File:** `Simulation.java`
**Line:** 8
**Change:** Updated `INN_MAINTENANCE_PER_ROOM` constant
- **Old value:** 2.6
- **New value:** 3.9
- **Calculation:** 2.6 × 1.50 = 3.9
- **Impact:** Inn maintenance cost per room per night increased by 50%

## Test Coverage

### New Test File: UpkeepCostTests.java
Created comprehensive test suite with 6 tests:

1. **testSecurityUpkeepDailyApplication** - Verifies security upkeep is accrued once per day
2. **testSecurityUpkeepNotDoubleApplied** - Ensures no double-application within same day
3. **testInnMaintenancePerNightApplication** - Verifies inn maintenance is accrued per night based on rooms booked
4. **testInnMaintenanceNotDoubleApplied** - Ensures no double-application within same night
5. **testNewSecurityUpkeepValue** - Validates the new security upkeep value (1.89)
6. **testNewInnMaintenanceValue** - Validates the new inn maintenance value (3.9)

### Existing Tests Status
All existing tests continue to pass:
- ✓ UpkeepCostTests (new)
- ✓ WageRentTests
- ✓ InnSystemTests
- ✓ BankingRefactorTests
- ✓ CheckIDsSecurityTests
- ✓ LateNightLicenceTests

## Cost Application

### Security Upkeep
- **Frequency:** Once per day (applied during night close)
- **Formula:** `baseSecurityLevel × 1.89 per day`
- **Example:** Level 10 security = £18.90 per day (was £15.75)
- **Weekly (7 days):** £132.30 (was £110.25)

### Inn Maintenance
- **Frequency:** Once per night (when inn nightly simulation runs)
- **Formula:** `roomsBooked × 3.9 per night`
- **Multiplier:** 1.15× if understaffed housekeeping
- **Example:** 5 rooms booked = £19.50 per night (was £13.00)

## UI Display

The costs are correctly displayed in the weekly report:
- **Security upkeep (daily):** Shows the daily cost
- **Security upkeep accrued:** Shows weekly accumulated cost
- **Inn maintenance accrued:** Shows weekly accumulated maintenance cost

## Balance Verification

✓ Costs are applied exactly once per relevant period:
  - Security: Once per day
  - Inn maintenance: Once per night based on actual rooms booked

✓ No double-application issues:
  - Each cost is tracked in its own accumulator
  - Applied at specific points in the simulation cycle
  - Tests verify single application per period

✓ UI consistency:
  - Report system uses the updated constants
  - All cost displays reflect new values
  - Weekly totals calculated correctly

## Example Output

```
=== After 5 Days of Operation ===

Security Costs:
  - Security Level: 8
  - Rate per Level: £1.89 per day (NEW: +20%)
  - Daily Cost: £15.12
  - Accrued (5 days): £75.60

Inn Maintenance Costs:
  - Total Rooms Booked: 37 (over 5 nights)
  - Rate per Room: £3.90 per night (NEW: +50%)
  - Accrued (5 days): £165.95

Report Display:
  Security level: 8
  Security upkeep (daily): 15.12
  Security upkeep accrued: 75.60
  Inn maintenance accrued: 165.95
```

## Files Modified
1. `SecuritySystem.java` - Security upkeep constant
2. `Simulation.java` - Inn maintenance constant
3. `UpkeepCostTests.java` - New comprehensive test suite (created)

## Backward Compatibility
The changes maintain full backward compatibility:
- Same calculation methods
- Same application timing
- Only the constant values changed
- All existing game mechanics continue to work

## Verification
All acceptance criteria met:
- ✓ New values apply exactly once per relevant period
- ✓ UI reflects the new upkeep totals
- ✓ Cost calculation tests ensure not applied twice
- ✓ Balance remains consistent (no accidental double-application)
