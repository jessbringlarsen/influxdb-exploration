package dk.bringlarsen.influxdbexploration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import org.testcontainers.containers.InfluxDBContainer;

import java.util.List;

class InfluxDbQueryApi extends InfluxDbApi {

    InfluxDbQueryApi(InfluxDBContainer<?> influxDBContainer) {
        super(influxDBContainer);
    }

    List<PerformanceMeasurement> executeQuery(String query) {
        try (InfluxDBClient client = createClient()) {
            QueryApi queryApi = client.getQueryApi();
            return queryApi.query(query, PerformanceMeasurement.class);
        }
    }

    List<PerformanceMeasurement> executeQuery(Flux query) {
        return executeQuery(query.toString());
    }

    List<FluxTable> executeNativeQuery(String query) {
        try (InfluxDBClient client = createClient()) {
            QueryApi queryApi = client.getQueryApi();
            return queryApi.query(query);
        }
    }
}
