package Output;

import Simulation.AngleDataPoint;
import Simulation.Model.Model;
import lombok.Data;

import java.util.ArrayList;

@Data
public class SimulationOutput {
    private ArrayList<AngleDataPoint> heelAngleDataPoints = new ArrayList<>();
    private ArrayList<AngleDataPoint> trimAngleDataPoints = new ArrayList<>();
    private Model model;
    private double mass;
    private AngleDataPoint stablePosition;
    private AngleDataPoint heelPointOfMaxStability;
    private AngleDataPoint trimPointOfMaxStability;
}
