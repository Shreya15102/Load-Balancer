import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalancerStrategy{

     private final AtomicInteger counter = new AtomicInteger(0);

     @Override
     public InetSocketAddress selectBackend(BackendRegistry registry){
         List<InetSocketAddress>healthyBackends = registry.getHealthyBackends();
         if(healthyBackends.isEmpty())return null;
         return healthyBackends.get(Math.floorMod(counter.getAndIncrement(), healthyBackends.size()));
     }
}
