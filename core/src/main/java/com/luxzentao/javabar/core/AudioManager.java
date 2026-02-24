package com.luxzentao.javabar.core;

public class AudioManager {
    private int musicVolume = 80;
    private int chatterVolume = 80;
    private String currentMusic = "None";
    private String currentChatter = "None";
    private boolean pubOpen;

    public synchronized void setMusicProfile(String profileName) { if (profileName != null) currentMusic = profileName; }
    public synchronized void onNightEnd() {}
    public synchronized void setPubOpen(boolean isOpen) { pubOpen = isOpen; if (!isOpen) currentChatter = "None"; }
    public synchronized void updateChatterOccupancy(int currentInBar, int barCapacity) { if (pubOpen) currentChatter = "SIMULATED"; }
    public synchronized void shutdown() { pubOpen = false; }
    public synchronized String currentMusicFileName() { return currentMusic; }
    public synchronized String currentChatterBandLabel() { return pubOpen ? "SIMULATED" : "OFF"; }
    public synchronized String currentChatterFileName() { return currentChatter; }
    public synchronized int getMusicVolume() { return musicVolume; }
    public synchronized void setMusicVolume(int volume) { musicVolume = Math.max(0, Math.min(100, volume)); }
    public synchronized int getChatterVolume() { return chatterVolume; }
    public synchronized void setChatterVolume(int volume) { chatterVolume = Math.max(0, Math.min(100, volume)); }
}
