package org.arnaudlt.warthog.ui.util;


import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class ButtonFactory {



    private ButtonFactory() {}


    public static Button buildSegoeButton(String text, String tooltipText) {

        Button button = new Button(text);
        button.setStyle("-fx-font-family: 'Segoe MDL2 Assets'");
        button.setTooltip(new Tooltip(tooltipText));
        return button;
    }

}
