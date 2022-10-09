package org.arnaudlt.warthog.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.DatasetCountRowsService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.ButtonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
@Component
public class OutputPane {


    private Stage owner;

    private final PoolService poolService;

    private final GlobalSettings globalSettings;

    private TabPane outputResultTabPane;



    @Autowired
    public OutputPane(PoolService poolService, GlobalSettings globalSettings) {

        this.poolService = poolService;
        this.globalSettings = globalSettings;
    }


    public Node buildOutputPane(Stage owner) {

        this.owner = owner;

        this.outputResultTabPane = new TabPane();
        this.outputResultTabPane.setSide(Side.TOP);
        this.outputResultTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        Button copyButton = ButtonFactory.buildSegoeButton("\uE8C8", "Copy all to clipboard", 14);
        copyButton.setOnAction(event -> copyAllToClipboard());

        Button countRowsButton = ButtonFactory.buildSegoeButton("\uF272", "Count rows", 14);
        countRowsButton.setOnAction(getDatasetCountRowsEventHandler());

        Button showQueryButton = ButtonFactory.buildSegoeButton("\uEC42", "Show query", 14);
        showQueryButton.setOnAction(getShowQueryEventHandler());

        Button showSchemaButton = ButtonFactory.buildSegoeButton("\ue822", "Show schema", 14);
        showSchemaButton.setOnAction(getDatasetShowSchemaEventHandler());

        VBox buttonBar = new VBox(copyButton, countRowsButton, showQueryButton, showSchemaButton);
        buttonBar.setFillWidth(true);

        HBox hBox = new HBox(buttonBar, this.outputResultTabPane);
        this.outputResultTabPane.prefWidthProperty().bind(hBox.widthProperty().subtract(buttonBar.widthProperty()));

        return hBox;
    }


    private void copyAllToClipboard() {

        OutputResultTab selectedOutputResultTab = getSelectedOutputResultTab();
        if (selectedOutputResultTab == null || selectedOutputResultTab.getTableView() == null) return;

        selectedOutputResultTab.copyAllToClipboard();
    }


    private OutputResultTab addOutputResultTab() {

        int openTabsCount = this.outputResultTabPane.getTabs().size();
        String tabName = "Output";
        if (openTabsCount > 0) {
            tabName = "Output (" + openTabsCount + ")";
        }

        OutputResultTab outputResultTab = new OutputResultTab();
        outputResultTab.build(tabName);
        this.outputResultTabPane.getTabs().add(outputResultTab);
        return outputResultTab;
    }


    private EventHandler<ActionEvent> getDatasetCountRowsEventHandler() {

        return event -> {

            OutputResultTab selectedOutputResultTab = getSelectedOutputResultTab();
            if (selectedOutputResultTab == null || selectedOutputResultTab.getPreparedDataset() == null) return;

            DatasetCountRowsService datasetCountRowsService = new DatasetCountRowsService(poolService, selectedOutputResultTab.getPreparedDataset().dataset());
            datasetCountRowsService.setOnSucceeded(success ->
                AlertFactory.showInformationAlert(owner, "Number of rows : " + String.format(Locale.US,"%,d", datasetCountRowsService.getValue())));
            datasetCountRowsService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Failed to count rows"));
            datasetCountRowsService.start();
        };
    }


    private EventHandler<ActionEvent> getDatasetShowSchemaEventHandler() {

        return event -> {

            OutputResultTab selectedOutputResultTab = getSelectedOutputResultTab();
            if (selectedOutputResultTab == null || selectedOutputResultTab.getPreparedDataset() == null) return;
            AlertFactory.showInformationAlert(owner, "Schema : ", selectedOutputResultTab.getPreparedDataset().dataset().schema().prettyJson());
        };
    }


    private EventHandler<ActionEvent> getShowQueryEventHandler() {

        return event -> {

            OutputResultTab selectedOutputResultTab = getSelectedOutputResultTab();
            if (selectedOutputResultTab == null || selectedOutputResultTab.getPreparedDataset() == null) return;
            AlertFactory.showInformationAlert(owner, "SQL query : ", selectedOutputResultTab.getPreparedDataset().sqlQuery());
        };
    }


    private OutputResultTab getSelectedOutputResultTab() {

        return (OutputResultTab) this.outputResultTabPane.getSelectionModel().getSelectedItem();
    }


    public void fill(PreparedDataset preparedDataset) {

        OutputResultTab outputResultTabToFill = lastUnpinnedOutputResultTab();
        outputResultTabToFill.clear();

        outputResultTabToFill.setPreparedDataset(preparedDataset);
        List<Map<String, String>> rows = preparedDataset.overview();

        boolean truncateAfterEnabled = globalSettings.getOverview().getTruncateAfter() != 0;

        for (StructField field : preparedDataset.dataset().schema().fields()) {

            TableColumn<Map<String,String>, Object> col = new TableColumn<>(field.name());
            col.setCellValueFactory(param -> {

                String rawValue = param.getValue().get(field.name());

                if (truncateAfterEnabled && rawValue.length() > globalSettings.getOverview().getTruncateAfter()) {

                    rawValue = rawValue.substring(0, globalSettings.getOverview().getTruncateAfter()).concat("...");
                }
                return new SimpleObjectProperty<>(rawValue);
            });
            outputResultTabToFill.getTableView().getColumns().add(col);
        }

        outputResultTabToFill.getTableView().getItems().addAll(rows);
        this.outputResultTabPane.getSelectionModel().select(outputResultTabToFill);
    }


    private OutputResultTab lastUnpinnedOutputResultTab() {

        return outputResultTabPane.getTabs().stream()
                .map(OutputResultTab.class::cast)
                .filter(Predicate.not(OutputResultTab::isPin))
                .findFirst()
                .orElseGet(this::addOutputResultTab);
    }

}
