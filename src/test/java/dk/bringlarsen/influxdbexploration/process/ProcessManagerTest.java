package dk.bringlarsen.influxdbexploration.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class ProcessManagerTest {

    private ProcessManager manager;

    @BeforeEach
    void setup() {
        manager = new ProcessManager();
    }

    @Test
    @DisplayName("given no threads started, expect no items processed")
    void noThreadsCase() {
        assertEquals(0, manager.getTotalItemsProcessed());
    }

    @Test
    @DisplayName("given one host and one host, expect items processed")
    void oneThreadCase() {
        manager.process(1, 1, 1);

        await()
                .atMost(Duration.of(5, ChronoUnit.SECONDS))
                .until(() -> manager.getCurrentlyExecutingThreads() == 0);

        assertTrue(manager.getTotalItemsProcessed() > 0);
    }

    @Test
    @DisplayName("given a stop command, expect no executing threads")
    void stopThreadsCase() {
        manager.process(10, 2, 2);

        manager.stopAll();

        assertEquals(0, manager.getCurrentlyExecutingThreads());
    }

    @Test
    @DisplayName("given blocking call, expect method to block until finished")
    void blockingCall() {
        manager.processAndBlock(10, 2, 2);

        assertEquals(0, manager.getCurrentlyExecutingThreads());
    }
}
