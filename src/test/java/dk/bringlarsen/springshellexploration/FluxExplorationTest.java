package dk.bringlarsen.springshellexploration;

import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FluxExplorationTest {

    @Container
    final InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.7.5"));

    @Test
    @DisplayName("expect results to be grouped by tag")
    void testCase1() {
        try (InfluxDBClient influxDBClient = createClient(influxDBContainer)) {
            writeTemperature(influxDBClient,
                    Temperature.create().withLocation("south").withTemperature(6),
                    Temperature.create().withLocation("south").withTemperature(2),
                    Temperature.create().withLocation("north").withTemperature(3));

            String query = String.format("""
                    from(bucket: "%s")
                       |> range(start: -5m)
                       |> mean()""", influxDBContainer.getBucket());

            List<Temperature> result = executeQuery(influxDBClient, query);

            assertThat(result)
                    .hasSize(2)
                    .anyMatch(withTemperature(Temperature.create().withLocation("south").withTemperature(4)))
                    .anyMatch(withTemperature(Temperature.create().withLocation("north").withTemperature(3)));
        }
    }

    InfluxDBClient createClient(InfluxDBContainer<?> influxDBContainer) {
        final InfluxDBClientOptions influxDBClientOptions = InfluxDBClientOptions
                .builder()
                .url(influxDBContainer.getUrl())
                .authenticate(influxDBContainer.getUsername(), influxDBContainer.getPassword().toCharArray())
                .bucket(influxDBContainer.getBucket())
                .org(influxDBContainer.getOrganization())
                .build();
        return InfluxDBClientFactory.create(influxDBClientOptions);
    }

    void writeTemperature(InfluxDBClient client, Temperature... temperatures) {
        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        for (Temperature temperature : temperatures) {
            writeApi.writeMeasurement(WritePrecision.MS, temperature);
        }
    }

    List<Temperature> executeQuery(InfluxDBClient client, String query) {
        QueryApi queryApi = client.getQueryApi();
        return queryApi.query(query, Temperature.class);
    }

    Predicate<Temperature> withTemperature(Temperature temp) {
        return temperature -> temperature.location.equals(temp.location) && temperature.value.equals(temp.value);
    }
}