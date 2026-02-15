// StaffNarrativeEvent.java
import java.io.Serializable;

/**
 * Represents narrative events related to staff relationships and interactions.
 * These are non-punitive, flavor events that add character to the staff system.
 */
public class StaffNarrativeEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        WORKS_WELL,           // Staff working well together
        CONFLICT,             // Minor disagreement or tension
        DATING_START,         // Two staff members start dating
        DATING_BREAKUP,       // Dating relationship ends
        MARRIAGE,             // Staff members get married
        FRIENDSHIP,           // Strong friendship forms
        COLLABORATION         // Staff collaborate effectively
    }

    private final int weekNumber;
    private final EventType type;
    private final String staffName1;
    private final String staffName2;
    private final String description;

    public StaffNarrativeEvent(int weekNumber, EventType type, String staffName1, String staffName2, String description) {
        this.weekNumber = weekNumber;
        this.type = type;
        this.staffName1 = staffName1;
        this.staffName2 = staffName2;
        this.description = description;
    }

    public int getWeekNumber() { return weekNumber; }
    public EventType getType() { return type; }
    public String getStaffName1() { return staffName1; }
    public String getStaffName2() { return staffName2; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "Week " + weekNumber + ": " + description;
    }
}
