package org.arnaudlt.warthog.ui.util;

import javafx.scene.control.Label;

public class LabelFactory {


    private LabelFactory() {}


    public static Label buildSegoeLabel(String text) {

        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Segoe MDL2 Assets';");
        return label;
    }


    public static Label buildSegoeLabel(String text, String color) {

        Label label = buildSegoeLabel(text);
        label.setStyle(label.getStyle() + "-fx-text-fill: "+color+";");
        return label;
    }
}
