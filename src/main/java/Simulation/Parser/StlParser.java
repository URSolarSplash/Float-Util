package Simulation.Parser;

import Simulation.Model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class StlParser implements ModelParser {
    @Override
    public Model parse(File inputFile) {
        try {
        Model resultModel = new Model();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String line = "";
        int p = 0;
        int facetId = 0;
        Triangle facet = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // trim excess space
            line = line.replaceAll("\\s+", " ");

            if (line.startsWith("solid")) { // Solid name
                //obj.name = line.split(" ")[1];
            } else if (line.startsWith("facet")) { // New facet
                facet = new Triangle();
                facet.normal = getNormal(line);
                //FloatUtilMain.log("FACE "+facetId+": ["+line+"]...");
                //FloatUtilMain.log("Started facet id "+(facetId));
            } else if (line.startsWith("endfacet")) { // End facet
                resultModel.addTriangle(facet);
                //FloatUtilMain.log("Finished facet id "+(facetId));
                facetId++;
            } else if (line.startsWith("outer")) { // New triangle
                p = 0;
            } else if (line.startsWith("vertex")) { // New vertex
                //FloatUtilMain.log("  VERTEX: ["+line+"]...");
                switch (p){
                    case 0: {
                        facet.setV1(getPoint(line));
                        break;
                    }
                    case 1: {
                        facet.setV2(getPoint(line));
                        break;
                    }
                    case 2: {
                        facet.setV3(getPoint(line));
                        break;
                    }
                }
                p++;
            } else if (line.startsWith("endloop")) { // Facet loop end

            }
        }

        return resultModel; }
        catch (Exception e){
            return null;
        }
    }

    private Vector3 getNormal(String facetline) throws NumberFormatException {
        String[] split = facetline.split(" ");
        double p1 = Double.parseDouble(split[2]);
        double p2 = Double.parseDouble(split[3]);
        double p3 = Double.parseDouble(split[4]);
        return new Vector3(p1, p2, p3);
    }

    private Vector3 getPoint(String pointline) throws NumberFormatException {

        String[] split = pointline.split(" ");
        double p1 = Double.parseDouble(split[1]);
        double p2 = Double.parseDouble(split[2]);
        double p3 = Double.parseDouble(split[3]);
        return new Vector3(p1, p2, p3);
    }

}
