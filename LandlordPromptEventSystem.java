import java.util.Random;

public class LandlordPromptEventSystem {
    private static final double BASE_CHANCE_PER_NIGHT = 0.08; // 8%
    private static final double CHAOS_HIGH_THRESHOLD = 60.0;
    private static final double CHAOS_HIGH_MODIFIER = 0.05; // +5%
    private static final double REPUTATION_HIGH_THRESHOLD = 60;
    private static final double REPUTATION_HIGH_MODIFIER = 0.03; // +3%
    private static final int INTRO_WEEK_THRESHOLD = 7; // Cannot occur during first week (7 days)
    private static final int COOLDOWN_NIGHTS = 2; // Cannot occur within last 2 nights
    private static final int MAX_EVENTS_PER_WEEK = 2;

    private final GameState s;
    private final Random random;

    public LandlordPromptEventSystem(GameState s) {
        this.s = s;
        this.random = s.random;
    }

    /**
     * Check if a landlord prompt event should spawn tonight.
     * Returns null if no event, or a random event definition if one should spawn.
     */
    public LandlordPromptEventDef maybeSpawnEvent() {
        // Check constraints
        if (!canSpawnEvent()) {
            return null;
        }

        // Calculate spawn chance
        double chance = BASE_CHANCE_PER_NIGHT;

        // Chaos modifier
        if (s.chaos >= CHAOS_HIGH_THRESHOLD) {
            chance += CHAOS_HIGH_MODIFIER;
        }

        // Reputation modifier
        if (s.reputation >= REPUTATION_HIGH_THRESHOLD) {
            chance += REPUTATION_HIGH_MODIFIER;
        }

        // Roll for spawn
        if (random.nextDouble() < chance) {
            // Select random event
            int eventIndex = random.nextInt(6);
            LandlordPromptEventId[] allIds = LandlordPromptEventId.values();
            return LandlordPromptEventCatalog.getById(allIds[eventIndex]);
        }

        return null;
    }

    /**
     * Check if an event can spawn based on constraints.
     */
    private boolean canSpawnEvent() {
        // Cannot occur during intro week
        if (s.dayCounter < INTRO_WEEK_THRESHOLD) {
            return false;
        }

        // Cannot occur if event occurred within last 2 nights
        if (s.nightCount - s.lastLandlordPromptEventNight <= COOLDOWN_NIGHTS) {
            return false;
        }

        // Cannot occur if already 2 events this week
        if (s.landlordPromptEventsThisWeek >= MAX_EVENTS_PER_WEEK) {
            return false;
        }

        return true;
    }

    /**
     * Apply the effect package to the game state.
     */
    public void applyEffects(LandlordPromptEffectPackage effects, EconomySystem eco, UILogger log) {
        boolean hasEffects = false;
        StringBuilder effectSummary = new StringBuilder();

        // Apply cash
        if (effects.getCashDelta() != 0) {
            s.cash += effects.getCashDelta();
            effectSummary.append("Cash ");
            if (effects.getCashDelta() > 0) {
                effectSummary.append("+£").append(effects.getCashDelta());
            } else {
                effectSummary.append("£").append(effects.getCashDelta());
            }
            hasEffects = true;
        }

        // Apply reputation
        if (effects.getReputationDelta() != 0) {
            if (hasEffects) effectSummary.append(", ");
            eco.applyRep(effects.getReputationDelta(), "Landlord event");
            effectSummary.append("Rep ");
            if (effects.getReputationDelta() > 0) {
                effectSummary.append("+");
            }
            effectSummary.append(effects.getReputationDelta());
            hasEffects = true;
        }

        // Apply chaos
        if (effects.getChaosDelta() != 0.0) {
            if (hasEffects) effectSummary.append(", ");
            s.chaos = Math.max(0.0, Math.min(100.0, s.chaos + effects.getChaosDelta()));
            effectSummary.append("Chaos ");
            if (effects.getChaosDelta() > 0) {
                effectSummary.append("+");
            }
            effectSummary.append(String.format("%.1f", effects.getChaosDelta()));
            hasEffects = true;
        }

        // Apply morale
        if (effects.getMoraleDelta() != 0) {
            if (hasEffects) effectSummary.append(", ");
            s.teamMorale = Math.max(0.0, Math.min(100.0, s.teamMorale + effects.getMoraleDelta()));
            effectSummary.append("Morale ");
            if (effects.getMoraleDelta() > 0) {
                effectSummary.append("+");
            }
            effectSummary.append(effects.getMoraleDelta());
            hasEffects = true;
        }

        // Apply service efficiency (temporary for next shift)
        if (effects.getServiceEfficiencyDelta() != 0) {
            if (hasEffects) effectSummary.append(", ");
            s.landlordPromptEventEfficiencyModifier = effects.getServiceEfficiencyDelta();
            effectSummary.append("Service ");
            if (effects.getServiceEfficiencyDelta() > 0) {
                effectSummary.append("+");
            }
            effectSummary.append(effects.getServiceEfficiencyDelta());
            hasEffects = true;
        }

        // Apply supplier trust
        if (effects.getSupplierTrustDelta() != 0.0) {
            if (hasEffects) effectSummary.append(", ");
            s.supplierTrustPenalty = Math.max(0.0, s.supplierTrustPenalty + effects.getSupplierTrustDelta());
            effectSummary.append("Supplier Trust ");
            if (effects.getSupplierTrustDelta() > 0) {
                effectSummary.append("+");
            }
            effectSummary.append(String.format("%.1f", effects.getSupplierTrustDelta()));
            hasEffects = true;
        }

        if (hasEffects) {
            log.info(" Effects: " + effectSummary.toString());
        }
    }

    /**
     * Record that an event occurred tonight.
     */
    public void recordEventOccurred() {
        s.lastLandlordPromptEventNight = s.nightCount;
        s.landlordPromptEventsThisWeek++;
    }

    /**
     * Reset weekly counters (call at week start).
     */
    public void resetWeeklyCounters() {
        s.landlordPromptEventsThisWeek = 0;
    }

    /**
     * Determine the result type based on RNG.
     * Distribution: GOOD 33%, NEUTRAL 33%, BAD 33%
     */
    public LandlordPromptResultType rollResultType() {
        int roll = random.nextInt(100);
        if (roll < 33) {
            return LandlordPromptResultType.GOOD;
        } else if (roll < 66) {
            return LandlordPromptResultType.NEUTRAL;
        } else {
            return LandlordPromptResultType.BAD;
        }
    }
}
