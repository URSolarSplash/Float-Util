package Simulation;

import Display.Point;
import Display.RGBA;
import Display.RenderWindow;
import Model.*;
import Parser.ModelParser;
import Parser.ModelParserLibrary;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.FileUtil;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.vvecmath.Transform;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class FloatUtilMain {

    public static String units = "in";

    public static void main(String[] args) {




        log("Test --- getting positive and negative slice and saving...");

        inputModel.setRotation(new Vector3(0,0,45));
        inputModel.calculateCSG();
        CSG pos = inputModel.getPositiveSlice();
        CSG neg = inputModel.getNegativeSlice();
        saveSTL(pos,"pos.stl");
        saveSTL(neg,"neg.stl");

        printHeader("Simulation Graphics Setup");
        window = new RenderWindow(1024,768);
        window.init();
        log("Initialized OpenGL graphics context.");

        printHeader("Simulation");

        // Perform the buoyancy simulation until we converge on a solution
        boolean converged = false;
        int iteration = 0;

        log("Calculating display settings...");
        //Calculate object bounds and use that to scale window
        objectWidth = inputModel.getBoundingBox().getWidth();
        objectHeight = inputModel.getBoundingBox().getHeight();
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
        offsetX = window.WIDTH / 2; // make (0,0) center of the window.
        offsetY = window.HEIGHT / 2;

        log("Simulating...");
        double angle = 0;
        double testCenterX = 0;
        double testCenterY = 0;
        double testCenterZ = 0;

        double modelY = 0;
        double waterDensityLbInCubed = 0.036;

        double testBouyancy = 0.0260416667;

        angle = 45;

        while (!converged & !window.shouldAbort()){
            window.startFrame();

            log("Calculate slice models for iteration "+iteration+"...");
            inputModel.setRotation(new Vector3(0,0,angle % 360));
            inputModel.setPosition(new Vector3(0,modelY,0));
            inputModel.calculateCSG();
            Model negModel = new Model(inputModel.getNegativeSlice());
            Model translatedModel = new Model(inputModel.getCsgModel());
            //translatedModel.setMass(0.1);
            translatedModel.setMass(750); //100 lb model
            //Model posModel = new Model(inputModel.getPositiveSlice());

            window.setFont("default",18);
            window.drawText(10,10,"Float-Util",window.rochesterYellow);
            window.setFont("monospace",16);
            int y = 26;
            window.drawText(10,y,"iteration "+iteration,window.rochesterYellow); y += 16;
            //window.drawText(10,y,"Above water mass: "+ posModel.getMass(),window.rochesterYellow); y += 16;
            //window.drawText(10,y,"Above water density: "+ posModel.getDensity(),window.rochesterYellow); y += 16;
            window.drawText(10,y,"Below water mass: "+ String.format("%.4f",negModel.getMass()),window.rochesterYellow); y += 16;
            window.drawText(10,y,"Below water density: "+ String.format("%.4f",negModel.getDensity()),window.rochesterYellow); y += 16;
            //double splitMass = negModel.getMass() + posModel.getMass();
            //double massError = Math.abs(inputModel.getMass() - splitMass);
            window.drawText(10,y,"mass actual: "+ String.format("%.4f",inputModel.getMass()),window.rochesterYellow); y += 16;

            double waterMass = negModel.getVolume() * waterDensityLbInCubed;

            window.drawText(10,y,"volume underwater: "+ String.format("%.4f",negModel.getVolume()),window.rochesterYellow); y += 16;
            window.drawText(10,y,"water displaced (lbs): "+ String.format("%.4f",waterMass),window.rochesterYellow); y += 16;
            //window.drawText(10,y,"mass error: "+ massError,window.rochesterYellow); y += 16;

            //Draw positive section
            drawModel(translatedModel,window.rochesterYellow,false);

            //Draw negative section
            drawModel(negModel,window.cyan,false);

            //draw waterline
            window.drawLine((objectWidth*-2 * scale) + offsetX,(0 * scale) + offsetY,(objectWidth*2 * scale) + offsetX,(0 * scale) + offsetY,window.cyan);

            //Get center of gravity for both sections
            // Use a test for now
            testCenterY+=glfwGetKey(window.window, GLFW.GLFW_KEY_UP)*0.001 - glfwGetKey(window.window, GLFW.GLFW_KEY_DOWN)*0.001;
            testCenterX+=-glfwGetKey(window.window, GLFW.GLFW_KEY_LEFT)*0.1 + glfwGetKey(window.window, GLFW.GLFW_KEY_RIGHT)*0.1;

            //Vector3 cogModel = scalePosition(new Vector3(testCenterX,testCenterY,0));
            Vector3 cogModel = scalePosition(translatedModel.getCenterOfMass());
            cogModel.setX(cogModel.getX()+ testCenterX*scale);
            Vector3 cogNegModel = scalePosition(negModel.getCenterOfMass());


            double positiveForce = waterMass;
            double negativeForce = -translatedModel.getMass();
            if (positiveForce < 0) { positiveForce = 0; }
            double rightingMoment = 0;
            if (negModel.getVolume() > 0.0001) {
                rightingMoment = cogModel.getX() - cogNegModel.getX();
            }

            window.drawText(10,y,"-------",window.rochesterYellow); y += 16;
            window.drawText(10,y,"COG offset: "+ String.format("%.4f",testCenterX),window.rochesterYellow); y += 16;
            window.drawText(10,y,"density (lbs/in3): "+ String.format("%.4f",testBouyancy),window.rochesterYellow); y += 16;
            window.drawText(10,y,"pos force (lbs): "+ String.format("%.4f",positiveForce),window.rochesterYellow); y += 16;
            window.drawText(10,y,"neg force (lbs): "+ String.format("%.4f",negativeForce),window.rochesterYellow); y += 16;

            double bouyancyForce = positiveForce+ negativeForce;
            double angleOffset = 0;//-glfwGetKey(window.window, GLFW.GLFW_KEY_LEFT) + glfwGetKey(window.window, GLFW.GLFW_KEY_RIGHT);
            angle+=rightingMoment*0.01 + 0.0001 + angleOffset;
            modelY+=bouyancyForce*0.01;
            testBouyancy = testCenterY;

            drawBoundingBox(inputModel.getCSGBoundingBox(),new RGBA(255,255,255,50));
            drawBoundingBox(negModel.getBoundingBox(),new RGBA(255,255,255,50));

            window.drawCircle(cogModel.getX(),cogModel.getY(),10,window.white);
            window.drawCircle(cogModel.getX(),cogModel.getY(),8,window.black);
            window.drawCircle(cogModel.getX(),cogModel.getY(),6,window.white);
            window.drawCircle(cogModel.getX(),cogModel.getY(),4,window.black);
            window.drawCircle(cogNegModel.getX(),cogNegModel.getY(),10,window.white);
            window.drawCircle(cogNegModel.getX(),cogNegModel.getY(),8,window.black);
            window.drawCircle(cogNegModel.getX(),cogNegModel.getY(),6,window.white);
            window.drawCircle(cogNegModel.getX(),cogNegModel.getY(),4,window.black);

            window.endFrame();
            iteration++;
        }
        window.terminate();

        log("Done.");


    }

    static RenderWindow window;
    static double objectWidth;
    static double objectHeight;
    static double scaleOffset;
    static double scale;
    static double offsetX;
    static double offsetY;
    static double offsetZ;

    public static Vector3 scalePosition(Vector3 in){
        return new Vector3((in.getX() * scale) + offsetX,(in.getY() * -scale) + offsetY,in.getZ());
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

        //Draw X lines
        window.drawLine(x1,y1,x1,y1,color);
        window.drawLine(x2,y1,x2,y1,color);
        window.drawLine(x1,y2,x1,y2,color);
        window.drawLine(x2,y2,x2,y2,color);
        //Draw Y lines
        window.drawLine(x1,y1,x1,y2,color);
        window.drawLine(x2,y1,x2,y2,color);
        window.drawLine(x1,y1,x1,y2,color);
        window.drawLine(x2,y1,x2,y2,color);
        //Draw Z lines
        window.drawLine(x1,y1,x2,y1,color);
        window.drawLine(x1,y2,x2,y2,color);
        window.drawLine(x1,y1,x2,y1,color);
        window.drawLine(x1,y2,x2,y2,color);
    }

    public static void drawModel(Model in, RGBA color, boolean wireframe){

        //Draw our model on the screen
        ArrayList<Point> fillPoints = new ArrayList<>();
        for (Triangle tri : in.getTriangles()){
            double x1 = (tri.getV1().getX() * scale) + offsetX;
            double y1 = (tri.getV1().getY() * -scale) + offsetY;
            double x2 = (tri.getV2().getX() * scale) + offsetX;
            double y2 = (tri.getV2().getY() * -scale) + offsetY;
            double x3 = (tri.getV3().getX() * scale) + offsetX;
            double y3 = (tri.getV3().getY() * -scale) + offsetY;
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
        }
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

    public static void saveSTL(CSG in, String filename){
        //Save model to stl
        try {
            FileUtil.write(Paths.get(filename),
                    in.toStlString()
            );
        } catch (IOException ex) {
        }
    }
}
