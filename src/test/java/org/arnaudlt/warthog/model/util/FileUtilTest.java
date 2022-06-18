package org.arnaudlt.warthog.model.util;


import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void getFileTypeParquetFile() {

        Format fileType = FileUtil.determineFormat(List.of(Paths.get("src/test/plop.parquet")));
        assertEquals(Format.PARQUET, fileType);
    }

    @Test
    void getFileTypeCsvFile() {

        Format fileType = FileUtil.determineFormat(List.of(Paths.get("src/test/resources/code-insee-sample.csv")));
        assertEquals(Format.CSV, fileType);
    }

    @Test
    void getFileTypeOrcDirectory() {

        Format fileType = FileUtil.determineFormat(List.of(
                Paths.get(".part-00000-64218e1e-0aae-4d0d-b9af-17fbe6fee7c1-c000.snappy.orc.crc"),
                Paths.get("src/test/resources/covid19-orc/part-00000-64218e1e-0aae-4d0d-b9af-17fbe6fee7c1-c000.snappy.orc")));
        assertEquals(Format.ORC, fileType);
    }

    @Test
    void getFileTypeCsvDirectory() {

        Format fileType = FileUtil.determineFormat(List.of(Paths.get("src/test/resources/first_join.csv")));
        assertEquals(Format.CSV, fileType);
    }
}