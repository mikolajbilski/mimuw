package pl.edu.mimuw.publictransitsystem.stops;

import pl.edu.mimuw.publictransitsystem.passengers.ArrayPassengerQueue;
import pl.edu.mimuw.publictransitsystem.passengers.Passenger;
import pl.edu.mimuw.publictransitsystem.passengers.PassengerQueue;

public class Stop implements Comparable<Stop>{
    private final String name;
    private final int capacity;
    private final PassengerQueue waiting;
    private int waitingCount;

    public void addPassengerFromVehicle(Passenger passenger, int day, int timestamp) {
        addPassenger(passenger);
        passenger.getOutOfVehicle(day, timestamp, this);
    }

    public int endDay() {
        int totalWaitingTime = 0;
        while(!waiting.isEmpty()) {
            Passenger p = waiting.dequeue();
            int waitTime = p.getWaitingTime(24 * 60);
            totalWaitingTime += waitTime;
        }
        waitingCount = 0;
        return totalWaitingTime;
    }

    public void addPassenger(Passenger passenger) {
        if(waitingCount == capacity) {
            throw new IllegalStateException("Próba wejścia pasażera na pełen przystanek!");
        }
        waiting.enqueue(passenger);
        waitingCount++;
    }

    public void addPassengersFromVehicle(Passenger[] passengers, int day, int timestamp) {
        for(Passenger p : passengers) {
            addPassengerFromVehicle(p, day, timestamp);
        }
    }

    public int availableSpace() {
        return capacity - waitingCount;
    }

    // Returns howMany passengers (or less, if there are less waiting) and removes them from the stop
    public Passenger[] getPassengers(int howMany) {
        int count = Math.min(howMany, waitingCount);
        waitingCount -= count;
        Passenger[] passengers = new Passenger[count];
        for(int i = 0; i < count; ++i) {
            passengers[i] = waiting.dequeue();
        }
        return passengers;
    }

    public Stop(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        waiting = new ArrayPassengerQueue(capacity);
        waitingCount = 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Stop o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}