package dk.bringlarsen.springshellexploration;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

@Measurement(name = "temperature")
public class Temperature {

    @Column(tag = true)
    String location;

    @Column
    Double value;

    static Temperature create() {
        return new Temperature();
    }

    Temperature withLocation(String location) {
        this.location = location;
        return this;
    }

    Temperature withTemperature(int temperature) {
        this.value = Double.valueOf(temperature);
        return this;
    }
}
