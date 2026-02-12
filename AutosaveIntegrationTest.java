import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test to verify that autosave actually saves the game at week start.
 * This mimics what WineBarGUI does with handleFreshWeekAutosave.
 */
public class AutosaveIntegrationTest {
    public static void main(String[] args) throws Exception {
        Path tmpHome = Files.createTempDirectory("javabarsim-autosave-integration-");
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tmpHome.toString());
            testAutosaveIntegration();
            System.out.println("✓ AutosaveIntegrationTest passed");
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            }
        }
    }

    private static void testAutosaveIntegration() throws Exception {
        // Verify no save exists initially
        assert !SaveManager.hasSave() : "No save should exist initially";
        
        // Create a new game
        GameState state = GameFactory.newGame();
        state.pubName = "TestPub-Autosave";
        state.cash = 500.0;
        
        // Setup a simple week-start hook that saves (mimicking WineBarGUI.handleFreshWeekAutosave)
        int[] lastAutosavedWeek = {-1};
        java.util.function.IntConsumer autosaveHook = week -> {
            if (week <= lastAutosavedWeek[0]) return;
            try {
                SaveManager.save(state);
                lastAutosavedWeek[0] = week;
                System.out.println("  Autosaved at week " + week);
            } catch (Exception ex) {
                throw new RuntimeException("Autosave failed", ex);
            }
        };
        
        javax.swing.JTextPane pane = new javax.swing.JTextPane();
        UILogger log = new UILogger(pane);
        Simulation sim = new Simulation(state, log);
        sim.setWeekStartHook(autosaveHook);
        
        // Add stock
        for (int i = 0; i < 50; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        int initialWeek = state.weekCount;
        System.out.println("  Starting at week: " + initialWeek);
        
        // Play through 7 days to trigger week rollover
        for (int day = 0; day < 7; day++) {
            sim.openNight();
            sim.closeNight("Closing time.");
        }
        
        // Verify autosave was triggered
        assert lastAutosavedWeek[0] == initialWeek + 1 : 
            "Autosave should have been triggered for week " + (initialWeek + 1);
        
        // Verify save file exists
        assert SaveManager.hasSave() : "Save file should exist after autosave";
        
        // Load the saved game
        GameState loaded = SaveManager.load();
        assert loaded != null : "Loaded state should not be null";
        assert "TestPub-Autosave".equals(loaded.pubName) : 
            "Loaded pub name should match, got: " + loaded.pubName;
        assert loaded.weekCount == initialWeek + 1 : 
            "Loaded week should be " + (initialWeek + 1) + ", got: " + loaded.weekCount;
        
        System.out.println("  Autosave worked correctly!");
        System.out.println("  Saved and loaded week: " + loaded.weekCount);
        System.out.println("  Saved and loaded pub name: " + loaded.pubName);
    }
}
