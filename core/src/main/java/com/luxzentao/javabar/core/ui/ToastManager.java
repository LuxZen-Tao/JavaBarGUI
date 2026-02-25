package com.luxzentao.javabar.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class ToastManager {
    private final Stage stage;
    private final Skin skin;

    public ToastManager(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    public void show(String text) {
        if (text == null || text.isBlank()) return;

        Label toastLabel = new Label(text, skin);
        toastLabel.setWrap(true);
        toastLabel.setAlignment(Align.left);

        Container<Label> bubble = new Container<>(toastLabel);
        bubble.setBackground(skin.newDrawable("white", new Color(0.08f, 0.08f, 0.12f, 0.92f)));
        bubble.pad(8f);
        bubble.fill();
        float bw = Math.min(440f, stage.getWidth() * 0.6f);
        float bh = 60f;
        bubble.setSize(bw, bh);
        bubble.setPosition((stage.getWidth() - bw) / 2f, (stage.getHeight() - bh) / 2f);
        bubble.getColor().a = 0f;

        stage.addActor(bubble);
        bubble.addAction(Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(2.2f),
                Actions.fadeOut(0.35f),
                Actions.removeActor()
        ));
    }
}
