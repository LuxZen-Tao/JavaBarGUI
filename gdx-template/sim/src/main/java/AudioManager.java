public class AudioManager {
    enum ChatterBand { LOW, MED, HIGH }

    private String currentMusicProfile = "None";
    private int musicVolume = 80;
    private int chatterVolume = 80;
    private boolean pubOpen;

    public synchronized void setMusicProfile(String profileName) {
        if (profileName != null && !profileName.isBlank()) {
            this.currentMusicProfile = profileName;
        }
    }

    public synchronized void onNightEnd() {}

    public synchronized void setPubOpen(boolean isOpen) {
        this.pubOpen = isOpen;
    }

    public synchronized void updateChatterOccupancy(int currentInBar, int barCapacity) {}

    public synchronized void fadeChatterTo(int targetVolume, int durationMs) {}

    public synchronized void setMusicVolume(int volume) {
        this.musicVolume = clamp(volume);
    }

    public synchronized void setChatterVolume(int volume) {
        this.chatterVolume = clamp(volume);
    }

    public synchronized int getMusicVolume() {
        return musicVolume;
    }

    public synchronized int getChatterVolume() {
        return chatterVolume;
    }

    public synchronized void shutdown() {}

    public synchronized String currentMusicFileName() {
        return currentMusicProfile;
    }

    public synchronized String currentChatterBandLabel() {
        return pubOpen ? "ACTIVE" : "IDLE";
    }

    public synchronized String currentChatterFileName() {
        return "chatter_stub";
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
