public class SupplierDeal  implements java.io.Serializable {

    public enum Type { DISCOUNT, SHORTAGE }

    private final Type type;
    private final Wine target;          // null means "no deal"
    private final double multiplier;    // e.g. 0.50 = 50% off, 1.40 = 40% increase
    private final String label;

    public SupplierDeal(Type type, Wine target, double multiplier, String label) {
        this.type = type;
        this.target = target;
        this.multiplier = multiplier;
        this.label = label;
    }

    public Type getType() { return type; }
    public Wine getTarget() { return target; }
    public double getMultiplier() { return multiplier; }
    public String getLabel() { return label; }

    public boolean appliesTo(Wine w) {
        return target != null && target.getName().equals(w.getName());
    }

    public double applyToCost(Wine w, double baseCost) {
        if (!appliesTo(w)) return baseCost;
        return baseCost * multiplier;
    }

    public static SupplierDeal none() {
        return new SupplierDeal(Type.DISCOUNT, null, 1.0, "No supplier deal today.");
    }
}
