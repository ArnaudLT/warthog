package org.arnaudlt.warthog.model.dataset.decoration;

import java.util.List;

public class AzureDecoration extends LocalDecoration {


    private final String source;


    public AzureDecoration(String basePath, List<String> parts, String format, Double sizeInMegaBytes, String source) {
        super(basePath, parts, format, sizeInMegaBytes);
        this.source = source;
    }


    public String getSource() {
        return source;
    }

}
