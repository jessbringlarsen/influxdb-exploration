package dk.bringlarsen.influxdbexploration.process;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class ProcessManager {

    private final List<Thread> executingThreads = new CopyOnWriteArrayList<>();
    private final List<Host> executingHosts = new CopyOnWriteArrayList<>();
    private final AtomicLong totalItemsProcessedCounter = new AtomicLong(0);

    public void processAndBlock(int secondsToProcess, int hostCount, int threadCount) {
        process(secondsToProcess, hostCount, threadCount);
        executingThreads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Processing was interrupted!");
            }
        });
    }

    public void process(int secondsToProcess, int hostCount, int threadCount) {
        for (int hostCounter = 1; hostCounter <= hostCount; hostCounter++) {
            final Host host = new Host(executingHosts.size() + 1);
            executingHosts.add(host);

            for (int threadCounter = 1; threadCounter <= threadCount; threadCounter++) {
                final int threadId = executingThreads.size() + 1;
                executingThreads.add(Thread.startVirtualThread(() -> {
                    SomeProcess process = new SomeProcess();
                    int itemsProcessed = process.execute(new WorkConfiguration(host, threadId, secondsToProcess));
                    totalItemsProcessedCounter.addAndGet(itemsProcessed);
                    executingHosts.remove(host);
                }));
            }
        }
    }

    public int stopAll() {
        int threadCountToStop = executingThreads.size();
        executingThreads.forEach(Thread::interrupt);
        executingThreads.clear();
        executingHosts.clear();
        return threadCountToStop;
    }

    public long getCurrentlyExecutingThreads() {
        return executingThreads.stream().filter(Thread::isAlive).count();
    }

    public long getTotalItemsProcessed() {
        return totalItemsProcessedCounter.get();
    }

    public List<Host> getExecutingHosts() {
        return executingHosts;
    }
}
