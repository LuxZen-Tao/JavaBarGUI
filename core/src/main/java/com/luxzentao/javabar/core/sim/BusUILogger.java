package com.luxzentao.javabar.core.sim;

import com.luxzentao.javabar.core.EventCard;
import com.luxzentao.javabar.core.UILogger;

public class BusUILogger extends UILogger {
    private final SimEventBus eventBus;

    public BusUILogger(SimEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override public void info(String s) { super.info(s); eventBus.fireLog(s); }
    @Override public void pos(String s) { super.pos(s); eventBus.fireLog(s); }
    @Override public void neg(String s) { super.neg(s); eventBus.fireLog(s); }
    @Override public void event(String s) { super.event(s); eventBus.fireLog(s); }
    @Override public void header(String s) { super.header(s); eventBus.fireLog(s); }

    @Override public void neutral(String s) { super.neutral(s); eventBus.fireLog(s); }
    @Override public void mid(String s) { super.mid(s); eventBus.fireLog(s); }
    @Override public void great(String s) { super.great(s); eventBus.fireLog(s); }
    @Override public void action(String s) { super.action(s); eventBus.fireLog(s); }
    @Override public void warning(String s) { super.warning(s); eventBus.fireLog(s); }
    @Override public void critical(String s) { super.critical(s); eventBus.fireLog(s); }
    @Override public void money(String s) { super.money(s); eventBus.fireLog(s); }
    @Override public void security(String s) { super.security(s); eventBus.fireLog(s); }
    @Override public void reputation(String s) { super.reputation(s); eventBus.fireLog(s); }

    @Override
    public void popup(String title, String body, String effects) {
        super.popup(title, body, effects);
        eventBus.fireLog((title == null ? "" : title) + " | " + (body == null ? "" : body));
    }

    @Override
    public void popup(EventCard card) {
        super.popup(card);
        if (card != null) eventBus.fireLog(card.title() + " | " + card.body());
    }

    @Override
    public void popupUpgrade(String title, String body, String effects) {
        super.popupUpgrade(title, body, effects);
        eventBus.fireLog((title == null ? "" : title) + " | " + (body == null ? "" : body));
    }
}
