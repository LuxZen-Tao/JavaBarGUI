package com.luxzentao.javabar.core.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class StatBarPanel extends Table {
    private final Label label;

    public StatBarPanel(Skin skin, Color bgColor) {
        super(skin);
        setBackground(skin.newDrawable("white", bgColor));
        pad(8f);
        label = new Label("--", skin);
        label.setWrap(true);
        label.setAlignment(Align.left);
        add(label).growX().left();
    }

    public void setText(String text) {
        label.setText(text == null || text.isBlank() ? "--" : text);
    }
}
