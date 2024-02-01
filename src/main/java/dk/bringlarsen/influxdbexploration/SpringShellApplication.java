package dk.bringlarsen.influxdbexploration;

import dk.bringlarsen.influxdbexploration.process.ProcessManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@SpringBootApplication
public class SpringShellApplication {

    private final ProcessManager manager = new ProcessManager();

    @ShellMethod(key = {"process"}, value = "Execute expensive process")
    public void process(@ShellOption(help = "How many items to process", defaultValue = "10") int itemCount,
                          @ShellOption(help = "Host count", defaultValue = "1") int hostCount,
                          @ShellOption(help = "Thread count", defaultValue = "1") int threadCount) {
        manager.process(itemCount, hostCount, threadCount);
    }

    @ShellMethod(key = {"stop"}, value = "Stop processing")
    public void stopAll() {
        manager.stopAll();
    }

    @ShellMethod(key = {"status"}, value = "How many threads are executing.")
    public void status() {
        manager.status();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringShellApplication.class, args);
    }
}
