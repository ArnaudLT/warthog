package org.arnaudlt.warthog.ui.util;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class ButtonFactory {



    private ButtonFactory() {}


    public static Button buildSegoeButton(String text, String tooltipText, int size) {

        return buildSegoeButton(text, tooltipText, size, "darkblue");
    }


    public static Button buildSegoeButton(String text, String tooltipText, int size, String color) {

        Button button = new Button(text);
        button.setAlignment(Pos.BASELINE_LEFT);
        button.setStyle("-fx-font-family: 'Segoe MDL2 Assets'; -fx-text-fill: "+color+"; -fx-font-size: "+size+"px;");
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(400));
        button.setTooltip(tooltip);
        return button;
    }
}
