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

import static dk.bringlarsen.influxdbexploration.PerformanceMeasurement.performanceMeasurement;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * Explore Flux queries acting on this data:
 *
 * Time (Minutes Ago)
 * |----|----|----|----|----|
 * 5    4    3    2    1    0 (Present)
 *      |    |    |    |
 *      |    |    |    +---> host=host-2, thread=2, processedItems=1
 *      |    |    |
 *      |    |    +--------> host=host-1, thread=2, processedItems=3
 *      |    |
 *      |    +-------------> host=host-2, thread=1, processedItems=2
 *      |
 *      +------------------> host=host-1, thread=1, processedItems=6
 */
@Testcontainers
class FluxExplorationTest {

    @Container
    static final InfluxDBContainer<?> influxDBContainer = new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.7.5"));
    InfluxDbApi influxDB;

    @BeforeEach
    void setup() {
        influxDB = new InfluxDbApi(influxDBContainer);
        influxDB.cleanAndWrite(
                performanceMeasurement().withHost("host-1").withThread("1").withProcessedItems(6).withTimeMinusMinutes(4),
                performanceMeasurement().withHost("host-2").withThread("1").withProcessedItems(2).withTimeMinusMinutes(3),
                performanceMeasurement().withHost("host-1").withThread("2").withProcessedItems(3).withTimeMinusMinutes(2),
                performanceMeasurement().withHost("host-2").withThread("2").withProcessedItems(1).withTimeMinusMinutes(1));
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
                .anyMatch(matchThread(performanceMeasurement().withThread("1").withProcessedItems(4)))
                .anyMatch(matchThread(performanceMeasurement().withThread("2").withProcessedItems(2)));

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
                .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(9)))
                .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(3)));
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
                .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(6)))
                .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(2)));
    }

    @Test
    @DisplayName("calculate the mean (average) items processed by each host in a six minute window")
    void testCase4() {
        List<PerformanceMeasurement> result = influxDB.executeQuery(String.format("""
                from(bucket: "%s")
                  |> range(start: -5m)
                  |> group(columns: ["host"])
                  |> aggregateWindow(every: 6m, fn: mean, createEmpty: false)
                  |> yield(name: "mean")
                    """, influxDBContainer.getBucket()));

        assertThat(result)
                .hasSize(2)
                .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(4.5)))
                .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(1.5)));
    }

    Predicate<PerformanceMeasurement> matchThread(PerformanceMeasurement performanceMeasurement) {
        return m -> m.thread.equals(performanceMeasurement.thread) && m.processedItems.equals(performanceMeasurement.processedItems);
    }

    Predicate<PerformanceMeasurement> matchHost(PerformanceMeasurement performanceMeasurement) {
        return m -> m.host.equals(performanceMeasurement.host) && m.processedItems.equals(performanceMeasurement.processedItems);
    }
}