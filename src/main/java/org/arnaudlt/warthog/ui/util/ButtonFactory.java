package org.arnaudlt.warthog.ui.util;


import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

public class ButtonFactory {


    private static final Font SEGOE_MDL2_FONT = new Font("Segoe MDL2 Assets", 12);


    private ButtonFactory() {}


    public static Button buildSegoeButton(String text, String tooltipText) {

        Button button = new Button(text);
        button.setFont(SEGOE_MDL2_FONT);
        button.setTooltip(new Tooltip(tooltipText));
        return button;
    }


    public static Button buildSegoeButton(String text, String tooltipText, String color) {

        Button button = buildSegoeButton(text, tooltipText);
        button.setStyle("-fx-text-fill: "+color+";");
        return button;
    }

}
