import javax.swing.JTextPane;
import java.util.ArrayList;
import java.util.List;

public class SoftFailTests {
    public static void main(String[] args) {
        testDebtSpiralStreakAndBailiffs();
        testBankruptcyCapAndReset();
        System.out.println("All SoftFailTests passed.");
        System.exit(0);
    }

    private static void testDebtSpiralStreakAndBailiffs() {
        GameState state = GameFactory.newGame();
        UILogger log = new UILogger(new JTextPane());
        Simulation sim = new Simulation(state, log);

        state.ownedUpgrades.add(PubUpgrade.DARTS);
        state.ownedUpgrades.add(PubUpgrade.POOL_TABLE);
        state.ownedUpgrades.add(PubUpgrade.CCTV);
        state.cash = 1000.0;

        for (int i = 0; i < 3; i++) {
            List<PaydayBill> bills = new ArrayList<>();
            PaydayBill missed = new PaydayBill(PaydayBill.Type.OTHER, "Stress Bill", 100.0, 100.0, null);
            missed.setSelectedAmount(0.0);
            bills.add(missed);
            sim.applyPaydayPayments(bills);
            if (state.consecutiveWeeksUnpaidMin != i + 1) {
                throw new IllegalStateException("Missed-min streak did not increment correctly.");
            }
        }

        int upgradesBeforeBailiffs = state.ownedUpgrades.size();
        List<PaydayBill> fourthMiss = new ArrayList<>();
        PaydayBill missedAgain = new PaydayBill(PaydayBill.Type.OTHER, "Stress Bill", 100.0, 100.0, null);
        missedAgain.setSelectedAmount(0.0);
        fourthMiss.add(missedAgain);
        sim.applyPaydayPayments(fourthMiss);

        if (state.consecutiveWeeksUnpaidMin < 4) {
            throw new IllegalStateException("Fourth missed minimum week not recorded.");
        }
        if (state.ownedUpgrades.size() >= upgradesBeforeBailiffs) {
            throw new IllegalStateException("Bailiffs did not remove upgrades on week 4 miss.");
        }
        if (!state.bailiffStigma) {
            throw new IllegalStateException("Bailiff stigma was not applied.");
        }
    }

    private static void testBankruptcyCapAndReset() {
        GameState state = GameFactory.newGame();
        UILogger log = new UILogger(new JTextPane());
        Simulation sim = new Simulation(state, log);

        state.ownedUpgrades.add(PubUpgrade.DARTS);
        state.ownedUpgrades.add(PubUpgrade.POOL_TABLE);
        state.pubLevel = 5;
        state.starCount = 2;

        sim.declareBankruptcy();

        if (!state.ownedUpgrades.isEmpty()) throw new IllegalStateException("Upgrades not cleared on bankruptcy.");
        if (state.pubLevel != 0) throw new IllegalStateException("Pub level not reset on bankruptcy.");
        if (state.starCount != 0) throw new IllegalStateException("Prestige not reset on bankruptcy.");
        if (Math.abs(state.supplierCreditCap() - 400.0) > 0.01) {
            throw new IllegalStateException("Supplier invoice credit cap not set to GBP 400.");
        }

        Wine wine = state.supplier.get(0);
        sim.buyFromSupplier(wine, 999);
        if (state.supplierWineCredit.getBalance() > 400.01) {
            throw new IllegalStateException("Supplier invoice credit exceeded bankruptcy cap.");
        }
    }
}
