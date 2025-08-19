import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CalculatorClient {
    public static void main(String[] args) throws Exception {
        final String host = (args.length > 0) ? args[0] : "localhost";
        final int port = 1099;
        final String name = "CalculatorService";

        Registry registry = LocateRegistry.getRegistry(host, port);
        Calculator calc = (Calculator) registry.lookup(name);

        // --- Functional test: min/max/lcm/gcd over entire stack ---
        // Build stack: [2, 3, 4, 8]
        calc.pushValue(2);
        calc.pushValue(3);
        calc.pushValue(4);
        calc.pushValue(8);
        calc.pushOperation("min");               // min(2,3,4,8) = 2
        System.out.println("min -> " + calc.pop());

        // Rebuild stack
        calc.pushValue(2); calc.pushValue(3); calc.pushValue(4); calc.pushValue(8);
        calc.pushOperation("max");               // max(...) = 8
        System.out.println("max -> " + calc.pop());

        calc.pushValue(4); calc.pushValue(6); calc.pushValue(8);
        calc.pushOperation("gcd");               // gcd(4,6,8) = 2
        System.out.println("gcd -> " + calc.pop());

        calc.pushValue(4); calc.pushValue(6); calc.pushValue(8);
        calc.pushOperation("lcm");               // lcm(4,6,8) = 24
        System.out.println("lcm -> " + calc.pop());

        // --- delayPop smoke test ---
        calc.pushValue(42);
        long t0 = System.currentTimeMillis();
        int v = calc.delayPop(300);
        long dt = System.currentTimeMillis() - t0;
        System.out.println("delayPop -> " + v + " (elapsed ~" + dt + "ms)");

        // --- Simple multi-client stress: 8 concurrent pushers + poppers ---
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();

        // Push 100 values concurrently
        for (int i = 0; i < 100; i++) {
            final int val = i;
            tasks.add(() -> { calc.pushValue(val); return null; });
        }
        // Perform 100 pops concurrently
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> { try { calc.pop(); } catch (Exception ignored) {} return null; });
        }

        for (Future<Void> f : pool.invokeAll(tasks)) f.get();
        pool.shutdown();
        System.out.println("Concurrent push/pop finished. isEmpty=" + calc.isEmpty());
    }
}
