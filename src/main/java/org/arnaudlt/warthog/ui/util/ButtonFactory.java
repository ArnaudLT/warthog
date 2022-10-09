package org.arnaudlt.warthog.ui.util;


import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class ButtonFactory {



    private ButtonFactory() {}


    public static Button buildSegoeButton(String text, String tooltipText) {

        Button button = new Button(text);
        button.setStyle("-fx-font-family: 'Segoe MDL2 Assets';");
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(400));
        button.setTooltip(tooltip);
        return button;
    }

    public static Button buildSegoeButton(String text, String tooltipText, int size) {

        Button button = buildSegoeButton(text, tooltipText);
        button.setStyle(button.getStyle() + "-fx-font-size: "+size+"px;");
        return button;
    }
}
