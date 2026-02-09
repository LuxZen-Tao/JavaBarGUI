import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameState {

    // time
    public int weekCount = 1;
    public int dayIndex = 0;            // 0..6
    public int dayCounter = 0;          // absolute day counter (increments each close)
    public int nightCount = 0;
    public boolean nightOpen = false;
    public int roundInNight = 0;
    public final int closingRound = 20;

    // economy
    public double cash = 100.0;
    public double debt = 0.0;
    public final double maxDebt = 10_000.0;

    public final double weeklyRent = 60.0;
    public double rentAccruedThisWeek = 0.0;
    public double securityUpkeepAccruedThisWeek = 0.0;
    public double opCostBaseThisWeek = 0.0;
    public double opCostStaffThisWeek = 0.0;
    public double opCostSkillThisWeek = 0.0;
    public double opCostOccupancyThisWeek = 0.0;

    //  debt interest (bank-style, not loan shark)
    public double weeklyDebtInterestRate = 0.045; // 4.5% per week (tune this)

    // rep/security
    public int reputation = 10;                 // -100..100
    public int consecutiveNeg100Rounds = 0;
    public int baseSecurityLevel = 0;           // persists across nights
    public int upgradeSecurityBonus = 0;
    public int peakReputation = 10;
    public int profitStreakWeeks = 0;

    // pricing
    public double priceMultiplier = 1.10;
    public boolean happyHour = false;

    // supplier
    public SupplierDeal supplierDeal = SupplierDeal.none();

    // reports
    public int reportIndex = 1;
    public int weeksIntoReport = 0;             // 0..3
    public int consecutiveDebtReports = 0;

    public double reportStartCash = 0;
    public double reportStartDebt = 0;
    public double reportRevenue = 0;
    public double reportCosts = 0;
    public final EnumMap<CostTag, Double> reportCostBreakdown = new EnumMap<>(CostTag.class);
    public int reportSales = 0;
    public int reportEvents = 0;

    // nightly stats
    public double nightStartCash = 0;
    public double nightStartDebt = 0;
    public double nightRoundCostsTotal = 0;
    public int nightSales = 0;
    public double nightRevenue = 0;
    public int nightUnserved = 0;
    public int nightKickedOut = 0;
    public int nightRefusedUnderage = 0;
    public int nightEvents = 0;
    public int nightFights = 0;
    public int nightRefunds = 0;
    public int nightFoodUnserved = 0;
    public double nightRefundTotal = 0.0;
    public double weekRefundTotal = 0.0;
    public double reportRefundTotal = 0.0;
    public double weekRevenue = 0.0;
    public double weekCosts = 0.0;
    public int unservedThisWeek = 0;
    public double weekPriceMultiplierSum = 0.0;
    public int weekPriceMultiplierSamples = 0;
    public double weekPriceMultiplierAbsDelta = 0.0;
    public double lastPriceMultiplierSample = 0.0;
    public double weekFoodQualityPoints = 0.0;
    public int weekFoodOrders = 0;
    public double weeklyRepDeltaAbs = 0.0;
    public double weeklyRepDeltaNet = 0.0;
    public int weekPositiveEvents = 0;
    public int weekNegativeEvents = 0;
    public double weekChaosTotal = 0.0;
    public int weekChaosRounds = 0;
    public int staffMisconductThisWeek = 0;
    public boolean staffIncidentThisNight = false;
    public boolean staffIncidentThisRound = false;
    public String lastStaffIncidentSummary = "None";
    public String lastStaffIncidentDrivers = "None";
    public String lastRumorHeadline = "None";
    public String lastRumorDrivers = "None";
    public int posStreak = 0;
    public int negStreak = 0;
    public String lastRoundClassification = "None";
    public double lastChaosDelta = 0.0;
    public double lastChaosDeltaBase = 0.0;
    public double lastChaosDeltaStreak = 0.0;
    public double lastChaosMoraleNegMult = 1.0;
    public double lastChaosMoralePosMult = 1.0;
    public final Deque<String> milestoneRewardLog = new ArrayDeque<>();
    public int weekActivityNights = 0;
    public EnumMap<PubIdentity, Double> weekIdentitySignals = new EnumMap<>(PubIdentity.class);
    public int roundsSinceLastEvent = 0;
    public int foodSpoiledLastNight = 0;
    public final Map<String, Integer> nightItemSales = new HashMap<>();
    public final Map<String, Integer> roundItemSales = new HashMap<>();
    public final Deque<Map<String, Integer>> recentRoundSales = new ArrayDeque<>();
    public int lastTrafficIn = 0;
    public int lastTrafficOut = 0;
    public String trafficForecastLine = "Forecast: 0–0 tonight";
    public String observationLine = null;
    public String topSalesForecastLine = "Top sellers (5r): Wine None | Food None";

    // weekly chaos
    public int fightsThisWeek = 0;

    // staff pools
    public final List<Staff> fohStaff = new ArrayList<>();
    public final List<Staff> bohStaff = new ArrayList<>();
    public final List<Staff> generalManagers = new ArrayList<>();

    public int baseStaffCap = 4;
    public int fohStaffCap = 4;

    public int baseManagerCap = 1;
    public int managerCap = 1;

    public int baseKitchenChefCap = 2;
    public int kitchenChefCap = 2;

    public int pubLevel = 0;
    public int pubLevelServeCapBonus = 0;
    public int pubLevelBarCapBonus = 0;
    public double pubLevelTrafficBonusPct = 0.0;
    public double pubLevelRepMultiplier = 1.0;
    public int pubLevelStaffCapBonus = 0;
    public int pubLevelManagerCapBonus = 0;
    public int pubLevelChefCapBonus = 0;
    public int pubLevelBouncerCapBonus = 0;

    public double fohMorale = 70.0;
    public double bohMorale = 70.0;
    public double teamMorale = 70.0;

    public int nextStaffId = 1;

    public int kitchenQualityBonus = 0;
    public double refundRiskReductionPct = 0.0;
    public double staffMisconductReductionPct = 0.0;
    public double tipsThisWeek = 0.0;

    // bouncer (nightly)
    public int baseBouncerCap = 1;
    public int bouncerCap = 1;
    public int bouncersHiredTonight = 0;
    public double bouncerTheftReduction = 0.0;
    public double bouncerNegReduction = 0.0;
    public double bouncerFightReduction = 0.0;
    public double bouncerNightPay = 0.0;
    public final List<BouncerQuality> bouncerQualitiesTonight = new ArrayList<>();

    // upgrades + activities
    public final EnumSet<PubUpgrade> ownedUpgrades = EnumSet.noneOf(PubUpgrade.class);
    public PubActivity activityTonight = null;
    public double wagesAccruedThisWeek = 0.0;
    public double totalCashEarned = 0.0;

    // milestone tracking + popups
    public final EnumSet<MilestoneSystem.Milestone> achievedMilestones = EnumSet.noneOf(MilestoneSystem.Milestone.class);
    public final Deque<String> milestonePopups = new ArrayDeque<>();

    // reputation event tracking
    public int consecutiveHighRepRounds = 0;

    // report popups
    public boolean weeklyReportReady = false;
    public boolean fourWeekReportReady = false;
    public String weeklyReportText = "";
    public String fourWeekReportText = "";
    public String weeklyIdentityFlavorText = "";
    public String identityDriftSummary = "";
    public String identityDrift = "";

    // temp perks
    public int nextNightServeCapBonus = 0;
    public int tempServeBonusTonight = 0;

    // night flags
    public boolean happyHourBacklashShown = false;
    public boolean happyHourCheatRepHitThisRound = false;
    public boolean overpricingRobberyPopupShown = false;
    public int foodDisappointmentThisRound = 0;
    public boolean foodDisappointmentPopupShown = false;

    // punters
    public final List<Punter> nightPunters = new ArrayList<>();
    public int nextPunterId = 1;

    // how many can be in the bar at once (arrivals stop once full)
    public int maxBarOccupancy = 5;

    // inventory spoilage (to prevent hoarding deals forever)
    public int spoilDays = 3;

    //  base rack capacity (upgrades add on top)
    public int baseRackCapacity = 50;
    public int baseFoodRackCapacity = 30;
    public int baseFoodPrepRounds = 3;
    public int baseFoodSpoilDays = 3;
    public int foodPrepRounds = 3;
    public int kitchenPrepSpeedBonus = 0;
    public int kitchenSpoilBonusDays = 0;
    public double bohMoraleResiliencePct = 0.0;
    public int foodNightRepBonus = 0;

    //  cached upgrade effects (set by Simulation.applyPersistentUpgrades)
    public int upgradeBarCapBonus = 0;
    public int upgradeServeCapBonus = 0;
    public double upgradeTipBonusPct = 0.0;
    public double upgradeEventDamageReductionPct = 0.0;
    public double upgradeRiskReductionPct = 0.0;
    public int upgradeFoodRackCapBonus = 0;

    // debug: last between-night event summary
    public String lastBetweenNightEventSummary = "None";

    // inventory
    public WineRack rack = new WineRack();
    public FoodRack foodRack = new FoodRack();
    public boolean kitchenUnlocked = false;

    public final List<FoodOrder> pendingFoodOrders = new ArrayList<>();
    public final List<PendingSupplierDelivery> pendingSupplierDeliveries = new ArrayList<>();
    public final List<PendingFoodDelivery> pendingFoodDeliveries = new ArrayList<>();

    public final List<Wine> supplier;
    public List<Food> foodSupplier = new ArrayList<>();

    // loan shark
    public final LoanSharkAccount loanShark = new LoanSharkAccount();

    public static final String[] DAYS = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
    public final Random random = new Random();

    public final EnumSet<PubActivity> unlockedActivities = EnumSet.noneOf(PubActivity.class);
    public String pubName;
    public final String pubId;

    public final List<PendingUpgradeInstall> pendingUpgradeInstalls = new ArrayList<>();
    public ScheduledActivity scheduledActivity = null;
    public double chaos = 0.0;
    public double betweenNightChaos = 0.0;

    public PubIdentity pubIdentity = PubIdentity.NEUTRAL;
    public double identityRespectable = 0.0;
    public double identityRowdy = 0.0;
    public double identityArtsy = 0.0;
    public double identityShady = 0.0;
    public double identityFamily = 0.0;
    public double identityUnderground = 0.0;

    public final EnumMap<Rumor, RumorInstance> activeRumors = new EnumMap<>(Rumor.class);
    public final EnumMap<Rumor, Integer> rumorHeat = new EnumMap<>(Rumor.class);
    public EnumMap<PubIdentity, Double> pubIdentityScore = new EnumMap<>(PubIdentity.class);
    public PubIdentity currentIdentity = PubIdentity.RESPECTABLE;
    public java.util.Deque<PubIdentitySystem.WeeklyIdentitySnapshot> identitySnapshots = new ArrayDeque<>();
    public java.util.List<PubIdentity> identityHistory = new ArrayList<>();
    public double lastIdentityScore = 0.0;

    public GameState(List<Wine> supplier) {
        this.supplier = supplier;
        for (CostTag t : CostTag.values()) reportCostBreakdown.put(t, 0.0);
        this.pubId = UUID.randomUUID().toString().substring(0, 8);

        // default rack setup
        rack.setCapacity(baseRackCapacity);
        rack.setSpoilAfterDays(spoilDays);

        foodRack.setCapacity(baseFoodRackCapacity);
        foodRack.setSpoilAfterDays(baseFoodSpoilDays);
        foodPrepRounds = baseFoodPrepRounds;

        // unlock default activities
        for (PubActivity activity : PubActivity.values()) {
            if (!activity.requiresUnlock()) unlockedActivities.add(activity);
        }
        for (Rumor rumor : Rumor.values()) {
            rumorHeat.put(rumor, 0);
        }

        for (PubIdentity identity : PubIdentity.values()) {
            pubIdentityScore.put(identity, 0.0);
            weekIdentitySignals.put(identity, 0.0);
        }
        identityHistory.add(currentIdentity);
    }

    public int absWeekIndex() { return weekCount - 1; }
    public int clampRep(int r) { return Math.max(-100, Math.min(100, r)); }
    public int absDayIndex() { return dayCounter; }

    public void addReportCost(CostTag tag, double amount) {
        if (amount <= 0) return;
        if (tag == null) tag = CostTag.OTHER;
        reportCostBreakdown.merge(tag, amount, Double::sum);
    }

    public void recordRefund(double amount) {
        if (amount <= 0) return;
        nightRefunds++;
        nightRefundTotal += amount;
        weekRefundTotal += amount;
        reportRefundTotal += amount;
    }

    public double reportCost(CostTag tag) {
        return reportCostBreakdown.getOrDefault(tag, 0.0);
    }

    public double invoiceDueNow(double wagesDue) {
        double tipsDue = tipsThisWeek * 0.50;
        return wagesDue + rentAccruedThisWeek + securityUpkeepAccruedThisWeek + tipsDue;
    }

    public void recordWeeklyPriceMultiplier(double multiplier) {
        weekPriceMultiplierSum += multiplier;
        weekPriceMultiplierSamples++;
    }

    public void recordFoodQuality(Food food) {
        if (food == null) return;
        weekFoodQualityPoints += food.getQualityTier();
        weekFoodOrders++;
    }

    public void recordActivitySignal(PubIdentity identity, double weight) {
        if (identity == null) return;
        weekIdentitySignals.merge(identity, weight, Double::sum);
    }

    public void resetReportBreakdown() {
        reportCostBreakdown.clear();
        for (CostTag t : CostTag.values()) reportCostBreakdown.put(t, 0.0);
    }

    public String dayName() {
        return DAYS[Math.max(0, Math.min(6, dayIndex))];
    }

    public record StaffSummary(
            int staffCount,
            int staffCap,
            int managerCount,
            int assistantManagerCount,
            int managerPoolCount,
            int managerCap,
            double teamMorale,
            int upgradesOwned,
            PubActivity activityTonight,
            int bouncersTonight,
            int bouncerCap
    ) {
        public String summaryLine() {
            return staffCount + "/" + staffCap
                    + " | Managers: " + managerPoolCount + "/" + managerCap
                    + " (GM " + managerCount + ", AM " + assistantManagerCount + ")"
                    + (bouncersTonight > 0 ? " | Bouncer: " + bouncersTonight + "/" + bouncerCap : "")
                    + " | Morale: " + (int)Math.round(teamMorale)
                    + " | Upgrades: " + upgradesOwned
                    + (activityTonight != null ? " | Activity: " + activityTonight : "");
        }
    }

    public StaffSummary staff() {
        return new StaffSummary(
                fohStaff.size(),
                fohStaffCap,
                generalManagers.size(),
                assistantManagerCount(),
                managerPoolCount(),
                managerCap,
                teamMorale,
                ownedUpgrades.size(),
                activityTonight,
                bouncersHiredTonight,
                bouncerCap
        );
    }

    public int staffCountOfType(Staff.Type type) {
        int count = 0;
        for (Staff st : fohStaff) if (st.getType() == type) count++;
        for (Staff st : bohStaff) if (st.getType() == type) count++;
        for (Staff st : generalManagers) if (st.getType() == type) count++;
        return count;
    }

    /**  Only defined once. Count BOH kitchen roles. */
    public int kitchenStaffCount() {
        int count = 0;
        for (Staff st : bohStaff) if (st.isKitchenRole()) count++;
        return count;
    }

    public boolean hasSkilledManager() {
        for (Staff st : generalManagers) {
            if (st.getSkill() >= 8) return true;
        }
        return false;
    }

    public boolean hasGeneralManager() {
        return !generalManagers.isEmpty();
    }

    public boolean hasAssistantManager() {
        for (Staff st : fohStaff) {
            if (st.getType() == Staff.Type.ASSISTANT_MANAGER) return true;
        }
        return false;
    }

    public int assistantManagerCount() {
        int count = 0;
        for (Staff st : fohStaff) {
            if (st.getType() == Staff.Type.ASSISTANT_MANAGER) count++;
        }
        return count;
    }

    public int managerPoolCount() {
        return generalManagers.size() + assistantManagerCount();
    }

    public String bouncerQualitySummary() {
        if (bouncerQualitiesTonight.isEmpty()) return "None";
        StringBuilder summary = new StringBuilder();
        if (bouncerQualitiesTonight.contains(BouncerQuality.HIGH)) summary.append("High");
        if (bouncerQualitiesTonight.contains(BouncerQuality.MEDIUM)) {
            if (!summary.isEmpty()) summary.append("/");
            summary.append("Med");
        }
        if (bouncerQualitiesTonight.contains(BouncerQuality.LOW)) {
            if (!summary.isEmpty()) summary.append("/");
            summary.append("Low");
        }
        return summary.toString();
    }

    public double bouncerMitigationChance() {
        if (bouncerQualitiesTonight.isEmpty()) return 0.0;
        if (bouncerQualitiesTonight.contains(BouncerQuality.HIGH)) return 0.85;
        if (bouncerQualitiesTonight.contains(BouncerQuality.MEDIUM)) return 0.60;
        return 0.35;
    }

    public boolean isWeekend() {
        return dayIndex >= 4;
    }

    public boolean canEmergencyRestock() {
        return hasGeneralManager() && hasAssistantManager();
    }

    public void recordRoundSale(String category, String itemName) {
        if (category == null || itemName == null) return;
        roundItemSales.merge(category + ": " + itemName, 1, Integer::sum);
    }

    public void addChaos(double amt) {
        chaos = Math.max(0.0, Math.min(100.0, chaos + amt));
    }

    public int staffSecurityBonus() {
        int bonus = 0;
        for (Staff st : fohStaff) bonus += st.getSecurityBonus();
        return bonus;
    }

    public double staffChaosCapacity() {
        double total = 0.0;
        double weight = 0.0;

        for (Staff st : fohStaff) {
            double w = 1.0 + (st.getSkill() / 6.0);
            total += st.getChaosTolerance() * w;
            weight += w;
        }
        for (Staff st : generalManagers) {
            double w = 1.0 + (st.getSkill() / 6.0);
            total += st.getChaosTolerance() * w;
            weight += w;
        }

        if (weight <= 0.0) return 0.0;
        return total / weight;
    }

    public record ReportSummary(
            int reportIndex,
            int weeksIntoReport,
            double revenue,
            double costs,
            double profit,
            int sales,
            int events
    ) {
        public String summaryLine() {
            return "#" + reportIndex
                    + " (week " + (weeksIntoReport + 1) + "/4)"
                    + " | profit " + String.format("%.0f", profit)
                    + " | sales " + sales
                    + " | events " + events;
        }
    }

    public ReportSummary reports() {
        double profit = reportRevenue - reportCosts;
        return new ReportSummary(reportIndex, weeksIntoReport, reportRevenue, reportCosts, profit, reportSales, reportEvents);
    }
}
