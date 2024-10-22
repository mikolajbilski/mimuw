package pl.edu.mimuw.mb458543.stockexchange.stockexchange;

import java.util.Objects;

public class Timestamp implements Comparable<Timestamp> {
    private final int turnNumber;
    private final int turnOrder;

    public Timestamp(int turnNumber, int turnOrder) {
        this.turnNumber = turnNumber;
        this.turnOrder = turnOrder;
    }

    @Override
    public int compareTo(Timestamp o) {
        if (this.turnNumber == o.turnNumber) {
            return this.turnOrder - o.turnOrder;
        }
        return this.turnNumber - o.turnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timestamp timestamp)) return false;
        return turnNumber == timestamp.turnNumber && turnOrder == timestamp.turnOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(turnNumber, turnOrder);
    }
}
