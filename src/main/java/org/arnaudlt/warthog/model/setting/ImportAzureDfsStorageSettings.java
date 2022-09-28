package org.arnaudlt.warthog.model.setting;


import org.arnaudlt.warthog.model.azure.AzurePathItem;

import java.util.List;

public record ImportAzureDfsStorageSettings(String azContainer, String azDirectoryPath, List<AzurePathItem> azPathItems,
                                            String localDirectoryPath, String basePath, String name) {}
