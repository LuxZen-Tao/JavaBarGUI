import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class UILogger implements Logger {

    public enum Tone { INFO, NEUTRAL, MID, POS, GREAT, NEG, EVENT, ACTION, HEADER }
    public record Segment(String text, Tone tone) {}
    private interface LogEntry {
        void append(StyledDocument doc) throws BadLocationException;
    }
    public record Chunk(String text, Tone tone) implements LogEntry {
        @Override
        public void append(StyledDocument doc) throws BadLocationException {
            doc.insertString(doc.getLength(), text, doc.getStyle(styleName(tone)));
        }
    }
    public record Segments(java.util.List<Segment> segments) implements LogEntry {
        @Override
        public void append(StyledDocument doc) throws BadLocationException {
            for (Segment segment : segments) {
                doc.insertString(doc.getLength(), segment.text(), doc.getStyle(styleName(segment.tone())));
            }
        }
    }

    private final JTextPane pane;
    private final StyledDocument doc;
    private final Deque<LogEntry> queue = new ArrayDeque<>();
    private final Timer timer;
    private boolean showTimestamps = false;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public record PopupMessage(String title, String body, String effects, UIPopup.PopupStyle style) {}

    private Consumer<String> eventSink;
    private Consumer<PopupMessage> popupSink;

    public UILogger(JTextPane pane) {
        this.pane = pane;
        this.doc = pane.getStyledDocument();
        setupStyles();

        pane.setEditable(false);
        pane.setMargin(new Insets(8, 10, 8, 10));
        Font mono = UIManager.getFont("TextPane.font");
        Font base = mono != null ? mono : new Font("Monospaced", Font.PLAIN, 12);
        pane.setFont(base.deriveFont((float) (base.getSize() + 2)));

        timer = new Timer(30, e -> flushOne());
        timer.start();
    }

    private void setupStyles() {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style info = doc.addStyle("INFO", def);
        StyleConstants.setForeground(info, new Color(220, 228, 235)); // light

        Style neutral = doc.addStyle("NEUTRAL", def);
        StyleConstants.setForeground(neutral, new Color(160, 168, 178)); // grey

        Style mid = doc.addStyle("MID", def);
        StyleConstants.setForeground(mid, new Color(240, 190, 90)); // yellow-ish

        Style pos = doc.addStyle("POS", def);
        StyleConstants.setForeground(pos, new Color(128, 210, 150)); // light green

        Style great = doc.addStyle("GREAT", def);
        StyleConstants.setForeground(great, new Color(80, 210, 110)); // green

        Style neg = doc.addStyle("NEG", def);
        StyleConstants.setForeground(neg, new Color(242, 96, 96)); // red

        Style ev = doc.addStyle("EVENT", def);
        StyleConstants.setForeground(ev, new Color(120, 170, 255)); // blue

        Style action = doc.addStyle("ACTION", def);
        StyleConstants.setForeground(action, new Color(240, 190, 90)); // yellow landlord actions
        StyleConstants.setBold(action, true);

        Style hdr = doc.addStyle("HEADER", def);
        StyleConstants.setForeground(hdr, new Color(255, 186, 90)); // orange-ish
        StyleConstants.setBold(hdr, true);
    }

    private void flushOne() {
        if (queue.isEmpty()) return;
        LogEntry entry = queue.removeFirst();
        try {
            entry.append(doc);
            pane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {}
    }

    private static String styleName(Tone tone) {
        return switch (tone) {
            case NEUTRAL -> "NEUTRAL";
            case MID -> "MID";
            case POS -> "POS";
            case GREAT -> "GREAT";
            case NEG -> "NEG";
            case EVENT -> "EVENT";
            case ACTION -> "ACTION";
            case HEADER -> "HEADER";
            default -> "INFO";
        };
    }

    private void push(String s, Tone t) {
        String prefix = "";
        if (showTimestamps) {
            prefix = "[" + LocalTime.now().format(timeFormatter) + "] ";
        }
        String txt = s.endsWith("\n") ? s : (s + "\n");
        String body = prefix + txt;
        if (s.startsWith("\n")) {
            body = "\n" + prefix + s.substring(1);
            if (!body.endsWith("\n")) {
                body = body + "\n";
            }
        }
        queue.addLast(new Chunk(body, t));
    }

    public void appendLogSegments(java.util.List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return;
        java.util.List<Segment> out = new java.util.ArrayList<>();
        String firstText = segments.get(0).text();
        Tone firstTone = segments.get(0).tone();
        if (firstText.startsWith("\n")) {
            out.add(new Segment("\n", Tone.INFO));
            firstText = firstText.substring(1);
        }
        if (showTimestamps) {
            String stamp = "[" + LocalTime.now().format(timeFormatter) + "] ";
            out.add(new Segment(stamp, Tone.INFO));
        }
        if (!firstText.isEmpty()) {
            out.add(new Segment(firstText, firstTone));
        }
        for (int i = 1; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            if (segment != null && segment.text() != null && !segment.text().isEmpty()) {
                out.add(segment);
            }
        }
        if (out.isEmpty() || !out.get(out.size() - 1).text().endsWith("\n")) {
            out.add(new Segment("\n", Tone.INFO));
        }
        queue.addLast(new Segments(out));
    }

    public void info(String s) { push(s, Tone.INFO); }
    public void neutral(String s) { push(s, Tone.NEUTRAL); }
    public void mid(String s) { push(s, Tone.MID); }

    public void pos(String s) { push(s, Tone.POS); }
    public void great(String s) { push(s, Tone.GREAT); }

    public void neg(String s) { push(s, Tone.NEG); }
    public void event(String s) {
        push(s, Tone.EVENT);
        publishEvent(s);
    }
    public void action(String s) { push(s, Tone.ACTION); }

    public void header(String s) { push("\n" + s + "\n", Tone.HEADER); }
    public void spacer() { push("", Tone.INFO); }

    public void setShowTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
    }

    public void setEventSink(Consumer<String> eventSink) {
        this.eventSink = eventSink;
    }

    public void setPopupSink(Consumer<PopupMessage> popupSink) {
        this.popupSink = popupSink;
    }

    public void publishEvent(String s) {
        if (eventSink != null && s != null && !s.isBlank()) {
            eventSink.accept(s.trim());
        }
    }

    public void popup(String title, String body) {
        popup(UIPopup.PopupStyle.EVENT, title, body, "");
    }

    public void popup(String title, String body, String effects) {
        popup(UIPopup.PopupStyle.EVENT, title, body, effects);
    }

    public void popup(UIPopup.PopupStyle style, String title, String body, String effects) {
        push(title + " - " + body, Tone.EVENT);
        publishEvent(title + " - " + body);
        publishPopup(title, body, effects, style);
    }

    public void upgrade(String prefix, String upgradeName, String suffix, Tone baseTone) {
        java.util.List<Segment> segments = new java.util.ArrayList<>();
        if (prefix != null && !prefix.isEmpty()) {
            segments.add(new Segment(prefix, baseTone));
        }
        if (upgradeName != null && !upgradeName.isEmpty()) {
            segments.add(new Segment(upgradeName, Tone.ACTION));
        }
        if (suffix != null && !suffix.isEmpty()) {
            segments.add(new Segment(suffix, baseTone));
        }
        appendLogSegments(segments);
    }

    public void popupUpgrade(String title, String upgradeName, String bodySuffix, String effects) {
        String safeTitle = title == null ? "" : title;
        String suffix = bodySuffix == null ? "" : bodySuffix;
        java.util.List<Segment> segments = new java.util.ArrayList<>();
        segments.add(new Segment(safeTitle + " - ", Tone.EVENT));
        if (upgradeName != null) {
            segments.add(new Segment(upgradeName, Tone.ACTION));
        }
        if (!suffix.isEmpty()) {
            segments.add(new Segment(suffix, Tone.EVENT));
        }
        appendLogSegments(segments);
        publishEvent(safeTitle + " - " + upgradeName + suffix);
        publishPopup(safeTitle, upgradeName + suffix, effects, UIPopup.PopupStyle.EVENT);
    }

    public void popup(EventCard card) {
        if (card == null) return;
        UIPopup.PopupStyle style = UIPopup.PopupStyle.EVENT;
        if (card.repDelta() < 0 || card.cashDelta() < 0) {
            style = UIPopup.PopupStyle.NEGATIVE;
        } else if (card.repDelta() > 0 || card.cashDelta() > 0) {
            style = UIPopup.PopupStyle.POSITIVE;
        }
        popup(style, card.title(), card.body(), card.effectsLine());
    }

    private void publishPopup(String title, String body, String effects, UIPopup.PopupStyle style) {
        if (popupSink != null) {
            popupSink.accept(new PopupMessage(title, body, effects, style));
        }
    }
}
