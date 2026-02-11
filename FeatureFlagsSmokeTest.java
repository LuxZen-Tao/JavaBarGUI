import javax.swing.JTextPane;

public class FeatureFlagsSmokeTest {
    public static void main(String[] args) {
        if (FeatureFlags.FEATURE_SEASONS) {
            throw new IllegalStateException("FEATURE_SEASONS must default to false.");
        }
        if (FeatureFlags.FEATURE_RIVALS) {
            throw new IllegalStateException("FEATURE_RIVALS must default to false.");
        }
        if (FeatureFlags.FEATURE_VIPS) {
            throw new IllegalStateException("FEATURE_VIPS must default to false.");
        }

        GameState state = GameFactory.newGame();
        UILogger logger = new UILogger(new JTextPane());
        Simulation simulation = new Simulation(state, logger);

        if (state == null || simulation == null) {
            throw new IllegalStateException("Core simulation failed to initialize.");
        }

        System.out.println("FeatureFlagsSmokeTest passed.");
        System.exit(0);
    }
}
