import java.util.*;

/**
 * Rumors model:
 * - s.rumorHeat: long-lived "heat" per Rumor type (0+)
 * - s.activeRumors: short-lived RumorInstance objects used for UI/reporting
 */
public class RumorSystem {

    private final GameState s;
    private final UILogger log;

    public RumorSystem(GameState s, UILogger log) {
        this.s = s;
        this.log = log;
    }

    /** Called once per week (end of week). */
    public void updateWeeklyRumors() {
        // Heat decay: morale + security calm things down; chaos + refunds keep heat alive.
        double calm = (s.teamMorale >= 70 ? 0.10 : (s.teamMorale <= 40 ? -0.05 : 0.0))
                + (s.baseSecurityLevel >= 2 ? 0.06 : 0.0)
                + (s.weekNegativeEvents >= 3 ? -0.06 : 0.0);

        for (Rumor r : Rumor.values()) {
            int heat = s.rumorHeat.getOrDefault(r, 0);
            if (heat <= 0) {
                s.rumorHeat.put(r, 0);
                continue;
            }
            int dec = 1 + (int)Math.round(2 + (heat * 0.04) - (calm * 5.0));
            dec = Math.max(1, Math.min(8, dec));
            heat = Math.max(0, heat - dec);
            s.rumorHeat.put(r, heat);
        }

        rebuildInstances();
    }

    /** Overall traffic multiplier from rumors. */
    public double trafficMultiplier() {
        double mult = 1.0;
        mult -= s.rumorHeat.getOrDefault(Rumor.WATERED_DOWN_DRINKS, 0) * 0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.FIGHTS_EVERY_WEEKEND, 0) * 0.0025;
        mult += s.rumorHeat.getOrDefault(Rumor.BEST_SUNDAY_ROAST, 0) * 0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.FOOD_POISONING_SCARE, 0) * 0.002;
        mult -= s.rumorHeat.getOrDefault(Rumor.SLOW_SERVICE, 0) * 0.002;
        mult += s.rumorHeat.getOrDefault(Rumor.FRIENDLY_STAFF, 0) * 0.002;
        mult += s.rumorHeat.getOrDefault(Rumor.GREAT_ATMOSPHERE, 0) * 0.002;
        return Math.max(0.80, Math.min(1.20, mult));
    }

    /** Bias for punter tier distribution (wealth). */
    public double wealthBias() {
        double bias = 0.0;
        for (RumorInstance ri : s.activeRumors.values()) bias += ri.wealthBias();
        return Math.max(-0.35, Math.min(0.35, bias));
    }

    /** Bias for punter mood. */
    public double moodBias() {
        double bias = 0.0;
        for (RumorInstance ri : s.activeRumors.values()) {
            double n = ri.intensity() / 100.0;
            switch (ri.type()) {
                case BEST_SUNDAY_ROAST, LIVE_MUSIC_SCENE, FRIENDLY_STAFF, GREAT_ATMOSPHERE -> bias += 0.15 * n;
                default -> bias -= 0.12 * n;
            }
        }
        return Math.max(-0.40, Math.min(0.40, bias));
    }

    private void rebuildInstances() {
        s.activeRumors.clear();
        for (Rumor r : Rumor.values()) {
            int heat = s.rumorHeat.getOrDefault(r, 0);
            if (heat <= 0) continue;

            int intensity = Math.min(100, heat);
            double spread = Math.min(1.0, 0.15 + (intensity / 200.0));
            int days = Math.max(2, Math.min(14, 3 + (intensity / 15)));

            s.activeRumors.put(r, new RumorInstance(r, RumorSource.PUNTER, RumorTruth.EXAGGERATED, intensity, spread, days));
        }

        if (!s.activeRumors.isEmpty()) {
            log.event("Rumors shift... " + s.activeRumors.size() + " active");
        }
    }
}
