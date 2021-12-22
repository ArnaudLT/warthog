package org.arnaudlt.warthog.model.dataset.decoration;

import java.util.List;

public class LocalDecoration implements Decoration {

    private final String basePath;

    private final List<String> parts;

    private final String format;

    private final Double sizeInMegaBytes;


    public LocalDecoration(String basePath, List<String> parts, String format, Double sizeInMegaBytes) {

        this.basePath = basePath;
        this.parts = parts;
        this.format = format;
        this.sizeInMegaBytes = sizeInMegaBytes;
    }


    public String getBasePath() {
        return basePath;
    }


    public List<String> getParts() {
        return parts;
    }


    public String getFormat() {
        return format;
    }


    public Double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }
}
