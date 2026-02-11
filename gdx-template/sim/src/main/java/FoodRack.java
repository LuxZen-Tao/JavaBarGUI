import java.util.*;

public class FoodRack  implements java.io.Serializable {

    public static class Meal implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final Food food;
        public final int dayAdded;
        public final int spoilAfterDays;

        public Meal(Food food, int dayAdded, int spoilAfterDays) {
            this.food = food;
            this.dayAdded = dayAdded;
            this.spoilAfterDays = spoilAfterDays;
        }
    }

    private final List<Meal> meals = new ArrayList<>();
    private int capacity = 30;
    private int spoilAfterDays = 3;

    public void setCapacity(int cap) { this.capacity = Math.max(1, cap); }
    public int getCapacity() { return capacity; }

    public void setSpoilAfterDays(int days) { this.spoilAfterDays = Math.max(1, days); }
    public int getSpoilAfterDays() { return spoilAfterDays; }

    public boolean addMeal(Food food, int dayAdded) {
        if (meals.size() >= capacity) return false;
        int spoil = (food != null && food.getSpoilDays() > 0) ? food.getSpoilDays() : spoilAfterDays;
        meals.add(new Meal(food, dayAdded, spoil));
        return true;
    }

    public int addMeals(Food food, int qty, int dayAdded) {
        int added = 0;
        for (int i = 0; i < qty; i++) {
            if (!addMeal(food, dayAdded)) break;
            added++;
        }
        return added;
    }

    public boolean removeFood(Food food) {
        for (int i = 0; i < meals.size(); i++) {
            if (meals.get(i).food.getName().equals(food.getName())) {
                meals.remove(i);
                return true;
            }
        }
        return false;
    }

    public int count() { return meals.size(); }
    public boolean isEmpty() { return meals.isEmpty(); }

    public Map<String, Integer> inventoryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Meal meal : meals) {
            String name = meal.food.getName();
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }
        return counts;
    }

    public record SpoilageLine(String foodName, int count, int daysRemaining) {}

    public List<SpoilageLine> spoilageForecast(int todayIndex) {
        Map<String, Map<Integer, Integer>> grouped = new HashMap<>();
        for (Meal meal : meals) {
            int age = todayIndex - meal.dayAdded;
            int daysRemaining = Math.max(0, meal.spoilAfterDays - age);
            grouped
                    .computeIfAbsent(meal.food.getName(), k -> new HashMap<>())
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
                .thenComparing(SpoilageLine::foodName));
        return lines;
    }

    public record SpoilageSummary(int nextSpoilDays, int atRiskCount) {}

    public SpoilageSummary spoilageSummary(int todayIndex) {
        if (meals.isEmpty()) return null;
        int nextSpoilDays = Integer.MAX_VALUE;
        int atRisk = 0;
        for (Meal meal : meals) {
            int age = todayIndex - meal.dayAdded;
            int daysRemaining = Math.max(0, meal.spoilAfterDays - age);
            if (daysRemaining < nextSpoilDays) nextSpoilDays = daysRemaining;
            if (daysRemaining <= 1) atRisk++;
        }
        if (nextSpoilDays == Integer.MAX_VALUE) return null;
        return new SpoilageSummary(nextSpoilDays, atRisk);
    }

    public Food pickRandomFood(Random random) {
        if (meals.isEmpty()) return null;
        return meals.get(random.nextInt(meals.size())).food;
    }

    public double getSellPrice(Food food, int qualityBonus) {
        double qualityMult = 1.0 + (0.05 * Math.max(0, food.getQualityTier() - 1));
        double bonus = 1.0 + (0.02 * Math.max(0, qualityBonus));
        return food.getBasePrice() * qualityMult * bonus;
    }

    public int removeSpoiled(int currentDay) {
        int removed = 0;
        for (int i = meals.size() - 1; i >= 0; i--) {
            Meal meal = meals.get(i);
            if ((currentDay - meal.dayAdded) >= meal.spoilAfterDays) {
                meals.remove(i);
                removed++;
            }
        }
        return removed;
    }
}
