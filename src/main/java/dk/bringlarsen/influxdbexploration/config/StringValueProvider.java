package dk.bringlarsen.influxdbexploration.config;

import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StringValueProvider implements ValueProvider {

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return List.of(
                new CompletionProposal("1"),
                new CompletionProposal("2"),
                new CompletionProposal("3")
        );
    }
}
