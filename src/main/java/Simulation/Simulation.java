package Simulation;

import Input.SimulationInput;
import Output.SimulationOutput;
import Simulation.Model.BoundingBox;
import Simulation.Model.Model;
import Simulation.Parser.ModelParser;
import Simulation.Parser.ModelParserLibrary;
import Utility.Application;
import Utility.PathUtilities;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Simulation {

    private static long startTime = System.currentTimeMillis();

    public static SimulationOutput run(SimulationInput in, String workingDirectory){
        startTime = System.currentTimeMillis();

        SimulationOutput simulationOutput = new SimulationOutput();

        ProgressBar pb = new ProgressBar("Initializing Simulation...", 4, 100, System.out, ProgressBarStyle.ASCII).start();

        // Convert the model path to a full directory.
        String modelFullPath = workingDirectory + "/" + in.getModel();
        pb.stepBy(1);

        // Validate that we can open the file
        if (!PathUtilities.isValidFile(modelFullPath)){
            Application.detailedError = "Could not load model file - Invalid file '"+modelFullPath+"'!";
            pb.stop();
            return null;
        }
        pb.stepBy(1);

        // Configure Model parser and load the model to a Model object
        ModelParserLibrary parserLib = new ModelParserLibrary();
        String modelExtension = FilenameUtils.getExtension(modelFullPath);
        ModelParser parser = parserLib.getParser(modelExtension);
        pb.stepBy(1);

        if (parser == null){
            Application.detailedError = "No parser for model file extension '"+modelExtension+"'!";
            pb.stop();
            return null;
        }

        Model inputModel = parser.parse(new File(modelFullPath));
        pb.stepBy(1);
        pb.stop();

        log("Loaded model. Model basic statistics:");
        log("- Num triangles: "+inputModel.getTriangles().size());
        log("- Num vertices: "+inputModel.getVertices().size());

        if (inputModel.getTriangles().size() == 0){
            log("Warning: Your model has 0 triangles!");
            log("Note: Model must be in ASCII STL format.");
            log("No calculation will be performed.");
            return null;
        }

        BoundingBox modelSize = inputModel.getBoundingBox();
        log("- Model width: "+modelSize.getWidth()+" "+in.getUnits());
        log("- Model height: "+modelSize.getHeight()+" "+in.getUnits());
        log("- Model depth: "+modelSize.getDepth()+" "+in.getUnits());
        log("- Model volume: "+ inputModel.getVolume()+" "+in.getUnits()+"^3");
        log("- Model surface area: "+ inputModel.getSurfaceArea()+" "+in.getUnits()+"^2");
        log("--- Generating CSG Mesh ----");
        log("Generating...");
        inputModel.calculateCSG();
        log("Done.");

        log("- CSG model num triangles: "+inputModel.getCsgModel().getPolygons().size());
        log("- CSG model num vertices: "+inputModel.getCsgModel().getPolygons().size()*3);
        log("- CSG model width: "+modelSize.getWidth()+" "+in.getUnits());
        log("- CSG model height: "+modelSize.getHeight()+" "+in.getUnits());
        log("- CSG model depth: "+modelSize.getDepth()+" "+in.getUnits());

        return simulationOutput;
    }

    public static void log(String text){
        String formattedTime = "[ " + convertTime(System.currentTimeMillis() - startTime)+" ] ";
        System.out.println(formattedTime + text);
    }

    public static String convertTime(long millis) {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(millis));
    }

}
