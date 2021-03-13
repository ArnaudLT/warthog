package org.arnaudlt.warthog.model.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void getFileTypeParquetFile() throws IOException {

        Format fileType = FileUtil.getFileType(new File("src/test/plop.parquet"));
        Assert.assertEquals(Format.PARQUET, fileType);
    }

    @Test
    void getFileTypeCsvFile() throws IOException {

        Format fileType = FileUtil.getFileType(new File("src/test/resources/code-insee-sample.csv"));
        Assert.assertEquals(Format.CSV, fileType);
    }

    @Test
    void getFileTypeOrcDirectory() throws IOException {

        Format fileType = FileUtil.getFileType(new File("src/test/resources/covid19-orc"));
        Assert.assertEquals(Format.ORC, fileType);
    }

    @Test
    void getFileTypeCsvDirectory() throws IOException {

        Format fileType = FileUtil.getFileType(new File("src/test/resources/first_join.csv"));
        Assert.assertEquals(Format.CSV, fileType);
    }
}