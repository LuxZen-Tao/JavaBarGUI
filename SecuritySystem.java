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
        int eff = s.baseSecurityLevel;

        //  upgrades
        eff += s.upgradeSecurityBonus;

        // bouncer + general manager
        if (s.bouncersHiredTonight > 0) eff += (s.bouncersHiredTonight * 2);
        if (s.hasSkilledManager()) eff += 1;
        eff += s.staffSecurityBonus();

        return Math.max(0, eff);
    }

    public void upgradeBaseSecurity() {
        eco.payOrDebt(25.0, "Security upgrade");
        if (s.debt > s.maxDebt) return;
        s.baseSecurityLevel++;
        log.pos("Upgraded base security to " + s.baseSecurityLevel + ".");
    }

    public void hireBouncerTonight() {
        if (!s.nightOpen) { log.neg("Bouncer can only be hired while pub is OPEN."); return; }
        if (s.bouncersHiredTonight >= s.bouncerCap) {
            log.info("Bouncer cap reached (" + s.bouncerCap + ").");
            return;
        }

        double theftRed = 0.10 + (s.random.nextInt(31) / 100.0);
        double negRed   = 0.10 + (s.random.nextInt(31) / 100.0);
        double fightRed = 0.10 + (s.random.nextInt(31) / 100.0);

        s.bouncerTheftReduction = Math.min(0.75, s.bouncerTheftReduction + theftRed);
        s.bouncerNegReduction   = Math.min(0.75, s.bouncerNegReduction + negRed);
        s.bouncerFightReduction = Math.min(0.75, s.bouncerFightReduction + fightRed);

        int basePay = (int)Math.round(30 * BOUNCER_COST_MULTIPLIER);
        int variance = (int)Math.round(30 * BOUNCER_COST_MULTIPLIER);
        double scale = 1.0 + (0.10 * s.bouncersHiredTonight);
        s.bouncerNightPay = (basePay + s.random.nextInt(variance + 1)) * scale;
        eco.payOrDebt(s.bouncerNightPay, "Bouncer (tonight)", CostTag.BOUNCER);
        if (s.debt > s.maxDebt) return;

        s.nightRoundCostsTotal += s.bouncerNightPay;
        s.bouncersHiredTonight++;

        log.pos(" Bouncer hired (" + s.bouncersHiredTonight + "/" + s.bouncerCap + "): theft -"
                + pct(s.bouncerTheftReduction)
                + ", neg -" + pct(s.bouncerNegReduction)
                + ", fights -" + pct(s.bouncerFightReduction));
    }

    private static String pct(double v) { return (int)(v * 100) + "%"; }
}
