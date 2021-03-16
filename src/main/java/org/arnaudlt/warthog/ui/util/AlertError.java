package org.arnaudlt.warthog.ui.util;

import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class AlertError {


    private AlertError() {
    }

    public static void showFailureAlert(WorkerStateEvent fail, String text) {

        showFailureAlert(fail.getSource().getException(), text);
    }


    public static void showFailureAlert(Throwable exception, String text) {

        log.error(text, exception);
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        alert.setHeaderText(text);
        TextArea stack = new TextArea(exception.toString());
        alert.getDialogPane().setExpandableContent(stack);
        alert.getDialogPane().setExpanded(true);
        alert.show();
    }


    public static Optional<ButtonType> showConfirmationAlert(String text) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(text);
        return alert.showAndWait();
    }
}
