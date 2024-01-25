package dk.bringlarsen.influxdbexploration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.function.Predicate;

import static dk.bringlarsen.influxdbexploration.PerformanceMeasurement.create;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FluxExplorationTest {

    @Container
    static final InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.7.5"));
    InfluxDbApi influxDB;

    @BeforeEach
    void setup() {
        influxDB = new InfluxDbApi(influxDBContainer);
        influxDB.cleanAndWrite(
                create().withThread("1").withProcessedItems(6),
                create().withThread("1").withProcessedItems(2),
                create().withThread("2").withProcessedItems(3),
                create().withThread("2").withProcessedItems(1));
    }

    @Test
    @DisplayName("expect mean results, grouped by tag")
    void testCase1() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> mean()""", influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withMeasurement(create().withThread("1").withProcessedItems(4)))
                .anyMatch(withMeasurement(create().withThread("2").withProcessedItems(2)));

    }

    @Test
    @DisplayName("expect results summed, grouped by tag")
    void testCase2() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> sum()""", influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withMeasurement(create().withThread("1").withProcessedItems(8)))
                .anyMatch(withMeasurement(create().withThread("2").withProcessedItems(4)));
    }

    @Test
    @DisplayName("expect latest results, grouped by tag")
    void testCase3() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> last()
                   """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withMeasurement(create().withThread("1").withProcessedItems(2)))
                .anyMatch(withMeasurement(create().withThread("2").withProcessedItems(1)));
    }

    @Test
    @DisplayName("expect latest results, grouped by tag")
    void testCase4() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> window(every: 1m)
                   |> quantile(q: 0.99)
                   """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withMeasurement(create().withThread("1").withProcessedItems(6)))
                .anyMatch(withMeasurement(create().withThread("2").withProcessedItems(3)));
    }

    @Test
    @DisplayName("expect grouping by tag every five minutes")
    void testCase5() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> aggregateWindow(every: 5m, fn: mean, createEmpty: false)
                """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withMeasurement(create().withThread("1").withProcessedItems(4)))
                .anyMatch(withMeasurement(create().withThread("2").withProcessedItems(2)));
    }

    Predicate<PerformanceMeasurement> withMeasurement(PerformanceMeasurement performanceMeasurement) {
        return m -> m.thread.equals(performanceMeasurement.thread) && m.processedItems.equals(performanceMeasurement.processedItems);
    }
}