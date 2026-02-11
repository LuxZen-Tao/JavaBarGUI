import javax.swing.JTextPane;

public class UpgradeBalanceDebugSim {
    public static void main(String[] args) {
        run("No upgrades", new PubUpgrade[]{});
        run("Throughput core", new PubUpgrade[]{PubUpgrade.FASTER_TAPS_I, PubUpgrade.EXTENDED_BAR, PubUpgrade.STAFF_TRAINING_I});
        run("Security core", new PubUpgrade[]{PubUpgrade.CCTV, PubUpgrade.REINFORCED_DOOR_I, PubUpgrade.LIGHTING_I});
    }

    private static void run(String label, PubUpgrade[] upgrades) {
        GameState s = GameFactory.newGame();
        s.cash = 30_000;
        UILogger log = new UILogger(new JTextPane());
        Simulation sim = new Simulation(s, log);
        for (PubUpgrade up : upgrades) {
            sim.installUpgradeForTest(up);
        }

        double unserved = 0;
        double refunds = 0;
        double chaosDelta = 0;
        double profit = 0;
        double bills = 0;
        for (int i = 0; i < 10; i++) {
            sim.openNight();
            double chaosBefore = s.chaos;
            while (s.nightOpen) {
                sim.playRound();
            }
            unserved += s.nightUnserved;
            refunds += s.nightRefundTotal;
            chaosDelta += (s.chaos - chaosBefore);
            profit += (s.nightRevenue - s.nightRoundCostsTotal);
            bills += (s.wagesAccruedThisWeek + s.rentAccruedThisWeek + s.securityUpkeepAccruedThisWeek);
        }

        System.out.printf("%s -> avgUnserved=%.2f avgRefunds=%.2f avgChaosDelta=%.2f avgProfit=%.2f billsStress=%.2f%n",
                label,
                unserved / 10.0,
                refunds / 10.0,
                chaosDelta / 10.0,
                profit / 10.0,
                bills / 10.0);
    }
}
