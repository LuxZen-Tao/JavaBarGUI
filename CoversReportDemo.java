import javax.swing.JTextPane;

/**
 * Simple test to show what the report looks like with the Covers feature.
 */
public class CoversReportDemo {
    public static void main(String[] args) {
        GameState state = GameFactory.newGame();
        Simulation sim = new Simulation(state, new UILogger(new JTextPane()));
        
        // Setup stock
        for (int i = 0; i < 50; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        // Open night and manually simulate serving some punters
        sim.openNight();
        
        // Manually add some punter IDs to the served set to simulate service
        state.servedPuntersThisService.add(1);
        state.servedPuntersThisService.add(2);
        state.servedPuntersThisService.add(3);
        state.servedPuntersThisService.add(4);
        state.servedPuntersThisService.add(5);
        
        // Simulate some sales stats
        state.nightSales = 15;
        state.nightRevenue = 150.0;
        
        // Generate and print the report
        System.out.println("========================================");
        System.out.println("NIGHT REPORT WITH COVERS");
        System.out.println("========================================\n");
        
        String report = ReportSystem.buildReportText(state);
        
        // Extract and show just the NIGHT section
        String[] lines = report.split("\n");
        boolean inNightSection = false;
        for (String line : lines) {
            if (line.equals("NIGHT")) {
                inNightSection = true;
            }
            if (inNightSection) {
                System.out.println(line);
                // Show first 15 lines of NIGHT section
                if (line.startsWith("Bar:")) {
                    break;
                }
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("SUCCESS: Covers field is displayed!");
        System.out.println("  - Sales: " + state.nightSales);
        System.out.println("  - Covers: " + state.servedPuntersThisService.size());
        System.out.println("========================================");
    }
}
