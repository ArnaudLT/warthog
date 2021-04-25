package org.arnaudlt.warthog.ui.pane.control;

import javafx.concurrent.Service;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TasksManagerDialog {


    private  Stage dialog;

    private final ListView<Service<?>> serviceListView;


    @Autowired
    public TasksManagerDialog(PoolService poolService) {

        this.serviceListView = new ListView<>(poolService.getServices());
        this.serviceListView.setCellFactory(x ->new ServiceCell());
    }


    public void buildTasksManagerDialog(Stage owner) {

        dialog = StageFactory.buildModalStage(owner, "Tasks manager");

        Scene dialogScene = new Scene(this.serviceListView);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showTasksManagerDialog() {

        this.dialog.show();
    }


    //https://stackoverflow.com/questions/15661500/javafx-listview-item-with-an-image-button
    private static class ServiceCell extends ListCell<Service<?>> {

        private final VBox vbox;

        private final Label label;

        private final ProgressIndicator progressIndicator;


        public ServiceCell() {
            super();
            this.label = new Label();
            this.progressIndicator = new ProgressBar();
            this.vbox = new VBox(this.label, this.progressIndicator);
        }

        @Override
        protected void updateItem(Service<?> item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                setGraphic(null);
            } else {
                this.label.textProperty().bind(item.messageProperty());
                this.progressIndicator.progressProperty().bind(item.progressProperty());
                setGraphic(vbox);
            }
        }
    }

}
