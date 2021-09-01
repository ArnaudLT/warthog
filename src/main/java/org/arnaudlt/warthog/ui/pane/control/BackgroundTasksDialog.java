package org.arnaudlt.warthog.ui.pane.control;

import javafx.concurrent.Service;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackgroundTasksDialog {


    private Stage dialog;

    private final PoolService poolService;


    @Autowired
    public BackgroundTasksDialog(PoolService poolService) {

        this.poolService = poolService;
    }


    public void buildBackgroundTasksDialog(Stage owner) {

        this.dialog = StageFactory.buildModalStage(owner, "Background tasks", Modality.NONE, StageStyle.DECORATED, true);
        dialog.setWidth(320);

        ListView<Service<?>> serviceListView = new ListView<>(poolService.getServices());
        serviceListView.setCellFactory(x ->new ServiceCell());
        serviceListView.setPlaceholder(new Label("No background tasks are running"));

        Scene dialogScene = StageFactory.buildScene(serviceListView);
        dialog.setScene(dialogScene);
    }


    public void showTasksManagerDialog() {

        this.dialog.show();
    }


    //https://stackoverflow.com/questions/15661500/javafx-listview-item-with-an-image-button
    @Slf4j
    private static class ServiceCell extends ListCell<Service<?>> {

        private final VBox content;

        private final Label label;

        private final ProgressIndicator progressIndicator;

        private final Button cancelButton;


        public ServiceCell() {

            super();

            this.label = new Label();
            this.label.setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);
            Pane pane = new Pane();
            this.cancelButton = new Button("", new MDL2IconFont("\uF78A"));

            HBox hBox = new HBox(this.label, pane, cancelButton);
            hBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane, Priority.ALWAYS);

            this.progressIndicator = new ProgressBar();
            this.content = new VBox(0, hBox, this.progressIndicator);
            this.content.setPrefWidth(300);

            setStyle("-fx-padding: 0px");
        }


        @Override
        protected void updateItem(Service<?> item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                setGraphic(null);
            } else {

                this.label.textProperty().unbind();
                this.progressIndicator.progressProperty().unbind();

                this.label.textProperty().bind(item.messageProperty());
                this.progressIndicator.progressProperty().bind(item.progressProperty());
                this.cancelButton.setOnAction(event -> {
                    log.info("Request cancel task : {}", item);
                    item.cancel();
                });

                setGraphic(content);
            }
        }



    }

}
