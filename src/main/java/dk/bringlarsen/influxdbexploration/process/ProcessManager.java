package dk.bringlarsen.influxdbexploration.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessManager {

    private final List<Thread> threads = new ArrayList<>();
    private final AtomicInteger totalHostCount = new AtomicInteger(0);
    private final AtomicInteger totalThreadCount = new AtomicInteger(0);

    public void process(int itemCount, int hostCount, int threadCount) {
        for (int hostCounter = 1; hostCounter <= hostCount; hostCounter++) {
            final int hostId = totalHostCount.addAndGet(1);

            for (int threadCounter = 1; threadCounter <= threadCount; threadCounter++) {
                final int threadId = totalThreadCount.addAndGet(1);
                threads.add(Thread.startVirtualThread(() -> {
                    SomeProcess process = new SomeProcess();
                    process.execute(new WorkConfiguration(hostId, threadId, itemCount));
                }));
            }
        }
        System.out.printf("%s host(s) with %s thread(s) are working on %s items each %n", hostCount, threadCount, itemCount);
    }

    public void stopAll() {
        System.out.printf("Stopping %s threads %n", threads.size());
        threads.forEach(Thread::interrupt);
        threads.clear();
    }

    public void status() {
        long threadsAlive = threads.stream().filter(Thread::isAlive).count();
        System.out.printf("Currently %s threads are executing %n", threadsAlive);
    }
}
