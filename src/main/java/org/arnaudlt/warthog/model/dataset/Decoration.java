package org.arnaudlt.warthog.model.dataset;

public class Decoration {

    private final String filePath;

    private final double sizeInMegaBytes;

    private final String separator;


    public Decoration(String filePath, double sizeInMegaBytes, String separator) {

        this.filePath = filePath;
        this.sizeInMegaBytes = sizeInMegaBytes;
        this.separator = separator;
    }


    public String getFilePath() {
        return filePath;
    }


    public double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }


    public String getSeparator() {
        return separator;
    }
}
