package pl.edu.mimuw.publictransitsystem.passengers;

import java.util.NoSuchElementException;

public class ArrayPassengerQueue implements PassengerQueue {
    private final int size;
    private int firstElement;
    private int firstEmpty;
    private final Passenger[] passengers;
    private int elementsCount;

    @Override
    public void enqueue(Passenger p) {
        if(elementsCount == size) {
            throw new IllegalStateException("Próba dodania pasażera do pełnej kolejki!");
        }
        passengers[firstEmpty++] = p;
        if(firstEmpty == size) {
            firstEmpty = 0;
        }
        elementsCount++;
    }

    @Override
    public Passenger dequeue() {
        if(isEmpty()) throw new NoSuchElementException("Próba pobrania pasażera z pustej kolejki!");
        Passenger p = passengers[firstElement];
        firstElement++;
        if(firstElement == size) {
            firstElement = 0;
        }
        elementsCount--;
        return p;
    }

    @Override
    public boolean isEmpty() {
        return elementsCount == 0;
    }

    @Override
    public void clear() {
        firstElement = 0;
        firstEmpty = 0;
        elementsCount = 0;
    }

    @Override
    public int currentSize() {
        return elementsCount;
    }

    public ArrayPassengerQueue(int size) {
        this.size = size;
        firstElement = 0;
        firstEmpty = 0;
        passengers = new Passenger[size];
        elementsCount = 0;
    }
}
