package com.luxzentao.javabar.core;

// Logger.java
public interface Logger {
    void info(String s);
    void pos(String s);
    void neg(String s);
    void event(String s);
    void header(String s);
    void spacer();

    default void action(String s) { info(s); }
    default void warning(String s) { info(s); }
    default void critical(String s) { info(s); }
    default void popup(String title, String body, String effects) {
        info((title == null ? "" : title) + " | " + (body == null ? "" : body) + " " + (effects == null ? "" : effects));
    }
    default void popup(EventCard card) {
        if (card != null) info(card.title() + " | " + card.body());
    }
    default void popupUpgrade(String title, String body, String effects, String tag) {
        popup(title, body, effects);
    }
}
