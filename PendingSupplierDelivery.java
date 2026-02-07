public record PendingSupplierDelivery(
        Wine wine,
        int quantity,
        int deliverRound,
        double cost
) {}
