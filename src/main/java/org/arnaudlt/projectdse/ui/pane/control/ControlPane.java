package org.arnaudlt.projectdse.ui.pane.control;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import org.arnaudlt.projectdse.PoolService;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;
import org.arnaudlt.projectdse.ui.pane.output.OutputPane;
import org.arnaudlt.projectdse.ui.pane.transform.TransformPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ControlPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlPane.class);

    private final Stage stage;

    private final PoolService poolService;

    private TransformPane transformPane;

    private OutputPane outputPane;


    public ControlPane(Stage stage, PoolService poolService) {

        this.stage = stage;
        this.poolService = poolService;
    }


    public Node buildControlPane() {

        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(poolService.tickTackProperty().greaterThan(0));

        Button settingsButton = new Button("_Settings", new MDL2IconFont("\uE713"));
        settingsButton.setOnAction(getSettingsActionEventHandler());

        Button overviewButton = new Button("_Overview", new MDL2IconFont("\uE7B3"));
        overviewButton.setOnAction(getOverviewActionEventHandler());

        Button exportButton = new Button("_Export", new MDL2IconFont("\uE74E"));
        exportButton.setOnAction(getExportActionEventHandler());

        HBox hbox = new HBox(2, progressBar, settingsButton, overviewButton, exportButton);
        hbox.setAlignment(Pos.BASELINE_CENTER);

        hbox.setMinHeight(27);
        hbox.setMaxHeight(27);
        return hbox;
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
            overviewService.setOnSucceeded(success -> {
                this.outputPane.fillOutput(overviewService.getValue());
                this.outputPane.fillSecret(overviewService.getValue());}
            );
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


    public void setTransformPane(TransformPane transformPane) {
        this.transformPane = transformPane;
    }


    public void setOutputPane(OutputPane outputPane) {
        this.outputPane = outputPane;
    }
}
