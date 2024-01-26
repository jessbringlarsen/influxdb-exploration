package dk.bringlarsen.influxdbexploration;

import java.util.Random;

public record WorkConfiguration(int hostId, int threadId, int itemCountToProcess) {

    public int getPerformanceConfig() {
        return new Random().nextInt(10000, 50000);
    }
}
