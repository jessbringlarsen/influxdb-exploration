package dk.bringlarsen.influxdbexploration.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Random;

public class SomeProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void execute(WorkConfiguration configuration) {
        boolean stopProcessing = false;
        try {
            MDC.put("thread", String.valueOf(configuration.threadId()));
            MDC.put("host", String.valueOf(configuration.hostId()));
            int itemsProcessed = configuration.getPerformanceConfig();
            for (int work = 1; work <= configuration.itemCountToProcess() && !stopProcessing; work++) {
                stopProcessing = doProcess(itemsProcessed);
            }
        } finally {
            MDC.remove("thread");
            MDC.remove("host");
        }
    }

    private boolean doProcess(int itemsProcessed) {
        try {
            Thread.sleep(1000);
            log.info("processed={}", new Random().nextInt(itemsProcessed, itemsProcessed + 5000));
        } catch (InterruptedException e) {
            return true;
        }
        return false;
    }
}
