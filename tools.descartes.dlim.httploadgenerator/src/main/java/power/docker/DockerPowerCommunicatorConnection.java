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
import java.util.stream.Collectors;

public class DockerPowerCommunicatorConnection {

    // https://docs.docker.com/engine/api/v1.41/#operation/ContainerStats

    private String url;

    public DockerPowerCommunicatorConnection(String hostname, int port, String containerId) {
        this.url = "http://" + hostname + ":" + port + "/v1.41/containers/" + containerId + "/stats";
    }


    public double getMeasurement() {
        try{
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            InputStream responseStream = connection.getInputStream();
            String json = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            JsonObject jobj = new JsonParser().parse(json).getAsJsonObject();
            JsonObject cpu_stats = jobj.getAsJsonObject("cpu_stats");
            JsonObject precpu_stats = jobj.getAsJsonObject("precpu_stats");
            // calculate cpu usage % (not times 100) according to API doc
            int number_cpus = cpu_stats.get("online_cpus").getAsInt();
            long cpu_delta = cpu_stats.getAsJsonObject("cpu_usage").get("total_usage").getAsLong() - precpu_stats.getAsJsonObject("cpu_usage").get("total_usage").getAsLong();
            long system_cpu_delta = cpu_stats.get("system_cpu_usage").getAsLong() - precpu_stats.get("system_cpu_usage").getAsLong();
            return  ((double)cpu_delta / system_cpu_delta) * number_cpus;
        } catch (IOException e) {
            return -1;
        }
    }

}
