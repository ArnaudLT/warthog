package org.arnaudlt.warthog.ui.pane.control;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
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


    public void buildSettingsDialog(Stage owner) {

        this.dialog = StageFactory.buildModalStage(owner, "Settings");

        GridPane grid = GridFactory.buildGrid();

        int i = 0;

        Label spark = new Label("Spark");
        spark.setFont(Font.font(null, FontWeight.BOLD, null, 14));
        Label needToRestart = new Label("(need to restart Warthog)");
        grid.addRow(i++, spark, needToRestart);

        Label sparkThreadsLabel = new Label("Threads :");
        Spinner<Integer> sparkThreads = new Spinner<>(1, 100, globalSettings.getSparkThreads(), 1);
        grid.addRow(i++, sparkThreadsLabel, sparkThreads);

        Label sparkUILabel = new Label("Monitoring UI :");
        Tooltip monitoringUI = new Tooltip("Accessible on http://localhost:4040");
        sparkUILabel.setTooltip(monitoringUI);
        CheckBox sparkUI = new CheckBox();
        sparkUI.setTooltip(monitoringUI);
        sparkUI.setSelected(globalSettings.getSparkUI());
        grid.addRow(i++, sparkUILabel, sparkUI);

        Separator s1 = new Separator(Orientation.HORIZONTAL);
        grid.add(s1, 0, i++, 2, 1);

        Label overview = new Label("Overview");
        overview.setFont(Font.font(null, FontWeight.BOLD, null, 14));
        grid.addRow(i++, overview);

        Label rowsLabel = new Label("Rows :");
        Spinner<Integer> rows = new Spinner<>(1, 1000, globalSettings.getOverviewRows(), 1);
        rows.setEditable(true);
        grid.addRow(i++, rowsLabel, rows);

        Separator s3 = new Separator(Orientation.HORIZONTAL);
        grid.add(s3, 0, i++, 2, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            try {

                globalSettings.setOverviewRows(rows.getValue());
                globalSettings.setSparkThreads(sparkThreads.getValue());
                globalSettings.setSparkUI(sparkUI.isSelected());
                globalSettings.persist();
            } catch (Exception e) {
                AlertFactory.showFailureAlert(owner, e, "Unable to save settings");
                return;
            }
            dialog.close();
        });
        grid.addRow(i, saveButton);

        Scene dialogScene = StageFactory.buildScene(grid);
        dialog.setScene(dialogScene);
    }


    public void showSettingsDialog() {

        dialog.show();
    }

}
