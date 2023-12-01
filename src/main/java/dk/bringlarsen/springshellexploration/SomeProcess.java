package dk.bringlarsen.springshellexploration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Random;
import java.util.UUID;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
public class SomeProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UUID threadId = UUID.randomUUID();

    public void execute(int workCount) {
        boolean stopProcessing = false;
        for (int work = 1; work <= workCount && !stopProcessing; work++) {
            StopWatch stopWatch = startStopWatch();
            MDC.put("thread", threadId.toString());
            try {
                stopProcessing = doProcess();
                stopWatch.stop();
                log.info("completed: {}, of: {}, processed: {}, in: {}",
                        work,
                        workCount,
                        new Random().nextInt(10000, 20000),
                        stopWatch.getTotalTimeMillis());
            } finally {
                MDC.remove("thread");
            }
        }
    }

    private StopWatch startStopWatch() {
        StopWatch stopWatch = new StopWatch(this.toString());
        stopWatch.start();
        return stopWatch;
    }

    private boolean doProcess() {
        try {
            Thread.sleep(new Random().nextInt(1000, 5000));
        } catch (InterruptedException e) {
            log.warn("Thread {} stopped", threadId);
            return true;
        }
        return false;
    }
}
