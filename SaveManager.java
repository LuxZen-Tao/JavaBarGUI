import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private static final String SAVE_TMP_FILE = "save.tmp";

    private SaveManager() {}

    public static Path getSaveFilePath() {
        return Path.of(System.getProperty("user.home"), APP_DIR, SAVE_FILE);
    }

    public static Path savePath() {
        return getSaveFilePath();
    }

    public static boolean hasSave() {
        return Files.isRegularFile(getSaveFilePath());
    }

    public static void save(GameState state) throws IOException {
        Path path = getSaveFilePath();
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(SAVE_TMP_FILE);
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
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(getSaveFilePath()))) {
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

        private final int schemaVersion;
        private final byte[] stateBytes;

        private SaveData(GameState state) {
            this.schemaVersion = 1;
            this.stateBytes = serializeState(state);
        }

        public static SaveData fromState(GameState state) {
            return new SaveData(state);
        }

        public GameState toState() {
            if (schemaVersion != 1) {
                throw new IllegalStateException("Unsupported save schema version: " + schemaVersion);
            }
            return deserializeState(stateBytes);
        }

        private static byte[] serializeState(GameState state) {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(state);
                out.flush();
                return bytes.toByteArray();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to serialize game state for save.", ex);
            }
        }

        private static GameState deserializeState(byte[] bytes) {
            try (ByteArrayInputStream inBytes = new ByteArrayInputStream(bytes);
                 ObjectInputStream in = new ObjectInputStream(inBytes)) {
                Object loaded = in.readObject();
                if (loaded instanceof GameState state) {
                    return state;
                }
                throw new IllegalStateException("SaveData payload is not GameState: " + loaded.getClass().getName());
            } catch (IOException | ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to deserialize game state from save data.", ex);
            }
        }
    }
}
