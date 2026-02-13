import javax.swing.JTextPane;
import java.util.Random;

public class InnSystemTests {
    public static void main(String[] args) {
        testInnUnlock();
        testInnTierUpgrade();
        testBookingDeterministic();
        testReceptionEffect();
        testHousekeepingEffect();
        testDutyManagerUnlockRequirements();
        testDutyManagerEffects();
        testMaintenanceAccrualAndPayday();
        testHohPoolAndCap();
        testRoomsTotalOnlyFromInnTier();
        testRateLockedBookings();
        testInnVolatilityPenalty();
        testInnReportStrings();
        testInnMenuSmoke();
        System.out.println("All InnSystemTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testInnUnlock() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        assert state.innUnlocked : "Inn should be unlocked after tier 1 upgrade.";
        assert state.innTier == 1 : "Inn tier should be 1.";
        assert state.roomsTotal == 3 : "Tier 1 should have 3 rooms.";
        assert state.roomPrice > 0.0 : "Room price should be initialized.";
        assert state.innRep > 0.0 : "Inn rep should be initialized.";
        assert state.cleanliness > 0.0 : "Cleanliness should be initialized.";
    }

    private static void testInnTierUpgrade() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        assert state.roomsTotal == 3 : "Tier 1 rooms should be 3.";
        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
        assert state.roomsTotal == 6 : "Tier 2 rooms should be 6.";
        sim.installUpgradeForTest(PubUpgrade.INN_WING_3);
        assert state.roomsTotal == 10 : "Tier 3 rooms should be 10.";
    }

    private static void testBookingDeterministic() {
        GameState stateA = GameFactory.newGame();
        Simulation simA = newSimulation(stateA);
        simA.installUpgradeForTest(PubUpgrade.INN_WING_2);
        stateA.roomPrice = 40.0;
        stateA.innRep = 70.0;
        stateA.cleanliness = 82.0;
        stateA.reputation = 20;
        stateA.fohStaff.add(StaffFactory.createStaff(stateA.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(1)));
        stateA.random.setSeed(42);
        simA.runInnNightly();
        int bookedA = stateA.lastNightRoomsBooked;

        GameState stateB = GameFactory.newGame();
        Simulation simB = newSimulation(stateB);
        simB.installUpgradeForTest(PubUpgrade.INN_WING_2);
        stateB.roomPrice = 40.0;
        stateB.innRep = 70.0;
        stateB.cleanliness = 82.0;
        stateB.reputation = 20;
        stateB.fohStaff.add(StaffFactory.createStaff(stateB.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(1)));
        stateB.random.setSeed(42);
        simB.runInnNightly();
        int bookedB = stateB.lastNightRoomsBooked;

        assert bookedA == bookedB : "Bookings should be deterministic for the same seed.";
        assert bookedA >= 0 && bookedA <= stateA.roomsTotal : "Bookings should be clamped to rooms total.";
    }

    private static void testReceptionEffect() {
        GameState base = GameFactory.newGame();
        Simulation simBase = newSimulation(base);
        simBase.installUpgradeForTest(PubUpgrade.INN_WING_3);
        base.roomPrice = 30.0;
        base.innRep = 80.0;
        base.cleanliness = 80.0;
        base.reputation = 40;
        base.random.setSeed(10);
        simBase.runInnNightly();
        int bookedWithoutReception = base.lastNightRoomsBooked;

        GameState staffed = GameFactory.newGame();
        Simulation simStaffed = newSimulation(staffed);
        simStaffed.installUpgradeForTest(PubUpgrade.INN_WING_3);
        staffed.roomPrice = 30.0;
        staffed.innRep = 80.0;
        staffed.cleanliness = 80.0;
        staffed.reputation = 40;
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(2)));
        staffed.random.setSeed(10);
        simStaffed.runInnNightly();
        int bookedWithReception = staffed.lastNightRoomsBooked;

        assert bookedWithReception >= bookedWithoutReception : "Reception staffing should not reduce bookings.";
    }

    private static void testHousekeepingEffect() {
        GameState base = GameFactory.newGame();
        Simulation simBase = newSimulation(base);
        simBase.installUpgradeForTest(PubUpgrade.INN_WING_3);
        base.roomPrice = 30.0;
        base.innRep = 70.0;
        base.cleanliness = 70.0;
        base.reputation = 30;
        base.fohStaff.add(StaffFactory.createStaff(base.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(3)));
        base.random.setSeed(15);
        simBase.runInnNightly();
        double cleanlinessWithout = base.cleanliness;

        GameState staffed = GameFactory.newGame();
        Simulation simStaffed = newSimulation(staffed);
        simStaffed.installUpgradeForTest(PubUpgrade.INN_WING_3);
        staffed.roomPrice = 30.0;
        staffed.innRep = 70.0;
        staffed.cleanliness = 70.0;
        staffed.reputation = 30;
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(3)));
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "HK", Staff.Type.HOUSEKEEPER, new Random(4)));
        staffed.random.setSeed(15);
        simStaffed.runInnNightly();
        double cleanlinessWith = staffed.cleanliness;

        assert cleanlinessWith >= cleanlinessWithout : "Housekeeping coverage should improve cleanliness.";
    }

    private static void testDutyManagerUnlockRequirements() {
        // Test 1: Can't hire duty manager without inn unlocked
        GameState state1 = GameFactory.newGame();
        Simulation sim1 = newSimulation(state1);
        sim1.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_I);
        sim1.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_II);
        int staffCountBefore1 = state1.fohStaffCount();
        sim1.hireStaff(Staff.Type.DUTY_MANAGER);
        assert state1.fohStaffCount() == staffCountBefore1 : "Should not hire duty manager without inn unlocked.";

        // Test 2: Can't hire duty manager with only INN_WING_1 (no leadership program)
        GameState state2 = GameFactory.newGame();
        Simulation sim2 = newSimulation(state2);
        sim2.installUpgradeForTest(PubUpgrade.INN_WING_1);
        int staffCountBefore2 = state2.fohStaffCount();
        sim2.hireStaff(Staff.Type.DUTY_MANAGER);
        assert state2.fohStaffCount() == staffCountBefore2 : "Should not hire duty manager without Leadership Program II.";

        // Test 3: Can't hire duty manager with only LEADERSHIP_PROGRAM_I
        GameState state3 = GameFactory.newGame();
        Simulation sim3 = newSimulation(state3);
        sim3.installUpgradeForTest(PubUpgrade.INN_WING_1);
        sim3.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_I);
        int staffCountBefore3 = state3.fohStaffCount();
        sim3.hireStaff(Staff.Type.DUTY_MANAGER);
        assert state3.fohStaffCount() == staffCountBefore3 : "Should not hire duty manager with only Leadership Program I.";

        // Test 4: CAN hire duty manager with inn and LEADERSHIP_PROGRAM_II
        GameState state4 = GameFactory.newGame();
        Simulation sim4 = newSimulation(state4);
        sim4.installUpgradeForTest(PubUpgrade.INN_WING_1);
        sim4.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_I);
        sim4.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_II);
        int staffCountBefore4 = state4.fohStaffCount();
        sim4.hireStaff(Staff.Type.DUTY_MANAGER);
        assert state4.fohStaffCount() == staffCountBefore4 + 1 : "Should hire duty manager with inn and Leadership Program II.";
    }

    private static void testDutyManagerEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        sim.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_I);
        sim.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_II);
        sim.hireStaff(Staff.Type.DUTY_MANAGER);
        assert state.managerPoolCount() == 1 : "Duty manager should count toward manager pool.";

        GameState base = GameFactory.newGame();
        Simulation simBase = newSimulation(base);
        simBase.installUpgradeForTest(PubUpgrade.INN_WING_2);
        base.roomsTotal = 7;
        base.roomPrice = 28.0;
        base.innRep = 80.0;
        base.cleanliness = 65.0;
        base.reputation = 20;
        base.fohStaff.add(StaffFactory.createStaff(base.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(5)));
        base.fohStaff.add(StaffFactory.createStaff(base.nextStaffId++, "HK", Staff.Type.HEAD_HOUSEKEEPER, new Random(6)));
        base.random.setSeed(22);
        simBase.runInnNightly();
        double maintenanceWithout = base.innMaintenanceAccruedWeekly;

        GameState staffed = GameFactory.newGame();
        Simulation simStaffed = newSimulation(staffed);
        simStaffed.installUpgradeForTest(PubUpgrade.INN_WING_2);
        simStaffed.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_I);
        simStaffed.installUpgradeForTest(PubUpgrade.LEADERSHIP_PROGRAM_II);
        staffed.roomsTotal = 7;
        staffed.roomPrice = 28.0;
        staffed.innRep = 80.0;
        staffed.cleanliness = 65.0;
        staffed.reputation = 20;
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(5)));
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "HK", Staff.Type.HEAD_HOUSEKEEPER, new Random(6)));
        staffed.fohStaff.add(StaffFactory.createStaff(staffed.nextStaffId++, "DM", Staff.Type.DUTY_MANAGER, new Random(7)));
        staffed.random.setSeed(22);
        simStaffed.runInnNightly();
        double maintenanceWith = staffed.innMaintenanceAccruedWeekly;

        assert maintenanceWith <= maintenanceWithout : "Duty manager should reduce maintenance impact via coverage boost.";
    }

    private static void testMaintenanceAccrualAndPayday() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        state.roomPrice = 30.0;
        state.innRep = 75.0;
        state.cleanliness = 80.0;
        state.reputation = 30;
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", Staff.Type.RECEPTIONIST, new Random(8)));
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "HK", Staff.Type.HOUSEKEEPER, new Random(9)));
        state.dayIndex = 6;
        state.random.setSeed(33);
        sim.openNight();
        sim.closeNight("Closing time.");
        assert state.innMaintenanceAccruedWeekly > 0.0 : "Inn maintenance should accrue.";
        boolean found = state.paydayBills.stream().anyMatch(b -> b.getType() == PaydayBill.Type.INN_MAINTENANCE);
        assert found : "Payday bills should include inn maintenance.";
    }

    private static void testHohPoolAndCap() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        assert state.hohStaffCap == 2 : "HOH cap should scale with inn tier.";
        sim.hireStaff(Staff.Type.RECEPTIONIST);
        sim.hireStaff(Staff.Type.HOUSEKEEPER);
        sim.hireStaff(Staff.Type.RECEPTION_TRAINEE);
        assert state.hohStaffCount() == 2 : "HOH cap should limit reception/housekeeping hires.";
        assert state.fohStaffCount() == 0 : "HOH staff should not count toward FOH.";
        assert state.bohStaff.isEmpty() : "HOH hires should not affect BOH.";

        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
        assert state.hohStaffCap == 4 : "HOH cap should increase with inn tier.";
        sim.hireStaff(Staff.Type.RECEPTION_TRAINEE);
        sim.hireStaff(Staff.Type.HEAD_HOUSEKEEPER);
        assert state.hohStaffCount() == 4 : "HOH hires should use new cap after tier upgrade.";
    }

    private static void testRoomsTotalOnlyFromInnTier() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
        int roomsBefore = state.roomsTotal;
        sim.installUpgradeForTest(PubUpgrade.SOUNDPROOFING_I);
        assert state.roomsTotal == roomsBefore : "Non-inn upgrades should not change rooms total.";
    }

    private static void testRateLockedBookings() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
        state.roomsTotal = 10;
        state.roomPrice = 40.0;
        state.innRep = 85.0;
        state.cleanliness = 85.0;
        state.reputation = 40;
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", Staff.Type.SENIOR_RECEPTIONIST, new Random(10)));
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "HK", Staff.Type.HEAD_HOUSEKEEPER, new Random(11)));
        sim.openNight();
        state.roundInNight = 10;
        sim.setRoomPrice(60.0);
        state.random.setSeed(12);
        sim.runInnNightly();
        assert state.lastNightRoomsBooked > 0 : "Bookings should occur for rate-lock test.";
        boolean hasBaseRate = false;
        boolean hasNewRate = false;
        double revenue = 0.0;
        int rooms = 0;
        for (GameState.InnBookingRecord record : state.lastNightInnBookings) {
            revenue += record.rooms() * record.rateApplied();
            rooms += record.rooms();
            if (Math.abs(record.rateApplied() - 40.0) < 0.01) hasBaseRate = true;
            if (Math.abs(record.rateApplied() - 60.0) < 0.01) hasNewRate = true;
        }
        assert rooms == state.lastNightRoomsBooked : "Booking records should sum to total rooms.";
        assert Math.abs(revenue - state.lastNightRoomRevenue) < 0.01 : "Revenue should match booking records.";
        assert hasBaseRate && hasNewRate : "Bookings should lock rates before and after price changes.";
    }

    private static void testInnVolatilityPenalty() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_1);
        state.innRep = 60.0;
        state.chaos = 0.0;
        sim.openNight();
        sim.setRoomPrice(46.0);
        sim.setRoomPrice(47.0);
        sim.setRoomPrice(48.0);
        assert state.innPriceChangesThisNight >= 3 : "Price changes should be tracked.";
        assert state.chaos > 0.0 : "Volatility penalty should increase chaos.";
        assert state.innRep < 60.0 : "Volatility penalty should reduce inn rep.";
    }

    private static void testInnReportStrings() {
        GameState state = GameFactory.newGame();
        state.innUnlocked = true;
        state.roomsTotal = 5;
        state.lastNightRoomsBooked = 2;
        state.lastNightRoomRevenue = 80.0;
        state.lastInnEventsCount = 1;
        state.weekInnRoomsSold = 10;
        state.weekInnRevenue = 400.0;
        state.innMaintenanceAccruedWeekly = 25.0;
        state.weekInnEventsCount = 2;
        state.weekInnComplaintCount = 1;
        state.weekInnEventMaintenance = 12.0;
        state.weekInnEventRefunds = 0.0;
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "Rec", Staff.Type.RECEPTIONIST, new Random(13)));
        String report = ReportSystem.buildReportText(state);
        assert report.contains("Inn: 2/5 rooms booked") : "Night report should include inn rooms.";
        assert report.contains("Inn revenue tonight") : "Night report should include inn revenue.";
        assert report.contains("Inn events: 1") : "Night report should include inn events.";
        String weeklyReport = ReportSystem.buildWeeklyReportText(state);
        assert weeklyReport.contains("INN SUMMARY") : "Weekly report should include inn summary.";
        assert weeklyReport.contains("Room nights sold: 10") : "Weekly report should include room nights sold.";
        assert weeklyReport.contains("Inn revenue:") : "Weekly report should include inn revenue.";
        assert weeklyReport.contains("Inn maintenance accrued:") : "Weekly report should include inn maintenance.";
        assert weeklyReport.contains("Inn staff wages:") : "Weekly report should include inn staff wages.";
        assert weeklyReport.contains("Inn events: 2") : "Weekly report should include inn events.";
        assert weeklyReport.contains("Net inn profit:") : "Weekly report should include net inn profit.";
    }

    private static void testInnMenuSmoke() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Skipping Inn menu smoke test in headless mode.");
            return;
        }
        GameState state = GameFactory.newGame();
        WineBarGUI gui = new WineBarGUI(state);
        gui.openInnWindow();
    }
}
