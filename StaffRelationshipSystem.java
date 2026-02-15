// StaffRelationshipSystem.java
import java.util.*;

/**
 * Manages staff relationships and generates narrative events based on staff interactions.
 * This system is separate from misconduct and has no financial or punitive effects.
 */
public class StaffRelationshipSystem {
    private final GameState s;
    private final Random random;
    
    // Event generation thresholds
    private static final double WORKS_WELL_THRESHOLD = 5.0;
    private static final double DISLIKES_THRESHOLD = -5.0;
    private static final double DATING_CHANCE_BASE = 3;  // 3% base chance per week for compatible staff
    private static final double BREAKUP_CHANCE_BASE = 8;  // 8% base chance if affinity drops
    private static final double MARRIAGE_CHANCE = 5;      // 5% chance per week if dating for 8+ weeks

    public StaffRelationshipSystem(GameState s) {
        this.s = s;
        this.random = s.random;
    }

    /**
     * Updates all staff relationships and generates narrative events.
     * Called once per week during end-of-week processing.
     */
    public List<StaffNarrativeEvent> weeklyRelationshipUpdate(UILogger log) {
        List<StaffNarrativeEvent> events = new ArrayList<>();
        
        // Get all staff
        List<Staff> allStaff = getAllStaff();
        if (allStaff.size() < 2) {
            return events; // Need at least 2 staff for relationships
        }

        // Ensure relationships exist for all staff pairs
        ensureRelationships(allStaff);

        // Update each relationship
        for (StaffRelationship rel : s.staffRelationships) {
            rel.incrementWeeksSinceStateChange();
            
            Staff staff1 = findStaffById(rel.getStaffId1(), allStaff);
            Staff staff2 = findStaffById(rel.getStaffId2(), allStaff);
            
            if (staff1 == null || staff2 == null) {
                continue; // Staff no longer employed
            }

            // Small random affinity drift based on working together
            double drift = (random.nextDouble() - 0.5) * 0.3;
            rel.adjustAffinity(drift);

            // Generate events based on current state and affinity
            StaffNarrativeEvent event = generateEventForRelationship(rel, staff1, staff2);
            if (event != null) {
                events.add(event);
                logEvent(log, event);
            }
        }

        // Clean up relationships for staff who have left
        cleanupRelationships(allStaff);

        return events;
    }

    private void ensureRelationships(List<Staff> allStaff) {
        for (int i = 0; i < allStaff.size(); i++) {
            for (int j = i + 1; j < allStaff.size(); j++) {
                Staff s1 = allStaff.get(i);
                Staff s2 = allStaff.get(j);
                if (findRelationship(s1.getId(), s2.getId()) == null) {
                    s.staffRelationships.add(new StaffRelationship(s1.getId(), s2.getId()));
                }
            }
        }
    }

    private void cleanupRelationships(List<Staff> currentStaff) {
        Set<Integer> currentIds = new HashSet<>();
        for (Staff staff : currentStaff) {
            currentIds.add(staff.getId());
        }
        
        s.staffRelationships.removeIf(rel -> 
            !currentIds.contains(rel.getStaffId1()) || !currentIds.contains(rel.getStaffId2())
        );
    }

    private StaffNarrativeEvent generateEventForRelationship(StaffRelationship rel, Staff staff1, Staff staff2) {
        // Check for state transitions based on affinity and current state
        switch (rel.getState()) {
            case NEUTRAL:
                return handleNeutralState(rel, staff1, staff2);
            case WORKS_WELL:
                return handleWorksWellState(rel, staff1, staff2);
            case DISLIKES:
                return handleDislikesState(rel, staff1, staff2);
            case DATING:
                return handleDatingState(rel, staff1, staff2);
            case BREAKUP:
                return handleBreakupState(rel, staff1, staff2);
            case MARRIED:
                return handleMarriedState(rel, staff1, staff2);
        }
        return null;
    }

    private StaffNarrativeEvent handleNeutralState(StaffRelationship rel, Staff staff1, Staff staff2) {
        if (rel.getAffinity() >= WORKS_WELL_THRESHOLD) {
            rel.setState(StaffRelationship.RelationshipState.WORKS_WELL);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.WORKS_WELL,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " are working together really well."
            );
        }
        
        if (rel.getAffinity() <= DISLIKES_THRESHOLD) {
            rel.setState(StaffRelationship.RelationshipState.DISLIKES);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.CONFLICT,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " don't seem to get along."
            );
        }

        // Small chance of starting to date if neutral and positive affinity
        if (rel.getAffinity() > 2.0 && random.nextInt(100) < DATING_CHANCE_BASE) {
            rel.setState(StaffRelationship.RelationshipState.DATING);
            rel.adjustAffinity(2.0); // Boost affinity
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.DATING_START,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " have started dating!"
            );
        }

        return null;
    }

    private StaffNarrativeEvent handleWorksWellState(StaffRelationship rel, Staff staff1, Staff staff2) {
        // Can drift back to neutral or forward to dating
        if (rel.getAffinity() < 3.0) {
            rel.setState(StaffRelationship.RelationshipState.NEUTRAL);
            return null;
        }

        // Chance to start dating if working well together
        if (rel.getWeeksSinceStateChange() >= 3 && random.nextInt(100) < DATING_CHANCE_BASE * 2) {
            rel.setState(StaffRelationship.RelationshipState.DATING);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.DATING_START,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " have started dating!"
            );
        }

        return null;
    }

    private StaffNarrativeEvent handleDislikesState(StaffRelationship rel, Staff staff1, Staff staff2) {
        // Can improve back to neutral
        if (rel.getAffinity() > -3.0) {
            rel.setState(StaffRelationship.RelationshipState.NEUTRAL);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.COLLABORATION,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " have resolved their differences."
            );
        }
        return null;
    }

    private StaffNarrativeEvent handleDatingState(StaffRelationship rel, Staff staff1, Staff staff2) {
        // Check for marriage if dating long enough
        if (rel.getWeeksSinceStateChange() >= 8 && random.nextInt(100) < MARRIAGE_CHANCE) {
            rel.setState(StaffRelationship.RelationshipState.MARRIED);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.MARRIAGE,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " got married!"
            );
        }

        // Check for breakup if affinity drops
        if (rel.getAffinity() < 0.0 && random.nextInt(100) < BREAKUP_CHANCE_BASE) {
            rel.setState(StaffRelationship.RelationshipState.BREAKUP);
            rel.adjustAffinity(-2.0);
            return new StaffNarrativeEvent(
                s.weekCount,
                StaffNarrativeEvent.EventType.DATING_BREAKUP,
                staff1.getName(),
                staff2.getName(),
                staff1.getName() + " and " + staff2.getName() + " broke up."
            );
        }

        return null;
    }

    private StaffNarrativeEvent handleBreakupState(StaffRelationship rel, Staff staff1, Staff staff2) {
        // After a breakup, gradually return to neutral
        if (rel.getWeeksSinceStateChange() >= 4) {
            rel.setState(StaffRelationship.RelationshipState.NEUTRAL);
            return null;
        }
        return null;
    }

    private StaffNarrativeEvent handleMarriedState(StaffRelationship rel, Staff staff1, Staff staff2) {
        // Married state is stable, just maintain good affinity
        if (rel.getAffinity() < 3.0) {
            rel.adjustAffinity(0.5); // Slowly restore affinity
        }
        return null;
    }

    private List<Staff> getAllStaff() {
        List<Staff> all = new ArrayList<>();
        all.addAll(s.fohStaff);
        all.addAll(s.bohStaff);
        all.addAll(s.generalManagers);
        return all;
    }

    private Staff findStaffById(int id, List<Staff> staffList) {
        for (Staff staff : staffList) {
            if (staff.getId() == id) {
                return staff;
            }
        }
        return null;
    }

    private StaffRelationship findRelationship(int id1, int id2) {
        int minId = Math.min(id1, id2);
        int maxId = Math.max(id1, id2);
        
        for (StaffRelationship rel : s.staffRelationships) {
            if (rel.getStaffId1() == minId && rel.getStaffId2() == maxId) {
                return rel;
            }
        }
        return null;
    }

    private void logEvent(UILogger log, StaffNarrativeEvent event) {
        if (log != null) {
            log.event(" [Staff] " + event.getDescription());
        }
    }

    /**
     * Generates an observation for display in the Observation HUD.
     * Returns null if no observation should be shown this round.
     */
    public String generateStaffObservation() {
        List<Staff> allStaff = getAllStaff();
        if (allStaff.size() < 2 || s.staffRelationships.isEmpty()) {
            return null;
        }

        // Randomly select an interesting relationship to observe
        List<StaffRelationship> interestingRelationships = new ArrayList<>();
        for (StaffRelationship rel : s.staffRelationships) {
            if (rel.getState() != StaffRelationship.RelationshipState.NEUTRAL) {
                interestingRelationships.add(rel);
            }
        }

        if (interestingRelationships.isEmpty()) {
            return null; // Nothing interesting to observe
        }

        // 15% chance to show a staff observation
        if (random.nextInt(100) >= 15) {
            return null;
        }

        StaffRelationship rel = interestingRelationships.get(random.nextInt(interestingRelationships.size()));
        Staff staff1 = findStaffById(rel.getStaffId1(), allStaff);
        Staff staff2 = findStaffById(rel.getStaffId2(), allStaff);

        if (staff1 == null || staff2 == null) {
            return null;
        }

        return generateObservationText(rel, staff1, staff2);
    }

    private String generateObservationText(StaffRelationship rel, Staff staff1, Staff staff2) {
        String name1 = staff1.getName();
        String name2 = staff2.getName();

        return switch (rel.getState()) {
            case NEUTRAL -> null;
            case WORKS_WELL -> name1 + " and " + name2 + " working like a well-oiled machine.";
            case DISLIKES -> "Tension between " + name1 + " and " + name2 + " tonight.";
            case DATING -> name1 + " and " + name2 + " seem extra cheerful tonight.";
            case BREAKUP -> name1 + " and " + name2 + " avoiding each other.";
            case MARRIED -> name1 + " and " + name2 + " keeping it professional.";
        };
    }
}
