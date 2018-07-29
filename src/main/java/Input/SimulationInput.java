package Input;

import lombok.Data;

@Data
public class SimulationInput {
    // Model
    private String model;

    // mass of the object
    private float mass;

    // Simulation metadata / settings
    private String units;
    // angle test range, use this to exclude obviously irrelevant values.
    private float minTrimAngle;
    private float maxTrimAngle;
    private float minHeelAngle;
    private float maxHeelAngle;

    // Simple COG offset, in model coordinates
    private float cogOffsetX;
    private float cogOffsetY;
    private float cogOffsetZ;

    //angle step size
    private float angleStepSize;
}
