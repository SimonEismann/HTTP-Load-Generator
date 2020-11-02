package hipstershop.power;

import tools.descartes.dlim.httploadgenerator.power.IPowerCommunicator;

import java.io.IOException;

public class PowerCommunicatorImpl implements IPowerCommunicator {

    private String hostname = "localhost";
    private int port = 22442;
    private PowerCommunicatorConnection conn = null;

    @Override
    public void initializePowerCommunicator(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;
        if (conn != null) {
            conn.close();
        }
        this.conn = new PowerCommunicatorConnection(this.hostname, this.port);
        this.conn.initialize();
    }

    @Override
    public double getPowerMeasurement() {
        if (this.conn != null) {
            return this.conn.getCurrentMeasurement();
        } else {
            System.err.println("No measurement available for " + this.hostname);
            return 0;
        }
    }

    @Override
    public void stopCommunicator() {
        this.conn.close();
    }

    @Override
    public void run() {     // ignored by implementation
    }

    public String getCommunicatorName() {
        return "Utilization of " + this.hostname;
    }
}
