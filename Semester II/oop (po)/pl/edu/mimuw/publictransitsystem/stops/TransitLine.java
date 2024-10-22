package pl.edu.mimuw.publictransitsystem.stops;

import pl.edu.mimuw.publictransitsystem.general.Accumulator;
import pl.edu.mimuw.publictransitsystem.general.Losowanie;
import pl.edu.mimuw.publictransitsystem.events.VehicleAtStopEvent;
import pl.edu.mimuw.publictransitsystem.vehicles.Vehicle;

public abstract class TransitLine {

    public enum DIRECTION {
        FORWARD,
        BACKWARD,
    }

    // Lets the code be expanded to include lines such as N32
    private final String name;
    private final int vehicleCount;
    // This allows for asymmetric lines in the future
    private final LineSegment[] forwardSegments;
    private final LineSegment[] backwardSegments;
    protected Vehicle[] vehicles;
    private final int loopTime;
    private static final int FIRST_DEPARTURE = 60 * 6;
    public static final int LAST_DEPARTURE = 60 * 23;

    public void endDay() {
        for(Vehicle vehicle : vehicles) {
            vehicle.endDay();
        }
    }

    public static DIRECTION oppositeDirection(DIRECTION d) {
        return (d == DIRECTION.FORWARD ? DIRECTION.BACKWARD : DIRECTION.FORWARD);
    }

    public VehicleAtStopEvent[] getMorningDepartures(int day, Accumulator waitCounter) {
        VehicleAtStopEvent[] departures = new VehicleAtStopEvent[vehicleCount];
        int interval = loopTime / ((vehicleCount + 1 ) / 2);
        int currentTime = FIRST_DEPARTURE;
        for(int i = 0; i < vehicleCount - 1; i += 2) {
            departures[i] = new VehicleAtStopEvent(currentTime, vehicles[i], day, true, false, waitCounter);
            departures[i + 1] = new VehicleAtStopEvent(currentTime, vehicles[i + 1], day, true, false, waitCounter);
            currentTime += interval;
        }
        if(vehicleCount % 2 == 1) {
            departures[vehicleCount - 1] = new VehicleAtStopEvent(currentTime, vehicles[vehicleCount - 1], day, true, false, waitCounter);
        }
        return departures;
    }

    public TransitLine(String name, int vehicleCount, LineSegment[] forwardSegments) {
        this.name = name;
        this.vehicleCount = vehicleCount;
        this.forwardSegments = forwardSegments;
        int stopsCount = forwardSegments.length;
        LineSegment[] backwardSegments = new LineSegment[stopsCount];

        for(int i = 0; i < stopsCount; ++i) {
            Stop s = forwardSegments[stopsCount - 1 - i].getStop();
            int length;
            if(i == stopsCount - 1) length = forwardSegments[stopsCount - 1].getLength();
            else length = forwardSegments[stopsCount - 2 - i].getLength();
            LineSegment.TYPE t = LineSegment.TYPE.NORMAL;
            if(i == 0) {
                t = LineSegment.TYPE.FIRST;
            } else if(i == stopsCount - 1) {
                t = LineSegment.TYPE.LAST;
            }
            backwardSegments[i] = new LineSegment(s, length, t);
        }
        this.backwardSegments = backwardSegments;

        // Calculate total loop time once
        int total = 0;
        for (LineSegment forwardSegment : forwardSegments) {
            total += forwardSegment.getLength();
        }
        loopTime = total;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public LineSegment getLineSegment(int currentStopID, DIRECTION d) {
        if(d == DIRECTION.FORWARD) {
            return forwardSegments[currentStopID];
        } else {
            return backwardSegments[currentStopID];
        }
    }

    public int getRandomStopFurtherOnLine(int stopID, DIRECTION d) {
        //int realID = (d == DIRECTION.FORWARD) ? stopID : forwardSegments.length - stopID - 1;
        if(d == DIRECTION.FORWARD) {
            return Losowanie.losuj(stopID + 1, forwardSegments.length - 1);
        } else {
            return Losowanie.losuj(0, forwardSegments.length - stopID - 2);
        }
    }

    public Stop getStopByID(int ID) {
        return forwardSegments[ID].getStop();
    }

    public int getStopCount() {
        return forwardSegments.length;
    }

    public String getName() {
        return name;
    }

    public LineSegment[] getForwardSegments() {
        return forwardSegments;
    }

    public LineSegment[] getBackwardSegments() {
        return  backwardSegments;
    }
}
