public record PendingFoodDelivery(
        Food food,
        int quantity,
        int deliverRound,
        double cost
) implements java.io.Serializable {}
