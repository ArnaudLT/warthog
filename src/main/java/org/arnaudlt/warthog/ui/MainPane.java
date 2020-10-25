package org.arnaudlt.warthog.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.pane.control.ControlPane;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;


public class MainPane {

    private final ControlPane controlPane;

    private final ExplorerPane explorerPane;

    private final TransformPane transformPane;

    private final OutputPane outputPane;


    public MainPane(Stage stage, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.outputPane = new OutputPane(stage);
        this.transformPane = new TransformPane(stage, poolService);

        this.controlPane = new ControlPane(stage, namedDatasetManager, poolService);
        this.explorerPane = new ExplorerPane(stage);

        this.controlPane.setExplorerPane(this.explorerPane);
        this.controlPane.setTransformPane(this.transformPane);
        this.controlPane.setOutputPane(this.outputPane);
        this.explorerPane.setTransformPane(this.transformPane);
        this.explorerPane.setControlPane(this.controlPane);
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
        splitPane.setDividerPositions(0, 0.58, 0.42);

        return splitPane;
    }
}
