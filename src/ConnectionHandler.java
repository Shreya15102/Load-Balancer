import java.io.IOException;
import java.net.*;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final String backendHost;
    private final int backendPort;


    public ConnectionHandler(Socket clientSocket, String backendHost, int backendPort) {
        this.clientSocket = clientSocket;
        this.backendHost = backendHost;
        this.backendPort = backendPort;
    }

    @Override
    public void run(){
        try (Socket backendSocket = new Socket(backendHost, backendPort)){
              Thread t1 = new Thread(new StreamForwarder(
                      clientSocket.getInputStream(),
                      backendSocket.getOutputStream(),
                      "CLIENT -> BACKEND"
              ));
              Thread t2 = new Thread(new StreamForwarder(
                      backendSocket.getInputStream(),
                      clientSocket.getOutputStream(),
                      "BACKEND -> CLIENT"
              ));
              t1.start();
              t2.start();

              t1.join();
              t2.join();
        }
        catch(Exception e){
            System.out.println("Connection Error: " + e.getMessage());
        }
        finally{
            try{ clientSocket.close(); } catch(IOException ignored){}
        }
    }
}
