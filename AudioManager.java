import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class AudioManager {
    private static final Path MUSIC_ROOT = Paths.get("MusicProfiles");
    private static final Path INDIE_ALT_ROOT = MUSIC_ROOT.resolve("INDIE_ALT");
    private static final Path AMBIENCE_ROOT = MUSIC_ROOT.resolve("Ambience");
    private static final long CHATTER_SWAP_COOLDOWN_MS = 2000L;

    enum ChatterBand { LOW, MED, HIGH }

    private Clip musicClip;
    private Clip chatterClip;

    private String currentMusicKey;
    private String currentMusicFileName = "None";
    private boolean pubOpen;
    private ChatterBand currentChatterBand;
    private String currentChatterFileName = "None";
    private long lastChatterSwapAtMs;

    private String indieAltChosenFileThisNight;
    private String highBandChosenFileThisNight;

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
        stopAndClose(chatterClip);
        chatterClip = null;
        currentChatterFileName = "None";
        pubOpen = false;
    }

    public synchronized void setPubOpen(boolean isOpen) {
        pubOpen = isOpen;
        if (!isOpen) {
            currentChatterBand = null;
            highBandChosenFileThisNight = null;
            stopAndClose(chatterClip);
            chatterClip = null;
            currentChatterFileName = "None";
        }
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
        if (swapChatter(target)) {
            currentChatterBand = targetBand;
            lastChatterSwapAtMs = now;
        }
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
        playLoop(musicClip);
        return true;
    }

    private boolean swapChatter(Path path) {
        Clip next = loadClip(path);
        if (next == null) return false;
        stopAndClose(chatterClip);
        chatterClip = next;
        currentChatterFileName = path.getFileName().toString();
        playLoop(chatterClip);
        return true;
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
        clip.stop();
        clip.flush();
        clip.close();
    }

    private void warn(String message) {
        System.err.println("[AudioManager] " + message);
    }
}
