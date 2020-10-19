package org.arnaudlt.projectdse.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.arnaudlt.projectdse.PoolService;
import org.arnaudlt.projectdse.model.dataset.NamedDatasetManager;
import org.arnaudlt.projectdse.ui.pane.control.ControlPane;
import org.arnaudlt.projectdse.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.projectdse.ui.pane.output.OutputPane;
import org.arnaudlt.projectdse.ui.pane.transform.TransformPane;


public class MainPane {

    private final ControlPane controlPane;

    private final ExplorerPane explorerPane;

    private final TransformPane transformPane;

    private final OutputPane outputPane;


    public MainPane(Stage stage, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.controlPane = new ControlPane(stage, namedDatasetManager, poolService);
        this.explorerPane = new ExplorerPane(stage);
        this.transformPane = new TransformPane(stage, namedDatasetManager);
        this.outputPane = new OutputPane(stage);

        this.explorerPane.setTransformPane(this.transformPane);
        this.controlPane.setExplorerPane(explorerPane);
        this.controlPane.setTransformPane(this.transformPane);
        this.controlPane.setOutputPane(this.outputPane);
    }


    public Parent build() {

        Node controlNode = this.controlPane.buildControlPane();
        Node explorerNode = this.explorerPane.buildExplorerPane();
        Node transformNode = this.transformPane.buildTransformPane();
        Node outputNode = this.outputPane.buildOutputPane();

        SplitPane middleSplitPane = new SplitPane();
        middleSplitPane.setOrientation(Orientation.HORIZONTAL);
        middleSplitPane.getItems().addAll(explorerNode, transformNode);
        middleSplitPane.setDividerPositions(0.226);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(controlNode, middleSplitPane, outputNode);
        splitPane.setDividerPositions(0, 0.6, 0.4);

        return splitPane;
    }
}
