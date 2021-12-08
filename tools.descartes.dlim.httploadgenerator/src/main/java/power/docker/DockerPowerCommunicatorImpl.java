package power.docker;

import tools.descartes.dlim.httploadgenerator.power.IPowerCommunicator;

import java.io.IOException;

public class DockerPowerCommunicatorImpl implements IPowerCommunicator {

    private final String containerId;
    private String hostname;
    private DockerPowerCommunicatorConnection conn;

    public DockerPowerCommunicatorImpl(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public void initializePowerCommunicator(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.conn = new DockerPowerCommunicatorConnection(this.hostname, port, this.containerId);
    }

    @Override
    public double getPowerMeasurement() {
        return this.conn.getMeasurement();
    }

    @Override
    public void stopCommunicator() {}

    @Override
    public void run() {}

    public String getCommunicatorName() {
        return "Utilization of " + this.hostname + "/" + containerId;
    }
}
