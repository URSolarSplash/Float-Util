package Input;

import Utility.Application;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class InputReader {
    public SimulationInput read(String filename){
        // Try to parse the scenario from JSON file to object
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(new File(filename), SimulationInput.class);
        } catch (Exception e){
            Application.detailedError = e.getMessage();
            return null;
        }
    }
}
