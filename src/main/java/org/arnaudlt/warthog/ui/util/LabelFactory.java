package org.arnaudlt.warthog.ui.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class LabelFactory {


    private LabelFactory() {}


    public static Label buildSegoeLabel(String text) {

        return buildSegoeLabel(text, "MidnightBlue");
    }


    public static Label buildSegoeLabel(String text, String color) {

        Label label = new Label(text);
        label.setAlignment(Pos.BASELINE_LEFT);
        label.setStyle("-fx-font-family: 'Segoe MDL2 Assets'; -fx-text-fill: " +color+"; -fx-font-size: 14px;");
        return label;
    }




}
