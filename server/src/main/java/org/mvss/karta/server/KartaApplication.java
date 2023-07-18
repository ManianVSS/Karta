package org.mvss.karta.server;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.cli.KartaMain;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.server.dataload.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Log4j2
@SpringBootApplication
public class KartaApplication implements CommandLineRunner {
    public static final String IMPORT_DATA_FILE = "importDataFile";
    public static final String EXPORT_DATA_FILE = "exportDataFile";
    public static List<Runnable> exitHooks = Collections.synchronizedList(new ArrayList<>());
    @Autowired
    private KartaRuntime kartaRuntime;

    @Autowired
    private DataLoader dataLoader;

    public static void main(String[] args) {
        if (args.length > 0) {
            KartaMain.main(args);
        } else {
            // Spring boot start
            SpringApplication.run(KartaApplication.class, args);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationStartup() {
        kartaRuntime.addNodes();
    }

    @Override
    public void run(String... args) {
        log.info("******************** Starting Karta Server *********************");
//        if ((args != null) && args.length > 0) {
//            kartaRuntime.addNodes();
//            boolean returnStatus = KartaMain.run(args);
//            System.exit(returnStatus ? 0 : -1);
//        }
        if (args.length > 0) {
            if (IMPORT_DATA_FILE.equals(args[0])) {
                if (args.length >= 2) {
                    String[] dataFileNames = Arrays.copyOfRange(args, 1, args.length);
                    try {
                        if (!dataLoader.importData(dataFileNames)) {
                            log.error("Error occurred while importing data.");
                        }
                    } catch (Exception e) {
                        log.error("Exception occurred during data file import", e);
                        System.exit(-1);
                    }
                    System.exit(0);
                }
            } else if (EXPORT_DATA_FILE.equals(args[0])) {
                if (args.length >= 2) {
                    String dataFileName = args[1];
                    try {
                        if (!dataLoader.exportData(dataFileName)) {
                            log.error("Error occurred while exporting data.");
                        }
                    } catch (Exception e) {
                        log.error("Exception occurred during data file export", e);
                        System.exit(-1);
                    }
                    System.exit(0);
                }
            }
        }
    }

    @PreDestroy
    public void onDestroy() {
        log.info("******************** Stopping Karta Server *********************");

        log.info("Triggering registered exit hooks");
        for (Runnable exitHook : new ArrayList<>(exitHooks)) {
            new Thread(exitHook).start();
        }
    }

}
