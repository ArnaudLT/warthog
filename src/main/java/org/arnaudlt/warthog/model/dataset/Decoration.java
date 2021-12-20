package org.arnaudlt.warthog.model.dataset;

import org.arnaudlt.warthog.model.util.Format;

import java.util.List;

public class Decoration {

    private final Format format;

    private final String basePath;

    private final List<String> parts;

    private final Double sizeInMegaBytes;


    public Decoration(Format format, String basePath, List<String> parts, Double sizeInMegaBytes) {

        this.format = format;
        this.basePath = basePath;
        this.parts = parts;
        this.sizeInMegaBytes = sizeInMegaBytes;
    }


    public Format getFormat() {
        return format;
    }


    public String getBasePath() {
        return basePath;
    }


    public List<String> getParts() {
        return parts;
    }


    public Double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }


    public String getFormatAsString() {
        if (format == null) {
            return "N/A";
        }
        return format.name();
    }
}
