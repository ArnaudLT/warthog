package org.arnaudlt.warthog.model.azure;

import com.azure.storage.file.datalake.models.PathItem;

public class AzurePathItem {

    protected final PathItem pathItem;


    public AzurePathItem(PathItem pathItem) {
        this.pathItem = pathItem;
    }


    public PathItem getPathItem() {
        return pathItem;
    }

}
