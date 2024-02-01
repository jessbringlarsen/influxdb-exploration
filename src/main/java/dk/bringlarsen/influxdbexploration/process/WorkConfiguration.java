package dk.bringlarsen.influxdbexploration.process;

import java.util.Random;

public record WorkConfiguration(int hostId, int threadId, int itemCountToProcess) {

    public int getPerformanceConfig() {
        return new Random().nextInt(10000, 50000);
    }
}
