import javax.swing.JTextPane;

/**
 * Tests for milestone streak logic fixes.
 * Validates that M2_NO_EMPTY_SHELVES checks actual inventory levels at end of service
 * and that M1_OPEN_FOR_BUSINESS requires 5 nights and resets on bankruptcy.
 */
public class MilestoneStreakTests {
    public static void main(String[] args) {
        testNoEmptyShelvesStreakResetsOnStockout();
        testNoEmptyShelvesAwardsAfterTwoNights();
        testNoEmptyShelvesDoesNotAwardAfterOneNight();
        testNoEmptyShelvesStreakResetsOnWineStockout();
        testNoEmptyShelvesStreakResetsOnFoodStockout();
        testOpenForBusinessRequiresFiveNights();
        testOpenForBusinessResetsOnBankruptcy();
        testOpenForBusinessDoesNotAwardAfterFourNights();
        System.out.println("All MilestoneStreakTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test: Night 1 OK, Night 2 stock-out → streak resets, no award
     */
    private static void testNoEmptyShelvesStreakResetsOnStockout() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Night 1: End with inventory
        Wine wine = state.supplier.get(0);
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        // Unlock kitchen so we can add food
        state.kitchenUnlocked = true;
        Food food = new Food("Test Meal", 2.0, 5.0, 1, FoodCategory.CHEAP_BAR_FOOD, 1.0, 3);
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 1 complete");
        
        // Verify streak incremented
        assert state.noStockoutStreakNights == 1 : "Streak should be 1 after night 1, was " + state.noStockoutStreakNights;
        
        // Night 2: Clear all inventory before closing
        state.rack.getBottleEntries().clear();
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 2 with stockout");
        
        // Verify streak reset
        assert state.noStockoutStreakNights == 0 : "Streak should reset to 0 after stockout, was " + state.noStockoutStreakNights;
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES)
            : "M2_NO_EMPTY_SHELVES should NOT be awarded after stockout";
    }

    /**
     * Test: Night 1 OK, Night 2 OK → awards
     */
    private static void testNoEmptyShelvesAwardsAfterTwoNights() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        state.kitchenUnlocked = true;
        Food food = new Food("Test Meal", 2.0, 5.0, 1, FoodCategory.CHEAP_BAR_FOOD, 1.0, 3);
        
        // Night 1: End with inventory
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 1");
        
        assert state.noStockoutStreakNights == 1 : "Streak should be 1 after night 1";
        
        // Night 2: End with inventory
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 2");
        
        // Verify streak incremented and milestone awarded
        assert state.noStockoutStreakNights == 2 : "Streak should be 2 after night 2, was " + state.noStockoutStreakNights;
        assert state.achievedMilestones.contains(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES)
            : "M2_NO_EMPTY_SHELVES should be awarded after 2 consecutive nights with inventory";
    }

    /**
     * Test: One night with inventory should not award milestone
     */
    private static void testNoEmptyShelvesDoesNotAwardAfterOneNight() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        state.kitchenUnlocked = true;
        Food food = new Food("Test Meal", 2.0, 5.0, 1, FoodCategory.CHEAP_BAR_FOOD, 1.0, 3);
        
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 1");
        
        assert state.noStockoutStreakNights == 1 : "Streak should be 1 after night 1";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES)
            : "M2_NO_EMPTY_SHELVES should NOT be awarded after only 1 night";
    }

    /**
     * Test: Streak resets when wine inventory is empty
     */
    private static void testNoEmptyShelvesStreakResetsOnWineStockout() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        state.kitchenUnlocked = true;
        Food food = new Food("Test Meal", 2.0, 5.0, 1, FoodCategory.CHEAP_BAR_FOOD, 1.0, 3);
        
        // Night 1: Both inventories OK
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 1");
        
        assert state.noStockoutStreakNights == 1 : "Streak should be 1 after night 1";
        
        // Night 2: Food OK, but wine empty
        state.rack.getBottleEntries().clear();  // Empty wine rack
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 2 - wine empty");
        
        assert state.noStockoutStreakNights == 0 : "Streak should reset when wine inventory empty";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES)
            : "M2_NO_EMPTY_SHELVES should NOT be awarded when wine inventory is empty";
    }

    /**
     * Test: Streak resets when food inventory is empty
     */
    private static void testNoEmptyShelvesStreakResetsOnFoodStockout() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        state.kitchenUnlocked = true;
        Food food = new Food("Test Meal", 2.0, 5.0, 1, FoodCategory.CHEAP_BAR_FOOD, 1.0, 3);
        
        // Night 1: Both inventories OK
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        for (int i = 0; i < 50; i++) {
            state.foodRack.addMeal(food, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 1");
        
        assert state.noStockoutStreakNights == 1 : "Streak should be 1 after night 1";
        
        // Night 2: Wine OK, but food empty
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        // Empty food rack by removing all meals
        while (!state.foodRack.isEmpty()) {
            state.foodRack.removeFood(food);
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 2 - food empty");
        
        assert state.noStockoutStreakNights == 0 : "Streak should reset when food inventory empty";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M2_NO_EMPTY_SHELVES)
            : "M2_NO_EMPTY_SHELVES should NOT be awarded when food inventory is empty";
    }

    /**
     * Test: Open for Business requires 5 nights without bankruptcy
     */
    private static void testOpenForBusinessRequiresFiveNights() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        
        // Run 5 nights
        for (int night = 0; night < 5; night++) {
            for (int i = 0; i < 100; i++) {
                state.rack.addBottle(wine, state.absDayIndex());
            }
            
            sim.openNight();
            for (int round = 0; round < 20; round++) {
                sim.playRound();
            }
            sim.closeNight("Night " + (night + 1));
            
            assert state.openForBusinessNights == (night + 1) : "Counter should be " + (night + 1) + " after night " + (night + 1);
            
            // Should not award before 5 nights
            if (night < 4) {
                assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)
                    : "M1_OPEN_FOR_BUSINESS should NOT be awarded before 5 nights (night " + (night + 1) + ")";
            }
        }
        
        // After 5 nights, should be awarded
        assert state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)
            : "M1_OPEN_FOR_BUSINESS should be awarded after 5 nights";
    }

    /**
     * Test: Open 4 nights, then bankruptcy → milestone not awarded
     */
    private static void testOpenForBusinessResetsOnBankruptcy() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        
        // Run 4 nights successfully
        for (int night = 0; night < 4; night++) {
            for (int i = 0; i < 100; i++) {
                state.rack.addBottle(wine, state.absDayIndex());
            }
            
            sim.openNight();
            for (int round = 0; round < 20; round++) {
                sim.playRound();
            }
            sim.closeNight("Night " + (night + 1));
        }
        
        assert state.openForBusinessNights == 4 : "Counter should be 4 after 4 nights";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)
            : "M1_OPEN_FOR_BUSINESS should NOT be awarded after only 4 nights";
        
        // Simulate bankruptcy before night 5
        state.bankruptcyDeclared = true;
        
        // Run night 5
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(wine, state.absDayIndex());
        }
        
        sim.openNight();
        for (int round = 0; round < 20; round++) {
            sim.playRound();
        }
        sim.closeNight("Night 5 with bankruptcy");
        
        // Counter should reset
        assert state.openForBusinessNights == 0 : "Counter should reset to 0 on bankruptcy, was " + state.openForBusinessNights;
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)
            : "M1_OPEN_FOR_BUSINESS should NOT be awarded after bankruptcy";
    }

    /**
     * Test: Four nights should not award milestone
     */
    private static void testOpenForBusinessDoesNotAwardAfterFourNights() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        Wine wine = state.supplier.get(0);
        
        // Run 4 nights
        for (int night = 0; night < 4; night++) {
            for (int i = 0; i < 100; i++) {
                state.rack.addBottle(wine, state.absDayIndex());
            }
            
            sim.openNight();
            for (int round = 0; round < 20; round++) {
                sim.playRound();
            }
            sim.closeNight("Night " + (night + 1));
        }
        
        assert state.openForBusinessNights == 4 : "Counter should be 4 after 4 nights";
        assert !state.achievedMilestones.contains(MilestoneSystem.Milestone.M1_OPEN_FOR_BUSINESS)
            : "M1_OPEN_FOR_BUSINESS should NOT be awarded after only 4 nights";
    }
}
