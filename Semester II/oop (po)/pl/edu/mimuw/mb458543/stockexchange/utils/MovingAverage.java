package pl.edu.mimuw.mb458543.stockexchange.utils;

import java.util.ArrayList;
import java.util.List;

public class MovingAverage {
    private final List<Integer> previousValues;
    private final int length;

    public MovingAverage(int length) {
        if(length <= 0) {
            throw new IllegalArgumentException("Moving average must have positive length!");
        }
        previousValues = new ArrayList<>(length);
        for(int i = 0; i < length; ++i) {
            previousValues.add(0);
        }
        this.length = length;
    }

    public void add(int value) {
        previousValues.removeFirst();
        previousValues.add(value);
    }

    public double getAverage() {
        double sum = 0;
        for (int value : previousValues) {
            sum += value;
        }
        return sum / length;
    }
}
