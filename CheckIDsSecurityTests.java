import javax.swing.JTextPane;

/**
 * Tests for Check IDs security task (T1_CHECK_IDS).
 * Validates that:
 * - Check IDs affects underage purchase logic (reduces serve chance)
 * - Check IDs remains active for exactly its configured duration
 * - Check IDs expires after duration and returns to baseline
 * - Cooldown starts after expiry and ticks down
 */
public class CheckIDsSecurityTests {
    public static void main(String[] args) {
        testCheckIDsReducesUnderageServeChance();
        testCheckIDsLastsConfiguredDuration();
        testCheckIDsExpiresAfterDuration();
        testCooldownStartsAfterExpiryAndTicksDown();
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
        s.baseSecurityLevel = 5;
        s.nightOpen = true;
        s.cash = 200.0;
        
        // Baseline incident multiplier
        double baselineMult = s.securityTaskIncidentChanceMultiplier();
        assert baselineMult == 1.0 : "Baseline incident multiplier should be 1.0";
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        
        // Task activates immediately
        assert s.activeSecurityTask == SecurityTask.T1_CHECK_IDS 
            : "Check IDs should be active";
        assert s.activeSecurityTaskRoundsRemaining == SecurityTask.T1_CHECK_IDS.getDurationRounds()
            : "Check IDs should have configured rounds remaining";

        sim.playRound();
        
        // Check that incident multiplier is reduced
        double activeMult = s.securityTaskIncidentChanceMultiplier();
        assert activeMult < baselineMult 
            : "Check IDs should reduce incident multiplier (expected 0.92, got " + activeMult + ")";
        assert Math.abs(activeMult - 0.92) < 0.01 
            : "Check IDs should have 0.92 incident multiplier";
    }

    /**
     * Test that Check IDs remains active for exactly its configured duration.
     */
    private static void testCheckIDsLastsConfiguredDuration() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        s.baseSecurityLevel = 5;
        s.nightOpen = true;
        s.cash = 200.0;
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        int duration = SecurityTask.T1_CHECK_IDS.getDurationRounds();
        assert s.activeSecurityTaskRoundsRemaining == duration : "Should start with configured duration";

        for (int round = 1; round < duration; round++) {
            sim.playRound();
            assert s.isSecurityTaskActive() : "Task should still be active before final active round";
            assert s.activeSecurityTaskRoundsRemaining == (duration - round)
                    : "Remaining rounds should decrement by one each round";
        }
    }

    /**
     * Test that Check IDs expires after duration and returns to baseline.
     */
    private static void testCheckIDsExpiresAfterDuration() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        s.baseSecurityLevel = 5;
        s.nightOpen = true;
        s.cash = 200.0;
        
        // Activate Check IDs
        sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        
        int duration = SecurityTask.T1_CHECK_IDS.getDurationRounds();
        for (int i = 0; i < duration; i++) {
            sim.playRound();
        }

        assert !s.isSecurityTaskActive() : "Task should expire right after configured duration";
        assert s.activeSecurityTask == null : "Active task should be cleared";
        assert s.activeSecurityTaskRoundsRemaining == 0 : "Rounds remaining should be 0";
        
        // Verify incident multiplier returns to baseline
        double mult = s.securityTaskIncidentChanceMultiplier();
        assert mult == 1.0 : "Incident multiplier should return to 1.0 after expiration";
    }

    private static void testCooldownStartsAfterExpiryAndTicksDown() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        s.baseSecurityLevel = 5;
        s.nightOpen = true;
        s.cash = 200.0;

        SecurityTask task = SecurityTask.T1_CHECK_IDS;
        sim.resolveSecurityTask(task);
        assert s.securityTaskCooldownRemaining(task) == 0 : "Cooldown should not begin on activation";

        for (int i = 0; i < task.getDurationRounds(); i++) {
            sim.playRound();
        }
        assert s.securityTaskCooldownRemaining(task) == task.getCooldownRounds()
                : "Cooldown should start after expiry";

        sim.playRound();
        assert s.securityTaskCooldownRemaining(task) == task.getCooldownRounds() - 1
                : "Cooldown should decrement each round";
    }
}
