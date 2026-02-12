import javax.swing.JTextPane;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test to verify that covers (servedPuntersThisService) are properly saved and loaded.
 */
public class SaveLoadCoversTest {
    public static void main(String[] args) throws Exception {
        Path tmpHome = Files.createTempDirectory("javabarsim-covers-save-test-");
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tmpHome.toString());
            testCoversSaveLoad();
            System.out.println("✓ SaveLoadCoversTest passed");
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            }
        }
    }

    private static void testCoversSaveLoad() throws Exception {
        // Create a new game
        GameState state = GameFactory.newGame();
        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));
        
        // Setup initial stock
        for (int i = 0; i < 50; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        // Open night and serve some punters
        sim.openNight();
        
        // Create 7 unique punters and serve them
        List<Punter> punters = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Punter p = Punter.randomPunter(state.nextPunterId++, state.random, Punter.Tier.DECENT);
            punters.add(p);
            state.nightPunters.add(p);
        }
        
        // Serve each punter
        UILogger log = new UILogger(new JTextPane());
        EconomySystem eco = new EconomySystem(state, log);
        PunterSystem punterSys = new PunterSystem(
            state,
            eco,
            new InventorySystem(state),
            new EventSystem(state, eco, log),
            new RumorSystem(state, log),
            log
        );
        
        for (Punter p : punters) {
            punterSys.handlePunter(p, 1.0, 0, false, 0.1);
        }
        
        // Verify covers before save
        int coversBefore = state.servedPuntersThisService.size();
        assert coversBefore == 7 : "Expected 7 covers before save, got: " + coversBefore;
        
        // Save the game
        SaveManager.save(state);
        
        // Load the game
        GameState loaded = SaveManager.load();
        
        // Verify covers after load
        int coversAfter = loaded.servedPuntersThisService.size();
        assert coversAfter == 7 : "Expected 7 covers after load, got: " + coversAfter;
        
        // Verify that the specific punter IDs are preserved
        for (Punter p : punters) {
            assert loaded.servedPuntersThisService.contains(p.getId()) : 
                "Punter ID " + p.getId() + " should be in loaded covers set";
        }
        
        System.out.println("  Covers before save: " + coversBefore);
        System.out.println("  Covers after load: " + coversAfter);
        System.out.println("  All punter IDs preserved: YES");
    }
}
