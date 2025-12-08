import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackendRegistry {

    private final Map<InetSocketAddress, Boolean>backends = new ConcurrentHashMap<>();
    public void addBackend(String host, int port){
        backends.put(new InetSocketAddress(host, port), true);
    }

    public List<InetSocketAddress> getHealthyBackends(){
          List<InetSocketAddress>healthyBackends = new ArrayList<>();
          for(InetSocketAddress server: backends.keySet()){
              if(backends.get(server)) healthyBackends.add(server);
          }
          return healthyBackends;
    }

    public void setHealthyStatus(InetSocketAddress backend, Boolean healthyStatus){
        if(backends.containsKey(backend)){
            backends.put(backend, healthyStatus);
        }
    }

    public Boolean isHealthy(InetSocketAddress backend){
            return backends.getOrDefault(backend, false);
    }

    public Set<InetSocketAddress> getAllBackends(){
        return backends.keySet();
    }
}
