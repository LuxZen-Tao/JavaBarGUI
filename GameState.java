import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class GameState implements java.io.Serializable {

    // time
    public int weekCount = 1;
    public int dayIndex = 0;            // 0..6
    public int dayCounter = 0;          // absolute day counter (increments each close)
    public int nightCount = 0;
    public boolean nightOpen = false;
    public int roundInNight = 0;
    public final int closingRound = 20;
    public static final LocalTime OPENING_TIME = LocalTime.of(11, 0);
    public static final LocalTime NORMAL_CLOSING_TIME = LocalTime.of(23, 0);
    public static final int MINUTES_PER_ROUND = 36;

    // economy
    public double cash = 100.0;
    public final CreditLineManager creditLines = new CreditLineManager();
    public int creditScore = 540;
    public double creditUtilization = 0.0;
    public int creditLinesOpenedThisWeek = 0;
    public int noDebtUsageWeeks = 0;
    public double supplierTrustPenalty = 0.0;
    public String supplierTrustStatus = "Neutral";
    public final SupplierTradeCredit supplierWineCredit = new SupplierTradeCredit();
    public final SupplierTradeCredit supplierFoodCredit = new SupplierTradeCredit();
    public transient CreditLineSelector creditLineSelector = null;
    public int sharkThreatTier = 0;
    public int sharkConsecutiveMisses = 0;
    public int sharkCleanWeeks = 0;
    public boolean sharkMissedPaymentThisWeek = false;
    public boolean sharkPaidOnTimeThisWeek = false;
    public boolean sharkHasBalanceThisWeek = false;
    public String sharkThreatTrigger = "None";
    public int consecutiveMissedWagePayments = 0;
    public boolean wagesPaidLastWeek = true;
    public int wagesPaidOnTimeWeeks = 0;
    public double wageTrafficPenaltyMultiplier = 1.0;
    public int wageTrafficPenaltyRounds = 0;
    public double wageServePenaltyPct = 0.0;
    public int wageServePenaltyWeeks = 0;
    public boolean banksLocked = false;
    public boolean businessCollapsed = false;
    public int consecutiveWeeksUnpaidMin = 0;
    public double weeklyTotalDueLastResolution = 0.0;
    public double weeklyTotalMinDueLastResolution = 0.0;
    public boolean metMinimumsLastWeek = true;
    public int debtSpiralTier = 0;
    public boolean bailiffStigma = false;
    public boolean bankruptcyDeclared = false;
    public boolean bankruptcySupplierStigma = false;
    public int bankruptcyLockWeeksRemaining = 0;
    public double supplierCreditCapOverride = 0.0;
    public double debtSpiralInterestMultiplier = 1.0;
    public double debtSpiralLateFeeMultiplier = 1.0;
    public double debtSpiralSupplierTrustMultiplier = 1.0;
    public double debtSpiralMoraleDecayMultiplier = 1.0;
    public double debtSpiralMisconductChanceMultiplier = 1.0;
    public double debtSpiralNegativeRepMultiplier = 1.0;
    public double debtSpiralPositiveRepMultiplier = 1.0;

    public final double weeklyRent = 420.0;
    public double rentAccruedThisWeek = 0.0;
    public double securityUpkeepAccruedThisWeek = 0.0;
    public double opCostBaseThisWeek = 0.0;
    public double opCostStaffThisWeek = 0.0;
    public double opCostSkillThisWeek = 0.0;
    public double opCostOccupancyThisWeek = 0.0;

    public double dailyRent() {
        return 60.0 + (roomsTotal * 20.0);
    }

    public double weeklyRentTotal() {
        return dailyRent() * 7.0;
    }

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
    public final LoanSharkAccount loanShark = new LoanSharkAccount();

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
    public double nightRoundCostsTotal = 0;
    public int nightSales = 0;
    public double nightRevenue = 0;
    public int nightUnserved = 0;
    public int nightKickedOut = 0;
    public int nightNaturalDepartures = 0;
    public int lastNaturalDepartures = 0;
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
    public String lastServiceDrivers = "Service: n/a";
    public String lastStabilityDrivers = "Stability: n/a";
    public double lastRoundWorkload = 1.0;
    public double lastRoundWorkloadPenalty = 0.0;
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
    public int noStockoutStreakNights = 0;
    public int calmNightsStreak = 0;
    public int calmNightsWithActivityStreak = 0;
    public int weeksNoStaffDepartures = 0;
    public int staffDeparturesThisWeek = 0;
    public int chaosRecoveryNightsRemaining = 0;
    public boolean chaosRecoveryPending = false;
    public int weeksDominantIdentityStreak = 0;
    public int weeklyDifferentActivityCategories = 0;
    public int nearCapacityServiceNightsThisWeek = 0;
    public boolean usedCreditThisWeek = false;
    public int creditScoreAtWeekStart = 540;
    public int zeroDebtWeekStreak = 0;
    public int goldenQuarterWeekStreak = 0;
    public int negativeRumorRecoveryWeeksRemaining = 0;
    public boolean negativeRumorRecoveryPending = false;
    public int largeBulkOrdersCompleted = 0;
    public boolean topTierActivityRanThisWeek = false;
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
    public int lastObservationRound = -999;
    public double lastObservationPriceMultiplier = 0.0;
    public int lastStaffChangeDay = -999;
    public String lastStaffChangeSummary = "";
    public String topSalesForecastLine = "Top sellers (5r): Wine None | Food None";
    public double lastNightChaosPeak = 0.0;

    // weekly chaos
    public int fightsThisWeek = 0;

    // staff pools
    public final List<Staff> fohStaff = new ArrayList<>();
    public final List<Staff> bohStaff = new ArrayList<>();
    public final List<Staff> generalManagers = new ArrayList<>();

    public int baseStaffCap = 4;
    public int fohStaffCap = 4;
    public int baseHohCap = 0;
    public int hohStaffCap = 0;

    public int baseManagerCap = 1;
    public int managerCap = 1;

    public int baseKitchenChefCap = 2;
    public int kitchenChefCap = 2;

    public int baseMarshallCap = 2;
    public int marshallCap = 2;
    public final List<BouncerQuality> marshalls = new ArrayList<>();

    // inn system
    public boolean innUnlocked = false;
    public int innTier = 0;
    public int roomsTotal = 0;
    public int roomsBookedLast = 0;
    public double roomPrice = 0.0;
    public double innRep = 0.0;
    public double cleanliness = 0.0;
    public double innMaintenanceAccruedWeekly = 0.0;
    public int weekInnRoomsSold = 0;
    public int lastNightRoomsBooked = 0;
    public double lastNightRoomRevenue = 0.0;
    public String lastNightInnSummaryLine = "Inn locked.";
    public double innDemandBoostNextNight = 0.0;
    public double lastInnDemandScore = 0.0;
    public double lastInnDemandBase = 0.0;
    public double lastInnDemandRep = 0.0;
    public double lastInnDemandClean = 0.0;
    public double lastInnDemandPubRep = 0.0;
    public double lastInnDemandPrice = 0.0;
    public double lastInnDemandSecurity = 0.0;
    public double lastInnDemandNoise = 0.0;
    public int lastInnReceptionCapacity = 0;
    public int lastInnHousekeepingCoverage = 0;
    public int lastInnHousekeepingNeeded = 0;
    public int lastInnEventsCount = 0;
    public double weekInnRevenue = 0.0;
    public int weekInnEventsCount = 0;
    public int weekInnComplaintCount = 0;
    public double weekInnEventMaintenance = 0.0;
    public double weekInnEventRefunds = 0.0;
    public final Deque<String> innEventLog = new ArrayDeque<>();
    public final List<InnBookingRecord> currentNightInnBookings = new ArrayList<>();
    public final List<InnBookingRecord> lastNightInnBookings = new ArrayList<>();
    public final List<InnPriceSegment> innPriceSegments = new ArrayList<>();
    public int innPriceChangesThisNight = 0;
    public int lastWeatherObservationDay = -999;
    public int lastMarshallObservationDay = -999;
    public String currentWeather = "Clear";
    public int lastEarlyCloseRoundsRemaining = 0;
    public int lastEarlyCloseRepPenalty = 0;
    public final Deque<String> earlyClosePenaltyLog = new ArrayDeque<>();
    public final Deque<String> chaosDeltaLog = new ArrayDeque<>();
    public MusicProfileType currentMusicProfile = MusicProfileType.ACOUSTIC_CHILL;
    public TimePhase lastMusicChangePhase = null;
    public int consecutiveNightsSameMusic = 0;
    public int weeklyMusicSwitches = 0;
    public MusicProfileType lastNightMusicProfile = MusicProfileType.ACOUSTIC_CHILL;
    public boolean sickCallTriggeredTonight = false;
    public String sickStaffNameTonight = "";
    public final List<Staff> sickStaffTonight = new ArrayList<>();
    public double teamFatigue = 0.0;
    public double rollingFatigueStress = 0.0;
    public int lastEarlyCloseCheckNight = -1;
    public int lastMusicProfileChangeRound = -999;

    private static final LocalDate START_DATE = LocalDate.of(1989, 1, 16);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public record InnBookingRecord(int rooms, double rateApplied) {}

    public record InnPriceSegment(int startRound, int endRound, double rateApplied) {}

    public int pubLevel = 0;
    public int pubLevelServeCapBonus = 0;
    public int pubLevelBarCapBonus = 0;
    public double pubLevelTrafficBonusPct = 0.0;
    public double pubLevelRepMultiplier = 1.0;
    public int pubLevelStaffCapBonus = 0;
    public int pubLevelManagerCapBonus = 0;
    public int pubLevelChefCapBonus = 0;
    public int pubLevelBouncerCapBonus = 0;
    public int starCount = 0;
    public final LegacyBonuses legacy = new LegacyBonuses();
    public int prestigeWeekStart = 1;
    public final EnumSet<MilestoneSystem.Milestone> prestigeMilestones = EnumSet.noneOf(MilestoneSystem.Milestone.class);
    public final EnumMap<LandlordActionId, LandlordActionState> landlordActionStates = new EnumMap<>(LandlordActionId.class);
    public int lastLandlordActionRound = -999;
    public double landlordIdentityScore = 0.0;
    public double landlordTrafficBonusPct = 0.0;
    public int landlordTrafficBonusRounds = 0;
    public SecurityPolicy securityPolicy = SecurityPolicy.BALANCED_DOOR;
    public SecurityTask activeSecurityTask = null;
    public int activeSecurityTaskRound = -999;
    public int lastSecurityTaskRound = -999;
    public final EnumMap<SecurityTask, Integer> securityTaskCooldowns = new EnumMap<>(SecurityTask.class);
    public final Deque<String> securityEventLog = new ArrayDeque<>();
    public int lastSecurityEventRound = -999;

    public double fohMorale = 70.0;
    public double bohMorale = 70.0;
    public double teamMorale = 70.0;

    public int nextStaffId = 1;

    public int kitchenQualityBonus = 0;
    public double refundRiskReductionPct = 0.0;
    public double staffMisconductReductionPct = 0.0;
    public double tipsThisWeek = 0.0;
    public double upgradeIncidentChanceMultiplier = 1.0;
    public double upgradeMoraleStabilityPct = 0.0;
    public double upgradeRepMitigationPct = 0.0;
    public double upgradeLossSeverityMultiplier = 1.0;

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
    public int unlockedLandlordActionTier = 1;
    public int supplierBulkUnlockTier = 0;
    public boolean premiumSupplierCatalogUnlocked = false;
    public final EnumSet<PubIdentity> weekActivityIdentityCategories = EnumSet.noneOf(PubIdentity.class);

    // reputation event tracking
    public int consecutiveHighRepRounds = 0;

    // report popups
    public boolean weeklyReportReady = false;
    public boolean fourWeekReportReady = false;
    public String weeklyReportText = "";
    public String fourWeekReportText = "";
    public boolean paydayReady = false;
    public final java.util.List<PaydayBill> paydayBills = new java.util.ArrayList<>();
    public String weeklyIdentityFlavorText = "";
    public String identityDriftSummary = "";
    public String identityDrift = "";
    public MarketPressure latestMarketPressure = MarketPressure.empty();
    public double rivalDemandTrafficMultiplier = 1.0;
    public double rivalPunterMixBias = 0.0;
    public double rivalRumorSentimentBias = 0.0;
    public String rivalDistrictUpdate = "District update: quiet week.";
    public double vipDemandBoostMultiplier = 1.0;
    public double vipRumorShield = 0.0;
    public final Deque<String> vipWeeklyNotes = new ArrayDeque<>();
    public String vipObservationSnippet = "";
    public int vipObservationRoundsRemaining = 0;

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
        for (LandlordActionId id : LandlordActionId.values()) {
            landlordActionStates.put(id, new LandlordActionState());
        }
        identityHistory.add(currentIdentity);
        currentWeather = rollWeather(random);
    }

    public int absWeekIndex() { return weekCount - 1; }
    public int weeksSincePrestige() { return Math.max(1, weekCount - prestigeWeekStart + 1); }
    public int clampRep(int r) { return Math.max(-100, Math.min(100, r)); }
    public int absDayIndex() { return dayCounter; }
    public LocalTime getCurrentTime() {
        int minutes = roundInNight * MINUTES_PER_ROUND;
        return OPENING_TIME.plusMinutes(minutes);
    }

    public TimePhase getCurrentPhase() {
        LocalTime current = getCurrentTime();
        if (current.isBefore(LocalTime.of(15, 0))) return TimePhase.EARLY_DAY;
        if (current.isBefore(LocalTime.of(18, 30))) return TimePhase.BUILD_UP;
        if (current.isBefore(LocalTime.of(21, 30))) return TimePhase.PEAK;
        return TimePhase.LATE;
    }

    public boolean isNightClosingTimeReached() {
        return roundInNight >= closingRound || !getCurrentTime().isBefore(NORMAL_CLOSING_TIME);
    }
    public LocalDate currentDate() { return START_DATE.plusDays(dayCounter); }
    public String dateString() { return currentDate().format(DATE_FORMAT); }
    public int clampCreditScore(int score) { return Math.max(300, Math.min(850, score)); }
    public double totalCreditBalance() { return creditLines.totalBalance(); }
    public double totalCreditLimit() { return creditLines.totalLimit(); }
    public double totalCreditWeeklyPaymentDue() { return creditLines.totalWeeklyPaymentDue(); }

    public double supplierPriceMultiplier() {
        double base;
        if (creditScore >= 700) base = 0.97;
        else if (creditScore >= 550) base = 1.0;
        else if (creditScore >= 450) base = 1.04;
        else base = 1.10;
        return base * (1.0 + supplierTrustPenalty);
    }

    public double bouncerRepDamageMultiplier() {
        if (bouncersHiredTonight <= 0) return 1.0;
        return Math.max(0.65, 0.9 - (0.05 * Math.min(3, bouncersHiredTonight)));
    }

    public double securityIncidentRepMultiplier() {
        double mult = 1.0;
        if (bouncersHiredTonight > 0) {
            mult *= bouncerRepDamageMultiplier();
        }
        double cctv = cctvRepMitigationPct();
        if (cctv > 0.0) {
            mult *= (1.0 - cctv);
        }
        if (upgradeRepMitigationPct > 0.0) {
            mult *= (1.0 - upgradeRepMitigationPct);
        }
        return Math.max(0.50, mult);
    }

    public int reinforcedDoorTier() {
        if (ownedUpgrades.contains(PubUpgrade.REINFORCED_DOOR_III)) return 3;
        if (ownedUpgrades.contains(PubUpgrade.REINFORCED_DOOR_II)) return 2;
        if (ownedUpgrades.contains(PubUpgrade.REINFORCED_DOOR_I)) return 1;
        return 0;
    }

    public int lightingTier() {
        if (ownedUpgrades.contains(PubUpgrade.LIGHTING_III)) return 3;
        if (ownedUpgrades.contains(PubUpgrade.LIGHTING_II)) return 2;
        if (ownedUpgrades.contains(PubUpgrade.LIGHTING_I)) return 1;
        return 0;
    }

    public int burglarAlarmTier() {
        if (ownedUpgrades.contains(PubUpgrade.BURGLAR_ALARM_III)) return 3;
        if (ownedUpgrades.contains(PubUpgrade.BURGLAR_ALARM_II)) return 2;
        if (ownedUpgrades.contains(PubUpgrade.BURGLAR_ALARM_I)) return 1;
        return 0;
    }

    public int mitigateSecurityRepHit(int repHit) {
        if (repHit >= 0) return repHit;
        double mult = securityIncidentRepMultiplier();
        int mitigated = (int) Math.round(repHit * mult);
        if (mitigated == 0 && repHit < 0) mitigated = -1;
        return mitigated;
    }

    public double cctvRepMitigationPct() {
        if (ownedUpgrades.contains(PubUpgrade.CCTV_PACKAGE)) return 0.10;
        if (ownedUpgrades.contains(PubUpgrade.CCTV)) return 0.06;
        return 0.0;
    }

    public int currentRoundIndex() {
        return dayCounter * closingRound + roundInNight;
    }

    public boolean isSecurityTaskActive() {
        return activeSecurityTask != null && activeSecurityTaskRound == currentRoundIndex();
    }

    public boolean isSecurityTaskQueued() {
        return activeSecurityTask != null && activeSecurityTaskRound > currentRoundIndex();
    }

    public double securityTaskIncidentChanceMultiplier() {
        if (!isSecurityTaskActive()) return 1.0;
        double mult = activeSecurityTask.getIncidentChanceMultiplier();
        if (lightingTier() >= 3 && activeSecurityTask.getCategory() == SecurityTaskCategory.BALANCED) {
            mult *= 0.98;
        }
        return mult;
    }

    public double securityTaskTrafficMultiplier() {
        return isSecurityTaskActive() ? activeSecurityTask.getTrafficMultiplier() : 1.0;
    }

    public int securityTaskCooldownRemaining(SecurityTask task) {
        if (task == null) return 0;
        return securityTaskCooldowns.getOrDefault(task, 0);
    }

    public double computeUpgradeIncidentChanceMultiplier() {
        double mult = 1.0;
        int doorTier = reinforcedDoorTier();
        if (doorTier == 1) mult *= 0.98;
        if (doorTier == 2) mult *= 0.95;
        if (doorTier == 3) mult *= 0.92;
        int lightTier = lightingTier();
        if (lightTier == 1) mult *= 0.99;
        if (lightTier == 2) mult *= 0.97;
        if (lightTier == 3) mult *= 0.95;
        int alarmTier = burglarAlarmTier();
        if (alarmTier == 1) mult *= 0.98;
        if (alarmTier == 2) mult *= 0.95;
        if (alarmTier == 3) mult *= 0.90;
        return Math.max(0.70, mult);
    }

    public double computeUpgradeMoraleStabilityPct() {
        int tier = lightingTier();
        if (tier == 2) return 0.05;
        if (tier == 3) return 0.10;
        return 0.0;
    }

    public double computeUpgradeRepMitigationPct() {
        double pct = 0.0;
        int doorTier = reinforcedDoorTier();
        if (doorTier == 2) pct += 0.03;
        if (doorTier == 3) pct += 0.06;
        int lightTier = lightingTier();
        if (lightTier == 2) pct += 0.02;
        if (lightTier == 3) pct += 0.04;
        return Math.min(0.20, pct);
    }

    public double computeUpgradeLossSeverityMultiplier() {
        double reduction = Math.max(0.0, Math.min(0.35, upgradeEventDamageReductionPct));
        return Math.max(0.55, 1.0 - reduction);
    }

    public void addSecurityLog(String entry) {
        if (entry == null || entry.isBlank()) return;
        String prefix = "W" + weekCount + " D" + (dayIndex + 1) + " R" + roundInNight + ": ";
        securityEventLog.addFirst(prefix + entry);
        lastSecurityEventRound = currentRoundIndex();
        while (securityEventLog.size() > 6) {
            securityEventLog.removeLast();
        }
    }

    public double supplierInvoiceMultiplier() {
        double base;
        if (creditScore >= 700) base = 0.98;
        else if (creditScore >= 550) base = 1.0;
        else if (creditScore >= 450) base = 1.03;
        else base = 1.08;
        return base * (1.0 + supplierTrustPenalty) * debtSpiralSupplierTrustMultiplier;
    }

    public String supplierTrustLabel() {
        if (creditScore >= 700) return "Good";
        if (creditScore >= 550) return "Neutral";
        if (creditScore >= 450) return "Poor";
        return "Very Poor";
    }

    public double supplierCreditCap() {
        if (supplierCreditCapOverride > 0.0) {
            return supplierCreditCapOverride;
        }
        double base;
        if (creditScore >= 700) base = 3200.0;
        else if (creditScore >= 600) base = 2500.0;
        else if (creditScore >= 500) base = 1800.0;
        else base = 1200.0;
        double trustMult = Math.max(0.6, 1.0 - (supplierTrustPenalty * 3.0));
        double levelMult = 1.0 + (0.08 * pubLevel);
        return base * trustMult * levelMult + legacy.supplierTradeCreditBonus;
    }

    public int debtSpiralTierFromStreak() {
        if (consecutiveWeeksUnpaidMin <= 0) return 0;
        if (consecutiveWeeksUnpaidMin == 1) return 1;
        if (consecutiveWeeksUnpaidMin == 2) return 2;
        if (consecutiveWeeksUnpaidMin == 3) return 3;
        return 4;
    }

    public double supplierMinDue(SupplierTradeCredit account) {
        if (account == null || account.getBalance() <= 0.0) return 0.0;
        return Math.max(35.0, account.getBalance() * 0.12);
    }

    public double supplierWineMinDue() {
        return supplierMinDue(supplierWineCredit);
    }

    public double supplierFoodMinDue() {
        return supplierMinDue(supplierFoodCredit);
    }

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
            int hohCount,
            int hohCap,
            int managerCount,
            int assistantManagerCount,
            int dutyManagerCount,
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
                    + " | HOH: " + hohCount + "/" + hohCap
                    + " | Managers: " + managerPoolCount + "/" + managerCap
                    + " (GM " + managerCount + ", AM " + assistantManagerCount + ", DM " + dutyManagerCount + ")"
                    + (bouncersTonight > 0 ? " | Bouncer: " + bouncersTonight + "/" + bouncerCap : "")
                    + " | Morale: " + (int)Math.round(teamMorale)
                    + " | Upgrades: " + upgradesOwned
                    + (activityTonight != null ? " | Activity: " + activityTonight : "");
        }
    }

    public StaffSummary staff() {
        return new StaffSummary(
                fohStaffCount() + hohStaffCount(),
                fohStaffCap + hohStaffCap,
                hohStaffCount(),
                hohStaffCap,
                generalManagers.size(),
                assistantManagerCount(),
                dutyManagerCount(),
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

    public int fohStaffCount() {
        int count = 0;
        for (Staff st : fohStaff) {
            if (st.getType() != Staff.Type.ASSISTANT_MANAGER && !isHohRole(st.getType())) count++;
        }
        return count;
    }

    public int hohStaffCount() {
        int count = 0;
        for (Staff st : fohStaff) {
            if (isHohRole(st.getType())) count++;
        }
        return count;
    }

    public boolean isHohRole(Staff.Type type) {
        if (type == null) return false;
        return type == Staff.Type.RECEPTION_TRAINEE
                || type == Staff.Type.RECEPTIONIST
                || type == Staff.Type.SENIOR_RECEPTIONIST
                || type == Staff.Type.HOUSEKEEPING_TRAINEE
                || type == Staff.Type.HOUSEKEEPER
                || type == Staff.Type.HEAD_HOUSEKEEPER;
    }

    public int marshallCount() {
        return marshalls.size();
    }

    public boolean isMarshallUnlocked() {
        return ownedUpgrades.contains(PubUpgrade.MARSHALLS_I)
                || ownedUpgrades.contains(PubUpgrade.MARSHALLS_II)
                || ownedUpgrades.contains(PubUpgrade.MARSHALLS_III);
    }

    public String weatherLabel() {
        String emoji = switch (currentWeather) {
            case "Sunny", "Sunshine" -> "☀️ ";
            case "Rain" -> "🌧️ ";
            case "Heavy Rain" -> "🌧️ ";
            case "Windy" -> "💨 ";
            case "Cold" -> "❄️ ";
            case "Hail" -> "🌨️ ";
            case "Stormy" -> "⛈️ ";
            case "Rainbow" -> "🌈 ";
            case "Cloudy" -> "☁️ ";
            default -> "";
        };
        return emoji + currentWeather;
    }

    public String rollWeather(Random rng) {
        int roll = rng.nextInt(100);
        if (roll < 14) return "Clear";
        if (roll < 27) return "Cloudy";
        if (roll < 38) return "Sunny";
        if (roll < 48) return "Sunshine";
        if (roll < 64) return "Rain";
        if (roll < 72) return "Heavy Rain";
        if (roll < 80) return "Windy";
        if (roll < 88) return "Cold";
        if (roll < 94) return "Hail";
        if (roll < 98) return "Stormy";
        return "Rainbow";
    }

    public double innStaffWeeklyWages() {
        double total = 0.0;
        for (Staff st : fohStaff) {
            if (st.getType() == Staff.Type.RECEPTION_TRAINEE
                    || st.getType() == Staff.Type.RECEPTIONIST
                    || st.getType() == Staff.Type.SENIOR_RECEPTIONIST
                    || st.getType() == Staff.Type.HOUSEKEEPING_TRAINEE
                    || st.getType() == Staff.Type.HOUSEKEEPER
                    || st.getType() == Staff.Type.HEAD_HOUSEKEEPER
                    || st.getType() == Staff.Type.DUTY_MANAGER) {
                total += st.getWeeklyWage();
            }
        }
        return total;
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
        return generalManagers.size() + assistantManagerCount() + dutyManagerCount();
    }

    public int dutyManagerCount() {
        int count = 0;
        for (Staff st : fohStaff) {
            if (st.getType() == Staff.Type.DUTY_MANAGER) count++;
        }
        return count;
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
