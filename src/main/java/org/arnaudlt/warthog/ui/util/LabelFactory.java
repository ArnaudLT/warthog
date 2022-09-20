package org.arnaudlt.warthog.ui.util;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class LabelFactory {


    private static final Font SEGOE_MDL2_FONT = new Font("Segoe MDL2 Assets", 12);


    private LabelFactory() {}


    public static Label buildSegoeLabel(String text) {

        Label label = new Label(text);
        label.setFont(SEGOE_MDL2_FONT);
        return label;
    }
}
