import java.net.InetSocketAddress;
import java.util.List;

public class LeastActiveConnectionsStrategy implements LoadBalancerStrategy{

    InetSocketAddress backend = null;
    Integer minConnections = Integer.MAX_VALUE;

    public InetSocketAddress selectBackend(BackendRegistry registry){
         List<InetSocketAddress>healthyBackends = registry.getHealthyBackends();
         for(InetSocketAddress backend: healthyBackends){
             if(registry.getActiveConnections(backend) < minConnections){
                 minConnections = registry.getActiveConnections(backend);
                 this.backend = backend;
             }
         }
         return this.backend;
    }
}
