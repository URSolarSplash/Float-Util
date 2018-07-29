package Output;

import Simulation.AngleDataPoint;
import Utility.Log;

import java.io.FileWriter;
import java.io.PrintWriter;

public class CSVWriter implements OutputWriter {
    @Override
    public void write(SimulationOutput out, String file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (AngleDataPoint angleDataPoint : out.getTrimAngleDataPoints()){
                printWriter.printf("%f,%f,%f,%f,%f\n",
                        angleDataPoint.getHeelAngle(),
                        angleDataPoint.getTrimAngle(),
                        angleDataPoint.getHeelRightingMoment(),
                        angleDataPoint.getTrimRightingMoment(),
                        angleDataPoint.getDisplacement());
            }

            printWriter.close();
        } catch (Exception e){
            Log.log("Error: Unable to write output CSV file!");
        }
    }
}
