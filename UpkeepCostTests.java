import javax.swing.JTextPane;
import java.util.Random;

/**
 * Tests to verify that security upkeep and inn maintenance costs are applied
 * exactly once per relevant period and not double-applied.
 */
public class UpkeepCostTests {
    public static void main(String[] args) {
        testSecurityUpkeepDailyApplication();
        testSecurityUpkeepNotDoubleApplied();
        testInnMaintenancePerNightApplication();
        testInnMaintenanceNotDoubleApplied();
        testNewSecurityUpkeepValue();
        testNewInnMaintenanceValue();
        System.out.println("All UpkeepCostTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that security upkeep is accrued once per day when running night cycle
     */
    private static void testSecurityUpkeepDailyApplication() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Set up security level
        state.baseSecurityLevel = 5;
        state.securityUpkeepAccruedThisWeek = 0.0;
        
        double expectedDailyCost = 5 * SecuritySystem.SECURITY_UPKEEP_PER_LEVEL;
        
        // Run one night
        sim.openNight();
        sim.closeNight("Test close");
        
        assert closeTo(state.securityUpkeepAccruedThisWeek, expectedDailyCost)
            : "Security upkeep should be accrued once per night. Expected: " + expectedDailyCost 
            + " Got: " + state.securityUpkeepAccruedThisWeek;
    }

    /**
     * Test that security upkeep is not double-applied within the same day
     */
    private static void testSecurityUpkeepNotDoubleApplied() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        state.baseSecurityLevel = 3;
        state.securityUpkeepAccruedThisWeek = 0.0;
        
        double expectedDailyCost = 3 * SecuritySystem.SECURITY_UPKEEP_PER_LEVEL;
        
        // Run one night
        sim.openNight();
        double afterOpen = state.securityUpkeepAccruedThisWeek;
        sim.closeNight("Test close");
        double afterClose = state.securityUpkeepAccruedThisWeek;
        
        // The cost should be the same whether checked after open or after close
        // and should equal exactly one day's cost
        assert closeTo(afterClose, expectedDailyCost)
            : "Security upkeep should equal one day's cost. Expected: " + expectedDailyCost 
            + " Got: " + afterClose;
    }

    /**
     * Test that inn maintenance is accrued per night based on rooms booked
     */
    private static void testInnMaintenancePerNightApplication() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Set up inn
        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
        state.roomsTotal = 6;
        state.roomPrice = 40.0;
        state.innRep = 80.0;
        state.cleanliness = 85.0;
        state.reputation = 30;
        state.innMaintenanceAccruedWeekly = 0.0;
        
        // Add reception staff to ensure bookings
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", 
            Staff.Type.SENIOR_RECEPTIONIST, new Random(42)));
        
        state.random.setSeed(100);
        sim.runInnNightly();
        
        int roomsBooked = state.lastNightRoomsBooked;
        assert roomsBooked > 0 : "Should have some rooms booked for this test.";
        
        // Base maintenance is INN_MAINTENANCE_PER_ROOM per room booked
        // Without understaffing, multiplier is 1.0
        // Note: Expected value is hardcoded to verify the exact implementation behavior
        double expectedMaintenancePerRoom = 3.9; // Simulation.INN_MAINTENANCE_PER_ROOM (private constant)
        double expectedMaintenance = roomsBooked * expectedMaintenancePerRoom;
        
        assert closeTo(state.innMaintenanceAccruedWeekly, expectedMaintenance)
            : "Inn maintenance should be accrued based on rooms booked. Expected: " 
            + expectedMaintenance + " Got: " + state.innMaintenanceAccruedWeekly;
    }

    /**
     * Test that inn maintenance is not double-applied within the same night
     */
    private static void testInnMaintenanceNotDoubleApplied() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Set up inn
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        state.roomPrice = 35.0;
        state.innRep = 70.0;
        state.cleanliness = 80.0;
        state.reputation = 25;
        state.innMaintenanceAccruedWeekly = 0.0;
        
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", 
            Staff.Type.RECEPTIONIST, new Random(50)));
        
        state.random.setSeed(200);
        
        // Run inn nightly once
        sim.runInnNightly();
        double firstRun = state.innMaintenanceAccruedWeekly;
        
        // Running it again should add more maintenance, not reset or double the first amount
        int firstRoomsBooked = state.lastNightRoomsBooked;
        sim.runInnNightly();
        int secondRoomsBooked = state.lastNightRoomsBooked;
        double secondRun = state.innMaintenanceAccruedWeekly;
        
        // Note: Expected value is hardcoded to verify the exact implementation behavior
        double expectedMaintenancePerRoom = 3.9; // Simulation.INN_MAINTENANCE_PER_ROOM (private constant)
        double expectedTotal = (firstRoomsBooked + secondRoomsBooked) * expectedMaintenancePerRoom;
        
        assert closeTo(secondRun, expectedTotal)
            : "Inn maintenance should accumulate correctly. Expected: " + expectedTotal 
            + " Got: " + secondRun;
    }

    /**
     * Test that the new security upkeep value (1.89) is correctly applied
     */
    private static void testNewSecurityUpkeepValue() {
        // Verify the constant is the new value (20% increase from 1.575)
        assert closeTo(SecuritySystem.SECURITY_UPKEEP_PER_LEVEL, 1.89)
            : "Security upkeep should be 1.89 (20% increase from 1.575). Got: " 
            + SecuritySystem.SECURITY_UPKEEP_PER_LEVEL;
        
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        state.baseSecurityLevel = 10;
        state.securityUpkeepAccruedThisWeek = 0.0;
        
        sim.openNight();
        sim.closeNight("Test");
        
        // With level 10 and rate 1.89, daily cost should be 18.9
        assert closeTo(state.securityUpkeepAccruedThisWeek, 18.9)
            : "Security upkeep at level 10 should be 18.9. Got: " 
            + state.securityUpkeepAccruedThisWeek;
    }

    /**
     * Test that the new inn maintenance value (3.9) is correctly applied
     */
    private static void testNewInnMaintenanceValue() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Set up inn with controlled conditions
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        state.roomPrice = 50.0;
        state.innRep = 90.0;
        state.cleanliness = 90.0;
        state.reputation = 50;
        state.innMaintenanceAccruedWeekly = 0.0;
        
        // Add staff to ensure good coverage (no understaffing multiplier)
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", 
            Staff.Type.SENIOR_RECEPTIONIST, new Random(300)));
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "HK", 
            Staff.Type.HEAD_HOUSEKEEPER, new Random(301)));
        
        state.random.setSeed(400);
        sim.runInnNightly();
        
        int roomsBooked = state.lastNightRoomsBooked;
        // With good staffing, multiplier is 1.0, so cost = rooms * maintenance rate
        // Note: Expected value is hardcoded to verify the exact implementation behavior
        double expectedMaintenancePerRoom = 3.9; // Simulation.INN_MAINTENANCE_PER_ROOM (private constant)
        double expectedCost = roomsBooked * expectedMaintenancePerRoom;
        
        assert closeTo(state.innMaintenanceAccruedWeekly, expectedCost)
            : "Inn maintenance per room should be 3.9 (50% increase from 2.6). Expected: " 
            + expectedCost + " Got: " + state.innMaintenanceAccruedWeekly;
    }

    private static boolean closeTo(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
