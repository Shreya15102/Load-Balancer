import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamForwarder implements Runnable {
    private final InputStream in;
    private final OutputStream out;
    private final String label;

    public StreamForwarder(InputStream in, OutputStream out, String label){
        this.in = in;
        this.out = out;
        this.label = label;
    }
    @Override
    public void run(){
        byte[] buffer = new byte[4096];
        try{
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        }
        catch(IOException e){
            System.out.println(label + e);
        }
        finally{
            try{ in.close(); } catch(IOException ignored){}
            try{ out.close(); } catch(IOException ignored){}
        }
    }
}
