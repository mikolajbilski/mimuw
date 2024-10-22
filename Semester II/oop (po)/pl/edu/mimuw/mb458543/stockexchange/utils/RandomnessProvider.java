package pl.edu.mimuw.mb458543.stockexchange.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomnessProvider {
    private static final Random rand = new Random();

    public static <T> T chooseRandom(List<T> collection) {
        return collection.get(rand.nextInt(collection.size()));
    }

    public static void shuffle(List<?> collection) {
        Collections.shuffle(collection, rand);
    }

    public static int getRandomNumber(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min, min = " + min + ", max = " + max);
        }
        return rand.nextInt((max - min) + 1) + min;
    }
}
