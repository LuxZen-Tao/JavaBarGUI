import com.badlogic.gdx.Game;

public class JavaBarGdxGame extends Game {
    private final SimBridge simBridge = new SimBridge();
    private final AudioSettings audioSettings = new AudioSettings();

    @Override
    public void create() {
        setScreen(new BootScreen(this));
    }

    public SimBridge simBridge() {
        return simBridge;
    }

    public AudioSettings audioSettings() {
        return audioSettings;
    }
}
