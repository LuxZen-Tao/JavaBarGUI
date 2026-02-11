import java.nio.file.Files;
import java.nio.file.Path;

public class SaveLoadReliabilityTests {
    public static void main(String[] args) throws Exception {
        Path tmpHome = Files.createTempDirectory("javabarsim-save-tests-");
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tmpHome.toString());
            runNoSaveLoadGuard();
            runRepeatedSaveLoadCycles();
            runNewGameFreshnessChecks();
            System.out.println("All SaveLoadReliabilityTests passed.");
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            }
        }
    }

    private static void runNoSaveLoadGuard() {
        assert !SaveManager.hasSave() : "Expected no save at start of test home.";
    }

    private static void runRepeatedSaveLoadCycles() throws Exception {
        for (int i = 1; i <= 25; i++) {
            GameState state = GameFactory.newGame();
            state.pubName = "Cycle-" + i;
            state.cash = 100.0 + i;
            state.reputation = 10 + i;
            state.weekCount = i;
            state.dayIndex = i % 7;

            SaveManager.save(state);
            assert SaveManager.hasSave() : "Expected save to exist after save() in cycle " + i;

            GameState loaded = SaveManager.load();
            assert loaded != null : "Loaded state should not be null (cycle " + i + ")";
            assert ("Cycle-" + i).equals(loaded.pubName) : "pubName mismatch in cycle " + i;
            assert Math.abs((100.0 + i) - loaded.cash) < 0.0001 : "cash mismatch in cycle " + i;
            assert loaded.reputation == 10 + i : "reputation mismatch in cycle " + i;
            assert loaded.weekCount == i : "weekCount mismatch in cycle " + i;
            assert loaded.dayIndex == i % 7 : "dayIndex mismatch in cycle " + i;
        }
    }

    private static void runNewGameFreshnessChecks() throws Exception {
        GameState saved = GameFactory.newGame();
        saved.pubName = "Saved-Name";
        saved.cash = 9999.0;
        SaveManager.save(saved);

        GameState fresh = GameFactory.newGame();
        assert !"Saved-Name".equals(fresh.pubName) : "New Game should not accidentally load saved pubName.";
        assert Math.abs(fresh.cash - 100.0) < 0.0001 : "New Game should use default cash, not saved cash.";

        GameState loaded = SaveManager.load();
        assert "Saved-Name".equals(loaded.pubName) : "Load should return the saved game data.";
        assert Math.abs(loaded.cash - 9999.0) < 0.0001 : "Load should return the saved cash.";
    }
}
