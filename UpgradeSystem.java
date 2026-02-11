public class UpgradeSystem {

    /*
     * Upgrade pipeline map (definition -> purchase -> install -> apply -> UI):
     * - Definitions live in PubUpgrade (cost/tier/effect payload/category hints).
     * - Purchase gating lives in MilestoneSystem#getUpgradeAvailability(...).
     * - Purchases create PendingUpgradeInstall entries in Simulation#buyUpgrade.
     * - Completed installs are resolved in Simulation#processPendingUpgradeInstallsAtNightEnd.
     * - Effects are centrally aggregated here into UpgradeModifierSnapshot and applied in Simulation#applyPersistentUpgrades.
     * - UI display uses Simulation/WineBarGUI with effect + lock reason text derived from this system and milestone gating.
     */

    public enum UpgradeCategory {
        THROUGHPUT,
        QUALITY,
        SECURITY,
        FINANCE,
        SUPPLIERS,
        IDENTITY,
        INN,
        UI_QOL
    }

    public record UpgradeModifierSnapshot(
            double trafficMultiplier,
            int repDriftPerRound,
            int eventBonusChance,
            int barCapBonus,
            int serveCapBonus,
            int rackCapBonus,
            int foodRackCapBonus,
            int securityBonus,
            int staffCapBonus,
            int bouncerCapBonus,
            int managerCapBonus,
            int chefCapBonus,
            int kitchenQualityBonus,
            double refundRiskReductionPct,
            double staffMisconductReductionPct,
            double wageEfficiencyPct,
            double tipBonusPct,
            double eventDamageReductionPct,
            double riskReductionPct,
            double incidentChanceMultiplier,
            double moraleStabilityPct,
            double repMitigationPct,
            double lossSeverityMultiplier
    ) {}

    private final GameState s;

    public UpgradeSystem(GameState s) { this.s = s; }

    public UpgradeModifierSnapshot buildModifierSnapshot() {
        double trafficPct = 0.0;
        int repDrift = 0;
        int eventChance = 0;
        int barCap = 0;
        int serveCap = 0;
        int rackCap = 0;
        int foodRackCap = 0;
        int security = 0;
        int staffCap = 0;
        int bouncerCap = 0;
        int managerCap = 0;
        int chefCap = 0;
        int kitchenQuality = 0;
        double refundReduction = 0.0;
        double misconductReduction = 0.0;
        double wageEff = 0.0;
        double tipBonus = 0.0;
        double eventDmgReduction = 0.0;
        double riskReduction = 0.0;

        for (PubUpgrade u : s.ownedUpgrades) {
            trafficPct += u.getTrafficBonusPct();
            repDrift += u.getRepDriftPerRound();
            eventChance += u.getEventBonusChance();
            barCap += u.getBarCapBonus();
            serveCap += u.getServeCapBonus();
            rackCap += u.getRackCapBonus();
            foodRackCap += u.getFoodRackCapBonus();
            security += u.getSecurityBonus();
            staffCap += u.getStaffCapBonus();
            bouncerCap += u.getBouncerCapBonus();
            managerCap += u.getManagerCapBonus();
            chefCap += u.getChefCapBonus();
            kitchenQuality += u.getKitchenQualityBonus();
            refundReduction += u.getRefundRiskReductionPct();
            misconductReduction += u.getStaffMisconductReductionPct();
            wageEff += u.getWageEfficiencyPct();
            tipBonus += u.getTipBonusPct();
            eventDmgReduction += u.getEventDamageReductionPct();
            riskReduction += u.getRiskReductionPct();
        }

        refundReduction = clamp(refundReduction, 0.0, 0.40);
        misconductReduction = clamp(misconductReduction, 0.0, 0.40);
        wageEff = clamp(wageEff, 0.0, 0.25);
        tipBonus = clamp(tipBonus, 0.0, 0.35);
        eventDmgReduction = clamp(eventDmgReduction, 0.0, 0.35);
        riskReduction = clamp(riskReduction, 0.0, 0.35);

        int doorTier = tier(PubUpgrade.REINFORCED_DOOR_I, PubUpgrade.REINFORCED_DOOR_II, PubUpgrade.REINFORCED_DOOR_III);
        int lightTier = tier(PubUpgrade.LIGHTING_I, PubUpgrade.LIGHTING_II, PubUpgrade.LIGHTING_III);
        int alarmTier = tier(PubUpgrade.BURGLAR_ALARM_I, PubUpgrade.BURGLAR_ALARM_II, PubUpgrade.BURGLAR_ALARM_III);

        double incidentMult = 1.0;
        if (doorTier == 1) incidentMult *= 0.98;
        if (doorTier == 2) incidentMult *= 0.95;
        if (doorTier == 3) incidentMult *= 0.92;
        if (lightTier == 1) incidentMult *= 0.99;
        if (lightTier == 2) incidentMult *= 0.97;
        if (lightTier == 3) incidentMult *= 0.95;
        if (alarmTier == 1) incidentMult *= 0.98;
        if (alarmTier == 2) incidentMult *= 0.95;
        if (alarmTier == 3) incidentMult *= 0.90;
        incidentMult = Math.max(0.70, incidentMult);

        double moraleStability = 0.0;
        if (lightTier == 2) moraleStability = 0.05;
        if (lightTier == 3) moraleStability = 0.10;

        double repMitigation = 0.0;
        if (doorTier == 2) repMitigation += 0.03;
        if (doorTier == 3) repMitigation += 0.06;
        if (lightTier == 2) repMitigation += 0.02;
        if (lightTier == 3) repMitigation += 0.04;
        repMitigation = Math.min(0.20, repMitigation);

        double lossSeverityMult = Math.max(0.55, 1.0 - eventDmgReduction);

        return new UpgradeModifierSnapshot(
                1.0 + trafficPct,
                repDrift,
                eventChance,
                barCap,
                serveCap,
                rackCap,
                foodRackCap,
                security,
                staffCap,
                bouncerCap,
                managerCap,
                chefCap,
                kitchenQuality,
                refundReduction,
                misconductReduction,
                wageEff,
                tipBonus,
                eventDmgReduction,
                riskReduction,
                incidentMult,
                moraleStability,
                repMitigation,
                lossSeverityMult
        );
    }

    public UpgradeCategory category(PubUpgrade u) {
        if (u == null) return UpgradeCategory.UI_QOL;
        return switch (u) {
            case CCTV, CCTV_PACKAGE,
                    REINFORCED_DOOR_I, REINFORCED_DOOR_II, REINFORCED_DOOR_III,
                    LIGHTING_I, LIGHTING_II, LIGHTING_III,
                    BURGLAR_ALARM_I, BURGLAR_ALARM_II, BURGLAR_ALARM_III,
                    DOOR_TEAM_I, DOOR_TEAM_II, DOOR_TEAM_III,
                    FIRE_SUPPRESSION_I, FIRE_SUPPRESSION_II, FIRE_SUPPRESSION_III,
                    MARSHALLS_I, MARSHALLS_II, MARSHALLS_III -> UpgradeCategory.SECURITY;
            case KITCHEN_SETUP, KITCHEN, NEW_KITCHEN_PLAN, KITCHEN_EQUIPMENT,
                    HYGIENE_TRAINING, CHEF_TRAINING,
                    KITCHEN_STAFFING_I, KITCHEN_STAFFING_II, KITCHEN_STAFFING_III,
                    FRIDGE_EXTENSION_1, FRIDGE_EXTENSION_2, FRIDGE_EXTENSION_3 -> UpgradeCategory.QUALITY;
            case INN_WING_1, INN_WING_2, INN_WING_3, INN_WING_4, INN_WING_5 -> UpgradeCategory.INN;
            case WINE_CELLAR, CELLAR_EXPANSION_I, CELLAR_EXPANSION_II, CELLAR_EXPANSION_III -> UpgradeCategory.SUPPLIERS;
            case STAFF_TRAINING_I, STAFF_TRAINING_II, STAFF_TRAINING_III,
                    FASTER_TAPS_I, FASTER_TAPS_II, FASTER_TAPS_III,
                    EXTENDED_BAR -> UpgradeCategory.THROUGHPUT;
            case STAFF_ROOM_I, STAFF_ROOM_II, STAFF_ROOM_III,
                    LEADERSHIP_PROGRAM,
                    BETTER_GLASSWARE_I, BETTER_GLASSWARE_II, BETTER_GLASSWARE_III -> UpgradeCategory.FINANCE;
            default -> UpgradeCategory.IDENTITY;
        };
    }

    public String effectSummary(PubUpgrade up) {
        if (up == null) return "No effect.";
        StringBuilder sb = new StringBuilder();
        append(sb, up.getTrafficBonusPct() > 0, "+" + pct(up.getTrafficBonusPct()) + " traffic");
        append(sb, up.getRepDriftPerRound() != 0, (up.getRepDriftPerRound() > 0 ? "+" : "") + up.getRepDriftPerRound() + " rep/round");
        append(sb, up.getEventBonusChance() > 0, "+" + up.getEventBonusChance() + "% event chance");
        append(sb, up.getBarCapBonus() > 0, "+" + up.getBarCapBonus() + " bar cap");
        append(sb, up.getServeCapBonus() > 0, "+" + up.getServeCapBonus() + " serve cap");
        append(sb, up.getRackCapBonus() > 0, "+" + up.getRackCapBonus() + " rack cap");
        append(sb, up.getFoodRackCapBonus() > 0, "+" + up.getFoodRackCapBonus() + " kitchen stock");
        append(sb, up.getSecurityBonus() > 0, "+" + up.getSecurityBonus() + " security");
        append(sb, up.getStaffCapBonus() > 0, "+" + up.getStaffCapBonus() + " staff cap");
        append(sb, up.getBouncerCapBonus() > 0, "+" + up.getBouncerCapBonus() + " bouncer cap");
        append(sb, up.getManagerCapBonus() > 0, "+" + up.getManagerCapBonus() + " manager cap");
        append(sb, up.getChefCapBonus() > 0, "+" + up.getChefCapBonus() + " chef cap");
        append(sb, up.getKitchenQualityBonus() > 0, "+" + up.getKitchenQualityBonus() + " kitchen quality");
        append(sb, up.getRefundRiskReductionPct() > 0, "-" + pct(up.getRefundRiskReductionPct()) + " refund risk");
        append(sb, up.getStaffMisconductReductionPct() > 0, "-" + pct(up.getStaffMisconductReductionPct()) + " misconduct risk");
        append(sb, up.getWageEfficiencyPct() > 0, "-" + pct(up.getWageEfficiencyPct()) + " wages");
        append(sb, up.getTipBonusPct() > 0, "+" + pct(up.getTipBonusPct()) + " tips");
        append(sb, up.getEventDamageReductionPct() > 0, "-" + pct(up.getEventDamageReductionPct()) + " event loss severity");
        append(sb, up.getRiskReductionPct() > 0, "-" + pct(up.getRiskReductionPct()) + " chaos risk");
        if (sb.length() == 0) {
            return "No direct modifier (unlock/prereq upgrade).";
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, boolean ok, String text) {
        if (!ok) return;
        if (sb.length() > 0) sb.append(" | ");
        sb.append(text);
    }

    private static String pct(double v) {
        return String.valueOf((int)Math.round(v * 100));
    }

    private int tier(PubUpgrade t1, PubUpgrade t2, PubUpgrade t3) {
        if (s.ownedUpgrades.contains(t3)) return 3;
        if (s.ownedUpgrades.contains(t2)) return 2;
        if (s.ownedUpgrades.contains(t1)) return 1;
        return 0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public double trafficMultiplier() { return buildModifierSnapshot().trafficMultiplier(); }
    public int repDriftPerRound() { return buildModifierSnapshot().repDriftPerRound(); }
    public int eventBonusChance() { return buildModifierSnapshot().eventBonusChance(); }
    public int barCapBonus() { return buildModifierSnapshot().barCapBonus(); }
    public int serveCapBonus() { return buildModifierSnapshot().serveCapBonus(); }
    public int rackCapBonus() { return buildModifierSnapshot().rackCapBonus(); }
    public int foodRackCapBonus() { return buildModifierSnapshot().foodRackCapBonus(); }
    public int securityBonus() { return buildModifierSnapshot().securityBonus(); }
    public int staffCapBonus() { return buildModifierSnapshot().staffCapBonus(); }
    public int bouncerCapBonus() { return buildModifierSnapshot().bouncerCapBonus(); }
    public int managerCapBonus() { return buildModifierSnapshot().managerCapBonus(); }
    public int chefCapBonus() { return buildModifierSnapshot().chefCapBonus(); }
    public int kitchenQualityBonus() { return buildModifierSnapshot().kitchenQualityBonus(); }
    public double refundRiskReductionPct() { return buildModifierSnapshot().refundRiskReductionPct(); }
    public double staffMisconductReductionPct() { return buildModifierSnapshot().staffMisconductReductionPct(); }
    public double tipBonusPct() { return buildModifierSnapshot().tipBonusPct(); }
    public double eventDamageReductionPct() { return buildModifierSnapshot().eventDamageReductionPct(); }
    public double riskReductionPct() { return buildModifierSnapshot().riskReductionPct(); }
    public double wageEfficiencyPct() { return buildModifierSnapshot().wageEfficiencyPct(); }
}
