import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen extends ScreenAdapter {
    private final JavaBarGdxGame game;
    private final Stage stage;
    private final Skin skin;
    private final SpriteBatch batch;
    private final Texture background;

    private final Label hud;
    private final Label tickLabel;

    public GameScreen(JavaBarGdxGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = UiSkinFactory.createBasicSkin();
        this.batch = new SpriteBatch();
        this.background = createBackgroundTexture();

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().pad(12);
        root.defaults().pad(5);

        hud = new Label("", skin);
        tickLabel = new Label("", skin);

        TextButton openButton = new TextButton("Open Service", skin);
        TextButton closeButton = new TextButton("Close Service", skin);
        TextButton tickButton = new TextButton("Advance", skin);

        openButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.simBridge().openService();
                refreshHud();
            }
        });

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.simBridge().closeService();
                refreshHud();
            }
        });

        tickButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.simBridge().advanceTick();
                refreshHud();
            }
        });

        root.add(hud).left().row();
        root.add(tickLabel).left().row();

        Table buttons = new Table();
        buttons.defaults().pad(4);
        buttons.add(openButton).width(130);
        buttons.add(closeButton).width(130);
        buttons.add(tickButton).width(130);
        root.add(buttons).left().row();

        stage.addActor(root);
        refreshHud();
    }

    private Texture createBackgroundTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.08f, 0.10f, 0.14f, 1f));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void refreshHud() {
        PresentationSnapshot s = game.simBridge().snapshot();
        hud.setText(String.format(
                "Cash £%.2f | Debt £%.2f | Rep %d | Chaos %.1f | Week %d Day %d | Service %s | Traffic %d",
                s.money(), s.debt(), s.reputation(), s.chaos(), s.week(), s.day(), s.serviceOpen() ? "OPEN" : "CLOSED", s.traffic()
        ));
        tickLabel.setText(String.format(
                "Round %d | Last tick: unserved %d, refunds %d, fights %d | Music %.2f, Chatter %.2f",
                s.round(), s.unservedLastTick(), s.refundsLastTick(), s.fightsLastTick(),
                game.audioSettings().musicVolume(), game.audioSettings().chatterVolume()
        ));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

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
        batch.dispose();
        background.dispose();
    }
}
