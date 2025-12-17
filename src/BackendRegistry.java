import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackendRegistry {

    private final Map<InetSocketAddress, Boolean> healthMap = new ConcurrentHashMap<>();
    private final Map<InetSocketAddress, Integer> activeConnections = new ConcurrentHashMap<>();

    public synchronized void addBackend(String host, int port){
        InetSocketAddress address = new InetSocketAddress(host, port);
        healthMap.put(address, true);
        activeConnections.put(address, 0);
    }

    public List<InetSocketAddress> getHealthyBackends(){
          List<InetSocketAddress>healthyBackends = new ArrayList<>();
          for(InetSocketAddress server: healthMap.keySet()){
              if(healthMap.get(server)) healthyBackends.add(server);
          }
          return healthyBackends;
    }

    public void markHealthy(InetSocketAddress address){
        healthMap.put(address, true);
    }

    public void markUnhealthy(InetSocketAddress address){
        healthMap.put(address, false);
    }

    public void incrementActiveConnections(InetSocketAddress address){
            activeConnections.compute(address, (k,v) -> {
                if(v == null) return 1;
                return v + 1;
            });
    }

    public void decrementActiveConnections(InetSocketAddress address){
        activeConnections.compute(address, (k,v) -> {
              if(v == null || v <= 1)return 0;
              return v-1;
        });
    }

    public Integer getActiveConnections(InetSocketAddress address){
        return activeConnections.getOrDefault(address, 0);
    }

    public List<InetSocketAddress> getAllBackends(){
        return new ArrayList<>(healthMap.keySet());
    }


}
