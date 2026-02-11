import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public final class RivalSystem {

    public MarketPressure runWeekly(List<RivalPub> rivals, Random random) {
        if (!FeatureFlags.FEATURE_RIVALS) return MarketPressure.empty();
        if (rivals == null || rivals.isEmpty()) return MarketPressure.empty();

        Random rng = random == null ? new Random(0L) : random;
        EnumMap<RivalStance, Integer> counts = new EnumMap<>(RivalStance.class);
        for (RivalStance stance : RivalStance.values()) counts.put(stance, 0);

        for (RivalPub rival : rivals) {
            RivalStance stance = pickStance(rival, rng);
            counts.put(stance, counts.get(stance) + 1);
        }
        return new MarketPressure(rivals.size(), counts);
    }

    RivalStance pickStance(RivalPub rival, Random random) {
        int price = rival.getPriceAggression();
        int quality = rival.getQualityFocus();
        int chaos = rival.getChaosTolerance();

        int priceWarWeight = 10 + (price * 8) + ((2 - quality) * 2);
        int qualityPushWeight = 10 + (quality * 8) + ((2 - price) * 2);
        int eventSpamWeight = 8 + ((2 - chaos) * 6) + ((price + quality) * 2);
        int layLowWeight = 10 + (chaos * 4) + ((2 - price) * 3);
        int chaosRecoveryWeight = 6 + ((2 - chaos) * 9);

        int roll = random.nextInt(priceWarWeight + qualityPushWeight + eventSpamWeight + layLowWeight + chaosRecoveryWeight);
        if ((roll -= priceWarWeight) < 0) return RivalStance.PRICE_WAR;
        if ((roll -= qualityPushWeight) < 0) return RivalStance.QUALITY_PUSH;
        if ((roll -= eventSpamWeight) < 0) return RivalStance.EVENT_SPAM;
        if ((roll -= layLowWeight) < 0) return RivalStance.LAY_LOW;
        return RivalStance.CHAOS_RECOVERY;
    }
}
