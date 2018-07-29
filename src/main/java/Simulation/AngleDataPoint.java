package Simulation;

import lombok.Data;

// Stores data for a given angle
@Data
public class AngleDataPoint {
    private double heelAngle;
    private double trimAngle;
    private double heelRightingMoment;
    private double trimRightingMoment;
    private double displacement;
    private double y;
}
