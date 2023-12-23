package dk.bringlarsen.springshellexploration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.List;

@ShellComponent
public class ExecuteProcess {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationContext context;
    private final List<Thread> threads = new ArrayList<>();

    @Autowired
    public ExecuteProcess(ApplicationContext context) {
        this.context = context;
    }

    @ShellMethod(key = { "process", "p"}, value = "Execute expensive process")
    public String process(@ShellOption(defaultValue = "10") int workCount) {
        threads.add(Thread.startVirtualThread(() -> {
            SomeProcess process = context.getBean(SomeProcess.class);
            process.execute(workCount);
        }));
        return "begin processing, itemCount: " + workCount;
    }

    @ShellMethod(key = { "stop", "s"}, value = "Stop processing")
    public void stopAll() {
        log.info("Stopping {} threads", threads.size());
        threads.forEach(Thread::interrupt);
        threads.clear();
    }

    @ShellMethod(key = { "up"}, value = "Increase processing time by one second")
    public void up() {
        WorkConfiguration.lowerBound.addAndGet(1000);
        WorkConfiguration.upperBound.addAndGet(1000);
    }
}

