import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class SeasonCalendar {
    private static final LocalDate START_DATE = LocalDate.of(1989, 1, 16);

    private static final List<SeasonPeriod> SEASON_PERIODS = List.of(
            new SeasonPeriod("Tourist Wave", 6, 1, 8, 4, List.of(SeasonTag.TOURIST_WAVE)),
            new SeasonPeriod("Exam Season", 5, 2, 6, 2, List.of(SeasonTag.EXAM_SEASON)),
            new SeasonPeriod("Winter Slump", 11, 3, 1, 2, List.of(SeasonTag.WINTER_SLUMP)),
            new SeasonPeriod("Derby Week", 3, 3, 3, 3, List.of(SeasonTag.DERBY_WEEK))
    );

    private final GameState state;

    public SeasonCalendar(GameState state) {
        this.state = state;
    }

    public List<SeasonTag> getActiveSeasonTags() {
        return getActiveSeasonTags(START_DATE.plusDays(state.dayCounter));
    }

    public List<SeasonTag> getActiveSeasonTags(LocalDate date) {
        if (!FeatureFlags.FEATURE_SEASONS) return Collections.emptyList();

        LinkedHashSet<SeasonTag> activeTags = new LinkedHashSet<>();
        for (SeasonPeriod period : SEASON_PERIODS) {
            if (period.isActive(date)) {
                activeTags.addAll(period.getTags());
            }
        }
        return List.copyOf(activeTags);
    }
}
