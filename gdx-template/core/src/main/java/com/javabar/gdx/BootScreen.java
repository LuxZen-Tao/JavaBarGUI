import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

public class BootScreen extends ScreenAdapter {
    private final JavaBarGdxGame game;
    private float elapsed;

    public BootScreen(JavaBarGdxGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        if (elapsed > 0.15f) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.08f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
    }
}
