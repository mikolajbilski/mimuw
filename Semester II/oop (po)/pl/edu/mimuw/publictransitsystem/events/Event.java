package pl.edu.mimuw.publictransitsystem.events;

import pl.edu.mimuw.publictransitsystem.general.Utils;

public abstract class Event implements Comparable<Event> {
    protected final int day;
    protected final int timestamp;
    private final long ID;
    private static long maxID = 0;

    // returns a new event to be put into event queue or null if there is no event
    public abstract Event handle();

    protected final long nextID() {
        return maxID++;
    }

    public Event(int timestamp, int day) {
        this.timestamp = timestamp;
        this.ID = nextID();
        this.day = day;
    }

    @Override
    public int compareTo(Event o) {
        if(timestamp == o.timestamp) {
            return Long.compare(ID, o.ID);
        }
        return timestamp - o.timestamp;
    }

    @Override
    public String toString() {
        return Utils.timeToString(day, timestamp) + ": ";
    }

    public int getTimestamp() {
        return timestamp;
    }
}
