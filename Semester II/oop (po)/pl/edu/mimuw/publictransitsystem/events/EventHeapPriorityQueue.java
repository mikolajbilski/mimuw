package pl.edu.mimuw.publictransitsystem.events;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class EventHeapPriorityQueue implements EventPriorityQueue {
    private Event[] heap;
    private int size;

    @Override
    public void enqueue(Event e) {
        if(size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
        heap[size] = e;
        swim();
        size++;
    }

    @Override
    public void enqueue(Event[] events) {
        for(Event e : events) {
            enqueue(e);
        }
    }

    @Override
    public Event getMin() {
        if(isEmpty()) throw new NoSuchElementException("Próba pobrania zdarzenia z pustej kolejki!");
        Event res = heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        sink();
        if(size * 4 < heap.length) {
            heap = Arrays.copyOf(heap, Math.max(size * 2, 1));
        }
        return res;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        size = 0;
        heap = new Event[1];
    }

    // Swaps heap[i] with heap[j]
    private void swap(int i, int j) {
        Event temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void swim() {
        int curr = size;
        int parent = (curr - 1) / 2;
        while(curr > 0 && heap[curr].compareTo(heap[parent]) < 0) {
            swap(curr, parent);
            curr = parent;
            parent = (curr - 1) / 2;
        }
    }

    private void sink() {
        int curr = 0;
        int childA = 1;
        int childB = 2;
        boolean flag = true;
        while(flag) {
            flag = false;
            int currMin = curr;
            if(childA < size && heap[childA].compareTo(heap[currMin]) < 0) {
                currMin = childA;
            }
            if(childB < size && heap[childB].compareTo(heap[currMin]) < 0) {
                currMin = childB;
            }
            if(currMin != curr) {
                flag = true;
                swap(curr, currMin);
                curr = currMin;
                childA = curr * 2 + 1;
                childB = curr * 2 + 2;
            }
        }
    }

    public EventHeapPriorityQueue() {
        heap = new Event[1];
        size = 0;
    }
}
