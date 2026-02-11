import java.time.LocalDate;
import java.util.List;

public class SeasonCalendarTests {
    public static void main(String[] args) {
        testFeatureGateOffReturnsEmpty();
        testActiveTagsForKnownDates();
        System.out.println("All SeasonCalendarTests passed.");
        System.exit(0);
    }

    private static void testFeatureGateOffReturnsEmpty() {
        FeatureFlags.FEATURE_SEASONS = false;
        GameState state = GameFactory.newGame();
        SeasonCalendar calendar = new SeasonCalendar(state);
        List<SeasonTag> tags = calendar.getActiveSeasonTags(LocalDate.of(1989, 6, 10));
        if (!tags.isEmpty()) {
            throw new IllegalStateException("Season tags must be empty when FEATURE_SEASONS is OFF.");
        }
    }

    private static void testActiveTagsForKnownDates() {
        boolean original = FeatureFlags.FEATURE_SEASONS;
        try {
            FeatureFlags.FEATURE_SEASONS = true;
            GameState state = GameFactory.newGame();
            SeasonCalendar calendar = new SeasonCalendar(state);

            List<SeasonTag> juneTags = calendar.getActiveSeasonTags(LocalDate.of(1989, 6, 10));
            assertContains(juneTags, SeasonTag.TOURIST_WAVE, "Expected TOURIST_WAVE in June week 2");
            assertContains(juneTags, SeasonTag.EXAM_SEASON, "Expected EXAM_SEASON in June week 2");

            List<SeasonTag> derbyTags = calendar.getActiveSeasonTags(LocalDate.of(1989, 3, 15));
            assertContains(derbyTags, SeasonTag.DERBY_WEEK, "Expected DERBY_WEEK in March week 3");

            List<SeasonTag> winterTags = calendar.getActiveSeasonTags(LocalDate.of(1989, 12, 20));
            assertContains(winterTags, SeasonTag.WINTER_SLUMP, "Expected WINTER_SLUMP in December week 3+");
        } finally {
            FeatureFlags.FEATURE_SEASONS = original;
        }
    }

    private static void assertContains(List<SeasonTag> tags, SeasonTag expected, String message) {
        if (!tags.contains(expected)) {
            throw new IllegalStateException(message + ". Actual tags: " + tags);
        }
    }
}
