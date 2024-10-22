package pl.edu.mimuw.publictransitsystem.events;

public interface EventPriorityQueue {
    void enqueue(Event e);
    void enqueue(Event[] events);
    Event getMin();
    boolean isEmpty();
    void clear();
}
