package org.arnaudlt.warthog.model.dataset.decoration;

import org.arnaudlt.warthog.model.util.Format;

import java.util.List;

public class LocalDecoration implements Decoration {

    private final String basePath;

    private final List<String> parts;

    private final Format format;

    private final Double sizeInMegaBytes;


    public LocalDecoration(String basePath, List<String> parts, Format format, Double sizeInMegaBytes) {

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


    public Format getFormat() {
        return format;
    }


    public Double getSizeInMegaBytes() {
        return sizeInMegaBytes;
    }
}
