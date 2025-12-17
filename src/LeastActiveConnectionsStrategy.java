import java.net.InetSocketAddress;
import java.util.List;

public class LeastActiveConnectionsStrategy implements LoadBalancerStrategy{


    public InetSocketAddress selectBackend(BackendRegistry registry){
        InetSocketAddress bestBackend = null;
        Integer minConnections = Integer.MAX_VALUE;
        List<InetSocketAddress>healthyBackends = registry.getHealthyBackends();
        for(InetSocketAddress backend: healthyBackends){
            if(registry.getActiveConnections(backend) < minConnections){
                minConnections = registry.getActiveConnections(backend);
                bestBackend= backend;
            }
        }
        return bestBackend;
    }
}
