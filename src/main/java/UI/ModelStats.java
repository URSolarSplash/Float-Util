package UI;

import Output.SimulationOutput;
import Simulation.Model.Model;

import javax.swing.*;
import java.awt.*;

public class ModelStats extends JPanel {
    private SimulationOutput output;
    private JLabel modelWidth;
    private JLabel modelHeight;
    private JLabel modelDepth;
    private JLabel modelMass;
    private JLabel stableHeelAngle;
    private JLabel stableTrimAngle;
    private JLabel stableTrimYOffset;
    private JLabel pointOfMaxStability;

    public ModelStats() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        modelWidth = new JLabel("Model Width: ");
        add(modelWidth,c);
        c.gridy++;

        modelHeight = new JLabel("Model Height: ");
        add(modelHeight,c);
        c.gridy++;

        modelDepth = new JLabel("Model Depth: ");
        add(modelDepth,c);
        c.gridy++;

        modelMass = new JLabel("Model Mass: ");
        add(modelMass,c);
        c.gridy++;

        stableHeelAngle = new JLabel("Stable Heel Angle: ");
        add(stableHeelAngle,c);
        c.gridy++;
        stableTrimAngle = new JLabel("Stable Trim Angle: ");
        add(stableTrimAngle,c);
        c.gridy++;
        stableTrimYOffset = new JLabel("Floated Y Offset: ");
        add(stableTrimYOffset,c);
        c.gridy++;
        pointOfMaxStability = new JLabel("Heel Point of Max. Stability: ");
        add(pointOfMaxStability,c);
        c.gridy++;
    }

    //Sets the model, and updates all labels
    public void setSimOutput(SimulationOutput out){
        output = out;
        update();
    }

    public void update(){

        if (output == null){
            modelWidth.setText("Model Width: ");
            modelHeight.setText("Model Height: ");
            modelDepth.setText("Model Depth: ");
            modelMass.setText("Model Mass: ");
            stableHeelAngle.setText("Stable Heel Angle: ");
            stableTrimAngle.setText("Stable Trim Angle: ");
            stableTrimYOffset.setText("Stable Trim Y Offset: ");
            pointOfMaxStability.setText("Point of Max Stability: ");
        } else {
            modelWidth.setText("Model Width: "+output.getModel().getBoundingBox().getWidth());
            modelHeight.setText("Model Height: "+output.getModel().getBoundingBox().getHeight());
            modelDepth.setText("Model Depth: "+output.getModel().getBoundingBox().getDepth());
            modelMass.setText("Model Mass: "+output.getMass());
            stableHeelAngle.setText("Stable Heel Angle: "+output.getStablePosition().getHeelAngle());
            stableTrimAngle.setText("Stable Trim Angle: "+output.getStablePosition().getTrimAngle());
            stableTrimYOffset.setText("Stable Trim Y Offset: "+output.getStablePosition().getY());
            pointOfMaxStability.setText("Point of Max Stability: "+output.getHeelPointOfMaxStability().getHeelAngle());
        }
    }

    public void clear(){
        // Clears the output
        output = null;
    }
}
