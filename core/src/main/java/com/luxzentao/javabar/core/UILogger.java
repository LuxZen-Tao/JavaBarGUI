package com.luxzentao.javabar.core;

import java.util.List;

public class UILogger implements Logger {
    public enum Tone { INFO, NEUTRAL, MID, POS, GREAT, NEG, EVENT, ACTION, HEADER, WARNING, CRITICAL, MONEY, SECURITY, REPUTATION }
    public record Segment(String text, Tone tone) {}

    @Override public void info(String s) {}
    public void neutral(String s) {}
    public void mid(String s) {}
    @Override public void pos(String s) {}
    public void great(String s) {}
    @Override public void neg(String s) {}
    @Override public void event(String s) {}
    @Override public void header(String s) {}
    public void action(String s) {}
    public void warning(String s) {}
    public void critical(String s) {}
    public void money(String s) {}
    public void security(String s) {}
    public void reputation(String s) {}
    @Override public void spacer() {}

    public void appendLogSegments(List<Segment> segments) {}
    public void popup(String title, String body, String effects) {}
    public void popupUpgrade(String title, String body, String effects) {}
    public void upgrade(String prefix, String label, String suffix, Tone tone) {}
    public static LandlordActionDef getById(LandlordActionId id) { return LandlordActionCatalog.getById(id); }
    public static java.util.List<LandlordActionDef> byId(LandlordActionCategory tier) { return LandlordActionCatalog.actionsForTier(tier); }
    public static java.util.List<LandlordActionDef> actionsForTier(LandlordActionCategory tier) { return LandlordActionCatalog.actionsForTier(tier); }
}
