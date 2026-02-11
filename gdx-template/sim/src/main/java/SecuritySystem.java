// SecuritySystem.java
public class SecuritySystem {
    public static final double SECURITY_UPKEEP_PER_LEVEL = 1.575;
    private static final double BOUNCER_COST_MULTIPLIER = 1.10;

    private final GameState s;
    private final EconomySystem eco;
    private final UILogger log;

    public SecuritySystem(GameState s, EconomySystem eco, UILogger log) {
        this.s = s;
        this.eco = eco;
        this.log = log;
    }

    public int effectiveSecurity() {
        int eff = s.baseSecurityLevel + s.legacy.baseSecurityBonus;

        //  upgrades
        eff += s.upgradeSecurityBonus;

        // policy
        if (s.securityPolicy != null) {
            eff += s.securityPolicy.getSecurityBonus();
        }

        // bouncer + general manager
        if (s.bouncersHiredTonight > 0) eff += (s.bouncersHiredTonight * 2);
        if (s.hasSkilledManager()) eff += 1;
        eff += s.staffSecurityBonus();

        return Math.max(0, eff);
    }

    public SecurityBreakdown breakdown() {
        int base = s.baseSecurityLevel + s.legacy.baseSecurityBonus;
        int upgrades = s.upgradeSecurityBonus;
        int policy = s.securityPolicy != null ? s.securityPolicy.getSecurityBonus() : 0;
        int bouncers = s.bouncersHiredTonight > 0 ? (s.bouncersHiredTonight * 2) : 0;
        int manager = s.hasSkilledManager() ? 1 : 0;
        int staff = s.staffSecurityBonus();
        int total = Math.max(0, base + upgrades + policy + bouncers + manager + staff);
        return new SecurityBreakdown(base, upgrades, policy, bouncers, manager, staff, total);
    }

    public record SecurityBreakdown(int base,
                                    int upgrades,
                                    int policy,
                                    int bouncers,
                                    int manager,
                                    int staff,
                                    int total) {
    }

    public void upgradeBaseSecurity() {
        double cost = nextUpgradeCost();
        if (!eco.tryPay(cost, TransactionType.UPGRADE, "Security upgrade", CostTag.UPGRADE)) return;
        s.baseSecurityLevel++;
        log.pos("Upgraded base security to " + s.baseSecurityLevel + ".");
    }

    public double nextUpgradeCost() {
        int level = Math.max(0, s.baseSecurityLevel);
        double base = 22.0;
        double linear = 5.0 * level;
        double curve = Math.pow(1.14, level) * 12.0;
        return base + linear + curve;
    }

    public void hireBouncerTonight() {
        if (!s.nightOpen) { log.neg("Bouncer can only be hired while pub is OPEN."); return; }
        if (s.bouncersHiredTonight >= s.bouncerCap) {
            log.info("Bouncer cap reached (" + s.bouncerCap + ").");
            return;
        }

        BouncerQuality quality = rollBouncerQuality();
        double theftRed = 0.10 + (s.random.nextInt(31) / 100.0);
        double negRed   = 0.10 + (s.random.nextInt(31) / 100.0);
        double fightRed = 0.10 + (s.random.nextInt(31) / 100.0);

        s.bouncerTheftReduction = Math.min(0.75, s.bouncerTheftReduction + theftRed);
        s.bouncerNegReduction   = Math.min(0.75, s.bouncerNegReduction + negRed);
        s.bouncerFightReduction = Math.min(0.75, s.bouncerFightReduction + fightRed);

        int basePay = (int)Math.round(30 * BOUNCER_COST_MULTIPLIER);
        int variance = (int)Math.round(30 * BOUNCER_COST_MULTIPLIER);
        double scale = 1.0 + (0.10 * s.bouncersHiredTonight);
        double nightPay = (basePay + s.random.nextInt(variance + 1)) * scale;
        if (!eco.tryPay(nightPay, TransactionType.WAGES, "Bouncer (tonight)", CostTag.BOUNCER)) return;

        s.bouncerNightPay = nightPay;
        s.nightRoundCostsTotal += s.bouncerNightPay;
        s.bouncersHiredTonight++;
        s.bouncerQualitiesTonight.add(quality);

        log.pos(" Bouncer hired (" + s.bouncersHiredTonight + "/" + s.bouncerCap + ", "
                + qualityLabel(quality) + "): theft -"
                + pct(s.bouncerTheftReduction)
                + ", neg -" + pct(s.bouncerNegReduction)
                + ", fights -" + pct(s.bouncerFightReduction));
    }

    private BouncerQuality rollBouncerQuality() {
        int roll = s.random.nextInt(100);
        if (roll < 35) return BouncerQuality.LOW;
        if (roll < 70) return BouncerQuality.MEDIUM;
        return BouncerQuality.HIGH;
    }

    private String qualityLabel(BouncerQuality quality) {
        if (quality == null) return "Unknown";
        return switch (quality) {
            case LOW -> "Low";
            case MEDIUM -> "Med";
            case HIGH -> "High";
        };
    }

    private static String pct(double v) { return (int)(v * 100) + "%"; }
}
