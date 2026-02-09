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
        state.supplierBalance = cap;
        int beforeCount = state.rack.count();
        double beforeBalance = state.supplierBalance;

        sim.buyFromSupplier(wine, 1);

        assert state.supplierBalance == beforeBalance : "Supplier balance should not increase past cap.";
        assert state.rack.count() == beforeCount : "Purchase should be blocked when credit cap exceeded.";
    }

    private static void testSupplierBalanceOnlyIncreasesOnStock() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        double beforeBalance = state.supplierBalance;
        sim.openCreditLine(Bank.TOWNLAND);
        assert state.supplierBalance == beforeBalance : "Supplier balance should not change for non-stock actions.";
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

        state.supplierBalance = 200.0;
        PaydayBill supplier = new PaydayBill(PaydayBill.Type.SUPPLIER, "Supplier", state.supplierMinDue(), state.supplierBalance, null);
        supplier.setSelectedAmount(state.supplierBalance);

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
}
