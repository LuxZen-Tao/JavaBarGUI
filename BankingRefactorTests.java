import javax.swing.JTextPane;
import java.util.ArrayList;
import java.util.List;

public class BankingRefactorTests {
    public static void main(String[] args) {
        testSupplierCreditCapBlocksPurchases();
        testSupplierBalanceOnlyIncreasesOnStock();
        testNoAutoPayAtEndOfWeek();
        testLateFeeAndPenaltyOnUnderMin();
        testPenaltyRecoveryStages();
        testCreditScoreBonusForFullPay();
        testCreditLineToCreditLinePayment();
        testWeeklyMinDueIncludesSupplier();
        testBouncerMitigatesRepDamage();
        testPubLevelRequiresTimeAndMilestones();
        testPubLevelAffectsSupplierCap();
        System.out.println("All BankingRefactorTests passed.");
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testSupplierCreditCapBlocksPurchases() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Wine wine = state.supplier.get(0);

        double cap = state.supplierCreditCap();
        state.supplierWineCredit.addBalance(cap);
        int beforeCount = state.rack.count();
        double beforeBalance = state.supplierWineCredit.getBalance();

        sim.buyFromSupplier(wine, 1);

        assert state.supplierWineCredit.getBalance() == beforeBalance : "Supplier balance should not increase past cap.";
        assert state.rack.count() == beforeCount : "Purchase should be blocked when credit cap exceeded.";
    }

    private static void testSupplierBalanceOnlyIncreasesOnStock() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        double beforeWine = state.supplierWineCredit.getBalance();
        double beforeFood = state.supplierFoodCredit.getBalance();
        sim.openCreditLine(Bank.TOWNLAND);
        assert state.supplierWineCredit.getBalance() == beforeWine : "Wine supplier balance should not change for non-stock actions.";
        assert state.supplierFoodCredit.getBalance() == beforeFood : "Food supplier balance should not change for non-stock actions.";

        Wine wine = state.supplier.get(0);
        double wineBalanceBefore = state.supplierWineCredit.getBalance();
        sim.buyFromSupplier(wine, 1);
        assert state.supplierWineCredit.getBalance() > wineBalanceBefore : "Wine supplier balance should increase after wine purchase.";

        state.kitchenUnlocked = true;
        Food food = state.foodSupplier.get(0);
        double foodBalanceBefore = state.supplierFoodCredit.getBalance();
        sim.buyFoodFromSupplier(food, 1);
        assert state.supplierFoodCredit.getBalance() > foodBalanceBefore : "Food supplier balance should increase after food purchase.";
    }

    private static void testNoAutoPayAtEndOfWeek() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        CreditLine line = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        line.addBalance(500.0);
        state.creditLines.updateWeeklyPayment(line);
        double cashBefore = state.cash;

        state.creditLines.applyWeeklyInterest(state, new UILogger(new JTextPane()));

        assert state.cash == cashBefore : "Cash should not change when weekly interest is applied.";
        assert line.getBalance() >= 500.0 : "Balance should not be auto-paid on weekly processing.";
    }

    private static void testLateFeeAndPenaltyOnUnderMin() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        CreditLine line = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        line.addBalance(400.0);
        state.creditLines.updateWeeklyPayment(line);

        PaydayBill bill = new PaydayBill(PaydayBill.Type.CREDIT_LINE, "Test Line", line.getWeeklyPayment(), line.getBalance(), line.getId());
        bill.setSelectedAmount(Math.max(0.0, line.getWeeklyPayment() - 10.0));

        List<PaydayBill> bills = new ArrayList<>();
        bills.add(bill);

        double balanceBefore = line.getBalance();
        sim.applyPaydayPayments(bills);

        assert line.getPenaltyAddOnApr() > 0.0 : "Penalty add-on APR should increase after under-min payment.";
        assert line.getBalance() > balanceBefore - bill.getSelectedAmount() : "Late fee should increase balance.";
    }

    private static void testPenaltyRecoveryStages() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.cash = 100000.0;
        CreditLine line = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        line.setPenaltyAddOnApr(0.10);
        line.setPenaltyRecoveryStage(0);
        line.setConsecutiveFullPays(0);

        double[] expected = new double[]{0.05, 0.035, 0.028, 0.0};
        for (int stage = 0; stage < 4; stage++) {
            for (int i = 0; i < 3; i++) {
                line.addBalance(300.0);
                state.creditLines.updateWeeklyPayment(line);
                PaydayBill bill = new PaydayBill(PaydayBill.Type.CREDIT_LINE, "Line", line.getWeeklyPayment(), line.getBalance(), line.getId());
                bill.setSelectedAmount(line.getBalance());
                List<PaydayBill> bills = new ArrayList<>();
                bills.add(bill);
                sim.applyPaydayPayments(bills);
            }
            double actual = line.getPenaltyAddOnApr();
            assert Math.abs(actual - expected[stage]) < 0.0001 : "Penalty recovery stage mismatch at stage " + stage;
        }
    }

    private static void testCreditScoreBonusForFullPay() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.cash = 100000.0;
        int beforeScore = state.creditScore;

        state.supplierWineCredit.addBalance(200.0);
        PaydayBill supplier = new PaydayBill(PaydayBill.Type.SUPPLIER, "Wine supplier", state.supplierWineMinDue(), state.supplierWineCredit.getBalance(), "SUPPLIER_WINE");
        supplier.setSelectedAmount(state.supplierWineCredit.getBalance());

        CreditLine line = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        line.addBalance(200.0);
        state.creditLines.updateWeeklyPayment(line);
        PaydayBill credit = new PaydayBill(PaydayBill.Type.CREDIT_LINE, "Line", line.getWeeklyPayment(), line.getBalance(), line.getId());
        credit.setSelectedAmount(line.getBalance());

        List<PaydayBill> bills = new ArrayList<>();
        bills.add(supplier);
        bills.add(credit);

        sim.applyPaydayPayments(bills);

        assert state.creditScore > beforeScore : "Credit score should increase when all bills are paid in full.";
    }

    private static void testCreditLineToCreditLinePayment() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);

        CreditLine lineA = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        CreditLine lineB = state.creditLines.openLine(Bank.SANTNERE, state.random);
        lineA.addBalance(300.0);
        state.creditLines.updateWeeklyPayment(lineA);

        PaydayBill bill = new PaydayBill(PaydayBill.Type.CREDIT_LINE, "Line A", lineA.getWeeklyPayment(), lineA.getBalance(), lineA.getId());
        bill.setSelectedAmount(lineA.getWeeklyPayment());
        bill.setSelectedSourceId(lineB.getId());

        List<PaydayBill> bills = new ArrayList<>();
        bills.add(bill);

        double balanceABefore = lineA.getBalance();
        double balanceBBefore = lineB.getBalance();

        sim.applyPaydayPayments(bills);

        assert lineA.getBalance() < balanceABefore : "Line A balance should decrease when paid by another credit line.";
        assert lineB.getBalance() > balanceBBefore : "Line B balance should increase when used as payment source.";
    }

    private static void testWeeklyMinDueIncludesSupplier() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.supplierWineCredit.addBalance(150.0);
        state.supplierFoodCredit.addBalance(120.0);
        Simulation.WeeklyDueBreakdown due = sim.weeklyMinDueBreakdown();
        double expectedSupplierMin = state.supplierWineMinDue() + state.supplierFoodMinDue();
        double expectedTotal = expectedSupplierMin
                + state.rentAccruedThisWeek
                + state.securityUpkeepAccruedThisWeek
                + state.tipsThisWeek * 0.50
                + state.wagesAccruedThisWeek;
        assert Math.abs(due.supplier() - expectedSupplierMin) < 0.001 : "Supplier minimum due should be included in weekly totals.";
        assert Math.abs(due.total() - expectedTotal) < 0.001 : "Weekly minimum due total should include supplier mins.";
    }

    private static void testBouncerMitigatesRepDamage() {
        GameState stateNoBouncer = GameFactory.newGame();
        GameState stateWithBouncer = GameFactory.newGame();
        UILogger log = new UILogger(new JTextPane());
        EconomySystem ecoNo = new EconomySystem(stateNoBouncer, log);
        EconomySystem ecoYes = new EconomySystem(stateWithBouncer, log);
        EventSystem eventsNo = new EventSystem(stateNoBouncer, ecoNo, log);
        EventSystem eventsYes = new EventSystem(stateWithBouncer, ecoYes, log);

        stateNoBouncer.reputation = 50;
        stateWithBouncer.reputation = 50;
        stateWithBouncer.bouncersHiredTonight = 1;

        int repBeforeNo = stateNoBouncer.reputation;
        int repBeforeYes = stateWithBouncer.reputation;

        eventsNo.triggerFight("test", 0.0);
        eventsYes.triggerFight("test", 0.0);

        int lossNo = repBeforeNo - stateNoBouncer.reputation;
        int lossYes = repBeforeYes - stateWithBouncer.reputation;
        assert lossYes < lossNo : "Bouncer mitigation should reduce rep damage.";
    }

    private static void testPubLevelRequiresTimeAndMilestones() {
        GameState state = GameFactory.newGame();
        PubLevelSystem levels = new PubLevelSystem();

        state.weekCount = 4;
        levels.updatePubLevel(state);
        assert state.pubLevel == 0 : "Pub level should not increase without milestones.";

        state.achievedMilestones.add(MilestoneSystem.Milestone.FIVE_NIGHTS);
        levels.updatePubLevel(state);
        assert state.pubLevel >= 1 : "Pub level should increase once time + milestone requirements met.";
    }

    private static void testPubLevelAffectsSupplierCap() {
        GameState state = GameFactory.newGame();
        double baseCap = state.supplierCreditCap();
        state.pubLevel = 2;
        double boostedCap = state.supplierCreditCap();
        assert boostedCap > baseCap : "Supplier credit cap should increase with pub level.";
    }
}
