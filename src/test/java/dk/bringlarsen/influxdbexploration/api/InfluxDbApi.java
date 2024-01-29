package dk.bringlarsen.influxdbexploration.api;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import org.testcontainers.containers.InfluxDBContainer;

public abstract class InfluxDbApi {

    final InfluxDBContainer<?> influxDBContainer;

    InfluxDbApi(InfluxDBContainer<?> influxDBContainer) {
        this.influxDBContainer = influxDBContainer;
    }

    public InfluxDBClient createClient() {
        final InfluxDBClientOptions influxDBClientOptions = InfluxDBClientOptions
                .builder()
                .url(influxDBContainer.getUrl())
                .authenticate(influxDBContainer.getUsername(), influxDBContainer.getPassword().toCharArray())
                .bucket(influxDBContainer.getBucket())
                .org(influxDBContainer.getOrganization())
                .build();
        return InfluxDBClientFactory.create(influxDBClientOptions);
    }
}