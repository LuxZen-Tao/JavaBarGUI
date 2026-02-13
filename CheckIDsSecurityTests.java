import javax.swing.JTextPane;

/**
 * Tests for Check IDs security task (T1_CHECK_IDS).
 * Validates that:
 * - Check IDs affects underage purchase logic (reduces serve chance)
 * - Check IDs remains active for exactly 3 rounds
 * - Check IDs expires after 3 rounds and returns to baseline
 */
public class CheckIDsSecurityTests {
    public static void main(String[] args) {
        testCheckIDsReducesUnderageServeChance();
        testCheckIDsLastsThreeRounds();
        testCheckIDsExpiresAfterThreeRounds();
        System.out.println("All CheckIDsSecurityTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that Check IDs task reduces incident chance when active.
     */
    private static void testCheckIDsReducesUnderageServeChance() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Baseline incident multiplier
        double baselineMult = s.securityTaskIncidentChanceMultiplier();
        assert baselineMult == 1.0 : "Baseline incident multiplier should be 1.0";
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        
        // Task is queued for next round
        assert s.activeSecurityTask == SecurityTask.T1_CHECK_IDS 
            : "Check IDs should be active";
        assert s.activeSecurityTaskRoundsRemaining == 3 
            : "Check IDs should have 3 rounds remaining";
        
        // Move to next round where task becomes active
        sim.openNight();
        sim.playRound();
        
        // Check that incident multiplier is reduced
        double activeMult = s.securityTaskIncidentChanceMultiplier();
        assert activeMult < baselineMult 
            : "Check IDs should reduce incident multiplier (expected 0.92, got " + activeMult + ")";
        assert Math.abs(activeMult - 0.92) < 0.01 
            : "Check IDs should have 0.92 incident multiplier";
    }

    /**
     * Test that Check IDs remains active for exactly 3 rounds.
     */
    private static void testCheckIDsLastsThreeRounds() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        assert s.activeSecurityTaskRoundsRemaining == 3 : "Should start with 3 rounds";
        
        // Open night and advance to first round of effect
        sim.openNight();
        sim.playRound(); // Round 1
        assert s.isSecurityTaskActive() : "Task should be active in round 1";
        assert s.activeSecurityTaskRoundsRemaining == 2 : "Should have 2 rounds remaining after round 1";
        
        sim.playRound(); // Round 2
        assert s.isSecurityTaskActive() : "Task should be active in round 2";
        assert s.activeSecurityTaskRoundsRemaining == 1 : "Should have 1 round remaining after round 2";
        
        sim.playRound(); // Round 3
        assert s.isSecurityTaskActive() : "Task should be active in round 3";
        assert s.activeSecurityTaskRoundsRemaining == 0 : "Should have 0 rounds remaining after round 3";
    }

    /**
     * Test that Check IDs expires after 3 rounds and returns to baseline.
     */
    private static void testCheckIDsExpiresAfterThreeRounds() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        
        // Open night and run through 3 rounds
        sim.openNight();
        sim.playRound(); // Round 1
        assert s.isSecurityTaskActive() : "Task should be active in round 1";
        
        sim.playRound(); // Round 2
        assert s.isSecurityTaskActive() : "Task should be active in round 2";
        
        sim.playRound(); // Round 3
        assert s.isSecurityTaskActive() : "Task should be active in round 3";
        
        sim.playRound(); // Round 4
        assert !s.isSecurityTaskActive() : "Task should NOT be active in round 4";
        assert s.activeSecurityTask == null : "Active task should be cleared";
        assert s.activeSecurityTaskRoundsRemaining == 0 : "Rounds remaining should be 0";
        
        // Verify incident multiplier returns to baseline
        double mult = s.securityTaskIncidentChanceMultiplier();
        assert mult == 1.0 : "Incident multiplier should return to 1.0 after expiration";
    }
}
