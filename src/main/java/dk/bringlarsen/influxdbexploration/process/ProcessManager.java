package dk.bringlarsen.influxdbexploration.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProcessManager {

    private final List<Thread> threads = new ArrayList<>();
    private final AtomicInteger totalHostCount = new AtomicInteger(0);
    private final AtomicInteger totalThreadCount = new AtomicInteger(0);
    private final AtomicLong totalItemsProcessedCounter = new AtomicLong(0);

    public void processAndBlock(int itemCount, int hostCount, int threadCount) {
        process(itemCount, hostCount, threadCount);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Processing was interrupted!");
            }
        });
    }

    public void process(int itemCount, int hostCount, int threadCount) {
        for (int hostCounter = 1; hostCounter <= hostCount; hostCounter++) {
            final Host host = new Host(totalHostCount.addAndGet(1));

            for (int threadCounter = 1; threadCounter <= threadCount; threadCounter++) {
                final int threadId = totalThreadCount.addAndGet(1);
                threads.add(Thread.startVirtualThread(() -> {
                    SomeProcess process = new SomeProcess(totalItemsProcessedCounter);
                    process.execute(new WorkConfiguration(host, threadId, itemCount));
                }));
            }
        }
    }

    public int stopAll() {
        int threadCountToStop = threads.size();
        threads.forEach(Thread::interrupt);
        threads.clear();
        totalHostCount.set(0);
        totalThreadCount.set(0);
        return threadCountToStop;
    }

    public long getCurrentlyExecutingThreads() {
        return threads.stream().filter(Thread::isAlive).count();
    }

    public long getTotalItemsProcessed() {
        return totalItemsProcessedCounter.get();
    }
}
