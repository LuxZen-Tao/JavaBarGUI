import javax.swing.JTextPane;
import java.util.Random;

public class WageRentTests {
    public static void main(String[] args) {
        testWageMultiplierApplied();
        testWeeklyWageAccrualTotal();
        testRentAccrual();
        testWeeklyMinDueIncludesRentAndWages();
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

    private static boolean closeTo(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }
}
