import javax.swing.JTextPane;

public class LandlordPromptEventManualTest {
    public static void main(String[] args) {
        // Create a game state that will trigger an event
        GameState state = GameFactory.newGame();
        state.dayCounter = 10; // Past intro week
        state.nightCount = 15;
        state.lastLandlordPromptEventNight = 5; // Long enough ago
        state.landlordPromptEventsThisWeek = 0; // No events this week yet
        state.chaos = 70.0; // High chaos for better spawn chance
        state.reputation = 65; // High reputation for better spawn chance
        
        // Create simulation
        UILogger log = new UILogger(new JTextPane());
        Simulation sim = new Simulation(state, log);
        
        // Test spawn logic
        System.out.println("Testing Landlord Prompt Event Spawn...");
        System.out.println("Day counter: " + state.dayCounter);
        System.out.println("Night count: " + state.nightCount);
        System.out.println("Last event night: " + state.lastLandlordPromptEventNight);
        System.out.println("Events this week: " + state.landlordPromptEventsThisWeek);
        System.out.println("Chaos: " + state.chaos);
        System.out.println("Reputation: " + state.reputation);
        System.out.println();
        
        // Try to spawn event multiple times
        int attempts = 0;
        int maxAttempts = 50;
        LandlordPromptEventDef triggeredEvent = null;
        
        while (triggeredEvent == null && attempts < maxAttempts) {
            attempts++;
            triggeredEvent = sim.checkLandlordPromptEvent();
        }
        
        if (triggeredEvent == null) {
            System.out.println("No event triggered after " + maxAttempts + " attempts.");
            System.out.println("This is possible due to RNG. Expected spawn rate is ~16% per attempt.");
            System.out.println("Test completed (no event to display).");
        } else {
            System.out.println("✅ Event triggered after " + attempts + " attempts!");
            System.out.println("Event: " + triggeredEvent.getId());
            System.out.println("Prompt: " + triggeredEvent.getPromptText());
            System.out.println();
            
            // Test all options
            for (LandlordPromptOption option : LandlordPromptOption.values()) {
                System.out.println("Option " + option + ": " + triggeredEvent.getOptionText(option));
                
                // Test all result types for this option
                for (LandlordPromptResultType resultType : LandlordPromptResultType.values()) {
                    LandlordPromptOutcome outcome = triggeredEvent.getOutcome(option, resultType);
                    System.out.println("  " + resultType + ": " + outcome.getRandomText(state.random));
                    LandlordPromptEffectPackage effects = outcome.getEffectPackage();
                    System.out.println("    Cash: " + effects.getCashDelta() + 
                                     ", Rep: " + effects.getReputationDelta() +
                                     ", Chaos: " + effects.getChaosDelta() +
                                     ", Morale: " + effects.getMoraleDelta() +
                                     ", Service: " + effects.getServiceEfficiencyDelta() +
                                     ", SupplierTrust: " + effects.getSupplierTrustDelta());
                }
                System.out.println();
            }
            
            // Record event and test application
            sim.recordLandlordPromptEventOccurred();
            System.out.println("Event recorded. Last event night: " + state.lastLandlordPromptEventNight);
            System.out.println("Events this week: " + state.landlordPromptEventsThisWeek);
            
            // Test effect application
            System.out.println("\nTesting effect application...");
            double cashBefore = state.cash;
            int repBefore = state.reputation;
            double chaosBefore = state.chaos;
            double moraleBefore = state.teamMorale;
            
            // Apply a sample effect
            LandlordPromptEffectPackage testEffect = LandlordPromptEffectPackage.builder()
                .cash(500)
                .reputation(3)
                .chaos(-5.0)
                .morale(2)
                .build();
            
            sim.applyLandlordPromptEventEffects(testEffect);
            
            System.out.println("Cash: " + cashBefore + " → " + state.cash + " (expected: " + (cashBefore + 500) + ")");
            System.out.println("Reputation: " + repBefore + " → " + state.reputation);
            System.out.println("Chaos: " + chaosBefore + " → " + state.chaos + " (expected: " + (chaosBefore - 5.0) + ")");
            System.out.println("Morale: " + moraleBefore + " → " + state.teamMorale + " (expected: " + (moraleBefore + 2) + ")");
            
            assert Math.abs(state.cash - (cashBefore + 500)) < 0.01 : "Cash should increase by 500";
            assert state.chaos == chaosBefore - 5.0 : "Chaos should decrease by 5.0";
            
            System.out.println("\n✅ All manual tests passed!");
        }
        
        System.exit(0);
    }
}
