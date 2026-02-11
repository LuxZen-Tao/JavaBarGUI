import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulation {
    private static final double INN_DEFAULT_ROOM_PRICE = 45.0;
    private static final double INN_MAINTENANCE_PER_ROOM = 2.6;
    private static final double INN_USAGE_CLEANLINESS_DECAY = 1.3;
    private static final double INN_CLEAN_RECOVERY = 2.0;
    private static final int INN_PRICE_VOLATILITY_THRESHOLD = 2;
    private static final double INN_PRICE_VOLATILITY_CHAOS = 1.0;
    private static final double INN_PRICE_VOLATILITY_INN_REP = 0.6;

    private static final List<String> MISCONDUCT_DRIVER_LINES = List.of(
            "Pressure stacked up with low morale and high chaos.",
            "Tension spiked; the room felt off and it showed.",
            "Loose oversight and a rough shift made a slip more likely.",
            "Night stress boiled over in a small but costly way.",
            "A shaky vibe set the stage for a bad call."
    );
    private static final List<String> RUMOR_DRIVER_LINES = List.of(
            "Review chatter leaned on tonight's vibe and morale.",
            "Posts reflected the mood shift and how the night felt.",
            "Word of mouth followed the room energy and service flow.",
            "Talk picked up around the staff mood and late-night feel.",
            "Comments mirrored the night's tone and pressure points."
    );

    private static final List<String> FOH_FREE_DRINKS_LINES = List.of(
            "Comped a couple rounds to smooth things over.",
            "Slipped extra drinks to a table without ringing them in.",
            "Handed out freebies to keep a loud group happy.",
            "Covered a tab they shouldn't have.",
            "Let a mate drink on the house."
    );
    private static final List<String> FOH_TILL_SHORT_LINES = List.of(
            "Till came up light after a busy rush.",
            "Cash drawer was short by the end of the round.",
            "A float went missing between tabs.",
            "Receipts didn’t line up with the register.",
            "Counted cash twice; still short."
    );
    private static final List<String> FOH_MANAGEMENT_INSULT_LINES = List.of(
            "Snapped at a manager in front of guests.",
            "Argued with the floor lead where customers could hear.",
            "Pushed back on direction mid-shift.",
            "Took a swipe at management on the floor.",
            "Let frustration spill out at the worst time."
    );
    private static final List<String> FOH_FLIRTING_LINES = List.of(
            "Turned on the charm and it worked a little too well.",
            "Kept the banter going and won over a table.",
            "Played up the friendly vibe and got extra tips.",
            "Handled a group with a wink and a grin.",
            "Kept things light and the room responded."
    );
    private static final List<String> FOH_COMPROMISING_LINES = List.of(
            "Caught taking a long break out back.",
            "Took an extended pause when the bar was busy.",
            "Disappeared mid-rush for a private breather.",
            "Left the floor short-handed for a bit.",
            "Got spotted off the clock while the queue grew."
    );

    private static final List<String> BOH_INGREDIENTS_LINES = List.of(
            "Miscounted prep and a tray vanished.",
            "Sent out ingredients for the wrong station.",
            "Prep inventory was lighter than expected.",
            "A box of staples went missing during the rush.",
            "Stock got raided without logging it."
    );
    private static final List<String> BOH_HYGIENE_LINES = List.of(
            "Prep standards slipped at the wrong moment.",
            "A hygiene miss got noticed during service.",
            "Cleanup lagged and it showed in the pass.",
            "Corner cuts on sanitizing caused a scare.",
            "Kitchen habits got sloppy during the rush."
    );
    private static final List<String> BOH_WASTED_BATCH_LINES = List.of(
            "A batch came out wrong and got binned.",
            "Overcooked a pan and had to start again.",
            "Dropped a tray mid-rush.",
            "Missed the timing on a big prep batch.",
            "Ruined a line of plates in the final minute."
    );
    private static final List<String> BOH_ARGUMENT_LINES = List.of(
            "The line got heated and lost focus.",
            "A shouting match broke out at the pass.",
            "The kitchen bickered through the rush.",
            "Tempers flared and tickets slowed.",
            "Voices rose and the line lost rhythm."
    );
    private static final List<String> BOH_HERO_LINES = List.of(
            "Recovered a rough service with clean plates.",
            "Pulled off a smooth finish under pressure.",
            "Kept quality high through a tough rush.",
            "Saved a wobble with sharp timing.",
            "Turned a messy moment into a clean win."
    );

    private static final int GOOD_UNSERVED_MAX = 1;
    private static final int BAD_UNSERVED_MIN = 4;
    private static final int BAD_FOOD_MISSES_MIN = 2;
    private static final double CHAOS_BAD_DELTA_1 = 4.0;
    private static final double CHAOS_BAD_DELTA_2 = 6.0;
    private static final double CHAOS_BAD_DELTA_3 = 8.0;
    private static final double CHAOS_GOOD_DELTA_1 = -3.0;
    private static final double CHAOS_GOOD_DELTA_2 = -5.0;
    private static final double CHAOS_GOOD_DELTA_3 = -7.0;
    private static final int CHAOS_STREAK_CAP = 3;
    private static final double CHAOS_MIN = 0.0;
    private static final double CHAOS_MAX = 100.0;
    private static final double ACTION_IDENTITY_RANGE = 10.0;
    private static final double ACTION_ALIGN_CHANCE_BOOST = 0.08;
    private static final double ACTION_BALANCED_OUTCOME_BONUS = 0.20;
    private static final double ACTION_ALIGNED_OUTCOME_BONUS = 0.12;
    private static final double ACTION_MISALIGNED_PENALTY = 0.06;
    private static final int BAILIFF_THRESHOLD_WEEKS = 3;
    private static final int BAILIFF_UPGRADES_REMOVED_PER_VISIT = 2;
    private static final double BAILIFF_CASH_SEIZE_FLAT = 120.0;
    private static final double BAILIFF_CASH_SEIZE_PCT = 0.10;
    private static final int BAILIFF_REP_SCAR = -12;
    private static final double BANKRUPTCY_SUPPLIER_CREDIT_CAP = 400.0;
    private static final int BANKRUPTCY_LONG_LOAN_LOCK_WEEKS = 24;
    private static final double BANKRUPTCY_SHARK_APR_BONUS = 0.38;
    private static final double BANKRUPTCY_SHARK_PENALTY_BONUS = 0.10;
    private static final double BANKRUPTCY_NEG_REP_MULT = 1.45;
    private static final double BANKRUPTCY_POS_REP_MULT = 0.55;


    private enum RumorTone { NEGATIVE, MIXED, POSITIVE }
    private enum RoundClassification { STRONGLY_NEGATIVE, MOSTLY_POSITIVE, NEUTRAL }
    private enum MisconductType {
        FREE_DRINKS,
        TILL_SHORT,
        MANAGEMENT_INSULT,
        FLIRTING_ATTENTION,
        COMPROMISING_BREAK,
        INGREDIENTS_MISSING,
        HYGIENE_SLIP,
        WASTED_BATCH,
        KITCHEN_ARGUMENT,
        KITCHEN_HERO
    }

    private final GameState s;
    private final UILogger log;

    private final EconomySystem eco;
    private final UpgradeSystem upgrades;
    private final ActivitySystem activities;
    private final InventorySystem inv;
    private final StaffSystem staff;
    private final SecuritySystem security;
    private final EventSystem events;
    private final PunterSystem punters;
    private final SupplierSystem supplierSystem;
    private final RivalSystem rivalSystem;
    private final MilestoneSystem milestones;
    private final PubIdentitySystem identitySystem;
    private final RumorSystem rumors;
    private final PubLevelSystem pubLevelSystem;
    private final PrestigeSystem prestigeSystem;
    private final ObservationEngine observationEngine;
    private final MusicSystem musicSystem;
    private final AudioManager audioManager;
    private final VIPSystem vipSystem;
    private java.util.function.IntConsumer weekStartHook;

    public Simulation(GameState state, UILogger log) {

        this.s = state;
        this.log = log;

        this.eco = new EconomySystem(s, log);
        this.upgrades = new UpgradeSystem(s);
        this.activities = new ActivitySystem(s);
        this.inv = new InventorySystem(s);
        this.staff = new StaffSystem(s, eco, upgrades);
        this.security = new SecuritySystem(s, eco, log);
        this.events = new EventSystem(s, eco, log);
        this.rumors = new RumorSystem(s, log);
        this.punters = new PunterSystem(s, eco, inv, events, rumors, log);
        this.supplierSystem = new SupplierSystem(s);
        this.rivalSystem = new RivalSystem();
        this.milestones = new MilestoneSystem(s, log);
        this.eco.setMilestones(milestones);
        this.identitySystem = new PubIdentitySystem(s, log);
        this.pubLevelSystem = new PubLevelSystem();
        this.prestigeSystem = new PrestigeSystem();
        this.observationEngine = new ObservationEngine();
        this.musicSystem = new MusicSystem(s);
        this.audioManager = new AudioManager();
        this.vipSystem = new VIPSystem();

        markReportStartIfMissing();
        s.creditScoreAtWeekStart = s.creditScore;

        // Apply persistent upgrade effects at boot
        applyPersistentUpgrades();
        applyDebtSpiralPenalties();
        staff.updateTeamMorale();

        if (s.pubName == null || s.pubName.isBlank()) {
            s.pubName = PubNameGenerator.randomName(s.random);
        }

        // Deal exists BETWEEN nights (so you can restock before opening)
        supplierSystem.rollNewDeal();
        log.popup(" Supplier deal", "Available between nights: " + supplierSystem.dealLabel(), "");

        audioManager.setMusicProfile(s.currentMusicProfile != null ? s.currentMusicProfile.name() : MusicProfileType.ACOUSTIC_CHILL.name());
        recomputeActivityAvailability();
        milestones.recomputeUpgradeAvailability();
    }

    public void setWeekStartHook(java.util.function.IntConsumer weekStartHook) {
        this.weekStartHook = weekStartHook;
    }

    public void setMusicVolume(int volume) {
        audioManager.setMusicVolume(volume);
    }

    public void setChatterVolume(int volume) {
        audioManager.setChatterVolume(volume);
    }

    public int getMusicVolume() {
        return audioManager.getMusicVolume();
    }

    public int getChatterVolume() {
        return audioManager.getChatterVolume();
    }

    public void shutdown() {
        audioManager.shutdown();
    }

    /** Re-apply upgrades that change hard caps / base stats. Call at boot + on buyUpgrade + on openNight. */
    private void applyPersistentUpgrades() {
        pubLevelSystem.updatePubLevel(s);
        // Rack cap + spoil tuning
        int baseRack = s.baseRackCapacity;
        int rackCap = baseRack + upgrades.rackCapBonus() + s.legacy.inventoryCapBonus;
        s.rack.setCapacity(rackCap);

        // Spoilage window (optional via upgrades later)
        s.rack.setSpoilAfterDays(s.spoilDays);

        // Security baseline bonus from upgrades (kept as separate field to avoid rewriting SecuritySystem)
        UpgradeSystem.UpgradeModifierSnapshot upgradeMods = upgrades.buildModifierSnapshot();
        s.upgradeSecurityBonus = upgradeMods.securityBonus();
        s.fohStaffCap = Math.max(1, s.baseStaffCap + s.pubLevelStaffCapBonus + upgradeMods.staffCapBonus());
        s.bouncerCap = Math.max(1, s.baseBouncerCap + s.pubLevelBouncerCapBonus + upgradeMods.bouncerCapBonus());
        s.managerCap = Math.max(1, s.baseManagerCap + s.pubLevelManagerCapBonus + upgradeMods.managerCapBonus());
        s.kitchenChefCap = Math.max(1, s.baseKitchenChefCap + s.pubLevelChefCapBonus + upgradeMods.chefCapBonus());
        s.marshallCap = Math.max(0, s.baseMarshallCap + marshallCapBonusFromUpgrades());
        s.kitchenQualityBonus = upgradeMods.kitchenQualityBonus();
        s.refundRiskReductionPct = upgradeMods.refundRiskReductionPct();
        s.staffMisconductReductionPct = upgradeMods.staffMisconductReductionPct();

        // Bar cap bonus is applied per-night (because base pool changes with rep/weekend)
        // We store it so openNight can add it.
        s.upgradeBarCapBonus = upgradeMods.barCapBonus();

        s.upgradeServeCapBonus = upgradeMods.serveCapBonus();
        s.upgradeTipBonusPct = upgradeMods.tipBonusPct();
        s.upgradeEventDamageReductionPct = upgradeMods.eventDamageReductionPct();
        s.upgradeRiskReductionPct = upgradeMods.riskReductionPct();
        s.upgradeFoodRackCapBonus = upgradeMods.foodRackCapBonus();
        s.upgradeIncidentChanceMultiplier = upgradeMods.incidentChanceMultiplier();
        s.upgradeMoraleStabilityPct = upgradeMods.moraleStabilityPct();
        s.upgradeRepMitigationPct = upgradeMods.repMitigationPct();
        s.upgradeLossSeverityMultiplier = upgradeMods.lossSeverityMultiplier();

        applyInnUpgradeState();

        int kitchenLevel = kitchenUpgradeLevel();
        if (!s.kitchenUnlocked) {
            kitchenLevel = 0;
        }
        s.kitchenPrepSpeedBonus = 0;
        s.kitchenSpoilBonusDays = 0;
        s.bohMoraleResiliencePct = 0.0;
        s.foodNightRepBonus = 0;
        int kitchenQualityBonus = s.kitchenQualityBonus;
        if (kitchenLevel >= 1) {
            s.kitchenPrepSpeedBonus += 1;
            s.kitchenSpoilBonusDays += 1;
        }
        if (kitchenLevel >= 2) {
            s.kitchenPrepSpeedBonus += 1;
            s.kitchenSpoilBonusDays += 2;
            s.bohMoraleResiliencePct = 0.12;
            s.foodNightRepBonus = 1;
        }
        if (kitchenLevel >= 3) {
            s.kitchenPrepSpeedBonus += 1;
            s.kitchenSpoilBonusDays += 3;
            kitchenQualityBonus += 2;
            s.bohMoraleResiliencePct = Math.max(s.bohMoraleResiliencePct, 0.20);
            s.foodNightRepBonus = Math.max(s.foodNightRepBonus, 2);
        }
        s.kitchenQualityBonus = kitchenQualityBonus;
        s.foodPrepRounds = Math.max(1, s.baseFoodPrepRounds - s.kitchenPrepSpeedBonus);
        s.foodRack.setSpoilAfterDays(s.baseFoodSpoilDays + s.kitchenSpoilBonusDays);

        int headChefs = s.staffCountOfType(Staff.Type.HEAD_CHEF);
        s.foodRack.setCapacity(s.baseFoodRackCapacity + s.upgradeFoodRackCapBonus
                + s.legacy.inventoryCapBonus + (headChefs * 5));
        milestones.recomputeUpgradeAvailability();
    }

    private void resetUpgradeStateForPrestige() {
        s.pubLevel = 0;
        s.pubLevelServeCapBonus = 0;
        s.pubLevelBarCapBonus = 0;
        s.pubLevelTrafficBonusPct = 0.0;
        s.pubLevelRepMultiplier = 1.0;
        s.pubLevelStaffCapBonus = 0;
        s.pubLevelManagerCapBonus = 0;
        s.pubLevelChefCapBonus = 0;
        s.pubLevelBouncerCapBonus = 0;

        s.ownedUpgrades.clear();
        s.pendingUpgradeInstalls.clear();
        s.upgradeSecurityBonus = 0;
        s.upgradeBarCapBonus = 0;
        s.upgradeServeCapBonus = 0;
        s.upgradeTipBonusPct = 0.0;
        s.upgradeEventDamageReductionPct = 0.0;
        s.upgradeRiskReductionPct = 0.0;
        s.upgradeFoodRackCapBonus = 0;
        s.upgradeIncidentChanceMultiplier = 1.0;
        s.upgradeMoraleStabilityPct = 0.0;
        s.upgradeRepMitigationPct = 0.0;
        s.upgradeLossSeverityMultiplier = 1.0;

        s.kitchenUnlocked = false;
        s.kitchenPrepSpeedBonus = 0;
        s.kitchenSpoilBonusDays = 0;
        s.bohMoraleResiliencePct = 0.0;
        s.foodNightRepBonus = 0;

        s.innUnlocked = false;
        s.innTier = 0;
        s.roomsTotal = 0;
        s.roomsBookedLast = 0;
        s.roomPrice = 0.0;
        s.innRep = 0.0;
        s.cleanliness = 0.0;
        s.lastNightInnSummaryLine = "Inn locked.";
        s.lastNightRoomsBooked = 0;
        s.lastNightRoomRevenue = 0.0;

        s.baseSecurityLevel = 0;
    }

    private void applyInnUpgradeState() {
        int tier = innTierFromUpgrades();
        if (tier <= 0 && !s.innUnlocked) return;
        if (tier > 0) {
            if (!s.innUnlocked) {
                s.innUnlocked = true;
                s.innRep = 60.0;
                s.cleanliness = 80.0;
                s.roomPrice = INN_DEFAULT_ROOM_PRICE;
                s.lastNightInnSummaryLine = "Inn unlocked. Ready for bookings.";
            }
            s.innTier = tier;
            s.roomsTotal = innRoomsForTier(tier) + s.legacy.innRoomBonus;
            s.hohStaffCap = s.baseHohCap + innHohCapForTier(tier);
            if (s.roomsBookedLast > s.roomsTotal) {
                s.roomsBookedLast = s.roomsTotal;
            }
            if (s.lastNightRoomsBooked > s.roomsTotal) {
                s.lastNightRoomsBooked = s.roomsTotal;
            }
        } else {
            s.hohStaffCap = s.baseHohCap;
        }
    }

    private int innTierFromUpgrades() {
        if (s.ownedUpgrades.contains(PubUpgrade.INN_WING_5)) return 5;
        if (s.ownedUpgrades.contains(PubUpgrade.INN_WING_4)) return 4;
        if (s.ownedUpgrades.contains(PubUpgrade.INN_WING_3)) return 3;
        if (s.ownedUpgrades.contains(PubUpgrade.INN_WING_2)) return 2;
        if (s.ownedUpgrades.contains(PubUpgrade.INN_WING_1)) return 1;
        return 0;
    }

    private int innRoomsForTier(int tier) {
        return switch (tier) {
            case 1 -> 3;
            case 2 -> 6;
            case 3 -> 10;
            case 4 -> 15;
            case 5 -> 25;
            default -> 0;
        };
    }

    private int innHohCapForTier(int tier) {
        return switch (tier) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 8;
            case 5 -> 10;
            default -> 0;
        };
    }

    private int marshallCapBonusFromUpgrades() {
        int bonus = 0;
        if (s.ownedUpgrades.contains(PubUpgrade.MARSHALLS_II)) bonus += 2;
        if (s.ownedUpgrades.contains(PubUpgrade.MARSHALLS_III)) bonus += 2;
        return bonus;
    }

    public int landlordActionTier() {
        return clamp(s.unlockedLandlordActionTier, 1, 5);
    }

    public String pubLevelBadgeLine() {
        return pubLevelSystem.compactNextLevelBadge(s);
    }

    public String pubNameBadgeHtml() {
        String stars = buildStarBadge();
        String nextLevelLine = pubLevelSystem.compactNextLevelBadge(s);
        return "<html> " + s.pubName + " (Lv " + s.pubLevel + ")" + stars
                + "<br/><span style='font-size:10px'>" + nextLevelLine + "</span></html>";
    }

    public String buildStarBadge() {
        if (s.starCount <= 0) return "";
        return " " + "★".repeat(s.starCount);
    }

    public boolean isPrestigeAvailable() {
        return prestigeSystem.isPrestigeEligible(s, pubLevelSystem);
    }

    public boolean isPrestigeMaxed() {
        return prestigeSystem.isMaxStars(s);
    }

    public PrestigeSystem.PrestigePreview buildPrestigePreview() {
        return prestigeSystem.buildPreview(s, upgrades, pubLevelSystem);
    }

    public boolean confirmPrestige() {
        if (!isPrestigeAvailable()) return false;
        LegacyBonuses award = prestigeSystem.computePrestigeAward(s, upgrades);
        s.legacy.add(award);
        s.starCount = Math.min(PrestigeSystem.MAX_STARS, s.starCount + 1);
        s.prestigeWeekStart = s.weekCount;
        s.prestigeMilestones.clear();
        s.achievedMilestones.clear();
        resetUpgradeStateForPrestige();
        applyPersistentUpgrades();
        return true;
    }

    public List<LandlordActionDef> getAvailableActionsForCurrentTier() {
        return LandlordActionCatalog.actionsForTier(landlordActionTier());
    }

    public boolean canUseLandlordActionThisRound() {
        return s.nightOpen && currentRoundIndex() != s.lastLandlordActionRound;
    }

    public LandlordActionAvailability landlordActionAvailability(LandlordActionDef def) {
        if (!s.nightOpen) {
            return new LandlordActionAvailability(false, "Night must be open.");
        }
        if (landlordActionTier() < def.getTier()) {
            return new LandlordActionAvailability(false, "Locked: Pub Level " + def.getTier());
        }
        if (currentRoundIndex() == s.lastLandlordActionRound) {
            return new LandlordActionAvailability(false, "Only 1 action per round.");
        }
        LandlordActionState actionState = s.landlordActionStates.get(def.getId());
        if (actionState != null && actionState.getCooldownRemaining() > 0) {
            return new LandlordActionAvailability(false, "Cooldown: " + actionState.getCooldownRemaining() + "r");
        }
        return new LandlordActionAvailability(true, "");
    }

    public double computeActionChance(LandlordActionDef def) {
        double base = def.getBaseChance();
        double idFactor = clamp(s.landlordIdentityScore / ACTION_IDENTITY_RANGE, -1.0, 1.0);
        if (def.getCategory() == LandlordActionCategory.CLASSY) {
            base += ACTION_ALIGN_CHANCE_BOOST * idFactor;
        } else if (def.getCategory() == LandlordActionCategory.SHADY) {
            base -= ACTION_ALIGN_CHANCE_BOOST * idFactor;
        }
        return clamp(base, 0.05, 0.95);
    }

    public String landlordIdentityLabel() {
        if (s.landlordIdentityScore >= 2.5) return "Classy-leaning";
        if (s.landlordIdentityScore <= -2.5) return "Shady-leaning";
        return "Neutral";
    }

    public LandlordActionResolution resolveLandlordAction(LandlordActionId id) {
        LandlordActionDef def = LandlordActionCatalog.byId(id);
        if (def == null) return LandlordActionResolution.blocked(null, "Unknown action.");
        LandlordActionAvailability availability = landlordActionAvailability(def);
        if (!availability.canUse()) {
            return LandlordActionResolution.blocked(def, availability.reason());
        }

        LandlordActionState actionState = s.landlordActionStates.get(def.getId());
        if (actionState == null) {
            actionState = new LandlordActionState();
            s.landlordActionStates.put(def.getId(), actionState);
        }

        double chance = computeActionChance(def);
        boolean success = s.random.nextDouble() < chance;
        LandlordActionEffectRange range = success ? def.getSuccessRange() : def.getFailureRange();
        int trafficRounds = success ? def.getSuccessTrafficRounds() : def.getFailureTrafficRounds();
        double scale = computeOutcomeScale(def, success, s.landlordIdentityScore);

        int repDelta = rollScaled(range.repMin(), range.repMax(), scale);
        int moraleDelta = rollScaled(range.moraleMin(), range.moraleMax(), scale);
        double trafficPct = rollScaled(range.trafficMinPct(), range.trafficMaxPct(), scale);
        double chaosDelta = rollScaled(range.chaosMin(), range.chaosMax(), scale);

        if (repDelta != 0) {
            eco.applyRep(repDelta, "Landlord action: " + def.getName());
        }
        if (moraleDelta != 0) {
            adjustStaffMorale(moraleDelta);
        }
        if (trafficPct != 0.0 && trafficRounds > 0) {
            applyLandlordTrafficBonus(trafficPct, trafficRounds);
        }
        if (chaosDelta != 0.0) {
            s.addChaos(chaosDelta);
        }

        updateLandlordIdentity(def.getCategory(), success);

        actionState.setCooldownRemaining(def.getCooldownRounds());
        actionState.setLastUsedRound(currentRoundIndex());
        s.lastLandlordActionRound = currentRoundIndex();

        String outcome = success ? "succeeded" : "fell flat";
        String summary = buildActionSummary(def, success, repDelta, moraleDelta, trafficPct, chaosDelta, trafficRounds);
        log.event(" Landlord action: " + def.getName() + " " + outcome + ". " + summary);
        s.observationLine = trimObservationLine(def.getName() + ": " + summary);
        s.lastObservationRound = currentRoundIndex();

        return new LandlordActionResolution(def, success, false, summary, repDelta, moraleDelta, trafficPct, trafficRounds, chaosDelta);
    }

    double computeOutcomeScale(LandlordActionDef def, boolean success, double identityScore) {
        double pos = clamp(identityScore / ACTION_IDENTITY_RANGE, 0.0, 1.0);
        double neg = clamp(-identityScore / ACTION_IDENTITY_RANGE, 0.0, 1.0);
        double scale = 1.0;

        if (def.getCategory() == LandlordActionCategory.BALANCED) {
            if (success) {
                scale += ACTION_BALANCED_OUTCOME_BONUS * pos;
            } else {
                scale += ACTION_BALANCED_OUTCOME_BONUS * neg;
            }
            return scale;
        }

        boolean aligned = (def.getCategory() == LandlordActionCategory.CLASSY && identityScore >= 0.0)
                || (def.getCategory() == LandlordActionCategory.SHADY && identityScore <= 0.0);
        double alignedFactor = Math.max(pos, neg);

        if (success) {
            scale += aligned ? ACTION_ALIGNED_OUTCOME_BONUS * alignedFactor : -ACTION_MISALIGNED_PENALTY * alignedFactor;
        } else {
            scale += aligned ? -ACTION_ALIGNED_OUTCOME_BONUS * alignedFactor : ACTION_MISALIGNED_PENALTY * alignedFactor;
        }
        return scale;
    }

    private void updateLandlordIdentity(LandlordActionCategory category, boolean success) {
        double delta = 0.0;
        switch (category) {
            case CLASSY -> delta = success ? 0.8 : 0.3;
            case SHADY -> delta = success ? -0.8 : -0.3;
            case BALANCED -> delta = success ? 0.35 : -0.35;
        }
        s.landlordIdentityScore = clamp(s.landlordIdentityScore + delta, -ACTION_IDENTITY_RANGE, ACTION_IDENTITY_RANGE);
    }

    private void adjustStaffMorale(int moraleDelta) {
        for (Staff st : s.fohStaff) st.adjustMorale(moraleDelta);
        for (Staff st : s.bohStaff) st.adjustMorale(moraleDelta);
        for (Staff st : s.generalManagers) st.adjustMorale(moraleDelta);
        staff.updateTeamMorale();
    }

    private void applyLandlordTrafficBonus(double pct, int rounds) {
        s.landlordTrafficBonusPct += pct;
        s.landlordTrafficBonusPct = clamp(s.landlordTrafficBonusPct, -0.5, 0.6);
        s.landlordTrafficBonusRounds = Math.max(s.landlordTrafficBonusRounds, rounds);
    }

    private void tickLandlordActionCooldowns() {
        for (LandlordActionState state : s.landlordActionStates.values()) {
            if (state.getCooldownRemaining() > 0) {
                state.setCooldownRemaining(state.getCooldownRemaining() - 1);
            }
        }
        if (s.landlordTrafficBonusRounds > 0) {
            s.landlordTrafficBonusRounds -= 1;
            if (s.landlordTrafficBonusRounds <= 0) {
                s.landlordTrafficBonusRounds = 0;
                s.landlordTrafficBonusPct = 0.0;
            }
        }
    }

    private void tickSecurityTaskCooldowns() {
        for (SecurityTask task : SecurityTask.values()) {
            int cooldown = s.securityTaskCooldownRemaining(task);
            if (cooldown > 0) {
                s.securityTaskCooldowns.put(task, cooldown - 1);
            }
        }
    }

    private int currentRoundIndex() {
        return s.dayCounter * s.closingRound + s.roundInNight;
    }

    private int rollScaled(int min, int max, double scale) {
        if (min > max) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        int value = min + s.random.nextInt(max - min + 1);
        return (int) Math.round(value * scale);
    }

    private double rollScaled(double min, double max, double scale) {
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        double value = min + (s.random.nextDouble() * (max - min));
        return value * scale;
    }

    private String buildActionSummary(LandlordActionDef def,
                                      boolean success,
                                      int repDelta,
                                      int moraleDelta,
                                      double trafficPct,
                                      double chaosDelta,
                                      int trafficRounds) {
        StringBuilder sb = new StringBuilder();
        sb.append(success ? "Success" : "Failure");
        sb.append(" | Rep ").append(formatDelta(repDelta));
        if (moraleDelta != 0) {
            sb.append(", Morale ").append(formatDelta(moraleDelta));
        }
        if (trafficPct != 0.0) {
            sb.append(", Traffic ").append(formatPct(trafficPct));
            if (trafficRounds > 0) sb.append(" (").append(trafficRounds).append("r)");
        }
        if (chaosDelta != 0.0) {
            sb.append(", Chaos ").append(formatDelta((int) Math.round(chaosDelta)));
        }
        return sb.toString();
    }

    private String formatDelta(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private String formatPct(double value) {
        int pct = (int) Math.round(value * 100);
        return pct >= 0 ? "+" + pct + "%" : pct + "%";
    }

    private String trimObservationLine(String text) {
        if (text == null) return null;
        if (text.length() <= ObservationEngine.MAX_OBSERVATION_LENGTH) return text;
        return text.substring(0, ObservationEngine.MAX_OBSERVATION_LENGTH - 1) + "…";
    }

    // GUI helper: show true supplier buy cost (rep + deal)
    public double peekSupplierCost(Wine w) { return peekSupplierCost(w, 1); }

    public double peekSupplierCost(Wine w, int qty) {
        if (w == null) return 0.0;
        qty = Math.max(1, qty);
        if (s.nightOpen && s.canEmergencyRestock()) {
            double markup = s.isWeekend() ? 1.7 : 1.3;
            double baseCost = w.getBaseCost() * qty * s.supplierPriceMultiplier();
            return Math.max(0.0, baseCost * markup);
        }
        double repMult = inv.repToSupplierCostMultiplier();
        double cost = supplierSystem.supplierBuyCost(w, repMult, qty);
        return Math.max(0.0, cost);
    }

    // ---------- GUI actions ----------
    public void setPriceMultiplier(double m) {
        s.priceMultiplier = Math.max(0.50, Math.min(2.50, m));
    }

    public boolean setMusicProfile(MusicProfileType profile) {
        if (profile == null) return false;
        if (s.currentMusicProfile == profile) return false;
        TimePhase phase = s.getCurrentPhase();
        if (s.nightOpen && s.lastMusicChangePhase == phase) {
            log.info(" Music profile can only be changed once per phase.");
            return false;
        }
        MusicProfileType previous = s.currentMusicProfile;
        s.currentMusicProfile = profile;
        s.lastMusicChangePhase = phase;
        s.lastMusicProfileChangeRound = s.currentRoundIndex();
        if (s.nightOpen) s.weeklyMusicSwitches++;
        log.info(" Music profile: " + (previous != null ? previous.getLabel() : "None") + " -> " + profile.getLabel());
        audioManager.setMusicProfile(profile.name());
        return true;
    }

    public String currentMusicTooltip() {
        return musicSystem.computeEffects(s.currentMusicProfile, s.getCurrentPhase()).summary();
    }

    public String currentTimePhaseLabel() {
        return "Time: " + s.getCurrentTime() + " | Phase: " + s.getCurrentPhase();
    }

    public String upgradeRequirementText(PubUpgrade up) {
        return milestones.upgradeRequirementText(up, s.cash);
    }

    public MilestoneSystem.UpgradeAvailability getUpgradeAvailability(PubUpgrade up) {
        return milestones.getUpgradeAvailability(up, s.cash);
    }

    public String upgradeEffectPreview(PubUpgrade up) {
        return upgrades.effectSummary(up);
    }

    public String upgradeBottleneckHint() {
        if (s.unservedThisWeek >= 10) return "Hint: throughput upgrades likely pay off (serve/bar/staff cap).";
        if (s.weekRefundTotal >= 80 || s.nightRefunds >= 3) return "Hint: quality/discipline upgrades can cut refunds and incidents.";
        if (s.chaos >= 55 || s.nightFights >= 2) return "Hint: security upgrades can stabilize chaos and incident pressure.";
        if (s.cash < 200 && s.wagesAccruedThisWeek > 0) return "Hint: finance upgrades can smooth weekly bills pressure.";
        return "Hint: pick upgrades aligned to your current weak point.";
    }

    public String activityRequirementText(PubActivity activity) {
        return milestones.activityRequirementText(activity);
    }

    public MilestoneSystem.ActivityAvailability getActivityAvailability(PubActivity activity) {
        return milestones.getActivityAvailability(activity);
    }

    public void recomputeActivityAvailability() {
        milestones.recomputeActivityAvailability();
        milestones.recomputeUpgradeAvailability();
    }

    public String activityCategoryHint(PubActivity activity) {
        if (activity == null || activity.getRequiredIdentity() == null) return "";
        return "Category: " + activity.getRequiredIdentity().name().replace('_', ' ')
                + " — best with matching identity (slightly weaker if not).";
    }

    public String activityEffectPreview(PubActivity activity) {
        double trafficPct = activities.effectiveTrafficBonusPct(activity) * 100.0;
        double idPct = (activities.identityMultiplier(activity) - 1.0) * 100.0;
        String idLabel = idPct >= 0 ? "match" : "mismatch";
        return "Traffic +" + (int)Math.round(trafficPct) + "% (Identity " + idLabel + ": "
                + (idPct >= 0 ? "+" : "") + (int)Math.round(idPct) + "%)";
    }

    public void toggleHappyHour(boolean on) {
        if (!s.nightOpen && on) { log.neg(" Happy Hour can only be toggled while the pub is OPEN."); return; }
        s.happyHour = on;
        log.action(on ? " Happy Hour ON - prices halved, traffic may spike." : " Happy Hour OFF");
    }

    public void startActivity(PubActivity a) {
        if (s.nightOpen) { log.neg("Activities can only be scheduled between nights."); return; }
        recomputeActivityAvailability();
        MilestoneSystem.ActivityAvailability availability = milestones.getActivityAvailability(a);
        if (!availability.unlocked()) {
            log.neg("That activity is locked: " + String.join(", ", availability.missingRequirements()) + ".");
            return;
        }
        if (s.scheduledActivity != null) { log.info("Activity already scheduled."); return; }
        if (s.activityTonight != null) { log.info("Activity already running tonight."); return; }

        if (!eco.tryPay(a.getCost(), TransactionType.ACTIVITY, "Activity: " + a.getLabel(), CostTag.ACTIVITY)) return;

        int delay = 1 + s.random.nextInt(3);
        int startIndex = s.absDayIndex() + delay;
        s.scheduledActivity = new ScheduledActivity(a, startIndex);
        log.pos(" Activity booked: " + a.getLabel() + " (starts in " + delay + " day(s)).");
        s.nightRoundCostsTotal += a.getCost();
        s.activityTonight = a;
        if (a.getRequiredIdentity() != null) {
            s.recordActivitySignal(a.getRequiredIdentity(), a.getIdentitySignal());
        }

        eco.applyRep(a.getRepInstantDelta(), "Activity kick-off: " + a.getLabel());
        log.pos(" Activity started: " + a);
    }

    public void buyFromSupplier(Wine w) { buyFromSupplier(w, 1); }

    public void buyFromSupplier(Wine w, int qty) {
        if (s.rack.count() >= s.rack.getCapacity()) { log.neg("Inventory full."); return; }

        qty = Math.max(1, qty);
        int space = s.rack.getCapacity() - s.rack.count();
        if (qty > space) qty = space;
        if (qty <= 0) { log.neg("Inventory full."); return; }

        double repMult = inv.repToSupplierCostMultiplier();
        double cost = supplierSystem.supplierBuyCost(w, repMult, qty);
        double disc = supplierSystem.bulkDiscountPct(qty);

        if (s.nightOpen) {
            if (!s.canEmergencyRestock()) {
                log.neg("Emergency restock requires a General Manager and Assistant Manager on staff.");
                return;
            }
            boolean weekend = s.isWeekend();
            double markup = weekend ? 1.7 : 1.3;
            int roundsDelay = 3;
            double baseCost = w.getBaseCost() * qty * s.supplierPriceMultiplier();
            double markedCost = baseCost * markup;
            if (!applySupplierTradeCredit(s.supplierWineCredit, markedCost, "Emergency restock " + qty + "x " + w.getName(), CostTag.SUPPLIER)) {
                return;
            }
            s.pendingSupplierDeliveries.add(new PendingSupplierDelivery(w, qty, s.roundInNight + roundsDelay, markedCost));
            log.popup(" Emergency supplier", qty + "x " + w.getName() + " ordered.", "Delivery in " + roundsDelay + " rounds | Markup x" + String.format("%.1f", markup));
            return;
        }

        if (!applySupplierTradeCredit(s.supplierWineCredit, cost, "Restock " + qty + "x " + w.getName() + " (rep x" + String.format("%.2f", repMult) + ")", CostTag.SUPPLIER)) {
            return;
        }

        int added = s.rack.addBottles(w, qty, s.absDayIndex());
        if (added <= 0) { log.neg("Inventory full."); return; }

        String bulkTag = (disc > 0) ? (" | bulk -" + (int)(disc * 100) + "%") : "";
        log.pos(" Bought " + added + "x " + w.getName()
                + " for GBP " + String.format("%.2f", cost)
                + bulkTag
                + (s.supplierDeal != null && s.supplierDeal.appliesTo(w) ? " (DEAL applied)" : ""));
        milestones.onSupplierOrder(added);
    }

    private boolean applySupplierTradeCredit(SupplierTradeCredit account, double amount, String label, CostTag tag) {
        if (amount <= 0.0) return true;
        double cap = s.supplierCreditCap();
        if (account.getBalance() + amount > cap) {
            log.neg("Supplier credit cap reached. Need GBP " + String.format("%.2f", amount)
                    + " but only GBP " + String.format("%.2f", (cap - account.getBalance())) + " available.");
            return false;
        }
        account.addBalance(amount);
        eco.recordCostOnly(amount, tag, label);
        return true;
    }

    public double peekFoodCost(Food food, int qty) {
        if (food == null) return 0.0;
        qty = Math.max(1, qty);
        double cost = food.getBaseCost() * qty * s.supplierPriceMultiplier();
        double disc = foodBulkDiscountPct(qty);
        return Math.max(0.0, cost * (1.0 - disc));
    }

    public void buyFoodFromSupplier(Food food, int qty) {
        if (!s.kitchenUnlocked) { log.neg("Kitchen not unlocked."); return; }
        if (s.foodRack.count() >= s.foodRack.getCapacity()) { log.neg("Kitchen inventory full."); return; }

        qty = Math.max(1, qty);
        int space = s.foodRack.getCapacity() - s.foodRack.count();
        if (qty > space) qty = space;
        if (qty <= 0) { log.neg("Kitchen inventory full."); return; }

        double disc = foodBulkDiscountPct(qty);
        double cost = food.getBaseCost() * qty * (1.0 - disc) * s.supplierPriceMultiplier();

        if (s.nightOpen) {
            if (s.staffCountOfType(Staff.Type.HEAD_CHEF) < 1) {
                log.neg("Emergency food order requires a Head Chef on staff.");
                return;
            }
            boolean weekend = s.isWeekend();
            double markup = weekend ? 1.7 : 1.4;
            int roundsDelay = weekend ? 4 : 3;
            double markedCost = cost * markup;
            if (!applySupplierTradeCredit(s.supplierFoodCredit, markedCost, "Emergency food restock " + qty + "x " + food.getName(), CostTag.FOOD)) {
                return;
            }

            s.pendingFoodDeliveries.add(new PendingFoodDelivery(food, qty, s.roundInNight + roundsDelay, markedCost));
            log.popup(" Emergency food supplier", qty + "x " + food.getName() + " ordered.", "Delivery in " + roundsDelay + " rounds | Markup x" + String.format("%.1f", markup));
            return;
        }

        if (!applySupplierTradeCredit(s.supplierFoodCredit, cost, "Restock " + qty + "x " + food.getName(), CostTag.FOOD)) {
            return;
        }

        int added = s.foodRack.addMeals(food, qty, s.absDayIndex());
        if (added <= 0) { log.neg("Kitchen inventory full."); return; }

        String bulkTag = (disc > 0) ? (" | bulk -" + (int)(disc * 100) + "%") : "";
        log.pos(" Bought " + added + "x " + food.getName()
                + " for GBP " + String.format("%.2f", cost) + bulkTag);
        milestones.onSupplierOrder(added);
    }

    private double foodBulkDiscountPct(int qty) {
        if (qty >= 25) return 0.08;
        if (qty >= 10) return 0.04;
        if (qty >= 5) return 0.02;
        return 0.0;
    }

    public void buyUpgrade(PubUpgrade up) {
        if (s.nightOpen) { log.neg("Upgrades can only be bought between nights."); return; }
        if (s.ownedUpgrades.contains(up)) { log.info("Already owned."); return; }
        if (isUpgradeInstalling(up)) { log.info("Upgrade already installing."); return; }
        MilestoneSystem.UpgradeAvailability availability = milestones.getUpgradeAvailability(up, s.cash);
        if (!availability.unlocked()) {
            log.neg("Upgrade locked: " + String.join(", ", availability.missingRequirements()));
            return;
        }

        double cost = up.getCost();
        if (s.cash < cost) {
            double shortfall = cost - s.cash;
            if (!s.creditLines.hasAvailableCredit(shortfall)) {
                log.neg("Upgrade purchase failed: credit limit exceeded for GBP " + String.format("%.2f", shortfall) + ".");
                return;
            }
        }

        if (!eco.tryPay(cost, TransactionType.UPGRADE, "Upgrade: " + up.getLabel(), CostTag.UPGRADE)) {
            log.neg("Upgrade purchase failed: unable to fund purchase from cash/credit.");
            return;
        }

        int nights = 1 + s.random.nextInt(4);
        s.pendingUpgradeInstalls.add(new PendingUpgradeInstall(up, nights, nights));
        milestones.recomputeUpgradeAvailability();
        log.upgrade(" Upgrade ordered: ", up.getLabel(), " (ETA " + nights + " night(s)).", UILogger.Tone.POS);
        eco.applyRep(+2, "Upgrade hype");

        if (s.ownedUpgrades.size() == 3) log.event(" Milestone: 3 upgrades owned - your pub is becoming a 'place'.");
        if (s.ownedUpgrades.size() == 6) log.event(" Milestone: 6 upgrades - locals start calling it 'their' pub. Dangerous.");
        recomputeActivityAvailability();
    }

    private boolean isUpgradeInstalling(PubUpgrade up) {
        for (PendingUpgradeInstall install : s.pendingUpgradeInstalls) {
            if (install.upgrade() == up) return true;
        }
        return false;
    }

    public void hireStaff(Staff.Type t) {
        if (s.nightOpen) { log.neg("Hire staff between nights."); return; }
        Staff.Type requestedType = t;
        t = applyDebtSpiralHiringShift(t);
        if (t != requestedType) {
            log.neg("Debt spiral limits the hiring pool this week. " + requestedType.name().replace("_", " ")
                    + " downgraded to " + t.name().replace("_", " ") + ".");
        }

        if (t == Staff.Type.MANAGER) {
            if (s.managerPoolCount() >= s.managerCap) {
                log.info("Manager cap reached (" + s.managerCap + ").");
                return;
            }
            Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random, s.weekCount, s.reputation);
            s.generalManagers.add(hire);
            staff.updateTeamMorale();
            log.pos(" Hired " + t.name().replace("_", " ") + ": " + hire);
            return;
        }

        if (t == Staff.Type.DUTY_MANAGER) {
            if (!s.innUnlocked || s.innTier < 2) {
                log.neg("Duty Manager unlocks at Inn Tier 2.");
                return;
            }
            if (s.managerPoolCount() >= s.managerCap) {
                log.info("Manager cap reached (" + s.managerCap + ").");
                return;
            }
            if (s.fohStaffCount() >= s.fohStaffCap) {
                log.neg("FOH staff cap reached (" + s.fohStaffCap + ").");
                return;
            }
            Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random, s.weekCount, s.reputation);
            s.fohStaff.add(hire);
            staff.updateTeamMorale();
            log.pos(" Hired " + t.name().replace("_", " ") + ": " + hire);
            return;
        }

        if (t == Staff.Type.CHEF || t == Staff.Type.HEAD_CHEF || t == Staff.Type.SOUS_CHEF
                || t == Staff.Type.CHEF_DE_PARTIE || t == Staff.Type.KITCHEN_ASSISTANT
                || t == Staff.Type.KITCHEN_PORTER) {
            if (!s.kitchenUnlocked) {
                log.neg("Kitchen not unlocked.");
                return;
            }
            if (t == Staff.Type.HEAD_CHEF && s.staffCountOfType(Staff.Type.HEAD_CHEF) >= 1) {
                log.neg("Only one Head Chef can be employed at a time.");
                return;
            }
            int chefs = s.kitchenStaffCount();
            if (chefs >= s.kitchenChefCap) {
                log.neg("Kitchen staff cap reached (" + s.kitchenChefCap + ").");
                return;
            }
            Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random, s.weekCount, s.reputation);
            s.bohStaff.add(hire);
            staff.updateTeamMorale();
            updateKitchenInventoryCap();
            log.pos(" Hired: " + hire);
            return;
        } else {
            if ((t == Staff.Type.RECEPTION_TRAINEE
                    || t == Staff.Type.RECEPTIONIST
                    || t == Staff.Type.SENIOR_RECEPTIONIST
                    || t == Staff.Type.HOUSEKEEPING_TRAINEE
                    || t == Staff.Type.HOUSEKEEPER
                    || t == Staff.Type.HEAD_HOUSEKEEPER)
                    && !s.innUnlocked) {
                log.neg("Inn not unlocked.");
                return;
            }
            if (t == Staff.Type.ASSISTANT_MANAGER && s.managerPoolCount() >= s.managerCap) {
                log.info("Manager cap reached (" + s.managerCap + ").");
                return;
            }
            if (s.isHohRole(t) && s.hohStaffCount() >= s.hohStaffCap) {
                log.neg("HOH staff cap reached (" + s.hohStaffCap + ").");
                return;
            }
            if (t != Staff.Type.ASSISTANT_MANAGER && !s.isHohRole(t) && s.fohStaffCount() >= s.fohStaffCap) {
                log.neg("FOH staff cap reached (" + s.fohStaffCap + ").");
                return;
            }
        }
        Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random, s.weekCount, s.reputation);
        s.fohStaff.add(hire);
        staff.updateTeamMorale();
        updateKitchenInventoryCap();
        log.pos(" Hired: " + hire);
    }

    private Staff.Type applyDebtSpiralHiringShift(Staff.Type requested) {
        if (requested == null) return Staff.Type.TRAINEE;
        int tier = s.debtSpiralTier;
        if (tier <= 0) return requested;
        double roll = s.random.nextDouble();
        double degradeChance = tier == 1 ? 0.12 : tier == 2 ? 0.24 : tier == 3 ? 0.38 : 0.55;
        if (s.bankruptcyDeclared) degradeChance = Math.max(degradeChance, 0.70);
        if (roll >= degradeChance) return requested;
        return switch (requested) {
            case MANAGER -> Staff.Type.ASSISTANT_MANAGER;
            case ASSISTANT_MANAGER, DUTY_MANAGER -> Staff.Type.EXPERIENCED;
            case HEAD_CHEF -> Staff.Type.SOUS_CHEF;
            case SOUS_CHEF, CHEF_DE_PARTIE -> Staff.Type.KITCHEN_ASSISTANT;
            case SENIOR_RECEPTIONIST, HEAD_HOUSEKEEPER -> Staff.Type.RECEPTIONIST;
            case RECEPTIONIST, HOUSEKEEPER -> Staff.Type.RECEPTION_TRAINEE;
            case SPEED, CHARISMA, SECURITY, EXPERIENCED -> Staff.Type.TRAINEE;
            case CHEF -> Staff.Type.KITCHEN_PORTER;
            default -> requested;
        };
    }


    public void fireStaffAt(int index) {
        if (s.nightOpen) { log.neg("Fire staff between nights."); return; }
        if (index < 0 || index >= s.fohStaff.size()) return;

        Staff st = s.fohStaff.get(index);
        double due = st.getAccruedThisWeek();
        if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (firing " + st.getType() + ")", CostTag.WAGES)) {
            return;
        }
        st.cashOutAccrued();
        s.fohStaff.remove(index);
        s.staffDeparturesThisWeek++;
        staff.updateTeamMorale();
        updateKitchenInventoryCap();

        log.event(" Fired staff. Paid accrued wages. Staff removed.");
        eco.applyRep(-1, "Firing (staff gossip)");
    }

    public void fireBohStaffAt(int index) {
        if (s.nightOpen) { log.neg("Fire staff between nights."); return; }
        if (index < 0 || index >= s.bohStaff.size()) return;

        Staff st = s.bohStaff.get(index);
        double due = st.getAccruedThisWeek();
        if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (firing " + st.getType() + ")", CostTag.WAGES)) {
            return;
        }
        st.cashOutAccrued();
        s.bohStaff.remove(index);
        s.staffDeparturesThisWeek++;
        staff.updateTeamMorale();
        updateKitchenInventoryCap();

        log.event(" Fired kitchen staff. Paid accrued wages.");
        eco.applyRep(-1, "Firing (kitchen gossip)");
    }

    public void fireManagerAt(int index) {
        if (s.nightOpen) { log.neg("Fire manager between nights."); return; }
        if (index < 0 || index >= s.generalManagers.size()) return;

        Staff mgr = s.generalManagers.get(index);
        double due = mgr.getAccruedThisWeek();
        if (due > 0 && !eco.tryPay(due, TransactionType.WAGES, "Wages payout (firing manager)", CostTag.WAGES)) {
            return;
        }
        mgr.cashOutAccrued();
        s.generalManagers.remove(index);
        s.staffDeparturesThisWeek++;
        staff.updateTeamMorale();
        updateKitchenInventoryCap();

        log.event(" Fired manager. Paid accrued wages.");
        eco.applyRep(-2, "Manager fired (panic)");
    }

    public void hireBouncerTonight() { security.hireBouncerTonight(); }
    public void upgradeSecurity() { security.upgradeBaseSecurity(); }
    public double peekSecurityUpgradeCost() { return security.nextUpgradeCost(); }

    public boolean canBuyUpgrade(PubUpgrade up) { return milestones.getUpgradeAvailability(up, s.cash).unlocked(); }
    public boolean isActivityUnlocked(PubActivity a) { return milestones.isActivityUnlocked(a); }

    // --------------------
    // Credit lines + loan shark
    // --------------------
    public void openCreditLine(Bank bank) {
        if (bank == null) return;
        if (s.bankruptcyLockWeeksRemaining > 0) {
            log.neg("Bankruptcy lock active: banks refuse credit for " + s.bankruptcyLockWeeksRemaining + " more week(s).");
            return;
        }
        if (s.banksLocked) {
            log.neg("Banks refuse new credit lines while your business is unstable.");
            return;
        }
        if (!bank.isUnlocked(s.creditScore)) {
            log.neg("Credit score too low for " + bank.getName() + " (requires " + bank.getMinScore() + ").");
            return;
        }
        if (s.creditLines.hasLine(bank.getName())) {
            log.info("Credit line already open with " + bank.getName() + ".");
            return;
        }
        CreditLine line = s.creditLines.openLine(bank, s.random);
        if (line == null) {
            log.neg("Could not open credit line with " + bank.getName() + ".");
            return;
        }
        log.pos("Opened credit line: " + bank.getName()
                + " | limit GBP " + String.format("%.0f", line.getLimit())
                + " | APR " + String.format("%.2f", line.getInterestAPR() * 100) + "%");
        s.creditLinesOpenedThisWeek++;
        if (s.creditLinesOpenedThisWeek > 1) {
            s.creditScore = s.clampCreditScore(s.creditScore - 5);
            log.neg("Credit score dips from opening multiple lines quickly.");
        }
    }

    public void openSharkLine() {
        if (s.loanShark.isOpen()) {
            log.info("Loan shark already engaged.");
            return;
        }
        double amount = 2000 + s.random.nextInt(4001);
        double apr = 0.18 + (s.random.nextDouble() * 0.17);
        if (s.bankruptcyDeclared) {
            apr += BANKRUPTCY_SHARK_APR_BONUS;
        }
        s.loanShark.openLoan(amount, apr);
        if (s.bankruptcyDeclared) {
            s.loanShark.setPenaltyAddOnApr(s.loanShark.getPenaltyAddOnApr() + BANKRUPTCY_SHARK_PENALTY_BONUS);
            log.neg("Bankruptcy stigma: shark terms are harsher and misses are punished harder.");
        }
        s.cash += amount;
        s.creditScore = s.clampCreditScore(s.creditScore - 50);
        log.neg("Loan shark money taken. Credit score takes a hit.");
        log.pos("Loan shark loan received | GBP " + String.format("%.0f", amount)
                + " | APR " + String.format("%.2f", apr * 100) + "%");
    }

    public void repayCreditLineInFull(String lineId) {
        CreditLine line = s.creditLines.getLineById(lineId);
        if (line == null) { log.neg("Credit line not found."); return; }
        s.creditLines.repayInFull(s, line, log);
    }

    // ---------- Night loop ----------
    public void openNight() {
        if (s.nightOpen) { log.info("Pub already open."); return; }

        // upgrades can change caps / security etc
        applyPersistentUpgrades();

        s.nightOpen = true;
        s.roundInNight = 0;
        s.nightCount++;
        s.lastMusicChangePhase = null;
        s.teamFatigue = Math.max(0.0, s.teamFatigue * 0.15);
        s.sickCallTriggeredTonight = false;
        s.sickStaffNameTonight = "";
        s.sickStaffTonight.clear();

        // nightly reset
        s.activityTonight = null;
        s.happyHour = false; //  reset nightly

        s.nightSales = 0;
        s.nightRevenue = 0;
        s.nightEvents = 0;
        s.nightUnserved = 0;
        s.nightKickedOut = 0;
        s.nightNaturalDepartures = 0;
        s.lastNaturalDepartures = 0;
        s.nightRefusedUnderage = 0;
        s.roundsSinceLastEvent = 0;
        s.nightRoundCostsTotal = 0;
        s.foodSpoiledLastNight = 0;
        s.nightRefunds = 0;
        s.nightFights = 0;
        s.nightItemSales.clear();
        s.pendingFoodOrders.clear();
        s.happyHourBacklashShown = false;
        s.overpricingRobberyPopupShown = false;
        s.staffIncidentThisNight = false;
        s.staffIncidentThisRound = false;
        s.lastRumorDrivers = "None";
        s.lastRumorHeadline = "None";
        s.posStreak = 0;
        s.negStreak = 0;
        s.lastRoundClassification = "None";
        s.lastChaosDelta = 0.0;
        s.lastChaosDeltaBase = 0.0;
        s.lastChaosDeltaStreak = 0.0;
        s.lastChaosMoraleNegMult = 1.0;
        s.lastChaosMoralePosMult = 1.0;
        s.lastNightChaosPeak = 0.0;

        staff.updateTeamMorale();
        maybeTriggerSickCall();

        // bouncer reset
        s.bouncersHiredTonight = 0;
        s.bouncerTheftReduction = 0;
        s.bouncerNegReduction = 0;
        s.bouncerFightReduction = 0;
        s.bouncerNightPay = 0;
        s.bouncerQualitiesTonight.clear();
        s.roundItemSales.clear();
        s.recentRoundSales.clear();
        s.topSalesForecastLine = "Top sellers (5r): Wine None | Food None";

        s.tempServeBonusTonight = s.nextNightServeCapBonus;
        s.nextNightServeCapBonus = 0;

        s.nightStartCash = s.cash;
        s.currentNightInnBookings.clear();
        s.innPriceSegments.clear();
        s.innPriceChangesThisNight = 0;
        if (s.innUnlocked) {
            s.innPriceSegments.add(new GameState.InnPriceSegment(1, s.closingRound, s.roomPrice));
        }

        if (s.scheduledActivity != null && s.absDayIndex() >= s.scheduledActivity.startAbsDayIndex()) {
            s.activityTonight = s.scheduledActivity.activity();
            s.scheduledActivity = null;
            s.weekActivityNights++;
            milestones.onActivityScheduled(s.activityTonight);
            eco.applyRep(s.activityTonight.getRepInstantDelta(), "Activity night: " + s.activityTonight.getLabel());
            log.pos(" Tonight's activity is live: " + s.activityTonight.getLabel());
            if (s.activityTonight.getTrafficBonusPct() >= 0.10) {
                addRumorHeat(Rumor.LIVE_MUSIC_SCENE, 6, RumorSource.EVENT);
            }
            if (s.activityTonight.getRiskBonusPct() >= 0.08) {
                addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 4, RumorSource.EVENT);
            }
        }

        // base population & bar cap
        int basePool = 5;
        int pool = clamp((int)Math.round(basePool * baseTrafficMultiplier() * timeOfDayTrafficMultiplier(s.getCurrentPhase(), s.getCurrentTime())), 5, 28);

        //  upgrades actually expand the bar cap
        s.maxBarOccupancy = pool + s.upgradeBarCapBonus + s.pubLevelBarCapBonus;
        if (s.maxBarOccupancy < 5) s.maxBarOccupancy = 5;

        punters.seedNightPunters(pool);
        if (FeatureFlags.FEATURE_VIPS) {
            vipSystem.ensureRosterFromNames(currentPunterNames(), s.random);
        }
        audioManager.setPubOpen(true);
        audioManager.updateChatterOccupancy(s.nightPunters.size(), s.maxBarOccupancy);

        log.header(" " + s.pubName + " OPEN - " + s.dayName() + " | Week " + s.weekCount);
        log.info("Punters in bar: " + s.nightPunters.size() + "/" + s.maxBarOccupancy);
        log.info("Inventory: " + s.rack.count() + "/" + s.rack.getCapacity());
        log.popup("Supplier deal", "Locked for this night: " + supplierSystem.dealLabel(), "");
        if (s.tempServeBonusTonight > 0) {
            log.popup(" Milestone perk", "+" + s.tempServeBonusTonight + " serve capacity tonight.", "");
        }
    }

    public void setRoomPrice(double price) {
        double nextPrice = Math.max(0.0, price);
        if (Math.abs(s.roomPrice - nextPrice) < 0.0001) return;
        double previousPrice = s.roomPrice;
        s.roomPrice = nextPrice;
        if (!s.nightOpen || !s.innUnlocked) return;

        if (s.innPriceSegments.isEmpty()) {
            s.innPriceSegments.add(new GameState.InnPriceSegment(1, s.closingRound, previousPrice));
        }

        if (s.roundInNight == 0) {
            s.innPriceSegments.set(0, new GameState.InnPriceSegment(1, s.closingRound, nextPrice));
        } else {
            int endRound = Math.min(s.roundInNight, s.closingRound);
            int lastIndex = s.innPriceSegments.size() - 1;
            GameState.InnPriceSegment last = s.innPriceSegments.get(lastIndex);
            int adjustedEnd = Math.max(last.startRound(), endRound);
            s.innPriceSegments.set(lastIndex, new GameState.InnPriceSegment(last.startRound(), adjustedEnd, last.rateApplied()));
            int startRound = Math.min(adjustedEnd + 1, s.closingRound);
            if (startRound <= s.closingRound) {
                s.innPriceSegments.add(new GameState.InnPriceSegment(startRound, s.closingRound, nextPrice));
            }
        }

        s.innPriceChangesThisNight++;
        if (s.innPriceChangesThisNight > INN_PRICE_VOLATILITY_THRESHOLD) {
            applyInnPriceVolatilityPenalty();
        }
    }

    public void setSecurityPolicy(SecurityPolicy policy) {
        if (policy == null) return;
        if (s.securityPolicy == policy) return;
        s.securityPolicy = policy;
        log.info("Security policy set: " + policy.getLabel() + ".");
        s.addSecurityLog("Policy set: " + policy.getLabel());
    }

    public void hireMarshall() {
        if (!s.isMarshallUnlocked()) {
            log.neg("Marshalls unlock via the Marshalls upgrade.");
            return;
        }
        if (s.marshallCount() >= s.marshallCap) {
            log.info("Marshall cap reached (" + s.marshallCap + ").");
            return;
        }
        if (s.nightOpen) {
            log.neg("Marshalls can only be hired between nights.");
            return;
        }
        BouncerQuality quality = rollMarshallQuality();
        s.marshalls.add(quality);
        log.pos(" Hired Marshall (" + quality.name().toLowerCase() + ").");
    }

    private BouncerQuality rollMarshallQuality() {
        int roll = s.random.nextInt(100);
        if (roll < 15) return BouncerQuality.LOW;
        if (roll < 60) return BouncerQuality.MEDIUM;
        return BouncerQuality.HIGH;
    }

    int earlyClosePenaltyForRemaining(int roundsRemaining) {
        return -2 * Math.max(0, roundsRemaining);
    }

    void applyChaosClassificationForTest(boolean badRound) {
        if (badRound) {
            updateChaosFromRound(BAD_UNSERVED_MIN, 1, 0, 0, 0);
        } else {
            updateChaosFromRound(0, 0, 0, 0, 0);
        }
    }

    public SecuritySystem.SecurityBreakdown securityBreakdown() {
        return security.breakdown();
    }

    public int securityTaskTier() {
        int base = s.baseSecurityLevel;
        if (base >= 30) return 3;
        if (base >= 15) return 2;
        if (base >= 5) return 1;
        return 0;
    }

    private int tierRequirement(int tier) {
        return switch (tier) {
            case 1 -> 5;
            case 2 -> 15;
            case 3 -> 30;
            default -> 0;
        };
    }

    public List<SecurityTask> getAvailableSecurityTasks() {
        return SecurityTask.tasksUpToTier(securityTaskTier());
    }

    public SecurityTaskAvailability securityTaskAvailability(SecurityTask task) {
        if (task == null) return new SecurityTaskAvailability(false, "Unknown task.");
        if (!s.nightOpen) return new SecurityTaskAvailability(false, "Night must be open.");
        if (securityTaskTier() < task.getTier()) {
            return new SecurityTaskAvailability(false, "Locked: Base Security " + tierRequirement(task.getTier()));
        }
        if (s.currentRoundIndex() == s.lastSecurityTaskRound) {
            return new SecurityTaskAvailability(false, "Only 1 task per round.");
        }
        int cooldown = s.securityTaskCooldownRemaining(task);
        if (cooldown > 0) {
            return new SecurityTaskAvailability(false, "Cooldown: " + cooldown + "r");
        }
        return new SecurityTaskAvailability(true, "");
    }

    public SecurityTaskResolution resolveSecurityTask(SecurityTask task) {
        SecurityTaskAvailability availability = securityTaskAvailability(task);
        if (!availability.canUse()) {
            log.neg("Security task unavailable: " + availability.reason());
            return SecurityTaskResolution.blocked(task, availability.reason());
        }

        s.activeSecurityTask = task;
        s.activeSecurityTaskRound = s.currentRoundIndex() + 1;
        s.securityTaskCooldowns.put(task, task.getCooldownRounds());
        s.lastSecurityTaskRound = s.currentRoundIndex();

        String message = task.getLabel() + " queued for next round.";
        log.info(" Security task: " + message);
        s.addSecurityLog("Task queued: " + task.getLabel());
        return SecurityTaskResolution.applied(task, message);
    }

    public double securityPolicyTrafficMultiplier() {
        return s.securityPolicy != null ? s.securityPolicy.getTrafficMultiplier() : 1.0;
    }

    public double securityPolicyIncidentChanceMultiplier() {
        return s.securityPolicy != null ? s.securityPolicy.getIncidentChanceMultiplier() : 1.0;
    }

    public double securityTaskTrafficMultiplier() {
        return s.securityTaskTrafficMultiplier();
    }

    public double securityTaskIncidentChanceMultiplier() {
        return s.securityTaskIncidentChanceMultiplier();
    }

    public void playRound() {
        if (!s.nightOpen) return;

        s.roundInNight++;
        tickLandlordActionCooldowns();
        tickSecurityTaskCooldowns();
        int repBefore = s.reputation;
        int fightsBefore = s.nightFights;
        int refundsBefore = s.nightRefunds;
        s.happyHourCheatRepHitThisRound = false;
        s.foodDisappointmentThisRound = 0;
        s.foodDisappointmentPopupShown = false;
        s.staffIncidentThisRound = false;
        log.header("- Round " + s.roundInNight + "/" + s.closingRound + " -");
        s.roundItemSales.clear();

        processSupplierDeliveries();
        processFoodOrders();
        for (Punter p : s.nightPunters) {
            p.tickFoodCooldown();
        }

        // 1b) Operating costs per round (tiny now, matters later)
        double opCost = staff.roundOperatingCost(s.nightPunters.size());
        if (!eco.tryPay(opCost, TransactionType.OTHER, "Operating costs (this round)", CostTag.OPERATING)) return;
        s.nightRoundCostsTotal += opCost;

        // 2) Reputation drift
        int repDrift = upgrades.repDriftPerRound();
        int repStaff = staff.repDeltaThisRound(s.random);
        eco.applyRep(repDrift + repStaff, "Atmosphere (upgrades+staff)");

        RoundModifiers modifiers = computeModifiersForCurrentRound();
        MusicEffects roundMusic = musicSystem.computeEffects(s.currentMusicProfile, s.getCurrentPhase());
        if (Math.abs(roundMusic.reputationDriftDelta()) > 0.001) {
            int musicRep = (int)Math.round(roundMusic.reputationDriftDelta());
            if (musicRep != 0) {
                eco.applyRep(musicRep, "Music profile response");
            }
        }
        applyMusicIdentityPressure(roundMusic.identityPressure());

        // 3) Effective price multiplier
        //  Happy Hour halves prices (true "sell-off" button)
        double activityPriceAdj = 1.0 + activities.priceMultiplierPct();
        double effectiveMult = s.priceMultiplier * (s.happyHour ? 0.50 : 1.0) * activityPriceAdj * modifiers.spendMultiplier();
        effectiveMult = Math.max(0.50, Math.min(2.50, effectiveMult));
        s.recordWeeklyPriceMultiplier(effectiveMult);

        // 4) Capacity this round
        int serveCap = staff.totalServeCapacity();
        StaffSystem.WorkloadProfile workloadProfile = null;

        double trafficMult = modifiers.trafficMultiplier();
        if (s.wageTrafficPenaltyRounds > 0 && s.wageTrafficPenaltyMultiplier < 1.0) {
            trafficMult *= s.wageTrafficPenaltyMultiplier;
            s.wageTrafficPenaltyRounds = Math.max(0, s.wageTrafficPenaltyRounds - 1);
            if (s.wageTrafficPenaltyRounds == 0) {
                s.wageTrafficPenaltyMultiplier = 1.0;
            }
        }


        //  Happy Hour: chance to spike traffic, but you're selling cheap
        if (s.happyHour) {
            int chance = 35; // base
            if (s.dayIndex == 4 || s.dayIndex == 5) chance += 10; // Fri/Sat louder
            if (s.random.nextInt(100) < chance) {
                trafficMult *= 1.15;
                log.event(" Happy Hour pop-off: foot traffic surges!");
            } else {
                log.info(" Happy Hour: steady flow (no spike).");
            }
        }

        boolean riskyWeekend = s.isWeekend();
        int sec = security.effectiveSecurity(); //  upgrade security applies
        sec = Math.max(0, sec);
        if (modifiers.lateChaosRisk()) {
            log.info(" Late phase risk: current music profile is stirring chaos.");
        }

        double identityTip = s.currentIdentity != null ? s.currentIdentity.getTipBonusPct() : 0.0;
        double tipRate = staff.tipRate() + s.upgradeTipBonusPct + activities.tipBonusPct() + identityTip;

        // 5b) Arrivals sometimes happen mid-night (respect bar cap)
        double expectedArrivals = expectedArrivals(trafficMult, riskyWeekend);
        int forecastMin = (int) Math.floor(expectedArrivals * 0.85);
        int forecastMax = (int) Math.ceil(expectedArrivals * 1.15);
        forecastMin = Math.max(0, forecastMin);
        forecastMax = Math.max(forecastMin, forecastMax);
        s.trafficForecastLine = "Forecast: " + forecastMin + "–" + forecastMax + " tonight";

        int arrivals = rollArrivals(trafficMult, riskyWeekend);
        int added = arrivals > 0 ? punters.addArrivals(arrivals) : 0;
        int leftNaturally = punters.applyNaturalDepartures();
        if (leftNaturally > 0) {
            log.info(" Some punters headed off — nothing wrong, just time to go. (" + leftNaturally + ")");
        }
        if (added > 0) {
            log.info(" " + added + " punter(s) wandered in.");
        }
        s.lastNaturalDepartures = leftNaturally;
        s.nightNaturalDepartures += leftNaturally;


        // 6) Events
        int eventsBefore = s.nightEvents;
        events.setChaosFactor(s.chaos / 100.0);
        events.maybeEventGuaranteed(upgrades.eventBonusChance() + identityEventBonusChance(), activities.eventBonusChance());
        int eventsThisRound = Math.max(0, s.nightEvents - eventsBefore);

        // 7) Service round
        List<Punter> bar = punters.inBarShuffled();
        int barCount = bar.size();

        int demand = Math.max(1, (int)Math.round(barCount * trafficMult));
        demand = Math.min(demand, barCount);

        workloadProfile = staff.workloadProfile(demand, serveCap);
        int servedCount = Math.min(workloadProfile.effectiveCapacity(), demand);
        int unserved = Math.max(0, demand - servedCount);
        s.unservedThisWeek += unserved;
        s.lastServiceDrivers = workloadProfile.serviceDriverLine();
        s.lastStabilityDrivers = workloadProfile.stabilityDriverLine();
        s.lastRoundWorkload = workloadProfile.workload();
        s.lastRoundWorkloadPenalty = workloadProfile.penalty();

        if (demand > serveCap) log.neg(" Overwhelmed: demand " + demand + " > serve cap " + serveCap);

        for (int i = 0; i < servedCount; i++) {
            punters.handlePunter(bar.get(i), effectiveMult, sec, riskyWeekend, tipRate);
        }

        if (unserved > 0) {
            punters.handleUnserved(bar.subList(servedCount, servedCount + unserved), effectiveMult);
            s.nightUnserved += unserved;
        }

        handleFoodSales(barCount, sec);
        finalizeRoundSales();

        int removed = punters.cleanupDeparted();
        audioManager.updateChatterOccupancy(s.nightPunters.size(), s.maxBarOccupancy);
        if (removed > 0) log.info("Bar cleared: -" + removed + " (now " + s.nightPunters.size() + "/" + s.maxBarOccupancy + ")");
        s.lastTrafficIn = added;
        s.lastTrafficOut = removed;

        log.info("Round summary: bar " + barCount
                + " | demand " + demand
                + " | served " + servedCount
                + " | unserved " + unserved
                + " | staff cap " + serveCap
                + " | traffic x" + String.format("%.2f", trafficMult)
                + " | price x" + String.format("%.2f", effectiveMult)
                + " | security " + sec);
        log.info("Drivers -> " + s.lastServiceDrivers);
        log.info("Drivers -> " + s.lastStabilityDrivers);
        int fightsThisRound = Math.max(0, s.nightFights - fightsBefore);
        int refundsThisRound = Math.max(0, s.nightRefunds - refundsBefore);
        updateObservationLine(barCount, unserved, fightsThisRound, eventsThisRound, refundsThisRound, modifiers);
        punters.refreshChaosContributions();
        s.chaos = recomputeChaos(barCount, demand, serveCap, unserved, fightsThisRound, refundsThisRound, eventsThisRound)
                + staff.chaosPressureDelta(workloadProfile) + modifiers.chaosDelta();
        s.chaos = Math.max(0.0, Math.min(100.0, s.chaos));

        checkHighRepScandal();

        handleStaffMisconduct(sec, workloadProfile);
        updateChaosFromRound(unserved, eventsThisRound, fightsThisRound, refundsThisRound, s.foodDisappointmentThisRound);
        s.weekChaosTotal += s.chaos;
        s.weekChaosRounds++;

        if (fightsThisRound > 0) {
            addRumorHeat(Rumor.FIGHTS_EVERY_WEEKEND, fightsThisRound * 4, RumorSource.EVENT);
        }
        if (unserved >= 6) {
            addRumorHeat(Rumor.WATERED_DOWN_DRINKS, 3, RumorSource.PUNTER);
        }
        if (s.chaos > 60 && s.random.nextInt(100) < 20) {
            addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 4, RumorSource.PUNTER);
        }

        staff.adjustMoraleAfterRound(unserved, eventsThisRound, s.reputation, tipRate + modifiers.tipBonus(), sec, s.chaos, modifiers.staffMoraleDelta(), modifiers.fatiguePressure());

        applyFatiguePressure(unserved, eventsThisRound, fightsThisRound, serveCap);

        if (maybeTriggerTimePhaseEarlyClose()) {
            closeNight("Team exhausted. Last orders called early.");
            return;
        }

        if (s.isSecurityTaskActive()) {
            s.activeSecurityTask = null;
            s.activeSecurityTaskRound = -999;
        }

        if (s.consecutiveNeg100Rounds >= 3) {
            closeNight("Reputation collapsed. Licence revoked! (-100 for 3 rounds).");
            log.header(" GAME OVER");
            return;
        }

        if (s.isNightClosingTimeReached()) {
            closeNight("Closing time.");
        }
    }

    public void closeNight(String reason) {
        if (!s.nightOpen) return;

        //  Early close penalty (strategy pressure)
        boolean early = s.roundInNight < s.closingRound;
        boolean isNormalClose = reason != null && reason.toLowerCase().contains("closing time");
        boolean isGameOver = reason != null && reason.toLowerCase().contains("licence") || (reason != null && reason.toLowerCase().contains("game over"));

        if (early && !isNormalClose && !isGameOver) {
            int remaining = s.closingRound - s.roundInNight;
            int repPenalty = earlyClosePenaltyForRemaining(remaining);
            s.lastEarlyCloseRoundsRemaining = Math.max(0, remaining);
            s.lastEarlyCloseRepPenalty = repPenalty;
            eco.applyRep(repPenalty, "Closed early (" + remaining + " rounds left)");
            String eventLine = "Closed early: -" + Math.abs(repPenalty) + " rep (" + remaining + " rounds remaining)";
            log.action(" " + eventLine);
            s.earlyClosePenaltyLog.addFirst(eventLine);
            while (s.earlyClosePenaltyLog.size() > 8) s.earlyClosePenaltyLog.removeLast();
        }

        s.lastNightChaosPeak = Math.max(s.lastNightChaosPeak, s.chaos);
        audioManager.setPubOpen(false);
        s.nightOpen = false;
        s.activeSecurityTask = null;
        s.activeSecurityTaskRound = -999;
        log.header(" PUB CLOSED");
        log.info(reason);

        // end of day
        s.dayIndex = (s.dayIndex + 1) % 7;
        s.dayCounter++;
        s.currentWeather = s.rollWeather(s.random);

        eco.accrueDailyRent();
        accrueSecurityUpkeep();

        // between-nights spice (v2 event system)
        events.runBetweenNightEvents(Math.max(0, security.effectiveSecurity()));

        // spoilage: bottles that sat too long go off
        int spoiled = s.rack.removeSpoiled(s.absDayIndex());
        if (spoiled > 0) {
            log.neg(" Spoilage: " + spoiled + " bottle(s) went off. Stock rotation is real.");
            eco.applyRep(-1, "Spoilage smell");
        }

        int foodSpoiled = s.foodRack.removeSpoiled(s.absDayIndex());
        s.foodSpoiledLastNight = foodSpoiled;
        if (foodSpoiled > 0) {
            log.neg(" Food spoilage: " + foodSpoiled + " meal(s) went off.");
            eco.applyRep(-1, "Food spoilage");
        }

        if (s.kitchenUnlocked && s.foodNightRepBonus > 0 && s.nightFoodUnserved == 0 && s.nightRefunds == 0) {
            eco.applyRep(s.foodNightRepBonus, "Strong food service night");
            log.pos(" Food service smooth: rep +" + s.foodNightRepBonus + ".");
        }

        generateNightRumor();

        runInnNightly();

        if (FeatureFlags.FEATURE_VIPS) {
            applyVipConsequences(vipSystem.evaluateNightWithConsequences(buildVipNightOutcome()));
        }

        if (!s.sickStaffTonight.isEmpty()) {
            for (Staff st : s.sickStaffTonight) {
                if (st.getType() == Staff.Type.HEAD_CHEF || st.getType() == Staff.Type.CHEF || st.getType() == Staff.Type.SOUS_CHEF) {
                    s.bohStaff.add(st);
                } else if (st.getType() == Staff.Type.MANAGER || st.getType() == Staff.Type.ASSISTANT_MANAGER || st.getType() == Staff.Type.DUTY_MANAGER) {
                    s.generalManagers.add(st);
                } else {
                    s.fohStaff.add(st);
                }
            }
            s.sickStaffTonight.clear();
        }

        if (s.lastNightMusicProfile == s.currentMusicProfile) {
            s.consecutiveNightsSameMusic++;
        } else {
            s.consecutiveNightsSameMusic = 1;
        }
        s.lastNightMusicProfile = s.currentMusicProfile;

        staff.accrueDailyWages();
        s.wagesAccruedThisWeek = staff.wagesDue();

        processPendingUpgradeInstallsAtNightEnd();

        double decay = (s.nightFights > 0 || s.nightUnserved > 6) ? 1.0 : 2.0;
        s.chaos = Math.max(0.0, s.chaos - decay);

        supplierSystem.rollNewDeal();
        log.popup(" Supplier deal", "New deal available: " + supplierSystem.dealLabel(), "");

        showEndOfNightReport();

        if (s.nightUnserved == 0 && s.nightFoodUnserved == 0) {
            s.noStockoutStreakNights++;
        } else {
            s.noStockoutStreakNights = 0;
        }
        boolean calmNight = s.nightFights == 0;
        if (calmNight) {
            s.calmNightsStreak++;
            if (s.activityTonight != null) s.calmNightsWithActivityStreak++;
        } else {
            s.calmNightsStreak = 0;
            s.calmNightsWithActivityStreak = 0;
        }
        if (!s.chaosRecoveryPending && s.lastNightChaosPeak >= 60.0) {
            s.chaosRecoveryPending = true;
            s.chaosRecoveryNightsRemaining = 2;
        } else if (s.chaosRecoveryPending) {
            s.chaosRecoveryNightsRemaining--;
            if (s.chaosRecoveryNightsRemaining <= 0 && s.chaos > 25.0) {
                s.chaosRecoveryPending = false;
            }
        }
        double nearCapacityTarget = s.maxBarOccupancy * s.closingRound * 0.65;
        if (s.nightSales >= nearCapacityTarget && s.nightUnserved <= 2 && s.nightRefunds <= 1) {
            s.nearCapacityServiceNightsThisWeek++;
        }

        if (s.dayIndex == 0) {
            endOfWeek();
            s.weekCount++;
            recomputeActivityAvailability();
            if (weekStartHook != null) weekStartHook.accept(s.weekCount);
        }

        milestones.onNightEnd();
        recomputeActivityAvailability();
        audioManager.onNightEnd();
    }

    void runInnNightly() {
        if (!s.innUnlocked || s.roomsTotal <= 0) {
            s.roomsBookedLast = 0;
            s.lastNightRoomsBooked = 0;
            s.lastNightRoomRevenue = 0.0;
            s.lastNightInnSummaryLine = "Inn locked.";
            s.currentNightInnBookings.clear();
            s.lastNightInnBookings.clear();
            return;
        }

        boolean hasDutyManager = s.dutyManagerCount() > 0;
        int receptionCapacity = computeReceptionCapacity(hasDutyManager);
        int housekeepingCoverage = computeHousekeepingCoverage(hasDutyManager);
        double marshallMitigation = marshallMitigationFactor();

        double demandBoost = s.innDemandBoostNextNight;
        s.innDemandBoostNextNight = 0.0;
        double demandScore = computeInnDemandScore(demandBoost);
        double noise = (s.random.nextDouble() * 2.0 - 1.0) * 1.5;
        int roomsBooked = clamp((int)Math.round(demandScore + noise), 0, s.roomsTotal);
        s.lastInnDemandNoise = noise;

        double receptionFactor = receptionCapacity >= s.roomsTotal
                ? 1.0
                : (0.60 + (0.40 * (receptionCapacity / (double)Math.max(1, s.roomsTotal))));
        roomsBooked = clamp((int)Math.round(roomsBooked * receptionFactor), 0, s.roomsTotal);

        s.roomsBookedLast = roomsBooked;
        s.lastNightRoomsBooked = roomsBooked;
        s.lastInnReceptionCapacity = receptionCapacity;
        s.lastInnHousekeepingCoverage = housekeepingCoverage;
        s.lastInnHousekeepingNeeded = roomsBooked;

        if (s.innPriceSegments.isEmpty()) {
            s.innPriceSegments.add(new GameState.InnPriceSegment(1, s.closingRound, s.roomPrice));
        }

        s.currentNightInnBookings.clear();
        int totalDuration = 0;
        for (GameState.InnPriceSegment segment : s.innPriceSegments) {
            totalDuration += Math.max(0, segment.endRound() - segment.startRound() + 1);
        }
        if (totalDuration <= 0) totalDuration = 1;

        int remaining = roomsBooked;
        double revenue = 0.0;
        for (int i = 0; i < s.innPriceSegments.size(); i++) {
            GameState.InnPriceSegment segment = s.innPriceSegments.get(i);
            int duration = Math.max(0, segment.endRound() - segment.startRound() + 1);
            int rooms = (i == s.innPriceSegments.size() - 1)
                    ? remaining
                    : (int)Math.round(roomsBooked * (duration / (double)totalDuration));
            rooms = Math.min(rooms, remaining);
            if (rooms > 0) {
                s.currentNightInnBookings.add(new GameState.InnBookingRecord(rooms, segment.rateApplied()));
                revenue += rooms * segment.rateApplied();
                remaining -= rooms;
            }
        }
        if (remaining > 0) {
            revenue += remaining * s.roomPrice;
            s.currentNightInnBookings.add(new GameState.InnBookingRecord(remaining, s.roomPrice));
        }

        if (revenue > 0.0) {
            eco.addCash(revenue, "Inn room bookings");
            s.weekInnRevenue += revenue;
        }
        if (roomsBooked > 0) {
            s.weekInnRoomsSold += roomsBooked;
        }
        s.lastNightRoomRevenue = revenue;
        s.lastNightInnBookings.clear();
        s.lastNightInnBookings.addAll(s.currentNightInnBookings);

        double cleanlinessDelta = -roomsBooked * INN_USAGE_CLEANLINESS_DECAY;
        boolean underHousekeeping = housekeepingCoverage < roomsBooked;
        if (roomsBooked > 0) {
            if (underHousekeeping) {
                cleanlinessDelta -= (roomsBooked - housekeepingCoverage) * 1.2;
            } else {
                cleanlinessDelta += INN_CLEAN_RECOVERY + Math.min(2.0, (housekeepingCoverage - roomsBooked) * 0.25);
            }
        }
        s.cleanliness = clamp01to100(s.cleanliness + cleanlinessDelta);

        double maintenanceMultiplier = underHousekeeping ? 1.15 : 1.0;
        double maintenanceAccrued = roomsBooked * INN_MAINTENANCE_PER_ROOM * maintenanceMultiplier;
        s.innMaintenanceAccruedWeekly += maintenanceAccrued;

        boolean understaffedReception = receptionCapacity < roomsBooked;
        boolean complaintRisk = underHousekeeping || understaffedReception || s.cleanliness < 50;
        double complaintChance = 0.08
                + (underHousekeeping ? 0.18 : 0.0)
                + (understaffedReception ? 0.10 : 0.0)
                + (s.cleanliness < 45 ? 0.12 : 0.0);
        if (hasDutyManager) complaintChance *= 0.7;
        if (marshallMitigation > 0.0) complaintChance *= (1.0 - marshallMitigation);

        boolean eventTriggered = false;
        s.lastInnEventsCount = 0;

        if (roomsBooked > 0 && s.random.nextDouble() < complaintChance) {
            if (underHousekeeping || s.cleanliness < 45) {
                applyInnEventWithMarshalls("Dirty room complaint", -2.0, -1, 0.0, hasDutyManager, marshallMitigation);
            } else {
                applyInnEventWithMarshalls("Slow check-in complaint", -1.5, -1, 0.0, hasDutyManager, marshallMitigation);
            }
            eventTriggered = true;
        }

        if (!eventTriggered && roomsBooked > 0 && s.cleanliness < 40 && s.random.nextDouble() < (hasDutyManager ? 0.08 : 0.12)) {
            double damageCost = 8.0 + (s.random.nextDouble() * 6.0);
            applyInnEventWithMarshalls("Room damage incident", -2.5, -1, damageCost, hasDutyManager, marshallMitigation);
            eventTriggered = true;
        }

        if (!eventTriggered && roomsBooked > 0 && s.cleanliness > 85 && !understaffedReception
                && s.random.nextDouble() < 0.15) {
            applyInnEventWithMarshalls("Spotless stay review", 2.0, 0, 0.0, hasDutyManager, marshallMitigation);
            s.innDemandBoostNextNight = Math.min(1.2, s.innDemandBoostNextNight + 0.6);
            eventTriggered = true;
        }

        if (!eventTriggered && roomsBooked > 0 && !understaffedReception && s.random.nextDouble() < 0.10) {
            applyInnEventWithMarshalls("Friendly reception shoutout", 1.2, 0, 0.0, hasDutyManager, marshallMitigation);
            eventTriggered = true;
        }

        if (!eventTriggered && roomsBooked > 0 && s.innRep > 70 && s.random.nextDouble() < 0.08) {
            applyInnEventWithMarshalls("Repeat guest booked another night", 1.0, 1, 0.0, hasDutyManager, marshallMitigation);
            eventTriggered = true;
        }

        if (!eventTriggered && roomsBooked == 0) {
            addInnEventLog("No bookings tonight. The inn stayed quiet.");
        }

        if (!eventTriggered && roomsBooked > 0 && s.cleanliness > 75 && !complaintRisk) {
            s.innRep = clamp01to100(s.innRep + 0.6);
        } else if (underHousekeeping) {
            s.innRep = clamp01to100(s.innRep - 0.5);
        }

        s.lastNightInnSummaryLine = "Rooms " + roomsBooked + "/" + s.roomsTotal
                + " | Rev " + money2(revenue)
                + " | Clean " + fmt1(s.cleanliness)
                + " | Inn rep " + fmt1(s.innRep);

        if (s.lastInnEventsCount == 0 && roomsBooked > 0) {
            s.observationLine = "Inn: " + roomsBooked + "/" + s.roomsTotal + " rooms booked.";
        }

        s.innDemandBoostNextNight = Math.max(0.0, s.innDemandBoostNextNight);
    }

    void installUpgradeForTest(PubUpgrade up) {
        installUpgradeNow(up, false);
    }

    private void installUpgradeNow(PubUpgrade up, boolean showPopup) {
        if (up == null) return;
        s.ownedUpgrades.add(up);
        applyPersistentUpgrades();
        if (up == PubUpgrade.KITCHEN_SETUP
                || up == PubUpgrade.KITCHEN
                || up == PubUpgrade.NEW_KITCHEN_PLAN
                || up == PubUpgrade.KITCHEN_EQUIPMENT) {
            s.kitchenUnlocked = true;
            if (showPopup) {
                log.event(" Kitchen unlocked. Food supplier now available.");
            }
        }
        if (up.isInnRelated()) {
            applyInnUpgradeState();
            if (showPopup) {
                log.event(" Inn upgraded to tier " + s.innTier + ".");
            }
        }
        if (showPopup) {
            log.popupUpgrade(" Upgrade installed", up.getLabel(), " is now active.", "");
        }
        recomputeActivityAvailability();
    }

    private void applyInnEvent(String headline, double innRepDelta, int pubRepDelta, double extraMaintenance, boolean hasDutyManager) {
        double repDelta = hasDutyManager ? (innRepDelta * 0.7) : innRepDelta;
        double maintenanceDelta = hasDutyManager ? (extraMaintenance * 0.7) : extraMaintenance;
        if (repDelta != 0.0) {
            s.innRep = clamp01to100(s.innRep + repDelta);
        }
        if (maintenanceDelta > 0.0) {
            s.innMaintenanceAccruedWeekly += maintenanceDelta;
            s.weekInnEventMaintenance += maintenanceDelta;
        }
        if (pubRepDelta != 0) {
            eco.applyRep(pubRepDelta, "Inn feedback");
        }
        s.weekInnEventsCount++;
        if (headline.toLowerCase().contains("complaint")) {
            s.weekInnComplaintCount++;
        }
        addInnEventLog(headline);
        s.lastInnEventsCount++;
    }

    private void applyInnEventWithMarshalls(String headline,
                                            double innRepDelta,
                                            int pubRepDelta,
                                            double extraMaintenance,
                                            boolean hasDutyManager,
                                            double marshallMitigation) {
        double mitigation = Math.max(0.0, Math.min(0.6, marshallMitigation * marshallQualityFactor()));
        double adjustedInnRep = innRepDelta < 0 ? innRepDelta * (1.0 - mitigation) : innRepDelta;
        int adjustedPubRep = pubRepDelta < 0 ? (int)Math.ceil(pubRepDelta * (1.0 - mitigation)) : pubRepDelta;
        double adjustedMaintenance = extraMaintenance > 0 ? extraMaintenance * (1.0 - mitigation) : extraMaintenance;
        applyInnEvent(headline, adjustedInnRep, adjustedPubRep, adjustedMaintenance, hasDutyManager);
    }

    private double marshallMitigationFactor() {
        if (s.marshallCount() <= 0) return 0.0;
        double base = s.marshallCount() * 0.05;
        double security = s.baseSecurityLevel * 0.002;
        double upgrades = (s.reinforcedDoorTier() + s.lightingTier() + s.burglarAlarmTier()) * 0.01
                + s.cctvRepMitigationPct();
        return Math.min(0.45, base + security + upgrades);
    }

    private double marshallQualityFactor() {
        if (s.marshalls.isEmpty()) return 0.0;
        double total = 0.0;
        for (BouncerQuality quality : s.marshalls) {
            total += switch (quality) {
                case LOW -> 0.6;
                case MEDIUM -> 0.85;
                case HIGH -> 1.0;
            };
        }
        return total / s.marshalls.size();
    }

    private void addInnEventLog(String headline) {
        if (headline == null || headline.isBlank()) return;
        s.innEventLog.addFirst(headline);
        while (s.innEventLog.size() > 8) {
            s.innEventLog.removeLast();
        }
        s.observationLine = "Inn: " + headline;
    }

    private void applyInnPriceVolatilityPenalty() {
        s.chaos += INN_PRICE_VOLATILITY_CHAOS;
        s.innRep = clamp01to100(s.innRep - INN_PRICE_VOLATILITY_INN_REP);
        log.neg(" Inn pricing volatility hurt confidence (chaos +" + fmt1(INN_PRICE_VOLATILITY_CHAOS)
                + ", inn rep -" + fmt1(INN_PRICE_VOLATILITY_INN_REP) + ").");
    }

    private double computeInnDemandScore(double demandBoost) {
        double rooms = Math.max(1, s.roomsTotal);
        double base = rooms * 0.40;
        double rep = ((s.innRep - 50.0) / 50.0) * rooms * 0.25;
        double clean = ((s.cleanliness - 50.0) / 50.0) * rooms * 0.25;
        double pubRep = (s.reputation / 100.0) * rooms * 0.18;
        double priceDelta = (s.roomPrice - INN_DEFAULT_ROOM_PRICE) / INN_DEFAULT_ROOM_PRICE;
        double price = -priceDelta * rooms * 0.30;
        double sec = Math.min(0.12, security.effectiveSecurity() * 0.01) * rooms;
        double demand = base + rep + clean + pubRep + price + sec + demandBoost;

        s.lastInnDemandBase = base;
        s.lastInnDemandRep = rep;
        s.lastInnDemandClean = clean;
        s.lastInnDemandPubRep = pubRep;
        s.lastInnDemandPrice = price;
        s.lastInnDemandSecurity = sec;
        s.lastInnDemandScore = demand;

        return clamp(demand, 0.0, rooms * 1.4);
    }

    private int computeReceptionCapacity(boolean hasDutyManager) {
        int capacity = 0;
        for (Staff st : s.fohStaff) {
            capacity += switch (st.getType()) {
                case RECEPTION_TRAINEE -> 2;
                case RECEPTIONIST -> 4;
                case SENIOR_RECEPTIONIST -> 6;
                default -> 0;
            };
        }
        if (hasDutyManager) {
            capacity = (int)Math.round(capacity * 1.1);
        }
        return capacity;
    }

    private int computeHousekeepingCoverage(boolean hasDutyManager) {
        int coverage = 0;
        for (Staff st : s.fohStaff) {
            coverage += switch (st.getType()) {
                case HOUSEKEEPING_TRAINEE -> 2;
                case HOUSEKEEPER -> 4;
                case HEAD_HOUSEKEEPER -> 6;
                default -> 0;
            };
        }
        if (hasDutyManager) {
            coverage = (int)Math.round(coverage * 1.1);
        }
        return coverage;
    }

    private double clamp01to100(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private void accrueSecurityUpkeep() {
        if (s.baseSecurityLevel <= 0) return;
        double cost = s.baseSecurityLevel * SecuritySystem.SECURITY_UPKEEP_PER_LEVEL;
        eco.accrueDailySecurityUpkeep(s.baseSecurityLevel, SecuritySystem.SECURITY_UPKEEP_PER_LEVEL);
        log.info("Security upkeep accrued: GBP " + String.format("%.2f", cost) + " for " + s.baseSecurityLevel + " levels.");
    }

    private void processPendingUpgradeInstallsAtNightEnd() {
        for (int i = s.pendingUpgradeInstalls.size() - 1; i >= 0; i--) {
            PendingUpgradeInstall install = s.pendingUpgradeInstalls.get(i);
            int remaining = install.nightsRemaining() - 1;
            if (remaining <= 0) {
                PubUpgrade up = install.upgrade();
                s.pendingUpgradeInstalls.remove(i);
                installUpgradeNow(up, true);
                if (isSecurityUpgrade(up)) {
                    s.addSecurityLog("Upgrade installed: " + up.getLabel());
                }
            } else {
                s.pendingUpgradeInstalls.set(i, new PendingUpgradeInstall(install.upgrade(), remaining, install.totalNights()));
            }
        }
    }

    private boolean isSecurityUpgrade(PubUpgrade up) {
        return switch (up) {
            case CCTV,
                    CCTV_PACKAGE,
                    REINFORCED_DOOR_I,
                    REINFORCED_DOOR_II,
                    REINFORCED_DOOR_III,
                    LIGHTING_I,
                    LIGHTING_II,
                    LIGHTING_III,
                    BURGLAR_ALARM_I,
                    BURGLAR_ALARM_II,
                    BURGLAR_ALARM_III -> true;
            default -> false;
        };
    }

    private void endOfWeek() {
        s.creditLines.applyWeeklyInterest(s, log);
        applySupplierWeeklyInterest();
        applyLoanSharkWeeklyInterest();
        s.supplierWineCredit.clearLateFees();
        s.supplierFoodCredit.clearLateFees();

        double raw = staff.wagesDueRaw();
        double eff = upgrades.wageEfficiencyPct();
        double wagesDue = staff.wagesDue();
        double tipsDue = s.tipsThisWeek * 0.50;
        if (eff > 0.0001) {
            double saved = raw - wagesDue;
            log.event(" Wage efficiency: raw GBP " + String.format("%.2f", raw)
                    + "  GBP " + String.format("%.2f", wagesDue)
                    + " (saved GBP " + String.format("%.2f", saved)
                    + ", " + (int)Math.round(eff * 100) + "%)");
        }

        staff.weeklyMoraleCheck(s.fightsThisWeek, s.random, log);
        staff.handleWeeklyLevelUps(s.random, log, s.chaos);
        applyDebtSpiralMoraleDecay();
        identitySystem.updateWeeklyIdentity();
        recomputeActivityAvailability();
        rumors.updateWeeklyRumors();
        runRivalDistrictWeek();
        endOfWeekReport();
        preparePaydayBills(wagesDue, tipsDue);
        if (s.staffDeparturesThisWeek == 0) s.weeksNoStaffDepartures++;
        else s.weeksNoStaffDepartures = 0;

        if (!s.identityHistory.isEmpty()) {
            PubIdentity latest = s.identityHistory.get(s.identityHistory.size() - 1);
            if (latest != PubIdentity.NEUTRAL && s.identityHistory.size() >= 2
                    && s.identityHistory.get(s.identityHistory.size() - 2) == latest) {
                s.weeksDominantIdentityStreak++;
            } else if (latest != PubIdentity.NEUTRAL) {
                s.weeksDominantIdentityStreak = 1;
            } else {
                s.weeksDominantIdentityStreak = 0;
            }
        }

        if (s.weekNegativeEvents > s.weekPositiveEvents) {
            s.negativeRumorRecoveryPending = true;
            s.negativeRumorRecoveryWeeksRemaining = 2;
        } else if (s.negativeRumorRecoveryPending) {
            s.negativeRumorRecoveryWeeksRemaining--;
            if (s.negativeRumorRecoveryWeeksRemaining <= 0) s.negativeRumorRecoveryPending = false;
        }

        s.usedCreditThisWeek = s.creditLinesOpenedThisWeek > 0 || s.totalCreditBalance() > 0.01;
        s.zeroDebtWeekStreak = s.totalCreditBalance() <= 0.01 ? s.zeroDebtWeekStreak + 1 : 0;
        double weekProfit = s.weekRevenue - s.weekCosts;
        double avgChaos = s.weekChaosRounds > 0 ? (s.weekChaosTotal / s.weekChaosRounds) : s.chaos;
        boolean goldenWeek = weekProfit > 0 && s.wagesPaidLastWeek && s.rentAccruedThisWeek <= 0.01
                && s.reputation >= 45 && avgChaos <= 35 && s.weekNegativeEvents <= 2;
        s.goldenQuarterWeekStreak = goldenWeek ? s.goldenQuarterWeekStreak + 1 : 0;

        milestones.onWeekEnd();
        if (s.bankruptcyLockWeeksRemaining > 0) s.bankruptcyLockWeeksRemaining--;
        applyPersistentUpgrades();
        applyDebtSpiralPenalties();
        if (s.wageServePenaltyWeeks > 0) {
            s.wageServePenaltyWeeks = Math.max(0, s.wageServePenaltyWeeks - 1);
            if (s.wageServePenaltyWeeks == 0) s.wageServePenaltyPct = 0.0;
        }
        s.fightsThisWeek = 0;
        s.weeklyMusicSwitches = 0;
        s.weekRefundTotal = 0.0;
        s.weekRevenue = 0.0;
        s.weekCosts = 0.0;
        s.weekInnRevenue = 0.0;
        s.weekInnRoomsSold = 0;
        s.weekInnEventsCount = 0;
        s.weekInnComplaintCount = 0;
        s.weekInnEventMaintenance = 0.0;
        s.weekInnEventRefunds = 0.0;
        s.unservedThisWeek = 0;
        s.weekPriceMultiplierSum = 0.0;
        s.weekPriceMultiplierSamples = 0;
        s.weekFoodOrders = 0;
        s.weekFoodQualityPoints = 0.0;
        s.weeklyRepDeltaAbs = 0.0;
        s.weeklyRepDeltaNet = 0.0;
        s.weekPositiveEvents = 0;
        s.weekNegativeEvents = 0;
        s.weekChaosTotal = 0.0;
        s.weekChaosRounds = 0;
        s.staffMisconductThisWeek = 0;
        s.creditLinesOpenedThisWeek = 0;
        s.weekActivityNights = 0;
        s.staffDeparturesThisWeek = 0;
        s.nearCapacityServiceNightsThisWeek = 0;
        s.weekActivityIdentityCategories.clear();
        s.weeklyDifferentActivityCategories = 0;
        s.topTierActivityRanThisWeek = false;
        s.creditScoreAtWeekStart = s.creditScore;
        s.largeBulkOrdersCompleted = 0;
    }

    private void updatePubIdentityFromWeek() {
        double profit = s.weekRevenue - s.weekCosts;
        double chaosAvg = s.weekChaosRounds > 0 ? (s.weekChaosTotal / s.weekChaosRounds) : s.chaos;
        double foodQuality = s.weekFoodOrders > 0 ? (s.weekFoodQualityPoints / s.weekFoodOrders) : 0.0;

        double decay = 0.85;
        s.identityRespectable *= decay;
        s.identityRowdy *= decay;
        s.identityArtsy *= decay;
        s.identityShady *= decay;
        s.identityFamily *= decay;
        s.identityUnderground *= decay;

        if (profit > 0) {
            s.identityRespectable += 1.2;
            s.identityFamily += 0.6;
        } else if (profit < 0) {
            s.identityShady += 0.8;
        }

        if (s.fightsThisWeek == 0) {
            s.identityRespectable += 1.0;
            s.identityFamily += 1.0;
        } else if (s.fightsThisWeek >= 3) {
            s.identityRowdy += 1.6;
        }
        if (s.unservedThisWeek >= 8) {
            s.identityRowdy += 0.8;
        }

        if (chaosAvg < 18) {
            s.identityRespectable += 0.8;
        } else if (chaosAvg > 40) {
            s.identityRowdy += 1.1;
            s.identityUnderground += 0.9;
        }

        if (s.weeklyRepDeltaNet > 0) {
            s.identityRespectable += 0.8;
            s.identityArtsy += 0.4;
        } else if (s.weeklyRepDeltaNet < 0) {
            s.identityShady += 0.9;
        }

        if (foodQuality >= 2.6 && s.weekRefundTotal < 6) {
            s.identityFamily += 1.0;
            s.identityRespectable += 0.6;
        } else if (s.weekRefundTotal > 12) {
            s.identityShady += 0.8;
        }

        if (s.staffMisconductThisWeek > 0) {
            s.identityShady += 1.4;
        }

        if (s.weekActivityNights > 0) {
            s.identityArtsy += s.weekActivityNights * 1.1;
        }

        if (s.loanShark.isOpen() && s.loanShark.getBalance() > 0.0) {
            s.identityShady += 0.6;
        }

        PubIdentity previous = s.pubIdentity;
        PubIdentity next = pickDominantIdentity(3.0);
        s.pubIdentity = next;
        if (previous == next) {
            s.identityDrift = "";
        } else if (next == PubIdentity.ROWDY || next == PubIdentity.SHADY || next == PubIdentity.UNDERGROUND) {
            s.identityDrift = "";
        } else {
            s.identityDrift = "";
        }
        s.weeklyIdentityFlavorText = "Locals now describe " + s.pubName + " as '" + identityDescriptor(next) + "'.";
        s.identityDriftSummary = buildIdentityDriftSummary(chaosAvg, profit);
    }

    private PubIdentity pickDominantIdentity(double threshold) {
        PubIdentity best = PubIdentity.NEUTRAL;
        double bestScore = threshold;
        if (s.identityRespectable > bestScore) { bestScore = s.identityRespectable; best = PubIdentity.RESPECTABLE; }
        if (s.identityRowdy > bestScore) { bestScore = s.identityRowdy; best = PubIdentity.ROWDY; }
        if (s.identityArtsy > bestScore) { bestScore = s.identityArtsy; best = PubIdentity.ARTSY; }
        if (s.identityShady > bestScore) { bestScore = s.identityShady; best = PubIdentity.SHADY; }
        if (s.identityFamily > bestScore) { bestScore = s.identityFamily; best = PubIdentity.FAMILY_FRIENDLY; }
        if (s.identityUnderground > bestScore) { bestScore = s.identityUnderground; best = PubIdentity.UNDERGROUND; }
        return best;
    }

    private String identityDescriptor(PubIdentity identity) {
        return switch (identity) {
            case NEUTRAL -> "neutral";
            case RESPECTABLE -> "respectable";
            case ROWDY -> "rowdy";
            case ARTSY -> "artsy";
            case SHADY -> "shady";
            case FAMILY_FRIENDLY -> "family-friendly";
            case UNDERGROUND -> "underground";
        };
    }

    private String buildIdentityDriftSummary(double chaosAvg, double profit) {
        StringBuilder sb = new StringBuilder();
        if (Math.abs(s.weeklyRepDeltaNet) < 2) {
            sb.append("Reputation stable");
        } else if (s.weeklyRepDeltaNet > 0) {
            sb.append("Reputation rising");
        } else {
            sb.append("Reputation slipping");
        }
        if (s.fightsThisWeek >= 2) sb.append(", rising fights");
        if (chaosAvg > 35) sb.append(", high chaos");
        if (profit < 0) sb.append(", profit pressure");
        sb.append(" pushed identity toward ").append(s.pubIdentity.name()).append(".");
        return sb.toString();
    }

    private void updateRumorsFromWeek() {
        double chaosAvg = s.weekChaosRounds > 0 ? (s.weekChaosTotal / s.weekChaosRounds) : s.chaos;
        int decay = (s.unservedThisWeek <= 2 && s.fightsThisWeek == 0) ? 8 : 6;
        decayRumorHeat(decay);

        if (s.weekRefundTotal > 10 || (s.weekFoodOrders > 0 && (s.weekFoodQualityPoints / s.weekFoodOrders) < 2.0)) {
            addRumorHeat(Rumor.FOOD_POISONING_SCARE, 18);
        }
        if (s.weekFoodOrders > 0 && (s.weekFoodQualityPoints / s.weekFoodOrders) >= 3.0 && s.weekRefundTotal < 6) {
            addRumorHeat(Rumor.BEST_SUNDAY_ROAST, 14);
        }
        if (s.fightsThisWeek >= 2 || s.weekNegativeEvents >= 3) {
            addRumorHeat(Rumor.FIGHTS_EVERY_WEEKEND, 16);
        }
        if (chaosAvg > 35) {
            addRumorHeat(Rumor.FIGHTS_EVERY_WEEKEND, 6);
        }
        if (s.staffMisconductThisWeek > 0) {
            addRumorHeat(Rumor.STAFF_STEALING, 15);
        }
        double avgPrice = s.weekPriceMultiplierSamples > 0 ? (s.weekPriceMultiplierSum / s.weekPriceMultiplierSamples) : 1.0;
        if (avgPrice >= 1.35) {
            addRumorHeat(Rumor.WATERED_DOWN_DRINKS, 12);
        }

        refreshActiveRumors();
    }

    private void decayRumorHeat(int amount) {
        for (Rumor rumor : Rumor.values()) {
            int current = s.rumorHeat.getOrDefault(rumor, 0);
            int next = Math.max(0, current - amount);
            s.rumorHeat.put(rumor, next);
        }
    }

    private void addRumorHeat(Rumor rumor, int amount) {
        int current = s.rumorHeat.getOrDefault(rumor, 0);
        int next = Math.min(100, current + amount);
        s.rumorHeat.put(rumor, next);
    }
    private void addRumorHeat(Rumor rumor, int amount, RumorSource source) {
        addRumorHeat(rumor, amount);
        // Opportunistically update activeRumors entry with the latest source.
        int heat = s.rumorHeat.getOrDefault(rumor, 0);
        if (heat > 0) {
            int intensity = Math.min(100, heat);
            double spread = Math.min(1.0, 0.15 + (intensity / 200.0));
            int days = Math.max(2, Math.min(14, 3 + (intensity / 15)));
            s.activeRumors.put(rumor, new RumorInstance(rumor, source, RumorTruth.EXAGGERATED, intensity, spread, days));
        }
    }

    private void generateNightRumor() {
        double baseChance = 0.10;
        if (s.nightUnserved >= 6) baseChance += 0.04;
        if (s.nightRefunds > 0 || s.nightFoodUnserved > 0) baseChance += 0.03;
        if (s.staffIncidentThisNight) baseChance += 0.04;
        if (s.nightFights > 0) baseChance += 0.03;
        if (s.chaos > 55) baseChance += 0.03;
        if (s.chaos < 18 && s.nightUnserved == 0 && s.nightRefunds == 0) baseChance -= 0.03;
        GameModifierSnapshot mods = buildModifierSnapshot();
        baseChance -= mods.vipRumorShield();
        baseChance = Math.max(0.02, Math.min(0.25, baseChance));

        RumorTone tone = rumorToneFromMorale();
        s.lastRumorDrivers = buildRumorDriverLine(baseChance, tone);

        if (s.random.nextDouble() > baseChance) {
            s.lastRumorHeadline = "None";
            return;
        }

        Rumor rumor = pickRumorForTone(tone);
        int heat = switch (tone) {
            case NEGATIVE -> 10 + s.random.nextInt(7);
            case MIXED -> 8 + s.random.nextInt(6);
            case POSITIVE -> 8 + s.random.nextInt(5);
        };
        addRumorHeat(rumor, heat, RumorSource.PUNTER);
        s.lastRumorHeadline = pickRumorHeadline(rumor, tone);
    }

    private RumorTone rumorToneFromMorale() {
        double morale = s.teamMorale;
        if (FeatureFlags.FEATURE_RIVALS) {
            morale -= (s.rivalRumorSentimentBias * 10.0);
        }
        if (morale <= 40) return RumorTone.NEGATIVE;
        if (morale >= 70) return RumorTone.POSITIVE;
        return RumorTone.MIXED;
    }

    private Rumor pickRumorForTone(RumorTone tone) {
        java.util.List<Rumor> negative = new java.util.ArrayList<>();
        java.util.List<Rumor> positive = new java.util.ArrayList<>();

        if (s.staffIncidentThisNight) negative.add(Rumor.STAFF_STEALING);
        if (s.nightUnserved >= 6) negative.add(Rumor.SLOW_SERVICE);
        if (s.nightRefunds > 0 || s.nightFoodUnserved > 0) negative.add(Rumor.FOOD_POISONING_SCARE);
        if (s.nightFights > 0 || s.chaos > 55) {
            negative.add(Rumor.DODGY_LATE_NIGHTS);
            negative.add(Rumor.FIGHTS_EVERY_WEEKEND);
        }

        if (s.teamMorale >= 70) positive.add(Rumor.FRIENDLY_STAFF);
        if (s.chaos < 20 && s.nightUnserved == 0) positive.add(Rumor.GREAT_ATMOSPHERE);
        if (s.kitchenUnlocked && s.nightRefunds == 0 && s.nightFoodUnserved == 0) {
            positive.add(Rumor.BEST_SUNDAY_ROAST);
        }
        if (s.activityTonight != null) positive.add(Rumor.LIVE_MUSIC_SCENE);

        if (tone == RumorTone.NEGATIVE) {
            return pickRumorFromList(negative, List.of(
                    Rumor.SLOW_SERVICE,
                    Rumor.STAFF_STEALING,
                    Rumor.DODGY_LATE_NIGHTS,
                    Rumor.FIGHTS_EVERY_WEEKEND,
                    Rumor.WATERED_DOWN_DRINKS,
                    Rumor.FOOD_POISONING_SCARE
            ));
        }
        if (tone == RumorTone.POSITIVE) {
            return pickRumorFromList(positive, List.of(
                    Rumor.FRIENDLY_STAFF,
                    Rumor.GREAT_ATMOSPHERE,
                    Rumor.BEST_SUNDAY_ROAST,
                    Rumor.LIVE_MUSIC_SCENE
            ));
        }
        java.util.List<Rumor> mixed = new java.util.ArrayList<>();
        mixed.addAll(negative);
        mixed.addAll(positive);
        return pickRumorFromList(mixed, List.of(
                Rumor.GREAT_ATMOSPHERE,
                Rumor.SLOW_SERVICE,
                Rumor.BEST_SUNDAY_ROAST,
                Rumor.DODGY_LATE_NIGHTS
        ));
    }

    private Rumor pickRumorFromList(java.util.List<Rumor> candidates, java.util.List<Rumor> fallback) {
        java.util.List<Rumor> list = candidates.isEmpty() ? fallback : candidates;
        return list.get(s.random.nextInt(list.size()));
    }

    private String buildRumorDriverLine(double chance, RumorTone tone) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chance ").append(String.format("%.1f%%", chance * 100));
        if (tone == RumorTone.NEGATIVE) sb.append(" | morale low");
        if (tone == RumorTone.POSITIVE) sb.append(" | morale high");
        if (tone == RumorTone.MIXED) sb.append(" | morale mixed");
        if (s.chaos >= 40) sb.append(" | chaos high");
        if (s.staffIncidentThisNight) sb.append(" | staff incident");
        if (s.nightRefunds > 0 || s.nightFoodUnserved > 0) sb.append(" | refunds/food misses");
        if (s.nightUnserved >= 6) sb.append(" | unserved spike");
        sb.append(" | ").append(pickPhrase(RUMOR_DRIVER_LINES));
        return sb.toString();
    }

    private String pickRumorHeadline(Rumor rumor, RumorTone tone) {
        return switch (rumor) {
            case SLOW_SERVICE -> pickPhrase(List.of(
                    "Service dragged once it got busy.",
                    "Waits stacked up after the rush hit.",
                    "Queue got long; patience ran out.",
                    "Orders took longer than anyone liked.",
                    "The bar fell behind during peak."
            ));
            case STAFF_STEALING -> pickPhrase(List.of(
                    "Staff were comping drinks a bit too freely.",
                    "Locals noticed the tab math didn't add up.",
                    "Whispers about the till being light.",
                    "People saw freebies flying out.",
                    "The register story felt off."
            ));
            case FOOD_POISONING_SCARE -> pickPhrase(List.of(
                    "Kitchen quality dipped tonight.",
                    "Food felt rushed and it showed.",
                    "A couple plates came back untouched.",
                    "The kitchen seemed off its game.",
                    "Prep standards were shaky."
            ));
            case WATERED_DOWN_DRINKS -> pickPhrase(List.of(
                    "Drinks felt a touch weak tonight.",
                    "Pours were light for the price.",
                    "The round didn’t taste as bold.",
                    "A few pints felt a bit thin.",
                    "Cocktails lacked their usual punch."
            ));
            case DODGY_LATE_NIGHTS -> pickPhrase(List.of(
                    "The late crowd felt rough around the edges.",
                    "Vibe got sketchy as the night wore on.",
                    "The room turned rowdy after last call.",
                    "Energy shifted in a bad way late.",
                    "The late-night mood got a bit dodgy."
            ));
            case FIGHTS_EVERY_WEEKEND -> pickPhrase(List.of(
                    "Too many scuffles for comfort.",
                    "The weekend crowd got rough.",
                    "A few blow-ups made the rounds.",
                    "It felt tense once it got packed.",
                    "Security had their hands full."
            ));
            case FRIENDLY_STAFF -> pickPhrase(List.of(
                    "Staff were sharp and friendly.",
                    "Service felt effortless and upbeat.",
                    "The team had the room smiling.",
                    "Warm service kept the night light.",
                    "Bar staff made it feel easy."
            ));
            case GREAT_ATMOSPHERE -> pickPhrase(List.of(
                    "Atmosphere was cosy and confident.",
                    "The room felt dialed-in and easy.",
                    "Great energy without the chaos.",
                    "Vibe was lively but calm.",
                    "The place felt like a proper local."
            ));
            case BEST_SUNDAY_ROAST -> pickPhrase(List.of(
                    "Kitchen was firing on all cylinders.",
                    "Food came out with real care.",
                    "The plates were clean and on time.",
                    "A strong kitchen showing tonight.",
                    "Food had people talking for the right reasons."
            ));
            case LIVE_MUSIC_SCENE -> pickPhrase(List.of(
                    "Music night pulled a solid crowd.",
                    "Live tunes lifted the room.",
                    "The set list had the place buzzing.",
                    "Music gave the night a lift.",
                    "The band kept the mood high."
            ));
        };
    }

    private String buildRumorTabText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Latest rumor: ").append(s.lastRumorHeadline == null ? "None" : s.lastRumorHeadline);
        sb.append("\nRumor drivers: ").append(s.lastRumorDrivers == null ? "None" : s.lastRumorDrivers);
        sb.append("\n\nActive rumors:\n");
        sb.append(buildRumorDetailText());
        return sb.toString();
    }


    private void refreshActiveRumors() {
        // Rebuild the lightweight instances used by the UI/report.
        s.activeRumors.clear();
        for (Rumor r : Rumor.values()) {
            int heat = s.rumorHeat.getOrDefault(r, 0);
            if (heat <= 0) continue;
            int intensity = Math.min(100, heat);
            double spread = Math.min(1.0, 0.15 + (intensity / 200.0));
            int days = Math.max(2, Math.min(14, 3 + (intensity / 15)));
            s.activeRumors.put(r, new RumorInstance(r, RumorSource.PUNTER, RumorTruth.EXAGGERATED, intensity, spread, days));
        }
    }


    private void endOfWeekReport() {
        double profit = s.reportRevenue - s.reportCosts;

        log.header(" END OF WEEK " + s.weekCount);
        log.info("Report #" + s.reportIndex + " week " + (s.weeksIntoReport + 1) + "/4"
                + " | Profit GBP " + String.format("%.0f", profit)
                + " | Rev GBP " + String.format("%.0f", s.reportRevenue)
                + " | Costs GBP " + String.format("%.0f", s.reportCosts)
                + " | Sales " + s.reportSales
                + " | Events " + s.reportEvents);

        if (profit > 0) s.profitStreakWeeks++;
        else s.profitStreakWeeks = 0;

        s.weeklyReportText = ReportSystem.buildWeeklyReportText(s);
        s.weeklyReportReady = true;

        s.weeksIntoReport++;

        if (s.weeksIntoReport >= 4) {
            rollReport();
        }
    }

    private void preparePaydayBills(double wagesDue, double tipsDue) {
        s.paydayBills.clear();
        if (s.supplierWineCredit.getBalance() > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.SUPPLIER,
                    "Wine supplier credit",
                    s.supplierWineMinDue(),
                    s.supplierWineCredit.getBalance(),
                    "SUPPLIER_WINE"
            ));
        }

        if (s.supplierFoodCredit.getBalance() > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.SUPPLIER,
                    "Food supplier credit",
                    s.supplierFoodMinDue(),
                    s.supplierFoodCredit.getBalance(),
                    "SUPPLIER_FOOD"
            ));
        }

        double wagesTotal = wagesDue + tipsDue;
        if (wagesTotal > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.WAGES,
                    "Wages + tips",
                    wagesTotal,
                    wagesTotal,
                    null
            ));
        }

        if (s.rentAccruedThisWeek > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.RENT,
                    "Rent",
                    s.rentAccruedThisWeek,
                    s.rentAccruedThisWeek,
                    null
            ));
        }

        if (s.securityUpkeepAccruedThisWeek > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.SECURITY,
                    "Security upkeep",
                    s.securityUpkeepAccruedThisWeek,
                    s.securityUpkeepAccruedThisWeek,
                    null
            ));
        }

        if (s.innMaintenanceAccruedWeekly > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.INN_MAINTENANCE,
                    "Inn Maintenance (Linens/Wear)",
                    s.innMaintenanceAccruedWeekly,
                    s.innMaintenanceAccruedWeekly,
                    null
            ));
        }

        for (CreditLine line : s.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            if (line.getBalance() <= 0.0) continue;
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.CREDIT_LINE,
                    "Credit line: " + line.getLenderName(),
                    line.getWeeklyPayment(),
                    line.getBalance(),
                    line.getId()
            ));
        }

        if (s.loanShark.isOpen() && s.loanShark.getBalance() > 0.0) {
            s.paydayBills.add(new PaydayBill(
                    PaydayBill.Type.LOAN_SHARK,
                    "Loan shark",
                    s.loanShark.minPaymentDue(),
                    s.loanShark.getBalance(),
                    "LOAN_SHARK"
            ));
        }

        s.paydayReady = true;
    }

    private void applySupplierWeeklyInterest() {
        applySupplierWeeklyInterest(s.supplierWineCredit, "Wine supplier");
        applySupplierWeeklyInterest(s.supplierFoodCredit, "Food supplier");
    }

    private void applySupplierWeeklyInterest(SupplierTradeCredit account, String label) {
        if (account == null || account.getBalance() <= 0.0) return;
        double baseApr = 0.10;
        double interest = account.getBalance() * ((baseApr + account.getPenaltyAddOnApr()) / 52.0) * s.debtSpiralInterestMultiplier;
        if (interest <= 0.0) return;
        account.addBalance(interest);
        log.info(label + " credit interest +GBP " + String.format("%.2f", interest));
    }

    private void applyLoanSharkWeeklyInterest() {
        if (!s.loanShark.isOpen() || s.loanShark.getBalance() <= 0.0) return;
        double interest = s.loanShark.weeklyInterestDue() * s.debtSpiralInterestMultiplier;
        if (interest <= 0.0) return;
        s.loanShark.applyInterest(interest);
        log.info("Loan shark interest +GBP " + String.format("%.2f", interest));
    }
    public int peekServeCapacity() {
        return staff.totalServeCapacity();
    }

    public double weeklyMinDueTotal() {
        return weeklyMinDueBreakdown().total;
    }

    public WeeklyDueBreakdown weeklyMinDueBreakdown() {
        double supplier = s.supplierWineMinDue() + s.supplierFoodMinDue();
        double wages = staff.wagesDue() + (s.tipsThisWeek * 0.50);
        double rent = s.rentAccruedThisWeek;
        double security = s.securityUpkeepAccruedThisWeek;
        double inn = s.innMaintenanceAccruedWeekly;
        double creditLines = 0.0;
        for (CreditLine line : s.creditLines.getOpenLines()) {
            if (!line.isEnabled() || line.getBalance() <= 0.0) continue;
            creditLines += line.getWeeklyPayment();
        }
        double shark = s.loanShark.isOpen() ? s.loanShark.minPaymentDue() : 0.0;
        double total = supplier + wages + rent + security + inn + creditLines + shark;
        return new WeeklyDueBreakdown(total, supplier, wages, rent, security, inn, creditLines, shark);
    }

    public record WeeklyDueBreakdown(
            double total,
            double supplier,
            double wages,
            double rent,
            double security,
            double innMaintenance,
            double creditLines,
            double loanShark
    ) {}

    public record LandlordActionAvailability(boolean canUse, String reason) {}

    public record SecurityTaskAvailability(boolean canUse, String reason) {}

    public record SupplierPaymentResult(boolean success, String message) {}

    public SupplierPaymentResult paySupplierInvoice(SupplierAccount accountType, double amount, String sourceId) {
        if (accountType == null) return new SupplierPaymentResult(false, "No supplier selected.");
        SupplierTradeCredit account = accountType == SupplierAccount.FOOD
                ? s.supplierFoodCredit
                : s.supplierWineCredit;
        double balance = account.getBalance();
        if (balance <= 0.0) {
            return new SupplierPaymentResult(false, "No outstanding supplier balance.");
        }
        if (amount <= 0.0) {
            return new SupplierPaymentResult(false, "Payment must be greater than zero.");
        }
        if (amount > balance + 0.01) {
            return new SupplierPaymentResult(false, "Amount exceeds current balance.");
        }

        String source = (sourceId == null || sourceId.isBlank()) ? "CASH" : sourceId;
        if ("CASH".equals(source)) {
            if (s.cash + 0.01 < amount) {
                return new SupplierPaymentResult(false, "Not enough cash available.");
            }
            s.cash -= amount;
        } else {
            CreditLine sourceLine = s.creditLines.getLineById(source);
            if (sourceLine == null || !sourceLine.isEnabled()) {
                return new SupplierPaymentResult(false, "Selected credit line unavailable.");
            }
            double available = sourceLine.availableCredit();
            if (available + 0.01 < amount) {
                return new SupplierPaymentResult(false, "Credit line limit exceeded.");
            }
            s.creditLines.addBalanceToLine(sourceLine, amount);
        }

        account.applyPayment(amount);
        double totalLimit = s.totalCreditLimit();
        s.creditUtilization = totalLimit > 0.0 ? (s.creditLines.totalBalance() / totalLimit) : 0.0;
        log.pos(" Supplier invoice paid: " + money2(amount) + " (" + accountType + ")");
        return new SupplierPaymentResult(true, "Payment applied.");
    }

    public void applyPaydayPayments() {
        applyPaydayPayments(s.paydayBills);
    }

    public void applyPaydayPayments(List<PaydayBill> bills) {
        if (bills == null || bills.isEmpty()) {
            s.paydayReady = false;
            milestones.onPaydayResolved();
            return;
        }
        double availableCash = s.cash;
        java.util.Map<String, Double> availableCredit = new java.util.HashMap<>();
        for (CreditLine line : s.creditLines.getOpenLines()) {
            if (!line.isEnabled()) continue;
            availableCredit.put(line.getId(), line.availableCredit());
        }

        boolean allFull = true;
        boolean anyCreditMiss = false;
        boolean allCreditOnTime = true;
        boolean sharkMissed = false;
        boolean sharkPaidOnTime = false;
        boolean sharkHasBalance = s.loanShark.isOpen() && s.loanShark.getBalance() > 0.0;
        double weeklyTotalDue = 0.0;
        double weeklyTotalMinDue = 0.0;
        boolean metMinThisWeek = true;

        for (PaydayBill bill : bills) {
            double targetAmount = Math.max(0.0, Math.min(bill.getFullDue(), bill.getSelectedAmount()));
            weeklyTotalDue += bill.getFullDue();
            weeklyTotalMinDue += bill.getMinDue();
            if (targetAmount <= 0.0 && bill.getFullDue() > 0.0) {
                allFull = false;
            }

            double paidAmount = 0.0;
            String source = bill.getSelectedSourceId();
            if ("CASH".equals(source)) {
                paidAmount = Math.min(targetAmount, availableCash);
                availableCash -= paidAmount;
                s.cash -= paidAmount;
            } else {
                CreditLine sourceLine = s.creditLines.getLineById(source);
                if (sourceLine != null && sourceLine.isEnabled()) {
                    double available = availableCredit.getOrDefault(sourceLine.getId(), 0.0);
                    paidAmount = Math.min(targetAmount, available);
                    availableCredit.put(sourceLine.getId(), available - paidAmount);
                    if (paidAmount > 0.0) {
                        s.creditLines.addBalanceToLine(sourceLine, paidAmount);
                    }
                }
            }

            boolean paidFull = bill.isFullPayment(paidAmount);
            if (paidAmount + 0.01 < bill.getMinDue()) {
                metMinThisWeek = false;
            }
            if (!paidFull && bill.getFullDue() > 0.0) {
                allFull = false;
            }

            switch (bill.getType()) {
                case SUPPLIER -> {
                    applySupplierPayment(paidAmount, bill.getMinDue(), paidFull, bill.getReferenceId());
                }
                case WAGES -> {
                    applyWagePayment(paidAmount, bill.getMinDue(), paidFull);
                }
                case RENT -> {
                    applyAccruedPayment(paidAmount, bill.getMinDue(), paidFull, "Rent");
                    s.rentAccruedThisWeek = Math.max(0.0, s.rentAccruedThisWeek - paidAmount);
                }
                case SECURITY -> {
                    applyAccruedPayment(paidAmount, bill.getMinDue(), paidFull, "Security upkeep");
                    s.securityUpkeepAccruedThisWeek = Math.max(0.0, s.securityUpkeepAccruedThisWeek - paidAmount);
                }
                case INN_MAINTENANCE -> {
                    applyAccruedPayment(paidAmount, bill.getMinDue(), paidFull, "Inn maintenance");
                    s.innMaintenanceAccruedWeekly = Math.max(0.0, s.innMaintenanceAccruedWeekly - paidAmount);
                }
                case CREDIT_LINE -> {
                    CreditLine line = s.creditLines.getLineById(bill.getReferenceId());
                    if (line != null) {
                        line.applyPayment(paidAmount);
                        s.creditLines.updateWeeklyPayment(line);
                        boolean onTime = paidAmount + 0.01 >= bill.getMinDue();
                        if (onTime) {
                            line.markPaidOnTime();
                        } else {
                            line.markMissedPayment();
                            anyCreditMiss = true;
                            allCreditOnTime = false;
                            applyCreditLinePenalty(line, bill.getMinDue() - paidAmount);
                        }
                        if (paidFull) {
                            line.markFullPayment();
                            applyPenaltyRecovery(line);
                        } else {
                            line.resetFullPayStreak();
                        }
                    }
                }
                case LOAN_SHARK -> {
                    if (s.loanShark.isOpen()) {
                        s.loanShark.applyPayment(paidAmount);
                        boolean onTime = paidAmount + 0.01 >= bill.getMinDue();
                        if (onTime) {
                            sharkPaidOnTime = true;
                        } else if (bill.getMinDue() > 0.0) {
                            sharkMissed = true;
                            applyLoanSharkPenalty(bill.getMinDue() - paidAmount);
                        }
                        if (paidFull) {
                            s.loanShark.markFullPayment();
                            applyPenaltyRecovery(s.loanShark);
                        } else {
                            s.loanShark.resetRecoveryStreak();
                        }
                    }
                }
                case OTHER -> {
                    if (paidFull) {
                        // no-op
                    }
                }
            }
        }

        if (anyCreditMiss) {
            s.creditScore = s.clampCreditScore(s.creditScore - 20);
        } else if (allCreditOnTime && s.creditLines.totalBalance() > 0.0) {
            s.creditScore = s.clampCreditScore(s.creditScore + 4);
        }

        sharkHasBalance = s.loanShark.isOpen() && s.loanShark.getBalance() > 0.0;
        s.sharkMissedPaymentThisWeek = sharkMissed;
        s.sharkPaidOnTimeThisWeek = sharkPaidOnTime;
        s.sharkHasBalanceThisWeek = sharkHasBalance;

        if (sharkMissed) {
            s.creditScore = s.clampCreditScore(s.creditScore - 60);
        }

        processSharkThreatWeekly();

        if (allFull && !bills.isEmpty()) {
            s.creditScore = s.clampCreditScore(s.creditScore + 8);
        }

        resolveWeeklyMinimums(weeklyTotalDue, weeklyTotalMinDue, metMinThisWeek);

        updateNoDebtUsageAfterPayday();
        double totalLimit = s.totalCreditLimit();
        s.creditUtilization = totalLimit > 0.0 ? (s.creditLines.totalBalance() / totalLimit) : 0.0;
        bills.clear();
        s.paydayReady = false;
        milestones.onPaydayResolved();
    }

    private void resolveWeeklyMinimums(double weeklyTotalDue, double weeklyTotalMinDue, boolean metMinThisWeek) {
        s.weeklyTotalDueLastResolution = weeklyTotalDue;
        s.weeklyTotalMinDueLastResolution = weeklyTotalMinDue;
        s.metMinimumsLastWeek = metMinThisWeek;
        if (metMinThisWeek) {
            s.consecutiveWeeksUnpaidMin = 0;
        } else {
            s.consecutiveWeeksUnpaidMin++;
        }
        s.debtSpiralTier = s.debtSpiralTierFromStreak();
        applyDebtSpiralPenalties();

        log.info("Weekly minimum resolution: min " + (metMinThisWeek ? "MET" : "MISSED")
                + " | due GBP " + fmt2(weeklyTotalDue)
                + " | min due GBP " + fmt2(weeklyTotalMinDue)
                + " | consecutive missed-min weeks " + s.consecutiveWeeksUnpaidMin
                + " | debt spiral tier " + s.debtSpiralTier + ".");

        triggerBailiffsIfNeeded();
    }

    private void applyDebtSpiralPenalties() {
        s.debtSpiralTier = s.debtSpiralTierFromStreak();
        int tier = s.debtSpiralTier;
        s.debtSpiralInterestMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.12;
            case 2 -> 1.22;
            case 3 -> 1.35;
            default -> 1.50;
        };
        s.debtSpiralLateFeeMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.10;
            case 2 -> 1.25;
            case 3 -> 1.45;
            default -> 1.65;
        };
        s.debtSpiralSupplierTrustMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.05;
            case 2 -> 1.12;
            case 3 -> 1.22;
            default -> 1.35;
        };
        s.debtSpiralMoraleDecayMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.12;
            case 2 -> 1.25;
            case 3 -> 1.40;
            default -> 1.60;
        };
        s.debtSpiralMisconductChanceMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.12;
            case 2 -> 1.26;
            case 3 -> 1.45;
            default -> 1.70;
        };
        s.debtSpiralNegativeRepMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 1.10;
            case 2 -> 1.22;
            case 3 -> 1.35;
            default -> 1.52;
        };
        s.debtSpiralPositiveRepMultiplier = switch (tier) {
            case 0 -> 1.0;
            case 1 -> 0.95;
            case 2 -> 0.86;
            case 3 -> 0.76;
            default -> 0.65;
        };

        if (s.bankruptcyDeclared) {
            s.debtSpiralNegativeRepMultiplier = Math.max(s.debtSpiralNegativeRepMultiplier, BANKRUPTCY_NEG_REP_MULT);
            s.debtSpiralPositiveRepMultiplier = Math.min(s.debtSpiralPositiveRepMultiplier, BANKRUPTCY_POS_REP_MULT);
            s.debtSpiralSupplierTrustMultiplier = Math.max(s.debtSpiralSupplierTrustMultiplier, 1.40);
            s.debtSpiralMisconductChanceMultiplier = Math.max(s.debtSpiralMisconductChanceMultiplier, 1.80);
        }
    }

    private void applyDebtSpiralMoraleDecay() {
        if (s.debtSpiralTier <= 0) return;
        int moraleHit = Math.max(1, (int)Math.round(s.debtSpiralTier * s.debtSpiralMoraleDecayMultiplier));
        adjustAllStaffMorale(-moraleHit);
        log.neg("Debt spiral pressure: staff morale -" + moraleHit + " this week.");
    }

    private void triggerBailiffsIfNeeded() {
        if (s.consecutiveWeeksUnpaidMin <= BAILIFF_THRESHOLD_WEEKS) {
            return;
        }

        java.util.List<PubUpgrade> removable = new java.util.ArrayList<>(s.ownedUpgrades);
        removable.sort(java.util.Comparator
                .comparingDouble(PubUpgrade::getCost)
                .thenComparing(PubUpgrade::name));

        int removeCount = Math.min(BAILIFF_UPGRADES_REMOVED_PER_VISIT, removable.size());
        java.util.List<String> removedLabels = new java.util.ArrayList<>();
        for (int i = 0; i < removeCount; i++) {
            PubUpgrade removed = removable.get(i);
            s.ownedUpgrades.remove(removed);
            removedLabels.add(removed.getLabel());
        }
        applyPersistentUpgrades();

        double seize = Math.min(s.cash, Math.max(BAILIFF_CASH_SEIZE_FLAT, s.cash * BAILIFF_CASH_SEIZE_PCT));
        if (seize > 0.0) {
            s.cash -= seize;
        }

        eco.applyRep(BAILIFF_REP_SCAR, "Bailiff enforcement");
        s.bailiffStigma = true;

        String removedText = removedLabels.isEmpty() ? "none" : String.join(", ", removedLabels);
        log.neg("Bailiffs arrived (4th consecutive missed-min week): upgrades removed [" + removedText
                + "], cash seized GBP " + fmt2(seize)
                + ", permanent rep scar " + BAILIFF_REP_SCAR + ".");
    }

    public void declareBankruptcy() {
        java.util.List<String> removedUpgrades = new java.util.ArrayList<>();
        for (PubUpgrade up : s.ownedUpgrades) {
            removedUpgrades.add(up.getLabel());
        }
        s.ownedUpgrades.clear();
        applyPersistentUpgrades();

        s.pubLevel = 0;
        s.pubLevelServeCapBonus = 0;
        s.pubLevelBarCapBonus = 0;
        s.pubLevelTrafficBonusPct = 0.0;
        s.pubLevelRepMultiplier = 1.0;
        s.pubLevelStaffCapBonus = 0;
        s.pubLevelBouncerCapBonus = 0;
        s.pubLevelManagerCapBonus = 0;
        s.pubLevelChefCapBonus = 0;
        s.starCount = 0;
        s.prestigeMilestones.clear();
        s.creditScore = 0;
        s.banksLocked = true;
        s.bankruptcyDeclared = true;
        s.bankruptcySupplierStigma = true;
        s.bankruptcyLockWeeksRemaining = BANKRUPTCY_LONG_LOAN_LOCK_WEEKS;
        s.supplierTrustPenalty = Math.max(s.supplierTrustPenalty, 0.20);
        s.supplierCreditCapOverride = BANKRUPTCY_SUPPLIER_CREDIT_CAP;
        s.consecutiveWeeksUnpaidMin = 0;
        s.debtSpiralTier = 0;
        s.metMinimumsLastWeek = true;

        s.loanShark.setApr(s.loanShark.getApr() + BANKRUPTCY_SHARK_APR_BONUS);
        s.loanShark.setPenaltyAddOnApr(s.loanShark.getPenaltyAddOnApr() + BANKRUPTCY_SHARK_PENALTY_BONUS);

        applyDebtSpiralPenalties();
        adjustAllStaffMorale(-18);
        eco.applyRep(-30, "Bankruptcy declared");

        log.header(" BANKRUPTCY DECLARED");
        log.neg("Bankruptcy filed: all upgrades removed, pub level reset to 0, credit score reset to 0,"
                + " supplier trust crushed, supplier invoice credit cap now GBP 400,"
                + " and loan sharks turned harsher.");
        if (!removedUpgrades.isEmpty()) {
            log.neg("Bankruptcy repossession list: " + String.join(", ", removedUpgrades));
        }
    }


    private void applySupplierPayment(double amount, double minDue, boolean paidFull, String referenceId) {
        SupplierTradeCredit account = "SUPPLIER_FOOD".equals(referenceId)
                ? s.supplierFoodCredit
                : s.supplierWineCredit;
        account.applyPayment(amount);
        if (amount + 0.01 < minDue) {
            double shortfall = Math.max(0.0, minDue - amount);
            double lateFee = Math.max(6.0, shortfall * 0.08) * s.debtSpiralLateFeeMultiplier;
            account.addLateFee(lateFee);
            account.setPenaltyAddOnApr(Math.min(0.20, account.getPenaltyAddOnApr() + 0.02));
            account.setConsecutiveFullPays(0);
            s.creditScore = s.clampCreditScore(s.creditScore - 6);
            adjustSupplierTrustPenalty(0.02);
            adjustAllStaffMorale(-2);
            log.neg("Supplier payment below minimum. Late fee GBP " + String.format("%.2f", lateFee));
        } else {
            account.clearLateFees();
            if (paidFull) {
                account.setConsecutiveFullPays(account.getConsecutiveFullPays() + 1);
                applyPenaltyRecoverySupplier(account);
            } else {
                account.setConsecutiveFullPays(0);
            }
        }
    }

    private void applyWagePayment(double amount, double minDue, boolean paidFull) {
        if (amount <= 0.0) {
            handleWageMiss();
            return;
        }
        staff.applyWagePayment(amount);
        if (paidFull) {
            staff.resetAccrual();
            s.wagesAccruedThisWeek = 0.0;
            handleWagesPaid();
            applyTipsPayout();
        } else if (amount + 0.01 < minDue) {
            handleWageMiss();
        }
    }

    private void applyAccruedPayment(double amount, double minDue, boolean paidFull, String label) {
        if (paidFull) return;
        if (amount + 0.01 < minDue) {
            log.neg(label + " paid below minimum.");
        }
    }

    private void applyCreditLinePenalty(CreditLine line, double shortfall) {
        if (line == null) return;
        double lateFee = Math.max(5.0, shortfall * 0.10) * s.debtSpiralLateFeeMultiplier;
        line.addBalance(lateFee);
        line.setPenaltyAddOnApr(Math.min(0.25, line.getPenaltyAddOnApr() + 0.015));
        log.neg("Late fee on " + line.getLenderName() + ": GBP " + String.format("%.2f", lateFee));
    }

    private void applyLoanSharkPenalty(double shortfall) {
        double lateFee = Math.max(8.0, shortfall * 0.12) * s.debtSpiralLateFeeMultiplier;
        s.loanShark.applyInterest(lateFee);
        s.loanShark.setPenaltyAddOnApr(Math.min(0.35, s.loanShark.getPenaltyAddOnApr() + 0.03));
        s.loanShark.markMissedPayment();
        log.neg("Loan shark payment missed. Late fee GBP " + String.format("%.2f", lateFee));
    }

    private void applyPenaltyRecovery(CreditLine line) {
        if (line.getConsecutiveFullPays() < 3) return;
        int stage = line.getPenaltyRecoveryStage();
        if (stage == 0) line.setPenaltyAddOnApr(line.getPenaltyAddOnApr() * 0.5);
        else if (stage == 1) line.setPenaltyAddOnApr(line.getPenaltyAddOnApr() * 0.7);
        else if (stage == 2) line.setPenaltyAddOnApr(line.getPenaltyAddOnApr() * 0.8);
        else {
            line.setPenaltyAddOnApr(0.0);
            line.setPenaltyRecoveryStage(0);
            line.setConsecutiveFullPays(0);
            return;
        }
        line.setPenaltyRecoveryStage(stage + 1);
        line.setConsecutiveFullPays(0);
    }

    private void applyPenaltyRecoverySupplier(SupplierTradeCredit account) {
        if (account == null || account.getConsecutiveFullPays() < 3) return;
        int stage = account.getPenaltyRecoveryStage();
        double addOn = account.getPenaltyAddOnApr();
        if (stage == 0) addOn *= 0.5;
        else if (stage == 1) addOn *= 0.7;
        else if (stage == 2) addOn *= 0.8;
        else {
            account.setPenaltyAddOnApr(0.0);
            account.setPenaltyRecoveryStage(0);
            account.setConsecutiveFullPays(0);
            return;
        }
        account.setPenaltyAddOnApr(addOn);
        account.setPenaltyRecoveryStage(stage + 1);
        account.setConsecutiveFullPays(0);
    }

    private void applyPenaltyRecovery(LoanSharkAccount shark) {
        if (shark.getConsecutiveFullPays() < 3) return;
        int stage = shark.getPenaltyRecoveryStage();
        double addOn = shark.getPenaltyAddOnApr();
        if (stage == 0) addOn *= 0.5;
        else if (stage == 1) addOn *= 0.7;
        else if (stage == 2) addOn *= 0.8;
        else {
            shark.setPenaltyAddOnApr(0.0);
            shark.setPenaltyRecoveryStage(0);
            shark.setConsecutiveFullPays(0);
            return;
        }
        shark.setPenaltyAddOnApr(addOn);
        shark.setPenaltyRecoveryStage(stage + 1);
        shark.setConsecutiveFullPays(0);
    }

    private void updateNoDebtUsageAfterPayday() {
        double totalBalance = s.creditLines.totalBalance();
        if (totalBalance <= 0.0) {
            s.noDebtUsageWeeks++;
            if (s.noDebtUsageWeeks >= 2) {
                int bonus = Math.min(3, s.noDebtUsageWeeks - 1);
                s.creditScore = s.clampCreditScore(s.creditScore + bonus);
            }
        } else {
            s.noDebtUsageWeeks = 0;
        }
    }

    private void applyTipsPayout() {
        if (s.tipsThisWeek <= 0) return;

        double payout = s.tipsThisWeek * 0.50;
        if (payout <= 0) return;

        int moraleDelta = 0;
        if (s.tipsThisWeek >= 60) moraleDelta += 3;
        else if (s.tipsThisWeek >= 25) moraleDelta += 2;
        if (s.tipsThisWeek < 10) moraleDelta -= 1;

        for (Staff st : s.fohStaff) st.adjustMorale(moraleDelta);
        for (Staff st : s.bohStaff) st.adjustMorale(moraleDelta);
        for (Staff st : s.generalManagers) st.adjustMorale(moraleDelta);

        staff.updateTeamMorale();

        log.popup(" Tips payout",
                "Staff split GBP " + String.format("%.2f", payout) + " in tips.",
                "Morale " + (moraleDelta >= 0 ? "+" : "") + moraleDelta);

        s.tipsThisWeek = 0.0;
    }

    public MetricsSnapshot buildMetricsSnapshot() {
        int serveCap = staff.totalServeCapacity();
        int sec = security.effectiveSecurity();
        String mood = repMoodLabel();
        String identityLine = s.pubIdentity.name().replace('_', ' ') + " " + s.identityDrift;
        String chaosLabel = chaosMoodLabel();
        double trafficMult = baseTrafficMultiplier() * identityTrafficMultiplier() * rumorTrafficMultiplier()
                * activities.trafficMultiplier() * securityPolicyTrafficMultiplier()
                * securityTaskTrafficMultiplier()
                * (1.0 + s.landlordTrafficBonusPct)
                * rivalTrafficMultiplier();
        double creditBalance = s.totalCreditBalance()
                + (s.loanShark.isOpen() ? s.loanShark.getBalance() : 0.0);
        double creditWeeklyDue = s.totalCreditWeeklyPaymentDue();

        java.util.List<String> overview = new java.util.ArrayList<>();
        overview.add("Cash: GBP " + fmt2(s.cash) + " | Debt: GBP " + fmt2(creditBalance)
                + " | Weekly Costs (Due at Payday): GBP " + fmt2(weeklyMinDueTotal()));
        overview.add("Credit score: " + s.creditScore
                + " | Utilisation: " + String.format("%.0f%%", s.creditUtilization * 100)
                + " | Supplier trust: " + s.supplierTrustLabel());
        overview.add("Reputation: " + s.reputation + " (" + mood + ")");
        overview.add("Identity: " + identityLine);
        overview.add("Chaos: " + String.format("%.1f", s.chaos) + " (" + chaosLabel + ")");
        overview.add("Morale: FOH " + (int)Math.round(s.fohMorale)
                + " | BOH " + (int)Math.round(s.bohMorale)
                + " | Team " + (int)Math.round(s.teamMorale));
        overview.add("Security: " + sec + " | Serve cap: " + serveCap
                + " | Bar cap: " + s.maxBarOccupancy + " | Traffic x" + String.format("%.2f", trafficMult));
        overview.add("Activity: " + activitySummaryLine());
        overview.add("Active rumors: " + s.activeRumors.size());
        overview.add("Business status: " + (s.businessCollapsed ? "Collapsed (Recovery Possible)" : "Operating"));

        String economy = "Revenue (week): GBP " + fmt2(s.weekRevenue)
                + "\nCosts (week): GBP " + fmt2(s.weekCosts)
                + "\nProfit (week): GBP " + fmt2(s.weekRevenue - s.weekCosts)
                + "\nCash: GBP " + fmt2(s.cash)
                + "\nDebt: GBP " + fmt2(creditBalance)
                + "\nCredit score: " + s.creditScore
                + "\nCredit utilisation: " + String.format("%.0f%%", s.creditUtilization * 100)
                + "\nSupplier trust: " + s.supplierTrustLabel()
                + "\nSupplier price mult: x" + fmt2(s.supplierPriceMultiplier())
                + "\nSupplier credit cap: GBP " + fmt2(s.supplierCreditCap())
                + "\nWeekly costs due at payday: GBP " + fmt2(weeklyMinDueTotal())
                + "\nWeekly credit repayments due: GBP " + fmt2(creditWeeklyDue)
                + "\nShark threat: Tier " + s.sharkThreatTier + " (" + sharkTierLabel(s.sharkThreatTier) + ")"
                + "\nShark trigger: " + s.sharkThreatTrigger
                + " | Misses: " + s.sharkConsecutiveMisses
                + " | Reduce: pay on time for 1 week"
                + "\nCredit lines:\n" + buildCreditLineSummary()
                + "\nWine supplier balance: GBP " + fmt2(s.supplierWineCredit.getBalance())
                + "\nWine supplier min due: GBP " + fmt2(s.supplierWineMinDue())
                + "\nWine supplier late fees: GBP " + fmt2(s.supplierWineCredit.getLateFeesThisWeek())
                + "\nFood supplier balance: GBP " + fmt2(s.supplierFoodCredit.getBalance())
                + "\nFood supplier min due: GBP " + fmt2(s.supplierFoodMinDue())
                + "\nFood supplier late fees: GBP " + fmt2(s.supplierFoodCredit.getLateFeesThisWeek())
                + "\nInn maintenance accrued: GBP " + fmt2(s.innMaintenanceAccruedWeekly)
                + "\n\nWages / Stability:"
                + "\nWages due (this week): GBP " + fmt2(staff.wagesDue())
                + "\nStatus: " + (s.wagesPaidLastWeek ? "Paid" : "Unpaid")
                + "\nConsecutive misses: " + s.consecutiveMissedWagePayments
                + "\nNext escalation: " + wageEscalationWarning()
                + "\nRecovery: " + wageRecoveryGuidance()
                + "\nPrice multiplier avg: " + fmt2(avgPriceMultiplier())
                + "\nPrice volatility: " + fmt2(s.weekPriceMultiplierAbsDelta);

        String operations = "Service: " + (s.nightOpen ? "OPEN" : "CLOSED")
                + "\nRound: " + s.roundInNight + "/" + s.closingRound
                + "\nServe cap: " + serveCap
                + "\nBar cap: " + s.maxBarOccupancy
                + "\nTraffic multiplier: x" + fmt2(trafficMult)
                + "\nChaos: " + String.format("%.1f", s.chaos) + " (" + chaosLabel + ")"
                + "\nChaos breakdown: " + chaosBreakdownLine()
                + "\nChaos insights: " + buildChaosInsightsLine()
                + "\nLast between-night event: " + s.lastBetweenNightEventSummary
                + "\n\nUpgrade dependencies:\n" + buildUpgradeDependencyText()
                + "\n\nActivity dependencies:\n" + buildActivityDependencyText()
                + "\n\nActivity info:\n" + buildUnlockedActivityInfoText()
                + "\n\nMilestone rewards:\n" + buildMilestoneRewardSummaryText();

        String staffText = buildStaffTabSummary(serveCap);

        String risk = "Security: " + sec
                + "\nPolicy: " + (s.securityPolicy != null ? s.securityPolicy.getLabel() : "Balanced")
                + "\nBouncers hired: " + s.bouncersHiredTonight + "/" + s.bouncerCap
                + "\nFights (week): " + s.fightsThisWeek
                + "\nRefunds (week): GBP " + fmt2(s.weekRefundTotal)
                + "\nUnserved (week): " + s.unservedThisWeek
                + "\nChaos: " + String.format("%.1f", s.chaos)
                + "\nActive risk rumors: " + riskRumorCount();

        String reputation = "Reputation: " + s.reputation + " (" + mood + ")"
                + "\nIdentity: " + identityLine
                + "\nWeekly narrative: " + (s.weeklyIdentityFlavorText == null ? "" : s.weeklyIdentityFlavorText)
                + "\nDrift summary: " + (s.identityDriftSummary == null ? "" : s.identityDriftSummary);

        String rumors = buildRumorTabText();

        String trafficPunters = "Traffic multiplier: x" + fmt2(trafficMult)
                + "\nBase traffic: x" + fmt2(baseTrafficMultiplier())
                + "\nIdentity traffic: x" + fmt2(identityTrafficMultiplier())
                + "\nRumor traffic: x" + fmt2(rumorTrafficMultiplier())
                + "\nSecurity policy traffic: x" + fmt2(securityPolicyTrafficMultiplier())
                + "\nSecurity task traffic: x" + fmt2(securityTaskTrafficMultiplier())
                + "\nActivity traffic: x" + fmt2(activities.trafficMultiplier())
                + "\nLegacy traffic: x" + fmt2(1.0 + s.legacy.trafficMultiplierBonus)
                + "\nPunters in bar: " + s.nightPunters.size() + "/" + s.maxBarOccupancy
                + "\nNatural departures (night): " + s.nightNaturalDepartures
                + "\nTier mix: " + punterTierBreakdown();

        String inventory = "Wine: " + s.rack.count() + "/" + s.rack.getCapacity()
                + "\nFood: " + (s.kitchenUnlocked ? (s.foodRack.count() + "/" + s.foodRack.getCapacity()) : "Locked")
                + "\nFood spoiled last night: " + s.foodSpoiledLastNight;

        String loans = buildLoanSummaryText();

        String financeBanking = buildFinanceBankingText(creditBalance, creditWeeklyDue);
        String payday = buildPaydayDetailText();
        String suppliers = buildSuppliersDetailText();
        String progression = buildProgressionDetailText();
        String securityDetail = buildSecurityDetailText(sec);
        String staffDetail = buildStaffDetailText();
        String innDetail = buildInnDetailText();
        String prestigeDetail = buildPrestigeText();
        String musicDetail = buildMusicDetailText();

        String logSummary = "Service events: " + s.nightEvents
                + "\nBetween-night: " + s.lastBetweenNightEventSummary;

        return new MetricsSnapshot(
                "Cash: GBP " + fmt2(s.cash),
                "Debt: GBP " + fmt2(creditBalance),
                "Reputation: " + s.reputation + " (" + mood + ")",
                " " + s.pubName + " (Lv " + s.pubLevel + ")",
                "Weekly Costs (Due at Payday): GBP " + fmt2(weeklyMinDueTotal()),
                "Week " + s.weekCount + "  " + s.dayName() + " | Service " + s.nightCount,
                s.nightOpen
                        ? ("Service OPEN  Round " + s.roundInNight + "/" + s.closingRound
                        + " | Bar " + s.nightPunters.size() + "/" + s.maxBarOccupancy)
                        : "Service CLOSED  Ready",
                "Security: " + sec,
                "Staff: " + s.staff().summaryLine() + " | Serve cap " + serveCap,
                "Report: " + s.reports().summaryLine(),
                "Can serve (per round): " + serveCap,
                overview,
                economy,
                operations,
                staffText,
                risk,
                reputation,
                rumors,
                trafficPunters,
                inventory,
                loans,
                logSummary,
                financeBanking,
                payday,
                suppliers,
                progression,
                securityDetail,
                staffDetail,
                innDetail,
                prestigeDetail,
                musicDetail
        );
    }
    private String buildMusicDetailText() {
        TimePhase phase = s.getCurrentPhase();
        MusicEffects fx = musicSystem.computeEffects(s.currentMusicProfile, phase);
        double timeMult = timeOfDayTrafficMultiplier(phase, s.getCurrentTime());
        StringBuilder sb = new StringBuilder();
        sb.append("Profile: ").append(s.currentMusicProfile.getLabel()).append("\n");
        sb.append("Audio track: ").append(audioManager.currentMusicFileName()).append("\n");
        sb.append("Crowd chatter: ").append(audioManager.currentChatterBandLabel())
                .append(" (").append(audioManager.currentChatterFileName()).append(")\n");
        sb.append("Time: ").append(s.getCurrentTime()).append(" | Phase: ").append(phase).append("\n");
        sb.append("Change rule: once per phase\n");
        sb.append("Last change round index: ").append(s.lastMusicProfileChangeRound).append("\n\n");

        sb.append("Current effects\n");
        sb.append("- Traffic multiplier: x").append(fmt2(fx.trafficMultiplier())).append("\n");
        sb.append("- Spend multiplier: x").append(fmt2(fx.spendMultiplier())).append("\n");
        sb.append("- Linger signal: x").append(fmt2(fx.lingerMultiplier())).append("\n");
        sb.append("- Chaos delta pressure: ").append(String.format("%+.2f", fx.chaosDelta())).append("\n");
        sb.append("- Reputation drift pressure: ").append(String.format("%+.2f", fx.reputationDriftDelta())).append("\n");
        sb.append("- Staff morale pressure: ").append(String.format("%+.2f", fx.staffMoraleDelta())).append("\n");
        sb.append("- Identity pressure: ").append(String.format("%+.2f", fx.identityPressure())).append("\n");
        sb.append("- Late chaos risk: ").append(fx.lateChaosRisk() ? "ELEVATED" : "Normal").append("\n\n");

        sb.append("Traffic stack (current round)\n");
        sb.append("- Base traffic: x").append(fmt2(baseTrafficMultiplier())).append("\n");
        sb.append("- Time-of-day curve: x").append(fmt2(timeMult)).append("\n");
        sb.append("- Music profile: x").append(fmt2(fx.trafficMultiplier())).append("\n");
        sb.append("- Identity: x").append(fmt2(identityTrafficMultiplier())).append("\n");
        sb.append("- Rumors: x").append(fmt2(rumorTrafficMultiplier())).append("\n");
        sb.append("- Activity: x").append(fmt2(activities.trafficMultiplier())).append("\n");
        sb.append("- Security policy/task: x").append(fmt2(securityPolicyTrafficMultiplier() * securityTaskTrafficMultiplier())).append("\n");

        sb.append("\nConsistency pressure\n");
        sb.append("- Consecutive nights same profile: ").append(s.consecutiveNightsSameMusic).append("\n");
        sb.append("- Switches this week: ").append(s.weeklyMusicSwitches).append("\n");
        sb.append("- Summary: ").append(fx.summary()).append("\n");
        return sb.toString();
    }

    private String buildPrestigeText() {

        StringBuilder sb = new StringBuilder();
        sb.append("Stars: ").append(s.starCount).append("/").append(PrestigeSystem.MAX_STARS)
                .append(" ").append(buildStarBadge()).append("\n");
        if (prestigeSystem.isMaxStars(s)) {
            sb.append("Status: Max ★ reached\n");
        } else if (isPrestigeAvailable()) {
            sb.append("Status: Prestige available (preview from Mission Control)\n");
        } else {
            sb.append("Status: Not ready\n");
        }
        sb.append("\nLegacy bonuses:\n");
        for (String line : s.legacy.detailLines()) {
            sb.append(" - ").append(line).append("\n");
        }
        sb.append("\nDiminishing returns (next star):\n");
        for (String line : prestigeSystem.diminishingReturnLines()) {
            sb.append(" - ").append(line).append("\n");
        }
        return sb.toString();
    }

    private String buildUpgradeDependencyText() {
        StringBuilder sb = new StringBuilder();
        for (PubUpgrade up : PubUpgrade.values()) {
            boolean owned = s.ownedUpgrades.contains(up);
            MilestoneSystem.UpgradeAvailability availability = milestones.getUpgradeAvailability(up, s.cash);
            String status;
            if (owned) {
                status = "Unlocked";
            } else if (availability.unlocked()) {
                status = "Available";
            } else {
                status = "Locked (" + String.join(", ", availability.missingRequirements()) + ")";
            }
            sb.append("- ").append(up.getLabel()).append(": ").append(status)
                    .append(" | ").append(upgrades.effectSummary(up)).append("\n");
        }
        return trimTrailingNewline(sb);
    }

    private String buildActivityDependencyText() {
        StringBuilder sb = new StringBuilder();
        for (PubActivity activity : PubActivity.values()) {
            boolean unlocked = milestones.isActivityUnlocked(activity);
            String requirement = milestones.activityRequirementText(activity);
            String status;
            if (unlocked && requirement == null) {
                status = "Unlocked";
            } else if (requirement == null) {
                status = "Available";
            } else {
                status = "Locked (" + requirement + ")";
            }
            sb.append("- ").append(activity.getLabel()).append(": ").append(status).append("\n");
        }
        return trimTrailingNewline(sb);
    }

    private String buildUnlockedActivityInfoText() {
        StringBuilder sb = new StringBuilder();
        for (PubActivity activity : PubActivity.values()) {
            MilestoneSystem.ActivityAvailability availability = milestones.getActivityAvailability(activity);
            if (!availability.unlocked()) continue;
            String effects = "traffic +" + (int)Math.round(activities.effectiveTrafficBonusPct(activity) * 100) + "%"
                    + ", rep " + (activity.getRepInstantDelta() >= 0 ? "+" : "") + activity.getRepInstantDelta();
            sb.append("- ").append(activity.getLabel())
                    .append(" | ").append(activityCategoryHint(activity))
                    .append(" | ").append(effects)
                    .append("\n");
        }
        if (sb.length() == 0) {
            sb.append("None unlocked yet.");
        }
        return trimTrailingNewline(sb);
    }

    private String buildMilestoneRewardSummaryText() {
        if (s.milestoneRewardLog.isEmpty()) {
            return "No recent milestone rewards.";
        }
        StringBuilder sb = new StringBuilder();
        for (String entry : s.milestoneRewardLog) {
            sb.append("- ").append(entry).append("\n");
        }
        return trimTrailingNewline(sb);
    }

    private String trimTrailingNewline(StringBuilder sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private String buildStaffTabSummary(int serveCap) {
        StringBuilder sb = new StringBuilder();
        int combinedCap = s.fohStaffCap + s.hohStaffCap + s.kitchenChefCap;
        int totalStaff = s.fohStaff.size() + s.bohStaff.size() + s.generalManagers.size();
        double tipRate = staff.tipRate();
        int kitchenCapacity = 0;
        for (Staff st : s.bohStaff) {
            kitchenCapacity += st.getKitchenCapacity();
        }

        sb.append("Total staff: ").append(totalStaff).append("/").append(combinedCap)
                .append(" (FOH ").append(s.fohStaffCount()).append("/").append(s.fohStaffCap)
                .append(", HOH ").append(s.hohStaffCount()).append("/").append(s.hohStaffCap)
                .append(", BOH ").append(s.bohStaff.size()).append("/").append(s.kitchenChefCap).append(")");
        sb.append("\nManager slots: ").append(s.managerPoolCount()).append("/").append(s.managerCap)
                .append(" (GM ").append(s.generalManagers.size())
                .append(", AM ").append(s.assistantManagerCount())
                .append(", DM ").append(s.dutyManagerCount()).append(")");
        sb.append("\nServe cap (total): ").append(serveCap);
        sb.append("\nKitchen capacity: ").append(kitchenCapacity);
        sb.append("\nTip rate from staff: ").append(String.format("%.1f%%", tipRate * 100));
        sb.append("\nSecurity bonus (staff): ").append(s.staffSecurityBonus());
        sb.append("\nChaos tolerance (weighted): ").append(String.format("%.1f", s.staffChaosCapacity()));
        sb.append("\nMisconduct reduction: ").append(String.format("%.0f%%", s.staffMisconductReductionPct * 100));
        sb.append("\nMorale: FOH ").append((int)Math.round(s.fohMorale))
                .append(" | BOH ").append((int)Math.round(s.bohMorale))
                .append(" | Team ").append((int)Math.round(s.teamMorale));
        sb.append("\nMisconduct (week): ").append(s.staffMisconductThisWeek);
        sb.append("\nLast staff incident: ").append(s.lastStaffIncidentSummary);
        sb.append("\nIncident drivers: ").append(s.lastStaffIncidentDrivers);
        sb.append("\nWages accrued: GBP ").append(fmt2(s.wagesAccruedThisWeek));
        sb.append("\nOperating cost (base): GBP ").append(fmt2(s.opCostBaseThisWeek));
        sb.append("\nOperating cost (staff): GBP ").append(fmt2(s.opCostStaffThisWeek));
        sb.append("\nOperating cost (skill): GBP ").append(fmt2(s.opCostSkillThisWeek));
        sb.append("\nOperating cost (occupancy): GBP ").append(fmt2(s.opCostOccupancyThisWeek));

        sb.append("\n\nManagers (GM):");
        if (s.generalManagers.isEmpty()) {
            sb.append("\n  (none)");
        } else {
            for (Staff st : s.generalManagers) {
                sb.append("\n  - ").append(st);
            }
        }

        sb.append("\n\nFOH roster:");
        if (s.fohStaff.isEmpty()) {
            sb.append("\n  (none)");
        } else {
            for (Staff st : s.fohStaff) {
                sb.append("\n  - ").append(st);
            }
        }

        sb.append("\n\nBOH roster:");
        if (s.bohStaff.isEmpty()) {
            sb.append("\n  (none)");
        } else {
            for (Staff st : s.bohStaff) {
                sb.append("\n  - ").append(st);
            }
        }

        return sb.toString();
    }

    private String wageEscalationWarning() {
        return switch (s.consecutiveMissedWagePayments) {
            case 0 -> "None";
            case 1 -> "Another miss will trigger mass walkouts.";
            case 2 -> "Another miss will cause a full collapse.";
            default -> "Collapsed. Stabilize wages to recover.";
        };
    }

    private String wageRecoveryGuidance() {
        if (!s.businessCollapsed) {
            return "Pay wages on time to rebuild trust.";
        }
        int remaining = Math.max(0, 3 - s.wagesPaidOnTimeWeeks);
        return "Pay wages on time for " + remaining + " more week(s) to stabilize.";
    }

    private String buildCreditLineSummary() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (CreditLine line : s.creditLines.getOpenLines()) {
            sb.append("- ").append(line.getLenderName())
                    .append(" | bal ").append(fmt2(line.getBalance()))
                    .append(" / ").append(fmt2(line.getLimit()))
                    .append(" | weekly ").append(fmt2(line.getWeeklyPayment()))
                    .append(" | APR ").append(String.format("%.1f", line.getInterestAPR() * 100)).append("%")
                    .append(" | missed ").append(line.getMissedPaymentCount())
                    .append("\n");
            count++;
        }
        if (count == 0) return "None";
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void rollReport() {
        double profit = s.reportRevenue - s.reportCosts;

        double rentC = s.reportCost(CostTag.RENT);
        double wagesC = s.reportCost(CostTag.WAGES);
        double opC = s.reportCost(CostTag.OPERATING);
        double foodC = s.reportCost(CostTag.FOOD);
        double supplierC = s.reportCost(CostTag.SUPPLIER);
        double upC = s.reportCost(CostTag.UPGRADE);
        double actC = s.reportCost(CostTag.ACTIVITY);
        double bounC = s.reportCost(CostTag.BOUNCER);
        double eventC = s.reportCost(CostTag.EVENT);
        double interestC = s.reportCost(CostTag.INTEREST);
        double securityC = s.reportCost(CostTag.SECURITY);

        if (profit < 0) {
            s.consecutiveDebtReports++;
            log.neg(" Report was LOSS-making. Streak: " + s.consecutiveDebtReports);
        } else {
            s.consecutiveDebtReports = 0;
        }

        log.header(" REPORT #" + s.reportIndex + " CLOSED");
        log.info("Profit GBP " + String.format("%.0f", profit)
                + " | Rev GBP " + String.format("%.0f", s.reportRevenue)
                + " | Costs GBP " + String.format("%.0f", s.reportCosts)
                + " | Sales " + s.reportSales
                + " | Events " + s.reportEvents);

        log.info("Costs breakdown: Rent GBP " + String.format("%.0f", rentC)
                + " | Wages GBP " + String.format("%.0f", wagesC)
                + " | Ops GBP " + String.format("%.0f", opC)
                + " | Food GBP " + String.format("%.0f", foodC)
                + " | Security GBP " + String.format("%.0f", securityC)
                + " | Stock GBP " + String.format("%.0f", supplierC)
                + " | Upgrades GBP " + String.format("%.0f", upC)
                + " | Activities GBP " + String.format("%.0f", actC)
                + " | Bouncer GBP " + String.format("%.0f", bounC)
                + " | Events GBP " + String.format("%.0f", eventC)
                + " | Interest GBP " + String.format("%.0f", interestC));

        s.fourWeekReportText = ReportSystem.buildFourWeekSummary(s);
        s.fourWeekReportReady = true;

        s.reportIndex++;
        s.weeksIntoReport = 0;

        s.reportRevenue = 0;
        s.reportCosts = 0;
        s.reportSales = 0;
        s.reportEvents = 0;
        s.reportRefundTotal = 0.0;
        s.reportCostBreakdown.clear();
        s.opCostBaseThisWeek = 0.0;
        s.opCostStaffThisWeek = 0.0;
        s.opCostSkillThisWeek = 0.0;
        s.opCostOccupancyThisWeek = 0.0;

        s.reportStartCash = s.cash;
        s.reportStartDebt = s.totalCreditBalance();

        log.header(" NEW REPORT #" + s.reportIndex);
        log.info("Report window: 4 weeks. Stay profitable, stay vaguely legal.");
    }

    private void handleFoodSales(int barCount, int security) {
        if (!s.kitchenUnlocked) return;
        if (s.foodRack.count() <= 0) return;
        if (kitchenCapacity() <= 0) {
            log.info("Kitchen idle: no kitchen staff available.");
            return;
        }

        int maxBuyers = Math.min(barCount, Math.min(s.foodRack.count(), kitchenCapacity()));
        if (maxBuyers <= 0) return;

        int buyers = Math.min(maxBuyers, Math.max(0, (int)Math.round(barCount * 0.30 + s.random.nextInt(3) - 1)));
        if (buyers <= 0) return;

        for (int i = 0; i < buyers; i++) {
            Food food = s.foodRack.pickRandomFood(s.random);
            if (food == null) break;

            double price = s.foodRack.getSellPrice(food, s.kitchenQualityBonus);
            s.foodRack.removeFood(food);
            int prepRounds = Math.max(1, s.foodPrepRounds);
            s.pendingFoodOrders.add(new FoodOrder(-1, "Walk-in", food, price, s.roundInNight + prepRounds));

            eco.addCash(price, "Meal order: " + food.getName());
            s.reportRevenue += price;
            s.nightRevenue += price;
            s.reportSales++;
            s.nightSales++;
            s.nightItemSales.merge("Food: " + food.getName(), 1, Integer::sum);
            s.recordRoundSale("Food", food.getName());
            s.recordFoodQuality(food);
        }
    }

    private void handleStaffMisconduct(int security, StaffSystem.WorkloadProfile workloadProfile) {
        List<Staff> eligible = new java.util.ArrayList<>();
        eligible.addAll(s.fohStaff);
        eligible.addAll(s.bohStaff);
        if (eligible.isEmpty()) return;

        int minMorale = 100;
        for (Staff st : eligible) {
            minMorale = Math.min(minMorale, st.getMorale());
        }

        double chance = computeMisconductChance(security, minMorale, workloadProfile);
        if (s.random.nextDouble() > chance) return;

        Staff offender = pickMisconductOffender(eligible);
        if (offender == null) return;

        boolean boh = s.bohStaff.contains(offender);
        MisconductType type = boh ? rollBohMisconduct() : rollFohMisconduct();
        s.staffMisconductThisWeek++;
        s.staffIncidentThisNight = true;
        s.staffIncidentThisRound = true;

        String driverLine = buildMisconductDriverLine(security, minMorale, chance, workloadProfile);
        String dept = boh ? "BOH" : "FOH";
        switch (type) {
            case FREE_DRINKS -> {
                double loss = 8 + s.random.nextInt(14);
                double paidLoss = applyMisconductLoss(loss, CostTag.OTHER);
                String detail = pickPhrase(FOH_FREE_DRINKS_LINES);
                log.popup(new EventCard("Staff misconduct",
                        "<b>" + offender.getName() + "</b> " + detail,
                        0, -paidLoss, 0, "THEFT"));
                addRumorHeat(Rumor.STAFF_STEALING, 10, RumorSource.STAFF);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") comped drinks. Cash -" + money0(paidLoss);
            }
            case TILL_SHORT -> {
                double loss = 12 + s.random.nextInt(18);
                double paidLoss = applyMisconductLoss(loss, CostTag.OTHER);
                String detail = pickPhrase(FOH_TILL_SHORT_LINES);
                log.popup(new EventCard("Staff misconduct",
                        "<b>" + offender.getName() + "</b> " + detail,
                        0, -paidLoss, 0, "THEFT"));
                addRumorHeat(Rumor.STAFF_STEALING, 12, RumorSource.STAFF);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") left the till short. Cash -" + money0(paidLoss);
            }
            case MANAGEMENT_INSULT -> {
                eco.applyRep(-2, "Management insult");
                s.betweenNightChaos += 1.5;
                String detail = pickPhrase(FOH_MANAGEMENT_INSULT_LINES);
                log.popup(new EventCard("Staff misconduct",
                        "<b>" + offender.getName() + "</b> " + detail,
                        -2, 0, 0, "DISRUPTION"));
                addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 8, RumorSource.STAFF);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") clashed with management. Rep -2";
            }
            case FLIRTING_ATTENTION -> {
                double tipsBoost = 4 + s.random.nextInt(6);
                s.tipsThisWeek += tipsBoost;
                eco.applyRep(1, "Friendly service");
                String detail = pickPhrase(FOH_FLIRTING_LINES);
                log.popup(new EventCard("Staff misconduct",
                        "<b>" + offender.getName() + "</b> " + detail,
                        1, 0, 0, "CHARM"));
                if (s.random.nextInt(100) < 35) {
                    addRumorHeat(Rumor.FRIENDLY_STAFF, 8, RumorSource.PUNTER);
                }
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") charmed guests. Rep +1, tips +" + money0(tipsBoost);
            }
            case COMPROMISING_BREAK -> {
                offender.adjustMorale(-2);
                staff.updateTeamMorale();
                eco.applyRep(-1, "Staff disappeared");
                String detail = pickPhrase(FOH_COMPROMISING_LINES);
                log.popup(new EventCard("Staff misconduct",
                        "<b>" + offender.getName() + "</b> " + detail,
                        -1, 0, 0, "ABSENT"));
                addRumorHeat(Rumor.SLOW_SERVICE, 6, RumorSource.PUNTER);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") took a long break. Rep -1";
            }
            case INGREDIENTS_MISSING -> {
                boolean kitchenActive = s.kitchenUnlocked;
                int removed = drainKitchenStock(2 + s.random.nextInt(3));
                double cost = removed > 0 ? 0.0 : (10 + s.random.nextInt(12));
                double paidLoss = removed <= 0 ? applyMisconductLoss(cost, CostTag.FOOD) : 0.0;
                String detail = pickPhrase(BOH_INGREDIENTS_LINES);
                log.popup(new EventCard("Kitchen incident",
                        "<b>" + offender.getName() + "</b> " + detail,
                        0, -paidLoss, 0, "STOCK"));
                if (removed > 0) {
                    s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") miscounted prep. Stock -" + removed;
                } else {
                    String note = kitchenActive ? "Stock 0" : "Stock impact N/A";
                    s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") lost ingredients. " + note + ", cost -" + money0(paidLoss);
                }
                addRumorHeat(Rumor.FOOD_POISONING_SCARE, 6, RumorSource.STAFF);
            }
            case HYGIENE_SLIP -> {
                double cost = 8 + s.random.nextInt(12);
                double paidLoss = applyMisconductLoss(cost, CostTag.FOOD);
                eco.applyRep(-2, "Hygiene slip");
                s.nightFoodUnserved++;
                String detail = pickPhrase(BOH_HYGIENE_LINES);
                log.popup(new EventCard("Kitchen incident",
                        "<b>" + offender.getName() + "</b> " + detail,
                        -2, -paidLoss, 0, "HYGIENE"));
                addRumorHeat(Rumor.FOOD_POISONING_SCARE, 10, RumorSource.PUNTER);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") hygiene slip. Rep -2, cost -" + money0(paidLoss);
            }
            case WASTED_BATCH -> {
                boolean kitchenActive = s.kitchenUnlocked;
                int removed = drainKitchenStock(2 + s.random.nextInt(4));
                double cost = removed > 0 ? 0.0 : (12 + s.random.nextInt(16));
                double paidLoss = removed <= 0 ? applyMisconductLoss(cost, CostTag.FOOD) : 0.0;
                s.nightFoodUnserved += 1;
                String detail = pickPhrase(BOH_WASTED_BATCH_LINES);
                log.popup(new EventCard("Kitchen incident",
                        "<b>" + offender.getName() + "</b> " + detail,
                        0, -paidLoss, 0, "WASTE"));
                if (removed > 0) {
                    s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") wasted a batch. Stock -" + removed;
                } else {
                    String note = kitchenActive ? "Stock 0" : "Stock impact N/A";
                    s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") wasted a batch. " + note + ", cost -" + money0(paidLoss);
                }
                addRumorHeat(Rumor.SLOW_SERVICE, 6, RumorSource.PUNTER);
            }
            case KITCHEN_ARGUMENT -> {
                for (Staff st : s.bohStaff) st.adjustMorale(-2);
                staff.updateTeamMorale();
                s.betweenNightChaos += 2.5;
                String detail = pickPhrase(BOH_ARGUMENT_LINES);
                log.popup(new EventCard("Kitchen incident",
                        "<b>" + offender.getName() + "</b> " + detail,
                        0, 0, 0, "ARGUMENT"));
                addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 6, RumorSource.STAFF);
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") sparked a kitchen argument. BOH morale -2";
            }
            case KITCHEN_HERO -> {
                offender.adjustMorale(2);
                staff.updateTeamMorale();
                eco.applyRep(1, "Kitchen hero");
                String detail = pickPhrase(BOH_HERO_LINES);
                log.popup(new EventCard("Kitchen incident",
                        "<b>" + offender.getName() + "</b> " + detail,
                        1, 0, 0, "HERO"));
                if (s.random.nextInt(100) < 50) {
                    addRumorHeat(Rumor.BEST_SUNDAY_ROAST, 8, RumorSource.PUNTER);
                }
                s.lastStaffIncidentSummary = offender.getName() + " (" + dept + ") saved the line. Rep +1";
            }
        }

        s.lastStaffIncidentDrivers = driverLine;
    }

    private double computeMisconductChance(int security, int minMorale, StaffSystem.WorkloadProfile workloadProfile) {
        double chance = 0.04;
        if (s.teamMorale < 55) {
            chance += (55 - s.teamMorale) * 0.002;
        }
        if (minMorale < 35) {
            chance += (35 - minMorale) * 0.003;
        }
        chance += Math.min(0.12, s.chaos * 0.0018);
        if (s.activeRumors.containsKey(Rumor.STAFF_STEALING)) {
            chance += 0.02;
        }
        if (s.activeRumors.containsKey(Rumor.SLOW_SERVICE)) {
            chance += 0.01;
        }
        double securityReduction = Math.min(0.45, security * 0.04);
        chance *= (1.0 - securityReduction);
        chance *= (1.0 - s.staffMisconductReductionPct);
        chance *= staff.misconductPressureMultiplier(workloadProfile);
        chance *= s.debtSpiralMisconductChanceMultiplier;
        return Math.max(0.01, Math.min(0.30, chance));
    }

    private Staff pickMisconductOffender(List<Staff> eligible) {
        // Weighted toward lower morale so stressed staff are more likely to slip.
        double totalWeight = 0.0;
        for (Staff st : eligible) {
            double weight = Math.max(1.0, 101 - st.getMorale());
            totalWeight += weight;
        }
        double roll = s.random.nextDouble() * totalWeight;
        for (Staff st : eligible) {
            roll -= Math.max(1.0, 101 - st.getMorale());
            if (roll <= 0) return st;
        }
        return eligible.get(0);
    }

    private MisconductType rollFohMisconduct() {
        int roll = s.random.nextInt(100);
        if (roll < 24) return MisconductType.FREE_DRINKS;
        if (roll < 46) return MisconductType.TILL_SHORT;
        if (roll < 64) return MisconductType.MANAGEMENT_INSULT;
        if (roll < 82) return MisconductType.FLIRTING_ATTENTION;
        return MisconductType.COMPROMISING_BREAK;
    }

    private MisconductType rollBohMisconduct() {
        int roll = s.random.nextInt(100);
        if (roll < 26) return MisconductType.INGREDIENTS_MISSING;
        if (roll < 50) return MisconductType.HYGIENE_SLIP;
        if (roll < 74) return MisconductType.WASTED_BATCH;
        if (roll < 92) return MisconductType.KITCHEN_ARGUMENT;
        return MisconductType.KITCHEN_HERO;
    }

    private int drainKitchenStock(int max) {
        if (!s.kitchenUnlocked || s.foodRack.count() <= 0) return 0;
        int removed = 0;
        for (int i = 0; i < max; i++) {
            Food food = s.foodRack.pickRandomFood(s.random);
            if (food == null) break;
            if (s.foodRack.removeFood(food)) removed++;
        }
        return removed;
    }

    private double applyMisconductLoss(double loss, CostTag tag) {
        if (!eco.tryPay(loss, TransactionType.OTHER, "Staff misconduct losses", tag)) {
            return 0.0;
        }
        return loss;
    }

    private String buildMisconductDriverLine(int security, int minMorale, double chance, StaffSystem.WorkloadProfile workloadProfile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chance ").append(String.format("%.1f%%", chance * 100));
        if (s.teamMorale < 55) sb.append(" | morale low");
        if (minMorale < 35) sb.append(" | very low individual morale");
        if (s.chaos >= 35) sb.append(" | chaos high");
        if (security > 0) sb.append(" | security mitigated");
        if (s.staffMisconductReductionPct > 0.001) sb.append(" | upgrades mitigated");
        if (workloadProfile != null && workloadProfile.workload() > 1.0) sb.append(" | overloaded floor");
        if (s.activeRumors.containsKey(Rumor.STAFF_STEALING) || s.activeRumors.containsKey(Rumor.SLOW_SERVICE)) {
            sb.append(" | staff rumor pressure");
        }
        sb.append(" | ").append(pickPhrase(MISCONDUCT_DRIVER_LINES));
        return sb.toString();
    }

    private void checkHighRepScandal() {
        if (!s.nightOpen) return;
        if (s.reputation > 85) {
            s.consecutiveHighRepRounds++;
        } else {
            s.consecutiveHighRepRounds = 0;
        }

        if (s.consecutiveHighRepRounds >= 5) {
            events.triggerHighRepScandal();
            s.consecutiveHighRepRounds = 0;
        }
    }

    private void markReportStartIfMissing() {
        if (s.reportStartCash == 0 && s.reportStartDebt == 0) {
            s.reportStartCash = s.cash;
            s.reportStartDebt = s.totalCreditBalance();
        }
    }

    private void handleWagesPaid() {
        s.wagesPaidLastWeek = true;
        s.consecutiveMissedWagePayments = 0;
        s.wagesPaidOnTimeWeeks++;
        if (s.wagesPaidOnTimeWeeks >= 2) {
            s.creditScore = s.clampCreditScore(s.creditScore + 3);
        }
        if (s.supplierTrustPenalty > 0.0) {
            s.supplierTrustPenalty = Math.max(0.0, s.supplierTrustPenalty - 0.01);
        }
        if (s.businessCollapsed) {
            int totalStaff = totalStaffCount();
            if (totalStaff > 0 && s.wagesPaidOnTimeWeeks >= 3) {
                s.businessCollapsed = false;
                if (s.bankruptcyLockWeeksRemaining <= 0) {
                    s.banksLocked = false;
                    log.pos("Business stability improving. Banks are willing to talk again.");
                }
            }
        }
    }

    private void handleWageMiss() {
        s.wagesPaidLastWeek = false;
        s.wagesPaidOnTimeWeeks = 0;
        s.consecutiveMissedWagePayments++;
        applyWageMissEffects(s.consecutiveMissedWagePayments);
    }

    private void applyWageMissEffects(int level) {
        int totalStaff = totalStaffCount();
        if (level <= 0 || totalStaff == 0) return;

        switch (level) {
            case 1 -> {
                adjustAllStaffMorale(-25);
                int walkouts = Math.min(Math.max(0, totalStaff - 1), 1 + s.random.nextInt(2));
                int removed = removeLowestMoraleStaff(walkouts);
                eco.applyRep(-12, "Missed wages");
                s.creditScore = s.clampCreditScore(s.creditScore - 90);
                s.chaos += 12.0;
                s.wageTrafficPenaltyMultiplier = 0.75;
                s.wageTrafficPenaltyRounds = Math.max(s.wageTrafficPenaltyRounds, 6);
                adjustSupplierTrustPenalty(0.03);
                addRumorHeat(Rumor.SLOW_SERVICE, 6, RumorSource.EVENT);
                addRumorHeat(Rumor.STAFF_STEALING, 4, RumorSource.EVENT);
                log.neg("Staff wages missed. Trust shattered.");
                if (removed > 0) log.neg(removed + " staff walked out after wages were missed.");
            }
            case 2 -> {
                adjustAllStaffMorale(-35);
                int walkouts = Math.min(Math.max(0, totalStaff - 1), Math.max(2, totalStaff / 2));
                int removed = removeLowestMoraleStaff(walkouts);
                s.wageServePenaltyPct = 0.25;
                s.wageServePenaltyWeeks = Math.max(s.wageServePenaltyWeeks, 2);
                eco.applyRep(-22, "Wages missed again");
                s.creditScore = s.clampCreditScore(s.creditScore - 140);
                s.chaos += 20.0;
                s.wageTrafficPenaltyMultiplier = 0.55;
                s.wageTrafficPenaltyRounds = Math.max(s.wageTrafficPenaltyRounds, 10);
                adjustSupplierTrustPenalty(0.06);
                addRumorHeat(Rumor.SLOW_SERVICE, 10, RumorSource.EVENT);
                addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 6, RumorSource.EVENT);
                log.neg("Wages missed again. Staff feel betrayed.");
                if (removed > 0) log.neg("Major walkouts: " + removed + " staff left.");
            }
            default -> {
                adjustAllStaffMorale(-45);
                int walkouts = Math.max(0, totalStaff - 1);
                int removed = removeLowestMoraleStaff(walkouts);
                s.wageServePenaltyPct = 0.45;
                s.wageServePenaltyWeeks = Math.max(s.wageServePenaltyWeeks, 3);
                eco.applyRep(-35, "Wages collapse");
                s.creditScore = s.clampCreditScore(s.creditScore - 200);
                s.chaos += 30.0;
                s.wageTrafficPenaltyMultiplier = 0.35;
                s.wageTrafficPenaltyRounds = Math.max(s.wageTrafficPenaltyRounds, 14);
                adjustSupplierTrustPenalty(0.10);
                s.banksLocked = true;
                s.businessCollapsed = true;
                addRumorHeat(Rumor.DODGY_LATE_NIGHTS, 12, RumorSource.EVENT);
                addRumorHeat(Rumor.STAFF_STEALING, 8, RumorSource.EVENT);
                log.neg("Third missed wages. The business staggers under a full collapse.");
                if (removed > 0) log.neg("Mass walkout: " + removed + " staff left.");
            }
        }
    }

    private int totalStaffCount() {
        return s.fohStaff.size() + s.bohStaff.size() + s.generalManagers.size();
    }

    private int removeLowestMoraleStaff(int count) {
        int removed = 0;
        for (int i = 0; i < count; i++) {
            Staff lowest = null;
            java.util.List<Staff> source = null;
            for (Staff st : s.fohStaff) {
                if (lowest == null || st.getMorale() < lowest.getMorale()) {
                    lowest = st;
                    source = s.fohStaff;
                }
            }
            for (Staff st : s.bohStaff) {
                if (lowest == null || st.getMorale() < lowest.getMorale()) {
                    lowest = st;
                    source = s.bohStaff;
                }
            }
            for (Staff st : s.generalManagers) {
                if (lowest == null || st.getMorale() < lowest.getMorale()) {
                    lowest = st;
                    source = s.generalManagers;
                }
            }
            if (lowest == null || source == null) break;
            source.remove(lowest);
            s.staffDeparturesThisWeek++;
            removed++;
        }
        if (removed > 0) staff.updateTeamMorale();
        return removed;
    }

    private void adjustSupplierTrustPenalty(double delta) {
        if (delta == 0.0) return;
        s.supplierTrustPenalty = Math.max(0.0, Math.min(0.30, s.supplierTrustPenalty + delta));
    }

    private void processSharkThreatWeekly() {
        int currentTier = s.sharkThreatTier;
        int targetTier = currentTier;

        if (s.sharkMissedPaymentThisWeek) {
            s.sharkConsecutiveMisses++;
            s.sharkCleanWeeks = 0;
            targetTier = Math.max(targetTier, Math.min(4, s.sharkConsecutiveMisses));
            s.sharkThreatTrigger = "Missed shark repayment";
        } else if (s.sharkHasBalanceThisWeek && s.sharkPaidOnTimeThisWeek) {
            s.sharkCleanWeeks++;
            if (s.sharkCleanWeeks >= 1 && targetTier > 0) {
                targetTier = Math.max(0, targetTier - 1);
                s.sharkThreatTrigger = "Clean week";
            }
        } else if (!s.sharkHasBalanceThisWeek) {
            if (targetTier > 0) {
                targetTier = Math.max(0, targetTier - 2);
                s.sharkThreatTrigger = "Shark balance cleared";
            }
            s.sharkConsecutiveMisses = 0;
            s.sharkCleanWeeks = 0;
        }

        if (targetTier != currentTier) {
            s.sharkThreatTier = targetTier;
            if (targetTier > currentTier) {
                log.neg("Loan shark threat escalated to Tier " + targetTier + ": " + sharkTierLabel(targetTier));
                applySharkTierEffects(targetTier);
            } else {
                log.pos("Loan shark threat reduced to Tier " + targetTier + ": " + sharkTierLabel(targetTier));
            }
        }
    }

    private void reduceSharkThreat(int amount, String reason) {
        if (amount <= 0) return;
        int before = s.sharkThreatTier;
        s.sharkThreatTier = Math.max(0, s.sharkThreatTier - amount);
        s.sharkConsecutiveMisses = 0;
        s.sharkCleanWeeks = 0;
        if (s.sharkThreatTier != before) {
            log.pos("Loan shark threat reduced to Tier " + s.sharkThreatTier
                    + ": " + sharkTierLabel(s.sharkThreatTier) + " (" + reason + ")");
        }
    }

    private void applySharkTierEffects(int tier) {
        String phrase = randomSharkPhrase(tier);
        switch (tier) {
            case 1 -> {
                s.chaos += 2.0;
                adjustAllStaffMorale(-1);
                eco.applyRep(-1, "Loan shark warning");
                log.event(" " + phrase);
            }
            case 2 -> {
                s.chaos += 5.0;
                adjustAllStaffMorale(-2);
                eco.applyRep(-2, "Loan shark collectors");
                eco.tryPay(18.0, TransactionType.REPAIR, "Collection fee", CostTag.EVENT);
                log.event(" " + phrase);
            }
            case 3 -> {
                s.chaos += 8.0;
                adjustAllStaffMorale(-3);
                eco.applyRep(-4, "Loan shark damage");
                eco.tryPay(45.0, TransactionType.REPAIR, "Repairs (damage incident)", CostTag.EVENT);
                log.event(" " + phrase);
            }
            case 4 -> {
                s.chaos += 12.0;
                adjustAllStaffMorale(-5);
                eco.applyRep(-6, "Loan shark blitz");
                s.creditScore = s.clampCreditScore(s.creditScore - 40);
                eco.tryPay(80.0, TransactionType.REPAIR, "Repairs (full blitz)", CostTag.EVENT);
                log.event(" " + phrase);
            }
            default -> {
            }
        }
    }

    private void adjustAllStaffMorale(int delta) {
        for (Staff st : s.fohStaff) st.adjustMorale(delta);
        for (Staff st : s.bohStaff) st.adjustMorale(delta);
        for (Staff st : s.generalManagers) st.adjustMorale(delta);
        staff.updateTeamMorale();
    }

    public String sharkTierLabel(int tier) {
        return switch (tier) {
            case 1 -> "Warning";
            case 2 -> "Collectors";
            case 3 -> "Damage";
            case 4 -> "Blitz";
            default -> "None";
        };
    }

    private String buildLoanSummaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total debt: GBP ").append(fmt2(s.totalCreditBalance()))
                .append(" / Limit ").append(fmt2(s.totalCreditLimit()))
                .append(" | Weekly due ").append(fmt2(s.totalCreditWeeklyPaymentDue()))
                .append("\n\n");
        sb.append("Credit Lines:\n");
        if (s.creditLines.getOpenLines().isEmpty()) {
            sb.append("  None\n");
        } else {
            for (CreditLine line : s.creditLines.getOpenLines()) {
                sb.append("  ").append(line.getLenderName())
                        .append(" | Limit ").append(fmt2(line.getLimit()))
                        .append(" | Balance ").append(fmt2(line.getBalance()))
                        .append(" | Weekly ").append(fmt2(line.getWeeklyPayment()))
                        .append(" | APR ").append(String.format("%.2f", line.getInterestAPR() * 100)).append("%")
                        .append(" | Missed ").append(line.getMissedPaymentCount())
                        .append("\n");
            }
        }

        sb.append("\nLoan Shark Threat:\n");
        if (s.loanShark.isOpen()) {
            sb.append("  Balance ").append(fmt2(s.loanShark.getBalance()))
                    .append(" | Weekly due ").append(fmt2(s.loanShark.minPaymentDue()))
                    .append(" | APR ").append(String.format("%.2f", s.loanShark.getApr() * 100)).append("%\n");
        } else {
            sb.append("  No loan shark debt.\n");
        }
        sb.append("  Tier ").append(s.sharkThreatTier)
                .append(" (").append(sharkTierLabel(s.sharkThreatTier)).append(")")
                .append(" | Trigger: ").append(s.sharkThreatTrigger)
                .append(" | Misses: ").append(s.sharkConsecutiveMisses).append("\n");
        sb.append("  Reduce by clean weeks or paying shark debt in full.\n");

        return sb.toString();
    }

    public String bankruptcyConsequencesText() {
        return "Declare Bankruptcy consequences:\n"
                + "- Remove ALL installed upgrades.\n"
                + "- Reset pub level to 0 and prestige progression to zero-state.\n"
                + "- Credit score reset to 0, banks locked for a long period.\n"
                + "- Supplier trust set to minimum with bankruptcy stigma.\n"
                + "- Supplier invoice credit cap set to GBP 400 (not cash).\n"
                + "- Reputation becomes harder to improve and easier to lose.\n"
                + "- Hiring pool worsens; instability rises.\n"
                + "- Loan sharks stay available but on harsher terms.";
    }

    private String buildFinanceBankingText(double creditBalance, double creditWeeklyDue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cash: GBP ").append(fmt2(s.cash)).append("\n");
        sb.append("Credit score: ").append(s.creditScore)
                .append(" | Utilisation: ").append(String.format("%.0f%%", s.creditUtilization * 100)).append("\n");
        sb.append("Total bank debt: GBP ").append(fmt2(creditBalance)).append(" / ")
                .append(fmt2(s.totalCreditLimit())).append("\n");
        sb.append("Weekly bank repayments due: GBP ").append(fmt2(creditWeeklyDue)).append("\n");
        sb.append("Consecutive missed-min weeks: ").append(s.consecutiveWeeksUnpaidMin)
                .append(" | Debt spiral tier: ").append(s.debtSpiralTier).append("\n");
        sb.append("Debt spiral modifiers: Interest x").append(fmt2(s.debtSpiralInterestMultiplier))
                .append(" | Late fees x").append(fmt2(s.debtSpiralLateFeeMultiplier))
                .append(" | Supplier trust x").append(fmt2(s.debtSpiralSupplierTrustMultiplier)).append("\n");
        sb.append("Rep bias: negative x").append(fmt2(s.debtSpiralNegativeRepMultiplier))
                .append(" | positive x").append(fmt2(s.debtSpiralPositiveRepMultiplier))
                .append(" | Staff risk x").append(fmt2(s.debtSpiralMisconductChanceMultiplier)).append("\n");
        sb.append("Bailiff rule: Bailiffs on 4th consecutive missed-min week.");
        if (s.consecutiveWeeksUnpaidMin == BAILIFF_THRESHOLD_WEEKS) {
            sb.append(" WARNING: miss minimums again and bailiffs arrive this week.");
        }
        sb.append("\n");
        if (s.loanShark.isOpen()) {
            sb.append("Loan shark balance: GBP ").append(fmt2(s.loanShark.getBalance()))
                    .append(" | Weekly due ").append(fmt2(s.loanShark.minPaymentDue()))
                    .append(" | APR ").append(String.format("%.2f", s.loanShark.getApr() * 100)).append("%\n");
        } else {
            sb.append("Loan shark: None (still available; harsher if bankrupt)\n");
        }
        if (s.bankruptcyLockWeeksRemaining > 0) {
            sb.append("Bankruptcy bank lock: ").append(s.bankruptcyLockWeeksRemaining).append(" week(s) remaining.\n");
        }
        sb.append("Supplier invoice credit cap: GBP ").append(fmt2(s.supplierCreditCap()));
        if (s.bankruptcyDeclared) sb.append(" (bankruptcy cap enforced)");
        sb.append("\n\nCredit lines:\n");
        for (CreditLine line : s.creditLines.getOpenLines()) {
            sb.append("  ").append(line.getLenderName())
                    .append(" | Balance ").append(fmt2(line.getBalance()))
                    .append(" / Limit ").append(fmt2(line.getLimit()))
                    .append(" | Weekly ").append(fmt2(line.getWeeklyPayment()))
                    .append(" | APR ").append(String.format("%.2f", line.getInterestAPR() * 100)).append("%")
                    .append(" | Penalty ").append(String.format("%.2f", line.getPenaltyAddOnApr() * 100)).append("%\n");
        }
        if (s.creditLines.getOpenLines().isEmpty()) sb.append("  None\n");
        return sb.toString();
    }

    private String buildPaydayDetailText() {
        StringBuilder sb = new StringBuilder();
        double minTotal = 0.0;
        double fullTotal = 0.0;
        for (PaydayBill bill : s.paydayBills) {
            minTotal += bill.getMinDue();
            fullTotal += bill.getFullDue();
        }
        sb.append("Weekly minimum due: GBP ").append(fmt2(minTotal)).append("\n");
        sb.append("Weekly full due:    GBP ").append(fmt2(fullTotal)).append("\n");
        sb.append("Last resolution: min ").append(s.metMinimumsLastWeek ? "MET" : "MISSED")
                .append(" | streak ").append(s.consecutiveWeeksUnpaidMin)
                .append(" | tier ").append(s.debtSpiralTier)
                .append(" | min due GBP ").append(fmt2(s.weeklyTotalMinDueLastResolution))
                .append(" | total due GBP ").append(fmt2(s.weeklyTotalDueLastResolution)).append("\n");
        sb.append("Bills due:\n");
        if (s.paydayBills.isEmpty()) {
            sb.append("  None\n");
        } else {
            for (PaydayBill bill : s.paydayBills) {
                sb.append("  ").append(bill.getDisplayName())
                        .append(" | Min ").append(fmt2(bill.getMinDue()))
                        .append(" | Full ").append(fmt2(bill.getFullDue()))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private String buildSuppliersDetailText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Supplier trust: ").append(s.supplierTrustLabel()).append("\n");
        sb.append("Supplier trust pressure mult: x").append(fmt2(s.debtSpiralSupplierTrustMultiplier)).append("\n");
        sb.append("Supplier credit cap: GBP ").append(fmt2(s.supplierCreditCap())).append("\n");
        sb.append("Price multiplier: x").append(fmt2(s.supplierPriceMultiplier())).append("\n");
        sb.append("Wine supplier balance: GBP ").append(fmt2(s.supplierWineCredit.getBalance()))
                .append(" | Min due ").append(fmt2(s.supplierWineMinDue()))
                .append(" | Penalty APR ").append(String.format("%.2f", s.supplierWineCredit.getPenaltyAddOnApr() * 100)).append("%\n");
        sb.append("Food supplier balance: GBP ").append(fmt2(s.supplierFoodCredit.getBalance()))
                .append(" | Min due ").append(fmt2(s.supplierFoodMinDue()))
                .append(" | Penalty APR ").append(String.format("%.2f", s.supplierFoodCredit.getPenaltyAddOnApr() * 100)).append("%\n");
        return sb.toString();
    }

    private String buildProgressionDetailText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pub level: ").append(s.pubLevel).append("\n");
        sb.append("Weeks active: ").append(s.weekCount).append("\n");
        sb.append("Milestones achieved: ").append(s.achievedMilestones.size()).append("\n");
        sb.append(pubLevelSystem.progressionSummary(s)).append("\n\n");
        sb.append("Milestone ladder\n").append(milestones.milestoneProgressReport()).append("\n");
        if (FeatureFlags.FEATURE_RIVALS) {
            sb.append("\nDistrict update\n");
            sb.append(s.rivalDistrictUpdate).append("\n");
            sb.append("Dominant rival stance: ").append(s.latestMarketPressure.dominantStance()).append("\n");
        }
        return sb.toString();
    }

    private String buildSecurityDetailText(int sec) {
        SecuritySystem.SecurityBreakdown breakdown = security.breakdown();
        StringBuilder sb = new StringBuilder();
        sb.append("Security policy: ").append(s.securityPolicy != null ? s.securityPolicy.getLabel() : "Balanced").append("\n");
        sb.append("Base security level: ").append(s.baseSecurityLevel)
                .append(" (+").append(s.legacy.baseSecurityBonus).append(" legacy)\n");
        sb.append("Effective security: ").append(sec).append("\n");
        sb.append("Incident chance mult: x").append(fmt2(incidentChanceMultiplier(sec))).append("\n");
        sb.append("Security task: ").append(securityTaskStatusLine()).append("\n");
        sb.append("Bouncers hired: ").append(s.bouncersHiredTonight).append("/").append(s.bouncerCap).append("\n");
        sb.append("Bouncer quality: ").append(s.bouncerQualitySummary()).append("\n");
        sb.append("Bouncer rep mitigation: x").append(fmt2(s.bouncerRepDamageMultiplier())).append("\n");
        sb.append("CCTV rep mitigation: ").append((int)Math.round(s.cctvRepMitigationPct() * 100)).append("%\n");
        sb.append("Upgrade rep mitigation: ").append((int)Math.round(s.upgradeRepMitigationPct * 100)).append("%\n");
        sb.append("Combined rep mitigation: x").append(fmt2(s.securityIncidentRepMultiplier())).append("\n");
        sb.append("Bouncer reductions: theft ").append(pct(s.bouncerTheftReduction))
                .append(" | negative ").append(pct(s.bouncerNegReduction))
                .append(" | fights ").append(pct(s.bouncerFightReduction)).append("\n");
        sb.append("Chaos total: ").append(fmt1(s.chaos)).append("\n");
        sb.append("Chaos delta (round): ").append(fmt1(s.lastChaosDelta)).append("\n");
        sb.append("Chaos avg (week): ")
                .append(fmt1(s.weekChaosRounds > 0 ? (s.weekChaosTotal / s.weekChaosRounds) : s.chaos)).append("\n");
        sb.append("Chaos streaks: good ").append(s.posStreak).append(" | bad ").append(s.negStreak).append("\n");
        sb.append("Chaos constants: bad ").append(fmt1(CHAOS_BAD_DELTA_1)).append("/")
                .append(fmt1(CHAOS_BAD_DELTA_2)).append("/").append(fmt1(CHAOS_BAD_DELTA_3))
                .append(" | good ").append(fmt1(CHAOS_GOOD_DELTA_1)).append("/")
                .append(fmt1(CHAOS_GOOD_DELTA_2)).append("/").append(fmt1(CHAOS_GOOD_DELTA_3))
                .append(" | cap ").append(CHAOS_STREAK_CAP).append("\n");
        sb.append("Early close formula: repPenalty = -2 * roundsRemaining\n");
        sb.append("Last early close: ").append(s.lastEarlyCloseRepPenalty)
                .append(" rep (R=").append(s.lastEarlyCloseRoundsRemaining).append(")\n");

        sb.append("\nSecurity breakdown:\n");
        sb.append("- Base security: ").append(breakdown.base()).append("\n");
        sb.append("- Upgrade bonuses: ").append(breakdown.upgrades()).append("\n");
        if (breakdown.upgrades() > 0) {
            sb.append("  ");
            java.util.List<String> upgradeParts = new java.util.ArrayList<>();
            for (PubUpgrade upgrade : s.ownedUpgrades) {
                if (upgrade.getSecurityBonus() > 0) {
                    upgradeParts.add(upgrade.getLabel() + " +" + upgrade.getSecurityBonus());
                }
            }
            if (!upgradeParts.isEmpty()) {
                sb.append(String.join(", ", upgradeParts));
            } else {
                sb.append("No security upgrades listed.");
            }
            sb.append("\n");
        }
        sb.append("- Policy modifier: ").append(breakdown.policy()).append("\n");
        sb.append("- Bouncer presence: ").append(breakdown.bouncers()).append("\n");
        sb.append("- Manager bonus: ").append(breakdown.manager()).append("\n");
        sb.append("- Staff bonus: ").append(breakdown.staff()).append("\n");
        sb.append("= Total: ").append(breakdown.total()).append("\n");
        int doorTier = s.reinforcedDoorTier();
        int lightTier = s.lightingTier();
        int alarmTier = s.burglarAlarmTier();
        sb.append("- Door tier: ").append(doorTier > 0 ? ("Tier " + doorTier) : "None")
                .append(" | incident x").append(fmt2(doorIncidentMultiplier(doorTier)))
                .append(" | rep mitig ").append((int)Math.round(doorRepMitigationPct(doorTier) * 100)).append("%\n");
        sb.append("- Lighting tier: ").append(lightTier > 0 ? ("Tier " + lightTier) : "None")
                .append(" | incident x").append(fmt2(lightingIncidentMultiplier(lightTier)))
                .append(" | morale stability ").append((int)Math.round(lightingMoraleStabilityPct(lightTier) * 100)).append("%\n");
        sb.append("- Alarm tier: ").append(alarmTier > 0 ? ("Tier " + alarmTier) : "None")
                .append(" | incident x").append(fmt2(alarmIncidentMultiplier(alarmTier)))
                .append(" | loss severity x").append(fmt2(alarmLossSeverityMultiplier(alarmTier))).append("\n");

        sb.append("\nInstalled security upgrades:\n");
        java.util.List<String> upgradesList = new java.util.ArrayList<>();
        if (s.ownedUpgrades.contains(PubUpgrade.CCTV) || s.ownedUpgrades.contains(PubUpgrade.CCTV_PACKAGE)) {
            upgradesList.add("CCTV");
        }
        if (s.reinforcedDoorTier() > 0) {
            upgradesList.add("Reinforced Door (Tier " + s.reinforcedDoorTier() + ")");
        }
        if (s.lightingTier() > 0) {
            upgradesList.add("Lighting (Tier " + s.lightingTier() + ")");
        }
        if (s.burglarAlarmTier() > 0) {
            upgradesList.add("Burglar Alarm (Tier " + s.burglarAlarmTier() + ")");
        }
        if (upgradesList.isEmpty()) {
            sb.append("None\n");
        } else {
            for (String entry : upgradesList) sb.append("- ").append(entry).append("\n");
        }

        sb.append("\nWhat this changes:\n");
        sb.append("- Policy incident chance: x").append(fmt2(securityPolicyIncidentChanceMultiplier())).append("\n");
        sb.append("- Policy traffic: x").append(fmt2(securityPolicyTrafficMultiplier())).append("\n");
        sb.append("- Task incident chance: x").append(fmt2(securityTaskIncidentChanceMultiplier())).append("\n");
        sb.append("- Task traffic: x").append(fmt2(securityTaskTrafficMultiplier())).append("\n");
        sb.append("- Upgrade incident chance: x").append(fmt2(s.upgradeIncidentChanceMultiplier)).append("\n");
        sb.append("- Upgrade loss severity: x").append(fmt2(s.upgradeLossSeverityMultiplier)).append("\n");
        sb.append("- Upgrade morale stability: ").append((int)Math.round(s.upgradeMoraleStabilityPct * 100)).append("%\n");
        sb.append("- Rep mitigation (bouncers+CCTV+upgrades): x").append(fmt2(s.securityIncidentRepMultiplier())).append("\n");

        sb.append("\nRecent early-close penalties:\n");
        if (s.earlyClosePenaltyLog.isEmpty()) {
            sb.append("None\n");
        } else {
            int earlyCount = 0;
            for (String entry : s.earlyClosePenaltyLog) {
                sb.append("- ").append(entry).append("\n");
                if (++earlyCount >= 4) break;
            }
        }

        sb.append("\nRecent security log:\n");
        if (s.securityEventLog.isEmpty()) {
            sb.append("None");
        } else {
            int count = 0;
            for (String entry : s.securityEventLog) {
                sb.append("- ").append(entry).append("\n");
                if (++count >= 6) break;
            }
        }
        return sb.toString();
    }

    private String buildInnDetailText() {
        StringBuilder sb = new StringBuilder();
        if (!s.innUnlocked) {
            sb.append("Inn: LOCKED\n");
            sb.append("Upgrade available: Inn Wing (Tier 1)\n");
            return sb.toString();
        }

        sb.append("Inn tier: ").append(s.innTier).append("\n");
        sb.append("Rooms: ").append(s.roomsTotal).append("\n");
        sb.append("Booked last night: ").append(s.lastNightRoomsBooked)
                .append(" | Revenue ").append(fmt2(s.lastNightRoomRevenue)).append("\n");
        sb.append("Room price: ").append(fmt2(s.roomPrice)).append("\n");
        sb.append("Inn rep: ").append(fmt1(s.innRep))
                .append(" | Cleanliness: ").append(fmt1(s.cleanliness)).append("\n");

        sb.append("\nDemand breakdown:\n");
        sb.append("- Base: ").append(fmt2(s.lastInnDemandBase)).append("\n");
        sb.append("- Inn rep: ").append(fmt2(s.lastInnDemandRep)).append("\n");
        sb.append("- Cleanliness: ").append(fmt2(s.lastInnDemandClean)).append("\n");
        sb.append("- Pub rep: ").append(fmt2(s.lastInnDemandPubRep)).append("\n");
        sb.append("- Price: ").append(fmt2(s.lastInnDemandPrice)).append("\n");
        sb.append("- Security: ").append(fmt2(s.lastInnDemandSecurity)).append("\n");
        sb.append("- Noise: ").append(fmt2(s.lastInnDemandNoise)).append("\n");
        sb.append("= Demand score: ").append(fmt2(s.lastInnDemandScore)).append("\n");

        sb.append("\nCoverage breakdown:\n");
        sb.append("- Reception capacity: ").append(s.lastInnReceptionCapacity).append("\n");
        sb.append("- Housekeeping coverage: ").append(s.lastInnHousekeepingCoverage)
                .append(" / rooms booked ").append(s.lastInnHousekeepingNeeded).append("\n");
        sb.append("- Duty manager: ").append(s.dutyManagerCount() > 0 ? "Yes" : "No").append("\n");

        sb.append("\nInn events:\n");
        if (s.innEventLog.isEmpty()) {
            sb.append("  None\n");
        } else {
            for (String line : s.innEventLog) {
                sb.append("  - ").append(line).append("\n");
            }
        }

        sb.append("\nWeekly financials:\n");
        sb.append("- Room revenue: ").append(fmt2(s.weekInnRevenue)).append("\n");
        sb.append("- Maintenance accrued: ").append(fmt2(s.innMaintenanceAccruedWeekly)).append("\n");
        sb.append("- Inn staff wages: ").append(fmt2(innStaffWages())).append("\n");
        return sb.toString();
    }

    private double innStaffWages() {
        double total = 0.0;
        for (Staff st : s.fohStaff) {
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

    private String securityTaskStatusLine() {
        SecurityTask task = s.activeSecurityTask;
        if (task == null) return "None";
        String status = s.isSecurityTaskActive() ? "Active" : (s.isSecurityTaskQueued() ? "Queued" : "Inactive");
        int cooldown = s.securityTaskCooldownRemaining(task);
        return task.getLabel() + " (" + status + ", CD " + cooldown + "r)";
    }

    private double doorIncidentMultiplier(int tier) {
        return switch (tier) {
            case 1 -> 0.98;
            case 2 -> 0.95;
            case 3 -> 0.92;
            default -> 1.0;
        };
    }

    private double lightingIncidentMultiplier(int tier) {
        return switch (tier) {
            case 1 -> 0.99;
            case 2 -> 0.97;
            case 3 -> 0.95;
            default -> 1.0;
        };
    }

    private double alarmIncidentMultiplier(int tier) {
        return switch (tier) {
            case 1 -> 0.98;
            case 2 -> 0.95;
            case 3 -> 0.90;
            default -> 1.0;
        };
    }

    private double doorRepMitigationPct(int tier) {
        return switch (tier) {
            case 2 -> 0.03;
            case 3 -> 0.06;
            default -> 0.0;
        };
    }

    private double lightingMoraleStabilityPct(int tier) {
        return switch (tier) {
            case 2 -> 0.05;
            case 3 -> 0.10;
            default -> 0.0;
        };
    }

    private double alarmLossSeverityMultiplier(int tier) {
        return switch (tier) {
            case 1 -> 0.96;
            case 2 -> 0.92;
            case 3 -> 0.86;
            default -> 1.0;
        };
    }

    private double incidentChanceMultiplier(int sec) {
        double base = Math.max(0.20, 1.0 - (sec * 0.08));
        return base * securityPolicyIncidentChanceMultiplier()
                * securityTaskIncidentChanceMultiplier()
                * s.upgradeIncidentChanceMultiplier;
    }

    private String buildStaffDetailText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team morale: ").append((int)Math.round(s.teamMorale)).append("\n");
        sb.append("FOH morale: ").append((int)Math.round(s.fohMorale))
                .append(" | BOH morale: ").append((int)Math.round(s.bohMorale)).append("\n");
        sb.append("Last staff incident: ").append(s.lastStaffIncidentSummary).append("\n");
        sb.append("Incident drivers: ").append(s.lastStaffIncidentDrivers).append("\n");
        sb.append("Chaos morale mult: -").append(fmt2(s.lastChaosMoraleNegMult))
                .append(" / +").append(fmt2(s.lastChaosMoralePosMult)).append("\n");
        return sb.toString();
    }

    private String randomSharkPhrase(int tier) {
        java.util.List<String> phrases = switch (tier) {
            case 1 -> java.util.List.of(
                    "A hard stare from a stranger. Message received.",
                    "A quiet reminder came with a cold look.",
                    "Someone lingered outside longer than usual."
            );
            case 2 -> java.util.List.of(
                    "Two visitors asked about your arrangement.",
                    "The collectors stopped by, all business, no smiles.",
                    "A sharp knock, a short talk, and a heavy silence."
            );
            case 3 -> java.util.List.of(
                    "A chair snapped. Someone laughed on the way out.",
                    "Glass clinked the wrong way. Repairs won’t be cheap.",
                    "A back-room mess left a bill on the counter."
            );
            case 4 -> java.util.List.of(
                    "Tonight felt cursed. Everyone noticed.",
                    "The room went tense. Even regulars kept quiet.",
                    "The air stayed heavy long after last call."
            );
            default -> java.util.List.of("The night passes without incident.");
        };
        return phrases.get(s.random.nextInt(phrases.size()));
    }

    private void updateKitchenInventoryCap() {
        int headChefs = s.staffCountOfType(Staff.Type.HEAD_CHEF);
        s.foodRack.setCapacity(s.baseFoodRackCapacity + s.upgradeFoodRackCapBonus + (headChefs * 5));
    }

    private int kitchenUpgradeLevel() {
        if (s.ownedUpgrades.contains(PubUpgrade.KITCHEN_EQUIPMENT)) return 3;
        if (s.ownedUpgrades.contains(PubUpgrade.NEW_KITCHEN_PLAN)) return 2;
        if (s.ownedUpgrades.contains(PubUpgrade.KITCHEN)) return 1;
        return 0;
    }

    private void updateChaosFromRound(int unserved,
                                      int eventsThisRound,
                                      int fightsThisRound,
                                      int refundsThisRound,
                                      int foodMissesThisRound) {
        RoundClassification classification = classifyRound(unserved, eventsThisRound, fightsThisRound, refundsThisRound, foodMissesThisRound);
        double base = 0.0;
        double streakDelta = 0.0;

        if (classification == RoundClassification.STRONGLY_NEGATIVE) {
            s.negStreak += 1;
            s.posStreak = 0;
            int streak = Math.min(CHAOS_STREAK_CAP, s.negStreak);
            base = switch (streak) {
                case 1 -> CHAOS_BAD_DELTA_1;
                case 2 -> CHAOS_BAD_DELTA_2;
                default -> CHAOS_BAD_DELTA_3;
            };
            streakDelta = base - CHAOS_BAD_DELTA_1;
        } else if (classification == RoundClassification.MOSTLY_POSITIVE) {
            s.posStreak += 1;
            s.negStreak = 0;
            int streak = Math.min(CHAOS_STREAK_CAP, s.posStreak);
            base = switch (streak) {
                case 1 -> CHAOS_GOOD_DELTA_1;
                case 2 -> CHAOS_GOOD_DELTA_2;
                default -> CHAOS_GOOD_DELTA_3;
            };
            streakDelta = base - CHAOS_GOOD_DELTA_1;
        } else {
            s.posStreak = 0;
            s.negStreak = 0;
        }

        double delta = base;
        s.chaos = Math.max(CHAOS_MIN, Math.min(CHAOS_MAX, s.chaos + delta));

        s.lastRoundClassification = formatClassification(classification);
        s.lastChaosDelta = delta;
        s.lastChaosDeltaBase = base;
        s.lastChaosDeltaStreak = streakDelta;
        addChaosDeltaLog(delta, classification);
        if (Math.abs(delta) >= 5.0) {
            if (delta > 0) {
                log.info("Chaos +" + (int)Math.round(delta) + " (bad streak x" + Math.max(1, Math.min(CHAOS_STREAK_CAP, s.negStreak)) + ")");
            } else {
                log.info("Order returning: chaos " + (int)Math.round(delta) + " (good streak x" + Math.max(1, Math.min(CHAOS_STREAK_CAP, s.posStreak)) + ")");
            }
        }
    }


    private void addChaosDeltaLog(double delta, RoundClassification classification) {
        if (Math.abs(delta) < 0.01) return;
        String line = String.format("%s | chaos %+,.1f | pos %d neg %d",
                formatClassification(classification), delta, s.posStreak, s.negStreak);
        s.chaosDeltaLog.addFirst(line);
        while (s.chaosDeltaLog.size() > 10) s.chaosDeltaLog.removeLast();
    }

    private RoundClassification classifyRound(int unserved,
                                              int eventsThisRound,
                                              int fightsThisRound,
                                              int refundsThisRound,
                                              int foodMissesThisRound) {
        boolean stronglyNegative = eventsThisRound > 0
                || fightsThisRound > 0
                || refundsThisRound > 0
                || unserved >= BAD_UNSERVED_MIN
                || foodMissesThisRound >= BAD_FOOD_MISSES_MIN
                || s.staffIncidentThisRound
                || s.happyHourCheatRepHitThisRound;
        if (stronglyNegative) return RoundClassification.STRONGLY_NEGATIVE;

        boolean mostlyPositive = eventsThisRound == 0
                && fightsThisRound == 0
                && refundsThisRound == 0
                && unserved <= GOOD_UNSERVED_MAX
                && foodMissesThisRound <= 0
                && !s.staffIncidentThisRound;
        if (mostlyPositive) return RoundClassification.MOSTLY_POSITIVE;

        return RoundClassification.NEUTRAL;
    }

    private String formatClassification(RoundClassification classification) {
        return classification.name().replace('_', ' ');
    }

    private double recomputeChaos(int barCount,
                                  int demand,
                                  int serveCap,
                                  int unserved,
                                  int fightsThisRound,
                                  int refundsThisRound,
                                  int eventsThisRound) {
        double punterSum = 0.0;
        for (Punter p : s.nightPunters) {
            if (!p.hasLeftBar() && !p.isBanned()) {
                punterSum += p.getChaosContribution();
            }
        }
        double unservedPressure = unserved * 1.2;
        double fightPressure = fightsThisRound * 4.0;
        double refundPressure = refundsThisRound * 1.5;
        double moralePressure = Math.max(0.0, 60.0 - s.teamMorale) / 6.0;
        double repVolatility = Math.min(10.0, s.weeklyRepDeltaAbs * 0.15);
        double occupancyRatio = Math.max(0.0, (double) barCount / Math.max(1, s.maxBarOccupancy));
        double overcrowdPressure = Math.max(0.0, occupancyRatio - 0.85) * 20.0;
        double eventPressure = eventsThisRound * 1.2;
        double activityPressure = (s.activityTonight != null)
                ? (2.0 + (s.activityTonight.getRiskBonusPct() * 18.0))
                : 0.0;
        double baseChaos = punterSum + unservedPressure + fightPressure + refundPressure
                + moralePressure + repVolatility + overcrowdPressure + eventPressure + activityPressure
                + s.betweenNightChaos;
        double smoothed = (s.chaos * 0.35) + (baseChaos * 0.65);
        s.betweenNightChaos = Math.max(0.0, s.betweenNightChaos - 1.5);
        return Math.max(0.0, Math.min(100.0, smoothed));
    }

    private record RoundModifiers(
            double trafficMultiplier,
            double spendMultiplier,
            double chaosDelta,
            double staffMoraleDelta,
            double serveCapacityMultiplier,
            double fatiguePressure,
            double tipBonus,
            boolean lateChaosRisk
    ) {}

    private RoundModifiers computeModifiersForCurrentRound() {
        TimePhase phase = s.getCurrentPhase();
        LocalTime now = s.getCurrentTime();
        MusicEffects musicEffects = musicSystem.computeEffects(s.currentMusicProfile, phase);
        double trafficMult =
                upgrades.trafficMultiplier()
                        * activities.trafficMultiplier()
                        * baseTrafficMultiplier()
                        * timeOfDayTrafficMultiplier(phase, now)
                        * musicEffects.trafficMultiplier()
                        * identityTrafficMultiplier()
                        * rumorTrafficMultiplier()
                        * securityPolicyTrafficMultiplier()
                        * securityTaskTrafficMultiplier()
                        * (1.0 + s.landlordTrafficBonusPct);
        if (rumors != null) {
            trafficMult *= rumors.trafficMultiplier();
        }

        double fatiguePenalty = 1.0 - Math.min(0.35, s.teamFatigue * (phase == TimePhase.LATE ? 0.010 : 0.007));
        double serveCapMult = Math.max(0.65, fatiguePenalty);

        return new RoundModifiers(
                Math.max(0.45, Math.min(2.1, trafficMult)),
                musicEffects.spendMultiplier(),
                musicEffects.chaosDelta(),
                musicEffects.staffMoraleDelta(),
                serveCapMult,
                s.teamFatigue,
                musicEffects.lingerMultiplier() > 1.0 ? 0.002 : 0.0,
                musicEffects.lateChaosRisk()
        );
    }

    private void applyMusicIdentityPressure(double pressure) {
        if (Math.abs(pressure) < 0.001) return;
        PubIdentity target = switch (s.currentMusicProfile) {
            case ACOUSTIC_CHILL, JAZZ_LOUNGE -> PubIdentity.RESPECTABLE;
            case INDIE_ALT -> PubIdentity.ARTSY;
            case CLASSIC_ROCK, ELECTRONIC_LATE -> PubIdentity.UNDERGROUND;
            case POP_PARTY -> PubIdentity.ROWDY;
            case SPORTS_TV -> PubIdentity.FAMILY_FRIENDLY;
        };
        s.recordActivitySignal(target, pressure);
        if (s.weeklyMusicSwitches >= 4) {
            s.recordActivitySignal(PubIdentity.NEUTRAL, Math.abs(pressure) * 0.5);
        }
    }

    private double timeOfDayTrafficMultiplier(TimePhase phase, LocalTime time) {
        double phaseMult = switch (phase) {
            case EARLY_DAY -> 0.82;
            case BUILD_UP -> 1.02;
            case PEAK -> 1.22;
            case LATE -> 0.92;
        };
        if (phase == TimePhase.BUILD_UP && !time.isBefore(LocalTime.of(17, 24)) && time.isBefore(LocalTime.of(18, 0))) {
            phaseMult *= 1.10;
        }
        if (phase == TimePhase.LATE && s.chaos > 60.0) {
            phaseMult *= 0.95;
        }
        return phaseMult;
    }

    private void applyFatiguePressure(int unserved, int eventsThisRound, int fightsThisRound, int serveCap) {
        TimePhase phase = s.getCurrentPhase();
        int activeStaff = Math.max(1, s.fohStaffCount() + s.bohStaff.size() + s.generalManagers.size());
        double coverageRatio = activeStaff / Math.max(1.0, serveCap / 2.0);
        double understaffPressure = coverageRatio < 1.0 ? (1.0 - coverageRatio) * 1.5 : 0.0;
        double phasePressure = switch (phase) {
            case EARLY_DAY -> 0.35;
            case BUILD_UP -> 0.60;
            case PEAK -> 1.05;
            case LATE -> 0.85;
        };
        double chaosPressure = Math.max(0.0, (s.chaos - 30.0) / 30.0);
        double incidentPressure = Math.max(0, eventsThisRound + fightsThisRound) * 0.25;
        double unservedPressure = Math.max(0, unserved) * 0.05;

        double gain = phasePressure + understaffPressure + chaosPressure + incidentPressure + unservedPressure;
        s.teamFatigue = Math.max(0.0, Math.min(30.0, s.teamFatigue + gain));
        s.rollingFatigueStress = Math.max(0.0, Math.min(50.0, (s.rollingFatigueStress * 0.8) + gain));
    }

    private void maybeTriggerSickCall() {
        double chance = 0.03;
        chance += Math.max(0.0, (55.0 - s.teamMorale) / 600.0);
        chance += Math.max(0.0, (s.lastNightChaosPeak - 45.0) / 500.0);
        chance += Math.max(0.0, s.rollingFatigueStress / 700.0);
        chance = Math.min(0.28, chance);
        if (s.random.nextDouble() >= chance) return;

        java.util.List<Staff> pool = new java.util.ArrayList<>();
        pool.addAll(s.fohStaff);
        pool.addAll(s.bohStaff);
        if (pool.isEmpty()) return;
        Staff picked = pool.get(s.random.nextInt(pool.size()));
        if (s.fohStaff.remove(picked) || s.bohStaff.remove(picked) || s.generalManagers.remove(picked)) {
            s.sickStaffTonight.add(picked);
            s.sickCallTriggeredTonight = true;
            s.sickStaffNameTonight = picked.getName();
            log.event(picked.getName() + " called in sick and can't make it tonight. Coverage reduced.");
            staff.updateTeamMorale();
        }
    }

    private boolean maybeTriggerTimePhaseEarlyClose() {
        TimePhase phase = s.getCurrentPhase();
        if (!(phase == TimePhase.PEAK || phase == TimePhase.LATE)) return false;
        if (s.lastEarlyCloseCheckNight == s.nightCount) return false;

        LocalTime now = s.getCurrentTime();
        boolean phaseStart = (phase == TimePhase.PEAK && now.equals(LocalTime.of(18, 48)))
                || (phase == TimePhase.LATE && now.equals(LocalTime.of(21, 48)));
        if (!phaseStart) return false;

        s.lastEarlyCloseCheckNight = s.nightCount;
        if (s.teamMorale > 45) return false;

        double chance = 0.05 + Math.max(0.0, (45.0 - s.teamMorale) / 280.0);
        if (s.teamFatigue > 12) chance += 0.03;
        if (s.random.nextDouble() < Math.min(0.20, chance)) {
            log.event("Staff morale collapsed at " + now + "; last orders called early.");
            return true;
        }
        return false;
    }

    void runRivalDistrictWeekForTests(java.util.Random rng) {
        applyRivalMarketPressure(rivalSystem.runWeekly(defaultDistrictRivals(), rng));
    }

    private void runRivalDistrictWeek() {
        if (!FeatureFlags.FEATURE_RIVALS) {
            applyRivalMarketPressure(MarketPressure.empty());
            return;
        }
        applyRivalMarketPressure(rivalSystem.runWeekly(defaultDistrictRivals(), s.random));
    }

    private void applyRivalMarketPressure(MarketPressure pressure) {
        s.latestMarketPressure = pressure == null ? MarketPressure.empty() : pressure;
        if (!FeatureFlags.FEATURE_RIVALS || s.latestMarketPressure.totalRivals() <= 0) {
            s.rivalDemandTrafficMultiplier = 1.0;
            s.rivalPunterMixBias = 0.0;
            s.rivalRumorSentimentBias = 0.0;
            s.rivalDistrictUpdate = "District update: quiet week.";
            return;
        }

        int priceWar = s.latestMarketPressure.countFor(RivalStance.PRICE_WAR);
        int qualityPush = s.latestMarketPressure.countFor(RivalStance.QUALITY_PUSH);
        int eventSpam = s.latestMarketPressure.countFor(RivalStance.EVENT_SPAM);
        int layLow = s.latestMarketPressure.countFor(RivalStance.LAY_LOW);
        int recovery = s.latestMarketPressure.countFor(RivalStance.CHAOS_RECOVERY);

        s.rivalDemandTrafficMultiplier = clamp(1.0
                - (priceWar * 0.03)
                - (eventSpam * 0.02)
                + (layLow * 0.01)
                + (recovery * 0.01), 0.90, 1.06);

        s.rivalPunterMixBias = clamp((qualityPush * 0.06) - (priceWar * 0.05) - (eventSpam * 0.03), -0.20, 0.20);
        s.rivalRumorSentimentBias = clamp((priceWar * 0.18) + (eventSpam * 0.14) - (qualityPush * 0.12) - (layLow * 0.08), -0.50, 0.60);

        s.rivalDistrictUpdate = buildRivalDistrictUpdate(s.latestMarketPressure);
    }

    private String buildRivalDistrictUpdate(MarketPressure pressure) {
        RivalStance dominant = pressure.dominantStance();
        String line1 = "District: rivals leaned " + dominant.name().replace('_', ' ').toLowerCase() + " this week.";
        String line2 = "Pressure: traffic x" + fmt2(s.rivalDemandTrafficMultiplier)
                + " | mix bias " + fmt2(s.rivalPunterMixBias)
                + " | rumor bias " + fmt2(s.rivalRumorSentimentBias);
        return line1 + "\n" + line2;
    }

    private List<RivalPub> defaultDistrictRivals() {
        return List.of(
                new RivalPub("The Copper Fox", 2, 1, 1, "noisy"),
                new RivalPub("Pearl Street Tap", 0, 2, 2, "upscale"),
                new RivalPub("North Lane Inn", 1, 1, 0, "mixed")
        );
    }

    private GameModifierSnapshot buildModifierSnapshot() {
        return GameModifierSnapshot.from(s);
    }

    private void debugModifierSnapshot(String context, GameModifierSnapshot mods) {
        if (!FeatureFlags.DEBUG_MODIFIER_LOGS || mods == null) return;
        log.info("[DEBUG modifiers:" + context + "] season x" + fmt2(mods.seasonTrafficMultiplier())
                + " -> rival x" + fmt2(mods.rivalTrafficMultiplier())
                + " -> vip x" + fmt2(mods.vipTrafficMultiplier())
                + " => final x" + fmt2(mods.finalTrafficMultiplier()));
    }

    private double rivalTrafficMultiplier() {
        return buildModifierSnapshot().rivalTrafficMultiplier();
    }

    private double baseTrafficMultiplier() {
        double repMult =
                (s.reputation >= 70) ? 1.28 :
                        (s.reputation >= 40) ? 1.14 :
                                (s.reputation >= -20) ? 1.00 :
                                        (s.reputation >= -60) ? 0.86 :
                                                0.72;

        if (s.reputation > 70) repMult += 0.08;
        else if (s.reputation > 40) repMult += 0.04;
        else if (s.reputation < -60) repMult -= 0.08;
        else if (s.reputation < -20) repMult -= 0.04;

        if (s.businessCollapsed) {
            repMult *= 0.35;
        }

        boolean weekend = s.isWeekend(); // Fri/Sat/Sun
        double weekendMult = weekend ? 1.20 : 1.0;
        if (!weekend) {
            weekendMult *= 0.92 + (s.random.nextDouble() * 0.16);
        }

        double identityMult = s.currentIdentity != null ? s.currentIdentity.getTrafficMultiplier() : 1.0;
        double levelMult = 1.0 + s.pubLevelTrafficBonusPct;
        double legacyMult = 1.0 + s.legacy.trafficMultiplierBonus;

        GameModifierSnapshot mods = buildModifierSnapshot();
        debugModifierSnapshot("traffic", mods);
        return repMult * weekendMult * identityMult * levelMult * legacyMult * mods.finalTrafficMultiplier();
    }

    private double identityTrafficMultiplier() {
        return switch (s.pubIdentity) {
            case RESPECTABLE -> 1.06;
            case ROWDY -> 1.08;
            case ARTSY -> 1.04;
            case SHADY -> 0.94;
            case FAMILY_FRIENDLY -> 1.05;
            case UNDERGROUND -> 1.00;
            case NEUTRAL -> 1.0;
        };
    }

    private int identityEventBonusChance() {
        return switch (s.pubIdentity) {
            case RESPECTABLE, ARTSY, FAMILY_FRIENDLY -> 2;
            case ROWDY, SHADY, UNDERGROUND -> 4;
            case NEUTRAL -> 0;
        };
    }

    private double rumorTrafficMultiplier() {
        double mult = 1.0;
        int watered = s.rumorHeat.getOrDefault(Rumor.WATERED_DOWN_DRINKS, 0);
        int fights = s.rumorHeat.getOrDefault(Rumor.FIGHTS_EVERY_WEEKEND, 0);
        int roast = s.rumorHeat.getOrDefault(Rumor.BEST_SUNDAY_ROAST, 0);
        int poisoning = s.rumorHeat.getOrDefault(Rumor.FOOD_POISONING_SCARE, 0);
        int slow = s.rumorHeat.getOrDefault(Rumor.SLOW_SERVICE, 0);
        int friendly = s.rumorHeat.getOrDefault(Rumor.FRIENDLY_STAFF, 0);
        int atmosphere = s.rumorHeat.getOrDefault(Rumor.GREAT_ATMOSPHERE, 0);

        mult -= watered * 0.002;
        mult -= fights * 0.0025;
        mult += roast * 0.002;
        mult -= poisoning * 0.002;
        mult -= slow * 0.002;
        mult += friendly * 0.002;
        mult += atmosphere * 0.002;

        return Math.max(0.80, Math.min(1.20, mult));
    }


    private List<String> currentPunterNames() {
        List<String> names = new java.util.ArrayList<>();
        for (Punter punter : s.nightPunters) {
            if (punter != null && punter.getName() != null && !punter.getName().isBlank()) {
                names.add(punter.getName());
            }
        }
        return names;
    }

    private VIPNightOutcome buildVipNightOutcome() {
        double foodQualitySignal = s.nightFoodUnserved == 0 && s.nightRefunds == 0 ? 0.8 : 0.3;
        return new VIPNightOutcome(
                s.nightUnserved,
                s.nightFights,
                s.nightEvents,
                s.nightRefunds,
                s.priceMultiplier,
                foodQualitySignal
        );
    }

    private void applyVipConsequences(List<VIPSystem.VIPConsequence> consequences) {
        if (consequences == null || consequences.isEmpty()) return;

        for (VIPSystem.VIPConsequence c : consequences) {
            if (c == null) continue;
            if (c.stage() == VIPArcStage.ADVOCATE) {
                s.vipDemandBoostMultiplier = clamp(s.vipDemandBoostMultiplier * 1.05, 1.0, 1.35);
                s.vipRumorShield = clamp(s.vipRumorShield + 0.02, 0.0, 0.20);
                eco.applyRep(+4, "VIP advocate: " + c.vip().getName());
            } else if (c.stage() == VIPArcStage.BACKLASH) {
                s.vipRumorShield = clamp(s.vipRumorShield - 0.02, 0.0, 0.20);
                eco.applyRep(-6, "VIP backlash: " + c.vip().getName());
                addRumorHeat(Rumor.SLOW_SERVICE, 10, RumorSource.PUNTER);
                s.baseSecurityLevel = Math.max(0, s.baseSecurityLevel - 1);
            }

            log.popup(c.popupTitle(), c.popupBody(), c.stage().name());
            addVipWeeklyNote(c.weeklyLine());
            s.vipObservationSnippet = c.observationLine();
            s.vipObservationRoundsRemaining = Math.max(s.vipObservationRoundsRemaining, 6);
        }
    }

    private void addVipWeeklyNote(String line) {
        if (line == null || line.isBlank()) return;
        s.vipWeeklyNotes.addFirst(line);
        while (s.vipWeeklyNotes.size() > 8) s.vipWeeklyNotes.removeLast();
    }

    private String repMoodLabel() {
        if (s.reputation >= 60) return "Loved";
        if (s.reputation >= 20) return "Solid";
        if (s.reputation >= -20) return "Shaky";
        if (s.reputation >= -60) return "Bad";
        return "Toxic";
    }

    private String chaosMoodLabel() {
        if (s.chaos <= 15) return "Calm";
        if (s.chaos <= 30) return "Tense";
        if (s.chaos <= 50) return "Volatile";
        if (s.chaos <= 70) return "Unstable";
        return "Explosive";
    }

    private String activitySummaryLine() {
        if (s.activityTonight != null) {
            return s.activityTonight.getLabel() + " (active)";
        }
        if (s.scheduledActivity != null) {
            int daysLeft = Math.max(0, s.scheduledActivity.startAbsDayIndex() - s.absDayIndex());
            return s.scheduledActivity.activity().getLabel() + " (scheduled in " + daysLeft + "d)";
        }
        return "None";
    }

    private String chaosBreakdownLine() {
        double punterSum = 0.0;
        int activePunters = 0;
        for (Punter p : s.nightPunters) {
            if (!p.hasLeftBar() && !p.isBanned()) {
                punterSum += p.getChaosContribution();
                activePunters++;
            }
        }
        double moralePressure = Math.max(0.0, 60.0 - s.teamMorale) / 6.0;
        double repVolatility = Math.min(10.0, s.weeklyRepDeltaAbs * 0.15);
        double occupancyRatio = Math.max(0.0, (double) activePunters / Math.max(1, s.maxBarOccupancy));
        double overcrowdPressure = Math.max(0.0, occupancyRatio - 0.85) * 20.0;
        return "punters " + fmt1(punterSum)
                + ", unserved " + s.nightUnserved
                + ", fights " + s.nightFights
                + ", refunds " + s.nightRefunds
                + ", morale " + fmt1(moralePressure)
                + ", rep swing " + fmt1(repVolatility)
                + ", overcrowd " + fmt1(overcrowdPressure)
                + (s.activityTonight != null ? ", activity " + fmt1(2.0 + (s.activityTonight.getRiskBonusPct() * 18.0)) : "")
                + (s.betweenNightChaos > 0 ? ", between-night " + fmt1(s.betweenNightChaos) : "");
    }

    private String buildChaosInsightsLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(s.lastRoundClassification)
                .append(" | pos ").append(s.posStreak)
                .append(" | neg ").append(s.negStreak)
                .append("\nChaos delta: ").append(fmt1(s.lastChaosDelta))
                .append(" (base ").append(fmt1(s.lastChaosDeltaBase))
                .append(", streak ").append(fmt1(s.lastChaosDeltaStreak)).append(")")
                .append("\nConstants: bad ")
                .append(fmt1(CHAOS_BAD_DELTA_1)).append("/")
                .append(fmt1(CHAOS_BAD_DELTA_2)).append("/")
                .append(fmt1(CHAOS_BAD_DELTA_3))
                .append(" | good ")
                .append(fmt1(CHAOS_GOOD_DELTA_1)).append("/")
                .append(fmt1(CHAOS_GOOD_DELTA_2)).append("/")
                .append(fmt1(CHAOS_GOOD_DELTA_3))
                .append(" | streak cap ").append(CHAOS_STREAK_CAP)
                .append("\nMorale mult: neg x").append(fmt2(s.lastChaosMoraleNegMult))
                .append(", pos x").append(fmt2(s.lastChaosMoralePosMult));
        if (!s.chaosDeltaLog.isEmpty()) {
            sb.append("\nRecent chaos deltas:");
            int count = 0;
            for (String line : s.chaosDeltaLog) {
                sb.append("\n- ").append(line);
                if (++count >= 5) break;
            }
        }
        return sb.toString();
    }

    private int riskRumorCount() {
        int count = 0;
        for (RumorInstance rumor : s.activeRumors.values()) {
            if (rumor.repDrift() < 0) count++;
        }
        return count;
    }

    private String buildRumorDetailText() {
        if (s.activeRumors.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        java.util.List<RumorInstance> list = new java.util.ArrayList<>(s.activeRumors.values());
        list.sort((a, b) -> Integer.compare(b.intensity(), a.intensity()));
        for (RumorInstance rumor : list) {
            sb.append("- ").append(rumor.describe())
                    .append(" | spread ").append(fmt2(rumor.spreadRate()))
                    .append(" | traffic x").append(fmt2(rumor.trafficMultiplier()))
                    .append(" | wealth ").append(fmt2(rumor.wealthBias()))
                    .append("\n");
        }
        return sb.toString();
    }

    private String punterTierBreakdown() {
        int lowlife = 0;
        int regular = 0;
        int decent = 0;
        int big = 0;
        for (Punter p : s.nightPunters) {
            if (p.hasLeftBar() || p.isBanned()) continue;
            switch (p.getTier()) {
                case LOWLIFE -> lowlife++;
                case REGULAR -> regular++;
                case DECENT -> decent++;
                case BIG_SPENDER -> big++;
            }
        }
        return "Lowlife " + lowlife + " | Regular " + regular + " | Decent " + decent + " | Big " + big;
    }

    private double avgPriceMultiplier() {
        return s.weekPriceMultiplierSamples > 0
                ? (s.weekPriceMultiplierSum / s.weekPriceMultiplierSamples)
                : s.priceMultiplier;
    }

    private static String fmt2(double value) {
        return String.format("%.2f", value);
    }

    private static String fmt1(double value) {
        return String.format("%.1f", value);
    }

    private static String pct(double value) {
        return String.format("%.0f%%", value * 100.0);
    }

    private static String money0(double value) {
        return "GBP " + String.format("%.0f", value);
    }

    private static String money2(double value) {
        return "GBP " + String.format("%.2f", value);
    }

    private String pickPhrase(List<String> options) {
        return options.get(s.random.nextInt(options.size()));
    }


    private double expectedArrivals(double trafficMult, boolean weekend) {
        // Base arrivals... scaled by traffic multiplier.
        double base = weekend ? 2.8 : 1.9;
        return base * Math.max(0.65, Math.min(1.60, trafficMult));
    }

    private int rollArrivals(double trafficMult, boolean weekend) {
        int capLeft = Math.max(0, s.maxBarOccupancy - s.nightPunters.size());
        if (capLeft <= 0) return 0;

        double expect = expectedArrivals(trafficMult, weekend);

        // Add mild randomness and clamp to remaining capacity.
        int arrivals = (int) Math.round(expect + (s.random.nextDouble() * 2.0 - 1.0));
        arrivals = Math.max(0, Math.min(capLeft, arrivals));
        return arrivals;
    }

    private void updateObservationLine(int barCount, int unserved, int fightsThisRound, int eventsThisRound, int refundsThisRound, RoundModifiers modifiers) {
        int roundIndex = absoluteRoundIndex();
        if (s.lastObservationPriceMultiplier <= 0.0) {
            s.lastObservationPriceMultiplier = s.priceMultiplier;
        }
        double priceChangeAbs = Math.abs(s.priceMultiplier - s.lastObservationPriceMultiplier);
        boolean staffChangeRecent = s.lastStaffChangeDay >= 0 && (s.dayCounter - s.lastStaffChangeDay) <= 1;
        ObservationEngine.ObservationContext ctx = new ObservationEngine.ObservationContext(
                roundIndex,
                barCount,
                unserved,
                fightsThisRound,
                eventsThisRound,
                refundsThisRound,
                s.lastTrafficIn,
                s.lastTrafficOut,
                s.priceMultiplier,
                priceChangeAbs,
                s.rack.count(),
                s.foodRack.count(),
                s.kitchenUnlocked,
                s.bouncersHiredTonight,
                s.staffIncidentThisRound,
                staffChangeRecent
        );
        ObservationEngine.ObservationResult result = observationEngine.nextObservation(s, ctx);
        if (result == null) return;
        String note = "[" + s.getCurrentPhase() + " | " + s.currentMusicProfile.getLabel() + "]";
        if (modifiers != null && modifiers.lateChaosRisk() && s.getCurrentPhase() == TimePhase.LATE) {
            note += " Late chaos risk up.";
        }
        if (s.sickCallTriggeredTonight) {
            note += " Sick call cut coverage.";
        }
        s.observationLine = trimObservationLine(result.text() + " " + note);
        s.lastObservationRound = roundIndex;
        s.lastObservationPriceMultiplier = s.priceMultiplier;
    }

    private int absoluteRoundIndex() {
        return s.dayCounter * s.closingRound + s.roundInNight;
    }


    private void processFoodOrders() {
        if (s.pendingFoodOrders.isEmpty()) return;
        if (!s.kitchenUnlocked) return;

        double chefAvgSkill = kitchenAverageSkill();
        int headChefs = s.staffCountOfType(Staff.Type.HEAD_CHEF);
        int sec = Math.max(0, security.effectiveSecurity() + s.upgradeSecurityBonus);

        for (int i = s.pendingFoodOrders.size() - 1; i >= 0; i--) {
            FoodOrder order = s.pendingFoodOrders.get(i);
            if (order.deliverRound() > s.roundInNight) continue;

            double refundChance = 0.18 + (order.food().getQualityTier() * 0.05) - (chefAvgSkill * 0.02);
            refundChance *= (1.0 - s.refundRiskReductionPct);
            refundChance *= (1.0 - Math.min(0.25, s.kitchenQualityBonus * 0.03));
            refundChance *= (1.0 - Math.min(0.25, sec * 0.03));
            refundChance *= (1.0 - Math.min(0.25, headChefs * 0.08));
            refundChance *= staff.refundPressureMultiplier(s.lastRoundWorkloadPenalty);
            refundChance = Math.max(0.04, Math.min(0.75, refundChance));

            if (s.random.nextInt(10000) < (int)Math.round(refundChance * 10000)) {
                double refundPct = 0.25 + (s.random.nextDouble() * 0.75);
                double refund = order.price() * refundPct;
                if (eco.tryPay(refund, TransactionType.OTHER, "Food refund", CostTag.FOOD)) {
                    s.recordRefund(refund);
                    s.nightRefunds++;
                    eco.applyRep(-1, "Food refund");
                    log.popup("Food refund", "<b>" + order.food().getName() + "</b> was sent back.", "Cash -" + String.format("%.2f", refund));
                }
            }
            s.pendingFoodOrders.remove(i);
        }
    }

    private void processSupplierDeliveries() {
        if (!s.pendingSupplierDeliveries.isEmpty()) {
            for (int i = s.pendingSupplierDeliveries.size() - 1; i >= 0; i--) {
                PendingSupplierDelivery delivery = s.pendingSupplierDeliveries.get(i);
                if (delivery.deliverRound() > s.roundInNight) continue;
                int added = s.rack.addBottles(delivery.wine(), delivery.quantity(), s.absDayIndex());
                log.popup(" Supplier delivery", delivery.quantity() + "x " + delivery.wine().getName() + " arrived.", "");
                s.pendingSupplierDeliveries.remove(i);
                if (added < delivery.quantity()) {
                    log.neg("Inventory full; some delivery stock was lost.");
                }
            }
        }

        if (!s.pendingFoodDeliveries.isEmpty()) {
            for (int i = s.pendingFoodDeliveries.size() - 1; i >= 0; i--) {
                PendingFoodDelivery delivery = s.pendingFoodDeliveries.get(i);
                if (delivery.deliverRound() > s.roundInNight) continue;
                int added = s.foodRack.addMeals(delivery.food(), delivery.quantity(), s.absDayIndex());
                log.popup(" Food delivery", delivery.quantity() + "x " + delivery.food().getName() + " arrived.", "");
                s.pendingFoodDeliveries.remove(i);
                if (added < delivery.quantity()) {
                    log.neg("Kitchen inventory full; some delivery stock was lost.");
                }
            }
        }
    }

    private int kitchenCapacity() {
        int cap = 0;
        for (Staff st : s.bohStaff) {
            cap += st.getKitchenCapacity();
        }
        double mult = 1.0;
        for (Staff st : s.generalManagers) {
            mult += (st.getCapacityMultiplier() - 1.0);
        }
        return Math.max(0, (int)Math.floor(cap * mult));
    }

    private double kitchenAverageSkill() {
        double sum = 0.0;
        int count = 0;
        for (Staff st : s.bohStaff) {
            if (st.isKitchenRole()) {
                sum += st.getSkill();
                count++;
            }
        }
        return sum / Math.max(1, count);
    }


    private void showEndOfNightReport() {
        int covers = Math.min(s.nightSales + s.nightUnserved, s.maxBarOccupancy);
        double profit = s.nightRevenue - s.nightRoundCostsTotal;
        String bestWine = bestSeller("Wine:");
        String bestFood = bestSeller("Food:");

        StringBuilder body = new StringBuilder();
        body.append("Covers: ").append(covers)
                .append(" | Served: ").append(s.nightSales)
                .append(" | Unserved: ").append(s.nightUnserved)
                .append("\nSales: ").append(s.nightSales)
                .append(" | Revenue GBP ").append(String.format("%.0f", s.nightRevenue))
                .append(" | Costs GBP ").append(String.format("%.0f", s.nightRoundCostsTotal))
                .append(" | Profit GBP ").append(String.format("%.0f", profit))
                .append("\nBest wine: ").append(bestWine)
                .append(" | Best food: ").append(bestFood)
                .append("\nEvents: ").append(s.nightEvents)
                .append(" | Fights: ").append(s.nightFights)
                .append(" | Kicked out: ").append(s.nightKickedOut)
                .append(" | Refunds GBP ").append(String.format("%.0f", s.nightRefundTotal))
                .append(" | Food misses: ").append(s.nightFoodUnserved)
                .append(" | Refund count: ").append(s.nightRefunds)
                .append("\n").append(s.lastServiceDrivers == null ? "Service: n/a" : s.lastServiceDrivers)
                .append("\n").append(s.lastStabilityDrivers == null ? "Stability: n/a" : s.lastStabilityDrivers);

        log.popup(" End of Night Report", body.toString().replace("\n", "<br/>"), "");
    }

    private String bestSeller(String prefix) {
        if (s.nightItemSales.isEmpty()) return "None";
        String best = "None";
        int max = 0;
        for (var entry : s.nightItemSales.entrySet()) {
            String key = entry.getKey();
            int val = entry.getValue();
            if (prefix != null && !key.startsWith(prefix)) continue;
            if (val > max) {
                max = val;
                best = key.replace(prefix, "").trim();
            }
        }
        return best;
    }

    private record TopSeller(String name, int count) {}

    private void finalizeRoundSales() {
        s.recentRoundSales.addLast(new HashMap<>(s.roundItemSales));
        while (s.recentRoundSales.size() > 5) {
            s.recentRoundSales.removeFirst();
        }
        if (s.roundInNight % 5 == 0) {
            updateTopSalesForecast();
        }
    }

    private void updateTopSalesForecast() {
        Map<String, Integer> aggregate = new HashMap<>();
        for (Map<String, Integer> roundSales : s.recentRoundSales) {
            for (Map.Entry<String, Integer> entry : roundSales.entrySet()) {
                aggregate.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        TopSeller wineTop = topSeller(aggregate, "Wine:");
        TopSeller foodTop = topSeller(aggregate, "Food:");
        String wineText = wineTop.count() > 0 ? wineTop.name() + " x" + wineTop.count() : "None";
        String foodText = foodTop.count() > 0 ? foodTop.name() + " x" + foodTop.count() : "None";
        s.topSalesForecastLine = "Top sellers (5r): Wine " + wineText + " | Food " + foodText;
    }

    private TopSeller topSeller(Map<String, Integer> sales, String prefix) {
        if (sales == null || sales.isEmpty()) return new TopSeller("None", 0);
        String best = "None";
        int max = 0;
        for (var entry : sales.entrySet()) {
            String key = entry.getKey();
            int val = entry.getValue();
            if (prefix != null && !key.startsWith(prefix)) continue;
            if (val > max) {
                max = val;
                best = key.replace(prefix, "").trim();
            }
        }
        return new TopSeller(best, max);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
