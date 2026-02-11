import java.util.List;
import java.util.Random;

public class StaffNameGenerator {
    private static final List<String> NAMES = List.of(
            "Alex", "Bailey", "Casey", "Drew", "Elliot",
            "Finley", "Harper", "Indy", "Jordan", "Kai",
            "Logan", "Morgan", "Parker", "Quinn", "Riley",
            "Rowan", "Sam", "Sky", "Taylor", "Zoe"
    );

    private StaffNameGenerator() {}

    public static String randomName(Random random) {
        return NAMES.get(random.nextInt(NAMES.size()));
    }
}
