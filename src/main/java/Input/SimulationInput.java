package Input;

import lombok.Data;

@Data
public class SimulationInput {
    // Model
    private String model;

    // Initial position
    private float x;
    private float y;
    private float z;
    private float rx;
    private float ry;
    private float rz;

    // mass of the object
    private float mass;

    // Simulation metadata / settings
    private String units;
}
