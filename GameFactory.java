// GameFactory.java
import java.util.List;

public class GameFactory {

    public static GameState newGame() {
        List<Wine> supplier = List.of(
                new Wine("Crisp & Fruity Blanco", 2024, "England", 0.9, 3.0,
                        WineCategory.CHEAP_HOUSE, 1.30, Punter.Tier.LOWLIFE, 2),
                new Wine("House White", 2021, "Italy", 3.00, 7.00,
                        WineCategory.CHEAP_HOUSE, 1.25, Punter.Tier.REGULAR, 3),
                new Wine("Cheap Table Red", 2020, "Spain", 3.50, 8.50,
                        WineCategory.CHEAP_HOUSE, 1.20, Punter.Tier.REGULAR, 3),
                new Wine("Mineral Riesling", 2022, "Germany", 4.80, 11.00,
                        WineCategory.NICHE_REGIONAL, 1.05, Punter.Tier.DECENT, 3),
                new Wine("Mid-range Merlot", 2018, "France", 7.00, 18.00,
                        WineCategory.MID_TIER_CLASSIC, 0.95, Punter.Tier.DECENT, 4),
                new Wine("Rioja Reserva", 2017, "Spain", 8.50, 21.00,
                        WineCategory.MID_TIER_CLASSIC, 0.95, Punter.Tier.DECENT, 4),
                new Wine("Orange Skin-Contact", 2021, "Georgia", 9.50, 24.00,
                        WineCategory.NICHE_REGIONAL, 0.90, Punter.Tier.DECENT, 4),
                new Wine("Penfolds Grange", 2010, "Australia", 70.00, 180.00,
                        WineCategory.PREMIUM_BOTTLE, 0.60, Punter.Tier.BIG_SPENDER, 5),
                new Wine("Chateau Margaux", 2015, "Bordeaux", 45.00, 120.00,
                        WineCategory.PREMIUM_BOTTLE, 0.65, Punter.Tier.BIG_SPENDER, 5),
                new Wine("Screaming Eagle", 2012, "Napa Valley", 95.00, 250.00,
                        WineCategory.PREMIUM_BOTTLE, 0.55, Punter.Tier.BIG_SPENDER, 5)
        );

        GameState s = new GameState(supplier);
        s.foodSupplier = List.of(
                new Food("Pub Chips", 1.50, 4.50, 1, FoodCategory.CHEAP_BAR_FOOD, 1.25, 2),
                new Food("Loaded Nachos", 2.10, 6.50, 1, FoodCategory.CHEAP_BAR_FOOD, 1.20, 2),
                new Food("Fish & Chips", 3.50, 9.50, 2, FoodCategory.MID_QUALITY_MEAL, 0.95, 3),
                new Food("Sunday Roast", 4.00, 12.00, 2, FoodCategory.MID_QUALITY_MEAL, 0.90, 3),
                new Food("Steak Pie", 4.50, 13.00, 3, FoodCategory.MID_QUALITY_MEAL, 0.90, 3),
                new Food("Veggie Curry", 3.20, 10.00, 2, FoodCategory.MID_QUALITY_MEAL, 0.95, 3),
                new Food("Truffle Mac", 5.80, 16.00, 3, FoodCategory.PREMIUM_DISH, 0.70, 4),
                new Food("Charred Lamb Plate", 7.20, 19.50, 4, FoodCategory.PREMIUM_DISH, 0.65, 4)
        );

        // starting rack
        s.rack.setCapacity(50);
        int today = s.absDayIndex();
        for (int i = 0; i < 10; i++) s.rack.addBottle(supplier.get(0), today);
        for (int i = 0; i < 10; i++) s.rack.addBottle(supplier.get(1), today);
        for (int i = 0; i < 5; i++)  s.rack.addBottle(supplier.get(2), today);

        // starting config
        s.cash = 100.00;
        s.debt = 0.0;
        s.reputation = 10;
        s.baseStaffCap = 4;
        s.fohStaffCap = 4;
        s.baseManagerCap = 1;
        s.managerCap = 1;
        s.baseKitchenChefCap = 2;
        s.kitchenChefCap = 2;
        s.priceMultiplier = 1.10;

        // mark report start (simple + robust)
        s.reportStartCash = s.cash;
        s.reportStartDebt = s.debt;

        return s;
    }
}
