package org.arnaudlt.warthog.model.setting;


import org.arnaudlt.warthog.model.azure.AzurePathItems;

public record ImportAzureDfsStorageSettings(String azContainer, String azDirectoryPath, AzurePathItems azPathItems,
                                            String localDirectoryPath, String basePath, String name) {}
