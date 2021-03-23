package org.arnaudlt.warthog.model.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.arnaudlt.warthog.model.exception.ProcessingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class FileUtil {


    private FileUtil() {}


    public static Format getFileType(File file) throws IOException {

        if (file.isDirectory()) {

            try (Stream<Path> walk = Files.walk(file.toPath(), FileVisitOption.FOLLOW_LINKS)) {

                String fileType = walk
                        .map(Path::toFile)
                        .dropWhile(File::isDirectory)
                        .map(FileUtil::getLowerCaseExtension)
                        .dropWhile(ext -> Format.valueFromLabel(ext) == null)
                        .findAny()
                        .orElseThrow(() -> new ProcessingException(String.format("Not able to determine the file type of %s", file)));
                log.info("A directory with {} files inside", fileType);
                return Format.valueFromLabel(fileType);
            }

        } else if (file.getName().contains(".")) {

            String fileType = getLowerCaseExtension(file);
            log.info("A directory with {} files inside", fileType);
            return Format.valueFromLabel(fileType);
        }

        throw new ProcessingException(String.format("Not able to determine the file type of %s", file));
    }


    public static String getLowerCaseExtension(File file) {

        return file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
    }


    public static String inferSeparator(File file) throws IOException {

        File fileToRead = findAnyCsvFile(file);

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


    private static File findAnyCsvFile(File file) throws IOException {

        if (file.isDirectory()) {

            try (Stream<Path> walk = Files.walk(file.toPath(), FileVisitOption.FOLLOW_LINKS)) {

                File anyCsv = walk
                        .map(Path::toFile)
                        .dropWhile(File::isDirectory)
                        .dropWhile(f -> !"csv".equals(getLowerCaseExtension(f)))
                        .findAny()
                        .orElseThrow(() -> new ProcessingException(String.format("Not able to find any csv file in directory %s ", file)));
                log.info("{} has been found in {}", anyCsv, file);
                return anyCsv;
            }
        }
        return file;
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


    public static double getSizeInMegaBytes(File file) {

        if (file.isDirectory()) {

            return FileUtils.sizeOfDirectory(file) / 1024d / 1024d;
        } else {
            return file.length() / 1024d / 1024d;
        }
    }
}
