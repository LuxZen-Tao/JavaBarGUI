import javax.swing.JTextPane;
import java.util.Random;

public class InnSystemTests {
    public static void main(String[] args) {
        testInnUnlock();
        testInnTierUpgrade();
        testBookingDeterministic();
        testReceptionEffect();
        testHousekeepingEffect();
        testDutyManagerEffects();
        testMaintenanceAccrualAndPayday();
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

    private static void testDutyManagerEffects() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        sim.installUpgradeForTest(PubUpgrade.INN_WING_2);
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
