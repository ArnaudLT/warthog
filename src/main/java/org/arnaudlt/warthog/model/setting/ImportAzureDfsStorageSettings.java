package org.arnaudlt.warthog.model.setting;

import lombok.Getter;

@Getter
public class ImportAzureDfsStorageSettings {

    private final String azContainer;

    private final String azDirectoryPath;

    private final String localDirectoryPath;

    private final String basePath;


    public ImportAzureDfsStorageSettings(String azContainer, String azDirectoryPath, String localDirectoryPath, String basePath) {

        this.azContainer = azContainer;
        this.azDirectoryPath = azDirectoryPath;
        this.localDirectoryPath = localDirectoryPath;
        this.basePath = basePath;
    }


}
