package pl.edu.mimuw.publictransitsystem.general;

import pl.edu.mimuw.publictransitsystem.events.*;
import pl.edu.mimuw.publictransitsystem.passengers.Passenger;
import pl.edu.mimuw.publictransitsystem.stops.LineSegment;
import pl.edu.mimuw.publictransitsystem.stops.Stop;
import pl.edu.mimuw.publictransitsystem.stops.TramLine;
import pl.edu.mimuw.publictransitsystem.stops.TransitLine;

import java.util.Arrays;
import java.util.Scanner;

public class Simulation {
    // Parametry symulacji
    private int simulationDays; // Mierzone w dniach
    private int stopCapacity;
    private int stopsCount;
    private int passengerCount;
    private int tramCapacity;
    private int lineCount;
    private Stop[] stops;
    private TransitLine[] lines;
    private final EventPriorityQueue queue;
    private Passenger[] passengers;
    private final Accumulator waitCounter;
    private int[] totalWaitingTimes;
    private int[] totalRides;

    // Wczytuje dane symulacji
    private void loadSimulationData() {
        Scanner scanner = new Scanner(System.in);
        boolean fail = false;
        try {
            simulationDays = scanner.nextInt();
            if(simulationDays < 0) {
                throw new IllegalArgumentException("Liczba dni musi być nieujemna!");
            }
            totalWaitingTimes = new int[simulationDays];
            totalRides = new int[simulationDays];

            stopCapacity = scanner.nextInt();
            if(stopCapacity < 0) {
                throw new IllegalArgumentException("Pojemność przystanku musi być nieujemna!");
            }

            stopsCount = scanner.nextInt();
            if(stopsCount < 0) {
                throw new IllegalArgumentException("Liczba przystanków musi być nieujemna!");
            }

            stops = new Stop[stopsCount];
            for(int i = 0; i < stopsCount; ++i) {
                String stopName = scanner.next();
                stops[i] = new Stop(stopName, stopCapacity);
            }
            Arrays.sort(stops);

            passengerCount = scanner.nextInt();
            if(passengerCount < 0) {
                throw new IllegalArgumentException("Liczba pasażerów musi być nieujemna!");
            }
            passengers = new Passenger[passengerCount];
            for(int i = 0; i < passengerCount; ++i) {
                passengers[i] = new Passenger(stops);
            }

            tramCapacity = scanner.nextInt();
            if(tramCapacity < 0) {
                throw new IllegalArgumentException("Pojemność tramwaju musi być nieujemna!");
            }

            lineCount = scanner.nextInt();
            if(lineCount < 0) {
                throw new IllegalArgumentException("Liczba linii musi być nieujemna!");
            }

            lines = new TransitLine[lineCount];
            for(int i = 0; i < lineCount; ++i) {
                int tramCount = scanner.nextInt();
                if(tramCount < 0) {
                    throw new IllegalArgumentException("Liczba tramwajów na linii musi być nieujemna!");
                }
                int lineLength = scanner.nextInt();
                if(lineLength <= 1) {
                    throw new IllegalArgumentException("Na linii muszą być co najmniej 2 przystanki!");
                }
                LineSegment[] segments = new LineSegment[lineLength];
                for(int j = 0; j < lineLength; ++j) {
                    String stopName = scanner.next();
                    Stop searchedStop = new Stop(stopName, stopCapacity);
                    int segmentLength = scanner.nextInt();
                    int stopID = Arrays.binarySearch(stops, searchedStop);
                    if(stopID < 0) {
                        throw new IllegalArgumentException("Na linii znajduje się nieistniejący przystanek!");
                    }
                    LineSegment.TYPE t = LineSegment.TYPE.NORMAL;
                    if(j == 0) {
                        t = LineSegment.TYPE.FIRST;
                    } else if(j == lineLength - 1) {
                        t = LineSegment.TYPE.LAST;
                    }
                    segments[j] = new LineSegment(stops[stopID], segmentLength, t);
                }
                lines[i] = new TramLine(String.valueOf(i), tramCount, segments, tramCapacity);
            }

            scanner.close();
        } catch(Exception e) {
            fail = true;
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        } finally {
            scanner.close();
        }

        if(fail) {
            System.out.println("Niepoprawne dane wejściowe!");
            System.exit(1);
        }

        printSimulationParameters();
    }

    private void printSimulationParameters() {
        System.out.println("----PARAMETRY SYMULACJI----");

        System.out.println("DNI SYMULACJI: " + simulationDays);
        System.out.println("POJEMNOŚĆ PRZYSTANKU: " + stopCapacity);
        System.out.println("LICZBA PRZYSTANKÓW: " + stopsCount);

        System.out.println("---NAZWY PRZYSTANKÓW---");
        for(Stop s : stops) {
            System.out.println(s);
        }
        System.out.println("---KONIEC NAZW PRZYSTANKÓW---");

        System.out.println("LICZBA PASAŻERÓW: " + passengerCount);
        System.out.println("POJEMNOŚĆ TRAMWAJU: " + tramCapacity);
        System.out.println("LICZBA LINII: " + lineCount);

        System.out.println("---DANE LINII---");
        System.out.println("FORMAT: [przystanek], [czas przejazdu do następnego przystanku]");
        System.out.println("Przy ostatnim przystanku podany jest czas postoju na pętli.");
        for(TransitLine l : lines) {
            System.out.println("--LINIA " + l.getName() + "--");
            LineSegment[] forwardSegments = l.getForwardSegments();
            System.out.println("LICZBA TRAMWAJÓW: " + l.getVehicleCount());
            System.out.println("LICZBA PRZYSTANKÓW: " + forwardSegments.length);
            System.out.println("-KIERUNEK " + forwardSegments[0].getStop() + " - " + forwardSegments[forwardSegments.length - 1].getStop() + "-");
            for(LineSegment segment : forwardSegments) {
                System.out.println(segment.getStop() + ", " + segment.getLength());
            }
            LineSegment[] backwardSegments = l.getBackwardSegments();
            System.out.println("-KIERUNEK " + backwardSegments[0].getStop() + " - " + backwardSegments[backwardSegments.length - 1].getStop() + "-");
            for(LineSegment segment : backwardSegments) {
                System.out.println(segment.getStop() + ", " + segment.getLength());
            }
        }
        System.out.println("---KONIEC DANYCH LINII---");

        System.out.println("----KONIEC PARAMETRÓW SYMULACJI----");
    }

    private void startDay(int day) {
        System.out.println("Początek dnia nr " + day);
        PassengerArrivesAtStopEvent[] passengerDepartures = new PassengerArrivesAtStopEvent[passengerCount];
        for(int i = 0; i < passengerCount; ++i) {
            passengerDepartures[i] = new PassengerArrivesAtStopEvent(passengers[i].getDepartureTime(), day, passengers[i]);
        }
        queue.enqueue(passengerDepartures);
        for(TransitLine tramLine : lines) {
            VehicleAtStopEvent[] tramDepartures = tramLine.getMorningDepartures(day, waitCounter);
            queue.enqueue(tramDepartures);
        }
    }

    private void endDay(int day) {
        System.out.println("Koniec dnia nr " + day);
        for(Stop s : stops) {
            waitCounter.add(s.endDay());
        }
        for(TransitLine line : lines) {
            line.endDay();
        }
        int totalRideCount = 0;
        for(Passenger p : passengers) {
            totalRideCount += p.endDay();
        }
        totalWaitingTimes[day - 1] = waitCounter.getValue();
        totalRides[day - 1] = totalRideCount;
        waitCounter.reset();
    }

    private void simulateDay(int day) {
        startDay(day);
        while(!queue.isEmpty()) {
            Event e = queue.getMin();
            if(e.getTimestamp() > 60 * 24) {
                queue.clear();
                break;
            }
            Event next = e.handle();
            if(next != null) {
                queue.enqueue(next);
            }
        }
        endDay(day);
    }

    public void simulate() {
        for(int i = 0; i < simulationDays; ++i) {
            simulateDay(i + 1);
        }
        endSimulation();
    }

    private void endSimulation() {
        System.out.println("----KONIEC SYMULACJI----");
        System.out.println("---STATYSTYKI---");
        for(int i = 0; i < simulationDays; ++i) {
            System.out.println("--DZIEŃ " + (i + 1) + "--");
            System.out.println("ŁĄCZNA LICZBA PRZEJAZDÓW: " + totalRides[i]);
            if(totalRides[i] > 0) {
                float averageWaitTime = (float) totalWaitingTimes[i] / totalRides[i];
                System.out.printf("ŚREDNI CZAS OCZEKIWANIA NA PRZYSTANKU: %.2f minut\n", averageWaitTime);
            }
        }
        System.out.println("-DLA CAŁOŚCI SYMULACJI-");
        int allRides = 0;
        for(int i : totalRides) {
            allRides += i;
        }
        System.out.println("ŁĄCZNA LICZBA PRZEJAZDÓW: " + allRides);
        if(allRides > 0) {
            int totalWaitingTime = 0;
            for(int i : totalWaitingTimes) {
                totalWaitingTime += i;
            }
            float averageWaitTime = (float) totalWaitingTime / allRides;
            System.out.printf("ŚREDNI CZAS OCZEKIWANIA NA PRZYSTANKU: %.2f minut\n", averageWaitTime);
        }
        System.out.println("---KONIEC STATYSTYK---");
    }

    public Simulation() {
        loadSimulationData();
        System.out.println("----POCZĄTEK SYMULACJI----");
        waitCounter = new Accumulator();
        queue = new EventHeapPriorityQueue();
    }
}