package org.arnaudlt.warthog.model.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

class FileUtilTest {

    @Test
    void getFileTypeParquetFile() {

        Format fileType = FileUtil.getFileType(List.of(Paths.get("src/test/plop.parquet")));
        Assert.assertEquals(Format.PARQUET, fileType);
    }

    @Test
    void getFileTypeCsvFile() {

        Format fileType = FileUtil.getFileType(List.of(Paths.get("src/test/resources/code-insee-sample.csv")));
        Assert.assertEquals(Format.CSV, fileType);
    }

    @Test
    void getFileTypeOrcDirectory() {

        Format fileType = FileUtil.getFileType(List.of(Paths.get("src/test/resources/covid19-orc")));
        Assert.assertEquals(Format.ORC, fileType);
    }

    @Test
    void getFileTypeCsvDirectory() {

        Format fileType = FileUtil.getFileType(List.of(Paths.get("src/test/resources/first_join.csv")));
        Assert.assertEquals(Format.CSV, fileType);
    }
}