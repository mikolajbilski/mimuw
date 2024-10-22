package pl.edu.mimuw.publictransitsystem.general;

import java.util.Random;

public class Losowanie {
    private static final Random generator = new Random();

    public static int losuj(int dolna, int gorna) {
        return generator.nextInt(gorna - dolna + 1) + dolna;
    }

    private Losowanie() {

    }
}