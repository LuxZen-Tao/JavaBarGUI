package com.luxzentao.javabar.core;

import java.util.List;
import java.util.Random;

public class PubNameGenerator {

    private static final List<String> CLASSICS = List.of(
            "The Red Lion",
            "The Crown",
            "The King's Arms",
            "The Swan",
            "The Fox & Hound",
            "The Railway Tavern",
            "The Golden Hart",
            "The White Horse",
            "The Coach & Horses",
            "The Black Bull",
            "The Dog & Duck",
            "The Ship Inn",
            "The Anchor",
            "The Fox",
            "The Rose & Crown"
    );

    private static final List<String> COLORS = List.of(
            "Red", "Golden", "Silver", "White", "Black", "Green"
    );

    private static final List<String> ANIMALS = List.of(
            "Lion", "Swan", "Stag", "Fox", "Badger", "Hound", "Hart", "Boar"
    );

    private PubNameGenerator() {}

    public static String randomName(Random random) {
        if (random == null) random = new Random();
        if (random.nextInt(100) < 65) {
            return CLASSICS.get(random.nextInt(CLASSICS.size()));
        }

        String color = COLORS.get(random.nextInt(COLORS.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        return "The " + color + " " + animal;
    }
}
