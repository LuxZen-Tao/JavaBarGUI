// EventSystem.java
public class EventSystem {

    private static final int SEC_MIN = 0; // no upper cap

    private static final int VANDALISM_BASE_CHANCE = 10;
    private static final int EGGING_BASE_CHANCE = 9;
    private static final int GLASS_BREAKAGE_BASE_CHANCE = 12;
    private static final int BURGLARY_ATTEMPT_BASE_CHANCE = 8;
    private static final int BURGLARY_BASE_CHANCE = 4;
    private static final int LEAK_BASE_CHANCE = 6;
    private static final int FIRE_BASE_CHANCE = 3;
    private static final int POWER_TRIP_BASE_CHANCE = 10;
    private static final int HEALTH_INSPECTION_BASE_CHANCE = 7;
    private static final int GRAFFITI_BASE_CHANCE = 8;
    private static final int NOISE_COMPLAINT_BASE_CHANCE = 7;
    private static final int PEST_CONTROL_BASE_CHANCE = 5;
    private static final int RUBBISH_STRIKE_BASE_CHANCE = 6;
    private static final int DELIVERY_DELAY_BASE_CHANCE = 6;
    private static final int LICENCE_AUDIT_BASE_CHANCE = 4;
    private static final int COMMUNITY_SHOUTOUT_BASE_CHANCE = 6;
    private static final int KITCHEN_INSPECTION_BASE_CHANCE = 6;

    private static final double VANDALISM_SECURITY_EFFECT = 0.90;
    private static final double EGGING_SECURITY_EFFECT = 0.85;
    private static final double GLASS_BREAKAGE_SECURITY_EFFECT = 0.80;
    private static final double BURGLARY_ATTEMPT_SECURITY_EFFECT = 0.85;
    private static final double BURGLARY_SECURITY_EFFECT = 0.75;
    private static final double LEAK_SECURITY_EFFECT = 0.90;
    private static final double FIRE_SECURITY_EFFECT = 0.70;
    private static final double POWER_TRIP_SECURITY_EFFECT = 0.95;
    private static final double HEALTH_INSPECTION_SECURITY_EFFECT = 0.80;
    private static final double GRAFFITI_SECURITY_EFFECT = 0.88;
    private static final double NOISE_COMPLAINT_SECURITY_EFFECT = 0.90;
    private static final double PEST_CONTROL_SECURITY_EFFECT = 0.85;
    private static final double RUBBISH_STRIKE_SECURITY_EFFECT = 0.90;
    private static final double DELIVERY_DELAY_SECURITY_EFFECT = 0.90;
    private static final double LICENCE_AUDIT_SECURITY_EFFECT = 0.85;
    private static final double COMMUNITY_SHOUTOUT_SECURITY_EFFECT = 1.0;
    private static final double KITCHEN_INSPECTION_SECURITY_EFFECT = 0.85;

    private static final EventRange LEAK_MINOR = new EventRange(20, 40, -1, "Minor");
    private static final EventRange LEAK_MODERATE = new EventRange(45, 80, -2, "Moderate");
    private static final EventRange LEAK_MAJOR = new EventRange(90, 140, -3, "Major");

    private static final EventRange FIRE_SMALL = new EventRange(60, 120, -3, "Small");
    private static final EventRange FIRE_MEDIUM = new EventRange(130, 220, -5, "Medium");
    private static final EventRange FIRE_LARGE = new EventRange(240, 360, -7, "Large");

    private final GameState s;
    private final EconomySystem eco;
    private final UILogger log;
    private double chaosFactor = 0.0;

    public EventSystem(GameState s, EconomySystem eco, UILogger log) {
        this.s = s;
        this.eco = eco;
        this.log = log;
    }

    public void setChaosFactor(double factor) {
        chaosFactor = Math.max(0.0, Math.min(1.0, factor));
    }

    private void popupEvent(String title, String body, int repDelta, double cashDelta, int moraleDelta, String tags) {
        log.popup(new EventCard(title, body, repDelta, cashDelta, moraleDelta, tags));
    }

    public void maybeEventGuaranteed(int upgradeBonus, int activityBonus) {
        s.roundsSinceLastEvent++;

        boolean force = s.roundsSinceLastEvent >= 5;
        int chance = 12 + upgradeBonus + activityBonus + (int)Math.round(chaosFactor * 10);
        if (s.reputation <= -40) chance += 6;
        if (s.reputation >= 60) chance += 3;
        chance = (int)Math.round(chance * seasonalRoundEventChanceMultiplier());

        boolean roll = s.random.nextInt(100) < chance;
        if (force || roll) {
            s.roundsSinceLastEvent = 0;
            s.nightEvents++;
            s.reportEvents++;
            doRandomEvent();
        }
    }

    /** Between-nights events (Night Events v2). */
    public int runBetweenNightEvents(int effectiveSecurity) {
        int sec = Math.max(SEC_MIN, effectiveSecurity); // no cap

        double chanceMult = chanceMultiplier(sec);
        double dmgMult = damageMultiplier(sec);
        double repChanceMult = (s.reputation >= 60) ? 0.85 : (s.reputation <= -40 ? 1.20 : 1.0);
        double repDmgMult = (s.reputation >= 60) ? 0.90 : (s.reputation <= -40 ? 1.15 : 1.0);
        double chaosMult = 1.0 + (chaosFactor * 0.35);
        chanceMult *= repChanceMult * chaosMult * seasonalBetweenNightChanceMultiplier();
        dmgMult *= repDmgMult * chaosMult;
        int triggered = 0;

        if (s.reputation >= 60) {
            if (handlePositiveBetweenNight("Community shoutout",
                    COMMUNITY_SHOUTOUT_BASE_CHANCE, COMMUNITY_SHOUTOUT_SECURITY_EFFECT,
                    6, 12, sec, chanceMult, "Local paper praise")) {
                triggered++;
            }
        }

        if (handleEvent("Vandalism", VANDALISM_BASE_CHANCE, VANDALISM_SECURITY_EFFECT, -2,
                20, 45, 0, 0, sec, chanceMult, dmgMult, "Repairs (vandalism)")) triggered++;
        if (handleEvent("Rival pub egging", EGGING_BASE_CHANCE, EGGING_SECURITY_EFFECT, -2,
                15, 35, 0, 0, sec, chanceMult, dmgMult, "Clean-up & repairs (egging)")) triggered++;
        if (handleEvent("Glass breakage", GLASS_BREAKAGE_BASE_CHANCE, GLASS_BREAKAGE_SECURITY_EFFECT, -1,
                10, 22, 0, 0, sec, chanceMult, dmgMult, "Glass replacement")) triggered++;
        if (handleEvent("Graffiti cleanup", GRAFFITI_BASE_CHANCE, GRAFFITI_SECURITY_EFFECT, -1,
                8, 18, 0, 0, sec, chanceMult, dmgMult, "Graffiti cleanup")) triggered++;
        if (handleEvent("Noise complaint fine", NOISE_COMPLAINT_BASE_CHANCE, NOISE_COMPLAINT_SECURITY_EFFECT, -2,
                12, 25, 0, 0, sec, chanceMult, dmgMult, "Noise complaint fine")) triggered++;
        if (handleEvent("Pest control call-out", PEST_CONTROL_BASE_CHANCE, PEST_CONTROL_SECURITY_EFFECT, -2,
                20, 40, 0, 0, sec, chanceMult, dmgMult, "Pest control")) triggered++;
        if (handleEvent("Rubbish pickup strike", RUBBISH_STRIKE_BASE_CHANCE, RUBBISH_STRIKE_SECURITY_EFFECT, -1,
                10, 22, 0, 0, sec, chanceMult, dmgMult, "Rubbish removal")) triggered++;
        if (handleEvent("Supplier delivery delay", DELIVERY_DELAY_BASE_CHANCE, DELIVERY_DELAY_SECURITY_EFFECT, -1,
                5, 15, 1, 3, sec, chanceMult, dmgMult, "Rush delivery")) triggered++;
        if (handleEvent("Licence paperwork audit", LICENCE_AUDIT_BASE_CHANCE, LICENCE_AUDIT_SECURITY_EFFECT, -1,
                18, 35, 0, 0, sec, chanceMult, dmgMult, "Licence paperwork")) triggered++;

        if (s.kitchenUnlocked) {
            if (handleEvent("Kitchen inspection", KITCHEN_INSPECTION_BASE_CHANCE, KITCHEN_INSPECTION_SECURITY_EFFECT, -2,
                    15, 30, 0, 0, sec, chanceMult, dmgMult, "Kitchen compliance")) {
                triggered++;
            }
        }

        if (handleBurglaryAttempt(sec, chanceMult, dmgMult)) triggered++;
        if (handleBurglary(sec, chanceMult, dmgMult)) triggered++;

        if (handleLeak(sec, chanceMult, dmgMult)) triggered++;
        if (handleFire(sec, chanceMult, dmgMult)) triggered++;
        if (handlePowerTrip(sec, chanceMult, dmgMult)) triggered++;
        if (handleHealthInspection(sec, chanceMult, dmgMult)) triggered++;

        if (triggered == 0) {
            s.lastBetweenNightEventSummary = "None";
        }
        return triggered;
    }


    double seasonalRoundEventChanceMultiplier() {
        return seasonalEventChanceMultiplier(1.0, 1.05, 0.95, 1.08);
    }

    double seasonalBetweenNightChanceMultiplier() {
        return seasonalEventChanceMultiplier(1.0, 1.06, 0.94, 1.10);
    }

    private double seasonalEventChanceMultiplier(double examMult, double touristMult, double winterMult, double derbyMult) {
        if (!FeatureFlags.FEATURE_SEASONS) return 1.0;

        java.util.List<SeasonTag> tags = new SeasonCalendar(s).getActiveSeasonTags();
        if (tags.isEmpty()) return 1.0;

        double mult = 1.0;
        for (SeasonTag tag : tags) {
            switch (tag) {
                case EXAM_SEASON -> mult *= examMult;
                case TOURIST_WAVE -> mult *= touristMult;
                case WINTER_SLUMP -> mult *= winterMult;
                case DERBY_WEEK -> mult *= derbyMult;
            }
        }
        return mult;
    }

    private boolean handleBurglaryAttempt(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(BURGLARY_ATTEMPT_BASE_CHANCE, BURGLARY_ATTEMPT_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        double successChance = Math.max(0.20, 0.70 - (sec * 0.06));

        log.popup("Between nights", "Burglary attempt (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        if (!roll(successChance)) {
            popupEvent("Between nights", "Burglary attempt stopped by security.", 0, 0.0, 0, "SECURITY");
            log.pos("Security scared them off. No losses.");
            s.lastBetweenNightEventSummary = "Burglary attempt (stopped)";
            return true;
        }

        double cashLoss = applyCost(30, 60, dmgMult, "Burglary losses (attempt)");
        int repHit = applyRep(-2, dmgMult, "Burglary rumours");
        int invLost = applyInventoryLoss(1, 3, dmgMult);
        popupEvent("Between nights", "Burglary attempt succeeded. Bottles lost: " + invLost + ".", repHit, -cashLoss, 0, "DAMAGE");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ", bottles -" + invLost + ".");
        setBetweenNightSummary("Burglary attempt", cashLoss, repHit, invLost);
        return true;
    }

    private boolean handleBurglary(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(BURGLARY_BASE_CHANCE, BURGLARY_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        log.popup("Between nights", "Burglary (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        double cashLoss = applyCost(80, 160, dmgMult, "Burglary losses");
        int repHit = applyRep(-4, dmgMult, "Burglary fallout");
        int invLost = applyInventoryLoss(3, 6, dmgMult);
        popupEvent("Between nights", "Burglary overnight. Bottles lost: " + invLost + ".", repHit, -cashLoss, 0, "DAMAGE");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ", bottles -" + invLost + ".");
        setBetweenNightSummary("Burglary", cashLoss, repHit, invLost);
        return true;
    }

    private boolean handleLeak(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(LEAK_BASE_CHANCE, LEAK_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        EventRange tier = rollTier(LEAK_MINOR, LEAK_MODERATE, LEAK_MAJOR);

        log.popup("Between nights", "Leak (" + tier.label + ") (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        double cashLoss = applyCost(tier.minCost, tier.maxCost, dmgMult, "Repairs (leak)");
        int repHit = applyRep(tier.repHit, dmgMult, "Service disruption");
        popupEvent("Between nights", "Leak (" + tier.label + ").", repHit, -cashLoss, 0, "DAMAGE");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ".");
        setBetweenNightSummary("Leak (" + tier.label + ")", cashLoss, repHit, 0);
        return true;
    }

    private boolean handleFire(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(FIRE_BASE_CHANCE, FIRE_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        EventRange tier = rollTier(FIRE_SMALL, FIRE_MEDIUM, FIRE_LARGE);

        log.popup("Between nights", "Fire (" + tier.label + ") (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        double cashLoss = applyCost(tier.minCost, tier.maxCost, dmgMult, "Repairs (fire)");
        int repHit = applyRep(tier.repHit, dmgMult, "Fire damage");
        int invLost = applyInventoryLoss(2, 6, dmgMult);
        popupEvent("Between nights", "Fire (" + tier.label + "). Bottles lost: " + invLost + ".", repHit, -cashLoss, 0, "DAMAGE");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ", bottles -" + invLost + ".");
        setBetweenNightSummary("Fire (" + tier.label + ")", cashLoss, repHit, invLost);
        return true;
    }

    private boolean handlePowerTrip(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(POWER_TRIP_BASE_CHANCE, POWER_TRIP_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        log.popup("Between nights", "Power trip (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        double cashLoss = applyCost(15, 30, dmgMult, "Generator reset");
        int repHit = applyRep(-1, dmgMult, "Disruption");
        popupEvent("Between nights", "Power trip overnight.", repHit, -cashLoss, 0, "DAMAGE");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ".");
        setBetweenNightSummary("Power trip", cashLoss, repHit, 0);
        return true;
    }

    private boolean handleHealthInspection(int sec, double chanceMult, double dmgMult) {
        double finalChance = finalChance(HEALTH_INSPECTION_BASE_CHANCE, HEALTH_INSPECTION_SECURITY_EFFECT, chanceMult);
        if (!roll(finalChance)) return false;

        log.popup("Between nights", "Health inspection warning (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        int baseRep = -3;
        if (sec >= 6 || s.hasSkilledManager()) {
            baseRep = -1;
        }
        double cashLoss = applyCost(10, 25, dmgMult, "Compliance fixes");
        int repHit = applyRep(baseRep, dmgMult, "Health inspection");
        popupEvent("Between nights", "Health inspection warning.", repHit, -cashLoss, 0, "REP");
        log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ".");
        setBetweenNightSummary("Health inspection warning", cashLoss, repHit, 0);
        return true;
    }

    private boolean handlePositiveBetweenNight(String name,
                                               int baseChance,
                                               double securityEffect,
                                               double cashMin,
                                               double cashMax,
                                               int sec,
                                               double chanceMult,
                                               String reason) {
        double finalChance = finalChance(baseChance, securityEffect, chanceMult);
        if (!roll(finalChance)) return false;

        log.popup("Between nights", name + " (sec " + sec
                + ", chance " + fmtPct(finalChance) + ")", "");

        double reward = cashMin + (s.random.nextDouble() * (cashMax - cashMin));
        popupEvent("Between nights", name + ".", 2, reward, 0, "CASH");
        eco.addCash(reward, reason);
        eco.applyRep(+2, name);
        s.weekPositiveEvents++;
        s.lastBetweenNightEventSummary = name + " | cash +" + fmt0(reward) + " | rep +2";
        return true;
    }

    private boolean handleEvent(String name,
                                int baseChance,
                                double securityEffect,
                                int baseRep,
                                double cashMin,
                                double cashMax,
                                int invMin,
                                int invMax,
                                int sec,
                                double chanceMult,
                                double dmgMult,
                                String costTag) {
        double finalChance = finalChance(baseChance, securityEffect, chanceMult);
        if (!roll(finalChance)) return false;

        log.popup("Between nights", name + " (sec " + sec
                + ", chance " + fmtPct(finalChance)
                + ", dmg x" + fmtMult(dmgMult) + ")", "");

        double cashLoss = applyCost(cashMin, cashMax, dmgMult, costTag);
        int repHit = applyRep(baseRep, dmgMult, name);
        int invLost = (invMin > 0 || invMax > 0) ? applyInventoryLoss(invMin, invMax, dmgMult) : 0;
        String details = name + (invLost > 0 ? " | Bottles lost: " + invLost : "");
        popupEvent("Between nights", details, repHit, -cashLoss, 0, "DAMAGE");
        s.weekNegativeEvents++;

        if (invLost > 0) {
            log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ", bottles -" + invLost + ".");
        } else {
            log.neg("Losses: GBP " + fmt0(cashLoss) + ", rep " + repHit + ".");
        }
        setBetweenNightSummary(name, cashLoss, repHit, invLost);
        return true;
    }

    private double chanceMultiplier(int security) {
        // With no cap, this bottoms out at 20% chance multiplier.
        double base = Math.max(0.20, 1.0 - (security * 0.08));
        double policyMult = s.securityPolicy != null ? s.securityPolicy.getIncidentChanceMultiplier() : 1.0;
        return Math.max(0.20, base * policyMult * s.upgradeIncidentChanceMultiplier);
    }

    private double damageMultiplier(int security) {
        // With no cap, base bottoms out at 35% before any upgrade reduction.
        double base = Math.max(0.35, 1.0 - (security * 0.06));
        double reduction = Math.max(0.0, Math.min(0.45, s.upgradeEventDamageReductionPct));
        return Math.max(0.25, base * (1.0 - reduction));
    }

    private double finalChance(int baseChance, double securityEffect, double chanceMult) {
        double finalChance = baseChance * chanceMult * securityEffect;
        return Math.max(1.0, Math.min(95.0, finalChance));
    }

    private boolean roll(double chance) {
        return s.random.nextInt(10000) < (int) Math.round(chance * 100);
    }

    private double applyCost(double min, double max, double dmgMult, String tag) {
        double raw = min + (s.random.nextDouble() * (max - min));
        double cost = Math.max(1.0, raw * dmgMult);
        return eco.tryPay(cost, TransactionType.REPAIR, tag, CostTag.EVENT) ? cost : 0.0;
    }

    private int applyRep(int baseRep, double dmgMult, String reason) {
        if (baseRep == 0) return 0;
        double mult = dmgMult;
        int rep = (int) Math.round(baseRep * mult);
        if (baseRep < 0) {
            rep = s.mitigateSecurityRepHit(rep);
        }
        if (rep == 0 && baseRep < 0) rep = -1;
        eco.applyRep(rep, reason);
        if (baseRep < 0) {
            logSecurityMitigation(rep, reason);
        }
        return rep;
    }

    private int applyIncidentRepHit(int repHit, String reason) {
        int mitigated = s.mitigateSecurityRepHit(repHit);
        eco.applyRep(mitigated, reason);
        logSecurityMitigation(mitigated, reason);
        s.addSecurityLog("Night event: " + reason + " | rep " + mitigated);
        return mitigated;
    }

    private void logSecurityMitigation(int repHit, String reason) {
        if (repHit >= 0) return;
        double mult = s.securityIncidentRepMultiplier();
        if (mult >= 0.99) return;
        if (s.bouncersHiredTonight > 0) {
            log.event("Bouncers contained some of the fallout.");
        }
        if (s.cctvRepMitigationPct() > 0.0) {
            log.event("CCTV footage softened the blow.");
        }
        if (s.upgradeRepMitigationPct > 0.0) {
            log.event("Reinforced security upgrades reduced the fallout.");
        }
    }

    private int applyInventoryLoss(int min, int max, double dmgMult) {
        if (s.rack.isEmpty()) return 0;
        int raw = min + s.random.nextInt(max - min + 1);
        int loss = Math.max(0, (int) Math.round(raw * dmgMult));
        if (loss == 0 && raw > 0) loss = 1;

        int removed = 0;
        for (int i = 0; i < loss; i++) {
            Wine w = s.rack.pickRandomBottle(s.random);
            if (w == null) break;
            if (s.rack.removeBottle(w)) removed++;
        }
        return removed;
    }

    private void setBetweenNightSummary(String name, double cashLoss, int repHit, int invLost) {
        String summary = name + " | cash -" + fmt0(cashLoss) + " | rep " + repHit;
        if (invLost > 0) summary += " | bottles -" + invLost;
        s.lastBetweenNightEventSummary = summary;
        s.addSecurityLog("Between-night: " + summary);
    }

    private EventRange rollTier(EventRange a, EventRange b, EventRange c) {
        int roll = s.random.nextInt(3);
        if (roll == 0) return a;
        if (roll == 1) return b;
        return c;
    }

    private String fmtPct(double val) {
        return String.format("%.1f%%", val);
    }

    private String fmtMult(double val) {
        return String.format("%.2f", val);
    }

    private String fmt0(double val) {
        return String.format("%.0f", val);
    }

    private record EventRange(double minCost, double maxCost, int repHit, String label) {
    }

    private void doRandomEvent() {
        double identityBias = (s.currentIdentity != null) ? s.currentIdentity.getEventBias() : 0.0;
        double rumorBias = eventBiasFromRumors(); // uses activeRumors list

        int posChance = (int) Math.round(
                50
                        + (s.reputation / 2.0)
                        + (identityBias * 30)
                        + (rumorBias * 25)
                        - (chaosFactor * 20)
        );

        posChance = Math.max(5, Math.min(95, posChance));

        boolean positive = s.random.nextInt(100) < posChance;

        if (maybeIdentityEvent(positive)) return;

        if (positive) positiveEvent();
        else negativeEvent();
    }


    private void positiveEvent() {
        s.weekPositiveEvents++;
        int roll = s.random.nextInt(100);
        if (s.reputation >= 60) roll -= 10;
        if (s.pubLevel < 2 && roll < 22) roll += 18;
        if (roll < 10) {
            int repDelta = 6;
            popupEvent("Night event", "Local celeb visits.", repDelta, 0.0, 0, "REP");
            eco.applyRep(repDelta, "Celebrity boost");
        } else if (roll < 22) {
            int repDelta = 4;
            double cash = 10;
            popupEvent("Night event", "TV crew films your pub.", repDelta, cash, 0, "REP");
            eco.applyRep(repDelta, "TV boost");
            eco.addCash(cash, "Promo cash");
        } else if (roll < 36) {
            int repDelta = 6;
            popupEvent("Night event", "Influencer story tags you.", repDelta, 0.0, 0, "REP");
            eco.applyRep(repDelta, "Influencer boost");
        } else if (roll < 52) {
            double cash = 40 + s.random.nextInt(31);
            int repDelta = 1;
            popupEvent("Night event", "Corporate booking leaves a deposit.", repDelta, cash, 0, "CASH");
            eco.addCash(cash, "Corporate deposit");
            eco.applyRep(repDelta, "Corporate booking");
        } else if (roll < 66) {
            double cash = 15 + s.random.nextInt(16);
            int repDelta = 2;
            popupEvent("Night event", "Regulars buy a round.", repDelta, cash, 0, "CASH");
            eco.addCash(cash, "Regulars round");
            eco.applyRep(repDelta, "Regulars loyalty");
        } else if (roll < 78) {
            double cash = 12 + s.random.nextInt(9);
            int repDelta = 5;
            popupEvent("Night event", "Brewery collab pops off.", repDelta, cash, 0, "CASH");
            eco.addCash(cash, "Collab sales");
            eco.applyRep(repDelta, "Brewery collab");
        } else if (roll < 88 && s.kitchenUnlocked) {
            double cash = 8 + s.random.nextInt(10);
            int repDelta = 4;
            popupEvent("Night event", "Rave review of the food.", repDelta, cash, 0, "FOOD");
            eco.addCash(cash, "Food buzz");
            eco.applyRep(repDelta, "Food review");
        } else if (roll < 90) {
            popupEvent("Night event", "Supplier freebie (2 House Whites).", 0, 0.0, 0, "STOCK");
            s.rack.addBottles(s.supplier.get(0), 2, s.absDayIndex());
        } else {
            int repDelta = 5;
            popupEvent("Night event", "Best atmosphere tonight.", repDelta, 0.0, 0, "REP");
            eco.applyRep(repDelta, "Atmosphere award");
        }
    }

    private void negativeEvent() {
        s.weekNegativeEvents++;
        double reduction = bouncerNegReduction();
        int roll = s.random.nextInt(100);
        if (s.reputation <= -40) roll += 15;
        if (s.pubLevel < 2 && roll > 95) roll = 85;

        if (roll < 10) {
            log.popup("Night event", "Pub fight kicks off!", "Rep - | Damages");
            triggerFight("Random fight", reduction);
        } else if (roll < 20) {
            log.popup("Night event", "Police visit.", "Rep -");
            int repHit = Math.max(3, (int) Math.round(7 * (1.0 - reduction)));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Police attention"));
            popupEvent("Night event", "Police visit.", -repHit, 0.0, 0, "REP");
        } else if (roll < 30) {
            log.popup("Night event", "Table collapses.", "Rep - | Repairs");
            int repHit = Math.max(3, (int) Math.round(6 * (1.0 - reduction)));
            double repair = Math.max(4, 8 * (1.0 - reduction));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Embarrassment"));
            popupEvent("Night event", "Table collapses.", -repHit, -repair, 0, "DAMAGE");
            eco.tryPay(repair, TransactionType.REPAIR, "Repairs", CostTag.EVENT);
        } else if (roll < 42) {
            log.popup("Night event", "Bad review thread pops off.", "Rep -");
            int repHit = Math.max(4, (int) Math.round(8 * (1.0 - reduction)));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Bad reviews"));
            popupEvent("Night event", "Bad review thread pops off.", -repHit, 0.0, 0, "REP");
        } else if (roll < 52 && s.kitchenUnlocked) {
            log.popup("Night event", "Food poisoning scare.", "Rep - | Refunds");
            int repHit = Math.max(4, (int) Math.round(7 * (1.0 - reduction)));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Food poisoning scare"));
            double loss = 12 + s.random.nextInt(18);
            popupEvent("Night event", "Food poisoning scare.", -repHit, -loss, 0, "REFUND");
            if (eco.tryPay(loss, TransactionType.OTHER, "Refunds (food scare)", CostTag.FOOD)) {
                s.recordRefund(loss);
            }
        } else if (roll < 62) {
            log.popup("Night event", "Glassware shortage slows service.", "Rep - | Costs");
            int repHit = Math.max(2, (int) Math.round(5 * (1.0 - reduction)));
            double cost = Math.max(5, 12 * (1.0 - reduction));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Glassware shortage"));
            popupEvent("Night event", "Glassware shortage slows service.", -repHit, -cost, 0, "DAMAGE");
            eco.tryPay(cost, TransactionType.REPAIR, "Emergency glassware", CostTag.EVENT);
        } else if (roll < 72) {
            log.popup("Night event", "Rowdy stag do swamps the bar.", "Fight risk");
            triggerFight("Stag do chaos", reduction);
            applyIncidentRepHit(-2, "Rowdy stag do");
        } else if (roll < 80) {
            triggerTeenTrouble(reduction);
        } else if (roll < 90) {
            log.popup("Night event", "Supplier no-show causes a scramble.", "Rep - | Stock lost");
            int repHit = Math.abs(applyIncidentRepHit(-2, "Supplier delay"));
            int invLost = applyInventoryLoss(1, 2, 1.0);
            popupEvent("Night event", "Supplier no-show causes a scramble. Bottles lost: " + invLost + ".", -repHit, 0.0, 0, "STOCK");
            if (invLost > 0) log.neg("  - Bottles lost to breakage: -" + invLost);
        } else if (roll < 96) {
            log.popup("Night event", "Staff argument rattles the room.", "Rep -");
            int repHit = Math.max(2, (int) Math.round(4 * (1.0 - reduction)));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Staff drama"));
            popupEvent("Night event", "Staff argument rattles the room.", -repHit, 0.0, 0, "REP");
        } else {
            log.popup("Night event", "Influencer backlash erupts.", "Rep -");
            int repHit = Math.max(5, (int) Math.round(9 * (1.0 - reduction)));
            repHit = Math.abs(applyIncidentRepHit(-repHit, "Influencer backlash"));
            popupEvent("Night event", "Influencer backlash erupts.", -repHit, 0.0, 0, "REP");
        }
    }

    private boolean maybeIdentityEvent(boolean positivePool) {
        if (s.currentIdentity == null) return false;

        int chance = 16 + Math.max(0, s.weekCount / 2);
        if (s.random.nextInt(100) >= chance) return false;

        switch (s.currentIdentity) {
            case RESPECTABLE -> {
                if (!positivePool) return false;
                double cash = 18 + s.random.nextInt(15);
                int rep = 4;
                popupEvent("Night event", "Charity fundraiser draws in donors.", rep, cash, 0, "REP");
                eco.addCash(cash, "Charity fundraiser");
                eco.applyRep(rep, "Charity fundraiser");
                s.weekPositiveEvents++;
                return true;
            }
            case ARTSY -> {
                if (!positivePool) return false;
                double cash = 10 + s.random.nextInt(10);
                int rep = 3;
                popupEvent("Night event", "Local artist residency boosts buzz.", rep, cash, 0, "REP");
                eco.addCash(cash, "Artist residency");
                eco.applyRep(rep, "Artist residency");
                s.weekPositiveEvents++;
                return true;
            }
            case FAMILY_FRIENDLY -> {
                if (!positivePool) return false;
                double cash = 12 + s.random.nextInt(12);
                int rep = 5;
                popupEvent("Night event", "Community family night packed the tables.", rep, cash, 0, "REP");
                eco.addCash(cash, "Family night");
                eco.applyRep(rep, "Family night");
                s.weekPositiveEvents++;
                return true;
            }
            case ROWDY -> {
                if (positivePool) return false;
                log.popup("Night event", "Rival pub brawl spills outside.", "Fight risk");
                triggerFight("Rival pub brawl", bouncerNegReduction());
                eco.applyRep(-2, "Rival pub brawl");
                s.weekNegativeEvents++;
                return true;
            }
            case SHADY -> {
                double cash = 25 + s.random.nextInt(25);
                int rep = positivePool ? -1 : -3;
                popupEvent("Night event", "Backroom deal keeps a table occupied.", rep, cash, 0, "CASH");
                eco.addCash(cash, "Backroom deal");
                eco.applyRep(rep, "Backroom deal");
                if (rep < 0) s.weekNegativeEvents++; else s.weekPositiveEvents++;
                return true;
            }
            case UNDERGROUND -> {
                if (positivePool) return false;
                double cash = 18 + s.random.nextInt(18);
                int rep = -2;
                popupEvent("Night event", "Secret after-hours session leaks online.", rep, cash, 0, "CASH");
                eco.addCash(cash, "After-hours session");
                eco.applyRep(rep, "After-hours heat");
                s.weekNegativeEvents++;
                return true;
            }
            default -> {
                return false;
            }
        }
    }


    private double rumorEventBias() {
        int fights = s.rumorHeat.getOrDefault(Rumor.FIGHTS_EVERY_WEEKEND, 0);
        int poisoning = s.rumorHeat.getOrDefault(Rumor.FOOD_POISONING_SCARE, 0);
        int roast = s.rumorHeat.getOrDefault(Rumor.BEST_SUNDAY_ROAST, 0);

        double bias = 0.0;
        bias -= fights * 0.002;
        bias -= poisoning * 0.002;
        bias += roast * 0.0015;

        return Math.max(-0.30, Math.min(0.25, bias));
    }

    private double eventBiasFromRumors() {
        if (s.activeRumors == null || s.activeRumors.isEmpty()) return 0.0;
        double bias = 0.0;
        for (RumorInstance rumor : s.activeRumors.values()) {
            bias += rumor.eventBias();
        }
        return Math.max(-0.30, Math.min(0.25, bias));
    }

    // FIXED: no recursion; does the fight math directly
    public void triggerFight(String reason, double baseReduction) {
        double fightRed = bouncerFightReduction();
        double totalRed = Math.min(0.75, baseReduction + fightRed);

        int repHit = Math.max(3, (int) Math.round(10 * (1.0 - totalRed)));
        repHit = Math.abs(s.mitigateSecurityRepHit(-repHit));
        double dmg = Math.max(4, 12 * (1.0 - totalRed));

        popupEvent("Fight", "Fight breaks out: " + reason + ".", -repHit, -dmg, 0, "FIGHT");
        eco.applyRep(-repHit, "Fight fallout (" + reason + ")");
        logSecurityMitigation(-repHit, "Fight fallout (" + reason + ")");
        s.addSecurityLog("Fight: " + reason + " | rep -" + repHit + " | dmg " + fmt0(dmg));
        eco.tryPay(dmg, TransactionType.REPAIR, "Damages (fight)", CostTag.EVENT);

        // weekly morale mechanics
        s.fightsThisWeek++;
        s.nightFights++;

        s.nightEvents++;
        s.reportEvents++;
    }

    public void triggerHighRepScandal() {
        if (s.reputation <= 85) return;
        int repBefore = s.reputation;
        double pct = 0.50 + (s.random.nextDouble() * 0.30);
        int reduction = (int)Math.round(repBefore * pct);
        int percentHit = (int)Math.round(pct * 100);

        log.popup(" Scandal erupts", "Police investigation + influencer backlash.", "Rep -" + percentHit + "%");
        eco.applyRep(-reduction, "Scandal fallout (-" + percentHit + "%)");
        log.neg("  - Reputation hit -" + percentHit + "% (rep -" + reduction + ").");
    }

    private void triggerTeenTrouble(double reduction) {
        log.popup("Night event", "Teenagers sneak in and cause chaos.", "Rep hit | Fight risk");
        int repHit = Math.max(2, (int) Math.round(5 * (1.0 - reduction)));
        applyIncidentRepHit(-repHit, "Underage trouble");
        if (s.random.nextInt(100) < 35) {
            triggerFight("Teen trouble", reduction);
        } else {
            s.nightRefusedUnderage += 1 + s.random.nextInt(3);
        }
    }

    private double bouncerNegReduction() {
        return bouncerIntervenes() ? s.bouncerNegReduction : 0.0;
    }

    private double bouncerFightReduction() {
        return bouncerIntervenes() ? s.bouncerFightReduction : 0.0;
    }

    private boolean bouncerIntervenes() {
        return s.bouncersHiredTonight > 0 && s.random.nextDouble() < s.bouncerMitigationChance();
    }
}
