import java.net.InetSocketAddress;
import java.util.*;

public interface LoadBalancerStrategy {
      InetSocketAddress selectBackend(List<InetSocketAddress> backends);
}
