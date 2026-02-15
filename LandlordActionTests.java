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
        testLandlordAction_CooldownScalesWithUse();
        testLandlordAction_CostApplied();
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

    private static void testLandlordAction_CooldownScalesWithUse() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        state.cash = 1000.0;  // Ensure enough cash
        state.nightOpen = true;
        state.roundInNight = 1;
        Simulation sim = newSimulation(state);
        LandlordActionDef action = sim.getAvailableActionsForCurrentTier().get(0);
        
        // First use - baseline cooldown
        LandlordActionResolution firstResult = sim.resolveLandlordAction(action.getId());
        assert !firstResult.blocked() : "First use should not be blocked.";
        int firstCooldown = state.landlordActionStates.get(action.getId()).getCooldownRemaining();
        int baseCooldown = action.getCooldownRounds();
        assert firstCooldown == baseCooldown : "First use should have base cooldown: expected " + baseCooldown + " but got " + firstCooldown;
        
        // Wait out the cooldown
        for (int i = 0; i < firstCooldown + 1; i++) {
            state.lastLandlordActionRound = -999;  // Reset per-round lock
            sim.playRound();
        }
        
        // Second use - should have higher cooldown
        state.lastLandlordActionRound = -999;
        LandlordActionResolution secondResult = sim.resolveLandlordAction(action.getId());
        assert !secondResult.blocked() : "Second use should not be blocked after cooldown.";
        int secondCooldown = state.landlordActionStates.get(action.getId()).getCooldownRemaining();
        assert secondCooldown > firstCooldown : "Second use cooldown (" + secondCooldown + ") should be greater than first (" + firstCooldown + ")";
        
        // Verify scaling formula: cooldown = baseCooldown * (1.0 + min(1.0, usesCount * 0.1))
        // After first use, usesCount = 1, so expected = baseCooldown * 1.1
        int expectedSecondCooldown = (int) Math.ceil(baseCooldown * 1.1);
        assert secondCooldown == expectedSecondCooldown : "Second cooldown should be " + expectedSecondCooldown + " but got " + secondCooldown;
    }

    private static void testLandlordAction_CostApplied() {
        GameState state = GameFactory.newGame();
        state.pubLevel = 1;
        state.cash = 100.0;
        state.nightOpen = true;
        state.roundInNight = 1;
        Simulation sim = newSimulation(state);
        LandlordActionDef action = sim.getAvailableActionsForCurrentTier().get(0);
        
        double cashBefore = state.cash;
        int expectedCost = action.getBaseCost();
        
        LandlordActionResolution result = sim.resolveLandlordAction(action.getId());
        assert !result.blocked() : "Action should not be blocked with sufficient cash.";
        
        double cashAfter = state.cash;
        double actualCost = cashBefore - cashAfter;
        
        assert Math.abs(actualCost - expectedCost) < 0.01 : "Cost should be " + expectedCost + " but " + actualCost + " was deducted.";
        
        // Test insufficient cash
        state.cash = 5.0;  // Less than action cost
        state.lastLandlordActionRound = -999;  // Reset per-round lock
        LandlordActionResolution blockedResult = sim.resolveLandlordAction(action.getId());
        assert blockedResult.blocked() : "Action should be blocked with insufficient cash.";
        assert blockedResult.message().contains("Insufficient cash") : "Blocked message should mention insufficient cash.";
    }
}
