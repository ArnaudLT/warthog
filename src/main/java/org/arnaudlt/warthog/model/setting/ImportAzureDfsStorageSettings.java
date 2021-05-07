package org.arnaudlt.warthog.model.setting;

import lombok.Getter;

import java.util.List;

@Getter
public class ImportAzureDfsStorageSettings {

    private final String azContainer;

    private final String azDirectoryPath;

    private final String localDirectoryPath;

    private final String basePath;

    private final List<String> listOfFiles;


    public ImportAzureDfsStorageSettings(String azContainer, String azDirectoryPath, String localDirectoryPath, String basePath, List<String> listOfFiles) {

        this.azContainer = azContainer;
        this.azDirectoryPath = azDirectoryPath;
        this.localDirectoryPath = localDirectoryPath;
        this.basePath = basePath;
        this.listOfFiles = listOfFiles;
    }


}
