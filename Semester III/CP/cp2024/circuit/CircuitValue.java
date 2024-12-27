package cp2024.circuit;

/** Interface for a deferred (future) value returned by a CircuitSolver. */
public interface CircuitValue {
    boolean getValue() throws InterruptedException;
}
