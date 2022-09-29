package org.arnaudlt.warthog.model.azure;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class AzurePathItems implements Iterable<AzurePathItem> {

    private final List<AzurePathItem> items;


    public AzurePathItems() {
        this.items = new ArrayList<>();
    }


    public AzurePathItems(List<AzurePathItem> items) {
        this.items = items;
    }


    public boolean isEmpty() {

        return items.isEmpty();
    }


    @NotNull
    @Override
    public Iterator<AzurePathItem> iterator() {
        return items.iterator();
    }
}
