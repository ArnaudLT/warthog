package org.arnaudlt.projectdse.model.util;

import org.arnaudlt.projectdse.model.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class FileUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);


    private FileUtil() {}


    public static String getFileType(String filename) {

        if (filename == null || !filename.contains(".")) {

            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    public static String inferSeparator(File file) throws IOException {

        List<String> testedSeparator = List.of(",", ";", "\\t", "\\|", "\\$");
        for (String separator : testedSeparator) {

            if (isAValidSeparator(file, separator)) {
                return separator;
            }
        }
        throw new ProcessingException(String.format("Not able to determine the delimiter for %s", file));
    }


    private static boolean isAValidSeparator(File file, String separator) throws IOException {

        String line;
        int lineIndex = 0;
        int columnCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            while ((line = reader.readLine()) != null && lineIndex < 10) {

                String[] split = line.split(separator, -1);
                if (split.length == 1) {
                    return false;
                }
                if (lineIndex == 0) {
                    columnCount = split.length;
                } else {
                    if (split.length != columnCount) {
                        return false;
                    }
                }
                lineIndex++;
            }
        }
        return true;
    }


    public static String determineName(String filename) {

        return filename.substring(0,filename.lastIndexOf("."))
                .replace(".", "-")
                .replace(" ", "_");
    }


    public static double getSizeInMegaBytes(File file) {

        return file.length() / 1024d / 1024d;
    }
}
