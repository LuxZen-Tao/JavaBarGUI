package com.luxzentao.javabar.core.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.luxzentao.javabar.core.bridge.HudSimBridge;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MissionControlModal extends Window {
    private final HudSimBridge bridge;
    private final Label content;
    private final Map<String, Supplier<String>> tabs = new LinkedHashMap<>();
    private String activeTab;

    public MissionControlModal(Skin skin, HudSimBridge bridge) {
        super("Mission Control", skin);
        this.bridge = bridge;
        setModal(true);
        setMovable(true);
        setResizable(true);
        pad(10f);

        tabs.put("Overview", bridge::missionOverview);
        tabs.put("Milestones / Progression", bridge::missionMilestones);
        tabs.put("Policies / Risk", bridge::missionRisk);
        tabs.put("Economy / Finance", bridge::missionEconomy);
        tabs.put("Staff / Management", bridge::missionStaff);

        Table tabsRow = new Table(skin);
        for (String tabName : tabs.keySet()) {
            TextButton btn = new TextButton(tabName, skin);
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) { activeTab = tabName; refresh(); }
            });
            tabsRow.add(btn).padRight(4f);
        }

        content = new Label("", skin);
        content.setWrap(true);
        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);

        add(tabsRow).growX().left().row();
        add(scrollPane).size(760f, 460f).grow().padTop(8f).row();

        TextButton close = new TextButton("Close", skin);
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setVisible(false); }
        });
        add(close).right().padTop(8f);

        activeTab = "Overview";
        refresh();
        pack();
    }

    public void refresh() {
        Supplier<String> supplier = tabs.get(activeTab);
        content.setText(supplier == null ? "--" : supplier.get());
    }

    public void show(Stage stage) {
        if (getStage() == null) stage.addActor(this);
        refresh();
        setVisible(true);
        setSize(Math.min(920f, stage.getWidth() * 0.92f), Math.min(620f, stage.getHeight() * 0.92f));
        setPosition((stage.getWidth() - getWidth()) / 2f, (stage.getHeight() - getHeight()) / 2f);
        toFront();
    }
}
