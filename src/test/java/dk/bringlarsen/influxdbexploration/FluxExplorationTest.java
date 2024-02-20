package dk.bringlarsen.influxdbexploration;

import com.influxdb.query.dsl.Flux;
import dk.bringlarsen.influxdbexploration.api.InfluxDbQueryApi;
import dk.bringlarsen.influxdbexploration.api.InfluxDbWriteApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;

import static dk.bringlarsen.influxdbexploration.PerformanceMeasurement.performanceMeasurement;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Explore Flux queries acting on this data:
 * <p>
 * Start time
 * |----|----|----|----|
 * 0    1    2    3    4
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

    private InfluxDbQueryApi queryApi;
    private final Instant start = Instant.parse("2023-10-15T10:00:00.00Z");
    private final Instant end = Instant.parse("2023-10-15T10:05:00.00Z");

    @BeforeEach
    void setup() {
        queryApi = new InfluxDbQueryApi(influxDBContainer);

        new InfluxDbWriteApi(influxDBContainer).cleanAndWrite(
            performanceMeasurement().withHost("host-1").withThread("1").withProcessedItems(6).withTimePlusMinutes(start, 1),
            performanceMeasurement().withHost("host-2").withThread("1").withProcessedItems(2).withTimePlusMinutes(start, 2),
            performanceMeasurement().withHost("host-1").withThread("2").withProcessedItems(3).withTimePlusMinutes(start, 3),
            performanceMeasurement().withHost("host-2").withThread("2").withProcessedItems(1).withTimePlusMinutes(start, 4));
    }

    @Test
    @DisplayName("expect results averaged and grouped by thread")
    void testGroupByMean() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("thread")
            .mean();

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
            .hasSize(2)
            .anyMatch(matchThread(performanceMeasurement().withThread("1").withProcessedItems(4)))
            .anyMatch(matchThread(performanceMeasurement().withThread("2").withProcessedItems(2)));

    }

    @Test
    @DisplayName("expect results where processed items are summed up grouped by host")
    void testGroupBySum() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("host")
            .sum();

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
            .hasSize(2)
            .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(9)))
            .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(3)));
    }

    @Test
    @DisplayName("expect a total of processed items are calculated")
    void testTotalSum() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("")
            .sum();

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
            .hasSize(1)
            .anyMatch(matchHost(performanceMeasurement().withProcessedItems(12)));
    }

    @Test
    @DisplayName("expect 0.99 percentile group by host")
    void testQuantileGrouped() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("host")
            .quantile(0.99f);

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
            .hasSize(2)
            .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(6)))
            .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(2)));
    }

    @Test
    @DisplayName("expect 0.99 percentile ungrouped")
    void testQuantileUngrouped() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("")
            .quantile(0.99f);

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
            .hasSize(1)
            .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(6)));
    }

    @Test
    @DisplayName("calculate average items processed by hosts in a one minute window without creating empty values")
    void testMeanUngroupedWindowed() {
        Flux query = Flux.from(influxDBContainer.getBucket())
            .range(start, end)
            .groupBy("host")
            .aggregateWindow(1L, ChronoUnit.MINUTES, "mean").withCreateEmpty(false)
            .yield("mean");

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

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
