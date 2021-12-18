package org.arnaudlt.warthog.ui.pane.control;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

        GridPane generalGrid = GridFactory.buildGrid();
        int rowIndex = 0;

        Label rowsNumberLabel = new Label("Number of rows in overview :");
        Spinner<Integer> rowsNumber = new Spinner<>(1, 1000, globalSettings.getOverviewRows(), 1);
        rowsNumber.setEditable(true);
        generalGrid.addRow(rowIndex++, rowsNumberLabel, rowsNumber);

        Label truncateAfterLabel = new Label("Truncate after (chars) :");
        Spinner<Integer> truncateAfter = new Spinner<>(0, 1000, globalSettings.getOverviewTruncateAfter(), 1);
        truncateAfter.setEditable(true);
        truncateAfter.setTooltip(new Tooltip("Set to 0 if you don't want to truncate value"));
        generalGrid.addRow(rowIndex++, truncateAfterLabel, truncateAfter);

        Tab generalsTab = new Tab("Generals", generalGrid);

        GridPane sparkGrid = GridFactory.buildGrid();
        rowIndex = 0;

        Label sparkThreadsLabel = new Label("Threads :");
        Spinner<Integer> sparkThreads = new Spinner<>(1, 100, globalSettings.getSparkThreads(), 1);
        sparkGrid.addRow(rowIndex++, sparkThreadsLabel, sparkThreads);

        Label sparkUILabel = new Label("Monitoring UI :");
        Tooltip monitoringUI = new Tooltip("Accessible on http://localhost:4040");
        sparkUILabel.setTooltip(monitoringUI);
        CheckBox sparkUI = new CheckBox();
        sparkUI.setTooltip(monitoringUI);
        sparkUI.setSelected(globalSettings.getSparkUI());
        sparkGrid.addRow(rowIndex++, sparkUILabel, sparkUI);

        Tab sparkTab = new Tab("Spark", sparkGrid);


        TabPane tabPane = new TabPane(generalsTab, sparkTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GridPane bottom = GridFactory.buildGrid();
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            try {

                globalSettings.setOverviewRows(rowsNumber.getValue());
                globalSettings.setOverviewTruncateAfter(truncateAfter.getValue());
                globalSettings.setSparkThreads(sparkThreads.getValue());
                globalSettings.setSparkUI(sparkUI.isSelected());
                globalSettings.persist();
            } catch (Exception e) {
                AlertFactory.showFailureAlert(owner, e, "Unable to save settings");
                return;
            }
            dialog.close();
        });
        bottom.addRow(0, saveButton);

        VBox vBox = new VBox(tabPane, bottom);
        Scene dialogScene = StageFactory.buildScene(vBox);
        dialog.setScene(dialogScene);
    }


    public void showSettingsDialog() {

        dialog.show();
    }

}
