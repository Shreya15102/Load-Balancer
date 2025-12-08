import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class TcpLoadBalancer {

     private final int listenPort;
     private final BackendRegistry registry;
     private LoadBalancerStrategy strategy;
     private final ExecutorService workerPool;

     public TcpLoadBalancer(int listenPort, BackendRegistry registry, int workerThreads) {
         this.listenPort = listenPort;
         this.registry = registry;
         this.workerPool = Executors.newFixedThreadPool(workerThreads);
     }

     public void start(){

         Thread hc = new Thread(new HealthChecker(registry, 3000));
         hc.setDaemon(true);
         hc.start();
         try (ServerSocket serverSocket = new ServerSocket(listenPort)){

             System.out.println("Load Balancer Listening on port " + listenPort);
             while(true){
                  Socket clientSocket = serverSocket.accept();
                  clientSocket.setSoTimeout(5000);
                  List<InetSocketAddress>healthyBackends = registry.getHealthyBackends();
                  if (healthyBackends.isEmpty()){
                      System.err.println("No available healthy backends");
                      clientSocket.close();
                      continue;
                  }
                  System.out.println("Accepted client connection from " + clientSocket.getInetAddress().getHostName());
                  InetSocketAddress backend = this.strategy.selectBackend(healthyBackends);
                  System.out.println("Routing to backend: " + backend);
                  workerPool.submit(new ConnectionHandler(clientSocket, backend));
             }

         }
         catch(Exception e){
             System.out.println("Server Error: " + e);
         }

     }

     private void setStrategy(LoadBalancerStrategy strategy){
          this.strategy = strategy;
     }


     public static void main(String[] args){
         BackendRegistry registry = new BackendRegistry();
         registry.addBackend("localhost", 9001);
         registry.addBackend("localhost", 9002);
         registry.addBackend("localhost", 9003);
         TcpLoadBalancer lb = new TcpLoadBalancer(9000, registry, 50);
         LoadBalancerStrategy strategy = new RoundRobinStrategy();
         lb.setStrategy(strategy);
         lb.start();
     }

};
