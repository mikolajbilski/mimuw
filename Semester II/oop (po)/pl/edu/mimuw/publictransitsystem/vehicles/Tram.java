package pl.edu.mimuw.publictransitsystem.vehicles;

import pl.edu.mimuw.publictransitsystem.stops.TransitLine;

public class Tram extends Vehicle {

    @Override
    public String getTypeName() {
        return "tramwaj";
    }

    @Override
    public String getDativeName() {
        return "tramwaju";
    }

    public Tram(TransitLine line, int capacity, TransitLine.DIRECTION direction) {
        super(line, capacity, direction);
    }
}