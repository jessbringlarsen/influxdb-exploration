package dk.bringlarsen.springshellexploration;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;

@Testcontainers
class SpringshellExplorationApplicationTest {

    @Container
    final InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.0.7"));

    @Test
    void test() {
        try (InfluxDBClient influxDBClient = createClient(influxDBContainer)) {
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

            writeApi.writeMeasurement(WritePrecision.MS, createTemprature(2, "south"));
            writeApi.writeMeasurement(WritePrecision.MS, createTemprature(4, "north"));
            writeApi.writeMeasurement(WritePrecision.MS, createTemprature(6, "south"));

            String flux = String.format("from(bucket:\"%s\") |> range(start: 0)", influxDBContainer.getBucket());
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux);
            for (FluxTable fluxTable : tables) {
                List<FluxRecord> records = fluxTable.getRecords();
                for (FluxRecord fluxRecord : records) {
                    System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                }
            }
        }
    }

    @NotNull
    private static Temperature createTemprature(int value, String location) {
        Temperature temperature = new Temperature();
        temperature.location = location;
        temperature.value = Double.valueOf(value);
        temperature.time = Instant.now();
        return temperature;
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }

    public static InfluxDBClient createClient(final InfluxDBContainer<?> influxDBContainer) {
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