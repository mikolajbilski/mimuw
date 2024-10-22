package pl.edu.mimuw.publictransitsystem.passengers;

import pl.edu.mimuw.publictransitsystem.general.Losowanie;
import pl.edu.mimuw.publictransitsystem.general.Utils;
import pl.edu.mimuw.publictransitsystem.stops.Stop;
import pl.edu.mimuw.publictransitsystem.vehicles.Vehicle;

public class Passenger {
    private final long ID;
    private static long maxID = 0;
    private final Stop homeStop;
    private static final int EARLIEST_DEPARTURE = 6 * 60;
    private static final int LATEST_DEPARTURE = 12 * 60;
    private int waitingBeginTime;
    private int totalRides;

    protected final long nextID() {
        return maxID++;
    }

    public Passenger(Stop[] stops) {
        ID = nextID();
        homeStop = stops[Losowanie.losuj(0, stops.length - 1)];
        totalRides = 0;
    }

    // returns the number of total rides for the current day and resets it's count to 0
    public int endDay() {
        int v = totalRides;
        totalRides = 0;
        return v;
    }

    public int getWaitingTime(int timestamp) {
        return timestamp - waitingBeginTime;
    }

    public int getInsideVehicle(int day, int timestamp, Vehicle v, Stop targetStop) {
        String s = Utils.timeToString(day, timestamp) +
                ": Pasażer " + ID + " wsiadł do " + v.getDativeName() + " nr " + v.getVehicleNumber() + " linii " +
                v.getLine().getName() + " z zamiarem dojechania do przystanku " + targetStop + ".";
        System.out.println(s);
        totalRides += 1;
        return getWaitingTime(timestamp);
    }

    public void getOutOfVehicle(int day, int timestamp, Stop stop) {
        String s = Utils.timeToString(day, timestamp) +
                ": Pasażer " + ID + " wysiadł na przystanku " + stop + ".";
        System.out.println(s);
        startWaiting(timestamp);
    }

    public void startWaiting(int timestamp) {
        waitingBeginTime = timestamp;
    }

    public Stop getHomeStop() {
        return homeStop;
    }

    public int getDepartureTime() {
        return Losowanie.losuj(EARLIEST_DEPARTURE, LATEST_DEPARTURE);
    }

    public long getID() {
        return ID;
    }
}
