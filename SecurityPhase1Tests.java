import javax.swing.JTextPane;

public class SecurityPhase1Tests {
    public static void main(String[] args) {
        testPolicyAffectsSecurityAndTraffic();
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

        sim.setSecurityPolicy(SecurityPolicy.STRICT_DOOR);
        int strict = sim.securityBreakdown().total();
        double strictTraffic = sim.securityPolicyTrafficMultiplier();

        sim.setSecurityPolicy(SecurityPolicy.FRIENDLY_WELCOME);
        int friendly = sim.securityBreakdown().total();
        double friendlyTraffic = sim.securityPolicyTrafficMultiplier();

        assert strict > balanced : "Strict should increase effective security.";
        assert friendly < balanced : "Friendly should decrease effective security.";
        assert strictTraffic < balancedTraffic : "Strict should reduce traffic.";
        assert friendlyTraffic > balancedTraffic : "Friendly should increase traffic.";
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
