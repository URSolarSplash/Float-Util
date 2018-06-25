package Simulation;

import Model.*;
import Parser.ModelParser;
import Parser.ModelParserLibrary;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FloatUtilMain {

    public static String units = "in";

    public static void main(String[] args) {
        printHeader("Float-Util v1.0");
        print("Float-Util v1.0");
        print("a command-line utility used for the calculation of boat buoyancy, stability, and waterline.\n" +
                "Used by the URSS team to evaluate boat hull designs for the 2019 competition year.");
        printHeader("Initialization");
        log("Parsing command-line parameters...");
        int numArgs = args.length;
        log("Found "+numArgs +" params.");
        if (numArgs == 0){
            log("Please specify at least one parameter.");
            log("Consult 'float-util --help' for usage instructions.");
            log("Short summary:");
            log("  float-util <input file> <output file> - Outputs the PDF report to the specified file.");
            print("\n");
            return;
        }
        if (!(numArgs == 1 | numArgs == 2 | numArgs == 3)){
            log("Too many command-line parameters!");
            log("Consult 'float-util --help' for usage instructions.");
            print("\n");
            return;
        }

        File inputFile;
        File outputFile;
        ModelParserLibrary modelParserLibrary;
        ModelParser parser;
        String fileType;

        try {
            inputFile = new File(args[0]);
            if(inputFile.exists() && !inputFile.isDirectory()) {
                log("Input file: "+inputFile.getPath());
                fileType = FilenameUtils.getExtension(inputFile.getPath());

                log("Initializing model parser library...");
                modelParserLibrary = new ModelParserLibrary();
                log("Retrieving model parser instance for file type '"+fileType+"'...");
                parser = modelParserLibrary.getParser(fileType);
                if (parser == null){

                    log("Error: No parser implemented for file type '"+fileType+"'!");
                    log("Parser available are: ");
                    for (String parserKey : modelParserLibrary.parsers.keySet()){
                        log(" - "+parserKey);
                    }
                    print("\n");
                    return;
                } else {
                    log("Found parser: "+parser);
                }
            } else {
                throw new IOException("Input file doesn't exist or is a directory.");
            }

            String outputFilename = "";
            if (numArgs == 1){
                outputFilename = inputFile.getPath().substring(0,inputFile.getPath().length() - fileType.length());
                outputFilename += "pdf";
                log("Generating output file from input filename...");
            } else {
                outputFilename = args[1];
                if (!FilenameUtils.getExtension(outputFilename).equals("pdf")){
                    log("Error: Output file must end in .pdf!");
                    print("\n");
                    return;
                }
            }
            outputFile = new File(outputFilename);
            log("Output file: "+outputFilename);

        } catch (IOException e){
            log("Input file is invalid or doesn't exist!");
            print("\n");
            return;
        }

        if (numArgs == 3){
            units = args[3];
            log("Units specified as "+units+"!");
        } else {
            log("Using default units ("+units+")!");
        }

        printHeader("IO Summary");
        log("INPUT FILE: "+inputFile.getPath());
        log("OUTPUT FILE: "+outputFile.getPath());
        log("--- Loading File ----");
        log("Loading model file...");
        Model inputModel = parser.parse(inputFile);

        if (inputModel == null){
            log("Error: Failed to load model!");
            print("\n");
            return;

        }


        log("Loaded model. Model basic statistics:");
        log("- Num triangles: "+inputModel.getTriangles().size());
        log("- Num vertices: "+inputModel.getVertices().size());

        printHeader("Simulation Setup");
        if (inputModel.getTriangles().size() == 0){
            log("Warning: Your model has 0 triangles!");
            log("Note: Model must be in ASCII STL format.");
            log("No calculation will be performed.");
            print("\n");
            return;
        }
        BoundingBox modelSize = inputModel.getBoundingBox();
        log("- Model width: "+modelSize.getWidth()+" "+units);
        log("- Model height: "+modelSize.getHeight()+" "+units);
        log("- Model depth: "+modelSize.getDepth()+" "+units);
        log("- Model volume: "+ inputModel.getVolume()+" "+units+"^3");
        log("- Model surface area: "+ inputModel.getSurfaceArea()+" "+units+"^2");


    }

    public static void helpMessage(){
        printHeader("Help & Usage");
        print("Usage:");
        print("  float-util --help - Displays this menu.");
        print("  float-util <input file> - Outputs the PDF report to a file with the same name as the input.");
        print("  float-util <input file> <output file> - Outputs the PDF report to the specified file.");
        print("  float-util <input file> <output file> <units> - Outputs the PDF report to the specified file, and uses the specified units.");
    }

    private static long startTime = System.currentTimeMillis();


    public static void printHeader(String header) {
        int headerLength = header.length();
        int totalLength = 80;
        int bufferLength = totalLength - headerLength;
        for (int i = 0; i < bufferLength/2 + (headerLength % 2); i++){
            System.out.print("=");
        }
        System.out.print(" "+header+" ");
        for (int i = 0; i < bufferLength/2; i++){
            System.out.print("=");
        }
        System.out.println();
    }

    //I'm lazy and don't wanna type out s.out.println
    // and later might put some formatting here
    public static void print(String text){
        System.out.println(text);
    }

    //Adds a datetime before message
    public static void log(String text){
        String formattedTime = "[ " + convertTime(System.currentTimeMillis() - startTime)+" ] ";
        System.out.println(formattedTime + text);
    }


    public static String convertTime(long millis) {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(millis));
    }
}