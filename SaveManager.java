import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SaveManager {
    private static final String APP_DIR = ".publandlordidle";
    private static final String SAVE_FILE = "savegame.dat";

    private SaveManager() {}

    public static Path savePath() {
        return Path.of(System.getProperty("user.home"), APP_DIR, SAVE_FILE);
    }

    public static boolean hasSave() {
        return Files.isRegularFile(savePath());
    }

    public static void save(GameState state) throws IOException {
        Path path = savePath();
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
            out.writeObject(SaveData.fromState(state));
            out.flush();
        }
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static GameState load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath()))) {
            Object loaded = in.readObject();
            if (loaded instanceof SaveData data) {
                return data.toState();
            }
            // Legacy fallback for existing save files written before SaveData wrapper.
            if (loaded instanceof GameState state) {
                return state;
            }
            throw new IOException("Unexpected save payload type: " + loaded.getClass().getName());
        }
    }

    public static final class SaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final GameState state;

        private SaveData(GameState state) {
            this.state = state;
        }

        public static SaveData fromState(GameState state) {
            return new SaveData(state);
        }

        public GameState toState() {
            return state;
        }
    }
}
