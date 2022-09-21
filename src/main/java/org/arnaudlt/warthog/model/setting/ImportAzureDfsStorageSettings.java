package org.arnaudlt.warthog.model.setting;


public record ImportAzureDfsStorageSettings(String azContainer, String azDirectoryPath,
                                            String localDirectoryPath, String basePath, String name) {}
