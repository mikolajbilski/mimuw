package cp2024.solution;

import cp2024.circuit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelCircuitSolver implements CircuitSolver {
    private final ExecutorService executor;
    private final AtomicBoolean stopped;

    private boolean solveRecurrently(CircuitNode node) throws InterruptedException {
        if (stopped.get())
            throw new InterruptedException();

        if (node.getType() == NodeType.LEAF)
            return ((LeafNode) node).getValue();

        CircuitNode[] args = node.getArgs();

        return switch (node.getType()) {
            case GT -> solveGT(args, ((ThresholdNode) node).getThreshold());
            case LT -> solveLT(args, ((ThresholdNode) node).getThreshold());
            case AND -> solveAND(args);
            case OR -> solveOR(args);
            case NOT -> solveNOT(args);
            case IF -> solveIF(args);
            default -> throw new RuntimeException("Illegal type " + node.getType());
        };
    }

    private boolean solveNOT(CircuitNode[] args) throws InterruptedException {
        return !solveRecurrently(args[0]);
    }

    private boolean solveLT(CircuitNode[] args, int threshold) throws InterruptedException {
        return !solveGT(args, threshold - 1);
    }

    private boolean solveGT(CircuitNode[] args, int threshold) throws InterruptedException {
        if (threshold < 0) return true;
        if (threshold >= args.length) return false;

        ExecutorCompletionService<Boolean> ex = new ExecutorCompletionService<>(executor);

        Collection<Future<Boolean>> futures = new ArrayList<>(args.length);

        for (CircuitNode node : args)
            futures.add(ex.submit(() -> solveRecurrently(node)));

        int trues = 0;
        int falses = 0;

        for (int i = 0; i < args.length; ++i) {
            try {
                if (ex.take().get()) {
                    ++trues;
                    if (trues > threshold) {
                        cancelFutures(futures);
                        return true;
                    }
                } else {
                    ++falses;
                    if (falses >= args.length - threshold) {
                        cancelFutures(futures);
                        return false;
                    }
                }
            } catch (Exception e) {
                cancelFutures(futures);
                throw new InterruptedException();
            }
        }

        return false;
    }

    private boolean solveOR(CircuitNode[] args) throws InterruptedException {
        return solveGT(args, 0);
    }

    private boolean solveAND(CircuitNode[] args) throws InterruptedException {
        return solveGT(args, args.length - 1);
    }

    private boolean solveIF(CircuitNode[] args) throws InterruptedException {
        ExecutorCompletionService<Boolean> ex = new ExecutorCompletionService<>(executor);

        List<Future<Boolean>> futures = new ArrayList<>(args.length);

        for (CircuitNode node : args)
            futures.add(ex.submit(() -> solveRecurrently(node)));

        try {
            Future<Boolean> future = ex.take();
            if (future == futures.getFirst())
                return ifCancelAndReturn(futures);

            boolean firstBranch = future.get();

            future = ex.take();
            if (future == futures.getFirst())
                return ifCancelAndReturn(futures);

            boolean secondBranch = future.get();

            if (firstBranch == secondBranch) {
                futures.getFirst().cancel(true);
                return firstBranch;
            }

            return futures.getFirst().get() ? futures.get(1).get() : futures.get(2).get();
        } catch (Exception e) {
            cancelFutures(futures);
            throw new InterruptedException();
        }
    }

    private void cancelFutures(Collection<Future<Boolean>> futures) {
        for (Future<Boolean> f : futures)
            f.cancel(true);
    }

    private boolean ifCancelAndReturn(List<Future<Boolean>> futures) throws ExecutionException, InterruptedException {
        if (futures.getFirst().get()) {
            futures.get(2).cancel(true);
            return futures.get(1).get();
        } else {
            futures.get(1).cancel(true);
            return futures.get(2).get();
        }
    }

    @Override
    public CircuitValue solve(Circuit c) {
        if (stopped.get())
            return new BrokenCircuitValue();
        return new ParallelCircuitValue(executor.submit(() -> solveRecurrently(c.getRoot())));
    }

    @Override
    public void stop() {
        stopped.set(true);
        executor.shutdownNow();
    }

    public ParallelCircuitSolver() {
        executor = newCachedThreadPool();
        stopped = new AtomicBoolean(false);
    }
}
