package dk.bringlarsen.springshellexploration;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "temperature")
public class Temperature {

    @Column(tag = true)
    String location;

    @Column
    Double value;

    @Column(timestamp = true)
    Instant time;

    static Temperature create() {
        return new Temperature();
    }

    Temperature withLocation(String location) {
        this.location = location;
        return this;
    }

    Temperature withTemperature(int temprature) {
        this.value = Double.valueOf(temprature);
        return this;
    }
}
