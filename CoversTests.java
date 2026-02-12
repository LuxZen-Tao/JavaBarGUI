import javax.swing.JTextPane;
import java.util.ArrayList;
import java.util.List;

public class CoversTests {
    public static void main(String[] args) {
        testCoversBasic();
        testCoversRepeatOrders();
        testCoversPuntersLeaveAndReturn();
        testCoversResetOnNewService();
        System.out.println("All CoversTests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Acceptance test: Serve punters 1..5 => covers 5
     */
    private static void testCoversBasic() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup initial stock
        for (int i = 0; i < 50; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        sim.openNight();
        assert state.servedPuntersThisService.size() == 0 : "Covers should be 0 at start";
        
        // Create 5 unique punters and serve them
        List<Punter> punters = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Punter p = Punter.randomPunter(state.nextPunterId++, state.random, Punter.Tier.DECENT);
            punters.add(p);
            state.nightPunters.add(p);
        }
        
        // Simulate serving each punter once
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
        
        assert state.servedPuntersThisService.size() == 5 : 
            "Covers should be 5 after serving 5 unique punters, got: " + state.servedPuntersThisService.size();
        
        System.out.println("✓ testCoversBasic passed");
    }

    /**
     * Acceptance test: Punter 1,4,6,7,8 order again => covers remains 8
     */
    private static void testCoversRepeatOrders() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup initial stock
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        sim.openNight();
        
        // Create punters and serve them
        List<Punter> punters = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Punter p = Punter.randomPunter(state.nextPunterId++, state.random, Punter.Tier.DECENT);
            punters.add(p);
            state.nightPunters.add(p);
        }
        
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
        
        // Serve all 8 punters
        for (Punter p : punters) {
            punterSys.handlePunter(p, 1.0, 0, false, 0.1);
        }
        
        assert state.servedPuntersThisService.size() == 8 : "Covers should be 8 after first round";
        
        // Serve punters 0, 3, 5, 6, 7 again (indices 0, 3, 5, 6, 7)
        for (int idx : new int[]{0, 3, 5, 6, 7}) {
            punterSys.handlePunter(punters.get(idx), 1.0, 0, false, 0.1);
        }
        
        assert state.servedPuntersThisService.size() == 8 : 
            "Covers should remain 8 after repeat orders, got: " + state.servedPuntersThisService.size();
        
        System.out.println("✓ testCoversRepeatOrders passed");
    }

    /**
     * Test: Punter leaving and returning during the same service should still count as 1 cover
     */
    private static void testCoversPuntersLeaveAndReturn() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup initial stock
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        sim.openNight();
        
        // Create and serve a punter
        Punter p = Punter.randomPunter(state.nextPunterId++, state.random, Punter.Tier.DECENT);
        state.nightPunters.add(p);
        int punterId = p.getId();
        
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
        
        // First service
        punterSys.handlePunter(p, 1.0, 0, false, 0.1);
        assert state.servedPuntersThisService.size() == 1 : "Covers should be 1 after first service";
        assert state.servedPuntersThisService.contains(punterId) : "Set should contain the punter ID";
        
        // Punter leaves
        p.leaveBar();
        state.nightPunters.remove(p);
        
        // Punter returns (same ID, simulating the same person returning)
        Punter returningPunter = Punter.randomPunter(punterId, state.random, Punter.Tier.DECENT);
        state.nightPunters.add(returningPunter);
        
        // Serve again
        punterSys.handlePunter(returningPunter, 1.0, 0, false, 0.1);
        
        assert state.servedPuntersThisService.size() == 1 : 
            "Covers should remain 1 when same punter returns, got: " + state.servedPuntersThisService.size();
        
        System.out.println("✓ testCoversPuntersLeaveAndReturn passed");
    }

    /**
     * Test: Covers reset to 0 at the start of each new service
     */
    private static void testCoversResetOnNewService() {
        GameState state = GameFactory.newGame();
        Simulation sim = newSimulation(state);
        
        // Setup initial stock
        for (int i = 0; i < 100; i++) {
            state.rack.addBottle(state.supplier.get(0), state.absDayIndex());
        }
        
        sim.openNight();
        
        // Create and serve punters
        List<Punter> punters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Punter p = Punter.randomPunter(state.nextPunterId++, state.random, Punter.Tier.DECENT);
            punters.add(p);
            state.nightPunters.add(p);
        }
        
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
        
        assert state.servedPuntersThisService.size() == 3 : "Covers should be 3 after first night";
        
        // Close night and open new night
        sim.closeNight("Closing time.");
        sim.openNight();
        
        assert state.servedPuntersThisService.size() == 0 : 
            "Covers should reset to 0 at start of new service, got: " + state.servedPuntersThisService.size();
        
        System.out.println("✓ testCoversResetOnNewService passed");
    }
}
