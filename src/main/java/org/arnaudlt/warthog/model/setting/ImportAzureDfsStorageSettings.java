package org.arnaudlt.warthog.model.setting;

import lombok.Getter;

@Getter
public class ImportAzureDfsStorageSettings {

    private final String container;

    private final String azDirectoryPath;

    private final String localDirectoryPath;


    public ImportAzureDfsStorageSettings(String container, String azDirectoryPath, String localDirectoryPath) {

        this.container = container;
        this.azDirectoryPath = azDirectoryPath;
        this.localDirectoryPath = localDirectoryPath;
    }


}
