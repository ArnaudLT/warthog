package org.arnaudlt.warthog.ui.util;

import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class AlertFactory {


    private AlertFactory() {}


    public static void showFailureAlert(Stage owner, WorkerStateEvent fail, String text) {

        showFailureAlert(owner, fail.getSource().getException(), text);
    }


    public static void showFailureAlert(Stage owner, Throwable exception, String text) {

        log.error(text, exception);
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        alert.setHeaderText("");
        alert.setContentText(text);
        TextArea stack = new TextArea(exception.toString());
        alert.getDialogPane().setExpandableContent(stack);
        alert.getDialogPane().setExpanded(true);
        alert.initOwner(owner);

        alert.show();
    }


    public static void showInformationAlert(Stage owner, String text) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);
        alert.setHeaderText("");
        alert.setContentText(text);
        alert.initOwner(owner);
        alert.show();
    }


    public static Optional<ButtonType> showConfirmationAlert(Stage owner, String text) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setHeaderText("");
        alert.setContentText(text);

        return alert.showAndWait();
    }
}
