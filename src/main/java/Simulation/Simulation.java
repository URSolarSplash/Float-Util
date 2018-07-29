package Simulation;

import Input.InputReader;
import Input.SimulationInput;
import Output.SimulationOutput;
import Renderer.Point;
import Renderer.RGBA;
import Renderer.RenderWindow;
import Simulation.Model.BoundingBox;
import Simulation.Model.Model;
import Simulation.Model.Triangle;
import Simulation.Model.Vector3;
import Simulation.Parser.ModelParser;
import Simulation.Parser.ModelParserLibrary;
import Utility.Application;
import Utility.PathUtilities;
import eu.mihosoft.vvecmath.ModifiableVector3dImpl;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static Utility.Log.log;

public class Simulation {

    public static boolean debug = true;
    private static int viewPlane = 0;
    static RenderWindow window;
    static double objectWidth;
    static double objectHeight;
    static double scaleOffset;
    static double scale;
    static double offsetX;
    static double offsetY;
    static double offsetZ;
    private static final double waterDensityLbInCubed = 0.036;

    public static boolean checkInputValid(String inFile){

        SimulationInput simulationInput = new InputReader().read(inFile);
        if (simulationInput == null){
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkModelDimensions(SimulationInput in, String workingDirectory){

        Model inputModel = getModel(in,workingDirectory);
        if (inputModel == null){
            return false;
        }

        //Check if model is the wrong orientation / shape
        // Model's width and height should be less than the length
        // Depth (length, Z axis) should be the largest coordinate
        if (inputModel.getBoundingBox().getWidth() > inputModel.getBoundingBox().getDepth()){
            Application.detailedError = "Model orientation incorrect - Width (X) is greater than depth (Z)! Please verify that centerline of body follows Z axis!";
            return false;
        }
        if (inputModel.getBoundingBox().getHeight() > inputModel.getBoundingBox().getDepth()){
            Application.detailedError = "Model orientation incorrect - Height (Y) is greater than depth (Z)! Please verify that centerline of body follows Z axis!";
            return false;
        }
        return true;
    }

    public static Model getModel(SimulationInput in, String workingDirectory){
        ProgressBar pb = new ProgressBar("Initializing Simulation...", 4, 100, System.out, ProgressBarStyle.ASCII).start();

        // Convert the model path to a full directory.
        String slash = "";
        if (!workingDirectory.endsWith("/")){
            slash = "/";
        }
        String modelFullPath = workingDirectory + slash + in.getModel();
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
        return inputModel;
    }

    public static SimulationOutput run(SimulationInput in, String workingDirectory){
        SimulationOutput simulationOutput = new SimulationOutput();

        Model inputModel = getModel(in,workingDirectory);
        simulationOutput.setModel(inputModel);

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

        if (debug){
            log("Simulation Graphics Setup");
            window = new RenderWindow(1024,768);
            window.init();
            log("Initialized OpenGL graphics context.");

            log("Calculating display settings...");
            //Calculate object bounds and use that to scale window
            if (viewPlane == 0) {
                // front view (XY)
                objectWidth = inputModel.getBoundingBox().getWidth();
                objectHeight = inputModel.getBoundingBox().getHeight();
            } else if (viewPlane == 1){
                //side view (ZY)
                objectWidth = inputModel.getBoundingBox().getDepth();
                objectHeight = inputModel.getBoundingBox().getHeight();
            }
            scaleOffset = 0.4; //leave a margin on the sides
            scale = 0;

            double objectRatio = objectWidth / objectHeight;
            double screenRatio = window.WIDTH / window.HEIGHT;

            if (objectRatio <= screenRatio){
                // Use height as the scaling dimension
                scale = ((window.HEIGHT * scaleOffset) / objectHeight);
            } else {
                // Use width as the scaling dimension
                scale = ((window.WIDTH * scaleOffset) / objectWidth);
            }
            if (viewPlane == 0) {
                offsetX = window.WIDTH / 2; // make (0,0) center of the window.
                offsetY = window.HEIGHT / 2;
            } else if (viewPlane == 1){
                offsetZ = window.WIDTH / 2; // make (0,0) center of the window.
                offsetY = window.HEIGHT / 2;
            }
        }

        double massFullWater = inputModel.getVolume() * waterDensityLbInCubed;
        log("- Specified mass for body: "+in.getMass());
        log("- Mass of water for full model displacement: "+massFullWater);
        boolean willSink = massFullWater < in.getMass();
        if (willSink){
            log("NOTE: Model will sink with specified mass! Algorithm will never converge, skipping.");
            Application.detailedError = "Model will sink with specified mass! Algorithm will never converge, skipping.";
            return null;
        }

        // Use continuous simulation to get stability points for heel and trim
        //AngleDataPoint stablePosition = floatForAngles(in,inputModel,0,0,false,debug ? window : null);

        // Get heel data points
        // Use the stable trim position here for maximum accuracy
        for (double heelAngle = in.getMinHeelAngle(); heelAngle <= in.getMaxHeelAngle(); heelAngle += in.getAngleStepSize()) {
            AngleDataPoint pointForAngles = floatForAngles(in,inputModel,heelAngle,0,true,debug ? window : null);
            simulationOutput.getHeelAngleDataPoints().add(pointForAngles);
        }

        // Get trim data points
        // Use the stable heel position here for maximum accuracy
        for (double trimAngle = in.getMinTrimAngle(); trimAngle <= in.getMaxTrimAngle(); trimAngle += in.getAngleStepSize()) {
            AngleDataPoint pointForAngles = floatForAngles(in,inputModel,0,trimAngle,true,debug ? window : null);
            simulationOutput.getHeelAngleDataPoints().add(pointForAngles);
        }

        if (debug) {
            window.terminate();
        }

        // Calculate other simulation output metadata
        simulationOutput.setMass(in.getMass());

        double bestRightingMoment = Double.MIN_VALUE;
        AngleDataPoint bestHeel = null;
        for (AngleDataPoint dp : simulationOutput.getHeelAngleDataPoints()){
            if (dp.getHeelRightingMoment() > bestRightingMoment){
                bestRightingMoment = dp.getHeelRightingMoment();
                bestHeel = dp;
            }
        }

        bestRightingMoment = Double.MIN_VALUE;
        double smallestRightingMoment = Double.MAX_VALUE;
        AngleDataPoint bestTrim = null;
        AngleDataPoint stableTrim = null;
        for (AngleDataPoint dp : simulationOutput.getTrimAngleDataPoints()){
            if (dp.getTrimRightingMoment() > bestRightingMoment){
                bestRightingMoment = dp.getTrimRightingMoment();
                bestTrim = dp;
            }
            if (Math.abs(dp.getTrimRightingMoment()) < smallestRightingMoment){
                smallestRightingMoment = dp.getTrimRightingMoment();
                stableTrim = dp;
            }
        }

        // Find the trim angle with the lowest righting arm
        simulationOutput.setStablePosition(stableTrim);
        simulationOutput.setHeelPointOfMaxStability(bestHeel);
        simulationOutput.setTrimPointOfMaxStability(bestTrim);

        return simulationOutput;
    }

    /**
     * Given a model and given angles, floats the model, figuring out the waterline position at this angle.
     * Waterline position is given in model coordinates.
     */
    private static AngleDataPoint floatForAngles(SimulationInput in,Model inputModel,double heelAngle,double trimAngle, boolean forceAngle, RenderWindow window){
        AngleDataPoint resultDataPoint =  new AngleDataPoint();
        ProgressBar pb = new ProgressBar(String.format("Heel: %.2f, Trim: %.2f",heelAngle,trimAngle), iterationHint, 100, System.out, ProgressBarStyle.ASCII).start();

        // Initialize temporary variables for this step.
        boolean converged = false;
        int iteration = 0;

        if (window != null){

        }

        int y = 0;
        double modelY;
        Vector3 cogModel;
        Vector3 cogNegModel;
        int moveDirection = 0;

        // Get the height from the input model position
        // This allows quicker calculations since we use the position given by the previous float step
        // Height will start at 0 initially and then quickly stabilize at a useful position
        modelY = inputModel.getPosition().getY();

        // Accuracy in inches or whatever the model units are
        // use 0.01 inches as default
        double moveSpeed = 0.01; //inputModel.getBoundingBox().getHeight()/100.0;

        // Temporary move model to origin to calculate true COG
        inputModel.setRotation(new Vector3(0,0,0));
        inputModel.setPosition(new Vector3(0,0,0));
        inputModel.calculateCSG();
        Vector3 cogOrigin = inputModel.getCenterOfMass();

        while (!converged){
            if (window != null){
                if (window.shouldAbort()) {
                    Application.detailedError = "Cancelled by user.";
                    return null;
                }
                window.startFrame();
            }

            // Translate the input model
            inputModel.setRotation(new Vector3(trimAngle % 360,0,heelAngle % 360));
            inputModel.setPosition(new Vector3(0,modelY,0));
            inputModel.calculateCSG();

            // Get below-water slice and find mass of displaced water
            Model negModel = new Model(inputModel.getNegativeSlice());
            Model translatedModel = new Model(inputModel.getCsgModel());
            translatedModel.setMass(in.getMass());
            double waterMass = negModel.getVolume() * waterDensityLbInCubed;

            if (window != null) {
                window.setFont("default", 18);
                window.drawText(10, 10, "Float-Util", window.rochesterYellow);
                window.setFont("monospace", 16);
                y = 26;
                window.drawText(10, y, "iteration " + iteration, window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "volume underwater: " + String.format("%.4f", negModel.getVolume()), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "water displaced (lbs): " + String.format("%.4f", waterMass), window.rochesterYellow);
                y += 16;
                //Draw positive section
                drawModel(translatedModel, window.rochesterYellow, false);
                //Draw negative section
                drawModel(negModel, window.cyan, false);
                //Draw waterline
                window.drawLine((objectWidth * -2 * scale) + offsetX, (0 * scale) + offsetY, (objectWidth * 2 * scale) + offsetX, (0 * scale) + offsetY, window.cyan);
            }

            // Get the center of masses for the models for calculating the righting moment
            cogModel = (translatedModel.getCenterOfMass());

            // Apply COG offset based on load case
            Transform transform = new Transform();
            transform.rot(trimAngle,0,heelAngle);
            transform.translate(new Vector3(in.getCogOffsetX(),in.getCogOffsetY(),in.getCogOffsetZ()));

            Vector3d vvec = (new Vector3()).transformed(transform);
            Vector3 offsetCog = new Vector3(vvec.getX() + cogModel.getX(),vvec.getY() + cogModel.getY(),vvec.getZ() + cogModel.getZ());

            cogNegModel = (negModel.getCenterOfMass());

            double positiveForce = waterMass;
            double negativeForce = -translatedModel.getMass();

            // Calculate the righting moment based on the center of masses
            // Distance between COGs multiplied by boat mass
            double heelRightingMoment = (cogNegModel.getX() - offsetCog.getX()) * in.getMass();
            double trimRightingMoment = (cogNegModel.getZ() - offsetCog.getZ()) * in.getMass();

            // If we are not running in "force angle mode", apply the righting moment to the angle
            double angleScalar = 0.0001;
            double scaledHeelRightingMoment = heelRightingMoment * angleScalar;
            double scaledTrimRightingMoment = trimRightingMoment * angleScalar;
            double mantissa = 0.01;
            boolean angleConverged = false;
            if (!forceAngle){
                if (!Double.isNaN(scaledHeelRightingMoment) & !Double.isNaN(scaledTrimRightingMoment)) {
                    trimAngle = (trimAngle + scaledHeelRightingMoment) % 360;
                    heelAngle = (heelAngle + scaledTrimRightingMoment) % 360;
                    if (Math.abs(scaledHeelRightingMoment) <= mantissa & Math.abs(scaledTrimRightingMoment) <= mantissa) {
                        //angleConverged = true;
                    }
                }
            } else {
                angleConverged = true;
            }

            // Clamp the positive force
            if (positiveForce < 0) {
                positiveForce = 0;
            }
            double buoyancyForce = positiveForce + negativeForce;

            if (moveDirection == 0){
                // Decide whether to float or sink
                if (buoyancyForce < 0){
                    moveDirection = 1;
                } else if (buoyancyForce > 0){
                    moveDirection = 2;
                }
            } else if (moveDirection == 1){
                // Sinking
                modelY-=moveSpeed;
                // Check if we've converged and the buoyancy has flipped to pushing up
                if (buoyancyForce > 0){
                    if (angleConverged) {
                        converged = true;
                    }
                }

            } else {
                // Floating
                modelY+=moveSpeed;
                // Check if we've converged and the buoyancy has flipped to pushing down
                if (buoyancyForce < 0){
                    if (angleConverged) {
                        converged = true;
                    }
                }
            }


            // Update the forces in the output object
            resultDataPoint.setHeelAngle(heelAngle);
            resultDataPoint.setTrimAngle(trimAngle);
            resultDataPoint.setDisplacement(waterMass);
            resultDataPoint.setHeelRightingMoment(heelRightingMoment);
            resultDataPoint.setTrimRightingMoment(trimRightingMoment);
            resultDataPoint.setY(modelY);

            //Render debug graphics window
            if (window != null) {
                window.drawText(10, y, "-------", window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "heel righting moment: " + String.format("%.4f", heelRightingMoment), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "trim righting moment: " + String.format("%.4f", trimRightingMoment), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "buoyancy force: " + String.format("%.4f", buoyancyForce), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "trim angle: " + String.format("%.4f", trimAngle), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "heel angle: " + String.format("%.4f", heelAngle), window.rochesterYellow);
                y += 16;
                window.drawText(10, y, "y: " + String.format("%.4f", modelY), window.rochesterYellow);
                y += 16;

                // SCALE COG AND COG NEG FOR RENDERING
                cogNegModel = scalePosition(cogNegModel);
                offsetCog = scalePosition(offsetCog);

                drawBoundingBox(inputModel.getCSGBoundingBox(), new RGBA(255, 255, 255, 50));
                drawBoundingBox(negModel.getBoundingBox(), new RGBA(255, 255, 255, 50));

                // Draw center of gravity circles
                drawCogCircle(window,cogNegModel.getX(),cogNegModel.getY(),cogNegModel.getZ());
                drawCogCircle(window,offsetCog.getX(),offsetCog.getY(),offsetCog.getZ());
                window.endFrame();
            }

            // Update progress bar
            pb.stepTo(iteration++);
            if (iteration > pb.getMax() - 10){
                pb.maxHint(iteration+10);
            }
        }
        pb.maxHint(iteration-1);
        pb.stop();
        iterationHint = iteration;


        return resultDataPoint;
    }

    static long iterationHint = 100;

    public static void drawCogCircle(RenderWindow window, double x, double y, double z){
        if (viewPlane == 0) {
            window.drawCircle(x, y, 10, window.white);
            window.drawCircle(x, y, 8, window.black);
            window.drawCircle(x, y, 6, window.white);
            window.drawCircle(x, y, 4, window.black);
        } else if (viewPlane == 1){
            window.drawCircle(z, y, 10, window.white);
            window.drawCircle(z, y, 8, window.black);
            window.drawCircle(z, y, 6, window.white);
            window.drawCircle(z, y, 4, window.black);
        }
    }


    public static Vector3 scalePosition(Vector3 in){
        return new Vector3(scaleX(in.getX()),scaleY(in.getY()),scaleZ(in.getZ()));
    }

    public static double scaleX(double in){
        return (in * scale) + offsetX;
    }
    public static double scaleY(double in){
        return (in * -scale) + offsetY;
    }
    public static double scaleZ(double in){
        return (in * scale) + offsetZ;
    }

    public static void drawBoundingBox(BoundingBox in, RGBA color){
        double x1 = in.getCenter().getX() - in.getWidth()/2;
        double x2 = in.getCenter().getX() + in.getWidth()/2;
        double y1 = in.getCenter().getY() - in.getHeight()/2;
        double y2 = in.getCenter().getY() + in.getHeight()/2;
        double z1 = in.getCenter().getZ() - in.getDepth()/2;
        double z2 = in.getCenter().getZ() + in.getDepth()/2;
        x1 = scaleX(x1);
        x2 = scaleX(x2);
        y1 = scaleY(y1);
        y2 = scaleY(y2);
        z1 = scaleZ(z1);
        z2 = scaleZ(z2);

        if (viewPlane == 0) {
            //Draw X lines
            window.drawLine(x1,y1,x2,y1,color);
            window.drawLine(x1,y2,x2,y2,color);
            window.drawLine(x1,y1,x2,y1,color);
            window.drawLine(x1,y2,x2,y2,color);
            //Draw Y lines
            window.drawLine(x1,y1,x1,y2,color);
            window.drawLine(x2,y1,x2,y2,color);
            window.drawLine(x1,y1,x1,y2,color);
            window.drawLine(x2,y1,x2,y2,color);
        } else if (viewPlane == 1){
            //Draw Z lines
            window.drawLine(z1,y1,z2,y1,color);
            window.drawLine(z1,y2,z2,y2,color);
            window.drawLine(z1,y1,z2,y1,color);
            window.drawLine(z1,y2,z2,y2,color);
            //Draw Y lines
            window.drawLine(z1,y1,z1,y2,color);
            window.drawLine(z2,y1,z2,y2,color);
            window.drawLine(z1,y1,z1,y2,color);
            window.drawLine(z2,y1,z2,y2,color);
        }
    }

    public static void drawModel(Model in, RGBA color, boolean wireframe){

        //Draw our model on the screen
        ArrayList<Point> fillPoints = new ArrayList<>();
        for (Triangle tri : in.getTriangles()){
            double x1 = (tri.getV1().getX() * scale) + offsetX;
            double y1 = (tri.getV1().getY() * -scale) + offsetY;
            double z1 = (tri.getV1().getZ() * scale) + offsetZ;
            double x2 = (tri.getV2().getX() * scale) + offsetX;
            double y2 = (tri.getV2().getY() * -scale) + offsetY;
            double z2 = (tri.getV2().getZ() * scale) + offsetZ;
            double x3 = (tri.getV3().getX() * scale) + offsetX;
            double y3 = (tri.getV3().getY() * -scale) + offsetY;
            double z3 = (tri.getV3().getZ() * scale) + offsetZ;

            if (viewPlane == 0) {
                if (wireframe) {
                    window.drawLine(x1, y1, x2, y2, color);
                    window.drawLine(x2, y2, x3, y3, color);
                    window.drawLine(x3, y3, x1, y1, color);
                } else {
                    fillPoints.add(new Point(x1,y1));
                    fillPoints.add(new Point(x2,y2));
                    fillPoints.add(new Point(x3,y3));
                    window.drawFilledLines(fillPoints,color);
                    fillPoints.clear();
                }
            } else if (viewPlane == 1){
                if (wireframe) {
                    window.drawLine(z1, y1, z2, y2, color);
                    window.drawLine(z2, y2, z3, y3, color);
                    window.drawLine(z3, y3, z1, y1, color);
                } else {
                    fillPoints.add(new Point(z1,y1));
                    fillPoints.add(new Point(z2,y2));
                    fillPoints.add(new Point(z3,y3));
                    window.drawFilledLines(fillPoints,color);
                    fillPoints.clear();
                }
            }

        }
    }
}
