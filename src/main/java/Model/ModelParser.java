package Model;

import java.io.File;


public interface ModelParser {
    public abstract Model parse(File inputFile);
}
