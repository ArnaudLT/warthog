package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.util.Compression;
import org.arnaudlt.warthog.model.util.Format;

public class ExportFileSettings {

    private final String filePath;

    private final Format format;

    private final String saveMode;

    private final String partitionBy;

    private final int repartition;

    // CSV only
    private final String separator;

    // CSV only
    private final Boolean header;

    // PARQUET
    private final Compression compression;


    public ExportFileSettings(String filePath, Format format, String saveMode, String partitionBy, int repartition,
                              String separator, Boolean header, Compression compression) {
        this.filePath = filePath;
        this.format = format;
        this.saveMode = saveMode;
        this.partitionBy = partitionBy;
        this.repartition = repartition;
        this.separator = separator;
        this.header = header;
        this.compression = compression;
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


    public int getRepartition() {
        return repartition;
    }


    public String getPartitionBy() {
        return partitionBy;
    }


    public String getSeparator() {
        return separator;
    }


    public Boolean getHeader() {
        return header;
    }


    public Compression getCompression() {
        return compression;
    }
}
