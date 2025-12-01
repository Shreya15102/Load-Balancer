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
     private final List<InetSocketAddress> backendServers;
     private final AtomicInteger rIndex = new AtomicInteger(0);

     private final ExecutorService workerPool = Executors.newFixedThreadPool(50);

     public TcpLoadBalancer(int listenPort, List<InetSocketAddress>backendServers) {
         this.listenPort = listenPort;
         this.backendServers = backendServers;
     }

     public void start(){
         try (ServerSocket serverSocket = new ServerSocket(listenPort)){

             System.out.println("Load Balancer Listening on port " + listenPort);
             while(true){
                  Socket clientSocket = serverSocket.accept();
                  clientSocket.setSoTimeout(5000);
                  System.out.println("Accepted client connection from " + clientSocket.getInetAddress().getHostName());
                  InetSocketAddress backend = getNextBackend();
                  workerPool.submit(new ConnectionHandler(clientSocket, backend.getHostName(), backend.getPort()));
             }
         }
         catch(Exception e){
             System.out.println("Server Error: " + e);
         }

     }

     private InetSocketAddress getNextBackend(){
         int index = rIndex.getAndIncrement() % backendServers.size();
         System.out.println("New Backend Server: " + backendServers.get(index));
         return backendServers.get(index);
     }

     public static void main(String[] args){
         List<InetSocketAddress> backendServers = List.of(
                 new InetSocketAddress("localhost", 9001),
                 new InetSocketAddress("localhost", 9002)
                 );
         TcpLoadBalancer lb = new TcpLoadBalancer(9000, backendServers);
         lb.start();
     }

};
