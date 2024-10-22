package pl.edu.mimuw.publictransitsystem.events;

import pl.edu.mimuw.publictransitsystem.general.Accumulator;
import pl.edu.mimuw.publictransitsystem.passengers.Passenger;
import pl.edu.mimuw.publictransitsystem.general.Utils;
import pl.edu.mimuw.publictransitsystem.stops.LineSegment;
import pl.edu.mimuw.publictransitsystem.stops.Stop;
import pl.edu.mimuw.publictransitsystem.stops.TransitLine;
import pl.edu.mimuw.publictransitsystem.vehicles.Vehicle;

public class VehicleAtStopEvent extends Event{
    private final Vehicle v;
    // Required to handle loops properly
    private final boolean letIn;
    private final boolean letOut;
    private final Accumulator waitCounter;

    @Override
    public Event handle() {
        System.out.println(this);
        LineSegment nextLineSegment = v.getLineSegment();
        Stop stop = v.getLineSegment().getStop();
        if(letOut) {
            int availableSpace = stop.availableSpace();
            Passenger[] passengers = v.getPassengers(availableSpace);
            stop.addPassengersFromVehicle(passengers, day, timestamp);
        }
        if(letIn) {
            int availableSpace = v.availableSpace();
            Passenger[] passengers =  stop.getPassengers(availableSpace);
            waitCounter.add(v.addPassengers(passengers, day, timestamp));
        }

        v.goToNextStop();
        int nextTime = timestamp + nextLineSegment.getLength();
        if(nextTime > TransitLine.LAST_DEPARTURE && v.isAtLineEnd()) {
            return null; // no departures after 23:00
        }
        boolean letIn = true;
        boolean letOut = true;
        switch (v.getLineSegment().getType()) {
            case LineSegment.TYPE.FIRST:
                letOut = false;
                break;
            case LineSegment.TYPE.LAST:
                letIn = false;
                break;
        }

        return new VehicleAtStopEvent(nextTime, v, day, letIn, letOut, waitCounter);
    }

    public VehicleAtStopEvent(int timestamp, Vehicle v, int day, boolean letIn, boolean letOut, Accumulator waitCounter) {
        super(timestamp, day);
        this.v = v;
        this.letIn = letIn;
        this.letOut = letOut;
        this.waitCounter = waitCounter;
    }

    @Override
    public String toString() {
        String arrivalType = switch (v.getLineSegment().getType()) {
            case LineSegment.TYPE.NORMAL -> " przyjechał na przystanek ";
            case LineSegment.TYPE.LAST -> " przyjechał na pętlę ";
            case LineSegment.TYPE.FIRST -> " wyjedzie z pętli ";
        };


        return super.toString() + Utils.capitalizeFirst(v.toString()) + arrivalType +
                v.getLineSegment().getStop() + ".";
    }
}
