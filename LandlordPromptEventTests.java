import java.util.Random;

public class LandlordPromptEventTests {
    public static void main(String[] args) {
        testEventCatalog();
        testSpawnLogic();
        testConstraints();
        testEffectPackages();
        testResultDistribution();
        System.out.println("All LandlordPromptEventTests passed.");
        System.exit(0);
    }

    private static void testEventCatalog() {
        // Test all 6 events are defined
        assert LandlordPromptEventCatalog.allEvents().size() == 6 : "Should have 6 events";
        
        // Test each event has 3 options
        for (LandlordPromptEventDef event : LandlordPromptEventCatalog.allEvents()) {
            assert event.getOptionText(LandlordPromptOption.A) != null : "Option A should exist";
            assert event.getOptionText(LandlordPromptOption.B) != null : "Option B should exist";
            assert event.getOptionText(LandlordPromptOption.C) != null : "Option C should exist";
            
            // Test each option has GOOD/NEUTRAL/BAD outcomes
            for (LandlordPromptOption option : LandlordPromptOption.values()) {
                assert event.getOutcome(option, LandlordPromptResultType.GOOD) != null : "GOOD outcome missing";
                assert event.getOutcome(option, LandlordPromptResultType.NEUTRAL) != null : "NEUTRAL outcome missing";
                assert event.getOutcome(option, LandlordPromptResultType.BAD) != null : "BAD outcome missing";
                
                // Test each outcome has 3 text variants
                LandlordPromptOutcome good = event.getOutcome(option, LandlordPromptResultType.GOOD);
                assert good.getTextVariants().size() == 3 : "Should have 3 text variants for GOOD";
                LandlordPromptOutcome neutral = event.getOutcome(option, LandlordPromptResultType.NEUTRAL);
                assert neutral.getTextVariants().size() == 3 : "Should have 3 text variants for NEUTRAL";
                LandlordPromptOutcome bad = event.getOutcome(option, LandlordPromptResultType.BAD);
                assert bad.getTextVariants().size() == 3 : "Should have 3 text variants for BAD";
            }
        }
    }

    private static void testSpawnLogic() {
        GameState state = GameFactory.newGame();
        state.dayCounter = 8; // Past intro week
        state.nightCount = 10;
        state.lastLandlordPromptEventNight = 1;
        state.landlordPromptEventsThisWeek = 0;
        
        LandlordPromptEventSystem system = new LandlordPromptEventSystem(state);
        
        // Test that event can spawn
        int spawns = 0;
        for (int i = 0; i < 1000; i++) {
            state.random = new Random(i);
            LandlordPromptEventDef event = system.maybeSpawnEvent();
            if (event != null) {
                spawns++;
            }
        }
        assert spawns > 0 : "Events should spawn over 1000 trials";
        assert spawns < 1000 : "Events should not spawn every time";
        
        // Test chaos modifier increases spawn rate
        state.chaos = 70.0;
        int highChaosSpawns = 0;
        for (int i = 0; i < 1000; i++) {
            state.random = new Random(i);
            LandlordPromptEventDef event = system.maybeSpawnEvent();
            if (event != null) {
                highChaosSpawns++;
            }
        }
        assert highChaosSpawns > spawns : "High chaos should increase spawn rate";
    }

    private static void testConstraints() {
        GameState state = GameFactory.newGame();
        LandlordPromptEventSystem system = new LandlordPromptEventSystem(state);
        
        // Test intro week constraint
        state.dayCounter = 3; // During intro week
        state.nightCount = 3;
        state.lastLandlordPromptEventNight = -999;
        state.landlordPromptEventsThisWeek = 0;
        state.chaos = 100.0; // High spawn chance
        state.reputation = 100; // High spawn chance
        
        boolean spawnedDuringIntro = false;
        for (int i = 0; i < 100; i++) {
            state.random = new Random(i);
            if (system.maybeSpawnEvent() != null) {
                spawnedDuringIntro = true;
                break;
            }
        }
        assert !spawnedDuringIntro : "Should not spawn during intro week";
        
        // Test cooldown constraint
        state.dayCounter = 10;
        state.nightCount = 10;
        state.lastLandlordPromptEventNight = 9; // Last event was 1 night ago
        state.landlordPromptEventsThisWeek = 0;
        
        boolean spawnedDuringCooldown = false;
        for (int i = 0; i < 100; i++) {
            state.random = new Random(i);
            if (system.maybeSpawnEvent() != null) {
                spawnedDuringCooldown = true;
                break;
            }
        }
        assert !spawnedDuringCooldown : "Should not spawn during cooldown period";
        
        // Test max events per week constraint
        state.nightCount = 15;
        state.lastLandlordPromptEventNight = 10;
        state.landlordPromptEventsThisWeek = 2; // Already 2 events this week
        
        boolean spawnedAfterMax = false;
        for (int i = 0; i < 100; i++) {
            state.random = new Random(i);
            if (system.maybeSpawnEvent() != null) {
                spawnedAfterMax = true;
                break;
            }
        }
        assert !spawnedAfterMax : "Should not spawn when max events per week reached";
    }

    private static void testEffectPackages() {
        // Test effect package builder
        LandlordPromptEffectPackage pkg = LandlordPromptEffectPackage.builder()
            .cash(1200)
            .reputation(3)
            .chaos(-2.0)
            .morale(4)
            .serviceEfficiency(2)
            .supplierTrust(-1.0)
            .build();
        
        assert pkg.getCashDelta() == 1200 : "Cash delta should be 1200";
        assert pkg.getReputationDelta() == 3 : "Reputation delta should be 3";
        assert pkg.getChaosDelta() == -2.0 : "Chaos delta should be -2.0";
        assert pkg.getMoraleDelta() == 4 : "Morale delta should be 4";
        assert pkg.getServiceEfficiencyDelta() == 2 : "Service efficiency delta should be 2";
        assert pkg.getSupplierTrustDelta() == -1.0 : "Supplier trust delta should be -1.0";
        
        // Test empty package
        LandlordPromptEffectPackage empty = LandlordPromptEffectPackage.builder().build();
        assert empty.getCashDelta() == 0 : "Default cash delta should be 0";
        assert empty.getReputationDelta() == 0 : "Default reputation delta should be 0";
    }

    private static void testResultDistribution() {
        GameState state = GameFactory.newGame();
        LandlordPromptEventSystem system = new LandlordPromptEventSystem(state);
        
        int goodCount = 0;
        int neutralCount = 0;
        int badCount = 0;
        
        // Roll 300 times to test distribution
        for (int i = 0; i < 300; i++) {
            state.random = new Random(i);
            LandlordPromptResultType result = system.rollResultType();
            switch (result) {
                case GOOD: goodCount++; break;
                case NEUTRAL: neutralCount++; break;
                case BAD: badCount++; break;
            }
        }
        
        // Each should be roughly 33% (allow 20-45% range for variance)
        assert goodCount >= 60 && goodCount <= 135 : "GOOD results should be ~33%: " + goodCount;
        assert neutralCount >= 60 && neutralCount <= 135 : "NEUTRAL results should be ~33%: " + neutralCount;
        assert badCount >= 60 && badCount <= 135 : "BAD results should be ~33%: " + badCount;
        assert goodCount + neutralCount + badCount == 300 : "Total should be 300";
    }
}
