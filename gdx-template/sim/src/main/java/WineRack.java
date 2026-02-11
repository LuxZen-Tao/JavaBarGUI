import java.util.*;
import java.util.function.Consumer;

public class WineRack  implements java.io.Serializable {

    /** Each bottle carries the day it was added (for spoilage). */
    public static class Bottle implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final Wine wine;
        public final int dayAdded;
        public final int spoilAfterDays;

        public Bottle(Wine wine, int dayAdded, int spoilAfterDays) {
            this.wine = wine;
            this.dayAdded = dayAdded;
            this.spoilAfterDays = spoilAfterDays;
        }
    }

    private final List<Bottle> bottles = new ArrayList<>();
    private int capacity = 50;

    /** After this many days in stock, the bottle goes off and is removed. */
    private int spoilAfterDays = 3;

    public void setCapacity(int cap) { this.capacity = Math.max(1, cap); }
    public int getCapacity() { return capacity; }

    public void setSpoilAfterDays(int days) { this.spoilAfterDays = Math.max(1, days); }
    public int getSpoilAfterDays() { return spoilAfterDays; }

    public boolean addBottle(Wine wine, int dayAdded) {
        if (bottles.size() >= capacity) return false;
        int spoil = (wine != null && wine.getSpoilDays() > 0) ? wine.getSpoilDays() : spoilAfterDays;
        bottles.add(new Bottle(wine, dayAdded, spoil));
        return true;
    }

    public int addBottles(Wine wine, int qty, int dayAdded) {
        int added = 0;
        for (int i = 0; i < qty; i++) {
            if (!addBottle(wine, dayAdded)) break;
            added++;
        }
        return added;
    }

    public boolean removeBottle(Wine wine) {
        for (int i = 0; i < bottles.size(); i++) {
            if (bottles.get(i).wine.getName().equals(wine.getName())) {
                bottles.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() { return bottles.isEmpty(); }
    public int count() { return bottles.size(); }

    public List<Bottle> getBottleEntries() { return bottles; }

    /** Returns a snapshot list of just wines (easy for UI + systems). */
    public List<Wine> getWinesSnapshot() {
        List<Wine> out = new ArrayList<>(bottles.size());
        for (Bottle b : bottles) out.add(b.wine);
        return out;
    }

    public double getSellPrice(Wine wine, double priceMultiplier) {
        return wine.getBasePrice() * priceMultiplier;
    }

    public Map<String, Integer> inventoryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Bottle b : bottles) {
            String name = b.wine.getName();
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }
        return counts;
    }

    public record SpoilageLine(String wineName, int count, int daysRemaining) {}

    public List<SpoilageLine> spoilageForecast(int todayIndex) {
        Map<String, Map<Integer, Integer>> grouped = new HashMap<>();
        for (Bottle b : bottles) {
            int age = todayIndex - b.dayAdded;
            int daysRemaining = Math.max(0, b.spoilAfterDays - age);
            grouped
                    .computeIfAbsent(b.wine.getName(), k -> new HashMap<>())
                    .merge(daysRemaining, 1, Integer::sum);
        }

        List<SpoilageLine> lines = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Integer>> entry : grouped.entrySet()) {
            for (Map.Entry<Integer, Integer> sub : entry.getValue().entrySet()) {
                lines.add(new SpoilageLine(entry.getKey(), sub.getValue(), sub.getKey()));
            }
        }

        lines.sort(Comparator
                .comparingInt(SpoilageLine::daysRemaining)
                .thenComparing(SpoilageLine::wineName));
        return lines;
    }

    public void displayInventory(Consumer<String> out) {
        if (bottles.isEmpty()) {
            out.accept("Inventory: (empty)");
            return;
        }
        out.accept("\n=== Inventory (count by type) ===");
        Map<String, Integer> counts = inventoryCounts();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            out.accept(" - " + e.getKey() + " x" + e.getValue());
        }
        out.accept("Total bottles: " + bottles.size() + "/" + capacity);
        out.accept("================================\n");
    }

    public Wine pickRandomBottle(Random random) {
        if (bottles.isEmpty()) return null;
        return bottles.get(random.nextInt(bottles.size())).wine;
    }

    /** Removes bottles that are older than spoilAfterDays. Returns how many removed. */
    public int removeSpoiled(int currentDay) {
        int removed = 0;
        for (int i = bottles.size() - 1; i >= 0; i--) {
            Bottle b = bottles.get(i);
            if ((currentDay - b.dayAdded) >= b.spoilAfterDays) {
                bottles.remove(i);
                removed++;
            }
        }
        return removed;
    }
}
