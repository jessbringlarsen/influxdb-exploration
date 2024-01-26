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
                create().withHost("host-1").withThread("1").withProcessedItems(6).withTimeMinusMinutes(4),
                create().withHost("host-2").withThread("1").withProcessedItems(2).withTimeMinusMinutes(3),
                create().withHost("host-1").withThread("2").withProcessedItems(3).withTimeMinusMinutes(2),
                create().withHost("host-2").withThread("2").withProcessedItems(1).withTimeMinusMinutes(1));
    }

    @Test
    @DisplayName("expect results averaged and grouped by thread")
    void testCase1() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> group(columns: ["thread"])
                   |> mean()""", influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withThread(create().withThread("1").withProcessedItems(4)))
                .anyMatch(withThread(create().withThread("2").withProcessedItems(2)));

    }

    @Test
    @DisplayName("expect results where processed items are summed up grouped by host")
    void testCase2() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> group(columns: ["host"])
                   |> sum()""", influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withHost(create().withHost("host-1").withProcessedItems(9)))
                .anyMatch(withHost(create().withHost("host-2").withProcessedItems(3)));
    }

    @Test
    @DisplayName("expect 0.99 percentile group by host")
    void testCase3() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> group(columns: ["host"])
                   |> quantile(q: 0.99)
                   """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withHost(create().withHost("host-1").withProcessedItems(6)))
                .anyMatch(withHost(create().withHost("host-2").withProcessedItems(2)));
    }

    @Test
    @DisplayName("expect results are down-sampled for every fire minutes")
    void testCase4() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                   |> range(start: -5m)
                   |> group(columns: ["host"])
                   |> aggregateWindow(every: 5m, fn: sum, createEmpty: false)
                """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(withHost(create().withHost("host-1").withProcessedItems(9)))
                .anyMatch(withHost(create().withHost("host-2").withProcessedItems(3)));
    }

    Predicate<PerformanceMeasurement> withThread(PerformanceMeasurement performanceMeasurement) {
        return m -> m.thread.equals(performanceMeasurement.thread) && m.processedItems.equals(performanceMeasurement.processedItems);
    }

    Predicate<PerformanceMeasurement> withHost(PerformanceMeasurement performanceMeasurement) {
        return m -> m.host.equals(performanceMeasurement.host) && m.processedItems.equals(performanceMeasurement.processedItems);
    }
}