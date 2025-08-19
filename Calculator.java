import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {
    void pushValue(int val) throws RemoteException;

    /**
     * Push an operator ("min", "max", "lcm", "gcd").
     * Semantics: atomically pop ALL current values on the stack,
     * compute the reduction per operator, then push the single result.
     */
    void pushOperation(String operator) throws RemoteException;

    int pop() throws RemoteException;

    boolean isEmpty() throws RemoteException;

    /**
     * Sleep for 'millis' milliseconds, then perform pop() and return the result.
     * Sleep is done WITHOUT holding the lock to avoid blocking other clients.
     */
    int delayPop(int millis) throws RemoteException;
}