package dk.bringlarsen.influxdbexploration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import org.testcontainers.containers.InfluxDBContainer;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public class InfluxDbWriteApi extends InfluxDbApi {
    InfluxDbWriteApi(InfluxDBContainer<?> influxDBContainer) {
        super(influxDBContainer);
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
