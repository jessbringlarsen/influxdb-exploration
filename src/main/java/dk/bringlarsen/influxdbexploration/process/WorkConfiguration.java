package dk.bringlarsen.influxdbexploration.process;

import java.util.List;
import java.util.Random;

public record WorkConfiguration(int hostId, int threadId, int itemCountToProcess) {

    public int getPerformanceConfig() {
        return new Random().nextInt(10000, 50000);
    }

    public String region() {
        List<String> countryCodes = List.of("DK", "SE", "DE", "NO", "NL", "GB", "FR", "ES", "AT", "CZ", "PL");
        return countryCodes.get(new Random().nextInt(countryCodes.size()));
    }
}
