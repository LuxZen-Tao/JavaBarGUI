package com.luxzentao.javabar.core.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityLogPanel extends Table {
    private record LogEntry(String message, Color color, String ts) {}

    private final Skin skin;
    private final CheckBox eventFeedToggle;
    private final CheckBox timestampToggle;
    private final Table lines;
    private final ScrollPane scrollPane;
    private final List<LogEntry> history = new ArrayList<>();

    public ActivityLogPanel(Skin skin) {
        super(skin);
        this.skin = skin;
        setBackground(skin.newDrawable("white", new Color(0.06f, 0.08f, 0.11f, 0.96f)));
        pad(6f);

        Label title = new Label("Activity Log", skin);
        eventFeedToggle = new CheckBox("Event Feed", skin);
        eventFeedToggle.setChecked(true);
        timestampToggle = new CheckBox("Timestamps", skin);

        Table header = new Table(skin);
        header.add(title).left().expandX();
        header.add(eventFeedToggle).padRight(10f);
        header.add(timestampToggle);
        add(header).growX().row();

        lines = new Table(skin);
        lines.top().left();
        scrollPane = new ScrollPane(lines, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        add(scrollPane).grow().padTop(4f);

        eventFeedToggle.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) { rebuild(); }
        });
        timestampToggle.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) { rebuild(); }
        });
    }

    public void append(String message) {
        String msg = message == null ? "" : message.trim();
        if (msg.isEmpty()) return;
        history.add(new LogEntry(msg, tone(msg), LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        if (history.size() > 500) history.remove(0);
        addEntry(history.get(history.size() - 1));
    }

    private void rebuild() {
        lines.clearChildren();
        for (LogEntry entry : history) addEntry(entry);
    }

    private void addEntry(LogEntry entry) {
        if (!eventFeedToggle.isChecked() && looksLikeEvent(entry.message)) return;
        String text = timestampToggle.isChecked() ? "[" + entry.ts + "] " + entry.message : entry.message;
        Label line = new Label(text, skin);
        line.setColor(entry.color);
        line.setWrap(true);
        line.setAlignment(Align.left);
        lines.add(line).left().growX().padBottom(2f).row();

        Gdx.app.postRunnable(() -> {
            scrollPane.layout();
            scrollPane.setScrollPercentY(1f);
        });
    }

    private boolean looksLikeEvent(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("event") || lower.contains("popup") || lower.contains("trigger");
    }

    private Color tone(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("fine") || lower.contains("violation") || lower.contains("underage") || lower.contains("rep -") || lower.contains("critical")) return new Color(1f, 0.45f, 0.45f, 1f);
        if (lower.contains("cash +") || lower.contains("sale") || lower.contains("profit") || lower.contains("tips") || lower.contains("opened")) return new Color(0.55f, 1f, 0.55f, 1f);
        if (lower.contains("week") || lower.contains("service") || lower.contains("report") || lower.contains("mission")) return new Color(0.55f, 0.75f, 1f, 1f);
        return Color.WHITE;
    }
}
