package pl.edu.mimuw.publictransitsystem.stops;

import pl.edu.mimuw.publictransitsystem.vehicles.Tram;

public class TramLine extends TransitLine {
    public TramLine(String lineName, int tramCount, LineSegment[] forwardSegments, int tram_capacity) {
        super(lineName, tramCount, forwardSegments);
        vehicles = new Tram[tramCount];
        DIRECTION d = DIRECTION.FORWARD;
        for(int i = 0; i < tramCount; ++i) {
            vehicles[i] = new Tram(this, tram_capacity, d);
            d = oppositeDirection(d);
        }
    }
}
