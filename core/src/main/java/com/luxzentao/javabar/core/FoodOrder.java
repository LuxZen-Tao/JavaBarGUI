package com.luxzentao.javabar.core;

public record FoodOrder(
        int punterId,
        String punterName,
        Food food,
        double price,
        int deliverRound
) {}
