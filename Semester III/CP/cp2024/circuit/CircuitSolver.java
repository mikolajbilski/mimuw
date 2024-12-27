package cp2024.circuit;

public interface CircuitSolver {
    CircuitValue solve(Circuit c);

    void stop();
}
