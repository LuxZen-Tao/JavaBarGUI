package com.luxzentao.javabar.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NameGenerator {
    private static final String FIRST_NAMES_RESOURCE = "names/first_names.txt";
    private static final String LAST_NAMES_RESOURCE = "names/last_names.txt";
    private static final List<String> FIRST_NAMES = new ArrayList<>();
    private static final List<String> LAST_NAMES = new ArrayList<>();
    private static boolean loaded = false;

    private NameGenerator() {}

    public static synchronized String randomName(Random random) {
        loadIfNeeded();
        if (FIRST_NAMES.isEmpty()) return "Punter";
        String first = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        if (!LAST_NAMES.isEmpty() && random.nextInt(100) < 85) {
            String last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            return first + " " + last;
        }
        return first;
    }

    private static void loadIfNeeded() {
        if (loaded) return;
        loadList(FIRST_NAMES_RESOURCE, FIRST_NAMES);
        loadList(LAST_NAMES_RESOURCE, LAST_NAMES);
        loaded = true;
    }

    private static void loadList(String resource, List<String> target) {
        try (InputStream input = NameGenerator.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                System.err.println("Warning: Could not find resource: " + resource);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String cleaned = line.trim();
                    if (cleaned.isEmpty() || cleaned.startsWith("#")) continue;
                    target.add(cleaned);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading name resource " + resource + ": " + e.getMessage());
        }
    }
}
