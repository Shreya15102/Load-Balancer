import java.io.*;
import java.net.*;

public class BackendServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Backend running on port: " + port);

        while (true) {
            Socket s = ss.accept();
            System.out.println("Accepted connection from " + s.getInetAddress());
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();

            byte[] buf = new byte[1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(("Echo: " + new String(buf, 0, read)).getBytes());
                out.flush();
            }
            s.close();
        }
    }
}
