package org.mvss.cucumber.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;

@Log4j2
public class CustomReporterPlugin implements Plugin, EventListener {

    public CustomReporterPlugin(String parameter) {
        log.info("Plugin parameter passed is " + parameter);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
        publisher.registerHandlerFor(TestRunFinished.class, this::finishReport);
    }

    private void printEvent(Event event) {
        try {
            log.info("Event occurred:" + ParserUtils.getObjectMapper().writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleTestSourceRead(TestSourceRead testSourceRead) {
        printEvent(testSourceRead);
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
