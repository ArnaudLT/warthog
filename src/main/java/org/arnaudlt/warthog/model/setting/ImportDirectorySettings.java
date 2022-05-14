package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.util.Format;

import java.nio.file.Path;
import java.util.List;

public class ImportDirectorySettings {

    private final String[] filePath;

    private final Format format;

    private final String name;

    // CSV only
    private final String separator;

    // CSV only
    private final Boolean header;

    // JSON only
    private final Boolean multiLine;

    // Advanced
    private final String basePath;


    public ImportDirectorySettings(String filePath, Format format, String name, String separator, Boolean header,
                                   Boolean multiLine, String basePath) {
        this.filePath = new String[]{filePath};
        this.format = format;
        this.name = name;
        this.separator = separator;
        this.header = header;
        this.multiLine = multiLine;
        this.basePath = basePath;
    }

    public ImportDirectorySettings(List<Path> filePaths, Format format, String name, String separator, Path basePath) {

        this.filePath = filePaths.stream()
                .map(Path::toString)
                .toArray(String[]::new);
        this.format = format;
        this.name = name;
        this.separator = separator;
        this.header = Boolean.TRUE;
        this.multiLine = Boolean.FALSE;
        this.basePath = basePath.toString();
    }

    public String[] getFilePath() {
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

    public Boolean getMultiLine() {
        return multiLine;
    }

    public String getBasePath() {
        return basePath;
    }
}
