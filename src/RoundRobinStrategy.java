import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalancerStrategy{

     private final AtomicInteger counter = new AtomicInteger(0);


     @Override
     public InetSocketAddress selectBackend(List<InetSocketAddress> backends){
         if(backends.isEmpty())return null;
         return backends.get(Math.floorMod(counter.getAndIncrement(), backends.size()));
     }
}
