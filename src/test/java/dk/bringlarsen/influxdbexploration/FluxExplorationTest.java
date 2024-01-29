package dk.bringlarsen.influxdbexploration;

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.MeanFlux;
import com.influxdb.query.dsl.functions.QuantileFlux;
import com.influxdb.query.dsl.functions.SumFlux;
import com.influxdb.query.dsl.functions.YieldFlux;
import dk.bringlarsen.influxdbexploration.api.InfluxDbQueryApi;
import dk.bringlarsen.influxdbexploration.api.InfluxDbWriteApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;

import static dk.bringlarsen.influxdbexploration.PerformanceMeasurement.performanceMeasurement;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Explore Flux queries acting on this data:
 * <p>
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
    InfluxDbWriteApi writeApi;
    InfluxDbQueryApi queryApi;

    @BeforeEach
    void setup() {
        queryApi = new InfluxDbQueryApi(influxDBContainer);
        writeApi = new InfluxDbWriteApi(influxDBContainer);
        writeApi.cleanAndWrite(
                performanceMeasurement().withHost("host-1").withThread("1").withProcessedItems(6).withTimeMinusMinutes(4),
                performanceMeasurement().withHost("host-2").withThread("1").withProcessedItems(2).withTimeMinusMinutes(3),
                performanceMeasurement().withHost("host-1").withThread("2").withProcessedItems(3).withTimeMinusMinutes(2),
                performanceMeasurement().withHost("host-2").withThread("2").withProcessedItems(1).withTimeMinusMinutes(1));
    }

    @Test
    @DisplayName("expect results averaged and grouped by thread")
    void testCase1() {
        MeanFlux query = Flux.from(influxDBContainer.getBucket())
                .range(-5L, ChronoUnit.MINUTES)
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
    void testCase2() {
        SumFlux query = Flux.from(influxDBContainer.getBucket())
                .range(-5L, ChronoUnit.MINUTES)
                .groupBy("host")
                .sum();

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
                .hasSize(2)
                .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(9)))
                .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(3)));
    }

    @Test
    @DisplayName("expect 0.99 percentile group by host")
    void testCase3() {
        QuantileFlux query = Flux.from(influxDBContainer.getBucket())
                .range(-5L, ChronoUnit.MINUTES)
                .groupBy("host")
                .quantile(Float.valueOf("0.99"));

        List<PerformanceMeasurement> result = queryApi.executeQuery(query);

        assertThat(result)
                .hasSize(2)
                .anyMatch(matchHost(performanceMeasurement().withHost("host-1").withProcessedItems(6)))
                .anyMatch(matchHost(performanceMeasurement().withHost("host-2").withProcessedItems(2)));
    }

    @Test
    @DisplayName("calculate the mean (average) items processed by each host in a six minute window")
    void testCase4() {
        YieldFlux query = Flux.from(influxDBContainer.getBucket())
                .range(-5L, ChronoUnit.MINUTES)
                .groupBy("host")
                .aggregateWindow(6L, ChronoUnit.MINUTES, "mean").withCreateEmpty(false)
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