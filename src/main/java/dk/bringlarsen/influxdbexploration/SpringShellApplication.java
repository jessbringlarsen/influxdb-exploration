package dk.bringlarsen.influxdbexploration;

import dk.bringlarsen.influxdbexploration.process.ProcessManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import static org.springframework.boot.ansi.AnsiColor.GREEN;
import static org.springframework.shell.context.InteractionMode.*;

@ShellComponent
@SpringBootApplication
public class SpringShellApplication {

    private final ProcessManager manager = new ProcessManager();

    @ShellMethod(key = {"process"}, interactionMode = ALL, value = "Execute expensive process")
    public void process(@ShellOption(help = "Processing time in seconds", defaultValue = "60") int secondsToProcess,
                        @ShellOption(help = "Host count", defaultValue = "2") int hostCount,
                        @ShellOption(help = "Thread count", defaultValue = "4") int threadCount,
                        @ShellOption(help = "Block while processing", defaultValue = "false") boolean block) {
        print("%s host(s) with %s thread(s) are processing in %s seconds", hostCount, threadCount, secondsToProcess);
        if (block) {
            manager.processAndBlock(secondsToProcess, hostCount, threadCount);
        } else {
            manager.process(secondsToProcess, hostCount, threadCount);
        }
    }

    @ShellMethod(key = {"stop"}, interactionMode = INTERACTIVE, value = "Stop processing")
    public void stopAll() {
        int threadStopped = manager.stopAll();
        print("Stopping %s thread(s)", threadStopped);
    }

    @ShellMethod(key = {"status"}, interactionMode = INTERACTIVE, value = "How many threads are executing.")
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
