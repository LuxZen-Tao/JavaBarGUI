import javax.swing.JTextPane;

/**
 * Tests for milestone trigger timing fixes.
 * Validates that M3_NO_ONE_LEAVES_ANGRY and M4_PAYROLL_GUARDIAN
 * only trigger at appropriate lifecycle events, not on supplier purchases.
 */
public class MilestoneTimingTests {
    public static void main(String[] args) {
        testSupplierPurchaseDoesNotTriggerMilestones();
        testNoOneLeavesAngryTriggersOnlyAfterFullService();
        testNoOneLeavesAngryDoesNotTriggerOnEarlyClose();
        testNoOneLeavesAngryDoesNotTriggerWithAngryDepartures();
        testPayrollGuardianTriggersOnlyAfterWeekRollover();
        testPayrollGuardianDoesNotTriggerBeforePaydayResolved();
        System.out.println("All MilestoneTimingTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that buying wine on day 1 does NOT trigger M3 or M4.
     */
    private static void testSupplierPurchaseDoesNotTriggerMilestones() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Buy wine from supplier
        Wine wine = state.supplier.get(0);
        sim.buyFromSupplier(wine, 10);
        
        // Verify neither milestone was awarded
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY)
            : "M3_NO_ONE_LEAVES_ANGRY should NOT trigger on supplier purchase";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN)
            : "M4_PAYROLL_GUARDIAN should NOT trigger on supplier purchase";
    }

    /**
     * Test that M3 triggers only after a full 20-round service with no angry/broke exits.
     */
    private static void testNoOneLeavesAngryTriggersOnlyAfterFullService() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup stock
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        sim.openNight();
        
        // Verify counters are reset at service start
        assert state.punterKickedOffFromNeglect == 0 : "Neglect counter should be 0 at service start";
        assert state.punterLeftBecauseBroke == 0 : "Broke counter should be 0 at service start";
        
        // Run exactly 20 rounds (full service)
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        
        // Close night normally
        sim.closeNight("Closing time.");
        
        // Verify milestone was awarded
        assert state.achievedMilestones.contains(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY)
            : "M3_NO_ONE_LEAVES_ANGRY should trigger after full 20-round service with no angry/broke exits";
    }

    /**
     * Test that M3 does NOT trigger if service closed early.
     */
    private static void testNoOneLeavesAngryDoesNotTriggerOnEarlyClose() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup stock
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        sim.openNight();
        
        // Verify counters are reset at service start
        assert state.punterKickedOffFromNeglect == 0 : "Neglect counter should be 0 at service start";
        assert state.punterLeftBecauseBroke == 0 : "Broke counter should be 0 at service start";
        
        // Run only 10 rounds (early close)
        for (int round = 0; round < 10; round++) {
            sim.playRound();
        }
        
        // Close night early
        sim.closeNight("Early close");
        
        // Verify milestone was NOT awarded
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY)
            : "M3_NO_ONE_LEAVES_ANGRY should NOT trigger on early close";
    }

    /**
     * Test that M3 does NOT trigger if punters left angry or broke.
     */
    private static void testNoOneLeavesAngryDoesNotTriggerWithAngryDepartures() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup stock
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        sim.openNight();
        
        // Run full 20 rounds
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        
        // Simulate some angry departures
        state.punterKickedOffFromNeglect = 2;
        state.punterLeftBecauseBroke = 1;
        
        // Close night normally
        sim.closeNight("Closing time.");
        
        // Verify milestone was NOT awarded
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M3_NO_ONE_LEAVES_ANGRY)
            : "M3_NO_ONE_LEAVES_ANGRY should NOT trigger with angry/broke departures";
    }

    /**
     * Test that M4 triggers only after week rollover with payday resolved.
     */
    private static void testPayrollGuardianTriggersOnlyAfterWeekRollover() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup: Set to Saturday (day before week end)
        state.dayIndex = 6;
        
        // Mark payday as resolved and bills paid
        state.paydayWindowClosed = true;
        state.wagesPaidLastWeek = true;
        state.rentAccruedThisWeek = 0.0;
        
        // Setup stock
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        // Open and close night (this will trigger week rollover)
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Closing time.");
        
        // Verify milestone was awarded
        assert state.achievedMilestones.contains(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN)
            : "M4_PAYROLL_GUARDIAN should trigger after week rollover with payday resolved";
    }

    /**
     * Test that M4 does NOT trigger before payday is resolved.
     */
    private static void testPayrollGuardianDoesNotTriggerBeforePaydayResolved() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup: Set to Saturday
        state.dayIndex = 6;
        
        // Bills paid but payday NOT resolved
        state.paydayWindowClosed = false;
        state.wagesPaidLastWeek = true;
        state.rentAccruedThisWeek = 0.0;
        
        // Setup stock
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        // Open and close night
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Closing time.");
        
        // Verify milestone was NOT awarded
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M4_PAYROLL_GUARDIAN)
            : "M4_PAYROLL_GUARDIAN should NOT trigger before payday is resolved";
    }
}
