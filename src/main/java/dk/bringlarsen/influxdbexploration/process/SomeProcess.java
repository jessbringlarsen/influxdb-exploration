package dk.bringlarsen.influxdbexploration.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Random;

public class SomeProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public int execute(WorkConfiguration configuration) {
        try {
            setupLogging(configuration);
            int performanceConfig = configuration.getPerformanceConfig();
            return doProcess(configuration, performanceConfig);
        } finally {
            cleanUp();
        }
    }

    private static void setupLogging(WorkConfiguration configuration) {
        MDC.put("thread", String.valueOf(configuration.threadId()));
        MDC.put("host", String.valueOf(configuration.host().id()));
        MDC.put("region", String.valueOf(configuration.host().region()));
    }

    private int doProcess(WorkConfiguration configuration, int performanceConfig) {
        int itemsProcessed = 0;
        try {
            for (int work = 1; work <= configuration.itemCountToProcess(); work++) {
                itemsProcessed += doProcess(performanceConfig);
            }
        } catch (InterruptedException e) {
            log.warn("Thread interrupted. Processing stopped!");
        }
        return itemsProcessed;
    }

    private static void cleanUp() {
        MDC.remove("thread");
        MDC.remove("region");
        MDC.remove("host");
    }

    private int doProcess(int performanceConfig) throws InterruptedException {
        Thread.sleep(1000);
        int itemsProcessed = new Random().nextInt(performanceConfig, performanceConfig + 5000);
        log.info("processed={}", itemsProcessed);
        return itemsProcessed;
    }
}
