import javax.swing.JTextPane;
import java.util.List;

/**
 * Tests for staff narrative events system.
 * Verifies that staff events are separate from misconduct events and properly tracked.
 */
public class StaffNarrativeEventTests {
    public static void main(String[] args) {
        testEventStreamSeparation();
        testBasicRelationshipEventGeneration();
        testRelationshipStateTransitions();
        System.out.println("All StaffNarrativeEventTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that staff narrative events are tracked separately from misconduct events.
     */
    private static void testEventStreamSeparation() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);

        // Hire staff to enable relationship events
        sim.hireStaff(Staff.Type.EXPERIENCED);
        sim.hireStaff(Staff.Type.EXPERIENCED);

        // Store initial counts
        int initialReportEvents = state.reportEvents;
        int initialReportStaffEvents = state.reportStaffEvents;

        // Simulate a week by opening and closing nights
        for (int day = 0; day < 7; day++) {
            sim.openNight();
            sim.closeNight("Test close");
        }

        // Staff events should be tracked separately
        // reportEvents should remain unchanged (no misconduct events)
        // reportStaffEvents may increase if relationship events occurred
        assert state.reportEvents == initialReportEvents : 
            "Misconduct events counter should not change: expected " + initialReportEvents + ", got " + state.reportEvents;
        
        // Verify staff events are being tracked
        assert state.reportStaffEvents >= initialReportStaffEvents : 
            "Staff events counter should not decrease";
        
        System.out.println(" ✓ Event stream separation test passed");
    }

    /**
     * Test basic relationship event generation.
     */
    private static void testBasicRelationshipEventGeneration() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);

        // Hire multiple staff to enable relationships
        sim.hireStaff(Staff.Type.EXPERIENCED);
        sim.hireStaff(Staff.Type.EXPERIENCED);
        sim.hireStaff(Staff.Type.CHARISMA);

        int staffCount = state.fohStaff.size();
        assert staffCount >= 2 : "Need at least 2 staff for relationships";

        // Simulate several weeks to allow relationship development
        for (int week = 0; week < 10; week++) {
            for (int day = 0; day < 7; day++) {
                sim.openNight();
                sim.closeNight("Test close");
            }
        }

        // Check that relationships have been created
        assert !state.staffRelationships.isEmpty() : 
            "Relationships should be created for staff pairs";

        // Expected number of relationships for n staff = n*(n-1)/2
        int expectedRelationships = staffCount * (staffCount - 1) / 2;
        assert state.staffRelationships.size() == expectedRelationships : 
            "Expected " + expectedRelationships + " relationships, got " + state.staffRelationships.size();

        System.out.println(" ✓ Basic relationship event generation test passed");
    }

    /**
     * Test that relationship states can transition properly.
     */
    private static void testRelationshipStateTransitions() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Hire two staff members
        sim.hireStaff(Staff.Type.EXPERIENCED);
        sim.hireStaff(Staff.Type.EXPERIENCED);
        
        Staff staff1 = state.fohStaff.get(0);
        Staff staff2 = state.fohStaff.get(1);

        // Create a relationship
        StaffRelationship rel = new StaffRelationship(staff1.getId(), staff2.getId());
        state.staffRelationships.add(rel);

        // Test affinity changes
        double initialAffinity = rel.getAffinity();
        rel.adjustAffinity(3.0);
        assert rel.getAffinity() == initialAffinity + 3.0 : 
            "Affinity should increase by 3.0";

        // Test affinity clamping
        rel.adjustAffinity(20.0); // Try to go way over
        assert rel.getAffinity() <= 10.0 : 
            "Affinity should be clamped at 10.0";

        rel.adjustAffinity(-30.0); // Try to go way under
        assert rel.getAffinity() >= -10.0 : 
            "Affinity should be clamped at -10.0";

        // Test state transitions
        StaffRelationship.RelationshipState initialState = rel.getState();
        assert initialState == StaffRelationship.RelationshipState.NEUTRAL : 
            "Initial state should be NEUTRAL";

        rel.setState(StaffRelationship.RelationshipState.WORKS_WELL);
        assert rel.getState() == StaffRelationship.RelationshipState.WORKS_WELL : 
            "State should transition to WORKS_WELL";
        assert rel.getWeeksSinceStateChange() == 0 : 
            "Weeks since state change should reset";

        rel.incrementWeeksSinceStateChange();
        assert rel.getWeeksSinceStateChange() == 1 : 
            "Weeks since state change should increment";

        System.out.println(" ✓ Relationship state transitions test passed");
    }
}
