package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.util.Format;

public class ImportDirectorySettings {

    private final String filePath;

    private final Format format;

    private final String name;

    // CSV only
    private final String separator;

    // CSV only
    private final Boolean header;

    // Advanced
    private final String basePath;


    public ImportDirectorySettings(String filePath, Format format, String name, String separator, Boolean header, String basePath) {
        this.filePath = filePath;
        this.format = format;
        this.name = name;
        this.separator = separator;
        this.header = header;
        this.basePath = basePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public Format getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public String getSeparator() {
        return separator;
    }

    public Boolean getHeader() {
        return header;
    }

    public String getBasePath() {
        return basePath;
    }
}
