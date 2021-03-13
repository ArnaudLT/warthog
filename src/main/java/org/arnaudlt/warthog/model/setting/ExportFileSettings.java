package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.util.Format;

public class ExportFileSettings {

    private final String filePath;

    private final Format format;

    private final String saveMode;

    // CSV only
    private final String separator;

    // CSV only
    private final Boolean header;


    public ExportFileSettings(String filePath, Format format, String saveMode, String separator, Boolean header) {
        this.filePath = filePath;
        this.format = format;
        this.saveMode = saveMode;
        this.separator = separator;
        this.header = header;
    }


    public String getFilePath() {
        return filePath;
    }


    public Format getFormat() {
        return format;
    }


    public String getSaveMode() {
        return saveMode;
    }


    public String getSeparator() {
        return separator;
    }


    public Boolean getHeader() {
        return header;
    }
}
