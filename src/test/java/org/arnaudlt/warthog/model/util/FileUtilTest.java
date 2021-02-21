package org.arnaudlt.warthog.model.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void getFileTypeParquetFile() throws IOException {

        String fileType = FileUtil.getFileType(new File("src/test/plop.parquet"));
        Assert.assertEquals("parquet", fileType);
    }

    @Test
    void getFileTypeCsvFile() throws IOException {

        String fileType = FileUtil.getFileType(new File("src/test/resources/code-insee-sample.csv"));
        Assert.assertEquals("csv", fileType);
    }

    @Test
    void getFileTypeOrcDirectory() throws IOException {

        String fileType = FileUtil.getFileType(new File("src/test/resources/covid19-orc"));
        Assert.assertEquals("orc", fileType);
    }

    @Test
    void getFileTypeCsvDirectory() throws IOException {

        String fileType = FileUtil.getFileType(new File("src/test/resources/first_join.csv"));
        Assert.assertEquals("csv", fileType);
    }
}