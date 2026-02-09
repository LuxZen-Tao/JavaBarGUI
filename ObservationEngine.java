import java.util.ArrayList;
import java.util.List;

public class ObservationEngine {
    public static final int MAX_OBSERVATION_LENGTH = 70;
    private static final int NORMAL_THROTTLE_ROUNDS = 2;
    private static final int FORCE_REFRESH_ROUNDS = 4;

    private static final List<String> OBS_NAMES = List.of(
            "Jamie", "Alex", "Casey", "Morgan", "Taylor", "Riley", "Sam", "Jordan"
    );

    private static final List<String> VIBE_LINES = List.of(
            "Local buzz is steady and relaxed.",
            "Feels like a proper neighborhood spot tonight.",
            "Easygoing crowd with good energy.",
            "Low-key night with a friendly vibe.",
            "Room feels balanced and comfortable."
    );

    private static final List<String> BUSY_LINES = List.of(
            "Packed house and quick pours kept it moving.",
            "Busy room, but the bar kept pace.",
            "Crowd was thick; service stayed sharp.",
            "Lively rush with a steady bar flow.",
            "Big crowd energy without the chaos."
    );

    private static final List<String> WAIT_LINES = List.of(
            "Service fell behind during a rush.",
            "Queues built up at the bar for a bit.",
            "A busy stretch led to a few long waits.",
            "Rush got messy; some folks waited it out.",
            "Bar pace slipped during the peak crowd."
    );

    private static final List<String> FIGHT_LINES = List.of(
            "A scuffle popped off and the mood dipped.",
            "Trouble sparked near the bar and folks noticed.",
            "Tempers flared; the room tightened up.",
            "A rough moment shook the vibe."
    );

    private static final List<String> BOUNCER_LINES = List.of(
            "Door staff shut down a flare-up fast.",
            "Bouncers kept a scuffle from spreading.",
            "Door presence calmed things down quickly.",
            "Security handled a hiccup without drama."
    );

    private static final List<String> PRICE_HIGH_LINES = List.of(
            "Prices feel a touch steep tonight.",
            "A few folks clocked the higher prices.",
            "Tickets read a bit premium right now.",
            "Cost feels higher than last round."
    );

    private static final List<String> PRICE_LOW_LINES = List.of(
            "Prices felt like a deal tonight.",
            "Good value pours had people smiling.",
            "Tickets came in light and it showed.",
            "Price tag was easy to swallow."
    );

    private static final List<String> PRICE_SHIFT_LINES = List.of(
            "Prices shifted and people noticed.",
            "Menu tags changed enough to spark chatter.",
            "A price move had the room comparing notes."
    );

    private static final List<String> STOCK_WINE_LINES = List.of(
            "Wine list looked a bit thin.",
            "A few bottles seemed to be running out.",
            "Shelves were light on selection."
    );

    private static final List<String> STOCK_FOOD_LINES = List.of(
            "Kitchen specials ran a little thin.",
            "Food options looked picked over.",
            "The kitchen felt light on stock."
    );

    private static final List<String> STAFF_CHANGE_LINES = List.of(
            "New faces behind the bar tonight.",
            "Fresh staff energy showed at the counter.",
            "Roster shake-up was noticeable."
    );

    private static final List<String> REP_HIGH_LINES = List.of(
            "Buzz is strong — locals are talking.",
            "Reputation glow is pulling people in.",
            "Word of mouth feels positive lately."
    );

    private static final List<String> REP_LOW_LINES = List.of(
            "Reputation feels bruised out there.",
            "Some folks seemed hesitant to settle in.",
            "Chatter hinted at a rough stretch."
    );

    public record ObservationContext(
            int roundIndex,
            int barCount,
            int unserved,
            int fightsThisRound,
            int eventsThisRound,
            int refundsThisRound,
            int trafficIn,
            int trafficOut,
            double priceMultiplier,
            double priceChangeAbs,
            int rackCount,
            int foodCount,
            boolean kitchenUnlocked,
            int bouncersHired,
            boolean staffIncidentThisRound,
            boolean staffChangeRecent
    ) {}

    public record ObservationResult(String text, boolean majorEvent) {}

    public ObservationResult nextObservation(GameState s, ObservationContext ctx) {
        boolean majorEvent = isMajorEvent(ctx);
        int roundsSince = ctx.roundIndex - s.lastObservationRound;
        if (!majorEvent && roundsSince < NORMAL_THROTTLE_ROUNDS) {
            return null;
        }
        if (!majorEvent && roundsSince < FORCE_REFRESH_ROUNDS && s.random.nextInt(100) >= 65) {
            return null;
        }
        String text = buildObservation(s, ctx, majorEvent);
        if (text == null || text.isBlank()) {
            return null;
        }
        return new ObservationResult(trimObservation(text, MAX_OBSERVATION_LENGTH), majorEvent);
    }

    private boolean isMajorEvent(ObservationContext ctx) {
        return ctx.fightsThisRound > 0
                || ctx.eventsThisRound > 0
                || ctx.refundsThisRound > 0
                || ctx.staffIncidentThisRound
                || ctx.unserved >= 4;
    }

    private String buildObservation(GameState s, ObservationContext ctx, boolean majorEvent) {
        List<Category> candidates = new ArrayList<>();
        addIf(candidates, Category.FIGHT, ctx.fightsThisRound > 0);
        addIf(candidates, Category.BUSY, ctx.unserved > 0 || ctx.barCount >= Math.max(8, s.maxBarOccupancy * 0.7));
        addIf(candidates, Category.PRICE_HIGH, ctx.priceMultiplier >= 1.30);
        addIf(candidates, Category.PRICE_LOW, ctx.priceMultiplier <= 0.85);
        addIf(candidates, Category.PRICE_SHIFT, ctx.priceChangeAbs >= 0.12);
        addIf(candidates, Category.STOCK_WINE, ctx.rackCount <= 4);
        addIf(candidates, Category.STOCK_FOOD, ctx.kitchenUnlocked && ctx.foodCount <= 4);
        addIf(candidates, Category.STAFF_CHANGE, ctx.staffChangeRecent);
        addIf(candidates, Category.REP_HIGH, s.reputation >= 60);
        addIf(candidates, Category.REP_LOW, s.reputation <= -20);
        addIf(candidates, Category.VIBE, candidates.isEmpty());

        if (candidates.isEmpty()) {
            candidates.add(Category.VIBE);
        } else if (!majorEvent && s.random.nextInt(100) < 20) {
            candidates.add(Category.VIBE);
        }

        Category pick = candidates.get(s.random.nextInt(candidates.size()));
        String line = switch (pick) {
            case FIGHT -> ctx.bouncersHired > 0 ? pick(BOUNCER_LINES, s) : pick(FIGHT_LINES, s);
            case BUSY -> ctx.unserved > 0 ? pick(WAIT_LINES, s) : pick(BUSY_LINES, s);
            case PRICE_HIGH -> pick(PRICE_HIGH_LINES, s);
            case PRICE_LOW -> pick(PRICE_LOW_LINES, s);
            case PRICE_SHIFT -> pick(PRICE_SHIFT_LINES, s);
            case STOCK_WINE -> pick(STOCK_WINE_LINES, s);
            case STOCK_FOOD -> pick(STOCK_FOOD_LINES, s);
            case STAFF_CHANGE -> pick(STAFF_CHANGE_LINES, s);
            case REP_HIGH -> pick(REP_HIGH_LINES, s);
            case REP_LOW -> pick(REP_LOW_LINES, s);
            case VIBE -> pick(VIBE_LINES, s);
        };
        return formatWithName(pickObservationName(s), line);
    }

    private void addIf(List<Category> list, Category category, boolean condition) {
        if (condition) list.add(category);
    }

    private String pick(List<String> options, GameState s) {
        return options.get(s.random.nextInt(options.size()));
    }

    private String pickObservationName(GameState s) {
        if (!s.nightPunters.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                Punter candidate = s.nightPunters.get(s.random.nextInt(s.nightPunters.size()));
                if (!candidate.isBanned() && !candidate.hasLeftBar()) {
                    String name = candidate.getName();
                    if (name != null && !name.isBlank()) return name;
                }
            }
        }
        return OBS_NAMES.get(s.random.nextInt(OBS_NAMES.size()));
    }

    private String formatWithName(String name, String line) {
        if (name == null || name.isBlank()) return line;
        return name + ": " + line;
    }

    private String trimObservation(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }

    private enum Category {
        FIGHT,
        BUSY,
        PRICE_HIGH,
        PRICE_LOW,
        PRICE_SHIFT,
        STOCK_WINE,
        STOCK_FOOD,
        STAFF_CHANGE,
        REP_HIGH,
        REP_LOW,
        VIBE
    }
}
