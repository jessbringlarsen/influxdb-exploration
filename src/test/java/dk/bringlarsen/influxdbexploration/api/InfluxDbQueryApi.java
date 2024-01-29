package dk.bringlarsen.influxdbexploration.api;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import dk.bringlarsen.influxdbexploration.PerformanceMeasurement;
import org.testcontainers.containers.InfluxDBContainer;

import java.util.List;

public class InfluxDbQueryApi extends InfluxDbApi {

    public InfluxDbQueryApi(InfluxDBContainer<?> influxDBContainer) {
        super(influxDBContainer);
    }

    public List<PerformanceMeasurement> executeQuery(String query) {
        try (InfluxDBClient client = createClient()) {
            QueryApi queryApi = client.getQueryApi();
            return queryApi.query(query, PerformanceMeasurement.class);
        }
    }

    public List<PerformanceMeasurement> executeQuery(Flux query) {
        return executeQuery(query.toString());
    }

    public List<FluxTable> executeNativeQuery(String query) {
        try (InfluxDBClient client = createClient()) {
            QueryApi queryApi = client.getQueryApi();
            return queryApi.query(query);
        }
    }
}
