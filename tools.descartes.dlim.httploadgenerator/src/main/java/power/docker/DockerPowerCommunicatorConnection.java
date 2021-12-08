package power.docker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DockerPowerCommunicatorConnection {

    // https://docs.docker.com/engine/api/v1.41/#operation/ContainerStats

    private final String url;
    private double util = 0;

    public DockerPowerCommunicatorConnection(String hostname, int port, String containerId) {
        this.url = "http://" + hostname + ":" + port + "/v1.41/containers/" + containerId + "/stats";
        Runnable updateTask = () -> {
            boolean firstRequest = true;    // on the first request, precpu_stats is empty --> NullPointerException
            while (true) {
                try{
                    URL url = new URL(this.url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream responseStream = connection.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
                    while (true) {
                        String text = in.readLine();
                        if (firstRequest) {
                            firstRequest = false;
                        } else {
                            JsonObject jobj = new JsonParser().parse(text).getAsJsonObject();
                            JsonObject cpu_stats = jobj.getAsJsonObject("cpu_stats");
                            JsonObject precpu_stats = jobj.getAsJsonObject("precpu_stats");
                            // calculate cpu usage % (not times 100) according to API doc
                            int number_cpus = cpu_stats.get("online_cpus").getAsInt();
                            long cpu_delta = cpu_stats.getAsJsonObject("cpu_usage").get("total_usage").getAsLong() - precpu_stats.getAsJsonObject("cpu_usage").get("total_usage").getAsLong();
                            long system_cpu_delta = cpu_stats.get("system_cpu_usage").getAsLong() - precpu_stats.get("system_cpu_usage").getAsLong();
                            this.util =  ((double)cpu_delta / system_cpu_delta) * number_cpus;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public double getMeasurement() {
        return this.util;
    }

}
