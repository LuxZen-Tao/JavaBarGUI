import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

public class BootSequencePanel extends JPanel {
    private static final double MIN_WRITE_SECONDS = 1.2;
    private static final double MAX_WRITE_SECONDS = 5.0;
    private static final double CHARACTERS_PER_SECOND = 14.0;

    private static final double GAME_BOOT_HOLD_SECONDS = 2.5;
    private static final double LOGO_FADE_IN_SECONDS = 0.9;
    private static final double LOGO_HOLD_SECONDS = 2.5;
    private static final double LOGO_FADE_OUT_SECONDS = 0.9;
    private static final double COVER_HOLD_SECONDS = 3.0;
    private static final double RANDOM_HOLD_SECONDS = 3.0;
    private static final double COVER_FADE_OUT_SECONDS = 1.0;
    private static final double RANDOM_FADE_OUT_SECONDS = 1.0;

    private static final String COVER_PHRASE = "Brava!! Summer of '93 XOXO";

    private enum Stage {
        GAME_BOOT_HOLD,
        LOGO_FADE_IN, LOGO_HOLD, LOGO_FADE_OUT,
        COVER_WRITE, COVER_HOLD, COVER_FADE_OUT,
        RANDOM_WRITE, RANDOM_HOLD, RANDOM_FADE_OUT,
        DONE
    }

    private static final class BootPhoto {
        final BufferedImage image;
        final String phrase;
        final Font font;
        final double rotationDeg;
        final double anchorXRatio;
        final double anchorYRatio;

        BootPhoto(BufferedImage image,
                  String phrase,
                  Font font,
                  double rotationDeg,
                  double anchorXRatio,
                  double anchorYRatio) {
            this.image = image;
            this.phrase = phrase;
            this.font = font;
            this.rotationDeg = rotationDeg;
            this.anchorXRatio = anchorXRatio;
            this.anchorYRatio = anchorYRatio;
        }
    }

    private static final class DrawRect {
        final int x;
        final int y;
        final int width;
        final int height;

        DrawRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private final Runnable onComplete;
    private final Timer timer;
    private final Random random = new Random();

    private final List<Font> loadedFonts = new ArrayList<>();
    private final List<String> phrases = new ArrayList<>();
    private final List<BootPhoto> randomPhotos = new ArrayList<>();

    private BufferedImage gameBootImage;
    private BufferedImage logoImage;
    private BufferedImage coverImage;
    private BootPhoto coverPhoto;

    private int randomIndex = 0;
    private Stage stage;
    private long stageStartNanos;

    public BootSequencePanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setBackground(Color.BLACK);
        loadAssets();
        chooseFlowStart();
        this.timer = new Timer(16, e -> tick());
        this.timer.setCoalesce(true);
        this.timer.start();
    }

    private void loadAssets() {
        Path root = Paths.get("Art", "BootSequence");
        gameBootImage = loadGameBootImage(root.resolve("Photos"));
        logoImage = readImage(root.resolve(Paths.get("Logos", "StudioLogo.png")));
        coverImage = readImage(root.resolve(Paths.get("Photos", "GameCoverv1.png")));

        loadFonts(root.resolve("Fonts"));
        if (loadedFonts.isEmpty()) {
            loadedFonts.add(new Font(Font.SANS_SERIF, Font.PLAIN, 28));
            System.err.println("[BootSequence] No TTF fonts loaded; using system fallback font.");
        }

        loadPhrases(root.resolve("phrases.txt"));
        if (phrases.isEmpty()) {
            Collections.addAll(phrases,
                    "Night shift, bright lights.",
                    "One more round before sunrise.",
                    "Keep the taps running.");
            System.err.println("[BootSequence] phrases.txt missing/empty; using fallback phrases.");
        }

        if (coverImage != null) {
            coverPhoto = new BootPhoto(coverImage, COVER_PHRASE, pickFontDeterministic(), -18.0, 0.40, 0.88);
        }

        loadRandomPhotos(root.resolve("Photos"));
    }

    private void chooseFlowStart() {
        if (gameBootImage != null) {
            setStage(Stage.GAME_BOOT_HOLD);
        } else if (logoImage != null) {
            setStage(Stage.LOGO_FADE_IN);
        } else if (coverPhoto != null) {
            setStage(Stage.COVER_WRITE);
        } else if (!randomPhotos.isEmpty()) {
            setStage(Stage.RANDOM_WRITE);
        } else {
            setStage(Stage.DONE);
        }
    }

    private void loadFonts(Path fontsDir) {
        if (!Files.isDirectory(fontsDir)) {
            System.err.println("[BootSequence] Fonts directory not found: " + fontsDir);
            return;
        }
        try (Stream<Path> stream = Files.walk(fontsDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ttf"))
                    .forEach(this::loadFontFile);
        } catch (IOException ex) {
            System.err.println("[BootSequence] Failed scanning fonts: " + ex.getMessage());
        }
    }

    private void loadFontFile(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            loadedFonts.add(font);
        } catch (Exception ex) {
            System.err.println("[BootSequence] Failed to load font " + path + ": " + ex.getMessage());
        }
    }

    private void loadPhrases(Path phrasesFile) {
        if (!Files.isRegularFile(phrasesFile)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(phrasesFile)) {
                String trimmed = line == null ? "" : line.trim();
                if (!trimmed.isEmpty()) {
                    phrases.add(trimmed);
                }
            }
        } catch (IOException ex) {
            System.err.println("[BootSequence] Failed reading phrases: " + ex.getMessage());
        }
    }

    private void loadRandomPhotos(Path photosDir) {
        if (!Files.isDirectory(photosDir)) {
            System.err.println("[BootSequence] Photos directory not found: " + photosDir);
            return;
        }

        List<Path> candidates = new ArrayList<>();
        try (Stream<Path> stream = Files.list(photosDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                    .filter(p -> !p.getFileName().toString().equalsIgnoreCase("GameCoverv1.png"))
                    .forEach(candidates::add);
        } catch (IOException ex) {
            System.err.println("[BootSequence] Failed listing photos: " + ex.getMessage());
            return;
        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(candidates, random);
        int count = Math.min(2, candidates.size());
        for (int i = 0; i < count; i++) {
            BufferedImage image = readImage(candidates.get(i));
            if (image == null) {
                continue;
            }
            randomPhotos.add(new BootPhoto(
                    image,
                    phrases.get(random.nextInt(phrases.size())),
                    loadedFonts.get(random.nextInt(loadedFonts.size())),
                    -10.0 + (20.0 * random.nextDouble()),
                    0.18 + (0.64 * random.nextDouble()),
                    0.62 + (0.28 * random.nextDouble())
            ));
        }
    }


    private BufferedImage loadGameBootImage(Path photosDir) {
        if (!Files.isDirectory(photosDir)) {
            return null;
        }

        BufferedImage image = readImage(photosDir.resolve("GameBootScreen"));
        if (image != null) return image;

        String[] names = {"GameBootScreen.png", "GameBootScreen.jpg", "GameBootScreen.jpeg"};
        for (String name : names) {
            image = readImage(photosDir.resolve(name));
            if (image != null) return image;
        }
        return null;
    }

    private BufferedImage readImage(Path path) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException ex) {
            System.err.println("[BootSequence] Failed reading image " + path + ": " + ex.getMessage());
            return null;
        }
    }

    private Font pickFontDeterministic() {
        if (loadedFonts.isEmpty()) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 28);
        }
        return loadedFonts.get(0);
    }

    private void tick() {
        if (stage == Stage.DONE) {
            timer.stop();
            SwingUtilities.invokeLater(onComplete);
            return;
        }

        double elapsedSeconds = elapsedSeconds();
        switch (stage) {
            case GAME_BOOT_HOLD -> {
                if (elapsedSeconds >= GAME_BOOT_HOLD_SECONDS) {
                    if (logoImage != null) {
                        setStage(Stage.LOGO_FADE_IN);
                    } else if (coverPhoto != null) {
                        setStage(Stage.COVER_WRITE);
                    } else if (!randomPhotos.isEmpty()) {
                        setStage(Stage.RANDOM_WRITE);
                    } else {
                        setStage(Stage.DONE);
                    }
                }
            }
            case LOGO_FADE_IN -> {
                if (elapsedSeconds >= LOGO_FADE_IN_SECONDS) setStage(Stage.LOGO_HOLD);
            }
            case LOGO_HOLD -> {
                if (elapsedSeconds >= LOGO_HOLD_SECONDS) setStage(Stage.LOGO_FADE_OUT);
            }
            case LOGO_FADE_OUT -> {
                if (elapsedSeconds >= LOGO_FADE_OUT_SECONDS) {
                    if (coverPhoto != null) {
                        setStage(Stage.COVER_WRITE);
                    } else if (!randomPhotos.isEmpty()) {
                        setStage(Stage.RANDOM_WRITE);
                    } else {
                        setStage(Stage.DONE);
                    }
                }
            }
            case COVER_WRITE -> {
                if (isWritingComplete(coverPhoto.phrase, elapsedSeconds)) {
                    setStage(Stage.COVER_HOLD);
                }
            }
            case COVER_HOLD -> {
                if (elapsedSeconds >= COVER_HOLD_SECONDS) setStage(Stage.COVER_FADE_OUT);
            }
            case COVER_FADE_OUT -> {
                if (elapsedSeconds >= COVER_FADE_OUT_SECONDS) {
                    if (!randomPhotos.isEmpty()) {
                        randomIndex = 0;
                        setStage(Stage.RANDOM_WRITE);
                    } else {
                        setStage(Stage.DONE);
                    }
                }
            }
            case RANDOM_WRITE -> {
                if (randomIndex >= randomPhotos.size()) {
                    setStage(Stage.DONE);
                } else if (isWritingComplete(randomPhotos.get(randomIndex).phrase, elapsedSeconds)) {
                    setStage(Stage.RANDOM_HOLD);
                }
            }
            case RANDOM_HOLD -> {
                if (elapsedSeconds >= RANDOM_HOLD_SECONDS) setStage(Stage.RANDOM_FADE_OUT);
            }
            case RANDOM_FADE_OUT -> {
                if (elapsedSeconds >= RANDOM_FADE_OUT_SECONDS) {
                    randomIndex++;
                    if (randomIndex < randomPhotos.size()) {
                        setStage(Stage.RANDOM_WRITE);
                    } else {
                        setStage(Stage.DONE);
                    }
                }
            }
            default -> {
            }
        }

        repaint();
    }

    private boolean isWritingComplete(String phrase, double elapsedSeconds) {
        if (phrase == null || phrase.isEmpty()) return true;
        int chars = charsToShow(phrase, elapsedSeconds);
        return chars >= phrase.length();
    }

    private int charsToShow(String phrase, double elapsedSeconds) {
        int length = phrase.length();
        if (length == 0) return 0;
        double writeSeconds = writeDurationSeconds(length);
        double fraction = clamp(elapsedSeconds / writeSeconds, 0.0, 1.0);
        return (int) Math.floor(length * fraction);
    }

    private double writeDurationSeconds(int length) {
        double computed = length / CHARACTERS_PER_SECOND;
        return clamp(computed, MIN_WRITE_SECONDS, MAX_WRITE_SECONDS);
    }

    private void setStage(Stage next) {
        this.stage = next;
        this.stageStartNanos = System.nanoTime();
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - stageStartNanos) / 1_000_000_000.0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (stage == Stage.GAME_BOOT_HOLD) {
            drawImageWithFade(g2, gameBootImage, null, 0.0);
        } else if (stage == Stage.LOGO_FADE_IN || stage == Stage.LOGO_HOLD || stage == Stage.LOGO_FADE_OUT) {
            drawImageWithFade(g2, logoImage, null, logoFadeAlpha());
        } else if (stage == Stage.COVER_WRITE || stage == Stage.COVER_HOLD || stage == Stage.COVER_FADE_OUT) {
            drawCover(g2, elapsedSeconds());
        } else if (stage == Stage.RANDOM_WRITE || stage == Stage.RANDOM_HOLD || stage == Stage.RANDOM_FADE_OUT) {
            drawRandom(g2, elapsedSeconds());
        }

        g2.dispose();
    }

    private double logoFadeAlpha() {
        double t = elapsedSeconds();
        if (stage == Stage.LOGO_FADE_IN) {
            return 1.0 - clamp(t / LOGO_FADE_IN_SECONDS, 0.0, 1.0);
        }
        if (stage == Stage.LOGO_FADE_OUT) {
            return clamp(t / LOGO_FADE_OUT_SECONDS, 0.0, 1.0);
        }
        return 0.0;
    }

    private void drawCover(Graphics2D g2, double elapsedSeconds) {
        if (coverPhoto == null) return;
        String phrase = coverPhoto.phrase;
        if (stage == Stage.COVER_WRITE) {
            int chars = charsToShow(phrase, elapsedSeconds);
            phrase = phrase.substring(0, Math.max(0, Math.min(chars, phrase.length())));
        }
        final String text = phrase;
        double fadeAlpha = stage == Stage.COVER_FADE_OUT
                ? clamp(elapsedSeconds / COVER_FADE_OUT_SECONDS, 0.0, 1.0)
                : 0.0;
        drawImageWithFade(g2, coverPhoto.image, (tg, rect) -> {
            int anchorX = rect.x + (int) (rect.width * coverPhoto.anchorXRatio);
            int anchorY = rect.y + (int) (rect.height * coverPhoto.anchorYRatio);
            drawHandwrittenText(tg, rect, text, coverPhoto.font, anchorX, anchorY, coverPhoto.rotationDeg, true);
        }, fadeAlpha);
    }

    private void drawRandom(Graphics2D g2, double elapsedSeconds) {
        if (randomIndex < 0 || randomIndex >= randomPhotos.size()) return;
        BootPhoto photo = randomPhotos.get(randomIndex);
        String phrase = photo.phrase;
        if (stage == Stage.RANDOM_WRITE) {
            int chars = charsToShow(phrase, elapsedSeconds);
            phrase = phrase.substring(0, Math.max(0, Math.min(chars, phrase.length())));
        }
        final String text = phrase;
        double fadeAlpha = stage == Stage.RANDOM_FADE_OUT
                ? clamp(elapsedSeconds / RANDOM_FADE_OUT_SECONDS, 0.0, 1.0)
                : 0.0;

        drawImageWithFade(g2, photo.image, (tg, rect) -> {
            int anchorX = rect.x + (int) (rect.width * photo.anchorXRatio);
            int anchorY = rect.y + (int) (rect.height * photo.anchorYRatio);
            drawHandwrittenText(tg, rect, text, photo.font, anchorX, anchorY, photo.rotationDeg, false);
        }, fadeAlpha);
    }

    private void drawImageWithFade(Graphics2D g2, BufferedImage image, TextDrawer textDrawer, double fadeAlpha) {
        if (image == null) return;
        DrawRect rect = computeFitRect(image.getWidth(), image.getHeight(), getWidth(), getHeight());
        g2.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
        if (textDrawer != null) {
            Graphics2D textGraphics = (Graphics2D) g2.create();
            textDrawer.draw(textGraphics, rect);
            textGraphics.dispose();
        }

        if (fadeAlpha > 0.0) {
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive((float) clamp(fadeAlpha, 0.0, 1.0)));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(old);
        }
    }

    private void drawHandwrittenText(Graphics2D g2,
                                     DrawRect rect,
                                     String phrase,
                                     Font font,
                                     int anchorX,
                                     int anchorY,
                                     double rotationDeg,
                                     boolean coverStyle) {
        if (phrase == null || phrase.isEmpty()) return;

        float size = Math.max(27f, (float) (rect.width * 42.0 / 1080.0));
        Font sized = font.deriveFont(Font.PLAIN, size);
        g2.setFont(sized);
        g2.setColor(Color.BLACK);

        int maxTextWidth = (int) (rect.width * (coverStyle ? 0.72 : 0.85));
        int marginX = Math.max(10, (int) (rect.width * 0.06));

        List<String> lines = wrapText(phrase, sized, g2.getFontRenderContext(), maxTextWidth);
        FontMetrics fm = g2.getFontMetrics(sized);
        int lineHeight = fm.getHeight();

        int lineMaxWidth = 0;
        for (String line : lines) {
            lineMaxWidth = Math.max(lineMaxWidth, fm.stringWidth(line));
        }

        int clampedAnchorX = clampInt(anchorX, rect.x + marginX, rect.x + rect.width - marginX);
        int clampedAnchorY = clampInt(anchorY, rect.y + marginX, rect.y + rect.height - marginX);

        AffineTransform oldTx = g2.getTransform();
        g2.translate(clampedAnchorX, clampedAnchorY);
        g2.rotate(Math.toRadians(rotationDeg));

        int textLeft = -lineMaxWidth / 2;
        int textTop = -Math.max(lineHeight, lineHeight * lines.size());

        int minTextX = rect.x + marginX - clampedAnchorX;
        int maxTextX = rect.x + rect.width - marginX - clampedAnchorX;
        int minTextY = rect.y + marginX - clampedAnchorY;
        int maxTextY = rect.y + rect.height - marginX - clampedAnchorY;

        textLeft = clampInt(textLeft, minTextX, maxTextX - lineMaxWidth);
        textTop = clampInt(textTop, minTextY, maxTextY - lineHeight * lines.size());

        int y = textTop + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, textLeft, y);
            y += lineHeight;
        }
        g2.setTransform(oldTx);
    }

    private List<String> wrapText(String phrase, Font font, FontRenderContext frc, int maxTextWidth) {
        List<String> lines = new ArrayList<>();
        if (phrase == null || phrase.isEmpty()) return lines;
        String[] words = phrase.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            int width = (int) font.getStringBounds(candidate, frc).getWidth();
            if (width <= maxTextWidth || current.isEmpty()) {
                current.setLength(0);
                current.append(candidate);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add(phrase);
        }
        return lines;
    }

    private DrawRect computeFitRect(int srcW, int srcH, int dstW, int dstH) {
        if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) {
            return new DrawRect(0, 0, Math.max(0, dstW), Math.max(0, dstH));
        }
        double scale = Math.min((double) dstW / srcW, (double) dstH / srcH);
        scale = Math.min(scale, 1.0) * 0.92;
        int w = Math.max(1, (int) Math.round(srcW * scale));
        int h = Math.max(1, (int) Math.round(srcH * scale));
        int x = (dstW - w) / 2;
        int y = (dstH - h) / 2;
        return new DrawRect(x, y, w, h);
    }

    private int clampInt(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @FunctionalInterface
    private interface TextDrawer {
        void draw(Graphics2D g2, DrawRect rect);
    }
}
