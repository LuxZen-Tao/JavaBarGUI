import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreditLineManager {
    private final List<CreditLine> openLines = new ArrayList<>();

    public List<CreditLine> getOpenLines() {
        return Collections.unmodifiableList(openLines);
    }

    public boolean hasAvailableCredit(int amount) {
        // TODO: Enable in Chunk 2 (banks/credit lines).
        return false;
    }

    public boolean applyCredit(int amount) {
        // TODO: Enable in Chunk 2 (banks/credit lines).
        return false;
    }
}
