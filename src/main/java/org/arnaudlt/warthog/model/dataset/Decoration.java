package org.arnaudlt.warthog.model.dataset;

public class Decoration {

    private final String filePath;

    private final double sizeInMegaBytes;


    public Decoration(String filePath, double sizeInMegaBytes) {

        this.filePath = filePath;
        this.sizeInMegaBytes = sizeInMegaBytes;
    }


    public String getFilePath() {
        return filePath;
    }


    public double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }


}
