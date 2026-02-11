import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class MarketPressure implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int totalRivals;
    private final EnumMap<RivalStance, Integer> stanceCounts;

    public MarketPressure(int totalRivals, Map<RivalStance, Integer> counts) {
        this.totalRivals = Math.max(0, totalRivals);
        this.stanceCounts = new EnumMap<>(RivalStance.class);
        for (RivalStance stance : RivalStance.values()) {
            this.stanceCounts.put(stance, Math.max(0, counts.getOrDefault(stance, 0)));
        }
    }

    public static MarketPressure empty() {
        return new MarketPressure(0, Collections.emptyMap());
    }

    public int totalRivals() {
        return totalRivals;
    }

    public int countFor(RivalStance stance) {
        return stanceCounts.getOrDefault(stance, 0);
    }

    public Map<RivalStance, Integer> stanceCounts() {
        return Collections.unmodifiableMap(stanceCounts);
    }

    public RivalStance dominantStance() {
        RivalStance best = RivalStance.LAY_LOW;
        int bestCount = -1;
        for (RivalStance stance : RivalStance.values()) {
            int count = countFor(stance);
            if (count > bestCount) {
                best = stance;
                bestCount = count;
            }
        }
        return best;
    }
}
