import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen extends ScreenAdapter {
    private final JavaBarGdxGame game;
    private final Stage stage;
    private final Skin skin;

    public MainMenuScreen(JavaBarGdxGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = UiSkinFactory.createBasicSkin();

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(8);

        Label title = new Label("JavaBarSim - libGDX Starter", skin);
        TextButton newGame = new TextButton("New Game", skin);
        TextButton loadGame = new TextButton("Load Game (stub)", skin);
        Label info = new Label("", skin);

        newGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.simBridge().newGame();
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        loadGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                info.setText("Load hook not wired yet.");
            }
        });

        table.add(title).row();
        table.add(newGame).width(260).row();
        table.add(loadGame).width(260).row();
        table.add(info).row();

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
