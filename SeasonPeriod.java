import java.time.LocalDate;
import java.util.List;

public final class SeasonPeriod {
    private final String name;
    private final int startMonth;
    private final int startWeekOfMonth;
    private final int endMonth;
    private final int endWeekOfMonth;
    private final List<SeasonTag> tags;

    public SeasonPeriod(String name,
                        int startMonth,
                        int startWeekOfMonth,
                        int endMonth,
                        int endWeekOfMonth,
                        List<SeasonTag> tags) {
        this.name = name;
        this.startMonth = startMonth;
        this.startWeekOfMonth = startWeekOfMonth;
        this.endMonth = endMonth;
        this.endWeekOfMonth = endWeekOfMonth;
        this.tags = List.copyOf(tags);
    }

    public String getName() {
        return name;
    }

    public List<SeasonTag> getTags() {
        return tags;
    }

    public boolean isActive(LocalDate date) {
        int key = monthWeekKey(date.getMonthValue(), weekOfMonth(date));
        int startKey = monthWeekKey(startMonth, startWeekOfMonth);
        int endKey = monthWeekKey(endMonth, endWeekOfMonth);

        if (startKey <= endKey) {
            return key >= startKey && key <= endKey;
        }
        return key >= startKey || key <= endKey;
    }

    private static int weekOfMonth(LocalDate date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    private static int monthWeekKey(int month, int weekOfMonth) {
        return month * 10 + weekOfMonth;
    }
}
