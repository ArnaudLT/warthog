package org.arnaudlt.projectdse.model.dataset;

public class Decoration {

    private final double sizeInMegaBytes;

    private final String separator;


    public Decoration(double sizeInMegaBytes, String separator) {
        this.sizeInMegaBytes = sizeInMegaBytes;
        this.separator = separator;
    }


    public double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }


    public String getSeparator() {
        return separator;
    }
}
