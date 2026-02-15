import javax.swing.JTextPane;

public class SupplierTrustTests {
    public static void main(String[] args) {
        testTrustLevelsAndMultipliers();
        testPricingReflectsTrustMultiplier();
        testCreditCapByTrustLevel();
        System.out.println("All SupplierTrustTests passed.");
    }

    private static void testTrustLevelsAndMultipliers() {
        // Very Poor (< 450)
        GameState s1 = GameFactory.newGame();
        s1.creditScore = 400;
        assert s1.getSupplierTrustLevel() == SupplierTrustLevel.VERY_POOR : "Score 400 should be Very Poor";
        assert s1.supplierTrustPriceMultiplier() == 1.5 : "Very Poor multiplier should be 1.5";
        assert s1.supplierCreditCap() == 1000.0 : "Very Poor cap should be 1000";

        // Poor (450-549)
        GameState s2 = GameFactory.newGame();
        s2.creditScore = 500;
        assert s2.getSupplierTrustLevel() == SupplierTrustLevel.POOR : "Score 500 should be Poor";
        assert s2.supplierTrustPriceMultiplier() == 1.3 : "Poor multiplier should be 1.3";
        assert s2.supplierCreditCap() == 1500.0 : "Poor cap should be 1500";

        // Neutral (550-699)
        GameState s3 = GameFactory.newGame();
        s3.creditScore = 600;
        assert s3.getSupplierTrustLevel() == SupplierTrustLevel.NEUTRAL : "Score 600 should be Neutral";
        assert s3.supplierTrustPriceMultiplier() == 1.1 : "Neutral multiplier should be 1.1";
        assert s3.supplierCreditCap() == 1800.0 : "Neutral cap should be 1800";

        // Good (700-749)
        GameState s4 = GameFactory.newGame();
        s4.creditScore = 720;
        assert s4.getSupplierTrustLevel() == SupplierTrustLevel.GOOD : "Score 720 should be Good";
        assert s4.supplierTrustPriceMultiplier() == 0.9 : "Good multiplier should be 0.9";
        assert s4.supplierCreditCap() == 2200.0 : "Good cap should be 2200";

        // Great (750+)
        GameState s5 = GameFactory.newGame();
        s5.creditScore = 800;
        assert s5.getSupplierTrustLevel() == SupplierTrustLevel.GREAT : "Score 800 should be Great";
        assert s5.supplierTrustPriceMultiplier() == 0.7 : "Great multiplier should be 0.7";
        assert s5.supplierCreditCap() == 3000.0 : "Great cap should be 3000";
    }

    private static void testPricingReflectsTrustMultiplier() {
        GameState state = GameFactory.newGame();
        SupplierSystem supplier = new SupplierSystem(state);
        
        // Get the first wine from the existing supplier
        if (state.supplier == null || state.supplier.isEmpty()) {
            System.err.println("Warning: No supplier items available for testing");
            return;
        }
        Wine testWine = state.supplier.get(0);
        double repMult = 1.0;

        // Test Very Poor (x1.5 trust * 1.10 other)
        state.creditScore = 400;
        double costVeryPoor = supplier.supplierBuyCost(testWine, repMult);
        
        // Test Neutral (x1.1 trust * 1.0 other)
        state.creditScore = 600;
        double costNeutral = supplier.supplierBuyCost(testWine, repMult);
        
        // Test Great (x0.7 trust * 0.97 other)
        state.creditScore = 800;
        double costGreat = supplier.supplierBuyCost(testWine, repMult);

        // Verify relative pricing
        assert costVeryPoor > costNeutral : "Very Poor pricing should be higher than Neutral";
        assert costNeutral > costGreat : "Neutral pricing should be higher than Great";
        
        // Verify combined ratios (trust multiplier + supplierPriceMultiplier)
        // Very Poor: 1.5 * 1.10 = 1.65, Neutral: 1.1 * 1.0 = 1.1, Great: 0.7 * 0.97 = 0.679
        double ratio1 = costVeryPoor / costNeutral;
        double expectedRatio1 = (1.5 * 1.10) / (1.1 * 1.0); // 1.5
        assert Math.abs(ratio1 - expectedRatio1) < 0.01 : "Very Poor to Neutral ratio should be ~" + expectedRatio1 + ", got " + ratio1;
        
        double ratio2 = costGreat / costNeutral;
        double expectedRatio2 = (0.7 * 0.97) / (1.1 * 1.0); // ~0.617
        assert Math.abs(ratio2 - expectedRatio2) < 0.01 : "Great to Neutral ratio should be ~" + expectedRatio2 + ", got " + ratio2;
    }

    private static void testCreditCapByTrustLevel() {
        GameState state = GameFactory.newGame();

        // Test each trust level's cap (with no penalties)
        state.creditScore = 400; // Very Poor
        state.supplierTrustPenalty = 0.0;
        assert state.supplierCreditCap() == 1000.0 : "Very Poor cap should be 1000";

        state.creditScore = 500; // Poor
        assert state.supplierCreditCap() == 1500.0 : "Poor cap should be 1500";

        state.creditScore = 600; // Neutral
        assert state.supplierCreditCap() == 1800.0 : "Neutral cap should be 1800";

        state.creditScore = 720; // Good
        assert state.supplierCreditCap() == 2200.0 : "Good cap should be 2200";

        state.creditScore = 800; // Great
        assert state.supplierCreditCap() == 3000.0 : "Great cap should be 3000";

        // Test that trust penalty affects cap
        state.supplierTrustPenalty = 0.10; // 10% penalty
        double expectedCap = 3000.0 * Math.max(0.6, 1.0 - (0.10 * 3.0)); // 3000 * 0.7 = 2100
        assert Math.abs(state.supplierCreditCap() - expectedCap) < 1.0 
            : "Cap with penalty should be reduced to " + expectedCap + ", got " + state.supplierCreditCap();

        // Test that cap override still works
        state.supplierTrustPenalty = 0.0;
        state.supplierCreditCapOverride = 500.0;
        assert state.supplierCreditCap() == 500.0 : "Override should take precedence";
    }
}
