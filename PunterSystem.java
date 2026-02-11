import java.util.*;

public class PunterSystem {

    private final GameState s;
    private final EconomySystem eco;
    private final InventorySystem inv;
    private final EventSystem events;
    private final RumorSystem rumors;
    private final UILogger log;

    public PunterSystem(GameState s, EconomySystem eco, InventorySystem inv, EventSystem events, RumorSystem rumors, UILogger log) {
        this.s = s;
        this.eco = eco;
        this.inv = inv;
        this.events = events;
        this.rumors = rumors;
        this.log = log;
    }

    public void seedNightPunters(int poolSize) {
        s.nightPunters.clear();
        for (int i = 0; i < poolSize; i++) {
            s.nightPunters.add(createPunterForReputation());
        }
    }

    /** Shuffled list of punters currently in the bar (not left, not banned). */
    public List<Punter> inBarShuffled() {
        List<Punter> bar = new ArrayList<>();
        for (Punter p : s.nightPunters) {
            if (!p.hasLeftBar() && !p.isBanned()) bar.add(p);
        }
        Collections.shuffle(bar, s.random);
        return bar;
    }

    /** Add arrivals mid-night (respect maxBarOccupancy). Returns how many added. */
    public int addArrivals(int requested) {
        int canAdd = Math.max(0, s.maxBarOccupancy - s.nightPunters.size());
        int add = Math.min(requested, canAdd);
        for (int i = 0; i < add; i++) {
            s.nightPunters.add(createPunterForReputation());
        }
        return add;
    }

    /** Some punters leave naturally each round to keep turnover flowing. */
    public int applyNaturalDepartures() {
        int left = 0;
        int count = s.nightPunters.size();
        double occupancyFactor = Math.min(0.04, count * 0.002);
        double lateNightFactor = Math.min(0.03, Math.max(0, s.roundInNight - 3) * 0.002);
        double baseChance = 0.015 + occupancyFactor + lateNightFactor;
        baseChance = Math.min(0.09, baseChance);
        for (Punter p : s.nightPunters) {
            if (p.hasLeftBar() || p.isBanned()) continue;
            if (s.random.nextDouble() < baseChance) {
                p.leaveBar();
                left++;
            }
        }
        return left;
    }

    /** Remove anyone who left or was kicked out. Returns how many removed. */
    public int cleanupDeparted() {
        int before = s.nightPunters.size();
        s.nightPunters.removeIf(p -> p.hasLeftBar() || p.isBanned());
        return before - s.nightPunters.size();
    }

    /** Apply consequences for punters who were present but NOT served this round. */
    public void handleUnserved(List<Punter> unserved, double effectiveMult) {
        if (unserved == null || unserved.isEmpty()) return;

        log.neg(" " + unserved.size() + " punter(s) not served this round.");

        // Small rep hit per unserved (tycoon rule: service matters)
        int repHit = Math.min(6, Math.max(1, unserved.size() / 2));
        eco.applyRep(-repHit, "Service failure (" + unserved.size() + " unserved)");

        for (Punter p : unserved) {
            p.incrementNoBuy();

            // Bouncer makes neglect less likely to turn into violence; they may just leave instead.
            boolean fightTriggered = p.escalateIfStaying();
            if (fightTriggered && bouncerIntervenes()) {
                // Bouncer diffuses it into "storming out" instead of a fight
                    p.leaveBar();
                    log.info("  - " + p.getName() + " storms out (bouncer prevents a fight).");
                    continue;
            }

            if (fightTriggered) {
                if (mitigateFightWithStaff()) {
                    log.info("  - Staff defuse " + p.getName() + " before it escalates.");
                    p.leaveBar();
                    continue;
                }
                log.popup("Fight", "<b>" + p.getName() + "</b> snaps after repeated neglect.", "Rep hit | Damages");
                events.triggerFight("Unserved MENACE", bouncerIntervenes() ? s.bouncerNegReduction : 0.0);
            } else {
                log.info("  - " + p.getName() + " escalates  " + p.getState() + " (no-buy " + p.getNoBuyStreak() + ")");
            }

            if (p.isBanned()) {
                kickOut(p, 0, "3 rounds no service / can't buy");
            }
        }
    }

    public void handlePunter(Punter p, double effectiveMult, int sec, boolean riskyWeekend, double tipRate) {
        if (p.isBanned() || p.hasLeftBar()) return;

        log.info("Punter: " + p);

        if (!p.canDrink()) {
            log.neg("  - Underage. Refused.");
            s.nightRefusedUnderage++;
            p.incrementNoBuy();
            if (p.isBanned()) kickOut(p, -4, "Underage trouble");
            return;
        }

        if (p.getWallet() < 3.0) {
            log.neg("  - Wallet < 3. Leaves early.");
            eco.applyRep(-1, "Broke punter leaves");
            p.leaveBar();
            return;
        }

        Wine cheapest = inv.cheapestWine(effectiveMult);
        if (cheapest == null) {
            log.neg("  - No stock. Can't buy. No-buy +1");
            cannotBuyStayAndEscalate(p, "No stock");
            return;
        }

        double cheapestPrice = inv.sellPrice(cheapest, effectiveMult);
        if (p.getWallet() < cheapestPrice) {
            log.neg("  - Can't afford cheapest (" + String.format("%.2f", cheapestPrice) + "). No-buy +1");
            handlePriceComplaintIfOverpriced(p, cheapest, effectiveMult);
            cannotBuyStayAndEscalate(p, "Unaffordable prices");
            maybeTheft(p, effectiveMult, sec, riskyWeekend);
            return;
        }

        int drinks = desiredDrinkCount(p);
        for (int d = 0; d < drinks; d++) {
            if (!attemptPurchase(p, effectiveMult, tipRate, sec, riskyWeekend)) {
                return;
            }
        }

        maybeOrderFood(p);
    }

    private void cannotBuyStayAndEscalate(Punter p, String reason) {
        if (reason.equals("No stock") || reason.equals("Unaffordable prices")) {
            eco.applyRep(-3, reason);
        }

        p.incrementNoBuy();

        boolean fightTriggered = p.escalateIfStaying();
        if (fightTriggered) {
            if (mitigateFightWithStaff()) {
                log.info("  - Staff de-escalate the situation.");
                p.leaveBar();
                return;
            }
            log.popup("Fight", "MENACE punter snaps after repeated no-buy.", "Rep hit | Damages");
            events.triggerFight("MENACE punter", bouncerIntervenes() ? s.bouncerNegReduction : 0.0);
        } else {
            log.info("  - Mood escalates  " + p.getState());
        }

        if (p.isBanned()) {
            kickOut(p, 0, "3 rounds can't buy");
        }
    }

    private void kickOut(Punter p, int repDelta, String reason) {
        s.nightKickedOut++;
        log.neg("  - KICKED OUT (" + reason + ").");
        if (repDelta != 0) eco.applyRep(repDelta, "Kickout");
        p.markKickedOut();
    }

    private void maybeTheft(Punter p, double effectiveMult, int sec, boolean riskyWeekend) {
        int theftChance = 10 + repToTheftBonus();
        if (effectiveMult > 1.30) theftChance += 6;
        if (riskyWeekend) theftChance += 6;

        if (bouncerIntervenes()) {
            theftChance = (int)Math.round(theftChance * (1.0 - s.bouncerTheftReduction));
        }
        theftChance = (int)Math.round(theftChance * chaosReductionMultiplier());
        double activityRisk = (s.activityTonight != null) ? s.activityTonight.getRiskBonusPct() : 0.0;
        double upgradeReduction = s.upgradeRiskReductionPct;
        double riskMult = Math.max(0.30, 1.0 + activityRisk - upgradeReduction);
        theftChance = (int)Math.round(theftChance * riskMult);
        if (s.securityPolicy != null) {
            theftChance = (int) Math.round(theftChance * s.securityPolicy.getIncidentChanceMultiplier());
        }
        theftChance = (int) Math.round(theftChance * s.securityTaskIncidentChanceMultiplier());
        theftChance = (int) Math.round(theftChance * s.upgradeIncidentChanceMultiplier);
        theftChance = Math.max(0, theftChance);

        if (s.random.nextInt(100) < theftChance) attemptTheft(p, sec);
    }

    private void attemptTheft(Punter p, int sec) {
        Wine stolen = s.rack.pickRandomBottle(s.random);
        if (stolen == null) return;

        log.neg("  - " + p.getName() + " attempts theft: " + stolen.getName() + " ");

        int chaos = repToTheftBonus();
        int caughtChance = 45 + sec * 10 - Math.max(0, chaos);

        if (bouncerIntervenes()) {
            caughtChance = (int)Math.round(caughtChance * (1.0 + s.bouncerTheftReduction));
        }
        caughtChance = Math.max(15, Math.min(95, caughtChance));

        if (s.random.nextInt(100) < caughtChance) {
            log.pos("  - Caught!");
            eco.applyRep(+2, "Theft caught");
            s.addSecurityLog("Theft caught: " + stolen.getName() + " | rep +2");
            p.incrementNoBuy();
        } else {
            log.neg("  - Success! Bottle stolen.");
            s.rack.removeBottle(stolen);
            int repHit = s.mitigateSecurityRepHit(-7);
            eco.applyRep(repHit, "Theft succeeded");
            s.addSecurityLog("Theft: bottle lost | rep " + repHit);
            p.incrementNoBuy();
        }

        if (p.isBanned()) kickOut(p, 0, "3 no-buy");
    }

    private int repToTheftBonus() {
        if (s.reputation >= 60) return -10;
        if (s.reputation >= 20) return -4;
        if (s.reputation >= -20) return 0;
        if (s.reputation >= -60) return +8;
        return +14;
    }

    private void handlePriceComplaintIfOverpriced(Punter p, Wine cheapest, double effectiveMult) {
        if (cheapest == null || p == null) return;
        if (effectiveMult <= 1.0) return;

        double basePrice = cheapest.getBasePrice();
        if (p.getWallet() < basePrice) return;

        double overPct = Math.max(0.0, effectiveMult - 1.0);
        if (overPct <= 0.02) return;

        double baseChance = 0.20 + (overPct * 0.45);
        double repMult = complaintRepBandMultiplier();
        double chance = Math.min(0.85, baseChance * repMult);
        chance *= chaosReductionMultiplier();
        if (cheapest != null) {
            chance *= cheapest.getPriceSensitivity();
        }

        if (s.random.nextInt(10000) >= (int)Math.round(chance * 10000)) return;

        int repLoss = 1 + (int)Math.round(overPct * 4.0);
        if (s.reputation < 0) repLoss += 1;
        if (s.reputation > 85) repLoss = Math.max(1, repLoss - 1);
        if (cheapest != null) {
            repLoss = Math.max(1, (int)Math.round(repLoss * cheapest.getPriceSensitivity()));
        }

        eco.applyRep(-repLoss, "Price complaints");
        p.leaveBar();
        applyTrafficLossFromComplaint(p);

        log.neg("  - Price complaint: \"" + cheapest.getName() + "\" felt overpriced ("
                + (int)Math.round(overPct * 100) + "% over). Rep -" + repLoss + " and they leave.");
    }

    private void applyTrafficLossFromComplaint(Punter complainant) {
        if (s.nightPunters.size() <= 1) return;
        int tries = 0;
        while (tries < 5) {
            Punter other = s.nightPunters.get(s.random.nextInt(s.nightPunters.size()));
            if (other != null && other != complainant && !other.hasLeftBar() && !other.isBanned()) {
                other.leaveBar();
                log.info("  - The mood dips; another punter heads out.");
                return;
            }
            tries++;
        }
    }

    private double chaosReductionMultiplier() {
        double cap = s.staffChaosCapacity();
        double reduction = Math.min(0.25, cap / 200.0);
        return Math.max(0.60, 1.0 - reduction);
    }

    private boolean mitigateFightWithStaff() {
        double cap = s.staffChaosCapacity();
        double chance = Math.min(0.35, cap / 200.0);
        return s.random.nextDouble() < chance;
    }

    private double complaintRepBandMultiplier() {
        if (s.reputation > 85) return 0.7;
        if (s.reputation >= 0) return 1.0;
        return 1.25;
    }

    private Punter createPunterForReputation() {
        Punter.Tier tier = rollTierForReputation();
        tier = adjustTierForIdentityAndRumors(tier);
        Punter p = Punter.randomPunter(s.nextPunterId++, s.random, tier);
        assignDescriptors(p);
        applyReputationBiasToPunter(p);
        applyIdentityRumorBiasToPunter(p);
        return p;
    }

    public void refreshChaosContributions() {
        for (Punter p : s.nightPunters) {
            updateChaosContribution(p);
        }
    }

    private void updateChaosContribution(Punter p) {
        if (p == null) return;
        int chaos = 0;
        for (Punter.Descriptor descriptor : p.getDescriptors()) {
            chaos += descriptor.getChaosDelta();
        }
        chaos += switch (p.getState()) {
            case CHILL -> -1;
            case ROWDY -> 1;
            case MENACE -> 2;
        };
        chaos += p.getTrouble();
        if (p.getNoBuyStreak() >= 2) chaos += 1;
        if (p.getNoBuyStreak() >= 3) chaos += 1;
        if (s.reputation >= 60) chaos -= 1;
        if (s.reputation <= -40) chaos += 1;
        chaos = Math.max(-2, Math.min(3, chaos));
        p.setChaosContribution(chaos);
    }

    private void assignDescriptors(Punter p) {
        if (p == null) return;
        int count = 1 + s.random.nextInt(3);
        java.util.Set<Punter.Descriptor> picked = new java.util.LinkedHashSet<>();
        int attempts = 0;
        while (picked.size() < count && attempts < 12) {
            Punter.DescriptorCategory category = pickDescriptorCategory();
            Punter.Descriptor descriptor = pickDescriptorFromCategory(category, picked);
            if (descriptor != null) {
                picked.add(descriptor);
            }
            attempts++;
        }
        p.setDescriptors(new java.util.ArrayList<>(picked));
    }

    private Punter.DescriptorCategory pickDescriptorCategory() {
        int roll = s.random.nextInt(100);
        if (roll < 50) return Punter.DescriptorCategory.PERSONALITY;
        if (roll < 80) return Punter.DescriptorCategory.SOCIAL;
        return Punter.DescriptorCategory.PHYSICAL;
    }

    private Punter.Descriptor pickDescriptorFromCategory(Punter.DescriptorCategory category, java.util.Set<Punter.Descriptor> taken) {
        int total = 0;
        for (Punter.Descriptor d : Punter.Descriptor.values()) {
            if (d.getCategory() != category || taken.contains(d)) continue;
            total += d.getWeight();
        }
        if (total <= 0) return null;
        int roll = s.random.nextInt(total);
        int cursor = 0;
        for (Punter.Descriptor d : Punter.Descriptor.values()) {
            if (d.getCategory() != category || taken.contains(d)) continue;
            cursor += d.getWeight();
            if (roll < cursor) return d;
        }
        return null;
    }

    private void applyReputationBiasToPunter(Punter p) {
        if (p == null) return;
        if (s.reputation > 50) {
            if (p.getState() == Punter.State.ROWDY && s.random.nextInt(100) < 40) {
                p.setState(Punter.State.CHILL);
            } else if (p.getState() == Punter.State.MENACE && s.random.nextInt(100) < 35) {
                p.setState(Punter.State.ROWDY);
            }
        } else if (s.reputation < 0) {
            if (p.getState() == Punter.State.CHILL && s.random.nextInt(100) < 25) {
                p.setState(Punter.State.ROWDY);
            } else if (p.getState() == Punter.State.ROWDY && s.random.nextInt(100) < 20) {
                p.setState(Punter.State.MENACE);
            }
        }
    }

    private void applyIdentityRumorBiasToPunter(Punter p) {
        if (p == null) return;

        double moodBias = 0.0;

        // Identity bias
        if (s.currentIdentity != null) {
            moodBias += s.currentIdentity.getMoodBias();
        } else {
            // fallback to enum identity if you still use it
            if (s.pubIdentity == PubIdentity.RESPECTABLE || s.pubIdentity == PubIdentity.FAMILY_FRIENDLY) moodBias += 0.15;
            if (s.pubIdentity == PubIdentity.ROWDY || s.pubIdentity == PubIdentity.SHADY) moodBias -= 0.18;
            if (s.pubIdentity == PubIdentity.UNDERGROUND) moodBias -= 0.10;
        }

        // Rumor bias (old heat map)
        moodBias -= rumorHeat(Rumor.FIGHTS_EVERY_WEEKEND) * 0.002;
        moodBias -= rumorHeat(Rumor.WATERED_DOWN_DRINKS) * 0.001;
        moodBias += rumorHeat(Rumor.BEST_SUNDAY_ROAST) * 0.001;

        // Rumor system bias (if implemented)
        if (rumors != null) {
            moodBias += rumors.moodBias();
        }

        // Apply bias to state gently
        if (moodBias > 0.15 && p.getState() != Punter.State.CHILL && s.random.nextInt(100) < 35) {
            p.setState(Punter.State.CHILL);
            return;
        }

        if (moodBias < -0.15) {
            if (p.getState() == Punter.State.CHILL && s.random.nextInt(100) < 30) {
                p.setState(Punter.State.ROWDY);
            } else if (p.getState() == Punter.State.ROWDY && s.random.nextInt(100) < 20) {
                p.setState(Punter.State.MENACE);
            }
        }
    }


    private int desiredDrinkCount(Punter p) {
        if (p == null) return 1;
        if (p.getTier() == Punter.Tier.BIG_SPENDER) {
            int roll = s.random.nextInt(100);
            if (roll < 20) return 3;
            if (roll < 55) return 2;
        } else if (p.getTier() == Punter.Tier.DECENT) {
            if (s.random.nextInt(100) < 25) return 2;
        }
        return 1;
    }

    private boolean attemptPurchase(Punter p, double effectiveMult, double tipRate, int sec, boolean riskyWeekend) {
        Wine chosen = inv.randomWineForTier(p.getTier());
        if (chosen == null) return false;

        double lastPrice = inv.sellPrice(chosen, effectiveMult);

        for (int attempts = 0; attempts < 12; attempts++) {
            double sellPrice = inv.sellPrice(chosen, effectiveMult);
            if (sellPrice <= p.getWallet()) {
                s.rack.removeBottle(chosen);

                eco.addCash(sellPrice, "Sale " + chosen.getName());
                s.reportRevenue += sellPrice;
                s.reportSales++;
                s.nightRevenue += sellPrice;
                s.nightSales++;
                s.nightItemSales.merge("Wine: " + chosen.getName(), 1, Integer::sum);
                s.recordRoundSale("Wine", chosen.getName());

                boolean cheated = s.happyHour && sellPrice > chosen.getBasePrice();
                double tipMult = priceTipMultiplier(sellPrice, chosen.getBasePrice(), p.getTier());

                if (cheated) {
                    tipMult *= 0.6;
                    if (!s.happyHourCheatRepHitThisRound) {
                        eco.applyRep(-1, "Happy Hour bait");
                        s.happyHourCheatRepHitThisRound = true;
                    }
                    if (!s.happyHourBacklashShown) {
                        log.popup(" Happy Hour backlash", "Punters expected a bargain. They got... maths.", "Rep -1 | Tips reduced");
                        s.happyHourBacklashShown = true;
                    }
                    if (s.random.nextInt(100) < 25) {
                        log.neg("  - " + p.getName() + " feels cheated and leaves.");
                        p.leaveBar();
                    }
                }

                applyOverpricingConsequences(p, chosen, sellPrice, chosen.getBasePrice());

                double tips = sellPrice * tipRate * tipMult;
                if (tips > 0) {
                    eco.addCash(tips, "Tips");
                    s.tipsThisWeek += tips;
                }

                p.spend(sellPrice);

                log.pos("  - Buys " + chosen.getName() + " for " + String.format("%.2f", sellPrice)
                        + (tips > 0 ? " (tips +" + String.format("%.2f", tips) + ")" : ""));

                if (effectiveMult <= 1.10) eco.applyRep(+1, "Satisfied customer");
                return true;
            }

            Wine cheaper = inv.randomCheaperThan(lastPrice, effectiveMult);
            if (cheaper == null) {
                log.neg("  - Can't afford remaining options. No-buy +1");
                cannotBuyStayAndEscalate(p, "Can't afford");
                maybeTheft(p, effectiveMult, sec, riskyWeekend);
                return false;
            }

            chosen = cheaper;
            lastPrice = inv.sellPrice(chosen, effectiveMult);
        }

        log.neg("  - Gives up. No-buy +1");
        cannotBuyStayAndEscalate(p, "Gives up");
        return false;
    }

    private void maybeOrderFood(Punter p) {
        if (p == null || !s.kitchenUnlocked) return;
        if (p.hasOrderedFoodThisVisit()) return;
        if (p.getFoodCooldownRounds() > 0) return;

        double chance = switch (p.getTier()) {
            case BIG_SPENDER -> 0.45;
            case DECENT -> 0.30;
            case REGULAR -> 0.18;
            case LOWLIFE -> 0.10;
        };
        if (s.pubIdentity == PubIdentity.FAMILY_FRIENDLY) chance += 0.06;
        if (s.pubIdentity == PubIdentity.SHADY || s.pubIdentity == PubIdentity.ROWDY) chance -= 0.04;
        chance -= rumorHeat(Rumor.FOOD_POISONING_SCARE) * 0.002;
        chance += rumorHeat(Rumor.BEST_SUNDAY_ROAST) * 0.002;
        if (s.currentIdentity != null) {
            if (s.currentIdentity == PubIdentity.FAMILY_FRIENDLY) chance += 0.06;
            if (s.currentIdentity == PubIdentity.SHADY || s.currentIdentity == PubIdentity.ROWDY) chance -= 0.04;
        }
        if (rumors != null) {
            chance += Math.max(-0.08, Math.min(0.08, rumors.wealthBias() * 0.2));
        }

        if (s.random.nextDouble() > chance) return;

        if (s.foodRack.count() <= 0) {
            if (s.kitchenQualityBonus >= 2 && s.random.nextInt(100) < 35) {
                p.setFoodCooldownRounds(1);
                return;
            }
            p.incrementFoodAttempts();
            p.setFoodCooldownRounds(2);
            s.foodDisappointmentThisRound++;
            if (p.getState() == Punter.State.CHILL) {
                p.setState(Punter.State.ROWDY);
            } else if (p.getState() == Punter.State.ROWDY && p.getFoodAttempts() >= 2) {
                p.setState(Punter.State.MENACE);
            }

            if (p.getFoodAttempts() >= 2) {
                s.nightFoodUnserved++;
                if (s.random.nextInt(100) < 35) {
                    eco.applyRep(-1, "Food out of stock");
                }
            }

            if (s.foodDisappointmentThisRound >= 3 && !s.foodDisappointmentPopupShown) {
                log.popup("Kitchen sold out", "Multiple punters wanted food but the kitchen is empty.", "Rep risk | Mood down");
                s.foodDisappointmentPopupShown = true;
            }
            return;
        }

        Food food = s.foodRack.pickRandomFood(s.random);
        if (food == null) return;

        double price = s.foodRack.getSellPrice(food, s.kitchenQualityBonus);
        if (p.getWallet() < price) return;

        s.foodRack.removeFood(food);
        int prepRounds = Math.max(1, s.foodPrepRounds);
        s.pendingFoodOrders.add(new FoodOrder(p.getId(), p.getName(), food, price, s.roundInNight + prepRounds));
        p.setOrderedFoodThisVisit(true);
        eco.addCash(price, "Meal order: " + food.getName());
        s.reportRevenue += price;
        s.nightRevenue += price;
        s.reportSales++;
        s.nightSales++;
        s.nightItemSales.merge("Food: " + food.getName(), 1, Integer::sum);
        p.spend(price);
        s.recordFoodQuality(food);
        applyFoodOverpricingConsequences(p, food, price);
        log.info("  - Orders food: " + food.getName() + " (ready in " + prepRounds + " rounds).");
    }

    private double priceTipMultiplier(double sellPrice, double basePrice, Punter.Tier tier) {
        if (basePrice <= 0) return 1.0;
        double ratio = sellPrice / basePrice;
        double mult = 1.0;
        if (ratio <= 1.2) {
            mult = 1.0;
        } else if (ratio <= 1.6) {
            mult = 0.85;
        } else if (ratio <= 2.2) {
            mult = 0.65;
        } else {
            mult = 0.40;
        }

        if (tier == Punter.Tier.BIG_SPENDER) mult *= 0.95;
        if (tier == Punter.Tier.LOWLIFE) mult *= 0.85;
        return Math.max(0.2, mult);
    }

    private void applyOverpricingConsequences(Punter p, Wine wine, double sellPrice, double basePrice) {
        if (p == null || basePrice <= 0) return;
        double ratio = sellPrice / basePrice;
        if (ratio <= 1.2) return;

        double repChance;
        int repLoss;
        if (ratio <= 1.6) {
            repChance = 0.10;
            repLoss = 1;
        } else if (ratio <= 2.2) {
            repChance = 0.20;
            repLoss = 1 + (s.reputation < 0 ? 1 : 0);
        } else {
            repChance = 0.35;
            repLoss = 2 + (s.reputation < 0 ? 1 : 0);
        }

        if (p.getTier() == Punter.Tier.BIG_SPENDER) repChance += 0.05;
        if (p.getTier() == Punter.Tier.LOWLIFE) repChance += 0.10;
        if (wine != null) {
            repChance *= wine.getPriceSensitivity();
            repLoss = Math.max(1, (int)Math.round(repLoss * wine.getPriceSensitivity()));
        }

        if (s.random.nextDouble() < repChance) {
            eco.applyRep(-repLoss, "Overpricing backlash");
            if (ratio > 2.2 && !s.overpricingRobberyPopupShown) {
                log.popup(" Overpricing backlash", "Punters feel robbed by the prices.", "Rep -" + repLoss);
                s.overpricingRobberyPopupShown = true;
            }
            if (s.random.nextInt(100) < 35) {
                p.leaveBar();
                log.neg("  - " + p.getName() + " storms out over prices.");
            }
        }
    }

    private void applyFoodOverpricingConsequences(Punter p, Food food, double sellPrice) {
        if (p == null || food == null) return;
        double base = food.getBasePrice();
        if (base <= 0) return;
        double ratio = sellPrice / base;
        if (ratio <= 1.15) return;

        double repChance = 0.10;
        int repLoss = 1;
        if (ratio > 1.5) {
            repChance = 0.22;
            repLoss = 2;
        }

        repChance *= food.getPriceSensitivity();
        repLoss = Math.max(1, (int)Math.round(repLoss * food.getPriceSensitivity()));

        if (s.random.nextDouble() < repChance) {
            eco.applyRep(-repLoss, "Food overpricing backlash");
            if (s.random.nextInt(100) < 25) {
                p.leaveBar();
                log.neg("  - " + p.getName() + " grumbles about food pricing.");
            }
        }
    }

    private Punter.Tier rollTierForReputation() {
        double bigWeight;
        double decentWeight;
        double regularWeight;
        double lowlifeWeight;

        if (s.reputation >= 70) {
            bigWeight = 20;
            decentWeight = 45;
            regularWeight = 30;
            lowlifeWeight = 5;
        } else if (s.reputation >= 40) {
            bigWeight = 12;
            decentWeight = 38;
            regularWeight = 35;
            lowlifeWeight = 15;
        } else if (s.reputation >= 0) {
            bigWeight = 6;
            decentWeight = 29;
            regularWeight = 40;
            lowlifeWeight = 25;
        } else {
            bigWeight = 3;
            decentWeight = 17;
            regularWeight = 35;
            lowlifeWeight = 45;
        }

        int pubLevelShift = Math.max(0, s.pubLevel) * 2;
        bigWeight += pubLevelShift;
        lowlifeWeight = Math.max(1.0, lowlifeWeight - pubLevelShift);

        bigWeight *= seasonalTierWeightMultiplier(Punter.Tier.BIG_SPENDER);
        decentWeight *= seasonalTierWeightMultiplier(Punter.Tier.DECENT);
        regularWeight *= seasonalTierWeightMultiplier(Punter.Tier.REGULAR);
        lowlifeWeight *= seasonalTierWeightMultiplier(Punter.Tier.LOWLIFE);

        double total = bigWeight + decentWeight + regularWeight + lowlifeWeight;
        double roll = s.random.nextDouble() * total;
        if (roll < bigWeight) return Punter.Tier.BIG_SPENDER;
        roll -= bigWeight;
        if (roll < decentWeight) return Punter.Tier.DECENT;
        roll -= decentWeight;
        if (roll < regularWeight) return Punter.Tier.REGULAR;
        return Punter.Tier.LOWLIFE;
    }

    double seasonalTierWeightMultiplier(Punter.Tier tier) {
        if (!FeatureFlags.FEATURE_SEASONS) return 1.0;

        List<SeasonTag> tags = new SeasonCalendar(s).getActiveSeasonTags();
        if (tags.isEmpty()) return 1.0;

        double mult = 1.0;
        for (SeasonTag tag : tags) {
            switch (tag) {
                case TOURIST_WAVE -> {
                    if (tier == Punter.Tier.BIG_SPENDER) mult *= 1.08;
                    else if (tier == Punter.Tier.DECENT) mult *= 1.05;
                    else if (tier == Punter.Tier.LOWLIFE) mult *= 0.94;
                }
                case EXAM_SEASON -> {
                    if (tier == Punter.Tier.DECENT) mult *= 1.08;
                    else if (tier == Punter.Tier.REGULAR) mult *= 1.04;
                    else if (tier == Punter.Tier.BIG_SPENDER) mult *= 0.96;
                }
                case WINTER_SLUMP -> {
                    if (tier == Punter.Tier.BIG_SPENDER) mult *= 0.92;
                    else if (tier == Punter.Tier.DECENT) mult *= 0.96;
                    else if (tier == Punter.Tier.LOWLIFE) mult *= 1.08;
                }
                case DERBY_WEEK -> {
                    if (tier == Punter.Tier.REGULAR) mult *= 1.05;
                    else if (tier == Punter.Tier.LOWLIFE) mult *= 1.12;
                    else if (tier == Punter.Tier.BIG_SPENDER) mult *= 0.95;
                }
            }
        }
        return mult;
    }

    private Punter.Tier adjustTierForIdentityAndRumors(Punter.Tier base) {
        double bias = 0.0;
        if (s.pubIdentity == PubIdentity.RESPECTABLE || s.pubIdentity == PubIdentity.ARTSY) bias += 0.18;
        if (s.pubIdentity == PubIdentity.FAMILY_FRIENDLY) bias += 0.12;
        if (s.pubIdentity == PubIdentity.SHADY || s.pubIdentity == PubIdentity.ROWDY) bias -= 0.16;
        if (s.pubIdentity == PubIdentity.UNDERGROUND) bias -= 0.08;

        bias -= rumorHeat(Rumor.WATERED_DOWN_DRINKS) * 0.002;
        bias += rumorHeat(Rumor.BEST_SUNDAY_ROAST) * 0.002;
        if (s.currentIdentity != null) bias += s.currentIdentity.getWealthBias();
        if (rumors != null) bias += rumors.wealthBias();
        if (FeatureFlags.FEATURE_RIVALS) bias += s.rivalPunterMixBias;
        bias += s.pubLevel * 0.06;

        if (bias > 0.15 && s.random.nextInt(100) < 35) {
            return promoteTier(base);
        }
        if (bias < -0.15 && s.random.nextInt(100) < 30) {
            return demoteTier(base);
        }
        return base;
    }

    private int rumorHeat(Rumor rumor) {
        return s.rumorHeat.getOrDefault(rumor, 0);
    }

    private boolean bouncerIntervenes() {
        return s.bouncersHiredTonight > 0 && s.random.nextDouble() < s.bouncerMitigationChance();
    }

    private Punter.Tier promoteTier(Punter.Tier tier) {
        return switch (tier) {
            case LOWLIFE -> Punter.Tier.REGULAR;
            case REGULAR -> Punter.Tier.DECENT;
            case DECENT -> Punter.Tier.BIG_SPENDER;
            case BIG_SPENDER -> Punter.Tier.BIG_SPENDER;
        };
    }

    private Punter.Tier demoteTier(Punter.Tier tier) {
        return switch (tier) {
            case BIG_SPENDER -> Punter.Tier.DECENT;
            case DECENT -> Punter.Tier.REGULAR;
            case REGULAR -> Punter.Tier.LOWLIFE;
            case LOWLIFE -> Punter.Tier.LOWLIFE;
        };
    }
}
