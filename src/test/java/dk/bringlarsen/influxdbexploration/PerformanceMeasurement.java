package dk.bringlarsen.influxdbexploration;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Instant;

@Measurement(name = "performance")
public class PerformanceMeasurement {

    @Column(tag = true)
    String thread;

    @Column(name = "value")
    Double processedItems;

    @Column(timestamp = true)
    Instant time;

    static PerformanceMeasurement create() {
        return new PerformanceMeasurement();
    }

    PerformanceMeasurement withThread(String thread) {
        this.thread = thread;
        return this;
    }

    PerformanceMeasurement withProcessedItems(int processedItems) {
        this.processedItems = Double.valueOf(processedItems);
        return this;
    }

    PerformanceMeasurement withTime(Instant time) {
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
