package org.arnaudlt.warthog.ui.pane.control;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.ButtonFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;


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

        Label rowsNumberLabel = new Label("Rows in overview :");
        Spinner<Integer> rowsNumber = new Spinner<>(1, 1000, globalSettings.getOverview().getRows(), 1);
        rowsNumber.setEditable(true);
        generalGrid.add(rowsNumberLabel, 0, rowIndex);
        generalGrid.add(rowsNumber, 1, rowIndex, 2, 1);
        rowIndex++;

        Label truncateAfterLabel = new Label("Truncate after (chars) :");
        Spinner<Integer> truncateAfter = new Spinner<>(0, 1000, globalSettings.getOverview().getTruncateAfter(), 1);
        truncateAfter.setEditable(true);
        truncateAfter.setTooltip(new Tooltip("Set to 0 if you don't want to truncate value"));
        generalGrid.add(truncateAfterLabel, 0, rowIndex);
        generalGrid.add(truncateAfter, 1, rowIndex, 2, 1);
        rowIndex++;

        Label preferredDownloadDirectoryLabel = new Label("Pref. download directory :");
        TextField preferredDownloadDirectory = new TextField(globalSettings.getUser().getPreferredDownloadDirectory());
        Button preferredDownloadDirectoryButton = ButtonFactory.buildExplorerButton();
        preferredDownloadDirectoryButton.setOnAction(event -> {

            DirectoryChooser dc = new DirectoryChooser();
            File file = dc.showDialog(this.dialog);

            if (file == null) return;
            preferredDownloadDirectory.setText(file.getAbsolutePath());
        });
        generalGrid.addRow(rowIndex++, preferredDownloadDirectoryLabel, preferredDownloadDirectory, preferredDownloadDirectoryButton);

        Label preferredExportDirectoryLabel = new Label("Pref. export directory :");
        TextField preferredExportDirectory = new TextField(globalSettings.getUser().getPreferredExportDirectory());
        Button preferredExportDirectoryButton = ButtonFactory.buildExplorerButton();
        preferredExportDirectoryButton.setOnAction(event -> {

            DirectoryChooser dc = new DirectoryChooser();
            File file = dc.showDialog(this.dialog);

            if (file == null) return;
            preferredExportDirectory.setText(file.getAbsolutePath());
        });
        generalGrid.addRow(rowIndex++, preferredExportDirectoryLabel, preferredExportDirectory, preferredExportDirectoryButton);

        Tab generalsTab = new Tab("Generals", generalGrid);


        GridPane sparkGrid = GridFactory.buildGrid();
        rowIndex = 0;

        Label sparkThreadsLabel = new Label("Threads :");
        Spinner<Integer> sparkThreads = new Spinner<>(1, 100, globalSettings.getSpark().getThreads(), 1);
        sparkGrid.addRow(rowIndex++, sparkThreadsLabel, sparkThreads);

        Label sparkUILabel = new Label("Monitoring UI :");
        Tooltip monitoringUI = new Tooltip("Accessible on http://localhost:4040");
        sparkUILabel.setTooltip(monitoringUI);
        CheckBox sparkUI = new CheckBox();
        sparkUI.setTooltip(monitoringUI);
        sparkUI.setSelected(globalSettings.getSpark().getUi());
        sparkGrid.addRow(rowIndex++, sparkUILabel, sparkUI);

        Tab sparkTab = new Tab("Spark", sparkGrid);

        TabPane tabPane = new TabPane(generalsTab, sparkTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GridPane bottom = GridFactory.buildGrid();
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            try {

                globalSettings.getUser().setPreferredDownloadDirectory(preferredDownloadDirectory.getText());
                globalSettings.getUser().setPreferredExportDirectory(preferredExportDirectory.getText());
                globalSettings.getOverview().setRows(rowsNumber.getValue());
                globalSettings.getOverview().setTruncateAfter(truncateAfter.getValue());
                globalSettings.getSpark().setThreads(sparkThreads.getValue());
                globalSettings.getSpark().setUi(sparkUI.isSelected());
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
