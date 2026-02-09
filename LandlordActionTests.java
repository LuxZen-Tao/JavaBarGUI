import javax.swing.JTextPane;

public class LandlordActionTests {
    public static void main(String[] args) {
        testActionsPerTier();
        testTierClamp();
        testOneActionPerRound();
        testCooldownTicking();
        testIdentityChanceBias();
        testBalancedOutcomePolarity();
        testActionsPanelSmoke();
        System.out.println("All LandlordActionTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    private static void testActionsPerTier() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 2;
        Simulation sim = newSimulation(state);
        assert sim.getAvailableActionsForCurrentTier().size() == 3 : "Tier should expose exactly 3 actions.";
    }

    private static void testTierClamp() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        state.pubLevel = 0;
        assert sim.landlordActionTier() == 1 : "Tier should clamp to min 1.";
        state.pubLevel = 9;
        assert sim.landlordActionTier() == 5 : "Tier should clamp to max 5.";
    }

    private static void testOneActionPerRound() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        state.nightOpen = true;
        state.roundInNight = 1;
        Simulation sim = newSimulation(state);
        LandlordActionDef first = sim.getAvailableActionsForCurrentTier().get(0);
        LandlordActionDef second = sim.getAvailableActionsForCurrentTier().get(1);

        LandlordActionResolution firstResult = sim.resolveLandlordAction(first.getId());
        assert !firstResult.blocked() : "First action should resolve.";
        LandlordActionResolution secondResult = sim.resolveLandlordAction(second.getId());
        assert secondResult.blocked() : "Second action in same round should be blocked.";
    }

    private static void testCooldownTicking() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        state.random.setSeed(42);
        Simulation sim = newSimulation(state);
        sim.openNight();
        LandlordActionDef action = sim.getAvailableActionsForCurrentTier().get(0);
        sim.resolveLandlordAction(action.getId());
        int cooldown = state.landlordActionStates.get(action.getId()).getCooldownRemaining();
        assert cooldown > 0 : "Cooldown should be set after use.";
        sim.playRound();
        int after = state.landlordActionStates.get(action.getId()).getCooldownRemaining();
        assert after == Math.max(0, cooldown - 1) : "Cooldown should tick down each round.";
        sim.playRound();
        int afterSecond = state.landlordActionStates.get(action.getId()).getCooldownRemaining();
        assert afterSecond <= after : "Cooldown should not increase while ticking.";
    }

    private static void testIdentityChanceBias() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        Simulation sim = newSimulation(state);
        LandlordActionDef classy = LandlordActionCatalog.byId(LandlordActionId.WORK_THE_ROOM);
        LandlordActionDef shady = LandlordActionCatalog.byId(LandlordActionId.PUSHY_UPSELL);

        state.landlordIdentityScore = 5.0;
        double classyPositive = sim.computeActionChance(classy);
        state.landlordIdentityScore = 0.0;
        double classyNeutral = sim.computeActionChance(classy);
        state.landlordIdentityScore = -5.0;
        double shadyNegative = sim.computeActionChance(shady);
        state.landlordIdentityScore = 0.0;
        double shadyNeutral = sim.computeActionChance(shady);

        assert classyPositive > classyNeutral : "Positive ID should boost classy chance.";
        assert shadyNegative > shadyNeutral : "Negative ID should boost shady chance.";
    }

    private static void testBalancedOutcomePolarity() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        Simulation sim = newSimulation(state);
        LandlordActionDef balanced = LandlordActionCatalog.byId(LandlordActionId.RUN_A_SPECIAL);

        double posSuccess = sim.computeOutcomeScale(balanced, true, 5.0);
        double negSuccess = sim.computeOutcomeScale(balanced, true, -5.0);
        double posFail = sim.computeOutcomeScale(balanced, false, 5.0);
        double negFail = sim.computeOutcomeScale(balanced, false, -5.0);

        assert posSuccess > negSuccess : "Positive ID should boost balanced success outcomes.";
        assert negFail > posFail : "Negative ID should worsen balanced failure outcomes.";
    }

    private static void testActionsPanelSmoke() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        Simulation sim = newSimulation(state);
        LandlordActionsPanel panel = new LandlordActionsPanel(sim, state, id -> {});
        assert panel.getActionRowCount() == 3 : "Actions panel should render three options.";
    }
}
