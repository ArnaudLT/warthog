package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.util.Compression;
import org.arnaudlt.warthog.model.util.Format;

public record ExportFileSettings(String filePath, Format format,
                                 String saveMode, String partitionBy, int repartition,
                                 String separator, Boolean header,
                                 Compression compression) {}
