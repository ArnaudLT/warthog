package org.arnaudlt.warthog.ui.pane.control;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SettingsDialog {


    private final GlobalSettings globalSettings;

    private Stage dialog;


    @Autowired
    public SettingsDialog(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }


    public void buildSettingsDialog(Stage stage) {

        this.dialog = new Stage();
        this.dialog.setTitle("Settings");
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(stage);
        this.dialog.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        int i = 0;

        Label rowCountLabel = new Label("Number of rows to display :");
        TextField rowCount = new TextField(globalSettings.getNumberOfRowsToDisplay().toString());

        grid.addRow(i++, rowCountLabel, rowCount);

        Separator separatorOne = new Separator(Orientation.HORIZONTAL);
        grid.add(separatorOne, 0, i++, 2, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            // TODO check validity
            globalSettings.setNumberOfRowsToDisplay(Integer.parseInt(rowCount.getText()));

            // TODO persist ?

            dialog.close();
        });
        grid.addRow(i++, saveButton);

        Scene dialogScene = new Scene(grid, 500, 300);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showSettingsDialog() {

        dialog.show();
    }

}
