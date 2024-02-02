package dk.bringlarsen.influxdbexploration.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SomeProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AtomicLong totalItemsProcessedCounter;

    public SomeProcess(AtomicLong totalItemsProcessedCounter) {
        this.totalItemsProcessedCounter = totalItemsProcessedCounter;
    }

    public void execute(WorkConfiguration configuration) {
        boolean stopProcessing = false;
        try {
            MDC.put("thread", String.valueOf(configuration.threadId()));
            MDC.put("host", String.valueOf(configuration.hostId()));
            int performanceConfig = configuration.getPerformanceConfig();
            for (int work = 1; work <= configuration.itemCountToProcess() && !stopProcessing; work++) {
                stopProcessing = doProcess(performanceConfig);
            }
        } finally {
            MDC.remove("thread");
            MDC.remove("host");
        }
    }

    private boolean doProcess(int performanceConfig) {
        try {
            Thread.sleep(1000);
            int itemsProcessed = new Random().nextInt(performanceConfig, performanceConfig + 5000);
            totalItemsProcessedCounter.addAndGet(itemsProcessed);
            log.info("processed={}", itemsProcessed);
        } catch (InterruptedException e) {
            return true;
        }
        return false;
    }
}
