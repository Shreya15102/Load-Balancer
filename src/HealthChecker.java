import java.net.InetSocketAddress;
import java.net.Socket;

public class HealthChecker implements Runnable{

    private final BackendRegistry registry;
    private final int interval;


    public HealthChecker(BackendRegistry registry, int interval){
        this.registry = registry;
        this.interval = interval;
    }

    @Override
    public void run(){
        while(true){
            try{
                for(InetSocketAddress backend: registry.getAllBackends()){
                    Boolean isAlive = isBackendAlive(backend);
                    if (isAlive)
                        registry.markHealthy(backend);
                    else
                        registry.markUnhealthy(backend);
                    System.out.println("Health Check: " + backend + " is " + (isAlive ? "UP" : "DOWN"));
                }
                Thread.sleep(interval);
            }
            catch (Exception e){
                System.err.println("Health checker error: " + e.getMessage());
            }
        }
    }

    private boolean isBackendAlive(InetSocketAddress backend){
        try(Socket socket = new Socket()){
            socket.connect(backend, 1000);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }
}
