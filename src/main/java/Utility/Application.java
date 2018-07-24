package Utility;

import Input.InputReader;
import Input.SimulationInput;
import Output.SimulationOutput;
import Simulation.Simulation;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Parses command-line arguments and runs either headless or UI-mode module.
 */
public class Application {

    boolean uiMode = true;

    public static void main(String[] args) {
        line();
        out("Float-Util v1.0");
        out("a command-line utility used for the calculation of boat buoyancy, stability, and waterline.\n" +
                "Used by the URSS team to evaluate boat hull designs for the 2019 competition year.");
        line();

        Options options = new Options();
        options.addOption(Option.builder("help")
                .required(false)
                .desc("Shows the help screen.")
                .build());
        options.addOption(Option.builder("headless")
                .required(false)
                .desc("Runs Float-Util in headless (no UI) mode.")
                .build());
        options.addOption(Option.builder("input")
                .required(false)
                .hasArg()
                .argName("filename")
                .desc("An input scenario file.")
                .build());
        options.addOption(Option.builder("output")
                .required(false)
                .hasArg()
                .argName("filename")
                .desc("An output file. Can be relative to the input scenario path.")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e){
            out("Error: "+e.getMessage());
            out("Could not parse command-line arguments. Please see -help for usage details!");
            return;
        }

        if (cmd.hasOption("help")){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "float-util", options);
            return;
        }

        if (cmd.hasOption("headless")){
            headlessMode(cmd);
        } else {
            uiMode(cmd);
        }

        out("Done.");
        line();
    }

    public static String detailedError = "No detailed error provided.";

    public static void headlessMode(CommandLine cmd){
        out("Running in headless mode...");

        // Check that input and output are set
        if (!cmd.hasOption("input")){
            out("Error: -input <file> must be provided if running in headless mode!");
            return;
        }
        if (!cmd.hasOption("output")){
            out("Error: -output <file> must be provided if running in headless mode!");
            return;
        }

        final String inputFile = cmd.getOptionValue("input");
        final String outputFile = cmd.getOptionValue("output");
        String fullOutputPath;
        String inputPath;

        out("Input file: "+inputFile);
        out("Output file: "+outputFile);
        out("");

        ProgressBar pb = new ProgressBar("Validating Files.....", 2, 100, System.out, ProgressBarStyle.ASCII).start();
        pb.stepBy(1);
        if (!PathUtilities.isValidFile(inputFile)) {
            pb.stop();
            out("");
            line();
            out("Error: Input file cannot be opened. Please check that the file exists!");
            return;
        }
        inputPath = PathUtilities.getPath(inputFile);
        if (PathUtilities.isAbsolute(outputFile)){
            fullOutputPath = outputFile;
        } else {
            fullOutputPath = inputPath + outputFile;
        }
        pb.stepBy(1);
        pb.stop();

        pb = new ProgressBar("Loading Simulation...", 2, 100, System.out, ProgressBarStyle.ASCII).start();
        pb.stepBy(1);

        // Load the simulation input file.
        SimulationInput simulationInput = new InputReader().read(inputFile);

        if (simulationInput == null){
            pb.stop();
            out("");
            line();
            System.out.println("Error: Unable to parse Scenario Input object.");
            out("Detailed error: "+detailedError);
            return;
        }

        pb.stepBy(1);
        pb.stop();

        SimulationOutput simulationOutput = Simulation.run(simulationInput,inputPath);

        if (simulationOutput == null){
            out("");
            line();
            out("Error: Simulation failed.");
            out("Detailed error: "+detailedError);
        } else {
            line();
            out("Simulation Complete.");
            out("Simulation summary: ");
            out("Input file: "+inputFile);
            out("Output file: "+fullOutputPath);
            out(simulationOutput.toString());
        }
    }

    public static void uiMode(CommandLine cmd){
        out("Running in UI mode...");
    }

    public static void out(String out){
        System.out.println(out);
    }

    public static void line(){
        out("----------------------------------------------------------------------------");
    }
}
