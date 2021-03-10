package org.arnaudlt.warthog.model.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalSettings {


    private Integer numberOfRowsToDisplay;

    @Autowired
    public GlobalSettings() {
        this.numberOfRowsToDisplay = 50;
    }


    public Integer getNumberOfRowsToDisplay() {
        return numberOfRowsToDisplay;
    }

    public void setNumberOfRowsToDisplay(Integer numberOfRowsToDisplay) {
        this.numberOfRowsToDisplay = numberOfRowsToDisplay;
    }
}
