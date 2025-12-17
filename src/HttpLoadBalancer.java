import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpLoadBalancer{

     private final int listenPort;
     private final BackendRegistry registry;
     private LoadBalancerStrategy strategy;

      public HttpLoadBalancer(int listenPort, BackendRegistry registry, LoadBalancerStrategy strategy){
          this.listenPort = listenPort;
          this.registry = registry;
          this.strategy = strategy;
      }

      public void start()throws IOException{
        Thread hc = new Thread(new HealthChecker(registry, 3000));
        hc.setDaemon(true);
        hc.start();
        HttpServer server = HttpServer.create(new InetSocketAddress(listenPort), 0);
        server.createContext("/", new ProxyHandler());
        server.setExecutor(null);
        server.start();
      }    


    private class ProxyHandler implements HttpHandler{
        
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            List<InetSocketAddress>healthyBackends = registry.getHealthyBackends();
                if(healthyBackends.isEmpty()){
                    String msg = "No available healthy backends";
                    System.err.println(msg);
                    exchange.sendResponseHeaders(503, msg.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()){
                        os.write(msg.getBytes());
                    }
                    return;
            }
            InetSocketAddress backend = strategy.selectBackend(registry);
            System.out.println("Forwading request to" + backend.getHostName() + backend.getPort());
            if(backend == null){
                String msg = "No backend selected";
                exchange.sendResponseHeaders(503, msg.getBytes().length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(msg.getBytes());
                }
                return;
            }
            registry.incrementActiveConnections(backend);
            try{
                proxyToBackend(exchange, backend);
            }
            finally{
                registry.decrementActiveConnections(backend);
            }
        }



        private void proxyToBackend(HttpExchange clientExchange, InetSocketAddress backend)throws IOException{
            String method = clientExchange.getRequestMethod().toUpperCase();
            String pathAndQuery = clientExchange.getRequestURI().toString();
            String backendUrlStr = "http://" + backend.getHostName() + ":" + backend.getPort() + pathAndQuery;
            URL backendUrl = new URL(backendUrlStr);
            HttpURLConnection conn = (HttpURLConnection) backendUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            conn.setDoInput(true);

            for(Map.Entry<String, List<String>> header: clientExchange.getRequestHeaders().entrySet()){
                String name = header.getKey();
                if(name == null)continue;
                if("Host".equalsIgnoreCase(name))continue;
                for(String value: header.getValue()){
                    conn.addRequestProperty(name, value);
                }
            }

            if("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)){
                conn.setDoOutput(true);
                try (InputStream is = clientExchange.getRequestBody();
                    OutputStream os = conn.getOutputStream()){
                        copyStream(is, os);
                    }
            }

            int backendStatus;
            InputStream backendIn;
            try{
                backendStatus = conn.getResponseCode();
                backendIn = conn.getInputStream();
            }
            catch (IOException e){
                backendStatus = conn.getResponseCode();
                backendIn = conn.getErrorStream();
            }

            for(Map.Entry<String, List<String>>header: conn.getHeaderFields().entrySet()){
                String name = header.getKey();
                if(name == null)continue;
                clientExchange.getResponseHeaders().put(name, header.getValue());
            }
            
            clientExchange.sendResponseHeaders(backendStatus, 0);
            try (OutputStream clientOut = clientExchange.getResponseBody()){
                if (backendIn != null){
                    copyStream(backendIn, clientOut);
                }
            }
            finally{
                if(backendIn != null){
                    backendIn.close();
                }
                conn.disconnect();
            }
        }

        private void copyStream(InputStream is, OutputStream os)throws IOException{
            byte[] buffer = new byte[8192];
            int read;
            while((read = is.read(buffer)) != -1){
                os.write(buffer, 0, read);
            }
        }
    }

    public static void main(String[] args) throws IOException{
        BackendRegistry registry = new BackendRegistry();
        registry.addBackend("localhost", 9001);
        registry.addBackend("localhost", 9002);
        registry.addBackend("localhost", 9003);
        LoadBalancerStrategy strategy = new RoundRobinStrategy();
        HttpLoadBalancer lb = new HttpLoadBalancer(8081, registry, strategy);
        lb.start();
    }
};


