package org.arnaudlt.warthog.ui.pane.control;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.explorer.NamedDatasetImportService;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
public class ControlPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlPane.class);

    private final Stage stage;

    private NamedDatasetManager namedDatasetManager;

    private PoolService poolService;

    private ExplorerPane explorerPane;

    private TransformPane transformPane;

    private OutputPane outputPane;


    public ControlPane(Stage stage, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.stage = stage;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
    }


    public Node buildControlPane() {

        MenuBar menuBar = buildMenuBar();

        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(poolService.tickTackProperty().greaterThan(0));

        HBox hBox = new HBox(10, menuBar, progressBar);
        hBox.setMaxHeight(30);
        hBox.setMinHeight(30);
        hBox.setAlignment(Pos.BASELINE_LEFT); // bas

        return hBox;
    }


    private MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");

        MenuItem openFileItem = new MenuItem("Import file...");
        openFileItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+I"));
        openFileItem.setOnAction(requestImportFile);

        MenuItem openParquetItem = new MenuItem("Import Parquet...");
        openParquetItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+SHIFT+I"));
        openParquetItem.setOnAction(requestImportFolder);

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+D"));
        deleteItem.setOnAction(requestDelete);

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+S"));
        settingsItem.setOnAction(getSettingsActionEventHandler());

        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        fileMenu.getItems().addAll(openFileItem, openParquetItem, separator1, settingsItem, separator2, deleteItem);

        Menu runMenu = new Menu("Run");

        MenuItem overviewItem = new MenuItem("Overview");
        overviewItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+O"));
        overviewItem.setOnAction(getOverviewActionEventHandler());

        MenuItem exportItem = new MenuItem("Export CSV...");
        exportItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+E"));
        exportItem.setOnAction(getExportActionEventHandler());

        runMenu.getItems().addAll(overviewItem, exportItem);

        return new MenuBar(fileMenu, runMenu);
    }


    private EventHandler<ActionEvent> getSettingsActionEventHandler() {

        return actionEvent -> {

            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this.stage);
            HBox dialogHBox = new HBox(new Text("Coming soon :-)"));

            Scene dialogScene = new Scene(dialogHBox, 800, 450);
            JMetro metro = new JMetro(Style.DARK);
            metro.setAutomaticallyColorPanes(true);
            metro.setScene(dialogScene);
            dialog.setScene(dialogScene);
            dialog.show();
        };
    }


    private EventHandler<ActionEvent> getOverviewActionEventHandler() {

        return event -> {

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) return;
            NamedDatasetOverviewService overviewService = new NamedDatasetOverviewService(selectedNamedDataset);
            overviewService.setOnSucceeded(success -> this.outputPane.fill(overviewService.getValue()));
            overviewService.setOnFailed(fail -> failToGenerate(selectedNamedDataset, "overview"));
            overviewService.setExecutor(poolService.getExecutor());
            overviewService.start();
        };
    }


    private EventHandler<ActionEvent> getExportActionEventHandler() {

        return event -> {

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) return;

            FileChooser fc = new FileChooser();
            File exportFile = fc.showSaveDialog(this.stage);

            if (exportFile == null) return;
            String filePath = exportFile.getAbsolutePath();

            NamedDatasetExportService exportService = new NamedDatasetExportService(selectedNamedDataset, filePath);
            exportService.setOnSucceeded(success -> LOGGER.info("Export success !"));
            exportService.setOnFailed(fail -> failToGenerate(selectedNamedDataset, "export"));
            exportService.setExecutor(poolService.getExecutor());
            exportService.start();
        };
    }


    private void failToGenerate(NamedDataset namedDataset, String context) {

        Alert namedDatasetExportAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        namedDatasetExportAlert.setHeaderText(String.format("Not able to generate an %s for the dataset '%s'",
                context, namedDataset.getName()));
        namedDatasetExportAlert.setContentText("Please check the logs ... and cry");
        namedDatasetExportAlert.show();
    }


    private final EventHandler<ActionEvent> requestImportFile = actionEvent -> {

        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(this.getStage());
        if (files != null) {

            for (File file : files) {

                importFile(file);
            }
        }
    };


    private final EventHandler<ActionEvent> requestImportFolder = actionEvent -> {

        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(this.getStage());
        if (file != null) {

            importFile(file);
        }
    };


    public void importFile(File file) {

        NamedDatasetImportService importService = new NamedDatasetImportService(namedDatasetManager, file);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> failToImport(file));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }


    private void failToImport(File file) {

        Alert datasetCreationAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        datasetCreationAlert.setHeaderText(String.format("Not able to add the dataset '%s'", file.getName()));
        datasetCreationAlert.setContentText("Please check the file format and its integrity");
        datasetCreationAlert.show();
    }


    private final EventHandler<ActionEvent> requestDelete = actionEvent -> {

        Set<NamedDataset> selectedItems = this.explorerPane.getSelectedItems();
        for (NamedDataset selectedNamedDataset : selectedItems) {

            log.info("Request to close named dataset {}", selectedNamedDataset.getName());
            this.transformPane.closeNamedDataset(selectedNamedDataset);
            this.explorerPane.removeNamedDataset(selectedNamedDataset);
            this.namedDatasetManager.deregisterNamedDataset(selectedNamedDataset);
        }
    };


    public void setExplorerPane(ExplorerPane explorerPane) {
        this.explorerPane = explorerPane;
    }


    public void setTransformPane(TransformPane transformPane) {
        this.transformPane = transformPane;
    }


    public void setOutputPane(OutputPane outputPane) {
        this.outputPane = outputPane;
    }


    public Stage getStage() {
        return stage;
    }
}
