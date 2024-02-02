package dk.bringlarsen.influxdbexploration;

import dk.bringlarsen.influxdbexploration.process.ProcessManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import static org.springframework.boot.ansi.AnsiColor.GREEN;

@ShellComponent
@SpringBootApplication
public class SpringShellApplication {

    private final ProcessManager manager = new ProcessManager();

    @ShellMethod(key = {"process"}, value = "Execute expensive process")
    public void process(@ShellOption(help = "How many items to process", defaultValue = "10") int itemCount,
                        @ShellOption(help = "Host count", defaultValue = "1") int hostCount,
                        @ShellOption(help = "Thread count", defaultValue = "1") int threadCount) {
        manager.process(itemCount, hostCount, threadCount);
        print("%s host(s) with %s thread(s) are working on %s items each", hostCount, threadCount, itemCount);
    }

    @ShellMethod(key = {"stop"}, value = "Stop processing")
    public void stopAll() {
        int threadStopped = manager.stopAll();
        print("Stopping %s thread(s)", threadStopped);
    }

    @ShellMethod(key = {"status"}, value = "How many threads are executing.")
    public void status() {
        print("Total threads processing: %s", manager.getCurrentlyExecutingThreads());
        print("Total items processed: %s", manager.getTotalItemsProcessed());
    }

    private void print(String message, Object... args) {
        System.out.printf(AnsiOutput.toString(GREEN, message + "%n"), args);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringShellApplication.class, args);
    }
}
