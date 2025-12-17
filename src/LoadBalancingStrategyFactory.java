public class LoadBalancingStrategyFactory{

    public static LoadBalancerStrategy getStrategyFromName(String name){
        switch (name.toUpperCase()){
                case "ROUND_ROBIN": 
                    return new RoundRobinStrategy();
                case "LEAST_ACTIVE_CONNECTIONS":
                    return new LeastActiveConnectionsStrategy();
                default:
                    throw new IllegalArgumentException("Unknown strategy: " + name);
        }        
    }
}