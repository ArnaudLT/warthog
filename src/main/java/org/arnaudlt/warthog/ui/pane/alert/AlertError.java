package org.arnaudlt.warthog.ui.pane.alert;

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

        log.error(text, fail.getSource().getException());
        Alert countRowsAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        countRowsAlert.setHeaderText(text);
        TextArea stack = new TextArea(fail.getSource().getException().toString());
        countRowsAlert.getDialogPane().setContent(stack);
        countRowsAlert.show();
    }
}
