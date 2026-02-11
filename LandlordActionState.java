public class LandlordActionState  implements java.io.Serializable {
    private int cooldownRemaining;
    private int lastUsedRound;

    public LandlordActionState() {
        this.cooldownRemaining = 0;
        this.lastUsedRound = -999;
    }

    public int getCooldownRemaining() {
        return cooldownRemaining;
    }

    public void setCooldownRemaining(int cooldownRemaining) {
        this.cooldownRemaining = Math.max(0, cooldownRemaining);
    }

    public int getLastUsedRound() {
        return lastUsedRound;
    }

    public void setLastUsedRound(int lastUsedRound) {
        this.lastUsedRound = lastUsedRound;
    }
}
