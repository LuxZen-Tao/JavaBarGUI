import java.util.List;

public class PrestigeSystem {
    public static final int MAX_STARS = 5;
    public static final int MAX_LEVEL = 5;

    /**
     * Diminishing returns by the star you are ABOUT to gain.
     * nextStar=1: 1.00, 2: 0.70, 3: 0.50, 4: 0.35, 5: 0.25
     */
    private static final double[] STAR_FACTORS = {1.00, 0.70, 0.50, 0.35, 0.25};

    public boolean isMaxStars(GameState s) {
        return s.starCount >= MAX_STARS;
    }

    public boolean isPrestigeEligible(GameState s, PubLevelSystem pubLevelSystem) {
        if (isMaxStars(s)) return false;
        return s.pubLevel >= MAX_LEVEL
                && pubLevelSystem.meetsLevelRequirement(s, MAX_LEVEL + 1);
    }

    public int nextStar(GameState s) {
        return Math.min(MAX_STARS, s.starCount + 1);
    }

    public double starFactor(int nextStar) {
        if (nextStar < 1 || nextStar > MAX_STARS) return 0.0;
        return STAR_FACTORS[nextStar - 1];
    }

    public LegacyBonuses computeBaseAward(GameState s, UpgradeSystem upgrades) {
        int inventoryBase = Math.max(0, upgrades.rackCapBonus() + upgrades.foodRackCapBonus());
        int inventoryAward = Math.min(80, (int) Math.round(inventoryBase * 0.12));

        int innAward = Math.min(12, (int) Math.round(s.roomsTotal * 0.08));

        double trafficFromUpgrades = Math.max(0.0, upgrades.trafficMultiplier() - 1.0);
        double trafficAward = Math.min(0.12, trafficFromUpgrades * 0.20);

        int upgradeCount = Math.max(0, s.ownedUpgrades.size());
        int supplierAward = Math.min(320, upgradeCount * 12);

        int securityAward = Math.min(6, Math.max(0, upgrades.securityBonus()));

        double staffEffAward = Math.min(0.10, Math.max(0.0, upgrades.wageEfficiencyPct() * 0.60));

        return new LegacyBonuses(
                inventoryAward,
                innAward,
                trafficAward,
                supplierAward,
                securityAward,
                staffEffAward
        );
    }

    public LegacyBonuses computePrestigeAward(GameState s, UpgradeSystem upgrades) {
        int nextStar = nextStar(s);
        double factor = starFactor(nextStar);
        LegacyBonuses base = computeBaseAward(s, upgrades);
        return base.scaled(factor);
    }

    public PrestigePreview buildPreview(GameState s, UpgradeSystem upgrades, PubLevelSystem pubLevelSystem) {
        int nextStar = nextStar(s);
        boolean maxed = isMaxStars(s);
        boolean eligible = isPrestigeEligible(s, pubLevelSystem);
        double factor = starFactor(nextStar);
        LegacyBonuses award = computePrestigeAward(s, upgrades);
        LegacyBonuses baseAward = computeBaseAward(s, upgrades);

        String title = maxed ? "Max ★ reached" : "Prestige Preview";
        String eligibilityLine = eligible
                ? "Eligible for prestige now (Level " + s.pubLevel + ")."
                : "Not eligible for prestige yet.";

        String factorLine = "Diminishing returns (next ★" + nextStar + "): x"
                + String.format("%.2f", factor);

        StringBuilder gains = new StringBuilder();
        gains.append("Gain: +1★ (Stars are permanent)\n");
        gains.append("Legacy bonuses to bank now:\n");
        for (String line : award.detailLines()) {
            gains.append(" - ").append(line).append("\n");
        }

        StringBuilder losses = new StringBuilder();
        losses.append("Lose on prestige:\n");
        losses.append(" - Pub level resets to 0\n");
        losses.append(" - Upgrade tiers reset (purchases cleared)\n");
        losses.append(" - Inn/Kitchen tiers reset\n");

        StringBuilder notes = new StringBuilder();
        notes.append("Award mapping: upgrades -> legacy (deterministic)\n");
        notes.append("Base award (before decay): ").append(baseAward.summaryLine()).append("\n");
        notes.append(factorLine);

        String body = eligibilityLine + "\n\n"
                + gains + "\n"
                + losses + "\n"
                + notes;

        return new PrestigePreview(title, body, eligible, maxed, award, nextStar, factor);
    }

    public record PrestigePreview(
            String title,
            String body,
            boolean eligible,
            boolean maxed,
            LegacyBonuses award,
            int nextStar,
            double factor
    ) {}

    public List<String> diminishingReturnLines() {
        return List.of(
                "Next ★1: x1.00",
                "Next ★2: x0.70",
                "Next ★3: x0.50",
                "Next ★4: x0.35",
                "Next ★5: x0.25"
        );
    }
}
