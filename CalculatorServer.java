import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorServer {
    public static void main(String[] args) {
        final int port = 1099;
        final String name = "CalculatorService";
        try {
            // Start an in-process registry if not already running
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.println("RMI registry started on port " + port);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(port);
                System.out.println("Using existing RMI registry on port " + port);
            }

            Calculator service = new CalculatorImplementation();
            registry.rebind(name, service);
            System.out.println("Bound " + name + " — server ready.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
