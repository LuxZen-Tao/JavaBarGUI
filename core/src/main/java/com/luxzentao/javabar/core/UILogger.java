package com.luxzentao.javabar.core;

import java.util.ArrayList;
import java.util.List;

public class UILogger implements Logger {
    public enum Tone { INFO, NEUTRAL, MID, POS, GREAT, NEG, EVENT, ACTION, HEADER, WARNING, CRITICAL, MONEY, SECURITY, REPUTATION }
    public record Segment(String text, Tone tone) {}

    private void out(String s) { if (s != null && !s.isBlank()) System.out.println(s); }

    @Override public void info(String s) { out(s); }
    public void neutral(String s) { out(s); }
    public void mid(String s) { out(s); }
    @Override public void pos(String s) { out(s); }
    public void great(String s) { out(s); }
    @Override public void neg(String s) { out(s); }
    @Override public void event(String s) { out(s); }
    @Override public void header(String s) { out(s); }
    public void action(String s) { out(s); }
    public void warning(String s) { out(s); }
    public void critical(String s) { out(s); }
    public void money(String s) { out(s); }
    public void security(String s) { out(s); }
    public void reputation(String s) { out(s); }
    @Override public void spacer() { System.out.println(); }

    public void appendLogSegments(List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return;
        StringBuilder b = new StringBuilder();
        for (Segment segment : segments) {
            if (segment != null && segment.text() != null) b.append(segment.text());
        }
        out(b.toString());
    }

    public void popup(String title, String body, String effects) { out(title + " | " + body + " " + effects); }
    public void popup(EventCard card) { if (card != null) out(card.title() + " | " + card.body()); }
    public void popupUpgrade(String title, String body, String effects) { out(title + " | " + body + " " + effects); }
    public void popupUpgrade(String title, String body, String effects, String ignoredTag) { popupUpgrade(title, body, effects); }
    public void upgrade(String prefix, String label, String suffix, Tone tone) { out(prefix + label + suffix); }

    public static LandlordActionDef getById(LandlordActionId id) { return LandlordActionCatalog.byId(id); }
    public static List<LandlordActionDef> byId(LandlordActionCategory category) {
        if (category == null) return List.of();
        List<LandlordActionDef> result = new ArrayList<>();
        for (LandlordActionDef def : LandlordActionCatalog.allActions()) {
            if (def.getCategory() == category) result.add(def);
        }
        return result;
    }
    public static List<LandlordActionDef> actionsForTier(int tier) { return LandlordActionCatalog.actionsForTier(tier); }
}
