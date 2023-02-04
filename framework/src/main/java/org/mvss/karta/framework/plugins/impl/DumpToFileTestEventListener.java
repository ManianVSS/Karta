package org.mvss.karta.framework.plugins.impl;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.Initializer;
import org.mvss.karta.framework.models.event.Event;
import org.mvss.karta.framework.plugins.TestEventListener;
import org.mvss.karta.framework.properties.PropertyMapping;
import org.mvss.karta.framework.utils.PropertyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Log4j2
public class DumpToFileTestEventListener implements TestEventListener {
    public static final String PLUGIN_NAME = "DumpToFileTestEventListener";
    private final Object writeLock = new Object();
    private ObjectOutputStream outputStream = null;
    @PropertyMapping(group = PLUGIN_NAME, value = "fileName")
    private String fileName = "KartaEventsRawDump.bin";
    private boolean initialized = false;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + " plugin");

        fileName = PropertyUtils.expandEnvVars(fileName);

        File eventDumpFile = new File(fileName);

        if (!eventDumpFile.exists()) {
            if (!eventDumpFile.createNewFile()) {
                return false;
            }
        }

        outputStream = new ObjectOutputStream(new FileOutputStream(eventDumpFile, true));
        initialized = true;

        return true;
    }

    @Override
    public synchronized void processEvent(Event event) {
        try {
            synchronized (writeLock) {
                outputStream.writeObject(event);
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void close() {
        try {
            log.info("Closing " + PLUGIN_NAME + " ... ");

            if (outputStream != null) {
                synchronized (writeLock) {

                    outputStream.close();
                    outputStream = null;
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }
}
