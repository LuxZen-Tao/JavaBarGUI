public class UpgradeSystem {

    private final GameState s;

    public UpgradeSystem(GameState s) { this.s = s; }

    public double trafficMultiplier() {
        double pct = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) pct += u.getTrafficBonusPct();
        return 1.0 + pct;
    }

    public int repDriftPerRound() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getRepDriftPerRound();
        return sum;
    }

    public int eventBonusChance() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getEventBonusChance();
        return sum;
    }

    public int barCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getBarCapBonus();
        return sum;
    }

    public int serveCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getServeCapBonus();
        return sum;
    }

    public int rackCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getRackCapBonus();
        return sum;
    }

    public int foodRackCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getFoodRackCapBonus();
        return sum;
    }

    public int securityBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getSecurityBonus();
        return sum;
    }

    public int staffCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getStaffCapBonus();
        return sum;
    }

    public int bouncerCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getBouncerCapBonus();
        return sum;
    }

    public int managerCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getManagerCapBonus();
        return sum;
    }

    public int chefCapBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getChefCapBonus();
        return sum;
    }

    public int kitchenQualityBonus() {
        int sum = 0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getKitchenQualityBonus();
        return sum;
    }

    public double refundRiskReductionPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getRefundRiskReductionPct();
        return Math.min(0.40, Math.max(0.0, sum));
    }

    public double staffMisconductReductionPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getStaffMisconductReductionPct();
        return Math.min(0.40, Math.max(0.0, sum));
    }

    public double tipBonusPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getTipBonusPct();
        return Math.max(0.0, sum);
    }

    public double eventDamageReductionPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getEventDamageReductionPct();
        return Math.min(0.35, Math.max(0.0, sum));
    }

    public double riskReductionPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getRiskReductionPct();
        return Math.min(0.35, Math.max(0.0, sum));
    }

    /** Total wage reduction % from upgrades, clamped so it can't go silly. */
    public double wageEfficiencyPct() {
        double sum = 0.0;
        for (PubUpgrade u : s.ownedUpgrades) sum += u.getWageEfficiencyPct();
        return Math.min(0.25, Math.max(0.0, sum)); // cap at -25%
    }
}
