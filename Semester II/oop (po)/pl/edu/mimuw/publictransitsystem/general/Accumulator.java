package pl.edu.mimuw.publictransitsystem.general;

public class Accumulator {
    private int value;

    public void reset() {
        value = 0;
    }

    public int getValue() {
        return  value;
    }

    public void add(int val) {
        value += val;
    }

    public Accumulator() {
        value = 0;
    }
}
