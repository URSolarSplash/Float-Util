package Parser;

import java.util.HashMap;

public class ModelParserLibrary {
    public HashMap<String,ModelParser> parsers;

    public ModelParserLibrary(){
        parsers = new HashMap<String, ModelParser>();
        //Register all the implementations of modelParser in this path
        parsers.put("stl",new StlParser());
    }

    public ModelParser getParser(String extension){
        return parsers.get(extension.toLowerCase());
    }
}
