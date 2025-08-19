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

        // Functional test: min/max/lcm/gcd over entire stack
        calc.pushValue(8);
        calc.pushValue(9);
        calc.pushValue(15);
        calc.pushValue(25);
        calc.pushOperation("min");               
        System.out.println("min -> " + calc.pop());

        // Rebuild stack
        calc.pushValue(8); calc.pushValue(9); calc.pushValue(15); calc.pushValue(25);
        calc.pushOperation("max");               
        System.out.println("max -> " + calc.pop());

        calc.pushValue(9); calc.pushValue(15); calc.pushValue(25);
        calc.pushOperation("gcd");               
        System.out.println("gcd -> " + calc.pop());

        calc.pushValue(9); calc.pushValue(15); calc.pushValue(25);
        calc.pushOperation("lcm");               
        System.out.println("lcm -> " + calc.pop());

        // delayPop smoke test
        calc.pushValue(225);
        long t0 = System.currentTimeMillis();
        int v = calc.delayPop(300);
        long dt = System.currentTimeMillis() - t0;
        System.out.println("delayPop -> " + v + " (elapsed ~" + dt + "ms)");

        // Simple multi-client stress: 25 concurrent pushers + poppers
        int threads = 25;
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
