import javax.swing.JTextPane;

public class UpgradeSystemTests {
    public static void main(String[] args) {
        snapshotContainsOwnedUpgradeEffects();
        availabilityReportsReasons();
        buyingUpgradeChargesCashAndQueuesInstall();
        System.out.println("All UpgradeSystemTests passed.");
    }

    private static void snapshotContainsOwnedUpgradeEffects() {
        GameState state = GameFactory.newGame();
        state.ownedUpgrades.add(PubUpgrade.FASTER_TAPS_I);
        state.ownedUpgrades.add(PubUpgrade.BETTER_GLASSWARE_III);
        state.ownedUpgrades.add(PubUpgrade.BURGLAR_ALARM_III);

        UpgradeSystem upgrades = new UpgradeSystem(state);
        UpgradeSystem.UpgradeModifierSnapshot snap = upgrades.buildModifierSnapshot();

        assert snap.serveCapBonus() >= 1 : "Serve cap bonus should include tap upgrade.";
        assert snap.tipBonusPct() >= 0.03 : "Tip bonus should include glassware tier.";
        assert snap.incidentChanceMultiplier() < 1.0 : "Alarm tier should reduce incident chance.";
        assert snap.lossSeverityMultiplier() <= 1.0 : "Loss multiplier must not exceed baseline.";
        assert snap.wageEfficiencyPct() >= 0.0 && snap.wageEfficiencyPct() <= 0.25 : "Wage efficiency clamp should hold.";
    }

    private static void availabilityReportsReasons() {
        GameState state = GameFactory.newGame();
        UILogger log = new UILogger(new JTextPane());
        MilestoneSystem milestones = new MilestoneSystem(state, log);

        MilestoneSystem.UpgradeAvailability cctv = milestones.getUpgradeAvailability(PubUpgrade.CCTV, 50.0);
        assert !cctv.unlocked() : "CCTV should be locked without milestone.";
        assert cctv.missingRequirements().stream().anyMatch(s -> s.contains("Margin With Manners"))
                : "CCTV lock reason should mention required milestone.";
        assert cctv.missingRequirements().stream().anyMatch(s -> s.contains("Insufficient"))
                : "Lock reason should include insufficient funds when cash is low.";
    }

    private static void buyingUpgradeChargesCashAndQueuesInstall() {
        GameState state = GameFactory.newGame();
        state.cash = 10_000.0;
        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));

        int pendingBefore = state.pendingUpgradeInstalls.size();
        double cashBefore = state.cash;
        sim.buyUpgrade(PubUpgrade.DARTS);

        assert state.pendingUpgradeInstalls.size() == pendingBefore + 1 : "Upgrade purchase should create pending install.";
        assert state.cash < cashBefore : "Upgrade purchase should charge cash immediately.";
    }
}
