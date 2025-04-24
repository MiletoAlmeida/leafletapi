package com.miletoalmeida.leafletapi.util;

import java.util.Random;

public class ApiUtils {

    private static final Random random = new Random();

    public static void simulateHumanBehavior() {
        try {
            // Random delay between 500ms and 2s
            Thread.sleep(500 + random.nextInt(1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String generateCacheKey(String prefix, String value) {
        return prefix + "_" + value.toLowerCase().replaceAll("\\s+", "_");
    }
}