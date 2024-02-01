package dk.bringlarsen.influxdbexploration;

import dk.bringlarsen.influxdbexploration.config.StringValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ShellComponent
public class ExecuteProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ApplicationContext context;
    private final List<Thread> threads = new ArrayList<>();
    private static AtomicInteger totalHostCount = new AtomicInteger(0);
    private static AtomicInteger totalThreadCount = new AtomicInteger(0);

    @Autowired
    public ExecuteProcess(ApplicationContext context) {
        this.context = context;
    }

    @ShellMethod(key = { "process"}, value = "Execute expensive process")
    public String process(@ShellOption(help = "How many items to process" ,defaultValue = "10", valueProvider = StringValueProvider.class) int itemCount,
                          @ShellOption(help = "Host count", defaultValue = "1", valueProvider = StringValueProvider.class) int hostCount,
                          @ShellOption(help = "Thread count", defaultValue = "1", valueProvider = StringValueProvider.class) int threadCount) {
        for (int hostCounter = 1; hostCounter <= hostCount; hostCounter++) {
            final int hostId = totalHostCount.addAndGet(1);
            for (int threadCounter = 1; threadCounter <= threadCount; threadCounter++) {
                final int threadId = totalThreadCount.addAndGet(1);
                threads.add(Thread.startVirtualThread(() -> {
                    SomeProcess process = context.getBean(SomeProcess.class);
                    process.execute(new WorkConfiguration(hostId, threadId, itemCount));
                }));
            }
        }
        return String.format("%s host(s) with %s thread(s) are working on %s items each", hostCount, threadCount, itemCount);
    }

    @ShellMethod(key = { "stop"}, value = "Stop processing")
    public void stopAll() {
        log.info("Stopping {} threads", threads.size());
        threads.forEach(Thread::interrupt);
        threads.clear();
    }

    @ShellMethod(key = { "status"}, value = "How many threads are executing.")
    public void status() {
        long threadsAlive = threads.stream().filter(Thread::isAlive).count();
        log.info("Currently {} threads are executing", threadsAlive);
    }
}
