import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulation {

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
    private static final double CHAOS_BASE_RISE = 3.0;
    private static final double CHAOS_BASE_FALL = 4.0;
    private static final double CHAOS_NEG_RAMP = 0.55;
    private static final double CHAOS_POS_RAMP = 0.65;
    private static final double ACTION_IDENTITY_RANGE = 10.0;
    private static final double ACTION_ALIGN_CHANCE_BOOST = 0.08;
    private static final double ACTION_BALANCED_OUTCOME_BONUS = 0.20;
    private static final double ACTION_ALIGNED_OUTCOME_BONUS = 0.12;
    private static final double ACTION_MISALIGNED_PENALTY = 0.06;

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
    private final MilestoneSystem milestones;
    private final PubIdentitySystem identitySystem;
    private final RumorSystem rumors;
    private final PubLevelSystem pubLevelSystem;
    private final ObservationEngine observationEngine;

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
        this.milestones = new MilestoneSystem(s, log);
        this.eco.setMilestones(milestones);
        this.identitySystem = new PubIdentitySystem(s, log);
        this.pubLevelSystem = new PubLevelSystem();
        this.observationEngine = new ObservationEngine();

        markReportStartIfMissing();

        // Apply persistent upgrade effects at boot
        applyPersistentUpgrades();
        staff.updateTeamMorale();

        if (s.pubName == null || s.pubName.isBlank()) {
            s.pubName = PubNameGenerator.randomName(s.random);
        }

        // Deal exists BETWEEN nights (so you can restock before opening)
        supplierSystem.rollNewDeal();
        log.popup(" Supplier deal", "Available between nights: " + supplierSystem.dealLabel(), "");
    }

    /** Re-apply upgrades that change hard caps / base stats. Call at boot + on buyUpgrade + on openNight. */
    private void applyPersistentUpgrades() {
        pubLevelSystem.updatePubLevel(s);
        // Rack cap + spoil tuning
        int baseRack = s.baseRackCapacity;
        int rackCap = baseRack + upgrades.rackCapBonus();
        s.rack.setCapacity(rackCap);

        // Spoilage window (optional via upgrades later)
        s.rack.setSpoilAfterDays(s.spoilDays);

        // Security baseline bonus from upgrades (kept as separate field to avoid rewriting SecuritySystem)
        s.upgradeSecurityBonus = upgrades.securityBonus();
        s.fohStaffCap = Math.max(1, s.baseStaffCap + s.pubLevelStaffCapBonus + upgrades.staffCapBonus());
        s.bouncerCap = Math.max(1, s.baseBouncerCap + s.pubLevelBouncerCapBonus + upgrades.bouncerCapBonus());
        s.managerCap = Math.max(1, s.baseManagerCap + s.pubLevelManagerCapBonus + upgrades.managerCapBonus());
        s.kitchenChefCap = Math.max(1, s.baseKitchenChefCap + s.pubLevelChefCapBonus + upgrades.chefCapBonus());
        s.kitchenQualityBonus = upgrades.kitchenQualityBonus();
        s.refundRiskReductionPct = upgrades.refundRiskReductionPct();
        s.staffMisconductReductionPct = upgrades.staffMisconductReductionPct();

        // Bar cap bonus is applied per-night (because base pool changes with rep/weekend)
        // We store it so openNight can add it.
        s.upgradeBarCapBonus = upgrades.barCapBonus();

        s.upgradeServeCapBonus = upgrades.serveCapBonus();
        s.upgradeTipBonusPct = upgrades.tipBonusPct();
        s.upgradeEventDamageReductionPct = upgrades.eventDamageReductionPct();
        s.upgradeRiskReductionPct = upgrades.riskReductionPct();
        s.upgradeFoodRackCapBonus = upgrades.foodRackCapBonus();

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
        s.foodRack.setCapacity(s.baseFoodRackCapacity + s.upgradeFoodRackCapBonus + (headChefs * 5));
    }

    public int landlordActionTier() {
        return clamp(s.pubLevel, 1, 5);
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
        if (s.pubLevel < def.getTier()) {
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

    public String upgradeRequirementText(PubUpgrade up) {
        return milestones.upgradeRequirementText(up);
    }

    public String activityRequirementText(PubActivity activity) {
        return milestones.activityRequirementText(activity);
    }

    public void toggleHappyHour(boolean on) {
        if (!s.nightOpen && on) { log.neg(" Happy Hour can only be toggled while the pub is OPEN."); return; }
        s.happyHour = on;
        log.action(on ? " Happy Hour ON - prices halved, traffic may spike." : " Happy Hour OFF");
    }

    public void startActivity(PubActivity a) {
        if (s.nightOpen) { log.neg("Activities can only be scheduled between nights."); return; }
        if (!milestones.isActivityUnlocked(a)) { log.neg("That activity is not unlocked yet."); return; }
        if (!s.unlockedActivities.contains(a)) { log.neg("That activity is not unlocked yet."); return; }
        if (s.scheduledActivity != null) { log.info("Activity already scheduled."); return; }
        if (a.getRequiredLevel() > 0 && s.pubLevel < a.getRequiredLevel()) {
            log.neg("That activity requires pub level " + a.getRequiredLevel() + ".");
            return;
        }
        if (a.getRequiredIdentity() != null && s.currentIdentity != a.getRequiredIdentity()) {
            log.neg("That activity requires identity: " + a.getRequiredIdentity() + ".");
            return;
        }
        if (a.getRequiredUpgrade() != null && !s.ownedUpgrades.contains(a.getRequiredUpgrade())) {
            log.neg("That activity requires upgrade: " + a.getRequiredUpgrade().getLabel() + ".");
            return;
        }
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
        if (!milestones.canBuyUpgrade(up)) { log.neg("Upgrade locked. Hit a milestone first."); return; }

        if (!eco.tryPay(up.getCost(), TransactionType.UPGRADE, "Upgrade: " + up.getLabel(), CostTag.UPGRADE)) return;

        int nights = 1 + s.random.nextInt(4);
        s.pendingUpgradeInstalls.add(new PendingUpgradeInstall(up, nights, nights));
        log.upgrade(" Upgrade ordered: ", up.getLabel(), " (ETA " + nights + " night(s)).", UILogger.Tone.POS);
        eco.applyRep(+2, "Upgrade hype");

        if (s.ownedUpgrades.size() == 3) log.event(" Milestone: 3 upgrades owned - your pub is becoming a 'place'.");
        if (s.ownedUpgrades.size() == 6) log.event(" Milestone: 6 upgrades - locals start calling it 'their' pub. Dangerous.");
    }

    private boolean isUpgradeInstalling(PubUpgrade up) {
        for (PendingUpgradeInstall install : s.pendingUpgradeInstalls) {
            if (install.upgrade() == up) return true;
        }
        return false;
    }

    public void hireStaff(Staff.Type t) {
        if (s.nightOpen) { log.neg("Hire staff between nights."); return; }

        if (t == Staff.Type.MANAGER) {
            if (s.managerPoolCount() >= s.managerCap) {
                log.info("Manager cap reached (" + s.managerCap + ").");
                return;
            }
            Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random);
            s.generalManagers.add(hire);
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
            Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random);
            s.bohStaff.add(hire);
            staff.updateTeamMorale();
            updateKitchenInventoryCap();
            log.pos(" Hired: " + hire);
            return;
        } else {
            if (t == Staff.Type.ASSISTANT_MANAGER && s.managerPoolCount() >= s.managerCap) {
                log.info("Manager cap reached (" + s.managerCap + ").");
                return;
            }
            if (s.fohStaff.size() >= s.fohStaffCap) {
                log.neg("FOH staff cap reached (" + s.fohStaffCap + ").");
                return;
            }
        }
        Staff hire = StaffFactory.createStaff(s.nextStaffId++, StaffNameGenerator.randomName(s.random), t, s.random);
        s.fohStaff.add(hire);
        staff.updateTeamMorale();
        updateKitchenInventoryCap();
        log.pos(" Hired: " + hire);
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
        staff.updateTeamMorale();
        updateKitchenInventoryCap();

        log.event(" Fired manager. Paid accrued wages.");
        eco.applyRep(-2, "Manager fired (panic)");
    }

    public void hireBouncerTonight() { security.hireBouncerTonight(); }
    public void upgradeSecurity() { security.upgradeBaseSecurity(); }
    public double peekSecurityUpgradeCost() { return security.nextUpgradeCost(); }

    public boolean canBuyUpgrade(PubUpgrade up) { return milestones.canBuyUpgrade(up); }
    public boolean isActivityUnlocked(PubActivity a) { return milestones.isActivityUnlocked(a); }

    // --------------------
    // Credit lines + loan shark
    // --------------------
    public void openCreditLine(Bank bank) {
        if (bank == null) return;
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
        s.loanShark.openLoan(amount, apr);
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

        staff.updateTeamMorale();

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

        if (s.scheduledActivity != null && s.absDayIndex() >= s.scheduledActivity.startAbsDayIndex()) {
            s.activityTonight = s.scheduledActivity.activity();
            s.scheduledActivity = null;
            s.weekActivityNights++;
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
        int pool = clamp((int)Math.round(basePool * baseTrafficMultiplier()), 5, 28);

        //  upgrades actually expand the bar cap
        s.maxBarOccupancy = pool + s.upgradeBarCapBonus + s.pubLevelBarCapBonus;
        if (s.maxBarOccupancy < 5) s.maxBarOccupancy = 5;

        punters.seedNightPunters(pool);

        log.header(" " + s.pubName + " OPEN - " + s.dayName() + " | Week " + s.weekCount);
        log.info("Punters in bar: " + s.nightPunters.size() + "/" + s.maxBarOccupancy);
        log.info("Inventory: " + s.rack.count() + "/" + s.rack.getCapacity());
        log.popup("Supplier deal", "Locked for this night: " + supplierSystem.dealLabel(), "");
        if (s.tempServeBonusTonight > 0) {
            log.popup(" Milestone perk", "+" + s.tempServeBonusTonight + " serve capacity tonight.", "");
        }
    }

    public void playRound() {
        if (!s.nightOpen) return;

        s.roundInNight++;
        tickLandlordActionCooldowns();
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

        // 1) Rent accrues gradually
        eco.accrueDailyRent();

        // 1b) Operating costs per round (tiny now, matters later)
        double opCost = staff.roundOperatingCost(s.nightPunters.size());
        if (!eco.tryPay(opCost, TransactionType.OTHER, "Operating costs (this round)", CostTag.OPERATING)) return;
        s.nightRoundCostsTotal += opCost;

        // 2) Reputation drift
        int repDrift = upgrades.repDriftPerRound();
        int repStaff = staff.repDeltaThisRound(s.random);
        eco.applyRep(repDrift + repStaff, "Atmosphere (upgrades+staff)");

        // 3) Effective price multiplier
        //  Happy Hour halves prices (true "sell-off" button)
        double activityPriceAdj = 1.0 + activities.priceMultiplierPct();
        double effectiveMult = s.priceMultiplier * (s.happyHour ? 0.50 : 1.0) * activityPriceAdj;
        effectiveMult = Math.max(0.50, Math.min(2.50, effectiveMult));
        s.recordWeeklyPriceMultiplier(effectiveMult);

        // 4) Capacity this round
        int serveCap = staff.totalServeCapacity();

        // 5) Traffic multiplier
        double trafficMult =
                upgrades.trafficMultiplier()
                        * activities.trafficMultiplier()
                        * baseTrafficMultiplier()
                        * identityTrafficMultiplier()
                        * rumorTrafficMultiplier()
                        * (1.0 + s.landlordTrafficBonusPct);

        if (rumors != null) {
            trafficMult *= rumors.trafficMultiplier();
        }
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
        int sec = security.effectiveSecurity() + s.upgradeSecurityBonus; //  upgrade security applies
        sec = Math.max(0, sec);

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

        int servedCount = Math.min(serveCap, demand);
        int unserved = Math.max(0, demand - servedCount);
        s.unservedThisWeek += unserved;

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
        int fightsThisRound = Math.max(0, s.nightFights - fightsBefore);
        int refundsThisRound = Math.max(0, s.nightRefunds - refundsBefore);
        updateObservationLine(barCount, unserved, fightsThisRound, eventsThisRound, refundsThisRound);
        punters.refreshChaosContributions();
        s.chaos = recomputeChaos(barCount, demand, serveCap, unserved, fightsThisRound, refundsThisRound, eventsThisRound);

        checkHighRepScandal();

        handleStaffMisconduct(sec);
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

        staff.adjustMoraleAfterRound(unserved, eventsThisRound, s.reputation, tipRate, sec, s.chaos);

        if (s.consecutiveNeg100Rounds >= 3) {
            closeNight("Reputation collapsed. Licence revoked! (-100 for 3 rounds).");
            log.header(" GAME OVER");
            return;
        }

        if (s.roundInNight >= s.closingRound) {
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
            int repHit = Math.max(1, (int)Math.ceil(remaining / 3.0));
            eco.applyRep(-repHit, "Closed early (" + remaining + " rounds left)");
            log.action(" Closed early - locals notice. Rep -" + repHit);
        }

        s.nightOpen = false;
        log.header(" PUB CLOSED");
        log.info(reason);

        // end of day
        s.dayIndex = (s.dayIndex + 1) % 7;
        s.dayCounter++;

        accrueSecurityUpkeep();

        // between-nights spice (v2 event system)
        events.runBetweenNightEvents(Math.max(0, security.effectiveSecurity() + s.upgradeSecurityBonus));

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

        staff.accrueDailyWages();
        s.wagesAccruedThisWeek = staff.wagesDue();

        processPendingUpgradeInstallsAtNightEnd();

        double decay = (s.nightFights > 0 || s.nightUnserved > 6) ? 1.0 : 2.0;
        s.chaos = Math.max(0.0, s.chaos - decay);

        supplierSystem.rollNewDeal();
        log.popup(" Supplier deal", "New deal available: " + supplierSystem.dealLabel(), "");

        showEndOfNightReport();

        if (s.dayIndex == 0) {
            endOfWeek();
            s.weekCount++;
        }

        milestones.onNightEnd();
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
                s.ownedUpgrades.add(up);
                applyPersistentUpgrades();
                if (up == PubUpgrade.KITCHEN_SETUP
                        || up == PubUpgrade.KITCHEN
                        || up == PubUpgrade.NEW_KITCHEN_PLAN
                        || up == PubUpgrade.KITCHEN_EQUIPMENT) {
                    s.kitchenUnlocked = true;
                    log.event(" Kitchen unlocked. Food supplier now available.");
                }
                log.popupUpgrade(" Upgrade installed", up.getLabel(), " is now active.", "");
            } else {
                s.pendingUpgradeInstalls.set(i, new PendingUpgradeInstall(install.upgrade(), remaining, install.totalNights()));
            }
        }
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
        identitySystem.updateWeeklyIdentity();
        rumors.updateWeeklyRumors();
        endOfWeekReport();
        preparePaydayBills(wagesDue, tipsDue);
        milestones.onWeekEnd();
        applyPersistentUpgrades();
        if (s.wageServePenaltyWeeks > 0) {
            s.wageServePenaltyWeeks = Math.max(0, s.wageServePenaltyWeeks - 1);
            if (s.wageServePenaltyWeeks == 0) s.wageServePenaltyPct = 0.0;
        }
        s.fightsThisWeek = 0;
        s.weekRefundTotal = 0.0;
        s.weekRevenue = 0.0;
        s.weekCosts = 0.0;
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
        baseChance = Math.max(0.04, Math.min(0.25, baseChance));

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
        if (s.teamMorale <= 40) return RumorTone.NEGATIVE;
        if (s.teamMorale >= 70) return RumorTone.POSITIVE;
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
        double interest = account.getBalance() * ((baseApr + account.getPenaltyAddOnApr()) / 52.0);
        if (interest <= 0.0) return;
        account.addBalance(interest);
        log.info(label + " credit interest +GBP " + String.format("%.2f", interest));
    }

    private void applyLoanSharkWeeklyInterest() {
        if (!s.loanShark.isOpen() || s.loanShark.getBalance() <= 0.0) return;
        double interest = s.loanShark.weeklyInterestDue();
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
        double creditLines = 0.0;
        for (CreditLine line : s.creditLines.getOpenLines()) {
            if (!line.isEnabled() || line.getBalance() <= 0.0) continue;
            creditLines += line.getWeeklyPayment();
        }
        double shark = s.loanShark.isOpen() ? s.loanShark.minPaymentDue() : 0.0;
        double total = supplier + wages + rent + security + creditLines + shark;
        return new WeeklyDueBreakdown(total, supplier, wages, rent, security, creditLines, shark);
    }

    public record WeeklyDueBreakdown(
            double total,
            double supplier,
            double wages,
            double rent,
            double security,
            double creditLines,
            double loanShark
    ) {}

    public record LandlordActionAvailability(boolean canUse, String reason) {}

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

        for (PaydayBill bill : bills) {
            double targetAmount = Math.max(0.0, Math.min(bill.getFullDue(), bill.getSelectedAmount()));
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

        updateNoDebtUsageAfterPayday();
        double totalLimit = s.totalCreditLimit();
        s.creditUtilization = totalLimit > 0.0 ? (s.creditLines.totalBalance() / totalLimit) : 0.0;
        bills.clear();
        s.paydayReady = false;
    }

    private void applySupplierPayment(double amount, double minDue, boolean paidFull, String referenceId) {
        SupplierTradeCredit account = "SUPPLIER_FOOD".equals(referenceId)
                ? s.supplierFoodCredit
                : s.supplierWineCredit;
        account.applyPayment(amount);
        if (amount + 0.01 < minDue) {
            double shortfall = Math.max(0.0, minDue - amount);
            double lateFee = Math.max(6.0, shortfall * 0.08);
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
        double lateFee = Math.max(5.0, shortfall * 0.10);
        line.addBalance(lateFee);
        line.setPenaltyAddOnApr(Math.min(0.25, line.getPenaltyAddOnApr() + 0.015));
        log.neg("Late fee on " + line.getLenderName() + ": GBP " + String.format("%.2f", lateFee));
    }

    private void applyLoanSharkPenalty(double shortfall) {
        double lateFee = Math.max(8.0, shortfall * 0.12);
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
                * activities.trafficMultiplier() * (1.0 + s.landlordTrafficBonusPct);
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
                + "\n\nWages / Stability:"
                + "\nWages due (this week): GBP " + fmt2(staff.wagesDue())
                + "\nStatus: " + (s.wagesPaidLastWeek ? "Paid" : "Unpaid")
                + "\nConsecutive misses: " + s.consecutiveMissedWagePayments
                + "\nNext escalation: " + wageEscalationWarning()
                + "\nRecovery: " + wageRecoveryGuidance()
                + "\nPrice multiplier avg: " + fmt2(avgPriceMultiplier())
                + "\nPrice volatility: " + fmt2(s.weekPriceMultiplierAbsDelta);

        String operations = "Night: " + (s.nightOpen ? "OPEN" : "CLOSED")
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
                + "\nActivity traffic: x" + fmt2(activities.trafficMultiplier())
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

        String logSummary = "Night events: " + s.nightEvents
                + "\nBetween-night: " + s.lastBetweenNightEventSummary;

        return new MetricsSnapshot(
                "Cash: GBP " + fmt2(s.cash),
                "Debt: GBP " + fmt2(creditBalance),
                "Reputation: " + s.reputation + " (" + mood + ")",
                " " + s.pubName + " (Lv " + s.pubLevel + ")",
                "Weekly Costs (Due at Payday): GBP " + fmt2(weeklyMinDueTotal()),
                "Week " + s.weekCount + "  " + s.dayName() + " | Night " + s.nightCount,
                s.nightOpen
                        ? ("Night OPEN  Round " + s.roundInNight + "/" + s.closingRound
                        + " | Bar " + s.nightPunters.size() + "/" + s.maxBarOccupancy)
                        : "Night CLOSED  Ready",
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
                staffDetail
        );
    }

    private String buildUpgradeDependencyText() {
        StringBuilder sb = new StringBuilder();
        for (PubUpgrade up : PubUpgrade.values()) {
            boolean owned = s.ownedUpgrades.contains(up);
            String requirement = milestones.upgradeRequirementText(up);
            String status;
            if (owned) {
                status = "Unlocked";
            } else if (requirement == null) {
                status = "Available";
            } else {
                status = "Locked (" + requirement + ")";
            }
            sb.append("- ").append(up.getLabel()).append(": ").append(status).append("\n");
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
            if (!milestones.isActivityUnlocked(activity)) continue;
            List<String> requirements = new java.util.ArrayList<>();
            if (activity.requiresUnlock()) requirements.add("Milestone unlock");
            if (activity.getRequiredUpgrade() != null) {
                requirements.add(activity.getRequiredUpgrade().getLabel() + " installed");
            }
            if (activity.getRequiredLevel() > 0) {
                requirements.add("Level " + activity.getRequiredLevel() + "+");
            }
            if (activity.getRequiredIdentity() != null) {
                requirements.add("Identity " + activity.getRequiredIdentity().name());
            }
            String requirementMet = requirements.isEmpty()
                    ? "No requirement"
                    : String.join(", ", requirements);
            String effects = "traffic +" + (int)Math.round(activity.getTrafficBonusPct() * 100) + "%"
                    + ", cap +" + activity.getCapacityBonus()
                    + ", rep " + (activity.getRepInstantDelta() >= 0 ? "+" : "") + activity.getRepInstantDelta();
            sb.append("- ").append(activity.getLabel())
                    .append(" | ").append(requirementMet)
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
        int combinedCap = s.fohStaffCap + s.kitchenChefCap;
        int totalStaff = s.fohStaff.size() + s.bohStaff.size() + s.generalManagers.size();
        double tipRate = staff.tipRate();
        int kitchenCapacity = 0;
        for (Staff st : s.bohStaff) {
            kitchenCapacity += st.getKitchenCapacity();
        }

        sb.append("Total staff: ").append(totalStaff).append("/").append(combinedCap)
                .append(" (FOH ").append(s.fohStaff.size()).append("/").append(s.fohStaffCap)
                .append(", BOH ").append(s.bohStaff.size()).append("/").append(s.kitchenChefCap).append(")");
        sb.append("\nManager slots: ").append(s.managerPoolCount()).append("/").append(s.managerCap)
                .append(" (GM ").append(s.generalManagers.size()).append(", AM ").append(s.assistantManagerCount()).append(")");
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

    private void handleStaffMisconduct(int security) {
        List<Staff> eligible = new java.util.ArrayList<>();
        eligible.addAll(s.fohStaff);
        eligible.addAll(s.bohStaff);
        if (eligible.isEmpty()) return;

        int minMorale = 100;
        for (Staff st : eligible) {
            minMorale = Math.min(minMorale, st.getMorale());
        }

        double chance = computeMisconductChance(security, minMorale);
        if (s.random.nextDouble() > chance) return;

        Staff offender = pickMisconductOffender(eligible);
        if (offender == null) return;

        boolean boh = s.bohStaff.contains(offender);
        MisconductType type = boh ? rollBohMisconduct() : rollFohMisconduct();
        s.staffMisconductThisWeek++;
        s.staffIncidentThisNight = true;
        s.staffIncidentThisRound = true;

        String driverLine = buildMisconductDriverLine(security, minMorale, chance);
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

    private double computeMisconductChance(int security, int minMorale) {
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

    private String buildMisconductDriverLine(int security, int minMorale, double chance) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chance ").append(String.format("%.1f%%", chance * 100));
        if (s.teamMorale < 55) sb.append(" | morale low");
        if (minMorale < 35) sb.append(" | very low individual morale");
        if (s.chaos >= 35) sb.append(" | chaos high");
        if (security > 0) sb.append(" | security mitigated");
        if (s.staffMisconductReductionPct > 0.001) sb.append(" | upgrades mitigated");
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
                s.banksLocked = false;
                log.pos("Business stability improving. Banks are willing to talk again.");
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

    private String buildFinanceBankingText(double creditBalance, double creditWeeklyDue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cash: GBP ").append(fmt2(s.cash)).append("\n");
        sb.append("Credit score: ").append(s.creditScore)
                .append(" | Utilisation: ").append(String.format("%.0f%%", s.creditUtilization * 100)).append("\n");
        sb.append("Total bank debt: GBP ").append(fmt2(creditBalance)).append(" / ")
                .append(fmt2(s.totalCreditLimit())).append("\n");
        sb.append("Weekly bank repayments due: GBP ").append(fmt2(creditWeeklyDue)).append("\n");
        if (s.loanShark.isOpen()) {
            sb.append("Loan shark balance: GBP ").append(fmt2(s.loanShark.getBalance()))
                    .append(" | Weekly due ").append(fmt2(s.loanShark.minPaymentDue()))
                    .append(" | APR ").append(String.format("%.2f", s.loanShark.getApr() * 100)).append("%\n");
        } else {
            sb.append("Loan shark: None\n");
        }
        sb.append("\nCredit lines:\n");
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
        sb.append(pubLevelSystem.progressionSummary(s)).append("\n");
        return sb.toString();
    }

    private String buildSecurityDetailText(int sec) {
        StringBuilder sb = new StringBuilder();
        sb.append("Effective security: ").append(sec).append("\n");
        sb.append("Bouncers hired: ").append(s.bouncersHiredTonight).append("/").append(s.bouncerCap).append("\n");
        sb.append("Bouncer rep mitigation: x").append(fmt2(s.bouncerRepDamageMultiplier())).append("\n");
        sb.append("Bouncer reductions: theft ").append(pct(s.bouncerTheftReduction))
                .append(" | negative ").append(pct(s.bouncerNegReduction))
                .append(" | fights ").append(pct(s.bouncerFightReduction)).append("\n");
        return sb.toString();
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
            base = CHAOS_BASE_RISE;
            streakDelta = CHAOS_BASE_RISE * (s.negStreak * CHAOS_NEG_RAMP);
        } else if (classification == RoundClassification.MOSTLY_POSITIVE) {
            s.posStreak += 1;
            s.negStreak = 0;
            base = -CHAOS_BASE_FALL;
            streakDelta = -CHAOS_BASE_FALL * (s.posStreak * CHAOS_POS_RAMP);
        } else {
            s.posStreak = 0;
            s.negStreak = 0;
        }

        double delta = base + streakDelta;
        s.chaos = Math.max(0.0, Math.min(100.0, s.chaos + delta));

        s.lastRoundClassification = formatClassification(classification);
        s.lastChaosDelta = delta;
        s.lastChaosDeltaBase = base;
        s.lastChaosDeltaStreak = streakDelta;
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
        return repMult * weekendMult * identityMult * levelMult;
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
        return s.lastRoundClassification
                + " | pos " + s.posStreak
                + " | neg " + s.negStreak
                + "\nChaos delta: " + fmt1(s.lastChaosDelta)
                + " (base " + fmt1(s.lastChaosDeltaBase)
                + ", streak " + fmt1(s.lastChaosDeltaStreak) + ")"
                + "\nMorale mult: neg x" + fmt2(s.lastChaosMoraleNegMult)
                + ", pos x" + fmt2(s.lastChaosMoralePosMult);
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

    private void updateObservationLine(int barCount, int unserved, int fightsThisRound, int eventsThisRound, int refundsThisRound) {
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
        s.observationLine = result.text();
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
            refundChance = Math.max(0.04, Math.min(0.45, refundChance));

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
                .append(" | Refund count: ").append(s.nightRefunds);

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
