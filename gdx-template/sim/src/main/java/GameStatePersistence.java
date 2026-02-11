import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class GameStatePersistence {

    public static String serializePrestigeState(GameState s) {
        Properties props = new Properties();
        props.setProperty("starCount", String.valueOf(s.starCount));
        props.setProperty("prestigeWeekStart", String.valueOf(s.prestigeWeekStart));
        props.setProperty("legacy.inventoryCapBonus", String.valueOf(s.legacy.inventoryCapBonus));
        props.setProperty("legacy.innRoomBonus", String.valueOf(s.legacy.innRoomBonus));
        props.setProperty("legacy.trafficMultiplierBonus", String.valueOf(s.legacy.trafficMultiplierBonus));
        props.setProperty("legacy.supplierTradeCreditBonus", String.valueOf(s.legacy.supplierTradeCreditBonus));
        props.setProperty("legacy.baseSecurityBonus", String.valueOf(s.legacy.baseSecurityBonus));
        props.setProperty("legacy.staffEfficiencyBonus", String.valueOf(s.legacy.staffEfficiencyBonus));
        props.setProperty("pubLevel", String.valueOf(s.pubLevel));
        props.setProperty("baseSecurityLevel", String.valueOf(s.baseSecurityLevel));
        props.setProperty("ownedUpgrades", s.ownedUpgrades.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(",")));
        props.setProperty("prestigeMilestones", s.prestigeMilestones.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(",")));

        try (StringWriter writer = new StringWriter()) {
            props.store(writer, "JavaBarGUI prestige save");
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize prestige state.", e);
        }
    }

    public static void applyPrestigeState(GameState s, String data) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(data));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load prestige state.", e);
        }

        s.starCount = parseInt(props.getProperty("starCount"), 0);
        s.prestigeWeekStart = parseInt(props.getProperty("prestigeWeekStart"), s.weekCount);
        s.legacy.inventoryCapBonus = parseInt(props.getProperty("legacy.inventoryCapBonus"), 0);
        s.legacy.innRoomBonus = parseInt(props.getProperty("legacy.innRoomBonus"), 0);
        s.legacy.trafficMultiplierBonus = parseDouble(props.getProperty("legacy.trafficMultiplierBonus"), 0.0);
        s.legacy.supplierTradeCreditBonus = parseInt(props.getProperty("legacy.supplierTradeCreditBonus"), 0);
        s.legacy.baseSecurityBonus = parseInt(props.getProperty("legacy.baseSecurityBonus"), 0);
        s.legacy.staffEfficiencyBonus = parseDouble(props.getProperty("legacy.staffEfficiencyBonus"), 0.0);
        s.pubLevel = parseInt(props.getProperty("pubLevel"), s.pubLevel);
        s.baseSecurityLevel = parseInt(props.getProperty("baseSecurityLevel"), s.baseSecurityLevel);

        s.ownedUpgrades.clear();
        String upgradesRaw = props.getProperty("ownedUpgrades", "");
        if (!upgradesRaw.isBlank()) {
            for (String name : upgradesRaw.split(",")) {
                if (name.isBlank()) continue;
                s.ownedUpgrades.add(PubUpgrade.valueOf(name));
            }
        }

        s.prestigeMilestones.clear();
        String milestoneRaw = props.getProperty("prestigeMilestones", "");
        if (!milestoneRaw.isBlank()) {
            Arrays.stream(milestoneRaw.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .forEach(value -> s.prestigeMilestones.add(MilestoneSystem.Milestone.valueOf(value)));
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
