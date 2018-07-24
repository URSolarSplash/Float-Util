package Utility;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PathUtilities {

    public static boolean isValidFile(String in){
        File inFile = new File(in);
        return inFile.exists() & inFile.isFile();
    }

    public static String getPath(String inFile){
        return FilenameUtils.getPath(inFile);
    }

    public static boolean isAbsolute(String inPath){
        return (new File(inPath)).isAbsolute();
    }

}
