import javax.swing.JTextPane;

/**
 * Test: SecurityTask_AvailabilityPrereqs
 * Validates that security tasks only appear when prerequisites are met:
 * 1. Player must have locks upgrade (any tier of Reinforced Door)
 * 2. Player must have Bouncer hired OR Marshalls hired
 */
public class SecurityTask_AvailabilityPrereqs {
    public static void main(String[] args) {
        testSecurityTaskRequiresLocksUpgrade();
        testSecurityTaskRequiresBouncerOrMarshalls();
        testSecurityTaskAvailableWithBouncerAndLocks();
        testSecurityTaskAvailableWithMarshallsAndLocks();
        testSecurityTaskAvailableWithBothBouncerMarshallsAndLocks();
        System.out.println("All SecurityTask_AvailabilityPrereqs tests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that security tasks are unavailable without locks upgrade.
     */
    private static void testSecurityTaskRequiresLocksUpgrade() {
        GameState s = GameFactory.newGame();
        s.baseSecurityLevel = 10; // High enough for tier 1 tasks
        s.nightOpen = true;
        s.cash = 500.0;
        s.bouncersHiredTonight = 1; // Have bouncer
        Simulation sim = newSimulation(s);
        
        // No locks upgrade
        assert s.reinforcedDoorTier() == 0 : "Should have no locks upgrade.";
        
        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        
        assert !availability.canUse() : "Task should be unavailable without locks upgrade.";
        assert availability.reason().contains("locks") || availability.reason().contains("Reinforced Door") 
            : "Reason should mention locks/Reinforced Door, got: " + availability.reason();
    }

    /**
     * Test that security tasks are unavailable without bouncers or marshalls.
     */
    private static void testSecurityTaskRequiresBouncerOrMarshalls() {
        GameState s = GameFactory.newGame();
        s.baseSecurityLevel = 10; // High enough for tier 1 tasks
        s.nightOpen = true;
        s.cash = 500.0;
        s.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_I); // Have locks
        Simulation sim = newSimulation(s);
        
        // No bouncer or marshalls
        assert s.bouncersHiredTonight == 0 : "Should have no bouncers.";
        assert s.marshallCount() == 0 : "Should have no marshalls.";
        
        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        
        assert !availability.canUse() : "Task should be unavailable without bouncer or marshalls.";
        assert availability.reason().contains("Bouncer") || availability.reason().contains("Marshall") 
            : "Reason should mention Bouncer or Marshalls, got: " + availability.reason();
    }

    /**
     * Test that security tasks are available with bouncer and locks.
     */
    private static void testSecurityTaskAvailableWithBouncerAndLocks() {
        GameState s = GameFactory.newGame();
        s.baseSecurityLevel = 10; // High enough for tier 1 tasks
        s.nightOpen = true;
        s.cash = 500.0;
        s.bouncersHiredTonight = 1; // Have bouncer
        s.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_I); // Have locks
        Simulation sim = newSimulation(s);
        
        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        
        assert availability.canUse() : "Task should be available with bouncer and locks. Reason: " + availability.reason();
    }

    /**
     * Test that security tasks are available with marshalls and locks (no bouncer).
     */
    private static void testSecurityTaskAvailableWithMarshallsAndLocks() {
        GameState s = GameFactory.newGame();
        s.baseSecurityLevel = 10; // High enough for tier 1 tasks
        s.nightOpen = true;
        s.cash = 500.0;
        s.bouncersHiredTonight = 0; // No bouncer
        s.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_II); // Have locks (tier 2)
        s.ownedUpgrades.add(PubUpgrade.MARSHALLS_I); // Have marshalls upgrade
        s.marshalls.add(BouncerQuality.MEDIUM); // Have at least one marshall hired
        Simulation sim = newSimulation(s);
        
        assert s.marshallCount() > 0 : "Should have marshalls.";
        
        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        
        assert availability.canUse() : "Task should be available with marshalls and locks. Reason: " + availability.reason();
    }

    /**
     * Test that security tasks are available with both bouncer, marshalls, and locks.
     */
    private static void testSecurityTaskAvailableWithBothBouncerMarshallsAndLocks() {
        GameState s = GameFactory.newGame();
        s.baseSecurityLevel = 10; // High enough for tier 1 tasks
        s.nightOpen = true;
        s.cash = 500.0;
        s.bouncersHiredTonight = 2; // Have bouncers
        s.ownedUpgrades.add(PubUpgrade.REINFORCED_DOOR_III); // Have locks (tier 3)
        s.ownedUpgrades.add(PubUpgrade.MARSHALLS_II); // Have marshalls upgrade
        s.marshalls.add(BouncerQuality.HIGH); // Have marshalls hired
        Simulation sim = newSimulation(s);
        
        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        
        assert availability.canUse() : "Task should be available with both bouncer, marshalls and locks. Reason: " + availability.reason();
        
        // Test that task can be resolved
        SecurityTaskResolution result = sim.resolveSecurityTask(task);
        assert result.applied() : "Task should apply successfully.";
    }
}
