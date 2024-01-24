package dk.bringlarsen.springshellexploration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
public class SomeProcess {

    static AtomicInteger threadId = new AtomicInteger(0);
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    static AtomicInteger lowerBound = new AtomicInteger(10000);
    static AtomicInteger upperBound = new AtomicInteger(15000);

    public void execute(int workCount) {
        boolean stopProcessing = false;
        try {
            MDC.put("thread", String.valueOf(threadId.addAndGet(1)));
            for (int work = 1; work <= workCount && !stopProcessing; work++) {
                stopProcessing = doProcess();
            }
        } finally {
            MDC.remove("thread");
        }
    }

    private boolean doProcess() {
        try {
            Thread.sleep(1000);
            log.info("processed={}", new Random().nextInt(lowerBound.get(), upperBound.get()));
        } catch (InterruptedException e) {
            log.warn("Thread {} stopped", threadId);
            return true;
        }
        return false;
    }
}
