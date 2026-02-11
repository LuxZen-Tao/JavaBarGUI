import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AudioManager {
    private static final Path MUSIC_ROOT = Paths.get("MusicProfiles");
    private static final Path INDIE_ALT_ROOT = MUSIC_ROOT.resolve("INDIE_ALT");
    private static final Path AMBIENCE_ROOT = MUSIC_ROOT.resolve("Ambience");
    private static final long CHATTER_SWAP_COOLDOWN_MS = 2000L;

    enum ChatterBand { LOW, MED, HIGH }

    private Clip musicClip;
    private Clip chatterClip;
    private final Map<Clip, AudioInputStream> openStreams = new HashMap<>();

    private String currentMusicKey;
    private String currentMusicFileName = "None";
    private boolean pubOpen;
    private ChatterBand currentChatterBand;
    private String currentChatterFileName = "None";
    private long lastChatterSwapAtMs;

    private int musicVolume = 80;
    private int chatterVolume = 80;
    private int chatterEffectiveVolume = 0;

    private String indieAltChosenFileThisNight;
    private String highBandChosenFileThisNight;
    private Timer chatterFadeTimer;

    public synchronized void setMusicProfile(String profileName) {
        if (profileName == null || profileName.isBlank()) return;
        String normalized = profileName.trim().toUpperCase(Locale.ROOT);
        Path target = resolveMusicPath(normalized);
        if (target == null) {
            warn("Music profile not found for: " + profileName);
            return;
        }
        String key = target.toAbsolutePath().normalize().toString();
        if (key.equals(currentMusicKey) && musicClip != null && musicClip.isOpen()) return;
        swapMusic(target);
    }

    public synchronized void onNightEnd() {
        indieAltChosenFileThisNight = null;
        highBandChosenFileThisNight = null;
        currentChatterBand = null;
    }

    public synchronized void setPubOpen(boolean isOpen) {
        pubOpen = isOpen;
        if (isOpen) {
            startChatterFadeIn(1600);
            return;
        }
        currentChatterBand = null;
        highBandChosenFileThisNight = null;
        startChatterFadeOut(2200);
    }

    public synchronized void updateChatterOccupancy(int currentInBar, int barCapacity) {
        if (!pubOpen) return;
        double denom = Math.max(1.0, barCapacity);
        double occupancy = Math.max(0.0, Math.min(1.0, currentInBar / denom));
        ChatterBand targetBand = occupancy < 0.30 ? ChatterBand.LOW : (occupancy < 0.75 ? ChatterBand.MED : ChatterBand.HIGH);
        long now = System.currentTimeMillis();
        if (targetBand == currentChatterBand) return;
        if ((now - lastChatterSwapAtMs) < CHATTER_SWAP_COOLDOWN_MS) return;

        Path target = resolveChatterPath(targetBand);
        if (target == null) {
            warn("Chatter file not found for band: " + targetBand);
            return;
        }
        swapChatter(target);
        currentChatterBand = targetBand;
        lastChatterSwapAtMs = now;
    }

    public synchronized void fadeChatterTo(int targetVolume, int durationMs) {
        int clampedTarget = clampVolume(targetVolume);
        int startVolume = chatterEffectiveVolume;
        int safeDuration = Math.max(1, durationMs);
        if (startVolume == clampedTarget || chatterClip == null) {
            chatterEffectiveVolume = clampedTarget;
            applyVolume(chatterClip, chatterEffectiveVolume);
            if (chatterEffectiveVolume <= 0 && !pubOpen) {
                stopAndClose(chatterClip);
                chatterClip = null;
                currentChatterFileName = "None";
            }
            return;
        }

        stopChatterFadeTimer();
        runOnEdt(() -> {
            stopChatterFadeTimer();
            final int steps = Math.max(1, safeDuration / 50);
            final double delta = (clampedTarget - startVolume) / (double) steps;
            final int[] tick = {0};
            chatterFadeTimer = new Timer(50, e -> {
                synchronized (AudioManager.this) {
                    tick[0]++;
                    boolean done = tick[0] >= steps;
                    int next = done ? clampedTarget : (int) Math.round(startVolume + (delta * tick[0]));
                    chatterEffectiveVolume = clampVolume(next);
                    applyVolume(chatterClip, chatterEffectiveVolume);
                    if (done) {
                        stopChatterFadeTimer();
                        if (chatterEffectiveVolume <= 0 && !pubOpen) {
                            stopAndClose(chatterClip);
                            chatterClip = null;
                            currentChatterFileName = "None";
                        }
                    }
                }
            });
            chatterFadeTimer.setCoalesce(true);
            chatterFadeTimer.start();
        });
    }

    public synchronized void startChatterFadeIn(int durationMs) {
        if (chatterClip == null) {
            Path fallback = resolveChatterPath(ChatterBand.LOW);
            if (fallback != null) {
                swapChatter(fallback);
                currentChatterBand = ChatterBand.LOW;
            }
        }
        fadeChatterTo(chatterVolume, durationMs);
    }

    public synchronized void startChatterFadeOut(int durationMs) {
        fadeChatterTo(0, durationMs);
    }

    public synchronized void shutdown() {
        stopChatterFadeTimer();
        stopAndClose(chatterClip);
        chatterClip = null;
        stopAndClose(musicClip);
        musicClip = null;
        pubOpen = false;
        currentMusicKey = null;
        currentMusicFileName = "None";
        currentChatterBand = null;
        currentChatterFileName = "None";
        chatterEffectiveVolume = 0;
        indieAltChosenFileThisNight = null;
        highBandChosenFileThisNight = null;
    }

    public synchronized String currentMusicFileName() {
        return currentMusicFileName;
    }

    public synchronized String currentChatterBandLabel() {
        if (!pubOpen || currentChatterBand == null) return "OFF";
        return currentChatterBand.name();
    }

    public synchronized String currentChatterFileName() {
        return currentChatterFileName;
    }

    private Path resolveMusicPath(String normalizedProfile) {
        if ("INDIE_ALT".equals(normalizedProfile)) {
            if (indieAltChosenFileThisNight == null) {
                indieAltChosenFileThisNight = ThreadLocalRandom.current().nextBoolean() ? "INDIE_ALT1.wav" : "INDIE_ALT2.wav";
            }
            return INDIE_ALT_ROOT.resolve(indieAltChosenFileThisNight);
        }
        return MUSIC_ROOT.resolve(normalizedProfile + ".wav");
    }

    private Path resolveChatterPath(ChatterBand band) {
        return switch (band) {
            case LOW -> AMBIENCE_ROOT.resolve("ChatterLow.wav");
            case MED -> AMBIENCE_ROOT.resolve("ChatterMed.wav");
            case HIGH -> {
                if (highBandChosenFileThisNight == null) {
                    highBandChosenFileThisNight = ThreadLocalRandom.current().nextBoolean() ? "ChatterLarge.wav" : "ChatterLarge2.wav";
                }
                yield AMBIENCE_ROOT.resolve(highBandChosenFileThisNight);
            }
        };
    }

    private boolean swapMusic(Path path) {
        Clip next = loadClip(path);
        if (next == null) return false;
        stopAndClose(musicClip);
        musicClip = next;
        currentMusicKey = path.toAbsolutePath().normalize().toString();
        currentMusicFileName = path.getFileName().toString();
        applyVolume(musicClip, musicVolume);
        playLoop(musicClip);
        return true;
    }

    private void swapChatter(Path path) {
        Clip next = loadClip(path);
        if (next == null) return;
        stopAndClose(chatterClip);
        chatterClip = next;
        currentChatterFileName = path.getFileName().toString();
        applyVolume(chatterClip, chatterEffectiveVolume);
        playLoop(chatterClip);
    }

    private Clip loadClip(Path wavPath) {
        if (wavPath == null || !Files.exists(wavPath)) {
            warn("Missing WAV file: " + wavPath);
            return null;
        }

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(wavPath.toFile())) {
            AudioFormat sourceFormat = sourceStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream)) {
                Clip clip = AudioSystem.getClip();
                clip.open(decodedStream);
                return clip;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException ex) {
            warn("Unable to load WAV " + wavPath + " | " + ex.getMessage());
            return null;
        }
    }

    private void playLoop(Clip clip) {
        if (clip == null) return;
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    private void stopAndClose(Clip clip) {
        if (clip == null) return;
        try {
            clip.stop();
            clip.flush();
            clip.close();
        } finally {
            AudioInputStream stream = openStreams.remove(clip);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    warn("Failed to close audio stream: " + ex.getMessage());
                }
            }
        }
    }


    public synchronized void setMusicVolume(int volume) {
        musicVolume = clampVolume(volume);
        applyVolume(musicClip, musicVolume);
    }

    public synchronized void setChatterVolume(int volume) {
        chatterVolume = clampVolume(volume);
        if (!pubOpen || chatterClip == null) {
            chatterEffectiveVolume = pubOpen ? chatterVolume : 0;
            applyVolume(chatterClip, chatterEffectiveVolume);
            return;
        }
        fadeChatterTo(chatterVolume, 250);
    }

    public synchronized int getMusicVolume() { return musicVolume; }

    public synchronized int getChatterVolume() { return chatterVolume; }

    private int clampVolume(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private void applyVolume(Clip clip, int volume) {
        if (clip == null) return;
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (volume <= 0) {
            gain.setValue(gain.getMinimum());
            return;
        }
        float dB = (float) (20.0 * Math.log10(volume / 100.0));
        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
        gain.setValue(dB);
    }

    private void stopChatterFadeTimer() {
        if (chatterFadeTimer != null) {
            chatterFadeTimer.stop();
            chatterFadeTimer = null;
        }
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }
        SwingUtilities.invokeLater(action);
    }

    private void warn(String message) {
        System.err.println("[AudioManager] " + message);
    }
}
