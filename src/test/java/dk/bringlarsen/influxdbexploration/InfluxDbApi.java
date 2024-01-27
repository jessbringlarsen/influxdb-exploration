package dk.bringlarsen.influxdbexploration;

import com.influxdb.client.*;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import org.testcontainers.containers.InfluxDBContainer;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

class InfluxDbApi {

    private final InfluxDBContainer<?> influxDBContainer;

    InfluxDbApi(InfluxDBContainer<?> influxDBContainer) {
        this.influxDBContainer = influxDBContainer;
    }

    InfluxDBClient createClient() {
        final InfluxDBClientOptions influxDBClientOptions = InfluxDBClientOptions
                .builder()
                .url(influxDBContainer.getUrl())
                .authenticate(influxDBContainer.getUsername(), influxDBContainer.getPassword().toCharArray())
                .bucket(influxDBContainer.getBucket())
                .org(influxDBContainer.getOrganization())
                .build();
        return InfluxDBClientFactory.create(influxDBClientOptions);
    }

    void cleanAndWrite(PerformanceMeasurement... performanceMeasurements) {
        clean();
        write(performanceMeasurements);
    }

    void write(List<PerformanceMeasurement> performanceMeasurements) {
        try (InfluxDBClient client = createClient()) {
            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            for (PerformanceMeasurement performanceMeasurement : performanceMeasurements) {
                writeApi.writeMeasurement(WritePrecision.NS, performanceMeasurement);
            }
        }
    }

    void write(PerformanceMeasurement... performanceMeasurements) {
        write(Arrays.asList(performanceMeasurements));
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

    void clean() {
        try (InfluxDBClient client = createClient()) {
            client.getDeleteApi().delete(
                    new DeletePredicateRequest()
                            .start(OffsetDateTime.parse("2001-12-03T10:15:30+01:00"))
                            .stop(OffsetDateTime.parse("2099-12-03T10:15:30+01:00")),
                    influxDBContainer.getBucket(),
                    influxDBContainer.getOrganization());
        }
    }
}