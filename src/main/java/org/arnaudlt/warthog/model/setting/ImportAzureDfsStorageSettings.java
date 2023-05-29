package org.arnaudlt.warthog.model.setting;


import org.arnaudlt.warthog.model.azure.AzurePathItems;

import java.util.List;

public record ImportAzureDfsStorageSettings(String azContainer, List<String> azDirectoryPaths, AzurePathItems azPathItems,
                                            String localDirectoryPath, String basePath, String name) {}
