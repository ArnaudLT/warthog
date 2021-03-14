package org.arnaudlt.warthog.ui.util;

import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlertError {


    private AlertError() {
    }

    public static void showFailureAlert(WorkerStateEvent fail, String text) {

        showFailureAlert(fail.getSource().getException(), text);
    }


    public static void showFailureAlert(Throwable exception, String text) {

        log.error(text, exception);
        Alert countRowsAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        countRowsAlert.setHeaderText(text);
        TextArea stack = new TextArea(exception.toString());
        countRowsAlert.getDialogPane().setContent(stack);
        countRowsAlert.show();
    }

}
