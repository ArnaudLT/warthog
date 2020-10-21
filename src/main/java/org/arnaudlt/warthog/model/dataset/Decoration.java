package org.arnaudlt.warthog.model.dataset;

import java.nio.file.Path;

public class Decoration {

    private final Path filePath;

    private final double sizeInMegaBytes;

    private final String separator;


    public Decoration(Path filePath, double sizeInMegaBytes, String separator) {

        this.filePath = filePath;
        this.sizeInMegaBytes = sizeInMegaBytes;
        this.separator = separator;
    }


    public Path getFilePath() {
        return filePath;
    }


    public double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }


    public String getSeparator() {
        return separator;
    }
}
