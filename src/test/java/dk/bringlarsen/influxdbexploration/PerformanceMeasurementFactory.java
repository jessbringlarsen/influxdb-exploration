package dk.bringlarsen.influxdbexploration;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerformanceMeasurementFactory {

    public static List<PerformanceMeasurement> getRandomDataSetOfSize(int size) {
        List<PerformanceMeasurement> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new PerformanceMeasurement()
                    .withTime(getRandomTime())
                    .withProcessedItems(new Random().nextInt(5000, 10000))
                    .withThread(String.valueOf(new Random().nextInt(1, 11))));
        }
        return result;
    }

    public static Instant getRandomTime() {
        LocalDateTime lastHour = LocalDateTime.now(Clock.systemUTC()).minusHours(1);
        LocalDateTime random = lastHour
                .withSecond(new Random().nextInt(1, 59))
                .withNano(new Random().nextInt(1, 999999999))
                .withMinute(new Random().nextInt(1, 59));
        return random.toInstant(ZoneOffset.UTC);
    }
}