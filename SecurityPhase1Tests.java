import javax.swing.JTextPane;

public class SecurityPhase1Tests {
    public static void main(String[] args) {
        testPolicyAffectsSecurityAndTraffic();
        testSecurityTaskTierUnlocks();
        testSecurityTaskUsageAndCooldown();
        testSecurityTaskActivationCost();
        testSecurityTaskActivationRequiresCash();
        testSecurityTaskMultipliers();
        testHudBadgeSummary();
        testMissionControlSecuritySummary();
        testBouncerMitigationApplies();
        testCctvMitigationApplies();
        testSecurityBreakdownConsistency();
        System.out.println("All SecurityPhase1Tests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testPolicyAffectsSecurityAndTraffic() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 2;
        Simulation sim = newSimulation(state);

        sim.setSecurityPolicy(SecurityPolicy.BALANCED_DOOR);
        int balanced = sim.securityBreakdown().total();
        double balancedTraffic = sim.securityPolicyTrafficMultiplier();
        double balancedIncident = sim.securityPolicyIncidentChanceMultiplier();

        sim.setSecurityPolicy(SecurityPolicy.STRICT_DOOR);
        int strict = sim.securityBreakdown().total();
        double strictTraffic = sim.securityPolicyTrafficMultiplier();
        double strictIncident = sim.securityPolicyIncidentChanceMultiplier();

        sim.setSecurityPolicy(SecurityPolicy.FRIENDLY_WELCOME);
        int friendly = sim.securityBreakdown().total();
        double friendlyTraffic = sim.securityPolicyTrafficMultiplier();
        double friendlyIncident = sim.securityPolicyIncidentChanceMultiplier();

        assert strict > balanced : "Strict should increase effective security.";
        assert friendly < balanced : "Friendly should decrease effective security.";
        assert strictTraffic < balancedTraffic : "Strict should reduce traffic.";
        assert friendlyTraffic > balancedTraffic : "Friendly should increase traffic.";
        assert strictIncident < balancedIncident : "Strict should reduce incident chance.";
        assert friendlyIncident > balancedIncident : "Friendly should increase incident chance.";
    }

    private static void testSecurityTaskTierUnlocks() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);

        state.baseSecurityLevel = 4;
        assert sim.getAvailableSecurityTasks().isEmpty() : "Base security <5 should unlock no tasks.";

        state.baseSecurityLevel = 5;
        assert sim.getAvailableSecurityTasks().size() == 3 : "Tier 1 should unlock 3 tasks.";

        state.baseSecurityLevel = 15;
        assert sim.getAvailableSecurityTasks().size() == 6 : "Tier 2 should unlock 6 tasks.";

        state.baseSecurityLevel = 30;
        assert sim.getAvailableSecurityTasks().size() == 9 : "Tier 3 should unlock 9 tasks.";
    }

    private static void testSecurityTaskUsageAndCooldown() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 30;
        state.nightOpen = true;
        state.cash = 500.0;
        Simulation sim = newSimulation(state);

        SecurityTask task = SecurityTask.T1_VISIBLE_PATROL;
        SecurityTaskResolution result = sim.resolveSecurityTask(task);
        assert result.applied() : "Expected task to apply.";
        assert state.activeSecurityTask == task : "Task should be active.";
        assert state.securityTaskCooldownRemaining(task) == 0 : "Cooldown should start only after expiry.";

        SecurityTaskResolution blocked = sim.resolveSecurityTask(SecurityTask.T1_CHECK_IDS);
        assert !blocked.applied() : "Only one task can be active at a time.";

        Simulation.SecurityTaskAvailability availability = sim.securityTaskAvailability(task);
        assert !availability.canUse() : "Active task should block reuse.";

        for (int i = 0; i < task.getDurationRounds(); i++) {
            sim.playRound();
        }
        assert state.activeSecurityTask == null : "Task should expire after duration.";
        assert state.securityTaskCooldownRemaining(task) == task.getCooldownRounds() : "Cooldown should begin after expiry.";
    }

    private static void testSecurityTaskActivationCost() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 30;
        state.nightOpen = true;
        state.cash = 200.0;
        Simulation sim = newSimulation(state);

        SecurityTask task = SecurityTask.T1_CHECK_IDS;
        double before = state.cash;
        SecurityTaskResolution result = sim.resolveSecurityTask(task);
        assert result.applied() : "Task should activate when cash is sufficient.";
        assert Math.abs(state.cash - (before - task.getActivationCost())) < 0.001 : "Activation should deduct cash immediately.";
    }

    private static void testSecurityTaskActivationRequiresCash() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 30;
        state.nightOpen = true;
        SecurityTask task = SecurityTask.T3_ZERO_TOLERANCE_NIGHT;
        state.cash = task.getActivationCost() - 1;
        Simulation sim = newSimulation(state);

        SecurityTaskResolution result = sim.resolveSecurityTask(task);
        assert !result.applied() : "Task should be blocked when cash is below cost.";
        assert state.activeSecurityTask == null : "No task should activate when cash is insufficient.";
    }

    private static void testSecurityTaskMultipliers() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 30;
        SecurityTask task = SecurityTask.T2_TARGETED_SCREENING;
        state.activeSecurityTask = task;
        state.activeSecurityTaskRound = state.currentRoundIndex();
        state.activeSecurityTaskRoundsRemaining = task.getDurationRounds();

        assert Math.abs(state.securityTaskIncidentChanceMultiplier() - task.getIncidentChanceMultiplier()) < 0.0001
                : "Incident multiplier should match task.";
        assert Math.abs(state.securityTaskTrafficMultiplier() - task.getTrafficMultiplier()) < 0.0001
                : "Traffic multiplier should match task.";
    }

    private static void testHudBadgeSummary() {
        String badge = WineBarGUI.buildSecurityBadgeText(8, "B", "Patrol", "Bouncers: 1/2", "Rep x0.80");
        assert badge.contains("Policy: B") : "HUD badge should include policy.";
        assert badge.contains("Task: Patrol") : "HUD badge should include task.";
        assert badge.contains("Bouncers: 1/2") : "HUD badge should include bouncers.";
        assert badge.contains("Rep x0.80") : "HUD badge should include mitigation.";
    }

    private static void testMissionControlSecuritySummary() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 10;
        state.activeSecurityTask = SecurityTask.T1_VISIBLE_PATROL;
        state.activeSecurityTaskRound = state.currentRoundIndex();
        state.securityPolicy = SecurityPolicy.STRICT_DOOR;
        Simulation sim = newSimulation(state);

        String securityText = sim.buildMetricsSnapshot().security;
        assert securityText.contains("Security policy: Strict Door") : "Security tab should show policy.";
        assert securityText.contains("Base security level: 10") : "Security tab should show base security.";
        assert securityText.contains("Security task: Visible Patrol") : "Security tab should show task.";
    }

    private static void testBouncerMitigationApplies() {
        GameState state = GameFactory.newGame();
        state.bouncersHiredTonight = 1;
        int repHit = -10;
        int mitigated = state.mitigateSecurityRepHit(repHit);
        assert mitigated > repHit : "Bouncer mitigation should reduce rep loss.";

        GameState noBouncer = GameFactory.newGame();
        int noMitigation = noBouncer.mitigateSecurityRepHit(repHit);
        assert noMitigation == repHit : "No bouncer should mean no mitigation.";
    }

    private static void testCctvMitigationApplies() {
        GameState state = GameFactory.newGame();
        state.ownedUpgrades.add(PubUpgrade.CCTV_PACKAGE);
        int repHit = -10;
        int mitigated = state.mitigateSecurityRepHit(repHit);
        assert mitigated > repHit : "CCTV should reduce rep loss.";

        Simulation sim = newSimulation(state);
        String securityText = sim.buildMetricsSnapshot().security;
        assert securityText.contains("CCTV") : "Security breakdown should mention CCTV.";
    }

    private static void testSecurityBreakdownConsistency() {
        GameState state = GameFactory.newGame();
        state.baseSecurityLevel = 3;
        state.bouncersHiredTonight = 1;
        state.securityPolicy = SecurityPolicy.STRICT_DOOR;
        Simulation sim = newSimulation(state);
        SecuritySystem.SecurityBreakdown breakdown = sim.securityBreakdown();
        int sum = breakdown.base() + breakdown.upgrades() + breakdown.policy()
                + breakdown.bouncers() + breakdown.manager() + breakdown.staff();
        assert sum == breakdown.total() : "Breakdown sum should equal total security.";
    }
}
