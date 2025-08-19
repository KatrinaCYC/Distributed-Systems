import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    // Shared stack for ALL clients (required by spec)
    private final Deque<Integer> stack = new ArrayDeque<>();
    // Fair lock to reduce starvation under contention
    private final ReentrantLock lock = new ReentrantLock(true);

    public CalculatorImplementation() throws RemoteException { super(); }

    @Override
    public void pushValue(int val) throws RemoteException {
        lock.lock();
        try {
            stack.push(val);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void pushOperation(String operator) throws RemoteException {
        // Normalize operator
        String op = operator == null ? "" : operator.trim().toLowerCase();
        lock.lock();
        try {
            if (stack.isEmpty()) {
                throw new RemoteException("pushOperation on empty stack");
            }
            // Drain all values atomically
            Deque<Integer> drained = new ArrayDeque<>();
            while (!stack.isEmpty()) drained.push(stack.pop());

            int result;
            switch (op) {
                case "min":
                    result = drained.stream().min(Integer::compareTo).get();
                    break;
                case "max":
                    result = drained.stream().max(Integer::compareTo).get();
                    break;
                case "gcd":
                    result = reduceGcd(drained);
                    break;
                case "lcm":
                    result = reduceLcm(drained);
                    break;
                default:
                    throw new RemoteException("Unsupported operator: " + operator);
            }

            stack.push(result);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int pop() throws RemoteException {
        lock.lock();
        try {
            if (stack.isEmpty()) throw new RemoteException("pop on empty stack");
            return stack.pop();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() throws RemoteException {
        lock.lock();
        try {
            return stack.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int delayPop(int millis) throws RemoteException {
        // Sleep OUTSIDE the lock to avoid blocking other clients
        try {
            Thread.sleep(Math.max(0, millis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted during delayPop sleep", e);
        }
        return pop();
    }

    // --- helpers ---

    private static int gcd(int a, int b) {
        a = Math.abs(a); b = Math.abs(b);
        while (b != 0) { int t = a % b; a = b; b = t; }
        return a;
    }

    private static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a / gcd(a, b) * b);
    }

    private static int reduceGcd(Deque<Integer> values) {
        Integer acc = null;
        for (int v : values) acc = (acc == null) ? v : gcd(acc, v);
        if (acc == null) throw new NoSuchElementException("No values for gcd");
        return acc;
        // Note: gcd over one value is that value; over zero is undefined (guarded by empty check).
    }

    private static int reduceLcm(Deque<Integer> values) {
        Integer acc = null;
        for (int v : values) acc = (acc == null) ? v : lcm(acc, v);
        if (acc == null) throw new NoSuchElementException("No values for lcm");
        return acc;
    }
}
