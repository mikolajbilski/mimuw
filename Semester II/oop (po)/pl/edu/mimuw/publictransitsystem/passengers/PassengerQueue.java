package pl.edu.mimuw.publictransitsystem.passengers;

public interface PassengerQueue {
    void enqueue(Passenger e);
    Passenger dequeue();
    boolean isEmpty();
    void clear();
    int currentSize();
}
