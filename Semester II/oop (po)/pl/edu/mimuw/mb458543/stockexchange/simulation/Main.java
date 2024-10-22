package pl.edu.mimuw.mb458543.stockexchange.simulation;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Użycie: java Main <dane symulacji> <liczba tur w symulacji>");
            return;
        }
        int simulationTime;
        try {
            simulationTime = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Liczba tur w symulacji musi być liczbą!");
            throw new IllegalArgumentException();
        }
        if (simulationTime < 0) {
            System.out.println("Liczba tur w symulacji nie może być ujemna!");
            throw new IllegalArgumentException();
        }
        Simulation simulation = new Simulation(args[0], simulationTime);
        simulation.simulate();
        System.out.println(simulation.getResults());
    }
}