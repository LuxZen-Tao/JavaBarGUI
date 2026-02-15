import javax.swing.JTextPane;

/**
 * Test: Security_CashOnly
 * Validates that security purchases (base security upgrades and bouncers) are cash-only.
 * Credit is not accepted for security purchases even if available.
 */
public class Security_CashOnly {
    public static void main(String[] args) {
        testBaseSecurityUpgradeRequiresCash();
        testBaseSecurityUpgradeRejectsCreditFallback();
        testBouncerHireRequiresCash();
        testBouncerHireRejectsCreditFallback();
        System.out.println("All Security_CashOnly tests passed.");
        System.exit(0);
    }

    private static Simulation newSimulation(GameState state) {
        return new Simulation(state, new UILogger(new JTextPane()));
    }

    /**
     * Test that base security upgrade succeeds when cash is sufficient.
     */
    private static void testBaseSecurityUpgradeRequiresCash() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        double cost = sim.peekSecurityUpgradeCost();
        s.cash = cost + 10.0; // Sufficient cash
        int initialLevel = s.baseSecurityLevel;
        
        sim.upgradeSecurity();
        
        assert s.baseSecurityLevel == initialLevel + 1 : "Security upgrade should succeed with sufficient cash.";
        assert s.cash < cost + 10.0 : "Cash should be deducted.";
    }

    /**
     * Test that base security upgrade fails when cash is insufficient,
     * even if credit is available.
     */
    private static void testBaseSecurityUpgradeRejectsCreditFallback() {
        GameState s = GameFactory.newGame();
        Simulation sim = newSimulation(s);
        
        // Set up: insufficient cash but available credit
        double cost = sim.peekSecurityUpgradeCost();
        s.cash = cost - 10.0; // Insufficient cash
        
        // Add a credit line with sufficient credit by opening a credit line
        sim.openCreditLine(Bank.TOWNLAND);
        
        assert s.creditLines.hasAvailableCredit(10.0) : "Credit should be available.";
        
        int initialLevel = s.baseSecurityLevel;
        double initialCash = s.cash;
        double initialCredit = s.creditLines.totalBalance();
        
        sim.upgradeSecurity();
        
        assert s.baseSecurityLevel == initialLevel : "Security upgrade should fail when cash is insufficient.";
        assert s.cash == initialCash : "Cash should not be touched.";
        assert s.creditLines.totalBalance() == initialCredit : "Credit should not be used.";
    }

    /**
     * Test that bouncer hire succeeds when cash is sufficient.
     */
    private static void testBouncerHireRequiresCash() {
        GameState s = GameFactory.newGame();
        s.nightOpen = true;
        s.cash = 200.0; // Sufficient cash
        Simulation sim = newSimulation(s);
        
        int initialBouncers = s.bouncersHiredTonight;
        double initialCash = s.cash;
        
        sim.hireBouncerTonight();
        
        assert s.bouncersHiredTonight == initialBouncers + 1 : "Bouncer should be hired with sufficient cash.";
        assert s.cash < initialCash : "Cash should be deducted.";
    }

    /**
     * Test that bouncer hire fails when cash is insufficient,
     * even if credit is available.
     */
    private static void testBouncerHireRejectsCreditFallback() {
        GameState s = GameFactory.newGame();
        s.nightOpen = true;
        s.cash = 5.0; // Insufficient cash for bouncer
        Simulation sim = newSimulation(s);
        
        // Add a credit line with sufficient credit
        sim.openCreditLine(Bank.TOWNLAND);
        
        assert s.creditLines.hasAvailableCredit(100.0) : "Credit should be available.";
        
        int initialBouncers = s.bouncersHiredTonight;
        double initialCash = s.cash;
        double initialCredit = s.creditLines.totalBalance();
        
        sim.hireBouncerTonight();
        
        assert s.bouncersHiredTonight == initialBouncers : "Bouncer hire should fail when cash is insufficient.";
        assert s.cash == initialCash : "Cash should not be touched.";
        assert s.creditLines.totalBalance() == initialCredit : "Credit should not be used.";
    }
}
