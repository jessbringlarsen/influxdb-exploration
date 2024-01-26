package dk.bringlarsen.influxdbexploration;

import java.util.Random;

record WorkConfiguration(int hostId, int threadId, int itemCountToProcess) {

    int getPerformanceConfig() {
        return new Random().nextInt(10000, 50000);
    }
}
