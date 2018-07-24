package Simulation;

import Input.SimulationInput;
import Output.SimulationOutput;
import Simulation.Model.BoundingBox;
import Simulation.Model.Model;
import Simulation.Model.Vector3;
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
import java.util.concurrent.ThreadLocalRandom;

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


        //pb = new ProgressBar("Running Simulation...", 100, 100, System.out, ProgressBarStyle.ASCII).start();

        boolean converged = false;
        int iteration = 0;
        double angle = 0;
        double testCenterX = 0;
        double testCenterY = 0;
        double testCenterZ = 0;
        double modelY = 0;
        double waterDensityLbInCubed = 0.036;
        double modelDensity = in.getMass() / inputModel.getVolume();
        double waterMass = 0;
        Vector3 cogModel = new Vector3();
        Vector3 cogNegModel = new Vector3();
        double positiveForce = 0;
        double negativeForce = 0;
        double rightingMoment = 0;
        double bouyancyForce = 0;
        double simulatedAnnealingConstant = 0;
        double combinedRightingMoment = 0;
        double resultMultiplier = 0.1;

        double massFullWater = inputModel.getVolume() * waterDensityLbInCubed;
        log("- Specified mass for body: "+in.getMass());
        log("- Mass of water for full model displacement: "+massFullWater);
        boolean willSink = massFullWater < in.getMass();
        if (willSink){
            log("NOTE: Model will sink with specified mass! Algorithm will never converge, skipping.");
            Application.detailedError = "Model will sink with specified mass! Algorithm will never converge, skipping.";
            return null;
        }

        while (!converged){
            log("[i="+iteration+",a="+angle+",da="+combinedRightingMoment+",y="+modelY+",wat="+waterMass+",b="+bouyancyForce+"]");
            inputModel.setRotation(new Vector3(0,0,angle % 360));
            inputModel.setPosition(new Vector3(0,modelY,0));
            inputModel.calculateCSG();

            Model negModel = new Model(inputModel.getNegativeSlice());
            Model translatedModel = new Model(inputModel.getCsgModel());
            translatedModel.setMass(in.getMass());

            waterMass = negModel.getVolume() * waterDensityLbInCubed;

            cogModel = translatedModel.getCenterOfMass();
            cogNegModel = negModel.getCenterOfMass();

            positiveForce = waterMass;
            negativeForce = -translatedModel.getMass();
            if (positiveForce < 0) { positiveForce = 0; }
            rightingMoment = 0;
            if (negModel.getVolume() > 0.0001) {
                rightingMoment = cogModel.getX() - cogNegModel.getX();
            }


            bouyancyForce = positiveForce+ negativeForce;
            simulatedAnnealingConstant = (ThreadLocalRandom.current().nextDouble(0.1)-0.05)/(iteration+1);
            combinedRightingMoment = rightingMoment*resultMultiplier + simulatedAnnealingConstant;
            angle+=combinedRightingMoment;
            modelY+=bouyancyForce*resultMultiplier;

            if (rightingMoment == 0 && bouyancyForce == 0){
                converged = true;
            }

            if (iteration >= pb.getMax() - 10){
                //pb.maxHint(iteration+20);
            }
            //pb.stepBy(1);
            iteration++;
            try {
                Thread.sleep(1000);
            } catch (Exception e){

            }
        }

        // Iterations done
        log("angle = "+angle);
        log("modelY = "+modelY);
        log("displaced water mass = "+waterMass);
        log("cogModel = "+cogModel);
        log("cogNegModel = "+cogNegModel);
        log("pos force (lbs): "+ String.format("%.4f",positiveForce));
        log("neg force (lbs): "+ String.format("%.4f",negativeForce));
        log("bouyancy force = "+bouyancyForce);
        log("righting moment = "+rightingMoment);
        log("simulated annealing const = "+simulatedAnnealingConstant);
        log("combined righting moment = "+combinedRightingMoment);


        //pb.stop();

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
