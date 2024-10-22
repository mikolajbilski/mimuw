package pl.edu.mimuw.publictransitsystem.vehicles;

import pl.edu.mimuw.publictransitsystem.passengers.ArrayPassengerQueue;
import pl.edu.mimuw.publictransitsystem.passengers.Passenger;
import pl.edu.mimuw.publictransitsystem.passengers.PassengerQueue;
import pl.edu.mimuw.publictransitsystem.stops.LineSegment;
import pl.edu.mimuw.publictransitsystem.stops.TransitLine;

public abstract class Vehicle {
    private final TransitLine line;
    private final String vehicleNumber;
    private final int capacity;
    private TransitLine.DIRECTION direction;
    private final TransitLine.DIRECTION startingDirection;
    private int currentStopID;
    private static long maxID = 0;
    private final PassengerQueue[] passengerQueues;
    private int passengerCount;

    public void endDay() {
        passengerCount = 0;
        for(PassengerQueue passengerQueue : passengerQueues) {
            passengerQueue.clear();
        }
        direction = startingDirection;
        currentStopID = 0;
    }

    // Get next stop on the line
    public LineSegment getLineSegment() {
        return line.getLineSegment(currentStopID, direction);
    }

    // returns an array of at most howMany passengers who want to get out at the current stop
    public Passenger[] getPassengers(int howMany) {
        PassengerQueue pq = passengerQueues[getAbsoluteStopID()];
        int count = Math.min(howMany, pq.currentSize());
        Passenger[] passengers = new Passenger[count];
        for(int i = 0; i < count; ++i) {
            passengers[i] = pq.dequeue();
        }
        passengerCount -= count;
        return passengers;
    }

    // Converts currentStopID to be relative to the forward direction
    private int getAbsoluteStopID() {
        if(direction == TransitLine.DIRECTION.FORWARD) {
            return currentStopID;
        } else {
            return line.getStopCount() - currentStopID - 1;
        }
    }

    public void goToNextStop() {
        int nextStopID = currentStopID + 1;
        if(nextStopID >= line.getStopCount()) {
            direction = TransitLine.oppositeDirection(direction);
            nextStopID = 0;
        }
        currentStopID = nextStopID;
    }

    public int addPassenger(Passenger passenger, int day, int timestamp) {
        int targetStopID = line.getRandomStopFurtherOnLine(currentStopID, direction);
        int waitTime = passenger.getInsideVehicle(day, timestamp, this, line.getStopByID(targetStopID));
        passengerQueues[targetStopID].enqueue(passenger);
        passengerCount++;
        return waitTime;
    }

    public int addPassengers(Passenger[] passengers, int day, int timestamp) {
        int totalWaitTime = 0;
        for(Passenger passenger : passengers) {
            totalWaitTime += addPassenger(passenger, day, timestamp);
        }
        return totalWaitTime;
    }

    protected final long nextID() {
        return maxID++;
    }

    public Vehicle(TransitLine line, int capacity, TransitLine.DIRECTION direction) {
        this.line = line;
        this.capacity = capacity;
        this.direction = direction;
        startingDirection = direction;
        vehicleNumber = String.valueOf(nextID());
        passengerCount = 0;
        passengerQueues = new ArrayPassengerQueue[line.getStopCount()];
        for(int i = 0; i < passengerQueues.length; ++i) {
            passengerQueues[i] = new ArrayPassengerQueue(capacity);
        }
    }

    public int availableSpace() {
        return capacity - passengerCount;
    }

    public abstract String getTypeName();
    public abstract String getDativeName();

    public TransitLine getLine() {
        return line;
    }

    // checks if vehicle is at the proper end of the line (i.e. is ready for a night)
    public boolean isAtLineEnd() {
        return (currentStopID == 0 && direction == startingDirection);
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    @Override
    public String toString() {
        return getTypeName() +
                " nr " + vehicleNumber +
                " linii " + line.getName();
    }
}