package dk.bringlarsen.influxdbexploration;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Measurement(name = "performance")
public class PerformanceMeasurement {

    @Column(tag = true)
    String host = "host-1";

    @Column(tag = true)
    String thread;

    @Column(name = "value")
    Double processedItems;

    @Column(timestamp = true)
    Instant time;

    static PerformanceMeasurement performanceMeasurement() {
        return new PerformanceMeasurement();
    }

    PerformanceMeasurement withHost(String host) {
        this.host = host;
        return this;
    }

    PerformanceMeasurement withThread(String thread) {
        this.thread = thread;
        return this;
    }

    PerformanceMeasurement withProcessedItems(int processedItems) {
        this.processedItems = Double.valueOf(processedItems);
        return this;
    }

    PerformanceMeasurement withProcessedItems(double processedItems) {
        this.processedItems = Double.valueOf(processedItems);
        return this;
    }

    PerformanceMeasurement withTime(Instant time) {
        this.time = time;
        return this;
    }

    PerformanceMeasurement withTimeMinusMinutes(int minutes) {
        return withTime(Instant.now(Clock.systemUTC()).minus(Duration.ofMinutes(minutes)));
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
