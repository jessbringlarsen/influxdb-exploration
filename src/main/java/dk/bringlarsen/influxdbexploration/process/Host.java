package dk.bringlarsen.influxdbexploration.process;

import java.util.List;
import java.util.Random;

public class Host {

    private final int id;
    private final String region;

    public Host(int id) {
        this.id = id;
        this.region = randomRegion();
    }

    public int id() {
        return id;
    }

    public String region() {
        return region;
    }

    private String randomRegion() {
        List<String> countryCodes = List.of("DK", "SE", "DE", "NO", "NL", "GB", "FR", "ES", "AT", "CZ", "PL");
        return countryCodes.get(new Random().nextInt(countryCodes.size()));
    }
}
