package org.arnaudlt.warthog.model.util;

import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.exception.ProcessingException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileUtil {


    private FileUtil() {}


    public static Format determineFormat(String[] filePath) {

        List<Path> filePaths = Arrays.stream(filePath)
                .map(Paths::get)
                .collect(Collectors.toList());

        return determineFormat(filePaths);
    }


    public static Format determineFormat(List<Path> filePaths) {

        String fileType = filePaths.stream()
                .map(f -> getLowerCaseExtension(f.getFileName().toString()))
                .dropWhile(ext -> Format.valueFromLabel(ext) == null)
                .findAny()
                .orElseThrow(() -> new ProcessingException("Not able to determine the file type"));
        return Format.valueFromLabel(fileType);
    }


    public static String getLowerCaseExtension(String fileName) {

        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }


    public static String inferSeparator(Format format, List<Path> filePaths) {

        if (format != Format.CSV) {
            return "";
        }
        return inferSeparator(filePaths);
    }


    public static String inferSeparator(List<Path> filePaths) {

        File fileToRead = findAnyCsvFile(filePaths);

        List<String> testedSeparator = List.of(",", ";", "\\t", "\\|", "\\$");
        for (String separator : testedSeparator) {

            if (isAValidSeparator(fileToRead, separator)) {
                switch (separator) {
                    case "\\|":
                        return "|";
                    case "\\$":
                        return "$";
                    case "\\t":
                        return "\t";
                    default:
                        return separator;
                }
            }
        }
        throw new ProcessingException(String.format("Not able to determine the delimiter for %s", fileToRead));
    }


    private static File findAnyCsvFile(List<Path> filePaths) {

        return filePaths.stream()
                .dropWhile(path -> !"csv".equals(getLowerCaseExtension(path.getFileName().toString())))
                .findAny()
                .orElseThrow(() -> new ProcessingException("Not able to find any csv file in the list"))
                .toFile();
    }


    private static boolean isAValidSeparator(File file, String separator) {

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
        } catch (IOException e) {
            throw new ProcessingException(String.format("Error while reading file %s", file.getName()), e);
        }
        return true;
    }


    public static double getSizeInMegaBytes(List<Path> filePaths) {

        return filePaths.stream()
                .mapToDouble(path -> path.toFile().length())
                .sum() / 1024d / 1024d;
    }
}
