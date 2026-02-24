package com.luxzentao.javabar.core;

public record PendingUpgradeInstall(PubUpgrade upgrade, int nightsRemaining, int totalNights) implements java.io.Serializable {}
