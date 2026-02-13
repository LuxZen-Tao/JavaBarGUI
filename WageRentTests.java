import javax.swing.JTextPane;
import java.util.Random;

public class WageRentTests {
    public static void main(String[] args) {
        testWageMultiplierApplied();
        testWeeklyWageAccrualTotal();
        testRentAccrual();
        testWeeklyMinDueIncludesRentAndWages();
        testBarCapStepRentScaling();
        testUpgradeDailyRentDeltas();
        testInnTierRentScaling();
        testCombinedRentStacking();
        testPaydayRentDueTracksDailyAccrual();
        System.out.println("All WageRentTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testWageMultiplierApplied() {
        double multiplier = StaffFactory.wageMultiplier();
        assert multiplier > 1.0 : "Wage multiplier should be > 1.";

        Random baseRandom = new Random(42);
        Random scaledRandom = new Random(42);

        double baseTrainee = StaffFactory.baseWeeklyWageFor(Staff.Type.TRAINEE, baseRandom);
        double scaledTrainee = StaffFactory.templateFor(Staff.Type.TRAINEE, scaledRandom).weeklyWage();
        assert closeTo(scaledTrainee, baseTrainee * multiplier) : "Trainee wage should match multiplier.";

        double baseManager = StaffFactory.baseWeeklyWageFor(Staff.Type.MANAGER, baseRandom);
        double scaledManager = StaffFactory.templateFor(Staff.Type.MANAGER, scaledRandom).weeklyWage();
        assert closeTo(scaledManager, baseManager * multiplier) : "Manager wage should match multiplier.";

        assert scaledTrainee > 0.0 && scaledManager > 0.0 : "Wages should be positive.";
    }

    private static void testWeeklyWageAccrualTotal() {
        GameState state = GameFactory.newGame();
        Random r = new Random(7);
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "A", Staff.Type.EXPERIENCED, r));
        state.bohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "B", Staff.Type.CHEF, r));
        state.generalManagers.add(StaffFactory.createStaff(state.nextStaffId++, "C", Staff.Type.MANAGER, r));

        StaffSystem staffSystem = new StaffSystem(state, new EconomySystem(state, new UILogger(new JTextPane())), new UpgradeSystem(state));
        double weeklyTotal = 0.0;
        for (Staff st : state.fohStaff) weeklyTotal += st.getWeeklyWage();
        for (Staff st : state.bohStaff) weeklyTotal += st.getWeeklyWage();
        for (Staff st : state.generalManagers) weeklyTotal += st.getWeeklyWage();

        for (int day = 0; day < 7; day++) {
            staffSystem.accrueDailyWages();
        }
        double accrued = staffSystem.wagesDueRaw();
        assert closeTo(accrued, weeklyTotal) : "Weekly wages due should match summed weekly wages.";
    }

    private static void testRentAccrual() {
        GameState state = GameFactory.newGame();
        EconomySystem eco = new EconomySystem(state, new UILogger(new JTextPane()));
        state.roomsTotal = 0;
        eco.accrueDailyRent();
        assert closeTo(state.rentAccruedThisWeek, 60.0) : "Rent should accrue at 60 per day.";
        state.roomsTotal = 2;
        eco.accrueDailyRent();
        assert closeTo(state.rentAccruedThisWeek, 160.0) : "Rent should scale with rooms total.";
        for (int i = 0; i < 5; i++) {
            eco.accrueDailyRent();
        }
        assert closeTo(state.rentAccruedThisWeek, 660.0) : "Rent should accrue to 7 days of daily rent.";
    }

    private static void testWeeklyMinDueIncludesRentAndWages() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        StaffSystem staffSystem = new StaffSystem(state, new EconomySystem(state, new UILogger(new JTextPane())), new UpgradeSystem(state));
        Random r = new Random(5);
        state.fohStaff.add(StaffFactory.createStaff(state.nextStaffId++, "A", Staff.Type.SECURITY, r));
        for (int day = 0; day < 7; day++) {
            staffSystem.accrueDailyWages();
            new EconomySystem(state, new UILogger(new JTextPane())).accrueDailyRent();
        }
        double wagesDue = staffSystem.wagesDue();
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        assert closeTo(due.rent(), state.weeklyRentTotal()) : "Weekly rent due should match rent accrual.";
        assert closeTo(due.wages(), wagesDue) : "Weekly wages due should be included in breakdown.";
        assert due.total() >= due.rent() + due.wages() + due.innMaintenance() : "Total due should include rent, wages, and inn maintenance.";
    }

    private static void testBarCapStepRentScaling() {
        GameState state = GameFactory.newGame();

        state.upgradeBarCapBonus = 0;
        assert closeTo(state.barCapStepRentDelta(), 0.0) : "Base cap should have zero bar-cap rent delta.";

        state.upgradeBarCapBonus = 4;
        assert closeTo(state.barCapStepRentDelta(), 0.0) : "+4 cap should not add a rent step.";

        state.upgradeBarCapBonus = 5;
        assert closeTo(state.barCapStepRentDelta(), 10.0) : "+5 cap should add £10/day.";

        state.upgradeBarCapBonus = 10;
        assert closeTo(state.barCapStepRentDelta(), 20.0) : "+10 cap should add £20/day.";
    }

    private static void testUpgradeDailyRentDeltas() {
        GameState state = GameFactory.newGame();

        state.ownedUpgrades.add(PubUpgrade.WINE_CELLAR);
        assert closeTo(state.upgradesDailyRentDeltaTotal(), 5.0) : "Wine Cellar should add £5/day.";

        state.ownedUpgrades.add(PubUpgrade.DARTS);
        assert closeTo(state.upgradesDailyRentDeltaTotal(), 5.0) : "Upgrades with zero delta should not change rent.";

        state.ownedUpgrades.add(PubUpgrade.KITCHEN);
        assert closeTo(state.upgradesDailyRentDeltaTotal(), 15.0) : "Upgrade deltas should sum across installed upgrades.";
    }

    private static void testInnTierRentScaling() {
        GameState state = GameFactory.newGame();

        state.innTier = 0;
        assert closeTo(state.innTierRent(), 0.0) : "No inn should have zero inn tier rent.";

        state.innTier = 1;
        assert closeTo(state.innTierRent(), 10.0) : "Inn tier 1 should add £10/day.";

        state.innTier = 2;
        assert closeTo(state.innTierRent(), 20.0) : "Inn tier 2 should add £20/day.";

        state.innTier = 3;
        assert closeTo(state.innTierRent(), 30.0) : "Inn tier 3 should add £30/day.";
    }

    private static void testCombinedRentStacking() {
        GameState state = GameFactory.newGame();
        state.upgradeBarCapBonus = 10;
        state.ownedUpgrades.add(PubUpgrade.WINE_CELLAR);
        state.ownedUpgrades.add(PubUpgrade.KITCHEN);

        double expectedBase = 60.0;
        double expectedCap = 20.0;
        double expectedUpgrade = 15.0;
        double expectedRooms = 0.0;
        double expectedInnTier = 0.0;

        assert closeTo(state.getEffectiveDailyBaseRent(), expectedBase + expectedCap + expectedUpgrade)
                : "Effective base daily rent should include base + bar-cap steps + upgrade deltas.";
        assert closeTo(state.dailyRent(), expectedBase + expectedCap + expectedUpgrade + expectedRooms + expectedInnTier)
                : "Total daily rent should include base-side total plus rooms plus inn tier.";
    }

    private static void testPaydayRentDueTracksDailyAccrual() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        EconomySystem eco = new EconomySystem(state, new UILogger(new JTextPane()));

        state.upgradeBarCapBonus = 10;
        state.ownedUpgrades.add(PubUpgrade.WINE_CELLAR);
        state.ownedUpgrades.add(PubUpgrade.KITCHEN);

        double daily = state.dailyRent();
        for (int day = 0; day < 3; day++) {
            eco.accrueDailyRent();
        }

        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        assert closeTo(state.rentAccruedThisWeek, daily * 3.0)
                : "Rent accrual should equal effective daily rent × elapsed days.";
        assert closeTo(due.rent(), daily * 3.0)
                : "Payday due rent should match accrued rent.";
    }

    private static boolean closeTo(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
