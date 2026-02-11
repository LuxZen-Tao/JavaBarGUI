import javax.swing.JTextPane;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SeasonalEffectsTests {
    private static final LocalDate START_DATE = LocalDate.of(1989, 1, 16);

    public static void main(String[] args) {
        testFeatureFlagOffBaseline();
        testTouristWaveModifiers();
        testWinterSlumpModifiers();
        FeatureFlags.FEATURE_SEASONS = false;
        System.out.println("All SeasonalEffectsTests passed.");
        System.exit(0);
    }

    private static void testFeatureFlagOffBaseline() {
        FeatureFlags.FEATURE_SEASONS = false;
        GameState state = GameFactory.newGame();
        setDate(state, LocalDate.of(1989, 6, 10));

        UILogger logger = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(state, logger);
        SupplierSystem supplier = new SupplierSystem(state);
        EventSystem events = new EventSystem(state, eco, logger);
        PunterSystem punters = new PunterSystem(state, eco, new InventorySystem(state), events,
                new RumorSystem(state, logger), logger);

        assertNear(supplier.seasonalSupplierPriceMultiplier(), 1.0, "Supplier seasonal multiplier should be baseline when OFF");
        assertNear(events.seasonalRoundEventChanceMultiplier(), 1.0, "Round event multiplier should be baseline when OFF");
        assertNear(events.seasonalBetweenNightChanceMultiplier(), 1.0, "Between-night event multiplier should be baseline when OFF");
        assertNear(punters.seasonalTierWeightMultiplier(Punter.Tier.BIG_SPENDER), 1.0,
                "Punter tier multiplier should be baseline when OFF");
    }

    private static void testTouristWaveModifiers() {
        FeatureFlags.FEATURE_SEASONS = true;
        GameState state = GameFactory.newGame();
        setDate(state, LocalDate.of(1989, 6, 10)); // Tourist + Exam overlap

        UILogger logger = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(state, logger);
        SupplierSystem supplier = new SupplierSystem(state);
        EventSystem events = new EventSystem(state, eco, logger);
        PunterSystem punters = new PunterSystem(state, eco, new InventorySystem(state), events,
                new RumorSystem(state, logger), logger);

        if (supplier.seasonalSupplierPriceMultiplier() <= 1.0) {
            throw new IllegalStateException("Supplier seasonal multiplier should increase during Tourist/Exam window.");
        }
        if (events.seasonalBetweenNightChanceMultiplier() <= 1.0) {
            throw new IllegalStateException("Between-night event chance should increase slightly during Tourist/Exam window.");
        }
        if (punters.seasonalTierWeightMultiplier(Punter.Tier.DECENT) <= 1.0) {
            throw new IllegalStateException("Punter DECENT mix weight should increase during Tourist/Exam window.");
        }
    }

    private static void testWinterSlumpModifiers() {
        FeatureFlags.FEATURE_SEASONS = true;
        GameState state = GameFactory.newGame();
        setDate(state, LocalDate.of(1989, 12, 20));

        UILogger logger = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(state, logger);
        SupplierSystem supplier = new SupplierSystem(state);
        EventSystem events = new EventSystem(state, eco, logger);
        PunterSystem punters = new PunterSystem(state, eco, new InventorySystem(state), events,
                new RumorSystem(state, logger), logger);

        if (supplier.seasonalSupplierPriceMultiplier() >= 1.0) {
            throw new IllegalStateException("Supplier seasonal multiplier should soften during Winter Slump.");
        }
        if (events.seasonalBetweenNightChanceMultiplier() >= 1.0) {
            throw new IllegalStateException("Between-night event chance should reduce slightly during Winter Slump.");
        }
        if (punters.seasonalTierWeightMultiplier(Punter.Tier.BIG_SPENDER) >= 1.0) {
            throw new IllegalStateException("Punter BIG_SPENDER weight should dip during Winter Slump.");
        }
    }

    private static void setDate(GameState state, LocalDate date) {
        state.dayCounter = (int) ChronoUnit.DAYS.between(START_DATE, date);
    }

    private static void assertNear(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.0001) {
            throw new IllegalStateException(message + " (actual=" + actual + ", expected=" + expected + ")");
        }
    }
}
