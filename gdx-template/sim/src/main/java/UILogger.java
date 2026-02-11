import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UILogger implements Logger {
    public enum Tone { INFO, NEUTRAL, MID, POS, GREAT, NEG, EVENT, ACTION, HEADER }
    public record Segment(String text, Tone tone) {}
    public record PopupMessage(String title, String body, String effects, UIPopup.PopupStyle style) {}

    private final List<String> lines = new ArrayList<>();
    private Consumer<String> eventSink;
    private Consumer<PopupMessage> popupSink;

    public UILogger() {}

    private void push(String text) {
        if (text == null || text.isBlank()) return;
        lines.add(text);
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }

    public void appendLogSegments(List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (Segment segment : segments) {
            if (segment != null && segment.text() != null) {
                sb.append(segment.text());
            }
        }
        push(sb.toString());
    }

    @Override public void info(String s) { push(s); }
    public void neutral(String s) { push(s); }
    public void mid(String s) { push(s); }
    @Override public void pos(String s) { push(s); }
    public void great(String s) { push(s); }
    @Override public void neg(String s) { push(s); }
    @Override public void event(String s) { push(s); publishEvent(s); }
    public void action(String s) { push(s); }
    @Override public void header(String s) { push(s); }
    @Override public void spacer() { push(" "); }

    public void setShowTimestamps(boolean showTimestamps) {}
    public void setEventSink(Consumer<String> eventSink) { this.eventSink = eventSink; }
    public void setPopupSink(Consumer<PopupMessage> popupSink) { this.popupSink = popupSink; }
    public void publishEvent(String s) { if (eventSink != null && s != null) eventSink.accept(s); }

    public void popup(String title, String body) { popup(UIPopup.PopupStyle.EVENT, title, body, ""); }
    public void popup(String title, String body, String effects) { popup(UIPopup.PopupStyle.EVENT, title, body, effects); }
    public void popup(UIPopup.PopupStyle style, String title, String body, String effects) {
        push(title + " - " + body);
        if (popupSink != null) popupSink.accept(new PopupMessage(title, body, effects, style));
    }


    public void popup(EventCard card) {
        if (card == null) return;
        popup(card.title(), card.body(), card.effectsLine());
    }

    public void popupUpgrade(String prefix, String upgradeName, String suffix, String effects) {
        String title = (prefix == null ? "" : prefix) + (upgradeName == null ? "" : upgradeName) + (suffix == null ? "" : suffix);
        popup(title, "", effects == null ? "" : effects);
    }
    public void upgrade(String prefix, String upgradeName, String suffix, Tone baseTone) {
        push((prefix == null ? "" : prefix) + (upgradeName == null ? "" : upgradeName) + (suffix == null ? "" : suffix));
    }
}
