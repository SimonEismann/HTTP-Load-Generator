package power.hipstershop;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HSPowerCommunicatorConnection {

    private final String hostname;
    private final int port;
    private Socket socket = null;
    private volatile double result = 0.0;
    private Thread updateThread = null;

    public HSPowerCommunicatorConnection(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void initialize(){
        if (updateThread == null) {
            try {
                this.socket = new Socket(this.hostname, this.port);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Runnable updateTask = () -> {
                try {
                    InputStream sockIn = this.socket.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(sockIn, StandardCharsets.US_ASCII));
                    OutputStream sockOut = this.socket.getOutputStream();
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sockOut, StandardCharsets.US_ASCII));
                    while (true) {
                        out.write("\n");    // request new result
                        out.flush();
                        String utilString = in.readLine();
                        this.result = Double.parseDouble(utilString);
                        try{
                            Thread.sleep(1000);     // request update every second
                        } catch (InterruptedException ignored) {}
                    }
                } catch (IOException e) {
                    System.err.println("Connection error at power communicator " + this.hostname + "\n" + e.getMessage());
                }
            };
            updateThread = new Thread(updateTask);
            updateThread.setDaemon(true);
            updateThread.start();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Socket close exception for host " + this.hostname + "\n" + e.getMessage());
        }
    }

    public double getCurrentMeasurement(){
        return result;
    }
}
