import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class PubIdentitySystem {

    private final GameState s;
    private final UILogger log;

    public PubIdentitySystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
    }

    public void updateWeeklyIdentity() {
        ensureIdentityData();

        WeeklyIdentitySnapshot snapshot = new WeeklyIdentitySnapshot(
                weeklyProfit(),
                s.weekRefundTotal,
                s.fightsThisWeek,
                s.unservedThisWeek,
                s.weekNegativeEvents,
                s.weekPositiveEvents,
                averagePriceMultiplier(),
                averageFoodQuality(),
                s.weeklyRepDeltaAbs,
                s.weeklyRepDeltaNet,
                s.teamMorale,
                s.baseSecurityLevel,
                new EnumMap<>(s.weekIdentitySignals)
        );

        s.identitySnapshots.addLast(snapshot);
        while (s.identitySnapshots.size() > 4) {
            s.identitySnapshots.removeFirst();
        }

        EnumMap<PubIdentity, Double> newScores = new EnumMap<>(PubIdentity.class);
        for (PubIdentity identity : PubIdentity.values()) {
            double score = scoreIdentity(identity);
            double prev = s.pubIdentityScore.getOrDefault(identity, 0.0);
            double blended = (prev * 0.70) + (score * 0.30);
            newScores.put(identity, blended);
        }

        s.pubIdentityScore.clear();
        s.pubIdentityScore.putAll(newScores);

        PubIdentity previous = s.currentIdentity;
        PubIdentity next = dominantIdentity();
        s.currentIdentity = next;
        s.identityHistory.add(next);

        double prevScore = previous == null ? 0.0 : s.lastIdentityScore;
        double currentScore = s.pubIdentityScore.getOrDefault(next, 0.0);
        s.lastIdentityScore = currentScore;
        s.identityDrift = driftIndicator(currentScore - prevScore);

        s.weeklyIdentityFlavorText = "Locals now describe " + s.pubName + " as '" + next.getDescriptor() + "'.";
        s.identityDriftSummary = buildIdentityDriftSummary(snapshot, next);

        if (previous != null && previous != next) {
            log.event(" Pub identity shift: " + previous + "  " + next);
        }
    }

    private double scoreIdentity(PubIdentity identity) {
        double fights = avg(snap -> snap.fights);
        double refunds = avg(snap -> snap.refunds);
        double unserved = avg(snap -> snap.unserved);
        double negEvents = avg(snap -> snap.negativeEvents);
        double posEvents = avg(snap -> snap.positiveEvents);
        double price = avg(snap -> snap.avgPriceMultiplier);
        double foodQuality = avg(snap -> snap.avgFoodQuality);
        double repVol = avg(snap -> snap.repVolatility);
        double repNet = avg(snap -> snap.repNet);
        double morale = avg(snap -> snap.teamMorale);
        double security = avg(snap -> snap.securityLevel);
        double profit = avg(snap -> snap.profit);
        double activityBias = avgActivityBias(identity);

        double score = 0.0;
        switch (identity) {
            case RESPECTABLE -> {
                score += scaleUp(repNet, -12, 12) * 2.2;
                score += scaleUp(morale, 35, 85) * 1.4;
                score += scaleDown(fights, 0, 6) * 2.2;
                score += scaleDown(negEvents, 0, 5) * 1.6;
                score += scaleUp(posEvents, 0, 5) * 0.8;
                score += scaleUp(foodQuality, 1.5, 3.6) * 1.2;
                score += scaleDown(refunds, 0, 18) * 1.0;
                score += scaleUp(security, 0, 3) * 0.8;
                score += scaleUp(activityBias, -1.0, 2.5) * 0.6;
            }
            case ROWDY -> {
                score += scaleUp(fights, 1, 7) * 2.0;
                score += scaleUp(negEvents, 1, 5) * 1.6;
                score += scaleDown(security, 0, 3) * 1.0;
                score += scaleDown(morale, 30, 75) * 1.0;
                score += scaleDown(price, 1.0, 1.4) * 0.6;
                score += scaleUp(unserved, 2, 12) * 1.2;
                score += scaleUp(activityBias, -1.0, 2.5) * 0.6;
            }
            case ARTSY -> {
                score += scaleUp(activityBias, -0.5, 2.5) * 2.2;
                score += scaleUp(posEvents, 0, 4) * 0.8;
                score += scaleDown(fights, 0, 5) * 1.1;
                score += scaleUp(foodQuality, 1.8, 3.4) * 1.3;
                score += scaleUp(price, 1.0, 1.4) * 0.6;
                score += scaleUp(repNet, -8, 12) * 0.8;
            }
            case SHADY -> {
                score += scaleDown(repNet, -12, 6) * 1.6;
                score += scaleUp(negEvents, 1, 6) * 1.6;
                score += scaleUp(refunds, 2, 20) * 1.0;
                score += scaleUp(repVol, 4, 16) * 1.2;
                score += scaleDown(security, 0, 3) * 0.8;
                score += scaleDown(foodQuality, 1.2, 3.2) * 0.8;
                score += scaleUp(unserved, 2, 12) * 0.8;
            }
            case FAMILY_FRIENDLY -> {
                score += scaleDown(fights, 0, 4) * 2.0;
                score += scaleUp(foodQuality, 2.0, 3.6) * 1.5;
                score += scaleDown(refunds, 0, 15) * 1.3;
                score += scaleUp(security, 0, 3) * 1.0;
                score += scaleUp(morale, 40, 85) * 1.0;
                score += scaleUp(price, 0.95, 1.25) * 0.5;
            }
            case UNDERGROUND -> {
                score += scaleUp(repVol, 4, 16) * 1.5;
                score += scaleDown(security, 0, 3) * 1.0;
                score += scaleDown(morale, 30, 70) * 0.8;
                score += scaleUp(negEvents, 1, 5) * 1.0;
                score += scaleUp(fights, 1, 6) * 1.1;
                score += scaleDown(price, 1.0, 1.5) * 0.6;
            }
        }

        score += scaleUp(profit, -40, 120) * 0.5;
        return score;
    }

    private double avg(ToDouble extractor) {
        if (s.identitySnapshots.isEmpty()) return 0.0;
        double sum = 0.0;
        for (WeeklyIdentitySnapshot snap : s.identitySnapshots) sum += extractor.get(snap);
        return sum / s.identitySnapshots.size();
    }

    private double avgActivityBias(PubIdentity identity) {
        if (s.identitySnapshots.isEmpty()) return 0.0;
        double sum = 0.0;
        for (WeeklyIdentitySnapshot snap : s.identitySnapshots) {
            sum += snap.identitySignals.getOrDefault(identity, 0.0);
        }
        return sum / s.identitySnapshots.size();
    }

    private double scaleUp(double value, double min, double max) {
        if (max <= min) return 0.0;
        double pct = (value - min) / (max - min);
        return clamp(pct, 0.0, 1.0) * 2.0 - 1.0;
    }

    private double scaleDown(double value, double min, double max) {
        return -scaleUp(value, min, max);
    }

    private double weeklyProfit() {
        return s.weekRevenue - s.weekCosts;
    }

    private double averageFoodQuality() {
        if (s.weekFoodOrders <= 0) return 2.0;
        return s.weekFoodQualityPoints / Math.max(1.0, s.weekFoodOrders);
    }

    private double averagePriceMultiplier() {
        if (s.weekPriceMultiplierSamples <= 0) return 1.0;
        return s.weekPriceMultiplierSum / Math.max(1.0, s.weekPriceMultiplierSamples);
    }

    private PubIdentity dominantIdentity() {
        PubIdentity best = PubIdentity.RESPECTABLE;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<PubIdentity, Double> entry : s.pubIdentityScore.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    private String driftIndicator(double delta) {
        if (delta > 0.35) return "";
        if (delta < -0.35) return "";
        return "";
    }

    private String buildIdentityDriftSummary(WeeklyIdentitySnapshot snap, PubIdentity identity) {
        StringBuilder sb = new StringBuilder();
        if (snap.repVolatility > 8) sb.append("Reputation volatile; ");
        if (snap.fights >= 3) sb.append("rising fights; ");
        if (snap.unserved >= 8) sb.append("service strain; ");
        if (snap.refunds >= 12) sb.append("refund pressure; ");
        if (snap.avgFoodQuality >= 3.0) sb.append("food praise; ");
        if (snap.securityLevel >= 2) sb.append("tighter security; ");
        if (sb.length() == 0) sb.append("Reputation stable; ");
        sb.append("identity nudges toward ").append(identity.name()).append(".");
        return sb.toString();
    }

    private void ensureIdentityData() {
        if (s.identitySnapshots == null) s.identitySnapshots = new ArrayDeque<>();
        if (s.pubIdentityScore == null) s.pubIdentityScore = new EnumMap<>(PubIdentity.class);
        if (s.identityHistory == null) s.identityHistory = new ArrayList<>();
        if (s.weekIdentitySignals == null) s.weekIdentitySignals = new EnumMap<>(PubIdentity.class);
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public record WeeklyIdentitySnapshot(
            double profit,
            double refunds,
            int fights,
            int unserved,
            int negativeEvents,
            int positiveEvents,
            double avgPriceMultiplier,
            double avgFoodQuality,
            double repVolatility,
            double repNet,
            double teamMorale,
            int securityLevel,
            EnumMap<PubIdentity, Double> identitySignals
    ) {}

    private interface ToDouble {
        double get(WeeklyIdentitySnapshot snap);
    }
}
