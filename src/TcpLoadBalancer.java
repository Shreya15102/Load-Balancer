import java.net.ServerSocket;
import java.io.*;
import java.net.Socket;


public class TcpLoadBalancer {

     private final int listenPort;
     private final String backendHost;
     private final int backendPort;

     public TcpLoadBalancer(int listenPort, String backendHost, int backendPort) {
         this.listenPort = listenPort;
         this.backendHost = backendHost;
         this.backendPort = backendPort;
     }

     public void start(){
         try (ServerSocket serverSocket = new ServerSocket(listenPort)){

             System.out.println("Server Listening on port " + listenPort);
             while(true){
                  Socket clientSocket = serverSocket.accept();
                  System.out.println("Accepted client connection from " + clientSocket.getInetAddress().getHostName());
                  new Thread(new ConnectionHandler(clientSocket, this.backendHost, this.backendPort)).start();
             }
         }
         catch(Exception e){
             System.out.println("Server Error: " + e);
         }

     }

     public static void main(String[] args){
         TcpLoadBalancer lb = new TcpLoadBalancer(9000, "localhost", 9001);
         lb.start();
     }

};
