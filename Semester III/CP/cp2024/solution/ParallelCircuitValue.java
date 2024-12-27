package cp2024.solution;

import cp2024.circuit.CircuitValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ParallelCircuitValue implements CircuitValue {
    private final Future<Boolean> res;

    @Override
    public boolean getValue() throws InterruptedException {
        try {
            return res.get();
        } catch (ExecutionException e) {
            throw new InterruptedException();
        }
    }

    public ParallelCircuitValue(Future<Boolean> result) {
        res = result;
    }
}
