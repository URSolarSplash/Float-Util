package UI;

import Output.SimulationOutput;
import Simulation.Model.Model;
import Simulation.Model.Triangle;
import Simulation.Model.Vector3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static java.lang.System.in;

public class ModelView extends JPanel {

    private SimulationOutput output;


    public ModelView() {
        super();
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Draw a test waterline
        int width = getWidth();
        int height = getHeight();
        Color waterColor = Color.BLUE;


        g.setColor(Color.GRAY);

        if (output != null) {
            //Draw our model on the screen
            ArrayList<Point> fillPoints = new ArrayList<>();
            for (Triangle tri : translatedModel.getTriangles()) {
                double x1 = (tri.getV1().getZ() * scale) + offsetX;
                double y1 = (tri.getV1().getY() * -scale) + offsetY;
                double x2 = (tri.getV2().getZ() * scale) + offsetX;
                double y2 = (tri.getV2().getY() * -scale) + offsetY;
                double x3 = (tri.getV3().getZ() * scale) + offsetX;
                double y3 = (tri.getV3().getY() * -scale) + offsetY;

                boolean wireframe = true;
                if (wireframe) {
                    g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                    g.drawLine((int)x2, (int)y2, (int)x3, (int)y3);
                    g.drawLine((int)x3, (int)y3, (int)x1, (int)y1);
                } else {
                    //fillPoints.add(new Point((int)x1, (int)y1));
                    //fillPoints.add(new Point((int)x2, (int)y2));
                    //fillPoints.add(new Point((int)x3, (int)y3));
                    //g.drawFilledLines(fillPoints, color);
                    //g.draw
                    //fillPoints.clear();
                }
            }
        }

        g.setColor(waterColor);
        g.drawLine(0,height/2,width,height/2);
    }

    double objectWidth;
    double objectHeight;
    double scaleOffset;
    double scale;
    double offsetX;
    double offsetY;
    Model translatedModel;

    // Update view coordinate system based on model size
    public void refresh(){
        double heel = output.getStablePosition().getHeelAngle();
        double trim = output.getStablePosition().getTrimAngle();
        output.getModel().setRotation(new Vector3(trim,0,heel));
        output.getModel().setPosition(new Vector3(0,output.getStablePosition().getY(),0));
        output.getModel().calculateCSG();

        translatedModel = new Model(output.getModel().getCsgModel());

        //side view (ZY)
        objectWidth = translatedModel.getBoundingBox().getDepth();
        objectHeight = translatedModel.getBoundingBox().getHeight();
        Vector3 cog = translatedModel.getCenterOfMass();

        int marginPx = 50;
        double frameWidth = getWidth();
        double frameHeight = getHeight();
        scale = Math.max(frameWidth/objectWidth,frameHeight/objectHeight);
        scale *= 0.25;

        offsetY = frameHeight/2;

    }

    public void setSimOutput(SimulationOutput out){
        output = out;
        refresh();
    }

    public void clear(){
        // Clears the output
        output = null;
    }
}
