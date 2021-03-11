package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.ui.pane.alert.AlertError;
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

        Label spark = new Label("Spark");
        spark.setFont(Font.font(18));
        Label needToRestart = new Label("(need to restart Warthog)");
        grid.addRow(i++, spark, needToRestart);

        Label sparkThreadsLabel = new Label("Threads :");
        TextField sparkThreads = new TextField(globalSettings.getSparkThreads().toString());
        grid.addRow(i++, sparkThreadsLabel, sparkThreads);

        Label sparkUILabel = new Label("Monitoring UI :");
        ComboBox<String> sparkUI = new ComboBox<>(FXCollections.observableArrayList("true", "false"));
        sparkUI.setValue(Boolean.TRUE.equals(globalSettings.getSparkUI()) ? "true" : "false");
        grid.addRow(i++, sparkUILabel, sparkUI);

        Separator s1 = new Separator(Orientation.HORIZONTAL);
        grid.add(s1, 0, i++, 2, 1);

        Label overview = new Label("Overview");
        overview.setFont(Font.font(18));
        grid.addRow(i++, overview);

        Label rowsLabel = new Label("Rows :");
        TextField rows = new TextField(globalSettings.getOverviewRows().toString());
        grid.addRow(i++, rowsLabel, rows);

        Separator s3 = new Separator(Orientation.HORIZONTAL);
        grid.add(s3, 0, i++, 2, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            try {

                globalSettings.setOverviewRows(Integer.parseInt(rows.getText()));
                globalSettings.setSparkThreads(Integer.parseInt(sparkThreads.getText()));
                globalSettings.setSparkUI(Boolean.parseBoolean(sparkUI.getValue()));
                GlobalSettings.serialize(globalSettings);
            } catch (Exception e) {
                AlertError.showFailureAlert(e, "Unable to save settings");
                return;
            }
            dialog.close();
        });
        grid.addRow(i, saveButton);

        Scene dialogScene = new Scene(grid, 300, 300);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showSettingsDialog() {

        dialog.show();
    }

}
