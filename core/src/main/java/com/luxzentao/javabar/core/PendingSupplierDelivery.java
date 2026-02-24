package com.luxzentao.javabar.core;

public record PendingSupplierDelivery(
        Wine wine,
        int quantity,
        int deliverRound,
        double cost
) implements java.io.Serializable {}
