import java.io.Serializable;

public class LandlordPromptEffectPackage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int cashDelta;
    private final int reputationDelta;
    private final double chaosDelta;
    private final int moraleDelta;
    private final int serviceEfficiencyDelta; // Temporary efficiency for this shift
    private final double supplierTrustDelta;

    public LandlordPromptEffectPackage(int cashDelta, int reputationDelta, double chaosDelta,
                                       int moraleDelta, int serviceEfficiencyDelta, double supplierTrustDelta) {
        this.cashDelta = cashDelta;
        this.reputationDelta = reputationDelta;
        this.chaosDelta = chaosDelta;
        this.moraleDelta = moraleDelta;
        this.serviceEfficiencyDelta = serviceEfficiencyDelta;
        this.supplierTrustDelta = supplierTrustDelta;
    }

    public int getCashDelta() { return cashDelta; }
    public int getReputationDelta() { return reputationDelta; }
    public double getChaosDelta() { return chaosDelta; }
    public int getMoraleDelta() { return moraleDelta; }
    public int getServiceEfficiencyDelta() { return serviceEfficiencyDelta; }
    public double getSupplierTrustDelta() { return supplierTrustDelta; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int cashDelta = 0;
        private int reputationDelta = 0;
        private double chaosDelta = 0.0;
        private int moraleDelta = 0;
        private int serviceEfficiencyDelta = 0;
        private double supplierTrustDelta = 0.0;

        public Builder cash(int delta) {
            this.cashDelta = delta;
            return this;
        }

        public Builder reputation(int delta) {
            this.reputationDelta = delta;
            return this;
        }

        public Builder chaos(double delta) {
            this.chaosDelta = delta;
            return this;
        }

        public Builder morale(int delta) {
            this.moraleDelta = delta;
            return this;
        }

        public Builder serviceEfficiency(int delta) {
            this.serviceEfficiencyDelta = delta;
            return this;
        }

        public Builder supplierTrust(double delta) {
            this.supplierTrustDelta = delta;
            return this;
        }

        public LandlordPromptEffectPackage build() {
            return new LandlordPromptEffectPackage(cashDelta, reputationDelta, chaosDelta,
                    moraleDelta, serviceEfficiencyDelta, supplierTrustDelta);
        }
    }
}
