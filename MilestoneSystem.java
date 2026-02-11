import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MilestoneSystem {

    public enum Milestone {
        M1_OPEN_FOR_BUSINESS,
        M2_NO_EMPTY_SHELVES,
        M3_NO_ONE_LEAVES_ANGRY,
        M4_PAYROLL_GUARDIAN,
        M5_CALM_HOUSE,
        M6_MARGIN_WITH_MANNERS,
        M7_CREW_THAT_STAYS,
        M8_ORDER_RESTORED,
        M9_KNOWN_FOR_SOMETHING,
        M10_MIXED_CROWD_WHISPERER,
        M11_NARRATIVE_RECOVERY,
        M12_BOOKED_OUT,
        M13_BRIDGE_DONT_BLEED,
        M14_DEBT_DIET,
        M15_BALANCED_BOOKS_BUSY_HOUSE,
        M16_SUPPLIERS_FAVOURITE,
        M17_GOLDEN_QUARTER,
        M18_STORMPROOF_OPERATOR,
        M19_HEADLINER_VENUE
    }

    public enum EvaluationReason {
        NIGHT_END,
        WEEK_END,
        REPUTATION_CHANGE,
        PAYDAY,
        ACTIVITY,
        SUPPLIER
    }

    private static final double CHAOS_HIGH_THRESHOLD = 60.0;
    private static final double CHAOS_SAFE_THRESHOLD = 25.0;
    private static final double CREW_MORALE_THRESHOLD = 65.0;
    private static final double BOOKED_OUT_SALES_FACTOR = 0.65;
    private static final double GOLDEN_QUARTER_REP_TARGET = 45.0;
    private static final int STORMPROOF_NEGATIVE_EVENTS = 3;

    private final GameState s;
    private final UILogger log;
    private final EnumMap<PubActivity, Milestone> activityMilestoneRequirements = new EnumMap<>(PubActivity.class);
    private final EnumMap<PubActivity, ActivityAvailability> activityAvailability = new EnumMap<>(PubActivity.class);
    private final EnumMap<PubUpgrade, UpgradeAvailability> upgradeAvailability = new EnumMap<>(PubUpgrade.class);
    private final List<MilestoneDefinition> definitions = new ArrayList<>();

    private record MilestoneDefinition(Milestone id, int tier, String title, String description, String rewardText) {}

    public static final class ActivityAvailability {
        private final boolean unlocked;
        private final List<String> missingRequirements;

        public ActivityAvailability(boolean unlocked, List<String> missingRequirements) {
            this.unlocked = unlocked;
            this.missingRequirements = missingRequirements == null ? List.of() : List.copyOf(missingRequirements);
        }

        public boolean unlocked() { return unlocked; }
        public List<String> missingRequirements() { return missingRequirements; }
    }


    public static final class UpgradeAvailability {
        private final boolean unlocked;
        private final List<String> missingRequirements;

        public UpgradeAvailability(boolean unlocked, List<String> missingRequirements) {
            this.unlocked = unlocked;
            this.missingRequirements = missingRequirements == null ? List.of() : List.copyOf(missingRequirements);
        }

        public boolean unlocked() { return unlocked; }
        public List<String> missingRequirements() { return missingRequirements; }
    }

    public MilestoneSystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
        buildDefinitions();
        wireActivityRequirements();
        if (!s.prestigeMilestones.isEmpty() && s.achievedMilestones.isEmpty()) {
            s.achievedMilestones.addAll(s.prestigeMilestones);
        }
        recomputeActivityAvailability();
        recomputeUpgradeAvailability();
    }

    private void buildDefinitions() {
        definitions.add(new MilestoneDefinition(Milestone.M1_OPEN_FOR_BUSINESS, 1, "Open For Business", "Survive 3 services without bankruptcy.", "Unlocks: Karaoke"));
        definitions.add(new MilestoneDefinition(Milestone.M2_NO_EMPTY_SHELVES, 1, "No Empty Shelves", "Two consecutive nights with zero stockouts.", "Unlocks: Cocktail Promo"));
        definitions.add(new MilestoneDefinition(Milestone.M3_NO_ONE_LEAVES_ANGRY, 1, "No One Leaves Angry", "One perfect service night (0 refunds, 0 unserved).", "Unlocks: Staff Room II upgrades"));
        definitions.add(new MilestoneDefinition(Milestone.M4_PAYROLL_GUARDIAN, 1, "Payroll Guardian", "Pay wages and rent on payday.", "Unlocks: Quiz Night"));
        definitions.add(new MilestoneDefinition(Milestone.M5_CALM_HOUSE, 2, "Calm House", "3 calm nights in a row while running activity.", "Unlocks: Open Mic"));
        definitions.add(new MilestoneDefinition(Milestone.M6_MARGIN_WITH_MANNERS, 2, "Margin With Manners", "Weekly avg price >=1.15 and positive rep delta.", "Unlocks: CCTV"));
        definitions.add(new MilestoneDefinition(Milestone.M7_CREW_THAT_STAYS, 2, "Crew That Stays", "2 weeks no staff departures and morale stable.", "Unlocks: Staff Room III"));
        definitions.add(new MilestoneDefinition(Milestone.M8_ORDER_RESTORED, 2, "Order Restored", "Recover from high chaos to safe chaos within 2 nights.", "Unlocks: Landlord actions tier 2"));
        definitions.add(new MilestoneDefinition(Milestone.M9_KNOWN_FOR_SOMETHING, 3, "Known For Something", "Hold one dominant identity for 2 weeks.", "Unlocks: Charity Night"));
        definitions.add(new MilestoneDefinition(Milestone.M10_MIXED_CROWD_WHISPERER, 3, "Mixed Crowd Whisperer", "Run 3 different activity categories without collapse.", "Unlocks: Family Lunch"));
        definitions.add(new MilestoneDefinition(Milestone.M11_NARRATIVE_RECOVERY, 3, "Narrative Recovery", "Recover from a negative rumor week in 2 weeks.", "Unlocks: Brewery Takeover"));
        definitions.add(new MilestoneDefinition(Milestone.M12_BOOKED_OUT, 3, "Booked Out", "Three near-capacity quality nights in one week.", "Unlocks: Landlord actions tier 3"));
        definitions.add(new MilestoneDefinition(Milestone.M13_BRIDGE_DONT_BLEED, 4, "Bridge, Don't Bleed", "Use credit and clear it same week without misses.", "Unlocks: Supplier bulk tier x100"));
        definitions.add(new MilestoneDefinition(Milestone.M14_DEBT_DIET, 4, "Debt Diet", "3 consecutive zero-debt week endings.", "Unlocks: Supplier bulk tier x300"));
        definitions.add(new MilestoneDefinition(Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE, 4, "Balanced Books, Busy House", "Hit profit target while funding wages/security.", "Unlocks: Door Team II"));
        definitions.add(new MilestoneDefinition(Milestone.M16_SUPPLIERS_FAVOURITE, 4, "Supplier's Favourite", "Good supplier trust + one large bulk order.", "Unlocks: Premium supplier catalog"));
        definitions.add(new MilestoneDefinition(Milestone.M17_GOLDEN_QUARTER, 5, "Golden Quarter", "4 strong weeks in a row.", "Unlocks: Landlord actions tier 4"));
        definitions.add(new MilestoneDefinition(Milestone.M18_STORMPROOF_OPERATOR, 5, "Stormproof Operator", "Profitable week with positive rep under high adversity.", "Unlocks: Door Team III"));
        definitions.add(new MilestoneDefinition(Milestone.M19_HEADLINER_VENUE, 5, "Headliner Venue", "Premium pricing + high rep + top-tier programming quality.", "Unlocks: Landlord actions tier 5 + supplier bulk x500"));
    }

    private void wireActivityRequirements() {
        activityMilestoneRequirements.put(PubActivity.KARAOKE, Milestone.M1_OPEN_FOR_BUSINESS);
        activityMilestoneRequirements.put(PubActivity.COCKTAIL_PROMO, Milestone.M2_NO_EMPTY_SHELVES);
        activityMilestoneRequirements.put(PubActivity.QUIZ_NIGHT, Milestone.M4_PAYROLL_GUARDIAN);
        activityMilestoneRequirements.put(PubActivity.OPEN_MIC, Milestone.M5_CALM_HOUSE);
        activityMilestoneRequirements.put(PubActivity.CHARITY_NIGHT, Milestone.M9_KNOWN_FOR_SOMETHING);
        activityMilestoneRequirements.put(PubActivity.FAMILY_LUNCH, Milestone.M10_MIXED_CROWD_WHISPERER);
        activityMilestoneRequirements.put(PubActivity.BREWERY_TAKEOVER, Milestone.M11_NARRATIVE_RECOVERY);
    }

    public void onRepChanged() { evaluateMilestones(EvaluationReason.REPUTATION_CHANGE); }
    public void onNightEnd() { evaluateMilestones(EvaluationReason.NIGHT_END); }
    public void onWeekEnd() { evaluateMilestones(EvaluationReason.WEEK_END); }
    public void onPaydayResolved() { evaluateMilestones(EvaluationReason.PAYDAY); }
    public void onActivityScheduled(PubActivity activity) {
        if (activity != null && activity.getRequiredIdentity() != null) {
            s.weekActivityIdentityCategories.add(activity.getRequiredIdentity());
            s.weeklyDifferentActivityCategories = s.weekActivityIdentityCategories.size();
            if (activity.getCost() >= 140) s.topTierActivityRanThisWeek = true;
        }
        evaluateMilestones(EvaluationReason.ACTIVITY);
    }
    public void onSupplierOrder(int qty) {
        if (qty >= 25) s.largeBulkOrdersCompleted++;
        evaluateMilestones(EvaluationReason.SUPPLIER);
    }

    public void evaluateMilestones(EvaluationReason reason) {
        for (MilestoneDefinition def : definitions) {
            if (s.achievedMilestones.contains(def.id())) continue;
            if (isMet(def.id())) {
                grant(def, reason);
            }
        }
        recomputeActivityAvailability();
        recomputeUpgradeAvailability();
    }

    private boolean isMet(Milestone id) {
        return switch (id) {
            case M1_OPEN_FOR_BUSINESS -> s.nightCount >= 3 && !s.businessCollapsed && !s.bankruptcyDeclared;
            case M2_NO_EMPTY_SHELVES -> s.noStockoutStreakNights >= 2;
            case M3_NO_ONE_LEAVES_ANGRY -> s.nightRefunds == 0 && s.nightUnserved == 0 && s.nightFoodUnserved == 0;
            case M4_PAYROLL_GUARDIAN -> s.wagesPaidLastWeek && s.rentAccruedThisWeek <= 0.01;
            case M5_CALM_HOUSE -> s.calmNightsStreak >= 3 && s.calmNightsWithActivityStreak >= 1;
            case M6_MARGIN_WITH_MANNERS -> s.weekPriceMultiplierSamples > 0
                    && (s.weekPriceMultiplierSum / s.weekPriceMultiplierSamples) >= 1.15
                    && s.weeklyRepDeltaNet > 0;
            case M7_CREW_THAT_STAYS -> s.weeksNoStaffDepartures >= 2 && s.teamMorale >= CREW_MORALE_THRESHOLD;
            case M8_ORDER_RESTORED -> s.chaosRecoveryPending && s.chaos <= CHAOS_SAFE_THRESHOLD;
            case M9_KNOWN_FOR_SOMETHING -> s.weeksDominantIdentityStreak >= 2;
            case M10_MIXED_CROWD_WHISPERER -> s.weeklyDifferentActivityCategories >= 3 && s.weeklyRepDeltaNet >= 0
                    && avgWeeklyChaos() <= 45.0;
            case M11_NARRATIVE_RECOVERY -> s.negativeRumorRecoveryPending && s.weekNegativeEvents <= s.weekPositiveEvents;
            case M12_BOOKED_OUT -> s.nearCapacityServiceNightsThisWeek >= 3;
            case M13_BRIDGE_DONT_BLEED -> s.usedCreditThisWeek && s.totalCreditBalance() <= 0.01 && s.metMinimumsLastWeek
                    && s.creditScore >= s.creditScoreAtWeekStart;
            case M14_DEBT_DIET -> s.zeroDebtWeekStreak >= 3;
            case M15_BALANCED_BOOKS_BUSY_HOUSE -> (s.weekRevenue - s.weekCosts) >= 250.0
                    && s.wagesAccruedThisWeek >= 200.0
                    && s.securityUpkeepAccruedThisWeek >= 10.0;
            case M16_SUPPLIERS_FAVOURITE -> "Good".equals(s.supplierTrustLabel()) && s.largeBulkOrdersCompleted > 0;
            case M17_GOLDEN_QUARTER -> s.goldenQuarterWeekStreak >= 4;
            case M18_STORMPROOF_OPERATOR -> (s.weekRevenue - s.weekCosts) > 0
                    && s.weeklyRepDeltaNet > 0
                    && s.weekNegativeEvents >= STORMPROOF_NEGATIVE_EVENTS;
            case M19_HEADLINER_VENUE -> averageWeekPrice() >= 1.22
                    && s.reputation >= 75
                    && s.topTierActivityRanThisWeek
                    && refundRate() <= 0.02;
        };
    }

    private void grant(MilestoneDefinition def, EvaluationReason reason) {
        s.achievedMilestones.add(def.id());
        s.prestigeMilestones.add(def.id());
        applyReward(def.id());
        String msg = def.title() + "\n" + def.rewardText();
        s.milestonePopups.add(msg);
        recordMilestoneReward(def.title(), def.description(), def.rewardText());
        log.event("Milestone achieved [" + reason + "]: " + def.title() + " -> " + def.rewardText());
    }

    private void applyReward(Milestone id) {
        switch (id) {
            case M6_MARGIN_WITH_MANNERS -> grantCashBonus(100, "Margin With Manners");
            case M8_ORDER_RESTORED -> s.unlockedLandlordActionTier = Math.max(s.unlockedLandlordActionTier, 2);
            case M12_BOOKED_OUT -> s.unlockedLandlordActionTier = Math.max(s.unlockedLandlordActionTier, 3);
            case M13_BRIDGE_DONT_BLEED -> s.supplierBulkUnlockTier = Math.max(s.supplierBulkUnlockTier, 1);
            case M14_DEBT_DIET -> s.supplierBulkUnlockTier = Math.max(s.supplierBulkUnlockTier, 2);
            case M16_SUPPLIERS_FAVOURITE -> s.premiumSupplierCatalogUnlocked = true;
            case M17_GOLDEN_QUARTER -> s.unlockedLandlordActionTier = Math.max(s.unlockedLandlordActionTier, 4);
            case M19_HEADLINER_VENUE -> {
                s.unlockedLandlordActionTier = Math.max(s.unlockedLandlordActionTier, 5);
                s.supplierBulkUnlockTier = Math.max(s.supplierBulkUnlockTier, 3);
            }
            default -> {
                // unlock is handled via requirement mapping and upgrade gates
            }
        }
    }

    public void recomputeActivityAvailability() {
        activityAvailability.clear();
        s.unlockedActivities.clear();
        for (PubActivity activity : PubActivity.values()) {
            List<String> missing = new ArrayList<>();
            if (activity.getRequiredUpgrade() != null && !hasUpgradeOrPending(activity.getRequiredUpgrade())) {
                missing.add("Upgrade: " + activity.getRequiredUpgrade().getLabel());
            }
            Milestone reqMilestone = requiredMilestone(activity);
            if (reqMilestone != null && !s.achievedMilestones.contains(reqMilestone)) {
                missing.add("Milestone: " + reqMilestone.name());
            }
            if (!activity.requiresUnlock()) {
                // base activities remain available unless blocked by explicit milestone/upgrade requirement
            }
            boolean unlocked = missing.isEmpty();
            if (unlocked) s.unlockedActivities.add(activity);
            activityAvailability.put(activity, new ActivityAvailability(unlocked, missing));
        }
    }

    private Milestone requiredMilestone(PubActivity activity) {
        return activityMilestoneRequirements.get(activity);
    }

    private boolean hasUpgradeOrPending(PubUpgrade upgrade) {
        if (s.ownedUpgrades.contains(upgrade)) return true;
        for (PendingUpgradeInstall pending : s.pendingUpgradeInstalls) {
            if (pending.upgrade() == upgrade) return true;
        }
        return false;
    }

    public boolean canBuyUpgrade(PubUpgrade upgrade) {
        return getUpgradeAvailability(upgrade, s.cash).unlocked();
    }

    public UpgradeAvailability getUpgradeAvailability(PubUpgrade upgrade, double availableCash) {
        if (upgrade == null) return new UpgradeAvailability(false, List.of("Unknown upgrade"));
        List<String> missing = new ArrayList<>();
        if (s.ownedUpgrades.contains(upgrade)) {
            return new UpgradeAvailability(false, List.of("Already owned"));
        }
        if (upgrade.isKitchenRelated() && upgrade != PubUpgrade.KITCHEN_SETUP && !s.kitchenUnlocked) {
            missing.add("Kitchen not unlocked");
        }
        if (upgrade == PubUpgrade.KITCHEN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN_SETUP)) {
            missing.add("Requires Kitchen Base");
        }
        if (upgrade == PubUpgrade.NEW_KITCHEN_PLAN && !s.ownedUpgrades.contains(PubUpgrade.KITCHEN)) {
            missing.add("Requires Kitchen Upgrade I");
        }
        if (upgrade == PubUpgrade.KITCHEN_EQUIPMENT && !s.ownedUpgrades.contains(PubUpgrade.NEW_KITCHEN_PLAN)) {
            missing.add("Requires Kitchen Upgrade II");
        }
        if (!upgrade.isInnRelated() && upgrade.getTier() > 1 && s.pubLevel < upgrade.getTier() - 1) {
            missing.add("Requires pub level " + (upgrade.getTier() - 1));
        }
        if (upgrade.getChainKey() != null && upgrade.getTier() > 1) {
            boolean hasPrev = false;
            for (PubUpgrade owned : s.ownedUpgrades) {
                if (upgrade.getChainKey().equals(owned.getChainKey()) && owned.getTier() == upgrade.getTier() - 1) {
                    hasPrev = true;
                    break;
                }
            }
            if (!hasPrev) {
                missing.add("Requires tier " + (upgrade.getTier() - 1) + " in chain");
            }
        }
        if ((upgrade == PubUpgrade.CCTV || upgrade == PubUpgrade.CCTV_PACKAGE)
                && !s.achievedMilestones.contains(Milestone.M6_MARGIN_WITH_MANNERS)) {
            missing.add("Requires milestone: Margin With Manners");
        }
        if (upgrade == PubUpgrade.DOOR_TEAM_II && !s.achievedMilestones.contains(Milestone.M15_BALANCED_BOOKS_BUSY_HOUSE)) {
            missing.add("Requires milestone: Balanced Books, Busy House");
        }
        if (upgrade == PubUpgrade.DOOR_TEAM_III && !s.achievedMilestones.contains(Milestone.M18_STORMPROOF_OPERATOR)) {
            missing.add("Requires milestone: Stormproof Operator");
        }
        if ((upgrade == PubUpgrade.STAFF_ROOM_II || upgrade == PubUpgrade.STAFF_ROOM_III)
                && !(s.achievedMilestones.contains(Milestone.M7_CREW_THAT_STAYS)
                || s.achievedMilestones.contains(Milestone.M3_NO_ONE_LEAVES_ANGRY))) {
            missing.add("Requires milestone: Crew That Stays");
        }
        return new UpgradeAvailability(missing.isEmpty(), missing);
    }

    public void recomputeUpgradeAvailability() {
        upgradeAvailability.clear();
        for (PubUpgrade up : PubUpgrade.values()) {
            upgradeAvailability.put(up, getUpgradeAvailability(up, s.cash));
        }
    }

    public String upgradeRequirementText(PubUpgrade upgrade, double availableCash) {
        UpgradeAvailability availability = getUpgradeAvailability(upgrade, availableCash);
        if (availability.unlocked()) return null;
        if (availability.missingRequirements().isEmpty()) return null;
        return String.join(", ", availability.missingRequirements());
    }

    public String activityRequirementText(PubActivity activity) {
        ActivityAvailability availability = getActivityAvailability(activity);
        if (availability.unlocked()) return null;
        return "Missing: " + String.join(", ", availability.missingRequirements());
    }

    public boolean isActivityUnlocked(PubActivity activity) {
        return getActivityAvailability(activity).unlocked();
    }

    public ActivityAvailability getActivityAvailability(PubActivity activity) {
        ActivityAvailability availability = activityAvailability.get(activity);
        if (availability == null) {
            recomputeActivityAvailability();
            availability = activityAvailability.get(activity);
        }
        return availability;
    }

    public String milestoneProgressReport() {
        StringBuilder sb = new StringBuilder();
        for (int tier = 1; tier <= 5; tier++) {
            sb.append("Tier ").append(tier).append("\n");
            for (MilestoneDefinition def : definitions) {
                if (def.tier() != tier) continue;
                boolean done = s.achievedMilestones.contains(def.id());
                sb.append(done ? "[✓] " : "[ ] ")
                        .append(def.title())
                        .append(" - ")
                        .append(def.description())
                        .append(" | Reward: ")
                        .append(def.rewardText())
                        .append(" | Progress: ")
                        .append(progress(def.id()))
                        .append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String progress(Milestone id) {
        return switch (id) {
            case M1_OPEN_FOR_BUSINESS -> s.nightCount + "/3 nights";
            case M2_NO_EMPTY_SHELVES -> s.noStockoutStreakNights + "/2 nights";
            case M4_PAYROLL_GUARDIAN -> (s.wagesPaidLastWeek ? "Wages paid" : "Wages pending") + ", rent due " + String.format("%.0f", s.rentAccruedThisWeek);
            case M5_CALM_HOUSE -> s.calmNightsStreak + "/3 calm nights";
            case M7_CREW_THAT_STAYS -> s.weeksNoStaffDepartures + "/2 weeks";
            case M8_ORDER_RESTORED -> s.chaosRecoveryPending ? ("Chaos " + String.format("%.1f", s.chaos) + " safe<=25") : "Awaiting high-chaos trigger";
            case M9_KNOWN_FOR_SOMETHING -> s.weeksDominantIdentityStreak + "/2 weeks";
            case M10_MIXED_CROWD_WHISPERER -> s.weeklyDifferentActivityCategories + "/3 categories";
            case M11_NARRATIVE_RECOVERY -> s.negativeRumorRecoveryPending ? "Recovery in progress" : "Awaiting negative rumor week";
            case M12_BOOKED_OUT -> s.nearCapacityServiceNightsThisWeek + "/3 nights";
            case M13_BRIDGE_DONT_BLEED -> (s.usedCreditThisWeek ? "credit used" : "no credit used") + ", debt " + String.format("%.0f", s.totalCreditBalance());
            case M14_DEBT_DIET -> s.zeroDebtWeekStreak + "/3 weeks";
            case M16_SUPPLIERS_FAVOURITE -> ("Good".equals(s.supplierTrustLabel()) ? "Good trust" : "Trust " + s.supplierTrustLabel()) + ", bulk " + s.largeBulkOrdersCompleted;
            case M17_GOLDEN_QUARTER -> s.goldenQuarterWeekStreak + "/4 weeks";
            default -> "In play";
        };
    }

    private double averageWeekPrice() {
        if (s.weekPriceMultiplierSamples <= 0) return s.priceMultiplier;
        return s.weekPriceMultiplierSum / s.weekPriceMultiplierSamples;
    }

    private double avgWeeklyChaos() {
        if (s.weekChaosRounds <= 0) return s.chaos;
        return s.weekChaosTotal / s.weekChaosRounds;
    }

    private double refundRate() {
        if (s.weekRevenue <= 0.0) return 0.0;
        return s.weekRefundTotal / s.weekRevenue;
    }

    private void recordMilestoneReward(String title, String requirement, String rewardText) {
        String entry = title + " | Req: " + requirement + " | " + rewardText;
        s.milestoneRewardLog.addFirst(entry);
        while (s.milestoneRewardLog.size() > 8) s.milestoneRewardLog.removeLast();
    }

    private void grantCashBonus(double amount, String reason) {
        if (amount <= 0) return;
        s.cash += amount;
        s.totalCashEarned += amount;
        log.pos("Milestone reward: cash +" + String.format("%.0f", amount) + " (" + reason + ").");
    }
}
