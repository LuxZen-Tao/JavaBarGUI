public class test_calc {
    public static void main(String[] args) {
        // Very Poor (400): trustMult=1.5, otherMult=1.10
        // Neutral (600): trustMult=1.1, otherMult=1.0
        // Great (800): trustMult=0.7, otherMult=0.97
        
        double base = 2.0;
        
        double veryPoor = base * 1.5 * 1.10;
        double neutral = base * 1.1 * 1.0;
        double great = base * 0.7 * 0.97;
        
        System.out.println("Very Poor: " + veryPoor);
        System.out.println("Neutral: " + neutral);
        System.out.println("Great: " + great);
        System.out.println("Ratio Very Poor / Neutral: " + (veryPoor / neutral));
        System.out.println("Ratio Great / Neutral: " + (great / neutral));
        System.out.println("Expected ratio VP/N: " + (1.5 * 1.10) / (1.1 * 1.0));
        System.out.println("Expected ratio G/N: " + (0.7 * 0.97) / (1.1 * 1.0));
    }
}
