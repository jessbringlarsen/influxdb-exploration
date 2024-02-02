package dk.bringlarsen.influxdbexploration;

import dk.bringlarsen.influxdbexploration.process.ProcessManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;

@ShellComponent
@SpringBootApplication
public class SpringShellApplication {

    private final ProcessManager manager = new ProcessManager();

    @ShellMethod(key = {"process"}, value = "Execute expensive process")
    public void process(@ShellOption(help = "How many items to process", defaultValue = "10") int itemCount,
                        @ShellOption(help = "Host count", defaultValue = "1") int hostCount,
                        @ShellOption(help = "Thread count", defaultValue = "1") int threadCount) {
        manager.process(itemCount, hostCount, threadCount);
        System.out.printf("%s host(s) with %s thread(s) are working on %s items each %n", hostCount, threadCount, itemCount);
    }

    @ShellMethod(key = {"stop"}, value = "Stop processing")
    public void stopAll() {
        int threadStopped = manager.stopAll();
        System.out.printf("Stopping %s thread(s) %n", threadStopped);
    }

    @ShellMethod(key = {"status"}, value = "How many threads are executing.")
    public Table status() {
        Object[][] data = {
                {"Total threads processing", "Total items processed"},
                {manager.getCurrentlyExecutingThreads(), manager.getTotalItemsProcessed()}
        };
        TableBuilder tableBuilder = new TableBuilder(new ArrayTableModel(data));
        tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.fancy_light);
        return tableBuilder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringShellApplication.class, args);
    }
}
