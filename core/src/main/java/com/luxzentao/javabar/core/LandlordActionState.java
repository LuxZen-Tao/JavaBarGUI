package com.luxzentao.javabar.core;

public class LandlordActionState  implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int cooldownRemaining;
    private int lastUsedRound;
    private int usesCount;

    public LandlordActionState() {
        this.cooldownRemaining = 0;
        this.lastUsedRound = -999;
        this.usesCount = 0;
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

    public int getUsesCount() {
        return usesCount;
    }

    public void setUsesCount(int usesCount) {
        this.usesCount = usesCount;
    }

    public void incrementUsesCount() {
        this.usesCount++;
    }
}
