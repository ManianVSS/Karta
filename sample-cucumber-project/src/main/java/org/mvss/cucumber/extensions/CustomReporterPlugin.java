package org.mvss.cucumber.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;

@Log4j2
public class CustomReporterPlugin implements ConcurrentEventListener {

    public CustomReporterPlugin(String parameter) {
        log.info("Plugin parameter passed is {}", parameter);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);

        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
        publisher.registerHandlerFor(StepDefinedEvent.class, this::handleStepDefinedEvent);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetsSuggestedEvent);

        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);

        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);


        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);


        publisher.registerHandlerFor(TestRunFinished.class, this::finishReport);

    }

    private void printEvent(Event event) {
        try {
            log.info("Event of type {} occurred:{}", event.getClass(), ParserUtils.getObjectMapper().writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleTestSourceRead(TestSourceRead testSourceRead) {
        printEvent(testSourceRead);
    }

    private void handleTestSourceParsed(TestSourceParsed testSourceParsed) {
        printEvent(testSourceParsed);
    }

    private void handleStepDefinedEvent(StepDefinedEvent stepDefinedEvent) {
        printEvent(stepDefinedEvent);
    }

    private void handleSnippetsSuggestedEvent(SnippetsSuggestedEvent snippetsSuggestedEvent) {
        printEvent(snippetsSuggestedEvent);
    }

    private void handleTestRunStarted(TestRunStarted testRunStarted) {
        printEvent(testRunStarted);
    }

    private void handleTestCaseStarted(TestCaseStarted testCaseStarted) {
        printEvent(testCaseStarted);
    }

    private void handleTestStepStarted(TestStepStarted testStepStarted) {
        printEvent(testStepStarted);
    }

    private void handleTestStepFinished(TestStepFinished testStepFinished) {
        printEvent(testStepFinished);
    }

    private void handleTestCaseFinished(TestCaseFinished testCaseFinished) {
        printEvent(testCaseFinished);
    }

    private void handleWrite(WriteEvent writeEvent) {
        printEvent(writeEvent);
    }

    private void handleEmbed(EmbedEvent embedEvent) {
        printEvent(embedEvent);
    }

    private void finishReport(TestRunFinished testRunFinished) {
        printEvent(testRunFinished);
    }
}
