package org.arnaudlt.warthog.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.history.WorkspaceHistory;
import org.arnaudlt.warthog.model.setting.ImportDatabaseTableSettings;
import org.arnaudlt.warthog.model.setting.ImportDirectorySettings;
import org.arnaudlt.warthog.model.setting.ImportSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.control.ControlPane;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromDatabaseService;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromLocalService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainPane {

    private final ControlPane controlPane;

    private final ExplorerPane explorerPane;

    private final TransformPane transformPane;

    private final OutputPane outputPane;

    private final NamedDatasetManager namedDatasetManager;

    private final WorkspaceHistory workspaceHistory;

    private final PoolService poolService;

    private Stage parent;


    @Autowired
    public MainPane(ControlPane controlPane, ExplorerPane explorerPane, TransformPane transformPane,
                    OutputPane outputPane, NamedDatasetManager namedDatasetManager, WorkspaceHistory workspaceHistory, PoolService poolService) {

        this.controlPane = controlPane;
        this.explorerPane = explorerPane;
        this.transformPane = transformPane;
        this.outputPane = outputPane;
        this.namedDatasetManager = namedDatasetManager;
        this.workspaceHistory = workspaceHistory;
        this.poolService = poolService;

        this.controlPane.setMainPane(this);
        this.explorerPane.setMainPane(this);
    }


    public Parent build(Stage stage) {

        parent = stage;
        Node controlNode = this.controlPane.buildControlPane(stage);

        Node explorerNode = this.explorerPane.buildExplorerPane(stage);
        Node transformNode = this.transformPane.buildTransformPane(stage);
        Node outputNode = this.outputPane.buildOutputPane(stage);

        loadPreviousWorkspace();

        SplitPane middleSplitPane = new SplitPane();
        middleSplitPane.setOrientation(Orientation.HORIZONTAL);
        middleSplitPane.getItems().addAll(explorerNode, transformNode);
        middleSplitPane.setDividerPositions(0.2);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(controlNode, middleSplitPane, outputNode);
        splitPane.setDividerPositions(0, 0.58, 0.42);

        return splitPane;
    }


    private void loadPreviousWorkspace() {

        for (ImportSettings is : workspaceHistory.getImportSettings()) {

            if (is instanceof ImportDirectorySettings ids) {

                importDirectory(ids);
            } else if (is instanceof ImportDatabaseTableSettings its) {

                importTable(its);
            }
        }
        workspaceHistory.getImportSettings().clear(); // useless to keep (?)
    }


    public void importDirectory(ImportDirectorySettings importDirectorySettings) {

        NamedDatasetImportFromLocalService importService = new NamedDatasetImportFromLocalService(poolService, namedDatasetManager, importDirectorySettings);
        importService.setOnSucceeded(success -> getExplorerPane().addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(parent, fail, "Not able to add the dataset '" + importDirectorySettings.getName() + "'"));
        importService.start();
    }


    public void importTable(ImportDatabaseTableSettings importDatabaseTableSettings) {

        NamedDatasetImportFromDatabaseService importService = new NamedDatasetImportFromDatabaseService(
                poolService, namedDatasetManager, importDatabaseTableSettings.connection(), importDatabaseTableSettings.tableName());
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(parent, fail, "Not able to import the dataset '" + importDatabaseTableSettings.tableName() + "'"));
        importService.start();
    }


    public TransformPane getTransformPane() {
        return this.transformPane;
    }

    public ControlPane getControlPane() {
        return this.controlPane;
    }

    public OutputPane getOutputPane() {
        return this.outputPane;
    }

    public ExplorerPane getExplorerPane() {
        return this.explorerPane;
    }



}
