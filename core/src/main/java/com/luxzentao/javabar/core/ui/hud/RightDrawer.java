package com.luxzentao.javabar.core.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class RightDrawer extends Table {
    private final Stage stage;
    private final float drawerWidth;
    private boolean open;

    public RightDrawer(Stage stage, Skin skin, String title, float drawerWidth, Runnable onClose) {
        super(skin);
        this.stage = stage;
        this.drawerWidth = drawerWidth;
        setBackground(skin.newDrawable("white", new com.badlogic.gdx.graphics.Color(0.08f, 0.10f, 0.14f, 0.97f)));
        pad(8f);

        Label titleLabel = new Label(title, skin);
        TextButton close = new TextButton("X", skin);
        close.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { onClose.run(); }
        });

        Table header = new Table(skin);
        header.add(titleLabel).left().expandX();
        header.add(close).right().width(42f);
        add(header).growX().row();

        setSize(drawerWidth, stage.getHeight());
        setPosition(stage.getWidth(), 0);
    }

    public void setBody(Actor body) {
        if (getCells().size > 1) removeActor(getCells().get(1).getActor());
        add(body).grow().padTop(8f).row();
    }

    public void open() {
        open = true;
        clearActions();
        addAction(Actions.moveTo(stage.getWidth() - drawerWidth, 0, 0.2f));
    }

    public void close() {
        open = false;
        clearActions();
        addAction(Actions.moveTo(stage.getWidth(), 0, 0.2f));
    }

    public boolean isOpen() { return open; }

    public void onResize() {
        setHeight(stage.getHeight());
        setPosition(open ? stage.getWidth() - drawerWidth : stage.getWidth(), 0);
    }
}
