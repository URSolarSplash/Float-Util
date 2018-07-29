package Output;

import Utility.Log;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;

public class TextWriter implements OutputWriter {
    @Override
    public void write(SimulationOutput out, String file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(file), out);
        } catch (Exception e){
            Log.log("Error: Unable to write output JSON file!");
        }
    }
}
