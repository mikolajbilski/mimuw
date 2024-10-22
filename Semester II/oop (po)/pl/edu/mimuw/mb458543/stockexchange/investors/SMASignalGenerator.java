package pl.edu.mimuw.mb458543.stockexchange.investors;

import pl.edu.mimuw.mb458543.stockexchange.utils.MovingAverage;

public class SMASignalGenerator {
    private final MovingAverage longSMA;
    private final MovingAverage shortSMA;

    public SMASignalGenerator(int longSMA, int shortSMA) {
        this.longSMA = new MovingAverage(longSMA);
        this.shortSMA = new MovingAverage(shortSMA);
    }

    // Generate SMA signal based on new price.
    // Positive value is buy signal, negative is sell signal
    // 0 means no signal
    // abs of signal is the signal's strength
    public double checkForSignal(int newValue) {
        double prevLongSMA = longSMA.getAverage();
        double prevShortSMA = shortSMA.getAverage();
        longSMA.add(newValue);
        shortSMA.add(newValue);
        double newLongSMA = longSMA.getAverage();
        double newShortSMA = shortSMA.getAverage();
        if(newLongSMA * newShortSMA * prevLongSMA * prevShortSMA == 0) {
            return 0;
        }
        if(prevShortSMA <= prevLongSMA && newShortSMA > newLongSMA) {
            return newShortSMA / newLongSMA - 0.5;
        }
        if(prevShortSMA >= prevLongSMA && newShortSMA < newLongSMA) {
            return -newLongSMA / newShortSMA + 0.5;
        }
        return 0;
    }
}
