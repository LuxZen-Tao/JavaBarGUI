import javax.swing.JTextPane;
import java.util.ArrayList;
import java.util.List;

public class BankingRefactorTests {
    public static void main(String[] args) {
        testSupplierCreditCapBlocksPurchases();
        testSupplierBalanceOnlyIncreasesOnStock();
        testSupplierInvoiceAnytimeReducesBalance();
        testSupplierInvoicePaymentFreesCredit();
        testSupplierInvoiceNoMinPenalty();
        testSupplierInvoicePaymentSources();
        testNoAutoPayAtEndOfWeek();
        testLateFeeAndPenaltyOnUnderMin();
        testPenaltyRecoveryStages();
        testCreditScoreBonusForFullPay();
        testCreditLineToCreditLinePayment();
        testWeeklyMinDueIncludesSupplier();
        testApplyCreditPrefersRequestedLine();
        testApplyCreditFallsBackToLowerAprLine();
        testObservationEngineMajorEventOverridesThrottle();
        testObservationEngineNoBouncerClaims();
        testObservationEngineLengthClamp();
        testBouncerMitigatesRepDamage();
        testPubLevelRequiresTimeAndMilestones();
        testPubLevelAffectsSupplierCap();
        System.out.println("All BankingRefactorTests passed.");
        System.exit(0);
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

    private static void testSupplierInvoiceAnytimeReducesBalance() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.cash = 500.0;
        state.supplierWineCredit.addBalance(200.0);
        double beforeBalance = state.supplierWineCredit.getBalance();
        double beforeCash = state.cash;

        Simulation.SupplierPaymentResult result = sim.paySupplierInvoice(SupplierAccount.WINE, 80.0, "CASH");

        assert result.success() : "Supplier invoice payment should succeed.";
        assert Math.abs(state.supplierWineCredit.getBalance() - (beforeBalance - 80.0)) < 0.01
                : "Supplier balance should decrease immediately after payment.";
        assert Math.abs(state.cash - (beforeCash - 80.0)) < 0.01 : "Cash should decrease with payment.";
    }

    private static void testSupplierInvoicePaymentFreesCredit() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        Wine wine = state.supplier.get(0);
        double cap = state.supplierCreditCap();
        state.cash = 500.0;
        state.supplierWineCredit.addBalance(cap);

        int beforeCount = state.rack.count();
        sim.buyFromSupplier(wine, 1);
        assert state.rack.count() == beforeCount : "Purchase should be blocked when supplier credit is maxed.";

        Simulation.SupplierPaymentResult result = sim.paySupplierInvoice(SupplierAccount.WINE, 150.0, "CASH");
        assert result.success() : "Supplier invoice payment should succeed.";
        sim.buyFromSupplier(wine, 1);
        assert state.rack.count() > beforeCount : "Purchase should succeed after freeing supplier credit.";
    }

    private static void testSupplierInvoiceNoMinPenalty() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.cash = 200.0;
        state.supplierWineCredit.addBalance(200.0);
        double minDue = state.supplierWineMinDue();
        double penaltyBefore = state.supplierWineCredit.getPenaltyAddOnApr();
        double lateBefore = state.supplierWineCredit.getLateFeesThisWeek();

        Simulation.SupplierPaymentResult result = sim.paySupplierInvoice(SupplierAccount.WINE, minDue - 10.0, "CASH");

        assert result.success() : "Supplier invoice payment should succeed for a custom amount.";
        assert state.supplierWineCredit.getPenaltyAddOnApr() == penaltyBefore
                : "Any-time supplier payments should not apply min-due penalties.";
        assert state.supplierWineCredit.getLateFeesThisWeek() == lateBefore
                : "Any-time supplier payments should not apply late fees.";
    }

    private static void testSupplierInvoicePaymentSources() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.supplierWineCredit.addBalance(120.0);
        state.cash = 40.0;

        CreditLine line = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        double available = line.availableCredit();

        Simulation.SupplierPaymentResult cashResult = sim.paySupplierInvoice(SupplierAccount.WINE, 40.0, "CASH");
        assert cashResult.success() : "Cash payment should succeed.";

        Simulation.SupplierPaymentResult lineResult = sim.paySupplierInvoice(SupplierAccount.WINE, 60.0, line.getId());
        assert lineResult.success() : "Credit line payment should succeed.";
        assert line.getBalance() > 0.0 : "Credit line balance should increase when used for payment.";

        Simulation.SupplierPaymentResult failResult = sim.paySupplierInvoice(SupplierAccount.WINE, available + 10.0, line.getId());
        assert !failResult.success() : "Payment should fail when exceeding credit line limit.";
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
                + state.innMaintenanceAccruedWeekly
                + state.wagesAccruedThisWeek; // Tips handled separately, not part of wages
        assert Math.abs(due.supplier() - expectedSupplierMin) < 0.001 : "Supplier minimum due should be included in weekly totals.";
        assert Math.abs(due.total() - expectedTotal) < 0.001 : "Weekly minimum due total should include supplier mins.";
    }


    private static void testApplyCreditPrefersRequestedLine() {
        GameState state = GameFactory.newGame();
        CreditLine lineA = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        CreditLine lineB = state.creditLines.openLine(Bank.SANTNERE, state.random);
        assert lineA != null && lineB != null : "Expected two credit lines to open.";

        boolean ok = state.creditLines.applyCredit(120.0, lineB.getId());
        assert ok : "applyCredit should succeed when preferred line can cover the amount.";
        assert lineB.getBalance() >= 120.0 : "Preferred line should receive the borrowed amount.";
        assert lineA.getBalance() == 0.0 : "Non-selected line should remain untouched.";
    }

    private static void testApplyCreditFallsBackToLowerAprLine() {
        GameState state = GameFactory.newGame();
        CreditLine lineA = state.creditLines.openLine(Bank.TOWNLAND, state.random);
        CreditLine lineB = state.creditLines.openLine(Bank.SANTNERE, state.random);
        assert lineA != null && lineB != null : "Expected two credit lines to open.";

        // Force deterministic effective APR ordering so fallback choice is testable.
        lineA.setPenaltyAddOnApr(0.25);
        lineB.setPenaltyAddOnApr(0.0);

        boolean ok = state.creditLines.applyCredit(75.0, "missing-id");
        assert ok : "applyCredit should still succeed without a valid preferred id.";
        assert lineB.getBalance() >= 75.0 : "Lower APR line should be selected as fallback.";
        assert lineA.getBalance() == 0.0 : "Higher APR line should not be selected when a cheaper line exists.";
    }

    private static void testObservationEngineMajorEventOverridesThrottle() {
        GameState state = GameFactory.newGame();
        state.random.setSeed(1);
        state.lastObservationRound = 10;
        ObservationEngine engine = new ObservationEngine();
        ObservationEngine.ObservationContext ctx = new ObservationEngine.ObservationContext(
                11,
                6,
                0,
                1,
                0,
                0,
                2,
                1,
                state.priceMultiplier,
                0.0,
                state.rack.count(),
                state.foodRack.count(),
                state.kitchenUnlocked,
                state.bouncersHiredTonight,
                false,
                false
        );

        ObservationEngine.ObservationResult result = engine.nextObservation(state, ctx);
        assert result != null : "Major events should force observation updates despite throttling.";
    }

    private static void testObservationEngineNoBouncerClaims() {
        GameState state = GameFactory.newGame();
        state.random.setSeed(2);
        state.lastObservationRound = 1;
        ObservationEngine engine = new ObservationEngine();
        ObservationEngine.ObservationContext ctx = new ObservationEngine.ObservationContext(
                2,
                6,
                0,
                1,
                0,
                0,
                1,
                1,
                state.priceMultiplier,
                0.0,
                state.rack.count(),
                state.foodRack.count(),
                state.kitchenUnlocked,
                0,
                false,
                false
        );

        ObservationEngine.ObservationResult result = engine.nextObservation(state, ctx);
        assert result != null : "Observation should be produced for a fight trigger.";
        String text = result.text().toLowerCase();
        assert !text.contains("bouncer") && !text.contains("door staff") : "No bouncer text should appear without bouncers.";
    }

    private static void testObservationEngineLengthClamp() {
        GameState state = GameFactory.newGame();
        state.random.setSeed(3);
        state.lastObservationRound = 0;
        ObservationEngine engine = new ObservationEngine();
        ObservationEngine.ObservationContext ctx = new ObservationEngine.ObservationContext(
                5,
                12,
                3,
                0,
                0,
                0,
                4,
                3,
                1.35,
                0.25,
                state.rack.count(),
                state.foodRack.count(),
                state.kitchenUnlocked,
                state.bouncersHiredTonight,
                false,
                false
        );

        ObservationEngine.ObservationResult result = engine.nextObservation(state, ctx);
        assert result != null : "Observation should be produced for busy/price triggers.";
        assert result.text().length() <= ObservationEngine.MAX_OBSERVATION_LENGTH
                : "Observation text should stay within display constraints.";
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

        state.prestigeMilestones.add(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS);
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
