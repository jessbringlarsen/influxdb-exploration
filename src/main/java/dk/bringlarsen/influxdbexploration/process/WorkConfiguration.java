package dk.bringlarsen.influxdbexploration.process;

import java.util.Random;

public record WorkConfiguration(Host host, int threadId, int secondsToProcess) {

    public int getPerformanceConfig() {
        return new Random().nextInt(10000, 50000);
    }
}
