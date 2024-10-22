package pl.edu.mimuw.publictransitsystem.events;

import pl.edu.mimuw.publictransitsystem.passengers.Passenger;
import pl.edu.mimuw.publictransitsystem.stops.Stop;

public class PassengerArrivesAtStopEvent extends Event {
    private final Passenger passenger;

    public PassengerArrivesAtStopEvent(int timestamp, int day, Passenger passenger) {
        super(timestamp, day);
        this.passenger = passenger;
    }

    @Override
    public Event handle() {
        Stop s = passenger.getHomeStop();
        if(s.availableSpace() > 0) {
            passenger.startWaiting(timestamp);
            s.addPassenger(passenger);
            System.out.println(this);
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() +
                "Pasażer " +
                passenger.getID() +
                " przyszedł na przystanek " +
                passenger.getHomeStop() + ".";
    }
}
