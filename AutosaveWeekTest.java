import javax.swing.JTextPane;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test to verify that the week-start autosave hook works correctly.
 */
public class AutosaveWeekTest {
    private static boolean autosaveCalled = false;
    private static int weekReceived = -1;
    
    public static void main(String[] args) throws Exception {
        Path tmpHome = Files.createTempDirectory("javabarsim-autosave-test-");
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tmpHome.toString());
            testWeekStartHook();
            System.out.println("✓ AutosaveWeekTest passed");
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            }
        }
    }

    private static void testWeekStartHook() throws Exception {
        // Create a new game
        GameState state = GameFactory.newGame();
        UILogger log = new UILogger(new JTextPane());
        Simulation sim = new Simulation(state, log);
        
        // Set up the week start hook to track calls
        sim.setWeekStartHook(week -> {
            autosaveCalled = true;
            weekReceived = week;
            System.out.println("  Week start hook called with week: " + week);
        });
        
        // Setup initial stock
        for (int i = 0; i < 50; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        int initialWeek = state.weekCount;
        System.out.println("  Initial week: " + initialWeek);
        System.out.println("  Initial dayIndex: " + state.dayIndex);
        
        // Play through a full week (7 days)
        for (int day = 0; day < 7; day++) {
            System.out.println("  Day " + day + " (dayIndex=" + state.dayIndex + ", weekCount=" + state.weekCount + ")");
            sim.openNight();
            sim.closeNight("Closing time.");
            
            if (autosaveCalled) {
                System.out.println("  Autosave triggered after day " + day);
                break;
            }
        }
        
        // Verify the hook was called at the start of the new week
        assert autosaveCalled : "Week start hook should have been called after 7 days";
        assert weekReceived == initialWeek + 1 : 
            "Week received should be " + (initialWeek + 1) + ", got: " + weekReceived;
        assert state.weekCount == initialWeek + 1 : 
            "Week count should be " + (initialWeek + 1) + ", got: " + state.weekCount;
        
        System.out.println("  Final week: " + state.weekCount);
        System.out.println("  Hook called with week: " + weekReceived);
    }
}
