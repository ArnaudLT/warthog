package org.arnaudlt.projectdse.ui.pane.output;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class CustomLogAppender extends WriterAppender {


    private static final ListView<String> logArea = new ListView<>();

    static {

        logArea.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logArea.setFixedCellSize(31);
    }


    @Override
    public void append(LoggingEvent event) {

        final String message = this.layout.format(event);
        Platform.runLater(() -> {

            int lineCount = logArea.getItems().size();
            if (lineCount > 100) {

                logArea.getItems().remove(0);
            }
            logArea.getItems().add(message);
        });
    }


    public static ListView<String> getLogArea() {

        return logArea;
    }
}
