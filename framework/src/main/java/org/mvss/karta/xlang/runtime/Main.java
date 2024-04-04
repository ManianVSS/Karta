package org.mvss.karta.xlang.runtime;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;

@Log4j2
public class Main {

    public static final String FILE_TO_RUN = "fileToRun";
    public static final String HELP = "help";
    public static final String XLANG = "xlang";


    public static void main(String[] args) {

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        DefaultParser parser = new DefaultParser();

        options.addOption("f", FILE_TO_RUN, true, "file to run");
        options.addOption(null, HELP, false, "prints this help message");

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(HELP)) {
                formatter.printHelp(XLANG, options);
            } else if (cmd.hasOption(FILE_TO_RUN)) {
                String fileName = cmd.getOptionValue(FILE_TO_RUN);
                try (Runner runner = new Runner()) {
                    System.out.println(runner.run(fileName));
                }
            } else {
                formatter.printHelp(XLANG, options);
                System.exit(-1);
            }
        } catch (UnrecognizedOptionException | MissingArgumentException uoe) {
            System.err.println(uoe.getMessage());
            formatter.printHelp(XLANG, options);
            System.exit(-1);
        } catch (Throwable t) {
            log.error("Exception caught while init: ", t);
            formatter.printHelp(XLANG, options);
            System.exit(-2);
        }
    }

}
